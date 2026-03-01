export type UserRole = 'ADMIN' | 'USER';

export interface User {
  id: number;
  email: string;
  role: UserRole;
  firstName?: string;
  lastName?: string;
}

export interface UserRequest {
  email: string;
  password?: string;
  role: string;
  firstName?: string;
  lastName?: string;
}
