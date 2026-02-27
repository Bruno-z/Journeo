import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { GuidesService } from '../../../core/services/guides.service';
import { UsersService } from '../../../core/services/users.service';
import { ActivitiesService } from '../../../core/services/activities.service';
import { Guide, MOBILITE_LABELS, PUBLIC_CIBLE_LABELS, SAISON_LABELS } from '../../../core/models/guide.model';
import { Activity, ActivityRequest, ACTIVITY_TYPE_LABELS } from '../../../core/models/activity.model';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-guide-detail',
  imports: [RouterLink, ReactiveFormsModule],
  templateUrl: './guide-detail.component.html',
  styleUrl: './guide-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuideDetailComponent implements OnInit {
  protected auth = inject(AuthService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);
  private guidesService = inject(GuidesService);
  private usersService = inject(UsersService);
  private activitiesService = inject(ActivitiesService);
  private fb = inject(FormBuilder);

  guide    = signal<Guide | null>(null);
  loading  = signal(true);
  error    = signal<string | null>(null);
  allUsers = signal<User[]>([]);
  deleting = signal(false);

  showActivityForm = signal(false);
  activitySaving   = signal(false);
  activityError    = signal<string | null>(null);
  deletingActivity = signal<number | null>(null);

  readonly mobiliteLabels     = MOBILITE_LABELS;
  readonly saisonLabels       = SAISON_LABELS;
  readonly publicCibleLabels  = PUBLIC_CIBLE_LABELS;
  readonly activityTypeLabels = ACTIVITY_TYPE_LABELS;

  activityForm = this.fb.group({
    titre:      ['', [Validators.required, Validators.minLength(2)]],
    description:[''],
    type:       ['MUSEE', Validators.required],
    adresse:    [''],
    heureDebut: [''],
    duree:      [60,  [Validators.required, Validators.min(1)]],
    ordre:      [1,   [Validators.required, Validators.min(1)]],
    jour:       [1,   [Validators.required, Validators.min(1)]],
  });

  activitiesByDay = computed(() => {
    const activities = this.guide()?.activities ?? [];
    const map = new Map<number, Activity[]>();
    for (const a of activities) {
      const day = a.jour ?? 1;
      const list = map.get(day) ?? [];
      list.push(a);
      map.set(day, list);
    }
    const sorted = new Map<number, Activity[]>();
    for (const [day, acts] of [...map.entries()].sort(([a], [b]) => a - b)) {
      sorted.set(day, [...acts].sort((a, b) => (a.ordre ?? 0) - (b.ordre ?? 0)));
    }
    return sorted;
  });

  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
    if (this.auth.isAdmin()) {
      this.usersService.getAll().subscribe({ next: users => this.allUsers.set(users), error: () => {} });
    }
  }

  load(id: number): void {
    this.loading.set(true);
    this.guidesService.getById(id).subscribe({
      next: data => { this.guide.set(data); this.loading.set(false); },
      error: err => {
        this.loading.set(false);
        this.error.set(err.status === 404 ? 'Guide introuvable.' : 'Erreur de chargement.');
      },
    });
  }

  delete(): void {
    const g = this.guide();
    if (!g || !confirm(`Supprimer "${g.titre}" ?`)) return;
    this.deleting.set(true);
    this.guidesService.delete(g.id).subscribe({
      next: () => this.router.navigate(['/dashboard/guides']),
      error: ()  => { this.deleting.set(false); alert('Erreur lors de la suppression.'); },
    });
  }

  removeUser(userId: number): void {
    const g = this.guide();
    if (!g) return;
    this.guidesService.removeUser(g.id, userId).subscribe({
      next: updated => this.guide.set(updated),
      error: ()     => alert('Erreur lors du retrait.'),
    });
  }

  addUser(userId: number): void {
    const g = this.guide();
    if (!g) return;
    this.guidesService.addUser(g.id, userId).subscribe({
      next: updated => this.guide.set(updated),
      error: ()     => alert('Erreur lors de l\'assignation.'),
    });
  }

  isUserAssigned(userId: number): boolean {
    return (this.guide()?.users ?? []).some(u => u.id === userId);
  }

  unassignedUsers = computed(() => {
    const assigned = new Set((this.guide()?.users ?? []).map(u => u.id));
    return this.allUsers().filter(u => !assigned.has(u.id));
  });

  daysArray = computed(() => Array.from(this.activitiesByDay().keys()));

  submitActivity(): void {
    if (this.activityForm.invalid) { this.activityForm.markAllAsTouched(); return; }
    const g = this.guide();
    if (!g) return;
    this.activitySaving.set(true);
    this.activityError.set(null);

    const v = this.activityForm.value;
    const payload: ActivityRequest = {
      titre:       v.titre!,
      description: v.description || undefined,
      type:        v.type!,
      adresse:     v.adresse || undefined,
      heureDebut:  v.heureDebut || undefined,
      duree:       v.duree!,
      ordre:       v.ordre!,
      jour:        v.jour!,
    };

    this.activitiesService.create(g.id, payload).subscribe({
      next: () => {
        this.activitySaving.set(false);
        this.showActivityForm.set(false);
        this.activityForm.reset({ type: 'MUSEE', duree: 60, ordre: 1, jour: 1 });
        this.load(g.id);
      },
      error: err => {
        this.activitySaving.set(false);
        this.activityError.set(err.error?.message ?? 'Erreur lors de la création.');
      },
    });
  }

  deleteActivity(activityId: number): void {
    if (!confirm('Supprimer cette activité ?')) return;
    const g = this.guide();
    if (!g) return;
    this.deletingActivity.set(activityId);
    this.activitiesService.delete(activityId).subscribe({
      next: () => { this.deletingActivity.set(null); this.load(g.id); },
      error: ()  => { this.deletingActivity.set(null); alert('Erreur lors de la suppression.'); },
    });
  }
}
