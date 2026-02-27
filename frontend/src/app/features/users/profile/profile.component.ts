import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { AuthService } from '../../../core/services/auth.service';
import { UsersService } from '../../../core/services/users.service';

@Component({
  selector: 'app-profile',
  imports: [ReactiveFormsModule],
  templateUrl: './profile.component.html',
  styleUrl: './profile.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class ProfileComponent {
  protected auth = inject(AuthService);
  private fb = inject(FormBuilder);
  private usersService = inject(UsersService);

  saving   = signal(false);
  success  = signal(false);
  error    = signal<string | null>(null);

  passwordForm = this.fb.group({
    newPassword: ['', [Validators.required, Validators.minLength(6)]],
    confirm:     ['', Validators.required],
  });

  logout(): void {
    this.auth.logout();
  }
}
