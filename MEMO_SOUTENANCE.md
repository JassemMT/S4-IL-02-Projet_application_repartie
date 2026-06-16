# Guide de lancement rapide pour la soutenance (Linux)

## 0. Préparation
Le site statique frontend doit déjà être présent dans votre espace Webetu (`~/www/nancy-carte/`).

---

## 1. Compiler le code Java (Facultatif si déjà fait)
Ouvrir un terminal, je me place dans le dossier racine du projet (qui contient `start_demo.sh` et `RMI/`), puis je compile les modules Java :

```bash
cd RMI
mvn clean package
cd ..
```

---

## 2. Lancer le script de démonstration
Toujours dans le dossier racine du projet, je rends le script exécutable (une seule fois) et je le lance :

```bash
chmod +x start_demo.sh
./start_demo.sh
```

Le script va me demander :
1. **Identifiant Oracle** : tapez mon login étudiant
2. **Mot de passe Oracle** : tapez mon mot de passe 

Le script va me demander :
- Récupérer l'adresse IP de la machine sur le réseau de l'IUT.
- Mettre à jour l'application frontend en temps réel (en créant `~/www/nancy-carte/env.js`).
- Démarrer le serveur RMI en arrière-plan.
- Démarrer le serveur Proxy HTTP en arrière-plan.

---

## 3. Accéder à l'application

Une fois le script lancé, un message encadré confirmera que tout est en ligne.
J'ouvre un navigateur web et je vais sur l'URL affichée par le script :

```text
https://webetu.iutnc.univ-lorraine.fr/www/e52526u/nancy-carte/
```

> **En cas de problème d'IP / de requête bloquée :**
> Si le site charge mais affiche "Chargement des stations..." indéfiniment ou n'arrive pas à contacter le backend, je vais dans l'onglet ** Configuration** sur la page web. J'entre l'URL du proxy qui s'est affichée dans mon terminal (ex: `http://192.168.x.x:8080`) et je clique sur Sauvegarder.

---

## 4. Fermer proprement les serveurs

Je laisse le terminal ouvert pendant toute la soutenance.
Une fois la démonstration complètement terminée, je retourne sur ce terminal et j'appuie simplement sur ENTRÉE.

Le script se chargera de tuer (kill) proprement les processus Java (Serveur RMI et Proxy HTTP) tournant en arrière-plan.
