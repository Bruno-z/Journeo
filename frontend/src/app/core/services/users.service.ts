import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { User, UserRequest } from '../models/user.model';
import { Guide } from '../models/guide.model';

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class UsersService {
  private http = inject(HttpClient);

  getAll(): Observable<User[]> {
    return this.http.get<User[]>(`${API}/users`);
  }

  getById(id: number): Observable<User> {
    return this.http.get<User>(`${API}/users/${id}`);
  }

  getGuides(userId: number): Observable<Guide[]> {
    return this.http.get<Guide[]>(`${API}/users/${userId}/guides`);
  }

  create(user: UserRequest): Observable<User> {
    return this.http.post<User>(`${API}/users`, user);
  }

  update(id: number, user: UserRequest): Observable<User> {
    return this.http.put<User>(`${API}/users/${id}`, user);
  }

  changeRole(id: number, role: string): Observable<User> {
    return this.http.patch<User>(`${API}/users/${id}/role?role=${role}`, {});
  }

  delete(id: number): Observable<void> {
    return this.http.delete<void>(`${API}/users/${id}`);
  }
}
