import {
  AfterViewInit,
  ChangeDetectionStrategy,
  Component,
  computed,
  effect,
  ElementRef,
  inject,
  OnDestroy,
  OnInit,
  signal,
  ViewChild,
} from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { AuthService } from '../../../core/services/auth.service';
import { CommentsService, Comment } from '../../../core/services/comments.service';
import { GuidesService } from '../../../core/services/guides.service';
import { Guide, MOBILITE_LABELS, PUBLIC_CIBLE_LABELS, SAISON_LABELS } from '../../../core/models/guide.model';
import { getCoverImage } from '../../../core/utils/cover-image.util';

declare const L: any;
declare const gsap: any;

/** Fallback city coordinates for title-based extraction */
const CITY_COORDS: Record<string, [number, number]> = {
  'paris':      [48.8566,  2.3522],
  'bali':       [-8.3405,  115.0920],
  'tokyo':      [35.6762,  139.6503],
  'kyoto':      [35.0116,  135.7681],
  'marrakech':  [31.6295,  -7.9811],
  'barcelone':  [41.3851,  2.1734],
  'barcelona':  [41.3851,  2.1734],
  'rome':       [41.9028,  12.4964],
  'dubai':      [25.2048,  55.2708],
  'amsterdam':  [52.3676,  4.9041],
  'new york':   [40.7128,  -74.0060],
  'london':     [51.5074,  -0.1278],
  'londres':    [51.5074,  -0.1278],
  'istanbul':   [41.0082,  28.9784],
  'lisbonne':   [38.7169,  -9.1395],
  'bangkok':    [13.7563,  100.5018],
  'sydney':     [-33.8688, 151.2093],
  'singapour':  [1.3521,   103.8198],
  'berlin':     [52.5200,  13.4050],
  'miami':      [25.7617,  -80.1918],
  'ecosse':     [56.4907,  -4.2026],
  'default':    [20.0,     10.0],
};

function extractCityFromTitle(titre: string): string {
  const lower = titre.toLowerCase();
  for (const key of Object.keys(CITY_COORDS)) {
    if (key !== 'default' && lower.includes(key)) return key;
  }
  return 'default';
}

@Component({
  selector: 'app-guide-list',
  imports: [RouterLink, FormsModule],
  templateUrl: './guide-list.component.html',
  styleUrl: './guide-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuideListComponent implements OnInit, AfterViewInit, OnDestroy {
  protected auth = inject(AuthService);
  private guidesService = inject(GuidesService);
  private commentsService = inject(CommentsService);

  @ViewChild('mapEl') mapElRef?: ElementRef<HTMLDivElement>;

  // Data
  guides   = signal<Guide[]>([]);
  loading  = signal(true);
  error    = signal<string | null>(null);
  search   = signal('');
  deleting = signal<number | null>(null);

  // Carousel
  carouselStart = signal(0);
  activeGuideId = signal<number | null>(null);

  // Comments
  comments        = signal<Comment[]>([]);
  commentsLoading = signal(false);
  newComment      = signal('');
  newRating       = signal(5);
  commentSaving   = signal(false);

  readonly mobiliteLabels    = MOBILITE_LABELS;
  readonly saisonLabels      = SAISON_LABELS;
  readonly publicCibleLabels = PUBLIC_CIBLE_LABELS;

  accessibleGuides = computed(() => {
    if (this.auth.isAdmin()) return this.guides();
    const email = this.auth.email();
    return this.guides().filter(g => g.users?.some(u => u.email === email) ?? false);
  });

  filtered = computed(() => {
    const q = this.search().toLowerCase().trim();
    const base = this.accessibleGuides();
    if (!q) return base;
    return base.filter(g =>
      g.titre.toLowerCase().includes(q) ||
      (g.description ?? '').toLowerCase().includes(q),
    );
  });

  activeGuide = computed(() => {
    const id   = this.activeGuideId();
    const list = this.filtered();
    if (!list.length) return null;
    return id !== null
      ? (list.find(g => g.id === id) ?? list[0])
      : list[0];
  });

  visibleGuides = computed(() =>
    this.filtered().slice(this.carouselStart(), this.carouselStart() + 3),
  );

  canPrev = computed(() => this.carouselStart() > 0);
  canNext = computed(() => this.carouselStart() + 3 < this.filtered().length);

  // Leaflet
  private map?: any;
  private activityMarkers: any[] = [];
  private mapInitialized = false;

  private subs = new Subscription();
  private commentSub?: Subscription;

  constructor() {
    effect(() => {
      const guide = this.activeGuide();
      if (guide && this.mapInitialized) {
        this.updateMapForGuide(guide);
      }
    });
  }

  ngOnInit(): void {
    this.load();
  }

  ngAfterViewInit(): void {
    setTimeout(() => this.initMap(), 300);
  }

  load(): void {
    this.loading.set(true);
    this.subs.add(
      this.guidesService.getAll().subscribe({
        next: data => {
          this.guides.set(data);
          this.loading.set(false);
          if (data.length > 0) {
            this.activeGuideId.set(data[0].id);
            this.loadComments(data[0].id);
            setTimeout(() => this.animateIn(), 100);
          }
        },
        error: () => {
          this.error.set('Impossible de charger les guides.');
          this.loading.set(false);
        },
      }),
    );
  }

  private animateIn(): void {
    if (typeof gsap === 'undefined') return;
    gsap.from('.guide-card', { y: 50, opacity: 0, duration: 0.55, stagger: 0.07, ease: 'power3.out' });
  }

  private initMap(): void {
    if (typeof L === 'undefined' || !this.mapElRef?.nativeElement) return;

    this.map = L.map(this.mapElRef.nativeElement, {
      zoomControl: false,
      attributionControl: false,
    }).setView([20, 10], 2);

    L.tileLayer('https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png', {
      maxZoom: 18,
    }).addTo(this.map);

    L.control.zoom({ position: 'bottomright' }).addTo(this.map);
    L.control.attribution({ position: 'bottomleft', prefix: '© OpenStreetMap contributors' }).addTo(this.map);

    this.mapInitialized = true;

    const guide = this.activeGuide();
    if (guide) this.updateMapForGuide(guide);
  }

  private clearMarkers(): void {
    this.activityMarkers.forEach(m => m.remove());
    this.activityMarkers = [];
  }

  private async updateMapForGuide(guide: Guide): Promise<void> {
    if (!this.map) return;
    this.clearMarkers();

    const activities = guide.activities ?? [];

    // ── 1. Activities with explicit lat/lng → place markers, fit bounds ──
    const withCoords = activities.filter(a => a.latitude != null && a.longitude != null);
    if (withCoords.length > 0) {
      const latlngs: [number, number][] = [];
      for (const a of withCoords) {
        const ll: [number, number] = [a.latitude!, a.longitude!];
        latlngs.push(ll);
        const popup = `<strong style="font-family:Inter,sans-serif;font-size:13px">${a.titre}</strong>` +
          (a.adresse ? `<br/><span style="font-size:11px;color:#6b7280">${a.adresse}</span>` : '');
        const m = L.circleMarker(ll, {
          radius: 9, fillColor: '#E85A1E', color: '#ffffff', weight: 2.5, fillOpacity: 0.92,
        }).addTo(this.map).bindPopup(popup);
        this.activityMarkers.push(m);
      }
      if (latlngs.length === 1) {
        this.map.flyTo(latlngs[0], 14, { animate: true, duration: 1.2 });
        this.activityMarkers[0]?.openPopup();
      } else {
        this.map.fitBounds(latlngs, { padding: [48, 48], animate: true });
      }
      return;
    }

    // ── 2. Activities with address only → Nominatim geocoding ──
    const withAddr = activities.filter(a => a.adresse);
    if (withAddr.length > 0) {
      try {
        const addr = withAddr[0].adresse!;
        const resp = await fetch(
          `https://nominatim.openstreetmap.org/search?q=${encodeURIComponent(addr)}&format=json&limit=1`,
          { headers: { 'Accept-Language': 'fr' } },
        );
        const data = await resp.json();
        if (data[0]) {
          const ll: [number, number] = [parseFloat(data[0].lat), parseFloat(data[0].lon)];
          this.map.flyTo(ll, 13, { animate: true, duration: 1.2 });
          const popup = `<strong style="font-family:Inter,sans-serif;font-size:13px">${guide.titre}</strong>` +
            `<br/><span style="font-size:11px;color:#6b7280">${addr}</span>`;
          const m = L.circleMarker(ll, {
            radius: 10, fillColor: '#E85A1E', color: '#ffffff', weight: 3, fillOpacity: 0.92,
          }).addTo(this.map).bindPopup(popup).openPopup();
          this.activityMarkers.push(m);
          return;
        }
      } catch { /* fall through to title fallback */ }
    }

    // ── 3. Fallback: extract city from guide title ──
    const city   = extractCityFromTitle(guide.titre);
    const coords: [number, number] = CITY_COORDS[city] ?? CITY_COORDS['default'];
    this.map.flyTo(coords, city === 'default' ? 2 : 10, { animate: true, duration: 1.2 });
    const m = L.circleMarker(coords, {
      radius: 10, fillColor: '#E85A1E', color: '#ffffff', weight: 3, fillOpacity: 0.92,
    }).addTo(this.map)
      .bindPopup(`<strong style="font-family:Inter,sans-serif;font-size:13px">${guide.titre}</strong>`)
      .openPopup();
    this.activityMarkers.push(m);
  }

  selectGuide(guide: Guide): void {
    if (guide.id === this.activeGuideId()) return;
    this.activeGuideId.set(guide.id);
    this.loadComments(guide.id);

    if (typeof gsap !== 'undefined') {
      gsap.fromTo('.guide-info', { opacity: 0, y: 12 }, { opacity: 1, y: 0, duration: 0.3, ease: 'power2.out' });
    }
  }

  prevCarousel(): void {
    this.carouselStart.update(n => Math.max(0, n - 3));
  }

  nextCarousel(): void {
    const max = Math.max(0, this.filtered().length - 3);
    this.carouselStart.update(n => Math.min(n + 3, max));
  }

  carouselPages(): number[] {
    return Array.from({ length: Math.ceil(this.filtered().length / 3) }, (_, i) => i);
  }

  currentPage(): number {
    return Math.floor(this.carouselStart() / 3);
  }

  goToPage(page: number): void {
    this.carouselStart.set(page * 3);
  }

  loadComments(guideId: number): void {
    this.commentSub?.unsubscribe();
    this.comments.set([]);
    this.commentsLoading.set(true);
    this.commentSub = this.commentsService.getForGuide(guideId).subscribe({
      next: c  => { this.comments.set(c); this.commentsLoading.set(false); },
      error: () => { this.commentsLoading.set(false); },
    });
  }

  submitComment(): void {
    const guide   = this.activeGuide();
    const content = this.newComment().trim();
    if (!guide || !content || this.commentSaving()) return;

    this.commentSaving.set(true);
    this.subs.add(
      this.commentsService.add(guide.id, { content, rating: this.newRating() }).subscribe({
        next: c => {
          this.comments.update(cs => [c, ...cs]);
          this.newComment.set('');
          this.newRating.set(5);
          this.commentSaving.set(false);
        },
        error: () => { this.commentSaving.set(false); },
      }),
    );
  }

  deleteComment(commentId: number): void {
    const guide = this.activeGuide();
    if (!guide) return;
    this.subs.add(
      this.commentsService.delete(guide.id, commentId).subscribe({
        next: () => this.comments.update(cs => cs.filter(c => c.id !== commentId)),
        error: () => {},
      }),
    );
  }

  updateSearch(value: string): void {
    this.search.set(value);
    this.carouselStart.set(0);
    const g = this.filtered()[0];
    if (g) {
      this.activeGuideId.set(g.id);
      this.loadComments(g.id);
    }
  }

  coverFor(guide: Guide): string {
    return getCoverImage(guide.titre, guide.saison);
  }

  starsArray(): number[] {
    return [1, 2, 3, 4, 5];
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

  canDeleteComment(c: Comment): boolean {
    return this.auth.isAdmin() || c.authorEmail === this.auth.email();
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

  authorName(c: Comment): string {
    if (c.authorFirstName && c.authorLastName) return `${c.authorFirstName} ${c.authorLastName}`;
    if (c.authorFirstName) return c.authorFirstName;
    return c.authorEmail.split('@')[0];
  }

  avatarColor(email: string): string {
    const palette = ['#E85A1E', '#4F46E5', '#059669', '#DC2626', '#7C3AED', '#0891B2', '#D97706'];
    let h = 0;
    for (let i = 0; i < email.length; i++) h = (h * 31 + email.charCodeAt(i)) & 0xffffffff;
    return palette[Math.abs(h) % palette.length];
  }

  likeCount(c: Comment): number {
    return (c.id % 28) + c.rating * 3;
  }

  dislikeCount(c: Comment): number {
    return c.id % 4;
  }

  delete(guide: Guide): void {
    if (!confirm(`Supprimer "${guide.titre}" ?`)) return;
    this.deleting.set(guide.id);
    this.subs.add(
      this.guidesService.delete(guide.id).subscribe({
        next: () => {
          this.guides.update(gs => gs.filter(g => g.id !== guide.id));
          this.deleting.set(null);
          const remaining = this.filtered();
          if (remaining.length > 0 && !remaining.find(g => g.id === this.activeGuideId())) {
            this.activeGuideId.set(remaining[0].id);
            this.loadComments(remaining[0].id);
          }
          const max = Math.max(0, remaining.length - 4);
          if (this.carouselStart() > max) this.carouselStart.set(max);
        },
        error: () => {
          this.deleting.set(null);
          alert('Erreur lors de la suppression.');
        },
      }),
    );
  }

  ngOnDestroy(): void {
    this.subs.unsubscribe();
    this.commentSub?.unsubscribe();
    this.map?.remove();
  }
}
