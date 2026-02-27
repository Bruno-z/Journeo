import { Injectable } from '@angular/core';
import { CanActivate, Router } from '@angular/router';
import { AuthService } from '../services/auth.service';

@Injectable({ providedIn: 'root' })
export class PublicGuard implements CanActivate {
  constructor(private auth: AuthService, private router: Router) {}

  canActivate(): boolean {
    // si l'utilisateur est connecté, on le redirige vers le dashboard
    if (this.auth.isLoggedIn()) {
      this.router.navigate(['/dashboard']);
      return false;
    }
    return true; // sinon, il peut accéder à la landing page
  }
}