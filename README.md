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
java -cp "service/target/rmi-service-1.0.jar;service/ojdbc17.jar" sae.s4.rmi.service.LancerServeur
```

**Linux / macOS :**
```bash
java -cp "service/target/rmi-service-1.0.jar:service/ojdbc17.jar" sae.s4.rmi.service.LancerServeur
```

Il est possible de surcharger les identifiants de la base de données (`db.user` et `db.password` du `config.properties`) directement en ligne de commande :

```bash
java -cp "service/target/rmi-service-1.0.jar;service/ojdbc17.jar" sae.s4.rmi.service.LancerServeur <user> <password>
```

> **Note :** Ces paramètres sont **optionnels**. S'ils ne sont pas fournis, les valeurs du fichier `config.properties` sont utilisées.

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
| `rmi.hostname` | Hostname RMI (java.rmi.server.hostname) | `127.0.0.1`               |
| `rmi.port`     | Port du registre RMI                | `1099`                      |
| `rmi.name`     | Nom du service dans le registre     | `serviceRestaurant`         |

---

## 4. Lancer le Proxy HTTP

> Le proxy fait la passerelle entre le navigateur (requêtes HTTP/REST) et le serveur RMI.

Dans un **nouveau terminal**, depuis le dossier `RMI` :

```bash
java -jar proxy/target/rmi-proxy-1.0.jar
```

Le proxy accepte les arguments optionnels suivants pour surcharger la configuration :
- `--httpPort` ou `-p` : Port d'écoute HTTP du proxy
- `--rmiHost` ou `-rh` : Hôte du serveur RMI
- `--rmiPort` ou `-rp` : Port du registre RMI
- `--useIutProxy` : `true` ou `false` pour forcer l'activation ou la désactivation du proxy IUT (ecrase le `config.properties`)

Exemple :
```bash
java -jar proxy/target/rmi-proxy-1.0.jar --httpPort 8080 --useIutProxy false
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
| `iut.proxy.host` | Proxy réseau IUT (laisser vide si hors IUT)  | `www-cache.iutnc.univ-lorraine.fr` |
| `iut.proxy.port` | Port du proxy IUT (laisser vide si hors IUT) | `3128`            |

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

## 6. Frontend et Déploiement Simplifié

Le frontend est une page HTML statique avec du TypeScript compilé, déployé sur le serveur Webetu de l'IUT. Il intègre une gestion dynamique de configuration, vous permettant de modifier l'adresse IP du serveur via l'onglet ** Configuration** directement depuis le navigateur.

**URL d'acces :**
```
https://webetu.iutnc.univ-lorraine.fr/www/e52526u/nancy-carte/
```

### Le script de démarrage automatisé 

Un script bash `start_demo.sh` est fourni à la racine du projet pour automatiser le lancement lors de la démonstration sur une machine Linux de l'IUT. Il s'occupe de :
1. Demander vos identifiants Oracle (sécurisé)
2. Récupérer automatiquement l'IP locale de la machine IUT
3. Injecter cette IP dans un fichier `env.js` copié vers votre déploiement Webetu (`~/www/nancy-carte/`)
4. Lancer le Serveur RMI et le Proxy HTTP en arrière-plan avec des limites de RAM optimisées

**Pour le lancer :**
```bash
./start_demo.sh
```

### Recompilation manuelle du frontend
Pour recompiler le code TypeScript après modification locale :

```bash
cd nancy-carte
npm install
npm run build
```

Puis déposer manuellement le contenu du dossier `nancy-carte/` sur Webetu quand c'est necessaire.

---

## Structure du projet

```
.
├── RMI/                          # Backend Java (Maven multi-modules)
│   ├── pom.xml                   # POM parent
│   ├── common/                   # Module interface RMI partagée
│   │   ├── pom.xml
│   │   └── src/main/java/sae/s4/rmi/common/
│   │       └── ServiceRestaurant.java        # Interface RMI (unique)
│   ├── service/                  # Module serveur RMI
│   │   ├── pom.xml
│   │   ├── ojdbc17.jar           # Driver Oracle (vendorisé)
│   │   └── src/main/java/sae/s4/rmi/service/
│   │       ├── LancerServeur.java
│   │       └── ServiceRestaurantImpl.java    # Implémentation (JDBC + Oracle)
│   ├── proxy/                    # Module proxy HTTP
│   │   ├── pom.xml
│   │   └── src/main/java/sae/s4/rmi/proxy/
│   │       └── ProxyHTTP.java               # Serveur HTTP (CORS + REST → RMI)
│   └── client/                   # Module client de test RMI
│       ├── pom.xml
│       └── src/main/java/sae/s4/rmi/client/
│           └── LancerClient.java
├── nancy-carte/                  # Frontend (TypeScript + Leaflet)
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

## Résumé : ordre de lancement (Manuellement sans le script)

```
1.  Base de données Oracle    →  Exécuter script.sql + insert.sql (une seule fois)
2.  mvn clean package         →  Compiler les 3 modules Java
3.  java ... LancerServeur    →  Démarrer le serveur RMI (port 1099) sur une machine IUT
4.  java -jar ...rmi-proxy    →  Démarrer le proxy HTTP  (port 8080) sur la même machine
5.  Frontend déjà en ligne    →  https://webetu.iutnc.univ-lorraine.fr/www/e52526u/nancy-carte/
```
*(Le script `./start_demo.sh` effectue les étapes 3 à 5 automatiquement)*

---

## Réponse à la question de réflexion

**Est-ce responsable d'ainsi contourner la politique de sécurité de votre navigateur ?**

> *Non, ce n'est **pas du tout responsable**. Le navigateur bloque nativement le **Contenu Mixte** (Mixed Content) et les requêtes **CORS** non autorisées pour protéger l'utilisateur contre le vol de données et les attaques "Man-in-the-Middle". Contourner ces sécurités directement via les paramètres du navigateur (en désactivant la protection dans Firefox ou Chrome) expose l'ordinateur de l'utilisateur à des failles de sécurité sur l'ensemble de sa navigation web. En production, il ne faut jamais demander à un client d'abaisser la sécurité de son navigateur. La "vraie" solution consiste à ce que le serveur gère lui-même ces requêtes (via le proxy backend) ou que l'API fournisse une version HTTPS avec les en-têtes CORS corrects.*

---

## Technologies

- **Java 11+** — RMI, HttpServer, JDBC
- **Maven** — Build multi-modules
- **Oracle** — Base de données relationnelle (ojdbc17)
- **Gson** — Parsing JSON dans le proxy
- **TypeScript + esbuild** — Frontend
- **Leaflet** — Carte interactive open-source
