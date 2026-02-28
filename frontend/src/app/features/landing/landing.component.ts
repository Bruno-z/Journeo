import { ChangeDetectionStrategy, Component, inject, OnInit, signal, ViewChildren, QueryList, ElementRef } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthModalService } from '../../shared/components/auth-modal/auth-modal.service';
import { AuthModalComponent } from '../../shared/components/auth-modal/auth-modal.component';
import { GuidesService } from '../../core/services/guides.service';
import { Guide } from '../../core/models/guide.model';

@Component({
  selector: 'app-landing',
  imports: [RouterLink, AuthModalComponent],
  templateUrl: './landing.component.html',
  styleUrls: ['./landing.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LandingComponent implements OnInit {
  protected modal = inject(AuthModalService);
  private guidesService = inject(GuidesService);

  guides = signal<Guide[]>([]);

  @ViewChildren('videoPlayer') videos!: QueryList<ElementRef<HTMLVideoElement>>;


  readonly galleryImages = [
    { url: 'https://videos.pexels.com/video-files/856195/856195-hd_1920_1080_25fps.mp4', alt: 'Paris' },
    { url: 'https://videos.pexels.com/video-files/3254011/3254011-hd_1920_1080_25fps.mp4', alt: 'Londres' },
    { url: 'https://videos.pexels.com/video-files/3130284/3130284-hd_1920_1080_25fps.mp4', alt: 'Barcelone' },
    { url: 'https://videos.pexels.com/video-files/857251/857251-hd_1920_1080_25fps.mp4', alt: 'Rome' },
    { url: 'https://videos.pexels.com/video-files/3141210/3141210-hd_1920_1080_25fps.mp4', alt: 'Berlin' },
    { url: 'https://videos.pexels.com/video-files/855564/855564-hd_1920_1080_25fps.mp4', alt: 'Prague' },
    { url: 'https://www.pexels.com/download/video/3177175/', alt: 'Tokyo' },
    { url: 'https://videos.pexels.com/video-files/3195650/3195650-hd_1920_1080_25fps.mp4', alt: 'Bali' },
    { url: 'https://videos.pexels.com/video-files/4106998/4106998-hd_1920_1080_25fps.mp4', alt: 'Marrakech' },
    { url: 'https://videos.pexels.com/video-files/856309/856309-hd_1920_1080_25fps.mp4', alt: 'Cape Town' },
    { url: 'https://videos.pexels.com/video-files/3141211/3141211-hd_1920_1080_25fps.mp4', alt: 'Dubai' },
    { url: 'https://videos.pexels.com/video-files/4763825/4763825-hd_1920_1080_25fps.mp4', alt: 'Petra' },
    { url: 'https://videos.pexels.com/video-files/3135808/3135808-hd_1920_1080_25fps.mp4', alt: 'Sydney' },
    { url: 'https://videos.pexels.com/video-files/857195/857195-hd_1920_1080_25fps.mp4', alt: 'Bangkok' },
  ];

  getPreviewImage(city: string): string {
    const previews: Record<string, string> = {
      Paris:     'https://images.pexels.com/photos/338515/pexels-photo-338515.jpeg',
      Londres:   'https://images.pexels.com/photos/358443/pexels-photo-358443.jpeg',
      Barcelone: 'https://images.pexels.com/photos/373912/pexels-photo-373912.jpeg',
      Rome:      'https://images.pexels.com/photos/417173/pexels-photo-417173.jpeg',
      Berlin:    'https://images.pexels.com/photos/1619311/pexels-photo-1619311.jpeg',
      Prague:    'https://images.pexels.com/photos/417344/pexels-photo-417344.jpeg',
      Tokyo:     'https://images.unsplash.com/photo-1617599137346-98e7c279ebe6?q=80&w=784&auto=format&fit=crop&ixlib=rb-4.1.0&ixid=M3wxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8fA%3D%3D',
      Bali:      'https://images.pexels.com/photos/248797/pexels-photo-248797.jpeg',
      Marrakech: 'https://images.pexels.com/photos/358485/pexels-photo-358485.jpeg',
      'Cape Town': 'https://images.pexels.com/photos/1538265/pexels-photo-1538265.jpeg',
      Dubai:     'https://images.pexels.com/photos/1619648/pexels-photo-1619648.jpeg',
      Petra:     'https://images.pexels.com/photos/3860094/pexels-photo-3860094.jpeg',
      Sydney:    'https://images.pexels.com/photos/2193300/pexels-photo-2193300.jpeg',
      Bangkok:   'https://images.pexels.com/photos/4666855/pexels-photo-4666855.jpeg',
    };
    return previews[city] ?? previews['Paris'];
  }

  readonly saisonGradients: Record<string, string> = {
    ETE:       'linear-gradient(135deg, #e8501e 0%, #ff8c42 100%)',
    HIVER:     'linear-gradient(135deg, #3b82f6 0%, #1e40af 100%)',
    PRINTEMPS: 'linear-gradient(135deg, #10b981 0%, #059669 100%)',
    AUTOMNE:   'linear-gradient(135deg, #f59e0b 0%, #d97706 100%)',
  };

  readonly features = [
    { icon: '🗺️', title: 'Itinéraires sur mesure', desc: 'Des guides créés par des passionnés pour chaque type de voyageur.' },
    { icon: '📅', title: 'Jour par jour', desc: 'Organisez votre voyage activité par activité, jour après jour.' },
    { icon: '📸', title: 'Galerie photos', desc: 'Revivez chaque destination grâce aux photos et vidéos des guides.' },
  ];

  ngOnInit(): void {
    this.guidesService.getAll().subscribe({
      next: data => this.guides.set(data.slice(0, 6)),
      error: () => {},
    });
  }

  gradientFor(saison: string): string {
    return this.saisonGradients[saison] ?? 'linear-gradient(135deg, #6b7280 0%, #374151 100%)';
  }

  // ================= VIDEO HOVER =================
  playVideo(index: number): void {
    const video = this.videos.get(index)?.nativeElement;
    if (video) {
      video.muted = true; // obligatoire pour autoplay sur Chrome/Safari
      video.play().catch(err => console.warn('Impossible de jouer la vidéo:', err));
    }
  }

  stopVideo(index: number): void {
    const video = this.videos.get(index)?.nativeElement;
    if (video) {
      video.pause();
      video.currentTime = 0;
    }
  }
}