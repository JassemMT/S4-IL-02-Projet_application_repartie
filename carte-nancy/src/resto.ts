/// <reference types="leaflet" />

import { map } from "./index";
import { CONFIG } from "./config";
import restaurantSvg from "../img/restaurant-icon.svg";
import { RestaurantInterface } from "./types";

function createRestaurantIcon(): L.DivIcon {
    return L.divIcon({
        className: "restaurant-marker",
        html: `<div class="marker-pin"><div class="marker-dot">${restaurantSvg}</div></div>`,
        iconSize: [44, 60],
        iconAnchor: [22, 60],
        popupAnchor: [0, -45],
    });
}

function verifierReponse(response: Response): Response {
    if (!response.ok) {
        throw new Error("Erreur HTTP : " + response.status);
    }
    return response;
}

function afficherRestaurant(restaurant: RestaurantInterface): void {
    L.marker([restaurant.lat, restaurant.lng], { icon: createRestaurantIcon() })
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

fetch(`${CONFIG.proxyUrl}/restaurants`)
    .then(verifierReponse)
    .then((r) => r.json() as Promise<RestaurantInterface[]>)
    .then(afficherRestaurants)
    .catch((e) => console.error("Impossible de charger les restaurants :", e));