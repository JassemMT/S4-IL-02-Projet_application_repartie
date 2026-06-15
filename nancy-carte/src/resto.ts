/// <reference types="leaflet" />

import { map, onSourceLoaded, onSourceError } from "./index";
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
    const popupContent = document.createElement("div");
    popupContent.innerHTML = `
        <div style="font-family: Arial, sans-serif;">
            <strong style="color: #333; font-size: 14px;">${restaurant.nom}</strong><br>
            <span style="font-size: 12px; color: #666;">${restaurant.adresse}</span>
            <hr style="border: 0; border-top: 1px solid #ccc; margin: 10px 0;">
            <form id="form-resa-${restaurant.id}" style="display: flex; flex-direction: column; gap: 5px;">
                <input type="text" name="nom" placeholder="Nom" required style="padding: 4px; font-size: 12px;">
                <input type="text" name="prenom" placeholder="Prénom" required style="padding: 4px; font-size: 12px;">
                <input type="tel" name="telephone" placeholder="Téléphone" required style="padding: 4px; font-size: 12px;">
                <input type="number" name="convives" placeholder="Nb convives" min="1" max="20" required style="padding: 4px; font-size: 12px;">
                <div style="display: flex; gap: 5px;">
                    <input type="date" name="date" required style="padding: 4px; font-size: 12px; flex: 1;" id="resa-date-${restaurant.id}">
                    <select name="heure" required style="padding: 4px; font-size: 12px; width: 80px;">
                        <option value="12:00">12:00</option>
                        <option value="19:00">19:00</option>
                        <option value="21:00">21:00</option>
                    </select>
                </div>
                <button type="submit" style="margin-top: 5px; padding: 6px; background-color: #277d4a; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px;">Réserver</button>
                <div id="msg-resa-${restaurant.id}" style="font-size: 12px; text-align: center; margin-top: 5px; min-height: 15px;"></div>
            </form>
        </div>
    `;

    // Met la date d'aujourd'hui par défaut
    setTimeout(() => {
        const dateInput = popupContent.querySelector(`#resa-date-${restaurant.id}`) as HTMLInputElement;
        if (dateInput) {
            dateInput.value = new Date().toISOString().split('T')[0];
        }
    }, 0);

    popupContent.querySelector("form")?.addEventListener("submit", (e) => {
        e.preventDefault();
        const form = e.target as HTMLFormElement;
        const msgDiv = popupContent.querySelector(`#msg-resa-${restaurant.id}`) as HTMLElement;
        const btn = form.querySelector("button") as HTMLButtonElement;
        
        msgDiv.textContent = "Réservation en cours...";
        msgDiv.style.color = "#333";
        btn.disabled = true;

        const payload = {
            idRestaurant: restaurant.id,
            nom: (form.elements.namedItem("nom") as HTMLInputElement).value,
            prenom: (form.elements.namedItem("prenom") as HTMLInputElement).value,
            telephone: (form.elements.namedItem("telephone") as HTMLInputElement).value,
            convives: parseInt((form.elements.namedItem("convives") as HTMLInputElement).value, 10),
            date: (form.elements.namedItem("date") as HTMLInputElement).value,
            heure: (form.elements.namedItem("heure") as HTMLInputElement).value
        };

        fetch(`${CONFIG.proxyUrl}/reserver`, {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(payload)
        })
        .then(verifierReponse)
        .then(r => r.json())
        .then((res: any) => {
            if (res.status === "ok") {
                msgDiv.textContent = "Réservation confirmée !";
                msgDiv.style.color = "green";
                form.reset();
            } else {
                msgDiv.textContent = res.message || "Erreur serveur";
                msgDiv.style.color = "red";
            }
            btn.disabled = false;
        })
        .catch(err => {
            msgDiv.textContent = "Connexion au proxy échouée";
            msgDiv.style.color = "red";
            btn.disabled = false;
        });
    });

    L.marker([restaurant.lat, restaurant.lng], { icon: createRestaurantIcon() })
        .addTo(map)
        .bindPopup(popupContent);
}

function afficherRestaurants(restaurants: RestaurantInterface[]): void {
    const sidebarItems: { name: string; lat: number; lng: number }[] = [];
    for (const restaurant of restaurants) {
        afficherRestaurant(restaurant);
        sidebarItems.push({ name: restaurant.nom, lat: restaurant.lat, lng: restaurant.lng });
    }
    console.log(`${restaurants.length} restaurants chargés.`);
    onSourceLoaded("restaurants", sidebarItems);
}

fetch(`${CONFIG.proxyUrl}/restaurants`)
    .then(verifierReponse)
    .then((r) => r.json() as Promise<RestaurantInterface[]>)
    .then(afficherRestaurants)
    .catch((e) => { console.error("Impossible de charger les restaurants :", e); onSourceError("restaurants"); });