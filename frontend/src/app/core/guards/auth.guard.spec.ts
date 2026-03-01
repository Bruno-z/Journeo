import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';

import { authGuard } from './auth.guard';
import { AuthService } from '../services/auth.service';

function setup(isLoggedIn: boolean) {
  const mockAuth = { isLoggedIn: () => isLoggedIn };
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: mockAuth },
    ],
  });
}

describe('authGuard', () => {
  afterEach(() => localStorage.clear());

  it('returns true when user is logged in', () => {
    setup(true);
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any),
    );
    expect(result).toBe(true);
  });

  it('returns a UrlTree when user is not logged in', () => {
    setup(false);
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any),
    );
    expect(result).toBeInstanceOf(UrlTree);
  });

  it('redirects to /auth/login when user is not logged in', () => {
    setup(false);
    const result = TestBed.runInInjectionContext(() =>
      authGuard({} as any, {} as any),
    );
    const serialized = TestBed.inject(Router).serializeUrl(result as UrlTree);
    expect(serialized).toBe('/auth/login');
  });
});
