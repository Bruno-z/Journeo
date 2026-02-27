-- 1) Guide Paris Incontournable
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('Paris Incontournable', 'Visite des lieux emblématiques de Paris', 1, 'A_PIED', 'ETE', 'FAMILLE');

-- Activités Paris
INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(1, 'Tour Eiffel', 'Symbole iconique de Paris', 'ACTIVITE', 'Champ de Mars, 5 Av. Anatole France, 75007 Paris, France', NULL, 'https://www.toureiffel.paris', '09:00', 120, 1, 1),
(1, 'Musée du Louvre', 'Musée d''art mondialement connu', 'MUSEE', 'Rue de Rivoli, 75001 Paris, France', NULL, 'https://www.louvre.fr', '11:30', 180, 2, 1),
(1, 'Montmartre & Sacré-Cœur', 'Quartier artistique et sa basilique', 'ACTIVITE', 'Montmartre, 75018 Paris, France', NULL, '', '15:30', 120, 3, 1);

-- 2) Guide NY Highlights
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('New York Highlights', 'Les incontournables de New York', 1, 'A_PIED', 'AUTOMNE', 'ENTRE_AMIS');

INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(2, 'Statue de la Liberté', 'Symbole de liberté', 'ACTIVITE', 'Liberty Island, NY 10004, USA', NULL, '', '10:00', 90, 1, 1),
(2, 'Times Square', 'Place emblématique animée', 'ACTIVITE', 'Manhattan, NY 10036, USA', NULL, '', '12:00', 90, 2, 1),
(2, 'Central Park', 'Grand parc urbain', 'PARC', 'New York, NY, USA', NULL, '', '14:00', 120, 3, 1);

-- 3) Guide Rome & Antiquités
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('Rome & Antiquités', 'Visite historique de Rome', 2, 'A_PIED', 'PRINTEMPS', 'FAMILLE');

INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(3, 'Colisée de Rome', 'Site antique emblématique', 'ACTIVITE', 'Piazza del Colosseo, 1, 00184 Roma RM, Italie', NULL, '', '09:00', 120, 1, 1),
(3, 'Forum Romain', 'Ruines antiques historiques', 'ACTIVITE', 'Via della Salara Vecchia, 5/6, Rome, Italie', NULL, '', '11:30', 90, 2, 1),
(3, 'Panthéon', 'Temple romain bien conservé', 'ACTIVITE', 'Piazza della Rotonda, 00186 Roma RM, Italie', NULL, '', '15:00', 60, 3, 1);

-- 4) Guide Taj Mahal
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('Taj Mahal Express', 'Découverte du Taj Mahal', 1, 'VOITURE', 'ETE', 'SEUL');

INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(4, 'Taj Mahal', 'Mausolée emblématique en Inde', 'ACTIVITE', 'Dharmapuri, Forest Colony, Agra, Uttar Pradesh 282001, Inde', NULL, 'https://www.tajmahal.gov.in', '10:00', 120, 1, 1),
(4, 'Jama Masjid Agra', 'Grande mosquée historique', 'ACTIVITE', 'Agra, Inde', NULL, '', '13:00', 90, 2, 1),
(4, 'Agra Fort', 'Forteresse moghole', 'ACTIVITE', 'Agra, Inde', NULL, '', '15:00', 90, 3, 1);

-- 5) Guide Londres
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('Londres Fun', 'Visite des essentiels de Londres', 1, 'A_PIED', 'PRINTEMPS', 'ENTRE_AMIS');

INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(5, 'Big Ben & Houses of Parliament', 'Iconiques monuments londoniens', 'ACTIVITE', 'Westminster, London SW1A 0AA, UK', NULL, '', '10:00', 90, 1, 1),
(5, 'London Eye', 'Grande roue panoramique', 'ACTIVITE', 'Riverside Building, County Hall, London SE1 7PB, UK', NULL, 'https://www.londoneye.com', '12:00', 90, 2, 1),
(5, 'Buckingham Palace', 'Résidence royale', 'ACTIVITE', 'London SW1A 1AA, UK', NULL, '', '14:00', 60, 3, 1);

-- 6) Guide Barcelone
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('Barcelone Architectural', 'Beautés de Barcelone', 1, 'VELO', 'ETE', 'FAMILLE');

INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(6, 'Sagrada Familia', 'Chef-d''oeuvre de Gaudí', 'ACTIVITE', 'Carrer de Mallorca, 401, 08013 Barcelona, Espagne', NULL, 'https://sagradafamilia.org', '09:30', 90, 1, 1),
(6, 'Park Güell', 'Parc coloré de Gaudí', 'PARC', 'Carrer d''Olot, s/n, 08024 Barcelone, Espagne', NULL, 'https://parkguell.barcelona', '11:30', 120, 2, 1),
(6, 'La Rambla', 'Promenade vivante', 'ACTIVITE', 'La Rambla, Barcelona, Espagne', NULL, '', '14:30', 60, 3, 1);

-- 7) Guide Santorini
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('Santorini Views', 'Paysages magnifiques de Santorin', 2, 'A_PIED', 'ETE', 'ENTRE_AMIS');

INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(7, 'Fira Viewpoint', 'Point de vue sur la caldera', 'ACTIVITE', 'Fira 847 00, Grèce', NULL, '', '08:00', 90, 1, 1),
(7, 'Oia Sunset', 'Coucher de soleil spectaculaire', 'ACTIVITE', 'Oia, Santorini, Grèce', NULL, '', '19:30', 120, 2, 1),
(7, 'Red Beach', 'Plage unique rouge', 'PARC', 'Akrotiri, Santorini, Grèce', NULL, '', '15:00', 180, 3, 1);

-- 8) Guide Rio
INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui)
VALUES ('Rio Highlights', 'Découverte de Rio de Janeiro', 1, 'VOITURE', 'ETE', 'ENTRE_AMIS');

INSERT INTO activities (guide_id, titre, description, type, adresse, telephone, site_internet, heure_debut, duree, ordre, jour) VALUES
(8, 'Christ Rédempteur', 'Statue emblématique du Corcovado', 'ACTIVITE', 'Parque Nacional da Tijuca, Rio de Janeiro, RJ, Brésil', NULL, '', '09:00', 120, 1, 1),
(8, 'Copacabana Beach', 'Plage célèbre', 'PARC', 'Copacabana, Rio de Janeiro, Brésil', NULL, '', '10:30', 90, 2, 1),
(8, 'Sugarloaf Mountain', 'Vue panoramique', 'ACTIVITE', 'Pão de Açúcar, Rio de Janeiro, Brésil', NULL, '', '14:00', 90, 3, 1);
