import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { GuidesService } from '../../../core/services/guides.service';
import { UsersService } from '../../../core/services/users.service';
import { Guide, MOBILITE_LABELS, SAISON_LABELS } from '../../../core/models/guide.model';
import { getCoverImage } from '../../../core/utils/cover-image.util';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent implements OnInit {
  protected auth = inject(AuthService);
  private guidesService = inject(GuidesService);
  private usersService = inject(UsersService);

  guides    = signal<Guide[]>([]);
  userCount = signal<number>(0);
  loading   = signal(true);

  accessibleGuides = computed(() => {
    if (this.auth.isAdmin()) return this.guides();
    const email = this.auth.email();
    return this.guides().filter(g => g.users?.some(u => u.email === email) ?? false);
  });

  // Affiche tous les guides (le filtrage par note nécessiterait une mise à jour du backend)
  ratedGuides = computed(() => {
    return this.accessibleGuides();
  });
  guideCount       = computed(() => this.accessibleGuides().length);
  totalActivities  = computed(() =>
    this.accessibleGuides().reduce((acc, g) => acc + (g.activities?.length ?? 0), 0)
  );
  
  // 🔹 Nouvelle donnée pour la liste verticale
  latestActivities = computed(() => {
    return this.accessibleGuides()
      .flatMap(g => (g.activities || []).map((a: any) => ({ ...a, guideId: g.id, guideTitle: g.titre })))
      .slice(0, 5); // On prend les 5 premières
  });

  readonly mobiliteLabels = MOBILITE_LABELS;
  readonly saisonLabels   = SAISON_LABELS;

  coverFor(guide: Guide): string {
    return getCoverImage(guide.titre, guide.saison);
  }

  get greeting(): string {
    const h = new Date().getHours();
    if (h < 12) return 'Bonjour';
    if (h < 18) return 'Bon après-midi';
    return 'Bonsoir';
  }

  ngOnInit(): void {
    this.guidesService.getAll().subscribe({
      next: data => {
        this.guides.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    if (this.auth.isAdmin()) {
      this.usersService.getAll().subscribe({
        next: users => this.userCount.set(users.length),
        error: () => {},
      });
    }
  }
}
