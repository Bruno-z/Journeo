import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { GuidesService } from '../../../core/services/guides.service';
import { Guide, MOBILITE_LABELS, PUBLIC_CIBLE_LABELS, SAISON_LABELS } from '../../../core/models/guide.model';

@Component({
  selector: 'app-guide-list',
  imports: [RouterLink, FormsModule],
  templateUrl: './guide-list.component.html',
  styleUrl: './guide-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuideListComponent implements OnInit {
  protected auth = inject(AuthService);
  private guidesService = inject(GuidesService);

  guides    = signal<Guide[]>([]);
  loading   = signal(true);
  error     = signal<string | null>(null);
  search    = signal('');
  deleting  = signal<number | null>(null);

  readonly mobiliteLabels     = MOBILITE_LABELS;
  readonly saisonLabels       = SAISON_LABELS;
  readonly publicCibleLabels  = PUBLIC_CIBLE_LABELS;

  filtered = computed(() => {
    const q = this.search().toLowerCase().trim();
    if (!q) return this.guides();
    return this.guides().filter(g =>
      g.titre.toLowerCase().includes(q) ||
      (g.description ?? '').toLowerCase().includes(q),
    );
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.loading.set(true);
    this.guidesService.getAll().subscribe({
      next: data => { this.guides.set(data); this.loading.set(false); },
      error: ()   => { this.error.set('Impossible de charger les guides.'); this.loading.set(false); },
    });
  }

  updateSearch(value: string): void {
    this.search.set(value);
  }

  delete(guide: Guide): void {
    if (!confirm(`Supprimer "${guide.titre}" ?`)) return;
    this.deleting.set(guide.id);
    this.guidesService.delete(guide.id).subscribe({
      next: () => {
        this.guides.update(gs => gs.filter(g => g.id !== guide.id));
        this.deleting.set(null);
      },
      error: () => {
        this.deleting.set(null);
        alert('Erreur lors de la suppression.');
      },
    });
  }
}
