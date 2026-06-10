/// <reference types="leaflet" />

import { CONFIG } from "./config";
import { map } from "./index";
import logoSvg from "../img/velo-meca.svg";
import {
    StationInformationInterface,
    StationStatusInterface,
    StationInformationResponseInterface,
    StationStatusResponseInterface
} from "./types";

function verifierReponse(response: Response): Response {
    if (!response.ok) {
        throw new Error("Erreur HTTP : " + response.status);
    }
    return response;
}

function createIconWithLogo(): L.DivIcon {
    return L.divIcon({
        className: "custom-div-icon",
        html: `<div class="marker-pin"><div class="marker-dot">${logoSvg}</div></div>`,
        iconSize: [44, 60],
        iconAnchor: [22, 60],
        popupAnchor: [0, -45]
    });
}

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

function afficherStation(
    station: StationInformationInterface,
    disponibilite: StationStatusInterface
): void {
    const adresse = station.address ?? "Adresse non renseignée";
    const capaciteTotale = disponibilite.num_bikes_available + disponibilite.num_docks_available;
    const derniereMiseAJour = new Date(disponibilite.last_reported * 1000).toLocaleString("fr-FR");

    L.marker([station.lat, station.lon], { icon: createIconWithLogo() })
        .addTo(map)
        .bindPopup(`
            <strong>${station.name}</strong><br><br>
            Adresse : ${adresse}<br>
            Capacité totale : ${capaciteTotale}<br><br>
            Vélos disponibles : ${disponibilite.num_bikes_available}<br>
            Places libres : ${disponibilite.num_docks_available}<br><br>
            Dernière mise à jour : ${derniereMiseAJour}
        `);
}

function afficherStations(
    reponses: [StationInformationResponseInterface, StationStatusResponseInterface]
): void {
    const stations = reponses[0].data.stations;
    const disponibilites = reponses[1].data.stations;

    for (const station of stations) {
        const disponibilite = chercherDisponibilite(station.station_id, disponibilites);
        if (disponibilite !== undefined) {
            afficherStation(station, disponibilite);
        }
    }
}

function afficherErreur(erreur: unknown): void {
    if (erreur instanceof Error) {
        console.log(erreur.message);
    }
}

const informationsPromise = fetch(CONFIG.stationInformationUrl)
    .then(verifierReponse)
    .then((r) => r.json() as Promise<StationInformationResponseInterface>);

const disponibilitesPromise = fetch(CONFIG.stationStatusUrl)
    .then(verifierReponse)
    .then((r) => r.json() as Promise<StationStatusResponseInterface>);

Promise.all([informationsPromise, disponibilitesPromise])
    .then(afficherStations)
    .catch(afficherErreur);