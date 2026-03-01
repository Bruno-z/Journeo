import { ChangeDetectionStrategy, Component, computed, inject, OnInit, signal } from '@angular/core';
import { CommonModule } from '@angular/common';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { from } from 'rxjs';
import { debounceTime, distinctUntilChanged, switchMap, tap } from 'rxjs/operators';
import { GuidesService } from '../../../core/services/guides.service';
import { GuideRequest, MOBILITE_LABELS, PUBLIC_CIBLE_LABELS, SAISON_LABELS } from '../../../core/models/guide.model';
import { getCoverImage, fetchWikipediaCover } from '../../../core/utils/cover-image.util';

@Component({
  selector: 'app-guide-form',
  imports: [CommonModule, ReactiveFormsModule, RouterLink],
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

  previewSaison = signal('ETE');

  // Async cover: starts with default, updated via Wikipedia lookup
  coverPreview  = signal<string>(getCoverImage('voyage', 'ETE'));
  coverLoading  = signal(false);

  // Labels pour les tuiles
  readonly saisonLabels      = SAISON_LABELS;
  readonly mobiliteLabels    = MOBILITE_LABELS;
  readonly publicCibleLabels = PUBLIC_CIBLE_LABELS;
  readonly saisons   = Object.keys(SAISON_LABELS) as Array<keyof typeof SAISON_LABELS>;
  readonly mobilites = Object.keys(MOBILITE_LABELS) as Array<keyof typeof MOBILITE_LABELS>;
  readonly cibles    = Object.keys(PUBLIC_CIBLE_LABELS) as Array<keyof typeof PUBLIC_CIBLE_LABELS>;

  form = this.fb.group({
    titre:       ['', [Validators.required, Validators.minLength(2)]],
    description: [''],
    jours:       [1, [Validators.required, Validators.min(1)]],
    mobilite:    ['A_PIED', Validators.required],
    saison:      ['ETE', Validators.required],
    pourQui:     ['FAMILLE', Validators.required],
  });

  ngOnInit(): void {
    // Live cover preview: debounce typing, switchMap cancels in-flight requests
    this.form.get('titre')!.valueChanges.pipe(
      debounceTime(600),
      distinctUntilChanged(),
      tap(() => this.coverLoading.set(true)),
      switchMap(v => from(fetchWikipediaCover((v ?? '').trim() || 'voyage', this.previewSaison()))),
    ).subscribe({
      next: url => { this.coverPreview.set(url); this.coverLoading.set(false); },
      error: ()  => this.coverLoading.set(false),
    });

    this.form.get('saison')?.valueChanges.subscribe(v => this.previewSaison.set(v ?? 'ETE'));

    const id = this.route.snapshot.paramMap.get('id');
    if (id) {
      this.editId.set(Number(id));
      this.loading.set(true);
      this.guidesService.getById(Number(id)).subscribe({
        next: guide => {
          this.form.patchValue({
            titre:       guide.titre,
            description: guide.description ?? '',
            jours:       guide.jours,
            mobilite:    guide.mobilite,
            saison:      guide.saison,
            pourQui:     guide.pourQui,
          });
          this.previewSaison.set(guide.saison);
          // Load cover for existing guide title immediately
          this.coverLoading.set(true);
          fetchWikipediaCover(guide.titre, guide.saison).then(url => {
            this.coverPreview.set(url);
            this.coverLoading.set(false);
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

  setVal(field: string, value: any): void {
    this.form.get(field)?.setValue(value);
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
