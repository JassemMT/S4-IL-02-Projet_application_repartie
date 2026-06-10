// Vélibs
export interface StationInformationInterface {
    station_id: string;
    name: string;
    address?: string;
    lat: number;
    lon: number;
    capacity?: number;
}

export interface StationStatusInterface {
    station_id: string;
    num_bikes_available: number;
    num_docks_available: number;
    is_installed: boolean;
    is_renting: boolean;
    is_returning: boolean;
    last_reported: number;
}

export interface StationInformationResponseInterface {
    data: { stations: StationInformationInterface[] };
}

export interface StationStatusResponseInterface {
    data: { stations: StationStatusInterface[] };
}

// Restaurants
export interface RestaurantInterface {
    id: number;
    nom: string;
    adresse: string;
    lat: number;
    lng: number;
}

// Incidents
export interface IncidentLocationInterface {
    street: string;
    polyline: string;
    location_description: string;
}

export interface IncidentInterface {
    id: string;
    type: string;
    short_description: string;
    description: string;
    starttime: string;
    endtime: string;
    location: IncidentLocationInterface;
}

export interface IncidentsResponseInterface {
    incidents: IncidentInterface[];
}