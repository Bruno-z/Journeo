export type UserRole = 'ADMIN' | 'USER';

export interface User {
  id: number;
  email: string;
  role: UserRole;
}

export interface UserRequest {
  email: string;
  password?: string;
  role: string;
}
