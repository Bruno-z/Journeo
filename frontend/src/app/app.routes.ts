import { Routes } from '@angular/router';
import { authGuard } from './core/guards/auth.guard';
import { adminGuard } from './core/guards/admin.guard';
import { publicGuard } from './core/guards/public.guard';

export const routes: Routes = [
  // Landing page publique
  {
    path: '',
    loadComponent: () =>
      import('./features/landing-page/landing-page.component').then(
        m => m.LandingPageComponent
      ),
    canActivate: [publicGuard], // redirige vers dashboard si déjà connecté
  },

  {
    path: 'auth',
    children: [
      {
        path: 'login',
        loadComponent: () =>
          import('./features/auth/login/login.component').then(m => m.LoginComponent),
      },
      {
        path: 'register',
        loadComponent: () =>
          import('./features/auth/register/register.component').then(m => m.RegisterComponent),
      },
      { path: '', redirectTo: 'login', pathMatch: 'full' },
    ],
  },

  {
    path: 'dashboard',
    canActivate: [authGuard],
    loadComponent: () =>
      import('./features/dashboard/dashboard.component').then(m => m.DashboardComponent),
    children: [
      {
        path: '',
        loadComponent: () =>
          import('./features/dashboard/home/home.component').then(m => m.HomeComponent),
      },
      {
        path: 'guides',
        children: [
          {
            path: '',
            loadComponent: () =>
              import('./features/guides/guide-list/guide-list.component').then(
                m => m.GuideListComponent,
              ),
          },
          {
            path: 'new',
            canActivate: [adminGuard],
            loadComponent: () =>
              import('./features/guides/guide-form/guide-form.component').then(
                m => m.GuideFormComponent,
              ),
          },
          {
            path: ':id',
            loadComponent: () =>
              import('./features/guides/guide-detail/guide-detail.component').then(
                m => m.GuideDetailComponent,
              ),
          },
          {
            path: ':id/edit',
            canActivate: [adminGuard],
            loadComponent: () =>
              import('./features/guides/guide-form/guide-form.component').then(
                m => m.GuideFormComponent,
              ),
          },
        ],
      },
      {
        path: 'users',
        canActivate: [adminGuard],
        loadComponent: () =>
          import('./features/users/users-list/users-list.component').then(
            m => m.UsersListComponent,
          ),
      },
      {
        path: 'profile',
        loadComponent: () =>
          import('./features/users/profile/profile.component').then(m => m.ProfileComponent),
      },
    ],
  },

  // Catch-all
  //{ path: '**', redirectTo: '/auth/login' },
];