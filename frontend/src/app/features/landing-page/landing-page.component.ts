import { Component, ViewChild, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { LoginComponent } from '../auth/login/login.component';
import { RegisterComponent } from '../auth/register/register.component';

@Component({
  selector: 'app-auth-modal',
  standalone: true,
  imports: [CommonModule, LoginComponent, RegisterComponent],
  template: `
    <div class="modal-backdrop" *ngIf="visible()">
      <div class="modal">
        <button class="close" (click)="close()">✕</button>
        <app-login></app-login> <!-- ou app-register selon besoin -->
      </div>
    </div>
  `,
  styleUrls: ['./auth-modal.component.scss'],
})
export class AuthModalComponent {
  visible = signal(false);

  open() { this.visible.set(true); }
  close() { this.visible.set(false); }
}