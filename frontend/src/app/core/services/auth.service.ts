import { computed, inject, Injectable, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { tap } from 'rxjs/operators';
import { Observable } from 'rxjs';
import { LoginRequest, LoginResponse } from '../models/auth.model';
import { environment } from '../../../environments/environment';

const API = environment.apiUrl;

@Injectable({ providedIn: 'root' })
export class AuthService {
  private http = inject(HttpClient);
  private router = inject(Router);

  private _token     = signal<string | null>(localStorage.getItem('jrn_token'));
  private _email     = signal<string | null>(localStorage.getItem('jrn_email'));
  private _role      = signal<string | null>(localStorage.getItem('jrn_role'));
  private _firstName = signal<string | null>(localStorage.getItem('jrn_firstName'));
  private _lastName  = signal<string | null>(localStorage.getItem('jrn_lastName'));

  readonly token       = this._token.asReadonly();
  readonly email       = this._email.asReadonly();
  readonly role        = this._role.asReadonly();
  readonly firstName   = this._firstName.asReadonly();
  readonly lastName    = this._lastName.asReadonly();
  readonly displayName = computed(() => {
    const f = this._firstName();
    const l = this._lastName();
    if (f && l) return `${f} ${l}`;
    if (f) return f;
    return this._email() ?? '';
  });
  readonly isLoggedIn = computed(() => !!this._token());
  readonly isAdmin    = computed(() => this._role() === 'ADMIN');

  login(credentials: LoginRequest): Observable<LoginResponse> {
    return this.http.post<LoginResponse>(`${API}/auth/login`, credentials).pipe(
      tap(res => {
        this._token.set(res.token);
        this._email.set(res.email);
        this._role.set(res.role);
        this._firstName.set(res.firstName ?? null);
        this._lastName.set(res.lastName ?? null);
        localStorage.setItem('jrn_token', res.token);
        localStorage.setItem('jrn_email', res.email);
        localStorage.setItem('jrn_role',  res.role);
        if (res.firstName) localStorage.setItem('jrn_firstName', res.firstName);
        if (res.lastName)  localStorage.setItem('jrn_lastName',  res.lastName);
      }),
    );
  }

  logout(): void {
    this._token.set(null);
    this._email.set(null);
    this._role.set(null);
    this._firstName.set(null);
    this._lastName.set(null);
    localStorage.removeItem('jrn_token');
    localStorage.removeItem('jrn_email');
    localStorage.removeItem('jrn_role');
    localStorage.removeItem('jrn_firstName');
    localStorage.removeItem('jrn_lastName');
    this.router.navigate(['/auth/login']);
  }
}
