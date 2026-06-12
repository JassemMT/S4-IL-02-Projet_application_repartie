/// <reference types="leaflet" />

import { map, onSourceLoaded, onSourceError } from "./index";
import { CONFIG } from "./config";
import { IncidentInterface, IncidentsResponseInterface } from "./types";

function verifierReponse(response: Response): Response {
    if (!response.ok) {
        throw new Error("Erreur HTTP : " + response.status);
    }
    return response;
}

function createIncidentIcon(): L.DivIcon {
    return L.divIcon({
        className: "incident-marker",
        html: `<div class="marker-pin"><div class="marker-dot">⚠</div></div>`,
        iconSize: [44, 60],
        iconAnchor: [22, 60],
        popupAnchor: [0, -45],
    });
}

function afficherIncident(incident: IncidentInterface): void {
    const coords = incident.location.polyline.split(" ");
    const lat = parseFloat(coords[0]);
    const lng = parseFloat(coords[1]);

    const dateDebut = new Date(incident.starttime).toLocaleDateString("fr-FR");
    const dateFin = new Date(incident.endtime).toLocaleDateString("fr-FR");

    L.marker([lat, lng], { icon: createIncidentIcon() })
        .addTo(map)
        .bindPopup(`
            <strong>${incident.short_description}</strong><br><br>
            ${incident.location.location_description}<br><br>
            ${incident.description}<br><br>
            Du ${dateDebut} au ${dateFin}
        `);
}

function afficherIncidents(data: IncidentsResponseInterface): void {
    const sidebarItems: { name: string; lat: number; lng: number }[] = [];
    for (const incident of data.incidents) {
        afficherIncident(incident);
        const coords = incident.location.polyline.split(" ");
        sidebarItems.push({
            name: incident.short_description,
            lat: parseFloat(coords[0]),
            lng: parseFloat(coords[1])
        });
    }
    console.log(`${data.incidents.length} incidents chargés.`);
    onSourceLoaded("incidents", sidebarItems);
}

function afficherErreur(erreur: unknown): void {
    if (erreur instanceof Error) {
        console.error("Impossible de charger les incidents :", erreur.message);
    }
    onSourceError("incidents");
}

fetch(`${CONFIG.proxyUrl}/incidents`)
    .then(verifierReponse)
    .then((r) => r.json() as Promise<IncidentsResponseInterface>)
    .then(afficherIncidents)
    .catch(afficherErreur);