--------------------------------------------------
-- GUIDES
--------------------------------------------------

INSERT INTO guides (titre, description, jours, mobilite, saison, pour_qui) VALUES
('Paris Incontournable','Visite des lieux emblématiques de Paris',1,'A_PIED','ETE','FAMILLE'),
('New York Highlights','Les incontournables de New York',1,'A_PIED','AUTOMNE','ENTRE_AMIS'),
('Rome & Antiquités','Visite historique de Rome',2,'A_PIED','PRINTEMPS','FAMILLE'),
('Taj Mahal Express','Découverte du Taj Mahal',1,'VOITURE','ETE','SEUL'),
('Londres Fun','Visite des essentiels de Londres',1,'A_PIED','PRINTEMPS','ENTRE_AMIS'),
('Barcelone Architectural','Beautés de Barcelone',1,'VELO','ETE','FAMILLE'),
('Santorini Views','Paysages magnifiques de Santorin',2,'A_PIED','ETE','ENTRE_AMIS'),
('Rio Highlights','Découverte de Rio de Janeiro',1,'VOITURE','ETE','ENTRE_AMIS'),
('Tokyo Explorer','Modernité et traditions japonaises',2,'METRO','PRINTEMPS','ENTRE_AMIS'),
('Bali Escape','Temples et nature balinaise',3,'MOTO','ETE','ENTRE_AMIS'),
('Marrakech Colors','Immersion marocaine',2,'A_PIED','AUTOMNE','ENTRE_AMIS'),
('Cape Town Adventure','Nature et océan',3,'VOITURE','PRINTEMPS','ENTRE_AMIS'),
('Dubai Experience','Luxe et désert',2,'VOITURE','HIVER','SEUL'),
('Petra Discovery','Merveille antique jordanienne',1,'A_PIED','PRINTEMPS','SEUL'),
('Sydney Coastal','Ville et plages australiennes',2,'VELO','ETE','FAMILLE');

--------------------------------------------------
-- ACTIVITIES
--------------------------------------------------

INSERT INTO activities
(guide_id,titre,description,type,adresse,telephone,site_internet,heure_debut,duree,ordre,jour)
VALUES

-- PARIS
(1,'Tour Eiffel','Symbole iconique de Paris','ACTIVITE','Champ de Mars, Paris',NULL,'https://www.toureiffel.paris','09:00',120,1,1),
(1,'Musée du Louvre','Musée mondialement connu','MUSEE','Rue de Rivoli, Paris',NULL,'https://www.louvre.fr','11:30',180,2,1),
(1,'Montmartre & Sacré-Cœur','Quartier artistique','ACTIVITE','Montmartre, Paris',NULL,'','15:30',120,3,1),

-- NEW YORK
(2,'Statue de la Liberté','Symbole de liberté','ACTIVITE','Liberty Island, NYC',NULL,'','10:00',90,1,1),
(2,'Times Square','Place animée','ACTIVITE','Manhattan',NULL,'','12:00',90,2,1),
(2,'Central Park','Grand parc urbain','PARC','New York',NULL,'','14:00',120,3,1),

-- ROME
(3,'Colisée','Site antique emblématique','ACTIVITE','Rome',NULL,'','09:00',120,1,1),
(3,'Forum Romain','Ruines historiques','ACTIVITE','Rome',NULL,'','11:30',90,2,1),
(3,'Panthéon','Temple antique','ACTIVITE','Rome',NULL,'','15:00',60,3,1),

-- TAJ MAHAL
(4,'Taj Mahal','Mausolée emblématique','ACTIVITE','Agra, Inde',NULL,'https://www.tajmahal.gov.in','10:00',120,1,1),
(4,'Agra Fort','Forteresse moghole','ACTIVITE','Agra',NULL,'','13:00',90,2,1),
(4,'Jama Masjid','Grande mosquée','ACTIVITE','Agra',NULL,'','15:00',90,3,1),

-- LONDRES
(5,'Big Ben','Monument iconique','ACTIVITE','Westminster, Londres',NULL,'','10:00',90,1,1),
(5,'London Eye','Vue panoramique','ACTIVITE','South Bank',NULL,'https://www.londoneye.com','12:00',90,2,1),
(5,'Buckingham Palace','Résidence royale','ACTIVITE','London',NULL,'','14:00',60,3,1),

-- BARCELONE
(6,'Sagrada Familia','Chef-d’oeuvre de Gaudí','ACTIVITE','Barcelona',NULL,'https://sagradafamilia.org','09:30',90,1,1),
(6,'Park Güell','Parc coloré','PARC','Barcelona',NULL,'','11:30',120,2,1),
(6,'La Rambla','Promenade vivante','ACTIVITE','Barcelona',NULL,'','14:30',60,3,1),

-- SANTORINI
(7,'Fira Viewpoint','Vue sur la caldera','ACTIVITE','Fira, Grèce',NULL,'','08:00',90,1,1),
(7,'Oia Sunset','Coucher de soleil','ACTIVITE','Oia',NULL,'','19:30',120,2,1),
(7,'Red Beach','Plage volcanique','PARC','Santorin',NULL,'','15:00',180,3,1),

-- RIO
(8,'Christ Rédempteur','Statue iconique','ACTIVITE','Rio',NULL,'','09:00',120,1,1),
(8,'Copacabana','Plage célèbre','PARC','Rio',NULL,'','10:30',90,2,1),
(8,'Sugarloaf','Vue panoramique','ACTIVITE','Rio',NULL,'','14:00',90,3,1),

-- TOKYO
(9,'Shibuya Crossing','Carrefour mythique','ACTIVITE','Tokyo',NULL,'','09:00',60,1,1),
(9,'Temple Senso-ji','Temple bouddhiste','ACTIVITE','Asakusa',NULL,'','11:00',90,2,1),
(9,'Akihabara','Quartier geek','ACTIVITE','Tokyo',NULL,'','15:00',120,3,1),

-- BALI
(10,'Tanah Lot','Temple sur la mer','ACTIVITE','Bali',NULL,'','09:30',120,1,1),
(10,'Tegalalang','Rizières iconiques','PARC','Ubud',NULL,'','13:00',120,2,1),
(10,'Monkey Forest','Forêt sacrée','PARC','Ubud',NULL,'','16:00',90,3,1),

-- MARRAKECH
(11,'Jemaa el-Fna','Place animée','ACTIVITE','Marrakech',NULL,'','10:00',120,1,1),
(11,'Jardin Majorelle','Jardin célèbre','PARC','Marrakech',NULL,'','13:00',90,2,1),
(11,'Souks','Marchés traditionnels','ACTIVITE','Médina',NULL,'','16:00',120,3,1),

-- CAPE TOWN
(12,'Table Mountain','Vue spectaculaire','ACTIVITE','Cape Town',NULL,'','09:00',120,1,1),
(12,'Boulders Beach','Pingouins','PARC','Cape Town',NULL,'','13:00',120,2,1),
(12,'Waterfront','Quartier portuaire','ACTIVITE','Cape Town',NULL,'','16:00',90,3,1),

-- DUBAI
(13,'Burj Khalifa','Plus haute tour du monde','ACTIVITE','Dubai',NULL,'','10:00',90,1,1),
(13,'Dubai Mall','Centre commercial géant','ACTIVITE','Dubai',NULL,'','12:00',120,2,1),
(13,'Safari désert','Excursion 4x4','ACTIVITE','Désert Dubai',NULL,'','16:00',180,3,1),

-- PETRA
(14,'Le Siq','Entrée canyon','ACTIVITE','Petra',NULL,'','08:30',60,1,1),
(14,'Le Trésor','Monument emblématique','ACTIVITE','Petra',NULL,'','10:00',90,2,1),
(14,'Monastère','Vue spectaculaire','ACTIVITE','Petra',NULL,'','14:00',120,3,1),

-- SYDNEY
(15,'Opéra de Sydney','Architecture iconique','ACTIVITE','Sydney',NULL,'','09:00',90,1,1),
(15,'Bondi Beach','Plage célèbre','PARC','Sydney',NULL,'','12:00',120,2,1),
(15,'Harbour Bridge Walk','Vue panoramique','ACTIVITE','Sydney',NULL,'','15:00',90,3,1);