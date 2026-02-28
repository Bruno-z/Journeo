/**
 * Returns an Unsplash cover image URL for a guide based on keywords in its title.
 * Falls back to a seasonal travel photo if no destination is detected.
 */

const DESTINATION_MAP: [string[], string][] = [
  [['paris', 'france', 'versailles', 'louvre'], 'photo-1502602898657-3e91760cbb34'],
  [['lisbonne', 'portugal', 'lisbon', 'porto'], 'photo-1513635269975-59663e0ac1ad'],
  [['londres', 'london', 'angleterre', 'england', 'bigben'], 'photo-1513635269975-59663e0ac1ad'],
  [['barcelone', 'barcelona', 'catalogne', 'espagne', 'madrid', 'séville', 'seville'], 'photo-1543783207-ec64e4d95325'],
  [['rome', 'italie', 'italy', 'florence', 'venise', 'venice', 'milan', 'naples'], 'photo-1552832230-c0197dd311b5'],
  [['alsace', 'strasbourg', 'colmar', 'haguenau'], 'photo-1615880484746-a134be9a6ecf'],
  [['chamonix', 'alpes', 'montagne', 'mont-blanc', 'grenoble', 'savoie'], 'photo-1483728642387-6c3bdd6c93e5'],
  [['amsterdam', 'pays-bas', 'hollande', 'netherlands'], 'photo-1534351590666-13e3e96b5702'],
  [['prague', 'tchéquie', 'czech', 'bohème'], 'photo-1541832676-9b763b0239ab'],
  [['berlin', 'allemagne', 'germany', 'munich', 'hambourg'], 'photo-1587330979470-3595ac045ab0'],
  [['tokyo', 'japon', 'japan', 'osaka', 'kyoto'], 'photo-1540959733332-eab4deabeeaf'],
  [['marrakech', 'maroc', 'morocco', 'casablanca', 'fès'], 'photo-1597212618440-806262de4f8b'],
  [['new york', 'newyork', 'manhattan', 'brooklyn', 'états-unis', 'usa'], 'photo-1534430480872-3498386e7856'],
  [['nice', 'côte d\'azur', 'cannes', 'antibes', 'provence', 'marseille'], 'photo-1555990790-e2f9a18823e0'],
  [['lyon', 'bordeaux', 'normandie', 'bretagne', 'rennes', 'nantes'], 'photo-1499856374310-1bdcee7aba17'],
  [['grèce', 'grece', 'greece', 'santorin', 'santorini', 'athènes', 'mykonos'], 'photo-1533105079780-92b9be482077'],
  [['dubai', 'abu dhabi', 'émirats', 'emirates'], 'photo-1581889470536-467bdbe30cd0'],
  [['bali', 'indonésie', 'indonesia', 'thaïlande', 'thailand', 'asie', 'asia'], 'photo-1537996194471-e657df975ab4'],
  [['new zealand', 'nouvelle-zélande', 'australie', 'australia', 'sydney'], 'photo-1506905925346-21bda4d32df4'],
  [['islande', 'iceland', 'scandinavie', 'norvège', 'norway', 'suède', 'sweden'], 'photo-1531168556467-80aace0d0144'],
];

const SAISON_FALLBACK: Record<string, string> = {
  ETE:       'photo-1476514525535-07fb3b4ae5f1',
  PRINTEMPS: 'photo-1462275646964-a0e3386b89fa',
  AUTOMNE:   'photo-1507003211169-0a1dd7228f2d',
  HIVER:     'photo-1483728642387-6c3bdd6c93e5',
};

const DEFAULT = 'photo-1476514525535-07fb3b4ae5f1';

export function getCoverImage(titre: string, saison = 'ETE'): string {
  const lower = titre.toLowerCase();

  for (const [keywords, photoId] of DESTINATION_MAP) {
    if (keywords.some(k => lower.includes(k))) {
      return `https://images.unsplash.com/${photoId}?w=800&h=500&fit=crop&q=80`;
    }
  }

  const fallbackId = SAISON_FALLBACK[saison] ?? DEFAULT;
  return `https://images.unsplash.com/${fallbackId}?w=800&h=500&fit=crop&q=80`;
}
