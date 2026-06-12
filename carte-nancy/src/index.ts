/// <reference types="leaflet" />

import { CONFIG } from "./config";
import "./station";
import "./resto";
import "./incidents";

export const map: L.Map = L.map("map").setView(
    [48.6921, 6.1844],
    13
);

L.tileLayer(CONFIG.tileLayerUrl, {
    maxZoom: 19,
    attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);

// --- Gestion du chargement et de la sidebar ---

interface SidebarItem {
    name: string;
    lat: number;
    lng: number;
}

const sidebarData: {
    velos: SidebarItem[];
    restaurants: SidebarItem[];
    incidents: SidebarItem[];
} = { velos: [], restaurants: [], incidents: [] };

let loadedCount = 0;
const TOTAL_SOURCES = 3;

function checkAllLoaded(): void {
    loadedCount++;
    if (loadedCount >= TOTAL_SOURCES) {
        const msgEl = document.getElementById("message");
        if (msgEl) {
            msgEl.style.display = "none";
        }
        renderSidebar();
    }
}

function renderSidebar(): void {
    const buildList = (items: SidebarItem[], containerId: string) => {
        const ul = document.getElementById(containerId);
        if (!ul) return;
        ul.innerHTML = "";
        if (items.length === 0) {
            ul.innerHTML = "<li class='sidebar-empty'>Aucun élément</li>";
            return;
        }
        for (const item of items) {
            const li = document.createElement("li");
            li.textContent = item.name;
            li.title = item.name;
            li.addEventListener("click", () => {
                map.setView([item.lat, item.lng], 17);
            });
            ul.appendChild(li);
        }
    };

    // Update counts in headers
    const countVelos = document.getElementById("count-velos");
    const countRestos = document.getElementById("count-restos");
    const countIncidents = document.getElementById("count-incidents");
    if (countVelos) countVelos.textContent = `(${sidebarData.velos.length})`;
    if (countRestos) countRestos.textContent = `(${sidebarData.restaurants.length})`;
    if (countIncidents) countIncidents.textContent = `(${sidebarData.incidents.length})`;

    buildList(sidebarData.velos, "list-velos");
    buildList(sidebarData.restaurants, "list-restos");
    buildList(sidebarData.incidents, "list-incidents");
}

export function onSourceLoaded(
    source: "velos" | "restaurants" | "incidents",
    items: SidebarItem[]
): void {
    sidebarData[source] = items;
    checkAllLoaded();
}

export function onSourceError(source: "velos" | "restaurants" | "incidents"): void {
    // On error, mark as loaded anyway (with empty list) so loading message disappears
    checkAllLoaded();
}