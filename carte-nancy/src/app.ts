/// <reference types="leaflet" />
// On est obliger de mettre cela pour utiliser la variable globale de Leaflet

/*
    Types des données reçues depuis les API
*/
import { CONFIG } from "./config";

interface StationInformationInterface {
    station_id: string;
    name: string;
    address?: string;
    lat: number;
    lon: number;
}

interface StationStatusInterface {
    station_id: string;
    num_bikes_available: number;
    num_docks_available: number;
}

interface StationInformationResponseInterface {
    data: {
        stations: StationInformationInterface[];
    };
}

interface StationStatusResponseInterface {
    data: {
        stations: StationStatusInterface[];
    };
}


/*
    Création de la carte centrée sur Nancy
*/

const latitudeNancy: number = 48.6921;
const longitudeNancy: number = 6.1844;

const map: L.Map = L.map("map").setView(
  [latitudeNancy, longitudeNancy],
  // niveau de zoom
    13
);


/*
    Zone HTML utilisée pour afficher un message
*/

// on ecrit dans l'html
const messageElement: HTMLElement | null =
    document.querySelector("#message");

  
function afficherMessage(message: string): void {
    if (messageElement !== null) {
        messageElement.textContent = message;
    }
}


/*
    Vérifie si une requête HTTP s'est bien passée
*/

function verifierReponse(response: Response): Response {
    if (!response.ok) {
        throw new Error(
            "Erreur HTTP : " + response.status
        );
    }

    return response;
}


/*
    Convertit les réponses JSON
*/

function lireInformationsStations(
    response: Response
): Promise<StationInformationResponseInterface> {
    return response.json() as Promise<StationInformationResponseInterface>;
}

function lireDisponibilitesStations(
    response: Response
): Promise<StationStatusResponseInterface> {
    return response.json() as Promise<StationStatusResponseInterface>;
}


/*
    Ajoute le fond de carte OpenStreetMap
*/

function ajouterFondDeCarte(): void {
    L.tileLayer(
        CONFIG.tileLayerUrl,
        {
            maxZoom: 19,

            attribution:
                '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
        }
    ).addTo(map);
}


/*
    Cherche les disponibilités correspondant à une station
*/

  function chercherDisponibilite(
      stationId: string,
      disponibilites: StationStatusInterface[]
  ): StationStatusInterface | undefined {
      for (const disponibilite of disponibilites) {
          if (disponibilite.station_id === stationId) {
              return disponibilite;
          }
      }

      return undefined;
  }


/*
    Affiche un marqueur sur la carte ( affiche seulement une station)
*/

function afficherStation(
    station: StationInformationInterface,
    disponibilite: StationStatusInterface
): void {
    const adresse: string =
        station.address || "Adresse non renseignée";

    L.marker([
        station.lat,
        station.lon
    ])
        .addTo(map)
        .bindPopup(`
            <strong>${station.name}</strong><br>
            Adresse : ${adresse}<br>
            Vélos disponibles : ${disponibilite.num_bikes_available}<br>
            Places libres : ${disponibilite.num_docks_available}
        `);
}


/*
    Affiche toutes les stations
*/

function afficherStations(
    reponses: [
        StationInformationResponseInterface,
        StationStatusResponseInterface
    ]
): void {
    const stations: StationInformationInterface[] =
        reponses[0].data.stations;

    const disponibilites: StationStatusInterface[] =
        reponses[1].data.stations;

    for (const station of stations) {
        const disponibilite:
            StationStatusInterface | undefined =

            chercherDisponibilite(
                station.station_id,
                disponibilites
            );
          // si ya un nombre de vélos pas undefined
        if (disponibilite !== undefined) {
            afficherStation(
                station,
                disponibilite
            );
        }
    }
    // On écrit dans le HTML
    afficherMessage(
        stations.length +
        " stations chargées. Clique sur un marqueur."
    );
}


/*
    Affiche une erreur si une requête échoue
*/

function afficherErreur(erreur: unknown): void {
    afficherMessage(
        "Impossible de charger les stations. Ouvre la console avec F12."
    );

    if (erreur instanceof Error) {
        console.log(erreur.message);
    }
}


/*
    Début du programme
*/

ajouterFondDeCarte();

const informationsPromise: Promise<StationInformationResponseInterface> =
    fetch(CONFIG.stationInformationUrl)
        .then(verifierReponse)
        .then(lireInformationsStations);


const disponibilitesPromise: Promise<StationStatusResponseInterface> =
    fetch(CONFIG.stationStatusUrl)
        .then(verifierReponse)
        .then(lireDisponibilitesStations);


Promise.all([
    informationsPromise,
    disponibilitesPromise
])
    .then(afficherStations)
    .catch(afficherErreur);