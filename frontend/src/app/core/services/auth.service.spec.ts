import { TestBed } from '@angular/core/testing';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';
import { provideHttpClient } from '@angular/common/http';
import { provideRouter } from '@angular/router';

import { AuthService } from './auth.service';

function setup() {
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(),
      provideHttpClientTesting(),
      provideRouter([]),
    ],
  });
  return {
    service: TestBed.inject(AuthService),
    httpMock: TestBed.inject(HttpTestingController),
  };
}

const ADMIN_RESPONSE = {
  token: 'jwt-abc-123',
  email: 'admin@hws.com',
  role: 'ADMIN',
  firstName: 'Alice',
  lastName: 'Admin',
};

// ─── Initial state ────────────────────────────────────────────────────────────

describe('AuthService — initial state (empty localStorage)', () => {
  beforeEach(() => localStorage.clear());

  it('token is null when localStorage is empty', () => {
    const { service, httpMock } = setup();
    expect(service.token()).toBeNull();
    httpMock.verify();
  });

  it('isLoggedIn is false when no token', () => {
    const { service, httpMock } = setup();
    expect(service.isLoggedIn()).toBe(false);
    httpMock.verify();
  });

  it('isAdmin is false when no role stored', () => {
    const { service, httpMock } = setup();
    expect(service.isAdmin()).toBe(false);
    httpMock.verify();
  });
});

describe('AuthService — initial state (pre-populated localStorage)', () => {
  beforeEach(() => {
    localStorage.setItem('jrn_token', 'persisted-token');
    localStorage.setItem('jrn_role', 'ADMIN');
    localStorage.setItem('jrn_firstName', 'Alice');
    localStorage.setItem('jrn_lastName', 'Admin');
  });

  afterEach(() => localStorage.clear());

  it('reads token from localStorage at startup', () => {
    const { service, httpMock } = setup();
    expect(service.token()).toBe('persisted-token');
    httpMock.verify();
  });

  it('isLoggedIn is true when token exists in localStorage', () => {
    const { service, httpMock } = setup();
    expect(service.isLoggedIn()).toBe(true);
    httpMock.verify();
  });

  it('isAdmin is true when role ADMIN is in localStorage', () => {
    const { service, httpMock } = setup();
    expect(service.isAdmin()).toBe(true);
    httpMock.verify();
  });

  it('displayName is "firstName lastName" from localStorage', () => {
    const { service, httpMock } = setup();
    expect(service.displayName()).toBe('Alice Admin');
    httpMock.verify();
  });
});

// ─── login() ─────────────────────────────────────────────────────────────────

describe('AuthService — login()', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.clear();
    ({ service, httpMock } = setup());
  });

  afterEach(() => httpMock.verify());

  it('stores token in signal after successful login', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock.expectOne(r => r.url.includes('/auth/login')).flush(ADMIN_RESPONSE);
    expect(service.token()).toBe('jwt-abc-123');
  });

  it('persists token to localStorage', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock.expectOne(r => r.url.includes('/auth/login')).flush(ADMIN_RESPONSE);
    expect(localStorage.getItem('jrn_token')).toBe('jwt-abc-123');
  });

  it('sets isLoggedIn to true', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock.expectOne(r => r.url.includes('/auth/login')).flush(ADMIN_RESPONSE);
    expect(service.isLoggedIn()).toBe(true);
  });

  it('sets isAdmin to true for ADMIN role', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock.expectOne(r => r.url.includes('/auth/login')).flush(ADMIN_RESPONSE);
    expect(service.isAdmin()).toBe(true);
  });

  it('sets isAdmin to false for USER role', () => {
    service.login({ email: 'user1@hws.com', password: 'user123' }).subscribe();
    httpMock
      .expectOne(r => r.url.includes('/auth/login'))
      .flush({ ...ADMIN_RESPONSE, role: 'USER' });
    expect(service.isAdmin()).toBe(false);
  });

  it('stores firstName and lastName in localStorage', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock.expectOne(r => r.url.includes('/auth/login')).flush(ADMIN_RESPONSE);
    expect(localStorage.getItem('jrn_firstName')).toBe('Alice');
    expect(localStorage.getItem('jrn_lastName')).toBe('Admin');
  });

  it('computes displayName as "firstName lastName"', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock.expectOne(r => r.url.includes('/auth/login')).flush(ADMIN_RESPONSE);
    expect(service.displayName()).toBe('Alice Admin');
  });

  it('falls back displayName to firstName alone when lastName is missing', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock
      .expectOne(r => r.url.includes('/auth/login'))
      .flush({ ...ADMIN_RESPONSE, lastName: undefined });
    expect(service.displayName()).toBe('Alice');
  });

  it('falls back displayName to email when no names provided', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    httpMock.expectOne(r => r.url.includes('/auth/login')).flush({
      token: 'jwt-abc-123',
      email: 'admin@hws.com',
      role: 'ADMIN',
    });
    expect(service.displayName()).toBe('admin@hws.com');
  });

  it('sends POST to /auth/login with credentials', () => {
    service.login({ email: 'admin@hws.com', password: 'admin123' }).subscribe();
    const req = httpMock.expectOne(r => r.url.includes('/auth/login'));
    expect(req.request.method).toBe('POST');
    expect(req.request.body).toEqual({ email: 'admin@hws.com', password: 'admin123' });
    req.flush(ADMIN_RESPONSE);
  });
});

// ─── logout() ─────────────────────────────────────────────────────────────────

describe('AuthService — logout()', () => {
  let service: AuthService;
  let httpMock: HttpTestingController;

  beforeEach(() => {
    localStorage.setItem('jrn_token', 'jwt-abc-123');
    localStorage.setItem('jrn_email', 'admin@hws.com');
    localStorage.setItem('jrn_role', 'ADMIN');
    localStorage.setItem('jrn_firstName', 'Alice');
    localStorage.setItem('jrn_lastName', 'Admin');
    ({ service, httpMock } = setup());
  });

  afterEach(() => {
    httpMock.verify();
    localStorage.clear();
  });

  it('clears token signal', () => {
    service.logout();
    expect(service.token()).toBeNull();
  });

  it('sets isLoggedIn to false', () => {
    service.logout();
    expect(service.isLoggedIn()).toBe(false);
  });

  it('clears email, role, firstName, lastName signals', () => {
    service.logout();
    expect(service.email()).toBeNull();
    expect(service.role()).toBeNull();
    expect(service.firstName()).toBeNull();
    expect(service.lastName()).toBeNull();
  });

  it('removes jrn_token from localStorage', () => {
    service.logout();
    expect(localStorage.getItem('jrn_token')).toBeNull();
  });

  it('removes all jrn_* keys from localStorage', () => {
    service.logout();
    expect(localStorage.getItem('jrn_email')).toBeNull();
    expect(localStorage.getItem('jrn_role')).toBeNull();
    expect(localStorage.getItem('jrn_firstName')).toBeNull();
    expect(localStorage.getItem('jrn_lastName')).toBeNull();
  });
});
