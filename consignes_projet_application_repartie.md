# S4 - IL - 02 - Projet d'application répartie

## Généralités
* **Annonces**
* **2025-2026** - Bloc notes pour inscrire vos groupes, votre lien git et votre page sur webetu.

## Évaluation
Consignes pour les présentations de SAE :
* Merci de renseigner la composition des équipes **sur ce pad**.
* Les présentations et démos de la SAE "Application Répartie" auront lieu le **mardi 16 juin en salle 125 ou 127** selon votre groupe.
* L'ordre et l'heure de passage seront mis en ligne sur la page ARCHE.
    * Vous devez utiliser le vidéo projecteur.
    * Chaque équipe aura 10 min pour présenter son travail.
    * Votre site web devra contenir un onglet qui illustre l'architecture de votre application et ses différents composants sur des schémas.
    * Tous les membres de l'équipe devront participer à la démonstration.
    * Essayer de scénariser votre présentation.

### Points évalués :
* Structuration du code, choix des classes/packages
* Robustesse de l'application au problème réseaux
* Ne pas coder les adresses IP ou les URL en dur dans le code
* Gestion des dépendances (Librairies externes) avec Maven ou npm
* Compilation automatique avec Maven ou Ant
* Javadoc de l'API public
* Documentation pour déployer et exécuter
* Exécution sur les machines de l'IUT
* Utilisation du serveur Web de webetu
* Déploiement automatique sur les machines de la salle.

### Considérations :
* Privilégier l'exécution sous linux car la démo se fera sous linux.
* Il n'y a pas de java sur la machine webetu (il ne servira qu'à héberger votre site).
* La version de java sur charlemagne n'est pas la même (plus vieille) que celle sur les machines du réseaux.
* Le SGBD sur charlemagne est Oracle. L'utilisation de MariaDB sur webetu sera plus difficile.

*Planning d'évaluation 2025/2026*

## Introduction
Cette SAÉ concerne les enseignements suivants :
* **R4.01** Architecture logicielle
* **R4.02** Qualité de dev. / tests
* **R4.03** Qualité et au-delà du relationnel
* **R4.A.10** Complément web

Le travail se fera par groupe de **4 étudiants**.

L'objectif est de proposer une application répartie, qui permettra d'afficher, dans un navigateur, sur une carte **Leaflet** des informations hétérogènes sur Nancy, informations vous appartenant, ou pas, transitant parfois par un proxy, qui fera la passerelle entre votre navigateur et des services.

Votre projet devra combiner les différents modules proposés ci-dessous, accessibles depuis un site web hébergé sur **webetu**.

Votre site devra proposer un onglet "compte rendu", contenant le compte rendu de votre travail :
* Un schéma complet de votre architecture, avec les technologies employées et échangées.
* Ce qui a été fait.
* Les informations nécessaires à l'utilisation de votre projet.
* ...et tout ce qui est utile à la compréhension de votre travail.
* Le lien vers votre dépôt Git avec tous les codes source.

À la fin de la SAÉ, vous devrez effectuer une démonstration. Pour cette démonstration vous exécuterez vos services sur des machines différentes de la salle de soutenance.

## Un service RMI qui interroge une base de données
Proposer un service RMI (et un client de test) permettant d'accéder à votre base de données de restaurants, base que vous devez enrichir et étendre.
* Ajouter à votre base des entrées de restaurants avec leur nom, leur adresse, et leurs coordonnées GPS (situer les à Nancy).
* Le service RMI devra pouvoir :
    * S'inscrire sur un service central.
    * Récupérer toutes les coordonnées de tous les restaurants nancéiens de votre base de données.
    * Réserver une table dans un restaurant en renseignant nom, prénom, nombre de convives, et coordonnées téléphoniques de la réservation.
* Les réponses du service devront se faire au format **JSON**.

## Des données ouvertes....

### Données partout...
De plus en plus de données dites *ouvertes* sont disponibles depuis le web, fournies par différents organismes, souvent d'états pour lesquels la diffusion est parfois obligatoire. Quelles informations propose **celle-ci** ?

### Visualisation des données
Créer une carte lisible depuis un navigateur, **centrée sur Nancy**. Ajouter sur votre carte, pour toutes les stations velibs :
* L'adresse de la station vélib.
* Le nombre de vélos disponible sur la station.
* Le nombre de places de parkings libres sur la station.

À noter que de **nombreuses autres données sont disponibles** et peuvent-être utilisées.

## Un service qui interroge les données bloquées..

### Des données bloquées sur le client
Certaines données posent problème. Essayer de récupérer, depuis votre site, **ces données**. En utilisant la console (cachée derrière la touche F-12) de votre navigateur, identifier le problème.

### Un service qui interroge des données ouvertes
En utilisant la classe **HttpClient** (en particulier le *Synchronous Example*) de java, proposer un service permettant de récupérer ces données.
* Penser à tester la bonne réception (code d'erreur) et le format des données.
* Attention, depuis les machines de l'IUT, vous devez renseigner un *proxy* pour accéder à l'extérieur. Vous trouverez les paramètres de ce proxy **dans la documentation informatique de l'iut**, vous devriez la retrouver dans la configuration par défaut de vos navigateurs (qui sont des clients http).
* Vous pouvez utiliser **ces classes**, et **leur documentation**.
* Pensez à gérer les erreurs possibles dues à l'utilisation d'un réseau.

## Un proxy qui interroge des services
À l'aide de la classe **HttpServer**, proposer un proxy entre votre site et les données bloquées.

Ce proxy devra proposer un service rmi donnant accès à vos services rmi de restaurants et aux "Incidents de circulation" liés aux travaux dans la Métropole du Grand Nancy.

En passant par ce proxy, ajouter sur votre carte :
* Les lieux des incidents prévus, avec leur adresse exacte, leur cause et les dates d'incidence.
* Votre liste de restaurants. Il doit être possible de réserver une table depuis la marque d'un restaurant.

*Au fait, est-ce responsable d'ainsi contourner la politique de sécurité de votre navigateur ?*
