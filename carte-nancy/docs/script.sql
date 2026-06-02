/* =========================================================
   RESTAURANTS
   ========================================================= */

CREATE TABLE RESTAURANT (
    NUM_RESTAURANT  NUMBER PRIMARY KEY,
    NOM             VARCHAR2(100) NOT NULL,
    ADRESSE         VARCHAR2(255) NOT NULL,
    LATITUDE        NUMBER(9, 6) NOT NULL,
    LONGITUDE       NUMBER(9, 6) NOT NULL
);


/* =========================================================
   TABLES PHYSIQUES DES RESTAURANTS
   ========================================================= */

CREATE TABLE TABLE_RESTAURANT (
    NUM_TABLE       NUMBER PRIMARY KEY,
    NUM_RESTAURANT  NUMBER NOT NULL,
    NB_PLACES       NUMBER NOT NULL,

    CONSTRAINT FK_TABLE_RESTAURANT
        FOREIGN KEY (NUM_RESTAURANT)
        REFERENCES RESTAURANT(NUM_RESTAURANT)
);


/* =========================================================
   CRÉNEAUX HORAIRES DES TABLES
   ========================================================= */

CREATE TABLE CRENEAU_TABLE (
    NUM_CRENEAU  NUMBER PRIMARY KEY,
    NUM_TABLE    NUMBER NOT NULL,
    DEBUT        TIMESTAMP NOT NULL,
    FIN          TIMESTAMP NOT NULL,
    STATUT       VARCHAR2(20) DEFAULT 'LIBRE' NOT NULL,

    CONSTRAINT FK_CRENEAU_TABLE
        FOREIGN KEY (NUM_TABLE)
        REFERENCES TABLE_RESTAURANT(NUM_TABLE)
);


/* =========================================================
   RÉSERVATIONS
   ========================================================= */

CREATE TABLE RESERVATION (
    NUM_RESERVATION  NUMBER PRIMARY KEY,
    NUM_CRENEAU      NUMBER NOT NULL,
    NOM_CLIENT       VARCHAR2(100) NOT NULL,
    PRENOM_CLIENT    VARCHAR2(100) NOT NULL,
    TELEPHONE        VARCHAR2(30) NOT NULL,
    NB_CONVIVES      NUMBER NOT NULL,
    DATE_CREATION    TIMESTAMP DEFAULT SYSTIMESTAMP NOT NULL,

    CONSTRAINT FK_RESERVATION_CRENEAU
        FOREIGN KEY (NUM_CRENEAU)
        REFERENCES CRENEAU_TABLE(NUM_CRENEAU)
);


/* =========================================================
   SÉQUENCES
   Les identifiants 1 à 99 sont réservés aux données de test.
   L'application générera les nouveaux identifiants à partir
   de 100.
   ========================================================= */

CREATE SEQUENCE SEQ_RESTAURANT
    START WITH 100
    INCREMENT BY 1;

CREATE SEQUENCE SEQ_TABLE_RESTAURANT
    START WITH 100
    INCREMENT BY 1;

CREATE SEQUENCE SEQ_CRENEAU_TABLE
    START WITH 100
    INCREMENT BY 1;

CREATE SEQUENCE SEQ_RESERVATION
    START WITH 100
    INCREMENT BY 1;