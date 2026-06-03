// src/config.ts
var CONFIG = {
  tileLayerUrl: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
  stationInformationUrl: "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_information.json",
  stationStatusUrl: "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_status.json"
};

// img/velo-meca.svg
var velo_meca_default = '<svg xmlns="http://www.w3.org/2000/svg" id="separateur-titre" viewBox="0 0 38 22"><path d="M29.85,7.78c-.8,0-1.56,.14-2.28,.38l-2.04-5.98c-.38-1.11-1.42-1.86-2.6-1.86h-1.71c-.58,0-1.05,.47-1.05,1.05s.47,1.05,1.05,1.05h1.71c.28,0,.52,.18,.61,.44l.3,.88H15l-.14-.43,.47-.06c.41-.05,.72-.4,.73-.82v-.09c0-.6-.45-1.1-1.04-1.16l-2.91-.28c-.35-.03-.7,.09-.95,.35-.25,.25-.37,.6-.33,.96l.1,.85c.02,.19,.12,.37,.27,.49,.15,.12,.35,.17,.54,.15l1-.12,.32,.98-2.25,3.76c-.83-.34-1.75-.54-2.7-.54-3.93,0-7.11,3.18-7.11,7.11s3.18,7.11,7.11,7.11c3.57,0,6.51-2.63,7.02-6.06h3c.31,0,.6-.14,.8-.37l6.37-7.53,.37,1.1c-1.78,1.29-2.94,3.38-2.94,5.75,0,3.93,3.18,7.11,7.11,7.11s7.11-3.18,7.11-7.11-3.18-7.11-7.11-7.11Zm-15.91-.59l2.2,6.65h-1c-.26-1.78-1.18-3.35-2.52-4.45l1.32-2.2Zm-2.41,4.04c.74,.69,1.27,1.59,1.49,2.61h-3.05l1.56-2.61Zm-3.41,8.67c-2.76,0-5.01-2.25-5.01-5.01s2.25-5.01,5.01-5.01c.56,0,1.1,.1,1.61,.27l-2.51,4.2c-.19,.32-.2,.73-.01,1.06,.19,.33,.54,.53,.91,.53h4.9c-.48,2.26-2.49,3.96-4.9,3.96Zm10.04-6.65l-2.45-7.41h8.72l-6.27,7.41Zm11.7,6.65c-2.76,0-5.01-2.25-5.01-5.01,0-1.43,.6-2.72,1.57-3.63,.56,1.62,1.49,3.13,2.69,4.36,.21,.21,.48,.32,.75,.32s.53-.1,.73-.3c.42-.4,.43-1.07,.02-1.48-1-1.03-1.77-2.29-2.23-3.64l-.13-.37c.5-.17,1.04-.27,1.6-.27,2.76,0,5.01,2.25,5.01,5.01s-2.25,5.01-5.01,5.01Z" fill="#ffffff"/></svg>';

// src/app.ts
var latitudeNancy = 48.6921;
var longitudeNancy = 6.1844;
var map = L.map("map").setView(
  [latitudeNancy, longitudeNancy],
  // niveau de zoom
  13
);
var messageElement = document.querySelector("#message");
function afficherMessage(message) {
  if (messageElement !== null) {
    messageElement.textContent = message;
  }
}
function verifierReponse(response) {
  if (!response.ok) {
    throw new Error(
      "Erreur HTTP : " + response.status
    );
  }
  return response;
}
function lireInformationsStations(response) {
  return response.json();
}
function lireDisponibilitesStations(response) {
  return response.json();
}
function ajouterFondDeCarte() {
  L.tileLayer(
    CONFIG.tileLayerUrl,
    {
      maxZoom: 19,
      attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
    }
  ).addTo(map);
}
function createIconWithLogo() {
  const html = `<div class="marker-pin"><div class="marker-dot">${velo_meca_default}</div></div>`;
  return L.divIcon({
    className: "custom-div-icon",
    html,
    iconSize: [44, 60],
    iconAnchor: [22, 60],
    popupAnchor: [0, -45]
  });
}
function chercherDisponibilite(stationId, disponibilites) {
  for (const disponibilite of disponibilites) {
    if (disponibilite.station_id === stationId) {
      return disponibilite;
    }
  }
  return void 0;
}
function afficherStation(station, disponibilite) {
  const adresse = station.address || "Adresse non renseign\xE9e";
  const capaciteTotale = disponibilite.num_bikes_available + disponibilite.num_docks_available;
  const stationInstallee = disponibilite.is_installed ? "Oui" : "Non";
  const empruntPossible = disponibilite.is_renting ? "Oui" : "Non";
  const retourPossible = disponibilite.is_returning ? "Oui" : "Non";
  const derniereMiseAJour = new Date(disponibilite.last_reported * 1e3).toLocaleString("fr-FR");
  L.marker([station.lat, station.lon], { icon: createIconWithLogo() }).addTo(
    map
  ).bindPopup(`
            <strong>${station.name}</strong><br><br>

            Adresse : ${adresse}<br>
            Capacit\xE9 totale : ${capaciteTotale}<br><br>

            V\xE9los disponibles :
            ${disponibilite.num_bikes_available}<br>

            Places libres :
            ${disponibilite.num_docks_available}<br><br>

            Station install\xE9e : ${stationInstallee}<br>
            Emprunt possible : ${empruntPossible}<br>
            Retour possible : ${retourPossible}<br><br>

            Derni\xE8re mise \xE0 jour :
            ${derniereMiseAJour}
        `);
}
function afficherStations(reponses) {
  const stations = reponses[0].data.stations;
  const disponibilites = reponses[1].data.stations;
  for (const station of stations) {
    const disponibilite = chercherDisponibilite(
      station.station_id,
      disponibilites
    );
    if (disponibilite !== void 0) {
      afficherStation(
        station,
        disponibilite
      );
    }
  }
  afficherMessage(
    stations.length + " stations charg\xE9es. Clique sur un marqueur."
  );
}
function afficherErreur(erreur) {
  afficherMessage(
    "Impossible de charger les stations. Ouvre la console avec F12."
  );
  if (erreur instanceof Error) {
    console.log(erreur.message);
  }
}
ajouterFondDeCarte();
var informationsPromise = fetch(CONFIG.stationInformationUrl).then(verifierReponse).then(lireInformationsStations);
var disponibilitesPromise = fetch(CONFIG.stationStatusUrl).then(verifierReponse).then(lireDisponibilitesStations);
Promise.all([
  informationsPromise,
  disponibilitesPromise
]).then((results) => {
  afficherStations([results[0], results[1]]);
}).catch(afficherErreur);
