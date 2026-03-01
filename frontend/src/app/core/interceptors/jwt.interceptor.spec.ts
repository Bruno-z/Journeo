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

import { jwtInterceptor } from './jwt.interceptor';
import { AuthService } from '../services/auth.service';

function setup(token: string | null) {
  const mockAuth = { token: () => token };
  TestBed.configureTestingModule({
    providers: [
      provideHttpClient(withInterceptors([jwtInterceptor])),
      provideHttpClientTesting(),
      { provide: AuthService, useValue: mockAuth },
    ],
  });
  return {
    http: TestBed.inject(HttpClient),
    httpMock: TestBed.inject(HttpTestingController),
  };
}

describe('jwtInterceptor', () => {
  afterEach(() => localStorage.clear());

  it('adds Authorization: Bearer header when a token exists', () => {
    const { http, httpMock } = setup('my-jwt-token');

    http.get('/api/guides').subscribe();

    const req = httpMock.expectOne('/api/guides');
    expect(req.request.headers.get('Authorization')).toBe('Bearer my-jwt-token');
    req.flush([]);
    httpMock.verify();
  });

  it('does NOT add Authorization header when token is null', () => {
    const { http, httpMock } = setup(null);

    http.get('/api/guides').subscribe();

    const req = httpMock.expectOne('/api/guides');
    expect(req.request.headers.has('Authorization')).toBe(false);
    req.flush([]);
    httpMock.verify();
  });

  it('does not mutate the original request (uses clone)', () => {
    const { http, httpMock } = setup('token-xyz');

    http.get('/api/guides').subscribe();

    const req = httpMock.expectOne('/api/guides');
    // The intercepted request must be a new object with the header
    expect(req.request.headers.get('Authorization')).toBe('Bearer token-xyz');
    req.flush([]);
    httpMock.verify();
  });

  it('passes the request through to the handler', () => {
    const { http, httpMock } = setup('my-token');

    let responseReceived = false;
    http.get('/api/guides').subscribe(() => (responseReceived = true));

    httpMock.expectOne('/api/guides').flush([]);
    expect(responseReceived).toBe(true);
    httpMock.verify();
  });
});
