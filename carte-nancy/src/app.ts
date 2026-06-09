/// <reference types="leaflet" />
// On est obliger de mettre cela pour utiliser la variable globale de Leaflet

/*
    Types des données reçues depuis les API
*/
import { CONFIG } from "./config";
import logoSvg from "../img/velo-meca.svg";
import restaurantSvg from "../img/restaurant-icon.svg";

interface StationInformationInterface {
    station_id: string;
    name: string;
    address?: string;
    lat: number;
    lon: number;
    capacity?: number,
}


interface StationStatusInterface {
    station_id: string;
    num_bikes_available: number;
    num_docks_available: number;
    is_installed: boolean;
    is_renting: boolean;
    is_returning: boolean;
    last_reported: number;
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

// Génère un L.DivIcon contenant le logo SVG dans le rond vert
function createIconWithLogo(): L.DivIcon {
    const html = `<div class="marker-pin"><div class="marker-dot">${logoSvg}</div></div>`;

        return L.divIcon({
                className: 'custom-div-icon',
                html,
                iconSize: [44, 60],
                iconAnchor: [22, 60],
                popupAnchor: [0, -45]
        });
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

function afficherStation( station: StationInformationInterface, disponibilite: StationStatusInterface,): void {
  const adresse: string = station.address || "Adresse non renseignée";

  const capaciteTotale: number =
    disponibilite.num_bikes_available + disponibilite.num_docks_available;

  const stationInstallee: string = disponibilite.is_installed ? "Oui" : "Non";

  const empruntPossible: string = disponibilite.is_renting ? "Oui" : "Non";

  const retourPossible: string = disponibilite.is_returning ? "Oui" : "Non";

  const derniereMiseAJour: string = new Date(disponibilite.last_reported * 1000).toLocaleString("fr-FR");

  L.marker([station.lat, station.lon], { icon: createIconWithLogo() }).addTo(
    map,
  ).bindPopup(`
            <strong>${station.name}</strong><br><br>

            Adresse : ${adresse}<br>
            Capacité totale : ${capaciteTotale}<br><br>

            Vélos disponibles :
            ${disponibilite.num_bikes_available}<br>

            Places libres :
            ${disponibilite.num_docks_available}<br><br>

            Station installée : ${stationInstallee}<br>
            Emprunt possible : ${empruntPossible}<br>
            Retour possible : ${retourPossible}<br><br>

            Dernière mise à jour :
            ${derniereMiseAJour}
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
    Types des données restaurants (depuis notre proxy RMI)
*/

interface RestaurantInterface {
    id: number;
    nom: string;
    adresse: string;
    latitude: number;
    longitude: number;
}


/*
    Types des données incidents Grand Nancy 
*/

interface IncidentGeoPoint {
    lon: number;
    lat: number;
}

interface IncidentInterface {
    geo_point_2d?: IncidentGeoPoint;
    adresse_exacte?: string;
    adresse?: string;
    cause?: string;
    motif?: string;
    date_debut?: string;
    date_fin?: string;
}

interface IncidentsResponseInterface {
    total_count: number;
    results: IncidentInterface[];
}


/*
    Icône marqueur restaurant (rouge)
*/

function createRestaurantIcon(): L.DivIcon {
    return L.divIcon({
        className: "restaurant-marker",
        html: `<div class="marker-pin"><div class="marker-dot">${restaurantSvg}</div></div>`,
        iconSize: [44, 60],
        iconAnchor: [22, 60],
        popupAnchor: [0, -45],
    });
}


/*
    Icône marqueur incident (orange)
*/

function createIncidentIcon(): L.DivIcon {
    return L.divIcon({
        className: "incident-marker",
        html: `<div class="marker-pin"><div class="marker-dot">⚠</div></div>`,
        iconSize: [44, 60],
        iconAnchor: [22, 60],
        popupAnchor: [0, -45],
    });
}


/*
    Affiche un marqueur restaurant sur la carte
*/

function afficherRestaurant(restaurant: RestaurantInterface): void {
    L.marker([restaurant.latitude, restaurant.longitude], { icon: createRestaurantIcon() })
        .addTo(map)
        .bindPopup(`
            <strong>${restaurant.nom}</strong><br><br>
            Adresse : ${restaurant.adresse}
        `);
}

function afficherRestaurants(restaurants: RestaurantInterface[]): void {
    for (const restaurant of restaurants) {
        afficherRestaurant(restaurant);
    }
    console.log(`${restaurants.length} restaurants chargés.`);
}


/*
    Affiche un marqueur incident sur la carte
*/

function afficherIncident(incident: IncidentInterface): void {
   //Yanis devra le faire
}

function afficherIncidents(data: IncidentsResponseInterface): void {
    //Yanis devra le faire
}


/*
    Début du programme
*/

ajouterFondDeCarte();

// Stations vélo (API Cyclocity directe)
const informationsPromise: Promise<StationInformationResponseInterface> =
    fetch(CONFIG.stationInformationUrl)
        .then(verifierReponse)
        .then(lireInformationsStations);

const disponibilitesPromise: Promise<StationStatusResponseInterface> =
    fetch(CONFIG.stationStatusUrl)
        .then(verifierReponse)
        .then(lireDisponibilitesStations);

Promise.all([informationsPromise, disponibilitesPromise])
    .then((results) => {
        afficherStations([
            results[0] as StationInformationResponseInterface,
            results[1] as StationStatusResponseInterface,
        ]);
    })
    .catch(afficherErreur);

// Restaurants (via proxy RMI)
fetch(`${CONFIG.proxyUrl}/restaurants`)
    .then(verifierReponse)
    .then((r) => r.json() as Promise<RestaurantInterface[]>)
    .then(afficherRestaurants)
    .catch((e) => console.error("Impossible de charger les restaurants :", e));

// Incidents de circulation Grand Nancy (via proxy HttpClient)
//Yanis devra le faire de la meme maniere que les restaurants