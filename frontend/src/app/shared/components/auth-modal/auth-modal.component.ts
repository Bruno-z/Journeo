import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { AuthModalService } from './auth-modal.service';
import { AuthService } from '../../../core/services/auth.service';
import { UsersService } from '../../../core/services/users.service';

@Component({
  selector: 'app-auth-modal',
  imports: [ReactiveFormsModule],
  templateUrl: './auth-modal.component.html',
  styleUrl: './auth-modal.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AuthModalComponent {
  protected modal = inject(AuthModalService);
  private fb          = inject(FormBuilder);
  private authService = inject(AuthService);
  private usersService = inject(UsersService);
  private router      = inject(Router);

  // Login form
  loginLoading = signal(false);
  loginError   = signal<string | null>(null);

  loginForm = this.fb.group({
    email:    ['', [Validators.required, Validators.email]],
    password: ['', Validators.required],
  });

  submitLogin(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }
    this.loginLoading.set(true);
    this.loginError.set(null);
    const { email, password } = this.loginForm.value;
    this.authService.login({ email: email!, password: password! }).subscribe({
      next: () => {
        this.modal.close();
        this.router.navigate(['/dashboard']);
      },
      error: err => {
        this.loginLoading.set(false);
        this.loginError.set(
          err.status === 401
            ? 'Email ou mot de passe incorrect.'
            : 'Erreur de connexion. Réessayez.',
        );
      },
    });
  }

  // Register form
  registerLoading = signal(false);
  registerError   = signal<string | null>(null);
  registerSuccess = signal(false);

  registerForm = this.fb.group({
    firstName: ['', Validators.required],
    lastName:  ['', Validators.required],
    email:     ['', [Validators.required, Validators.email]],
    password:  ['', [Validators.required, Validators.minLength(6)]],
  });

  submitRegister(): void {
    if (this.registerForm.invalid) {
      this.registerForm.markAllAsTouched();
      return;
    }
    this.registerLoading.set(true);
    this.registerError.set(null);
    const { firstName, lastName, email, password } = this.registerForm.value;
    this.usersService.create({ firstName: firstName!, lastName: lastName!, email: email!, password: password!, role: 'USER' }).subscribe({
      next: () => {
        this.registerSuccess.set(true);
        this.registerLoading.set(false);
        setTimeout(() => {
          this.registerSuccess.set(false);
          this.registerForm.reset();
          this.modal.switchTab('login');
        }, 1500);
      },
      error: err => {
        this.registerLoading.set(false);
        this.registerError.set(
          err.status === 409
            ? 'Cet email est déjà utilisé.'
            : 'Erreur lors de la création du compte.',
        );
      },
    });
  }
}
