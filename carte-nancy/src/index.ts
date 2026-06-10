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