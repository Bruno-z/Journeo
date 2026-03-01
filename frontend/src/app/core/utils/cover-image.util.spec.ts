import { vi, describe, it, expect, beforeEach, afterEach } from 'vitest';
import { getCoverImage, fetchWikipediaCover } from './cover-image.util';

// ─── getCoverImage() ──────────────────────────────────────────────────────────

describe('getCoverImage()', () => {
  it('returns the Paris Unsplash photo for "Voyage à Paris"', () => {
    const url = getCoverImage('Voyage à Paris');
    expect(url).toContain('unsplash.com');
    expect(url).toContain('1502602898657');
  });

  it('returns the Tokyo photo for "Tokyo en famille"', () => {
    const url = getCoverImage('Tokyo en famille');
    expect(url).toContain('1540959733332');
  });

  it('returns the Rome photo when title contains "Italie"', () => {
    const url = getCoverImage('Circuit en Italie');
    expect(url).toContain('1552832230');
  });

  it('returns the summer Unsplash fallback for an unknown city with ETE season', () => {
    const url = getCoverImage('Voyage quelque part', 'ETE');
    expect(url).toContain('1476514525535');
  });

  it('returns the winter fallback for an unknown city with HIVER season', () => {
    const url = getCoverImage('Quelque part', 'HIVER');
    expect(url).toContain('1483728642387');
  });

  it('defaults to summer fallback when season is unrecognized', () => {
    const url = getCoverImage('Inconnu xyz', 'UNKNOWN_SEASON');
    expect(url).toContain('1476514525535');
  });

  it('URL always includes correct dimensions (800x500)', () => {
    const url = getCoverImage('Paris');
    expect(url).toContain('w=800');
    expect(url).toContain('h=500');
    expect(url).toContain('fit=crop');
  });

  it('is case-insensitive (PARIS matches paris keyword)', () => {
    const lower = getCoverImage('paris');
    const upper = getCoverImage('PARIS');
    expect(lower).toBe(upper);
  });
});

// ─── fetchWikipediaCover() ────────────────────────────────────────────────────

describe('fetchWikipediaCover()', () => {
  beforeEach(() => {
    vi.stubGlobal('fetch', vi.fn());
  });

  afterEach(() => {
    vi.unstubAllGlobals();
  });

  it('returns Unsplash URL instantly for known cities without calling fetch', async () => {
    const url = await fetchWikipediaCover('Voyage à Paris');
    expect(url).toContain('unsplash.com');
    expect(url).toContain('1502602898657');
    expect(fetch).not.toHaveBeenCalled();
  });

  it('returns Wikipedia thumbnail when FR wiki responds with a thumbnail', async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          thumbnail: {
            // Real Wikipedia thumbnail URLs come from upload.wikimedia.org, not wikipedia.org
            source:
              'https://upload.wikimedia.org/wikipedia/commons/thumb/X/Y/400px-Bruges.jpg',
          },
          type: 'standard',
        }),
    } as Response);

    const url = await fetchWikipediaCover('Bruges');
    expect(url).toContain('wikimedia.org');   // upload.wikimedia.org is the CDN
    expect(url).toContain('800px-Bruges.jpg');
  });

  it('upscales thumbnail from any size to 800px', async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          thumbnail: {
            source:
              'https://upload.wikimedia.org/wikipedia/commons/thumb/X/Y/220px-City.jpg',
          },
          type: 'standard',
        }),
    } as Response);

    // "Bruges" is not in DESTINATION_MAP so it will call Wikipedia API
    const url = await fetchWikipediaCover('Bruges');
    expect(url).toMatch(/800px-City\.jpg/);
    expect(url).not.toContain('220px-');
  });

  it('skips disambiguation pages and falls back to seasonal Unsplash', async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () =>
        Promise.resolve({
          thumbnail: {
            source: 'https://upload.wikimedia.org/thumb/X/Y/400px-Disamb.jpg',
          },
          type: 'disambiguation',
        }),
    } as Response);

    const url = await fetchWikipediaCover('Springfield');
    expect(url).toContain('unsplash.com');
  });

  it('falls back to EN wikipedia when FR has no thumbnail', async () => {
    vi.mocked(fetch)
      .mockResolvedValueOnce({
        ok: true,
        json: () => Promise.resolve({ type: 'standard' }), // FR — no thumbnail
      } as Response)
      .mockResolvedValueOnce({
        ok: true,
        json: () =>
          Promise.resolve({
            thumbnail: {
              source:
                'https://upload.wikimedia.org/thumb/X/Y/400px-ENCity.jpg',
            },
            type: 'standard',
          }),
      } as Response); // EN — has thumbnail

    const url = await fetchWikipediaCover('Ghent');
    expect(url).toContain('800px-ENCity.jpg');
  });

  it('returns winter Unsplash fallback when both wikis fail (network error)', async () => {
    vi.mocked(fetch).mockRejectedValue(new Error('network error'));

    const url = await fetchWikipediaCover('xyznowhereplace', 'HIVER');
    expect(url).toContain('unsplash.com');
    expect(url).toContain('1483728642387'); // HIVER photo id
  });

  it('returns default ETE fallback when both wikis fail and no season given', async () => {
    vi.mocked(fetch).mockRejectedValue(new Error('network error'));

    const url = await fetchWikipediaCover('xyznowhereplace');
    expect(url).toContain('unsplash.com');
    expect(url).toContain('1476514525535'); // ETE / DEFAULT photo id
  });

  it('strips travel words from title before querying Wikipedia', async () => {
    vi.mocked(fetch).mockResolvedValue({
      ok: true,
      json: () => Promise.resolve({ type: 'standard' }), // no thumbnail, just to capture call
    } as Response);

    await fetchWikipediaCover('Voyage à Bruges');

    // Wikipedia should be queried with "Bruges", not "Voyage à Bruges"
    const calledUrl = (vi.mocked(fetch).mock.calls[0][0] as string);
    expect(calledUrl).toContain('Bruges');
    expect(calledUrl).not.toContain('Voyage');
  });
});
