export interface ConfigurationInterface {
  tileLayerUrl: string;
  stationInformationUrl: string;
  stationStatusUrl: string;
}

export const CONFIG: ConfigurationInterface = {
  tileLayerUrl: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",

  stationInformationUrl:
    "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_information.json",

  stationStatusUrl:
    "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_status.json",
};
