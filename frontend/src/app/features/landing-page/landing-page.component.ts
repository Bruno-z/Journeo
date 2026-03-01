import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { AuthModalService } from '../../shared/components/auth-modal/auth-modal.service';
import { AuthModalComponent } from '../../shared/components/auth-modal/auth-modal.component';

@Component({
  selector: 'app-landing-page',
  imports: [AuthModalComponent],
  templateUrl: './landing-page.component.html',
  styleUrls: ['./landing-page.component.scss'],
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class LandingPageComponent {
  protected modal = inject(AuthModalService);

  activeTab = signal<string>('Tous');

  readonly tabs = ['Tous', 'Populaires', 'Europe', 'Asie', 'Aventure'];

  readonly destinations = [
    {
      name: 'Plage de Kelingking',
      location: 'Bali, Indonésie',
      rating: 4.8,
      reviews: 245,
      price: 950,
      image: 'https://images.pexels.com/photos/3225531/pexels-photo-3225531.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
    {
      name: 'Côte de Napali',
      location: 'Hawaï, USA',
      rating: 4.9,
      reviews: 312,
      price: 1200,
      image: 'https://images.pexels.com/photos/1450353/pexels-photo-1450353.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
    {
      name: 'Pamukkale',
      location: 'Turquie',
      rating: 4.7,
      reviews: 198,
      price: 780,
      image: 'https://images.pexels.com/photos/2832734/pexels-photo-2832734.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
    {
      name: 'Baie Turquoise',
      location: 'Australie',
      rating: 4.8,
      reviews: 267,
      price: 1100,
      image: 'https://images.pexels.com/photos/1430672/pexels-photo-1430672.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
    {
      name: 'Fjord de Geiranger',
      location: 'Norvège',
      rating: 4.9,
      reviews: 189,
      price: 1350,
      image: 'https://images.pexels.com/photos/4321802/pexels-photo-4321802.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
    {
      name: 'Lac Banff',
      location: 'Canada',
      rating: 4.8,
      reviews: 423,
      price: 990,
      image: 'https://images.pexels.com/photos/1761279/pexels-photo-1761279.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
    {
      name: 'Antelope Canyon',
      location: 'Arizona, USA',
      rating: 4.9,
      reviews: 356,
      price: 850,
      image: 'https://images.pexels.com/photos/2253842/pexels-photo-2253842.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
    {
      name: 'Îles Phi Phi',
      location: 'Thaïlande',
      rating: 4.7,
      reviews: 512,
      price: 720,
      image: 'https://images.pexels.com/photos/1268855/pexels-photo-1268855.jpeg?auto=compress&cs=tinysrgb&w=600',
    },
  ];

  readonly features = [
    {
      icon: 'location',
      title: 'Des guides variés',
      desc: 'Explorez des centaines d\'itinéraires conçus par des passionnés pour chaque style de voyage.',
    },
    {
      icon: 'guide',
      title: 'Experts locaux',
      desc: 'Des guides détaillés créés par des voyageurs expérimentés qui connaissent chaque destination.',
    },
    {
      icon: 'booking',
      title: 'Planification simple',
      desc: 'Organisez votre voyage étape par étape, jour après jour, en quelques clics.',
    },
  ];

  readonly stats = [
    { value: '2000+', label: 'Voyageurs' },
    { value: '500+', label: 'Destinations' },
    { value: '20+', label: 'Guides' },
  ];

  setTab(tab: string): void {
    this.activeTab.set(tab);
  }

  starsArray(): number[] {
    return [0, 1, 2, 3, 4];
  }
}
