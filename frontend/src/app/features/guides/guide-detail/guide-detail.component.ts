import {
  ChangeDetectionStrategy, ChangeDetectorRef, Component, computed,
  inject, OnInit, signal,
} from '@angular/core';
import { DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';

import { AuthService } from '../../../core/services/auth.service';
import { GuidesService } from '../../../core/services/guides.service';
import { UsersService } from '../../../core/services/users.service';
import { ActivitiesService } from '../../../core/services/activities.service';
import { CommentsService, Comment } from '../../../core/services/comments.service';
import { GuideMediaService, GuideMedia } from '../../../core/services/guide-media.service';
import { Guide, MOBILITE_LABELS, PUBLIC_CIBLE_LABELS, SAISON_LABELS } from '../../../core/models/guide.model';
import { Activity, ActivityRequest, ACTIVITY_TYPE_LABELS } from '../../../core/models/activity.model';
import { User } from '../../../core/models/user.model';
import { getCoverImage } from '../../../core/utils/cover-image.util';

type Tab = 'programme' | 'galerie' | 'commentaires';

@Component({
  selector: 'app-guide-detail',
  imports: [RouterLink, ReactiveFormsModule, DatePipe],
  templateUrl: './guide-detail.component.html',
  styleUrl: './guide-detail.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuideDetailComponent implements OnInit {
  protected auth            = inject(AuthService);
  private route             = inject(ActivatedRoute);
  private router            = inject(Router);
  private guidesService     = inject(GuidesService);
  private usersService      = inject(UsersService);
  private activitiesService = inject(ActivitiesService);
  private commentsService   = inject(CommentsService);
  private mediaService      = inject(GuideMediaService);
  private sanitizer         = inject(DomSanitizer);
  private cdr               = inject(ChangeDetectorRef);
  private fb                = inject(FormBuilder);

  // ── Core guide state ─────────────────────────────────────────────────────
  guide    = signal<Guide | null>(null);
  loading  = signal(true);
  error    = signal<string | null>(null);
  allUsers = signal<User[]>([]);
  deleting = signal(false);

  // ── Cover image ───────────────────────────────────────────────────────────
  coverImageUrl = signal<string>('');

  // ── Tabs ─────────────────────────────────────────────────────────────────
  activeTab = signal<Tab>('programme');

  // ── Activity form ─────────────────────────────────────────────────────────
  showActivityForm = signal(false);
  activitySaving   = signal(false);
  activityError    = signal<string | null>(null);
  deletingActivity = signal<number | null>(null);
  selectedActivity = signal<Activity | null>(null);

  // ── Comments ──────────────────────────────────────────────────────────────
  comments        = signal<Comment[]>([]);
  commentsLoading = signal(false);
  commentSaving   = signal(false);
  commentError    = signal<string | null>(null);
  commentSuccess  = signal(false);
  deletingComment = signal<number | null>(null);
  selectedRating  = signal(5);

  commentForm = this.fb.group({
    content: ['', [Validators.required, Validators.minLength(3)]],
  });

  // ── Media ─────────────────────────────────────────────────────────────────
  media        = signal<GuideMedia[]>([]);
  mediaLoading = signal(false);
  uploadSaving = signal(false);
  deletingMedia = signal<number | null>(null);

  // ── Labels ────────────────────────────────────────────────────────────────
  readonly mobiliteLabels     = MOBILITE_LABELS;
  readonly saisonLabels       = SAISON_LABELS;
  readonly publicCibleLabels  = PUBLIC_CIBLE_LABELS;
  readonly activityTypeLabels = ACTIVITY_TYPE_LABELS;

  // ── Activity form group ───────────────────────────────────────────────────
  activityForm = this.fb.group({
    titre:       ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    type:        ['MUSEE', Validators.required],
    adresse:     [''],
    heureDebut:  [''],
    duree:       [60,  [Validators.required, Validators.min(1)]],
    ordre:       [1,   [Validators.required, Validators.min(1)]],
    jour:        [1,   [Validators.required, Validators.min(1)]],
  });

  // ── Computed ──────────────────────────────────────────────────────────────
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

  daysArray = computed(() => Array.from(this.activitiesByDay().keys()));

  unassignedUsers = computed(() => {
    const assigned = new Set((this.guide()?.users ?? []).map(u => u.id));
    return this.allUsers().filter(u => !assigned.has(u.id));
  });

  activitiesWithCoords = computed(() =>
    (this.guide()?.activities ?? []).filter(
      a => a.latitude != null && a.longitude != null,
    )
  );

  starsArray = [1, 2, 3, 4, 5];

  // ── Map URL (Google Maps) ─────────────────────────────────────────────────
  mapUrl = computed(() => {
    // 1. Si une activité est sélectionnée, on centre la carte dessus
    const selected = this.selectedActivity();
    if (selected) {
      const location = this.getLocationString(selected);
      return this.sanitizer.bypassSecurityTrustResourceUrl(`https://maps.google.com/maps?q=${encodeURIComponent(location)}&output=embed`);
    }

    const activities = this.guide()?.activities;
    if (!activities || activities.length === 0) {
      // Default map (e.g. Paris) if no activities
      return this.sanitizer.bypassSecurityTrustResourceUrl('https://maps.google.com/maps?q=Paris&output=embed');
    }

    // Sort activities to define the route
    const sorted = [...activities].sort((a, b) => {
      if ((a.jour ?? 0) !== (b.jour ?? 0)) return (a.jour ?? 0) - (b.jour ?? 0);
      return (a.ordre ?? 0) - (b.ordre ?? 0);
    });

    const origin = this.getLocationString(sorted[0]);

    if (sorted.length === 1) {
      return this.sanitizer.bypassSecurityTrustResourceUrl(`https://maps.google.com/maps?q=${encodeURIComponent(origin)}&output=embed`);
    }

    const destination = this.getLocationString(sorted[sorted.length - 1]);
    return this.sanitizer.bypassSecurityTrustResourceUrl(`https://maps.google.com/maps?saddr=${encodeURIComponent(origin)}&daddr=${encodeURIComponent(destination)}&output=embed`);
  });

  // ── Lifecycle ─────────────────────────────────────────────────────────────
  ngOnInit(): void {
    const id = Number(this.route.snapshot.paramMap.get('id'));
    this.load(id);
    if (this.auth.isAdmin()) {
      this.usersService.getAll().subscribe({ next: users => this.allUsers.set(users), error: () => {} });
    }
  }

  // ── Guide CRUD ────────────────────────────────────────────────────────────
  load(id: number): void {
    this.loading.set(true);
    this.guidesService.getById(id).subscribe({
      next: data => {
        this.guide.set(data);
        this.coverImageUrl.set(getCoverImage(data.titre, data.saison));
        this.loading.set(false);
        this.cdr.markForCheck();
      },
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

  // ── User assignment ───────────────────────────────────────────────────────
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
      error: ()     => alert("Erreur lors de l'assignation."),
    });
  }

  isUserAssigned(userId: number): boolean {
    return (this.guide()?.users ?? []).some(u => u.id === userId);
  }

  // ── Tabs ─────────────────────────────────────────────────────────────────
  setTab(tab: Tab): void {
    this.activeTab.set(tab);
    const g = this.guide();
    if (!g) return;
    if (tab === 'commentaires' && this.comments().length === 0 && !this.commentsLoading()) {
      this.loadComments(g.id);
    }
    if (tab === 'galerie' && this.media().length === 0 && !this.mediaLoading()) {
      this.loadMedia(g.id);
    }
  }

  // ── Activity CRUD ─────────────────────────────────────────────────────────
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

  // ── Comments ──────────────────────────────────────────────────────────────
  loadComments(guideId: number): void {
    this.commentsLoading.set(true);
    this.commentsService.getForGuide(guideId).subscribe({
      next: list => { this.comments.set(list); this.commentsLoading.set(false); this.cdr.markForCheck(); },
      error: ()  => { this.commentsLoading.set(false); },
    });
  }

  setRating(star: number): void {
    this.selectedRating.set(star);
  }

  submitComment(): void {
    if (this.commentForm.invalid) { this.commentForm.markAllAsTouched(); return; }
    const g = this.guide();
    if (!g) return;
    this.commentSaving.set(true);
    this.commentError.set(null);

    this.commentsService.add(g.id, {
      content: this.commentForm.value.content!,
      rating:  this.selectedRating(),
    }).subscribe({
      next: added => {
        this.comments.update(list => [added, ...list]);
        this.commentForm.reset();
        this.selectedRating.set(5);
        this.commentSaving.set(false);
        this.commentSuccess.set(true);
        setTimeout(() => this.commentSuccess.set(false), 3000);
        this.cdr.markForCheck();
      },
      error: err => {
        this.commentSaving.set(false);
        this.commentError.set(err.error?.message ?? 'Erreur lors de la publication.');
      },
    });
  }

  deleteComment(commentId: number): void {
    if (!confirm('Supprimer ce commentaire ?')) return;
    const g = this.guide();
    if (!g) return;
    this.deletingComment.set(commentId);
    this.commentsService.delete(g.id, commentId).subscribe({
      next: () => {
        this.comments.update(list => list.filter(c => c.id !== commentId));
        this.deletingComment.set(null);
        this.cdr.markForCheck();
      },
      error: () => { this.deletingComment.set(null); },
    });
  }

  canDeleteComment(comment: Comment): boolean {
    return this.auth.isAdmin() || comment.authorEmail === this.auth.email();
  }

  timeAgo(dateStr: string): string {
    const diff = Math.floor((Date.now() - new Date(dateStr).getTime()) / 1000);
    if (diff < 60) return 'il y a quelques secondes';
    if (diff < 3600) { const m = Math.floor(diff / 60); return `il y a ${m} minute${m > 1 ? 's' : ''}`; }
    if (diff < 86400) { const h = Math.floor(diff / 3600); return `il y a ${h} heure${h > 1 ? 's' : ''}`; }
    if (diff < 604800) { const d = Math.floor(diff / 86400); return `il y a ${d} jour${d > 1 ? 's' : ''}`; }
    const w = Math.floor(diff / 604800);
    return `il y a ${w} semaine${w > 1 ? 's' : ''}`;
  }

  authorName(comment: Comment): string {
    if (comment.authorFirstName && comment.authorLastName) return `${comment.authorFirstName} ${comment.authorLastName}`;
    if (comment.authorFirstName) return comment.authorFirstName;
    return comment.authorEmail.split('@')[0];
  }

  avatarColor(email: string): string {
    const palette = ['#E85A1E', '#4F46E5', '#059669', '#DC2626', '#7C3AED', '#0891B2', '#D97706'];
    let h = 0;
    for (let i = 0; i < email.length; i++) h = (h * 31 + email.charCodeAt(i)) & 0xffffffff;
    return palette[Math.abs(h) % palette.length];
  }

  likeCount(comment: Comment): number {
    return (comment.id % 28) + comment.rating * 3;
  }

  dislikeCount(comment: Comment): number {
    return comment.id % 4;
  }

  // ── Media ─────────────────────────────────────────────────────────────────
  loadMedia(guideId: number): void {
    this.mediaLoading.set(true);
    this.mediaService.getForGuide(guideId).subscribe({
      next: list => { this.media.set(list); this.mediaLoading.set(false); this.cdr.markForCheck(); },
      error: ()  => { this.mediaLoading.set(false); },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    const g = this.guide();
    if (!file || !g) return;
    this.uploadSaving.set(true);
    this.mediaService.upload(g.id, file).subscribe({
      next: added => {
        this.media.update(list => [added, ...list]);
        this.uploadSaving.set(false);
        this.cdr.markForCheck();
        input.value = '';
      },
      error: () => { this.uploadSaving.set(false); alert('Erreur lors du téléversement.'); },
    });
  }

  deleteMedia(mediaId: number): void {
    if (!confirm('Supprimer ce fichier ?')) return;
    const g = this.guide();
    if (!g) return;
    this.deletingMedia.set(mediaId);
    this.mediaService.delete(g.id, mediaId).subscribe({
      next: () => {
        this.media.update(list => list.filter(m => m.id !== mediaId));
        this.deletingMedia.set(null);
        this.cdr.markForCheck();
      },
      error: () => { this.deletingMedia.set(null); },
    });
  }

  focusActivity(activity: Activity): void {
    // Si on clique sur l'activité déjà sélectionnée, on désélectionne (retour vue globale)
    if (this.selectedActivity()?.id === activity.id) {
      this.selectedActivity.set(null);
    } else {
      this.selectedActivity.set(activity);
    }
  }

  getActivityIcon(type: string): string {
    switch (type) {
      case 'MUSEE': return '🏛️';
      case 'CHATEAU': return '🏰';
      case 'PARC': return '🌳';
      case 'GROTTE': return '🪨';
      default: return '📍';
    }
  }

  private getLocationString(activity: Activity): string {
    if (activity.latitude && activity.longitude) {
      return `${activity.latitude},${activity.longitude}`;
    }
    return activity.adresse || activity.titre;
  }
}
