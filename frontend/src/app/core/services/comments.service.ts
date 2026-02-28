import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface Comment {
  id: number;
  content: string;
  rating: number;
  authorEmail: string;
  authorFirstName?: string;
  authorLastName?: string;
  authorId: number;
  createdAt: string;
  guideId: number;
}

export interface CommentRequest {
  content: string;
  rating: number;
}

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class CommentsService {
  private http = inject(HttpClient);

  getForGuide(guideId: number): Observable<Comment[]> {
    return this.http.get<Comment[]>(`${API}/guides/${guideId}/comments`);
  }

  add(guideId: number, payload: CommentRequest): Observable<Comment> {
    return this.http.post<Comment>(`${API}/guides/${guideId}/comments`, payload);
  }

  delete(guideId: number, commentId: number): Observable<void> {
    return this.http.delete<void>(`${API}/guides/${guideId}/comments/${commentId}`);
  }
}
