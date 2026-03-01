import { Injectable, signal } from '@angular/core';

export type AuthTab = 'login' | 'register';

@Injectable({ providedIn: 'root' })
export class AuthModalService {
  readonly isOpen    = signal(false);
  readonly activeTab = signal<AuthTab>('login');

  open(tab: AuthTab = 'login'): void {
    this.activeTab.set(tab);
    this.isOpen.set(true);
    document.body.style.overflow = 'hidden';
  }

  close(): void {
    this.isOpen.set(false);
    document.body.style.overflow = '';
  }

  switchTab(tab: AuthTab): void {
    this.activeTab.set(tab);
  }
}
