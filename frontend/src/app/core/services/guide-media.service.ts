import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

export interface GuideMedia {
  id: number;
  fileName: string;
  originalName: string;
  fileType: 'IMAGE' | 'VIDEO';
  contentType: string;
  size: number;
  uploadedAt: string;
  guideId: number;
  url: string;
}

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class GuideMediaService {
  private http = inject(HttpClient);

  getForGuide(guideId: number): Observable<GuideMedia[]> {
    return this.http.get<GuideMedia[]>(`${API}/guides/${guideId}/media`);
  }

  upload(guideId: number, file: File): Observable<GuideMedia> {
    const form = new FormData();
    form.append('file', file);
    return this.http.post<GuideMedia>(`${API}/guides/${guideId}/media`, form);
  }

  delete(guideId: number, mediaId: number): Observable<void> {
    return this.http.delete<void>(`${API}/guides/${guideId}/media/${mediaId}`);
  }
}
