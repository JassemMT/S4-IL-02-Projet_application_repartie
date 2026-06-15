#!/bin/bash

echo "=========================================================="
echo " Lancement du Serveur Backend (RMI uniquement)"
echo "=========================================================="

# 1. Demande des identifiants
read -p " Identifiant Oracle (ex: jdupont) : " DB_USER
read -s -p " Mot de passe Oracle : " DB_PASSWORD
echo ""

# 2. Récupération de l'adresse IP de la machine
SERVER_IP=$(hostname -I 2>/dev/null | awk '{print $1}')
if [ -z "$SERVER_IP" ]; then
    SERVER_IP="127.0.0.1"
fi
echo " IP de la machine : $SERVER_IP"

# 3. Lancement du Serveur RMI
cd RMI || exit

echo " Démarrage du Serveur RMI..."
# Lancement du serveur avec limites de RAM pour éviter les crashs
java -Xmx64m -Xss256k -cp "service/target/rmi-service-1.0.jar:service/ojdbc17.jar" sae.s4.rmi.service.LancerServeur "$DB_USER" "$DB_PASSWORD" &
RMI_PID=$!

echo "=========================================================="
echo " SERVEUR RMI EN LIGNE !"
echo " Le Serveur RMI est prêt sur le port 1099"
echo "=========================================================="
echo " [!] Laissez ce terminal ouvert."
echo " Appuyez sur ENTRÉE pour fermer proprement le serveur..."
read

echo " Fermeture du service..."
kill $RMI_PID
echo " Au revoir !"
