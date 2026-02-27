import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Guide, GuideRequest } from '../models/guide.model';

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class GuidesService {
  private http = inject(HttpClient);

  getAll(): Observable<Guide[]> {
    return this.http.get<Guide[]>(`${API}/guides`);
  }

  getById(id: number): Observable<Guide> {
    return this.http.get<Guide>(`${API}/guides/${id}`);
  }

  create(guide: GuideRequest): Observable<Guide> {
    return this.http.post<Guide>(`${API}/guides`, guide);
  }

  update(id: number, guide: GuideRequest): Observable<Guide> {
    return this.http.put<Guide>(`${API}/guides/${id}`, guide);
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/guides/${id}`);
  }

  addUser(guideId: number, userId: number): Observable<Guide> {
    return this.http.post<Guide>(`${API}/guides/${guideId}/users/${userId}`, {});
  }

  removeUser(guideId: number, userId: number): Observable<Guide> {
    return this.http.delete<Guide>(`${API}/guides/${guideId}/users/${userId}`);
  }
}
