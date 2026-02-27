import { inject, Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Activity, ActivityRequest } from '../models/activity.model';

const API = 'http://localhost:8080/api';

@Injectable({ providedIn: 'root' })
export class ActivitiesService {
  private http = inject(HttpClient);

  getByGuide(guideId: number): Observable<Activity[]> {
    return this.http.get<Activity[]>(`${API}/activities/guide/${guideId}`);
  }

  create(guideId: number, activity: ActivityRequest): Observable<Activity> {
    return this.http.post<Activity>(`${API}/activities/guide/${guideId}`, activity);
  }

  update(activityId: number, activity: ActivityRequest): Observable<Activity> {
    return this.http.put<Activity>(`${API}/activities/${activityId}`, activity);
  }

  delete(activityId: number): Observable<void> {
    return this.http.delete<void>(`${API}/activities/${activityId}`);
  }
}
