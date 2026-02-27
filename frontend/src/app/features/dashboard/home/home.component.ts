import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../core/services/auth.service';
import { GuidesService } from '../../../core/services/guides.service';
import { UsersService } from '../../../core/services/users.service';
import { Guide } from '../../../core/models/guide.model';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.component.html',
  styleUrl: './home.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class HomeComponent implements OnInit {
  protected auth = inject(AuthService);
  private guidesService = inject(GuidesService);
  private usersService = inject(UsersService);

  guides    = signal<Guide[]>([]);
  userCount = signal<number>(0);
  loading   = signal(true);

  recentGuides = computed(() => this.guides().slice(0, 4));
  guideCount   = computed(() => this.guides().length);

  ngOnInit(): void {
    this.guidesService.getAll().subscribe({
      next: data => {
        this.guides.set(data);
        this.loading.set(false);
      },
      error: () => this.loading.set(false),
    });

    if (this.auth.isAdmin()) {
      this.usersService.getAll().subscribe({
        next: users => this.userCount.set(users.length),
        error: () => {},
      });
    }
  }
}
