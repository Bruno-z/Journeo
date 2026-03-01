import { Activity } from './activity.model';
import { User } from './user.model';

export type Mobilite = 'VOITURE' | 'VELO' | 'A_PIED' | 'MOTO' | 'METRO' | 'TRANSPORTS_EN_COMMUN';
export type Saison = 'ETE' | 'PRINTEMPS' | 'AUTOMNE' | 'HIVER';
export type PublicCible = 'FAMILLE' | 'SEUL' | 'EN_GROUPE' | 'ENTRE_AMIS';

export interface Guide {
  id: number;
  titre: string;
  description?: string;
  jours: number;
  mobilite: Mobilite;
  saison: Saison;
  pourQui: PublicCible;
  activities: Activity[];
  users?: User[];
}

export interface GuideRequest {
  titre: string;
  description?: string;
  jours: number;
  mobilite: string;
  saison: string;
  pourQui: string;
}

export const MOBILITE_LABELS: Record<Mobilite, string> = {
  VOITURE: 'Voiture',
  VELO: 'Vélo',
  A_PIED: 'À pied',
  MOTO: 'Moto',
  METRO: 'Métro',
  TRANSPORTS_EN_COMMUN: 'Transports en commun',
};

export const SAISON_LABELS: Record<Saison, string> = {
  ETE: 'Été',
  PRINTEMPS: 'Printemps',
  AUTOMNE: 'Automne',
  HIVER: 'Hiver',
};

export const PUBLIC_CIBLE_LABELS: Record<PublicCible, string> = {
  FAMILLE: 'Famille',
  SEUL: 'Solo',
  EN_GROUPE: 'En groupe',
  ENTRE_AMIS: 'Entre amis',
};
