import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/auth.model';

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private _token = signal<string | null>(localStorage.getItem('jrn_token'));
  private _email = signal<string | null>(localStorage.getItem('jrn_email'));
  private _role  = signal<string | null>(localStorage.getItem('jrn_role'));

  readonly token    = this._token.asReadonly();
  readonly email    = this._email.asReadonly();
  readonly role     = this._role.asReadonly();
  readonly isLoggedIn = computed(() => !!this._token());
  readonly isAdmin    = computed(() => this._role() === 'ADMIN');

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API}/auth/login`, credentials).pipe(
      tap(res => {
        this._token.set(res.token);
        this._email.set(res.email);
        this._role.set(res.role);
        localStorage.setItem('jrn_token', res.token);
        localStorage.setItem('jrn_email', res.email);
        localStorage.setItem('jrn_role',  res.role);
      }),
    );
  }

  logout(): void {
    this._token.set(null);
    this._email.set(null);
    this._role.set(null);
    localStorage.removeItem('jrn_token');
    localStorage.removeItem('jrn_email');
    localStorage.removeItem('jrn_role');
    this.router.navigate(['/auth/login']);
  }
}
