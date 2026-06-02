// src/config.ts
var CONFIG = {
  tileLayerUrl: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
  stationInformationUrl: "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_information.json",
  stationStatusUrl: "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_status.json"
};

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
  L.marker([
    station.lat,
    station.lon
  ]).addTo(map).bindPopup(`
            <strong>${station.name}</strong><br>
            Adresse : ${adresse}<br>
            V\xE9los disponibles : ${disponibilite.num_bikes_available}<br>
            Places libres : ${disponibilite.num_docks_available}
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
]).then(afficherStations).catch(afficherErreur);
