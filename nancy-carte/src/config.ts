// Interface pour `window.APP_CONFIG` défini dans env.js par start_demo.sh
declare global {
  interface Window {
    APP_CONFIG?: {
      proxyUrl?: string;
    };
  }
}

export interface ConfigurationInterface {
  tileLayerUrl: string;
  stationInformationUrl: string;
  stationStatusUrl: string;
  proxyUrl: string;
  incidentsUrl: string;
}

// 1. Priorité au localStorage (si l'utilisateur a utilisé l'interface de configuration)
// 2. Sinon, on lit la variable globale injectée par env.js (déployé par start_demo.sh)
// 3. Fallback sur localhost (pour le dev local)
const proxyBaseUrl = 
  localStorage.getItem("proxyUrl") || 
  window.APP_CONFIG?.proxyUrl || 
  "http://localhost:8080";

export const CONFIG: ConfigurationInterface = {
  tileLayerUrl: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",

  stationInformationUrl:
    "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_information.json",

  stationStatusUrl:
    "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_status.json",

  proxyUrl: proxyBaseUrl,

  incidentsUrl: `${proxyBaseUrl}/incidents`,
};
