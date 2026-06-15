#!/bin/bash

echo "=========================================================="
echo " Démarrage et Déploiement Local (Machine IUT)"
echo "=========================================================="

# 1. Demande des identifiants Oracle (Le mdp est caché avec -s)
read -p " Identifiant Oracle (ex: jdupont) : " DB_USER
read -s -p " Mot de passe Oracle : " DB_PASSWORD
echo ""

# 2. Récupération de l'adresse IP de la machine de l'IUT
SERVER_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
if [ -z "$SERVER_IP" ]; then
    SERVER_IP="127.0.0.1"
fi
echo " Utilisation de l'IP de la machine : $SERVER_IP"

# 3. Mise à jour de app.js (Frontend)
echo " Mise à jour de la configuration du Frontend (sans npm)..."
CONFIG_FILE="nancy-carte/dist/app.js"
# On remplace l'ancienne IP par la nouvelle IP, en cherchant le port 8080
sed "s|http://[a-zA-Z0-9.\-]*:8080|http://$SERVER_IP:8080|g" $CONFIG_FILE > $CONFIG_FILE.tmp
mv $CONFIG_FILE.tmp $CONFIG_FILE

# 5. Déploiement dans le ~/www de la machine
echo " Copie du site vers le dossier Webetu (~/www/carte-nancy)..."
mkdir -p ~/www/carte-nancy
cp nancy-carte/index.html ~/www/carte-nancy/
cp -r nancy-carte/dist ~/www/carte-nancy/

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
java -Xmx64m -Xss256k -jar proxy/target/rmi-proxy-1.0.jar &
PROXY_PID=$!

echo "=========================================================="
echo " TOUT EST EN LIGNE SUR CETTE MACHINE !"
echo " Frontend accessible sur : https://webetu.iutnc.univ-lorraine.fr/www/$USER/carte-nancy/"
echo " Proxy RMI écoute sur : http://$SERVER_IP:8080"
echo "=========================================================="
echo " [!] Laissez ce terminal ouvert pendant votre soutenance."
echo " Appuyez sur ENTRÉE pour fermer proprement les serveurs à la fin..."
read

echo " Fermeture des services..."
kill $RMI_PID
kill $PROXY_PID
echo " Au revoir !"
