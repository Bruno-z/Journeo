import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';

import { publicGuard } from './public.guard';
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

describe('publicGuard', () => {
  afterEach(() => localStorage.clear());

  it('returns true (allows access) when user is NOT logged in', () => {
    setup(false);
    const result = TestBed.runInInjectionContext(() =>
      publicGuard({} as any, {} as any),
    );
    expect(result).toBe(true);
  });

  it('returns a UrlTree when user is already logged in', () => {
    setup(true);
    const result = TestBed.runInInjectionContext(() =>
      publicGuard({} as any, {} as any),
    );
    expect(result).toBeInstanceOf(UrlTree);
  });

  it('redirects logged-in users to /dashboard', () => {
    setup(true);
    const result = TestBed.runInInjectionContext(() =>
      publicGuard({} as any, {} as any),
    );
    const serialized = TestBed.inject(Router).serializeUrl(result as UrlTree);
    expect(serialized).toBe('/dashboard');
  });
});
