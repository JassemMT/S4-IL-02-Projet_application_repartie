export interface ConfigurationInterface {
  tileLayerUrl: string;
  stationInformationUrl: string;
  stationStatusUrl: string;
  proxyUrl: string;
  incidentsUrl: string;

}

export const CONFIG: ConfigurationInterface = {
  tileLayerUrl: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",

  stationInformationUrl:
    "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_information.json",

  stationStatusUrl:
    "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_status.json",

  proxyUrl: "http://localhost:8080",

  incidentsUrl: 
    "http://localhost:8080/incidents",

};
