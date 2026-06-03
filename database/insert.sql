/* =========================================================================
   1. LES 5 RESTAURANTS
   ========================================================================= */

INSERT INTO RESTAURANT (
    NUM_RESTAURANT,
    NOM,
    ADRESSE,
    LATITUDE,
    LONGITUDE
)
VALUES (
    1,
    'L''Excelsior',
    '50 Rue Henri Poincaré, 54000 Nancy',
    48.690312,
    6.175402
);

INSERT INTO RESTAURANT (
    NUM_RESTAURANT,
    NOM,
    ADRESSE,
    LATITUDE,
    LONGITUDE
)
VALUES (
    2,
    'Le Grand Café Foy',
    '1 Place Stanislas, 54000 Nancy',
    48.693752,
    6.183201
);

INSERT INTO RESTAURANT (
    NUM_RESTAURANT,
    NOM,
    ADRESSE,
    LATITUDE,
    LONGITUDE
)
VALUES (
    3,
    'Le Bouche à Oreille',
    '42 Rue des Carmes, 54000 Nancy',
    48.691236,
    6.181347
);

INSERT INTO RESTAURANT (
    NUM_RESTAURANT,
    NOM,
    ADRESSE,
    LATITUDE,
    LONGITUDE
)
VALUES (
    4,
    'Le Capucin Gourmand',
    '31 Rue Gambetta, 54000 Nancy',
    48.693151,
    6.179374
);

INSERT INTO RESTAURANT (
    NUM_RESTAURANT,
    NOM,
    ADRESSE,
    LATITUDE,
    LONGITUDE
)
VALUES (
    5,
    'La Table du Bon Roi Stanislas',
    '7 Rue Gustave Simon, 54000 Nancy',
    48.692484,
    6.181284
);


/* =========================================================================
   2. LES TABLES PHYSIQUES DES RESTAURANTS
   ========================================================================= */

-- Tables du restaurant 1 : L'Excelsior
INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (1, 1, 2);

INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (2, 1, 4);

INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (3, 1, 6);


-- Tables du restaurant 2 : Le Grand Café Foy
INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (4, 2, 2);

INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (5, 2, 4);

INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (6, 2, 8);


-- Tables du restaurant 3 : Le Bouche à Oreille
INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (7, 3, 2);

INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (8, 3, 4);


-- Tables du restaurant 4 : Le Capucin Gourmand
INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (9, 4, 2);

INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (10, 4, 4);


-- Tables du restaurant 5 : La Table du Bon Roi Stanislas
INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (11, 5, 2);

INSERT INTO TABLE_RESTAURANT (
    NUM_TABLE,
    NUM_RESTAURANT,
    NB_PLACES
)
VALUES (12, 5, 4);


/* =========================================================================
   3. LES CRÉNEAUX HORAIRES DES TABLES
   Deux services : 19 h - 21 h et 21 h - 23 h
   ========================================================================= */

-- Créneaux du restaurant 1
INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    1,
    1,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    2,
    1,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    3,
    2,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    4,
    2,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    5,
    3,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    6,
    3,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);


-- Créneaux du restaurant 2
INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    7,
    4,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    8,
    4,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    9,
    5,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    10,
    5,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    11,
    6,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    12,
    6,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);


-- Créneaux du restaurant 3
INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    13,
    7,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    14,
    7,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    15,
    8,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    16,
    8,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);


-- Créneaux du restaurant 4
INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    17,
    9,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    18,
    9,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    19,
    10,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    20,
    10,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);


-- Créneaux du restaurant 5
INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    21,
    11,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    22,
    11,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    23,
    12,
    TIMESTAMP '2026-06-10 19:00:00',
    TIMESTAMP '2026-06-10 21:00:00',
    'LIBRE'
);

INSERT INTO CRENEAU_TABLE (
    NUM_CRENEAU,
    NUM_TABLE,
    DEBUT,
    FIN,
    STATUT
)
VALUES (
    24,
    12,
    TIMESTAMP '2026-06-10 21:00:00',
    TIMESTAMP '2026-06-10 23:00:00',
    'LIBRE'
);


/* =========================================================================
   4. LES PLATS PROPOSÉS PAR LES RESTAURANTS
   ========================================================================= */

-- Plats du restaurant 1 : L'Excelsior
INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    1,
    1,
    'Quiche lorraine',
    12.50,
    20
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    2,
    1,
    'Entrecôte grillée',
    24.00,
    12
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    3,
    1,
    'Tarte aux mirabelles',
    7.50,
    15
);


-- Plats du restaurant 2 : Le Grand Café Foy
INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    4,
    2,
    'Burger maison',
    16.00,
    20
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    5,
    2,
    'Salade vosgienne',
    13.50,
    18
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    6,
    2,
    'Crème brûlée',
    6.50,
    15
);


-- Plats du restaurant 3 : Le Bouche à Oreille
INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    7,
    3,
    'Pizza reine',
    14.00,
    25
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    8,
    3,
    'Pâtes carbonara',
    13.00,
    20
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    9,
    3,
    'Tiramisu',
    6.50,
    12
);


-- Plats du restaurant 4 : Le Capucin Gourmand
INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    10,
    4,
    'Filet de poulet',
    18.00,
    14
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    11,
    4,
    'Gratin dauphinois',
    7.00,
    20
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    12,
    4,
    'Fondant au chocolat',
    7.50,
    16
);


-- Plats du restaurant 5 : La Table du Bon Roi Stanislas
INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    13,
    5,
    'Croque-monsieur',
    11.00,
    18
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    14,
    5,
    'Pavé de saumon',
    22.00,
    10
);

INSERT INTO PLAT (
    NUM_PLAT,
    NUM_RESTAURANT,
    LIBELLE,
    PRIX_UNITAIRE,
    QTE_STOCKEE
)
VALUES (
    15,
    5,
    'Café gourmand',
    8.00,
    20
);


/* =========================================================================
   5. RÉSERVATIONS DE TEST
   Obligatoires pour pouvoir créer les commandes ci-dessous.
   ========================================================================= */

INSERT INTO RESERVATION (
    NUM_RESERVATION,
    NUM_CRENEAU,
    NOM_CLIENT,
    PRENOM_CLIENT,
    TELEPHONE,
    NB_CONVIVES
)
VALUES (
    1,
    3,
    'Dupont',
    'Alice',
    '0601020304',
    2
);

INSERT INTO RESERVATION (
    NUM_RESERVATION,
    NUM_CRENEAU,
    NOM_CLIENT,
    PRENOM_CLIENT,
    TELEPHONE,
    NB_CONVIVES
)
VALUES (
    2,
    11,
    'Martin',
    'Karim',
    '0611223344',
    4
);

INSERT INTO RESERVATION (
    NUM_RESERVATION,
    NUM_CRENEAU,
    NOM_CLIENT,
    PRENOM_CLIENT,
    TELEPHONE,
    NB_CONVIVES
)
VALUES (
    3,
    17,
    'Bernard',
    'Lina',
    '0677889900',
    2
);


/* =========================================================================
   6. MISE À JOUR DES CRÉNEAUX RÉSERVÉS
   ========================================================================= */

UPDATE CRENEAU_TABLE
SET STATUT = 'RESERVEE'
WHERE NUM_CRENEAU IN (
    3,
    11,
    17
);


/* =========================================================================
   7. COMMANDES DE TEST
   ========================================================================= */

-- Commande de la réservation 1 au restaurant 1
INSERT INTO COMMANDE (
    NUM_COMMANDE,
    NUM_RESERVATION,
    MONTANT,
    STATUT
)
VALUES (
    1,
    1,
    36.50,
    'VALIDEE'
);


-- Commande de la réservation 2 au restaurant 2
INSERT INTO COMMANDE (
    NUM_COMMANDE,
    NUM_RESERVATION,
    MONTANT,
    STATUT
)
VALUES (
    2,
    2,
    38.50,
    'EN_COURS'
);


-- Commande de la réservation 3 au restaurant 4
INSERT INTO COMMANDE (
    NUM_COMMANDE,
    NUM_RESERVATION,
    MONTANT,
    STATUT
)
VALUES (
    3,
    3,
    32.50,
    'VALIDEE'
);


/* =========================================================================
   8. CONTENU DES COMMANDES
   ========================================================================= */

-- Commande 1 : 12,50 € + 24,00 € = 36,50 €
INSERT INTO CONTIENT (
    NUM_COMMANDE,
    NUM_PLAT,
    QUANTITE,
    PRIX_UNITAIRE_COMMANDE
)
VALUES (
    1,
    1,
    1,
    12.50
);

INSERT INTO CONTIENT (
    NUM_COMMANDE,
    NUM_PLAT,
    QUANTITE,
    PRIX_UNITAIRE_COMMANDE
)
VALUES (
    1,
    2,
    1,
    24.00
);


-- Commande 2 : 2 × 16,00 € + 6,50 € = 38,50 €
INSERT INTO CONTIENT (
    NUM_COMMANDE,
    NUM_PLAT,
    QUANTITE,
    PRIX_UNITAIRE_COMMANDE
)
VALUES (
    2,
    4,
    2,
    16.00
);

INSERT INTO CONTIENT (
    NUM_COMMANDE,
    NUM_PLAT,
    QUANTITE,
    PRIX_UNITAIRE_COMMANDE
)
VALUES (
    2,
    6,
    1,
    6.50
);


-- Commande 3 : 18,00 € + 7,00 € + 7,50 € = 32,50 €
INSERT INTO CONTIENT (
    NUM_COMMANDE,
    NUM_PLAT,
    QUANTITE,
    PRIX_UNITAIRE_COMMANDE
)
VALUES (
    3,
    10,
    1,
    18.00
);

INSERT INTO CONTIENT (
    NUM_COMMANDE,
    NUM_PLAT,
    QUANTITE,
    PRIX_UNITAIRE_COMMANDE
)
VALUES (
    3,
    11,
    1,
    7.00
);

INSERT INTO CONTIENT (
    NUM_COMMANDE,
    NUM_PLAT,
    QUANTITE,
    PRIX_UNITAIRE_COMMANDE
)
VALUES (
    3,
    12,
    1,
    7.50
);


/* =========================================================================
   9. MISE À JOUR DES STOCKS APRÈS LES COMMANDES DE TEST
   ========================================================================= */

-- Commande 1
UPDATE PLAT
SET QTE_STOCKEE = QTE_STOCKEE - 1
WHERE NUM_PLAT = 1;

UPDATE PLAT
SET QTE_STOCKEE = QTE_STOCKEE - 1
WHERE NUM_PLAT = 2;


-- Commande 2
UPDATE PLAT
SET QTE_STOCKEE = QTE_STOCKEE - 2
WHERE NUM_PLAT = 4;

UPDATE PLAT
SET QTE_STOCKEE = QTE_STOCKEE - 1
WHERE NUM_PLAT = 6;


-- Commande 3
UPDATE PLAT
SET QTE_STOCKEE = QTE_STOCKEE - 1
WHERE NUM_PLAT = 10;

UPDATE PLAT
SET QTE_STOCKEE = QTE_STOCKEE - 1
WHERE NUM_PLAT = 11;

UPDATE PLAT
SET QTE_STOCKEE = QTE_STOCKEE - 1
WHERE NUM_PLAT = 12;


/* =========================================================================
   10. VALIDATION FINALE
   ========================================================================= */

COMMIT;