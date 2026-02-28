import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { UsersService } from '../../../core/services/users.service';
import { User } from '../../../core/models/user.model';

@Component({
  selector: 'app-users-list',
  imports: [RouterLink],
  templateUrl: './users-list.component.html',
  styleUrl: './users-list.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class UsersListComponent implements OnInit {
  protected auth = inject(AuthService);
  private usersService = inject(UsersService);

  users        = signal<User[]>([]);
  loading      = signal(true);
  error        = signal<string | null>(null);
  deleting     = signal<number | null>(null);
  changingRole = signal<number | null>(null);
  menuOpen     = signal<number | null>(null);

  adminCount   = computed(() => this.users().filter(u => u.role === 'ADMIN').length);
  adminPercent = computed(() => {
    const total = this.users().length;
    return total === 0 ? 0 : Math.round((this.adminCount() / total) * 100);
  });

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.usersService.getAll().subscribe({
      next: data => { this.users.set(data); this.loading.set(false); },
      error: ()   => { this.error.set('Impossible de charger les utilisateurs.'); this.loading.set(false); },
    });
  }

  toggleMenu(userId: number): void {
    this.menuOpen.update(v => v === userId ? null : userId);
  }

  toggleRole(user: User): void {
    const newRole = user.role === 'ADMIN' ? 'USER' : 'ADMIN';
    this.changingRole.set(user.id);
    this.menuOpen.set(null);
    this.usersService.changeRole(user.id, newRole).subscribe({
      next: updated => {
        this.users.update(us => us.map(u => u.id === updated.id ? updated : u));
        this.changingRole.set(null);
      },
      error: () => {
        this.changingRole.set(null);
        alert('Erreur lors du changement de rôle.');
      },
    });
  }

  delete(user: User): void {
    if (!confirm(`Supprimer l'utilisateur "${user.email}" ?`)) return;
    this.deleting.set(user.id);
    this.menuOpen.set(null);
    this.usersService.delete(user.id).subscribe({
      next: () => {
        this.users.update(us => us.filter(u => u.id !== user.id));
        this.deleting.set(null);
      },
      error: () => {
        this.deleting.set(null);
        alert('Erreur lors de la suppression.');
      },
    });
  }

  initials(user: User): string {
    if (user.firstName && user.lastName) {
      return (user.firstName[0] + user.lastName[0]).toUpperCase();
    }
    return (user.firstName ?? user.email)[0].toUpperCase();
  }

  fullName(user: User): string {
    if (user.firstName && user.lastName) return `${user.firstName} ${user.lastName}`;
    return user.email.split('@')[0];
  }
}
