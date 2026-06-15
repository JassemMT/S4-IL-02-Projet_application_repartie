#!/bin/bash

echo "=========================================================="
echo " Démarrage et Déploiement Local (Machine IUT)"
echo "=========================================================="

# 1. Demande des identifiants et configuration
read -p " Identifiant Oracle (ex: jdupont) : " DB_USER
read -s -p " Mot de passe Oracle : " DB_PASSWORD
echo ""
read -p " Faut-il activer le proxy IUT pour l'accès Internet ? (O/n) : " USE_IUT_PROXY
echo ""

if [[ "$USE_IUT_PROXY" =~ ^[nN]$ ]]; then
    PROXY_ARG="--useIutProxy false"
else
    PROXY_ARG="--useIutProxy true"
fi

# 2. Récupération de l'adresse IP de la machine de l'IUT
SERVER_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
if [ -z "$SERVER_IP" ]; then
    SERVER_IP="127.0.0.1"
fi
echo " Utilisation de l'IP de la machine : $SERVER_IP"

# 3. Mise à jour de la configuration dynamique du Frontend sur Webetu
echo " Génération de env.js avec l'IP du serveur dans ~/www/nancy-carte..."
echo "window.APP_CONFIG = { proxyUrl: 'http://$SERVER_IP:8080' };" > ~/www/nancy-carte/env.js

# 6. Lancement du Backend
echo " Démarrage du Serveur RMI..."
cd RMI || exit

# (Optionnel) Compilation Java
# mvn clean package -q

# Lancement du serveur avec limites de RAM pour éviter les crashs sur la machine IUT
java -Xmx64m -Xss256k -cp "service/target/rmi-service-1.0.jar:service/ojdbc17.jar" sae.s4.rmi.service.LancerServeur "$DB_USER" "$DB_PASSWORD" &
RMI_PID=$!

echo " Attente (2s) de l'initialisation de l'annuaire RMI..."
sleep 2

echo " Lancement du Proxy HTTP..."
java -Xmx64m -Xss256k -jar proxy/target/rmi-proxy-1.0.jar $PROXY_ARG &
PROXY_PID=$!

echo "=========================================================="
echo " TOUT EST EN LIGNE SUR CETTE MACHINE !"
echo " Frontend accessible sur : https://webetu.iutnc.univ-lorraine.fr/www/$USER/nancy-carte/"
echo " Proxy RMI écoute sur : http://$SERVER_IP:8080"
echo "=========================================================="
echo " [!] Laissez ce terminal ouvert pendant votre soutenance."
echo " Appuyez sur ENTRÉE pour fermer proprement les serveurs à la fin..."
read

echo " Fermeture des services..."
kill $RMI_PID
kill $PROXY_PID
echo " Au revoir !"
