import { ChangeDetectionStrategy, Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { UsersService } from '../../../core/services/users.service';

@Component({
  selector: 'app-register',
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './register.component.html',
  styleUrl: './register.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class RegisterComponent {
  private fb = inject(FormBuilder);
  private usersService = inject(UsersService);
  private router = inject(Router);

  loading = signal(false);
  error   = signal<string | null>(null);
  success = signal(false);

  form = this.fb.group({
    firstName: ['', [Validators.required, Validators.minLength(2)]],
    lastName:  ['', [Validators.required, Validators.minLength(2)]],
    email:     ['', [Validators.required, Validators.email]],
    password:  ['', [Validators.required, Validators.minLength(6)]],
  });

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.loading.set(true);
    this.error.set(null);

    const { email, password, firstName, lastName } = this.form.value;
    this.usersService.create({ email: email!, password: password!, role: 'USER', firstName: firstName!, lastName: lastName! }).subscribe({
      next: () => {
        this.success.set(true);
        this.loading.set(false);
        setTimeout(() => this.router.navigate(['/auth/login']), 1500);
      },
      error: err => {
        this.loading.set(false);
        this.error.set(
          err.status === 409
            ? 'Cet email est déjà utilisé.'
            : 'Erreur lors de la création du compte.',
        );
      },
    });
  }
}
