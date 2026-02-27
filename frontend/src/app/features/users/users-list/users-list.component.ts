import { ChangeDetectionStrategy, Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
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
  private usersService = inject(UsersService);

  users    = signal<User[]>([]);
  loading  = signal(true);
  error    = signal<string | null>(null);
  deleting = signal<number | null>(null);

  ngOnInit(): void {
    this.load();
  }

  load(): void {
    this.usersService.getAll().subscribe({
      next: data => { this.users.set(data); this.loading.set(false); },
      error: ()   => { this.error.set('Impossible de charger les utilisateurs.'); this.loading.set(false); },
    });
  }

  delete(user: User): void {
    if (!confirm(`Supprimer l'utilisateur "${user.email}" ?`)) return;
    this.deleting.set(user.id);
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
}
