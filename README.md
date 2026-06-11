# Application Répartie — Carte de Nancy

Application distribuée permettant de visualiser et réserver des tables dans les restaurants de Nancy, avec une carte interactive affichant également les stations VélOstan'lib et les incidents de circulation.

## Architecture

```
┌──────────────────┐       HTTP (REST)       ┌──────────────┐       RMI        ┌──────────────────┐       JDBC       ┌────────────┐
│  Frontend        │  ───────────────────►   │  Proxy HTTP  │  ─────────────►  │  Serveur RMI     │  ─────────────►  │  Oracle DB │
│  (TypeScript /   │       port 8080         │  (Java)      │     port 1099    │  (Java)          │                  │            │
│   Leaflet)       │  ◄───────────────────   │              │  ◄─────────────  │                  │  ◄─────────────  │            │
└──────────────────┘                         └──────────────┘                  └──────────────────┘                  └────────────┘
```

- **Frontend** : carte interactive Leaflet en TypeScript, compilée avec esbuild
- **Proxy HTTP** : passerelle Java (`HttpServer`) résolvant le CORS entre le navigateur et les services RMI
- **Serveur RMI** : service Java exposant les méthodes `getRestaurants()` et `reserverTable(...)` via RMI
- **Base de données** : Oracle, avec les tables `RESTAURANT`, `TABLE_RESTAURANT`, `CRENEAU_TABLE`, `RESERVATION`, `PLAT`, `COMMANDE`, `CONTIENT`

---

## Déploiement en production (IUT)

L'application est déployée dans l'environnement de l'IUT Nancy-Charlemagne :

| Composant | Hébergement | Détails |
|-----------|-------------|---------- |
| **Frontend** | Serveur Webetu de l'IUT | Accessible à : https://webetu.iutnc.univ-lorraine.fr/www/e52526u/nancy-carte/ |
| **Serveur RMI + Proxy HTTP** | Machine de l'IUT (en salle) | Lancés manuellement par un membre du groupe via un terminal sur la machine |
| **Base de données Oracle** | Serveur Charlemagne (`charlemagne.iutnc.univ-lorraine.fr`) | Accès via les identifiants élève d'un membre du groupe |

> **Note :** L'accès à la base de données se fait via le compte Oracle étudiant d'un des membres du groupe. Le serveur RMI est configuré avec les identifiants de cet élève dans `config.properties`. Le proxy HTTP et le serveur RMI doivent être lancés sur une machine de l'IUT ayant accès au réseau interne (base de données Oracle).

Pour que l'application fonctionne en production :
1. Un membre du groupe se connecte sur une machine de l'IUT
2. Il lance le **serveur RMI** (qui se connecte à la base Oracle via son compte élève)
3. Il lance le **proxy HTTP** (qui se connecte au serveur RMI)
4. Le frontend, déjà déployé sur Webetu, est accessible par tous via le navigateur

---

## Prérequis

| Outil          | Version minimale |
|----------------|------------------|
| **Java JDK**   | 11+              |
| **Maven**      | 3.6+             |
| **Node.js**    | 16+ (pour le frontend) |
| **npm**        | 8+ (pour le frontend)  |
| **Oracle DB**  | Accès à une instance Oracle (ex: serveur Charlemagne de l'IUT) |

---

## 1. Préparer la base de données

Exécuter les scripts SQL dans l'ordre sur votre instance Oracle :

```sql
-- Création des tables
@database/script.sql

-- Insertion des données de test (restaurants, tables, créneaux…)
@database/insert.sql
```

---

## 2. Compiler le backend Java (Maven)

Depuis la racine du dossier `RMI` :

```bash
cd RMI
mvn clean package
```

Cela compile les trois modules (`service`, `proxy`, `client`) et produit les JARs dans leurs répertoires `target/` respectifs.

---

## 3. Lancer le Serveur RMI

> Le serveur RMI doit être lancé **en premier** car le proxy s'y connecte au démarrage.

Depuis le dossier `RMI` :

**Windows :**
```bash
java -cp "service/target/rmi-service-1.0.jar;service/ojdbc17.jar" LancerServeur
```

**Linux / macOS :**
```bash
java -cp "service/target/rmi-service-1.0.jar:service/ojdbc17.jar" LancerServeur
```

Vous devriez voir :
```
Service RMI 'serviceRestaurant' démarré sur le port 1099
En attente de requêtes...
```

### Configuration du serveur

Le fichier `RMI/service/src/main/resources/config.properties` contient :

| Propriété      | Description                         | Valeur par défaut           |
|----------------|-------------------------------------|-----------------------------|
| `db.url`       | URL JDBC de la base Oracle          | *(à adapter)*               |
| `db.user`      | Utilisateur Oracle                  | *(à adapter)*               |
| `db.password`  | Mot de passe Oracle                 | *(à adapter)*               |
| `rmi.port`     | Port du registre RMI                | `1099`                      |
| `rmi.name`     | Nom du service dans le registre     | `serviceRestaurant`         |

---

## 4. Lancer le Proxy HTTP

> Le proxy fait la passerelle entre le navigateur (requêtes HTTP/REST) et le serveur RMI.

Dans un **nouveau terminal**, depuis le dossier `RMI` :

```bash
java -jar proxy/target/rmi-proxy-1.0.jar
```

Vous devriez voir :
```
Connecté au service RMI : serviceRestaurant @ localhost:1099
Proxy HTTP démarré sur le port 8080
```

### Configuration du proxy

Le fichier `RMI/proxy/src/main/resources/config.properties` contient :

| Propriété        | Description                                  | Valeur par défaut |
|------------------|----------------------------------------------|-------------------|
| `rmi.host`       | Hôte du serveur RMI                          | `localhost`       |
| `rmi.port`       | Port du registre RMI                         | `1099`            |
| `rmi.name`       | Nom du service RMI                           | `serviceRestaurant` |
| `http.port`      | Port d'écoute HTTP du proxy                  | `8080`            |
| `incidents.url`  | URL de l'API Grand Nancy (incidents)         | *(URL open data)* |
| `iut.proxy.host` | Proxy réseau IUT (laisser vide si hors IUT)  | *(vide)*          |
| `iut.proxy.port` | Port du proxy IUT (laisser vide si hors IUT) | *(vide)*          |

### Endpoints exposés par le proxy

| Méthode | Route          | Description                                    |
|---------|----------------|------------------------------------------------|
| `GET`   | `/restaurants` | Liste tous les restaurants (JSON)               |
| `POST`  | `/reserver`    | Réserve une table (body JSON)                   |
| `GET`   | `/incidents`   | Incidents de circulation Grand Nancy            |

#### Exemple de body pour `/reserver` :

```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "convives": 3,
  "telephone": "0612345678",
  "idRestaurant": 1,
  "date": "2026-06-10",
  "heure": "19:00"
}
```

---

## 5. Tester avec le Client RMI (optionnel)

Le client de test se connecte directement au serveur RMI (sans passer par le proxy) pour valider le fonctionnement métier :

```bash
java -jar client/target/rmi-client-1.0.jar
```

Il exécute automatiquement plusieurs scénarios de test (récupération des restaurants, réservation valide, restaurant inexistant, champs manquants, convives invalides).

---

## 6. Frontend

Le frontend est une page HTML statique avec du TypeScript compilé. En production, il est déployé sur le serveur Webetu de l'IUT :

**URL de production :**
```
https://webetu.iutnc.univ-lorraine.fr/www/e52526u/nancy-carte/
```

Pour recompiler le code TypeScript après modification :

```bash
cd carte-nancy
npm install
npm run build
```

Puis déposer le contenu du dossier `carte-nancy/` sur Webetu (via SFTP ou le gestionnaire de fichiers de l'IUT).

---

## Structure du projet

```
.
├── RMI/                          # Backend Java (Maven multi-modules)
│   ├── pom.xml                   # POM parent
│   ├── service/                  # Module serveur RMI
│   │   ├── pom.xml
│   │   ├── ojdbc17.jar           # Driver Oracle (vendorisé)
│   │   └── src/main/java/
│   │       ├── LancerServeur.java
│   │       ├── ServiceRestaurant.java        # Interface RMI
│   │       └── ServiceRestaurantImpl.java    # Implémentation (JDBC + Oracle)
│   ├── proxy/                    # Module proxy HTTP
│   │   ├── pom.xml
│   │   └── src/main/java/
│   │       ├── ProxyHTTP.java               # Serveur HTTP (CORS + REST → RMI)
│   │       └── ServiceRestaurant.java       # Interface RMI (partagée)
│   └── client/                   # Module client de test RMI
│       ├── pom.xml
│       └── src/main/java/
│           ├── LancerClient.java
│           └── ServiceRestaurant.java       # Interface RMI (partagée)
├── carte-nancy/                  # Frontend (TypeScript + Leaflet)
│   ├── index.html
│   ├── package.json
│   ├── src/                      # Sources TypeScript
│   └── dist/                     # Bundle compilé (app.js)
├── database/                     # Scripts SQL
│   ├── script.sql                # Création des tables
│   └── insert.sql                # Données de test
└── Documents/                    # Documentation
    └── architecture.png          # Schéma d'architecture
```

---

## Résumé : ordre de lancement

```
1.  Base de données Oracle    →  Exécuter script.sql + insert.sql (une seule fois)
2.  mvn clean package         →  Compiler les 3 modules Java
3.  java ... LancerServeur    →  Démarrer le serveur RMI (port 1099) sur une machine IUT
4.  java -jar ...rmi-proxy    →  Démarrer le proxy HTTP  (port 8080) sur la même machine
5.  Frontend déjà en ligne    →  https://webetu.iutnc.univ-lorraine.fr/www/e52526u/nancy-carte/
```

---

## Technologies

- **Java 11+** — RMI, HttpServer, JDBC
- **Maven** — Build multi-modules
- **Oracle** — Base de données relationnelle (ojdbc17)
- **Gson** — Parsing JSON dans le proxy
- **TypeScript + esbuild** — Frontend
- **Leaflet** — Carte interactive open-source
