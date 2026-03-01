import { vi } from 'vitest';
import { TestBed } from '@angular/core/testing';
import {
  HttpClient,
  provideHttpClient,
  withInterceptors,
} from '@angular/common/http';
import {
  HttpTestingController,
  provideHttpClientTesting,
} from '@angular/common/http/testing';

import { errorInterceptor } from './error.interceptor';
import { AuthService } from '../services/auth.service';

function setup(isLoggedIn: boolean) {
  const mockAuth = {
    isLoggedIn: () => isLoggedIn,
    logout: vi.fn(),
  };
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([errorInterceptor])),
      provideHttpClientTesting(),
      { provide: AuthService, useValue: mockAuth },
    ],
  });
  return {
    http: TestBed.inject(HttpClient),
    httpMock: TestBed.inject(HttpTestingController),
    mockAuth,
  };
}

describe('errorInterceptor', () => {
  afterEach(() => localStorage.clear());

  it('calls auth.logout() on 401 when user is logged in', () => {
    const { http, httpMock, mockAuth } = setup(true);

    http.get('/api/guides').subscribe({ error: () => {} });
    httpMock
      .expectOne('/api/guides')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(mockAuth.logout).toHaveBeenCalledOnce();
    httpMock.verify();
  });

  it('does NOT call logout on 401 when user is already logged out', () => {
    const { http, httpMock, mockAuth } = setup(false);

    http.get('/api/guides').subscribe({ error: () => {} });
    httpMock
      .expectOne('/api/guides')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(mockAuth.logout).not.toHaveBeenCalled();
    httpMock.verify();
  });

  it('does NOT call logout on 403 Forbidden', () => {
    const { http, httpMock, mockAuth } = setup(true);

    http.get('/api/guides').subscribe({ error: () => {} });
    httpMock
      .expectOne('/api/guides')
      .flush(null, { status: 403, statusText: 'Forbidden' });

    expect(mockAuth.logout).not.toHaveBeenCalled();
    httpMock.verify();
  });

  it('does NOT call logout on 500 Server Error', () => {
    const { http, httpMock, mockAuth } = setup(true);

    http.get('/api/guides').subscribe({ error: () => {} });
    httpMock
      .expectOne('/api/guides')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });

    expect(mockAuth.logout).not.toHaveBeenCalled();
    httpMock.verify();
  });

  it('propagates the error to the subscriber regardless of status', () => {
    const { http, httpMock } = setup(true);

    let errorStatus: number | undefined;
    http.get('/api/guides').subscribe({ error: e => (errorStatus = e.status) });
    httpMock
      .expectOne('/api/guides')
      .flush(null, { status: 500, statusText: 'Internal Server Error' });

    expect(errorStatus).toBe(500);
    httpMock.verify();
  });

  it('propagates the 401 error to the subscriber even after calling logout', () => {
    const { http, httpMock } = setup(true);

    let errorStatus: number | undefined;
    http.get('/api/guides').subscribe({ error: e => (errorStatus = e.status) });
    httpMock
      .expectOne('/api/guides')
      .flush(null, { status: 401, statusText: 'Unauthorized' });

    expect(errorStatus).toBe(401);
    httpMock.verify();
  });
});
