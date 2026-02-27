import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router } from '@angular/router';
import { GuidesService } from '../../../core/services/guides.service';
import { GuideRequest } from '../../../core/models/guide.model';

@Component({
  selector: 'app-guide-form',
  imports: [ReactiveFormsModule],
  templateUrl: './guide-form.component.html',
  styleUrl: './guide-form.component.scss',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class GuideFormComponent implements OnInit {
  private fb = inject(FormBuilder);
  private guidesService = inject(GuidesService);
  private route = inject(ActivatedRoute);
  private router = inject(Router);

  loading  = signal(false);
  saving   = signal(false);
  error    = signal<string | null>(null);
  editId   = signal<number | null>(null);
  isEdit   = computed(() => this.editId() !== null);

  form = this.fb.group({
    titre:       ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    jours:       [1, [Validators.required, Validators.min(1)]],
    mobilite:    ['A_PIED', Validators.required],
    saison:      ['ETE', Validators.required],
    pourQui:     ['FAMILLE', Validators.required],
  });

  ngOnInit(): void {
    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editId.set(Number(id));
      this.loading.set(true);
      this.guidesService.getById(Number(id)).subscribe({
        next: guide => {
          this.form.patchValue({
            titre: guide.titre,
            description: guide.description ?? '',
            jours: guide.jours,
            mobilite: guide.mobilite,
            saison: guide.saison,
            pourQui: guide.pourQui,
          });
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Impossible de charger le guide.');
          this.loading.set(false);
        },
      });
    }
  }

  submit(): void {
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }
    this.saving.set(true);
    this.error.set(null);

    const payload: GuideRequest = {
      titre:       this.form.value.titre!,
      description: this.form.value.description ?? undefined,
      jours:       this.form.value.jours!,
      mobilite:    this.form.value.mobilite!,
      saison:      this.form.value.saison!,
      pourQui:     this.form.value.pourQui!,
    };

    const id = this.editId();
    const req = id
      ? this.guidesService.update(id, payload)
      : this.guidesService.create(payload);

    req.subscribe({
      next: guide => this.router.navigate(['/dashboard/guides', guide.id]),
      error: err  => {
        this.saving.set(false);
        this.error.set(err.error?.message ?? 'Erreur lors de la sauvegarde.');
      },
    });
  }

  cancel(): void {
    const id = this.editId();
    if (id) {
      this.router.navigate(['/dashboard/guides', id]);
    } else {
      this.router.navigate(['/dashboard/guides']);
    }
  }
}
