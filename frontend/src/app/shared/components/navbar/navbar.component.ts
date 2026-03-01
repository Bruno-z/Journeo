import {
  ChangeDetectionStrategy,
  Component,
  inject,
  OnDestroy,
  OnInit,
  signal,
} from '@angular/core';
import { NavigationEnd, Router, RouterLink } from '@angular/router';
import { Subscription } from 'rxjs';
import { filter } from 'rxjs/operators';
import { AuthService } from '../../../core/services/auth.service';
import { SidebarStateService } from '../../../core/services/sidebar-state.service';

const PAGE_TITLES: Record<string, string> = {
  '/dashboard':        'Tableau de bord',
  '/dashboard/guides': 'Mes Guides',
  '/dashboard/users':  'Utilisateurs',
  '/dashboard/profile': 'Mon profil',
};

@Component({
  selector: 'app-navbar',
  imports: [RouterLink],
  templateUrl: './navbar.component.html',
  styleUrl: './navbar.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class NavbarComponent implements OnInit, OnDestroy {
  protected auth = inject(AuthService);
  protected sidebarState = inject(SidebarStateService);
  private router = inject(Router);

  pageTitle = signal('');

  private sub = new Subscription();

  ngOnInit(): void {
    this.pageTitle.set(PAGE_TITLES[this.router.url] ?? '');
    this.sub.add(
      this.router.events
        .pipe(filter(e => e instanceof NavigationEnd))
        .subscribe(e => {
          const url = (e as NavigationEnd).urlAfterRedirects.split('?')[0];
          this.pageTitle.set(PAGE_TITLES[url] ?? '');
        }),
    );
  }

  logout(): void {
    this.auth.logout();
    this.router.navigate(['/']);
  }

  ngOnDestroy(): void {
    this.sub.unsubscribe();
  }
}
