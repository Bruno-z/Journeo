import { TestBed } from '@angular/core/testing';
import { Router, UrlTree } from '@angular/router';
import { provideRouter } from '@angular/router';

import { adminGuard } from './admin.guard';
import { AuthService } from '../services/auth.service';

function setup(isAdmin: boolean) {
  const mockAuth = { isAdmin: () => isAdmin };
  TestBed.configureTestingModule({
    providers: [
      provideRouter([]),
      { provide: AuthService, useValue: mockAuth },
    ],
  });
}

describe('adminGuard', () => {
  afterEach(() => localStorage.clear());

  it('returns true when user has ADMIN role', () => {
    setup(true);
    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as any),
    );
    expect(result).toBe(true);
  });

  it('returns a UrlTree when user is not an admin', () => {
    setup(false);
    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as any),
    );
    expect(result).toBeInstanceOf(UrlTree);
  });

  it('redirects to /dashboard when user is not an admin', () => {
    setup(false);
    const result = TestBed.runInInjectionContext(() =>
      adminGuard({} as any, {} as any),
    );
    const serialized = TestBed.inject(Router).serializeUrl(result as UrlTree);
    expect(serialized).toBe('/dashboard');
  });
});
