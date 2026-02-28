export type ActivityType = 'MUSEE' | 'CHATEAU' | 'ACTIVITE' | 'PARC' | 'GROTTE';

export interface Activity {
  id: number;
  titre: string;
  description?: string;
  type: ActivityType;
  adresse?: string;
  telephone?: string;
  siteInternet?: string;
  heureDebut?: string;
  duree: number;
  ordre: number;
  jour: number;
  latitude?: number;
  longitude?: number;
}

export interface ActivityRequest {
  titre: string;
  description?: string;
  type: string;
  adresse?: string;
  telephone?: string;
  siteInternet?: string;
  heureDebut?: string;
  duree: number;
  ordre: number;
  jour: number;
}

export const ACTIVITY_TYPE_LABELS: Record<ActivityType, string> = {
  MUSEE: 'Musée',
  CHATEAU: 'Château',
  ACTIVITE: 'Activité',
  PARC: 'Parc',
  GROTTE: 'Grotte',
};
