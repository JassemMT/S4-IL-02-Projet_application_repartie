// src/config.ts
var CONFIG = {
  tileLayerUrl: "https://tile.openstreetmap.org/{z}/{x}/{y}.png",
  stationInformationUrl: "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_information.json",
  stationStatusUrl: "https://api.cyclocity.fr/contracts/nancy/gbfs/v2/station_status.json",
  proxyUrl: "http://localhost:8080",
  incidentsUrl: "http://localhost:8080/incidents"
};

// img/velo-meca.svg
var velo_meca_default = '<svg xmlns="http://www.w3.org/2000/svg" id="separateur-titre" viewBox="0 0 38 22"><path d="M29.85,7.78c-.8,0-1.56,.14-2.28,.38l-2.04-5.98c-.38-1.11-1.42-1.86-2.6-1.86h-1.71c-.58,0-1.05,.47-1.05,1.05s.47,1.05,1.05,1.05h1.71c.28,0,.52,.18,.61,.44l.3,.88H15l-.14-.43,.47-.06c.41-.05,.72-.4,.73-.82v-.09c0-.6-.45-1.1-1.04-1.16l-2.91-.28c-.35-.03-.7,.09-.95,.35-.25,.25-.37,.6-.33,.96l.1,.85c.02,.19,.12,.37,.27,.49,.15,.12,.35,.17,.54,.15l1-.12,.32,.98-2.25,3.76c-.83-.34-1.75-.54-2.7-.54-3.93,0-7.11,3.18-7.11,7.11s3.18,7.11,7.11,7.11c3.57,0,6.51-2.63,7.02-6.06h3c.31,0,.6-.14,.8-.37l6.37-7.53,.37,1.1c-1.78,1.29-2.94,3.38-2.94,5.75,0,3.93,3.18,7.11,7.11,7.11s7.11-3.18,7.11-7.11-3.18-7.11-7.11-7.11Zm-15.91-.59l2.2,6.65h-1c-.26-1.78-1.18-3.35-2.52-4.45l1.32-2.2Zm-2.41,4.04c.74,.69,1.27,1.59,1.49,2.61h-3.05l1.56-2.61Zm-3.41,8.67c-2.76,0-5.01-2.25-5.01-5.01s2.25-5.01,5.01-5.01c.56,0,1.1,.1,1.61,.27l-2.51,4.2c-.19,.32-.2,.73-.01,1.06,.19,.33,.54,.53,.91,.53h4.9c-.48,2.26-2.49,3.96-4.9,3.96Zm10.04-6.65l-2.45-7.41h8.72l-6.27,7.41Zm11.7,6.65c-2.76,0-5.01-2.25-5.01-5.01,0-1.43,.6-2.72,1.57-3.63,.56,1.62,1.49,3.13,2.69,4.36,.21,.21,.48,.32,.75,.32s.53-.1,.73-.3c.42-.4,.43-1.07,.02-1.48-1-1.03-1.77-2.29-2.23-3.64l-.13-.37c.5-.17,1.04-.27,1.6-.27,2.76,0,5.01,2.25,5.01,5.01s-2.25,5.01-5.01,5.01Z" fill="#ffffff"/></svg>';

// src/station.ts
function verifierReponse(response) {
  if (!response.ok) {
    throw new Error("Erreur HTTP : " + response.status);
  }
  return response;
}
function createIconWithLogo() {
  return L.divIcon({
    className: "custom-div-icon",
    html: `<div class="marker-pin"><div class="marker-dot">${velo_meca_default}</div></div>`,
    iconSize: [44, 60],
    iconAnchor: [22, 60],
    popupAnchor: [0, -45]
  });
}
function chercherDisponibilite(stationId, disponibilites) {
  for (const disponibilite of disponibilites) {
    if (disponibilite.station_id === stationId) {
      return disponibilite;
    }
  }
  return void 0;
}
function afficherStation(station, disponibilite) {
  const adresse = station.address ?? "Adresse non renseign\xE9e";
  const capaciteTotale = disponibilite.num_bikes_available + disponibilite.num_docks_available;
  const derniereMiseAJour = new Date(disponibilite.last_reported * 1e3).toLocaleString("fr-FR");
  L.marker([station.lat, station.lon], { icon: createIconWithLogo() }).addTo(map).bindPopup(`
            <strong>${station.name}</strong><br><br>
            Adresse : ${adresse}<br>
            Capacit\xE9 totale : ${capaciteTotale}<br><br>
            V\xE9los disponibles : ${disponibilite.num_bikes_available}<br>
            Places libres : ${disponibilite.num_docks_available}<br><br>
            Derni\xE8re mise \xE0 jour : ${derniereMiseAJour}
        `);
}
function afficherStations(reponses) {
  const stations = reponses[0].data.stations;
  const disponibilites = reponses[1].data.stations;
  for (const station of stations) {
    const disponibilite = chercherDisponibilite(station.station_id, disponibilites);
    if (disponibilite !== void 0) {
      afficherStation(station, disponibilite);
    }
  }
}
function afficherErreur(erreur) {
  if (erreur instanceof Error) {
    console.log(erreur.message);
  }
}
var informationsPromise = fetch(CONFIG.stationInformationUrl).then(verifierReponse).then((r) => r.json());
var disponibilitesPromise = fetch(CONFIG.stationStatusUrl).then(verifierReponse).then((r) => r.json());
Promise.all([informationsPromise, disponibilitesPromise]).then(afficherStations).catch(afficherErreur);

// img/restaurant-icon.svg
var restaurant_icon_default = '<?xml version="1.0" encoding="iso-8859-1"?>\r\n<!-- Uploaded to: SVG Repo, www.svgrepo.com, Generator: SVG Repo Mixer Tools -->\r\n<svg height="800px" width="800px" version="1.1" id="Capa_1" xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" \r\n	 viewBox="0 0 254.019 254.019" xml:space="preserve">\r\n<g>\r\n	<g>\r\n		<g>\r\n			<path style="fill:#010002;" d="M126.514,48.282c-43.428,0-78.738,35.319-78.738,78.738c0,43.389,35.309,78.718,78.738,78.718\r\n				c43.389,0,78.738-35.329,78.738-78.708C205.252,83.601,169.932,48.282,126.514,48.282z M126.514,198.898\r\n				c-39.647,0-71.879-32.232-71.879-71.869s32.222-71.928,71.879-71.928s71.879,32.29,71.879,71.928\r\n				S166.171,198.898,126.514,198.898z M193.352,127.029c0,36.882-29.926,66.808-66.828,66.808\r\n				c-36.912,0-66.838-29.936-66.838-66.808c0-36.921,29.936-66.847,66.838-66.847C163.426,60.172,193.352,90.108,193.352,127.029z\r\n				 M41.308,56.733l0.02,38.836h-0.059c-0.391,12.389-13.971,17.117-13.971,17.117v24.924h0.02l-0.02,57.312\r\n				c0,0-5.491,7.093-12.604,0v-82.226h0.166c-8.285-3.527-14.411-4.748-14.802-17.117H0V56.733l6.224-0.029L9.252,94.28H16.6\r\n				l0.928-38.348h5.989l0.723,38.348h8.617l1.983-37.547L41.308,56.733L41.308,56.733z M252.1,122.857h-15.232v77.038h-17.596\r\n				V54.144h17.596C236.878,54.134,260.844,84.548,252.1,122.857z"/>\r\n		</g>\r\n	</g>\r\n</g>\r\n</svg>';

// src/resto.ts
function createRestaurantIcon() {
  return L.divIcon({
    className: "restaurant-marker",
    html: `<div class="marker-pin"><div class="marker-dot">${restaurant_icon_default}</div></div>`,
    iconSize: [44, 60],
    iconAnchor: [22, 60],
    popupAnchor: [0, -45]
  });
}
function verifierReponse2(response) {
  if (!response.ok) {
    throw new Error("Erreur HTTP : " + response.status);
  }
  return response;
}
function afficherRestaurant(restaurant) {
  const popupContent = document.createElement("div");
  popupContent.innerHTML = `
        <div style="font-family: Arial, sans-serif;">
            <strong style="color: #333; font-size: 14px;">${restaurant.nom}</strong><br>
            <span style="font-size: 12px; color: #666;">${restaurant.adresse}</span>
            <hr style="border: 0; border-top: 1px solid #ccc; margin: 10px 0;">
            <form id="form-resa-${restaurant.id}" style="display: flex; flex-direction: column; gap: 5px;">
                <input type="text" name="nom" placeholder="Nom" required style="padding: 4px; font-size: 12px;">
                <input type="text" name="prenom" placeholder="Pr\xE9nom" required style="padding: 4px; font-size: 12px;">
                <input type="tel" name="telephone" placeholder="T\xE9l\xE9phone" required style="padding: 4px; font-size: 12px;">
                <input type="number" name="convives" placeholder="Nb convives" min="1" max="20" required style="padding: 4px; font-size: 12px;">
                <div style="display: flex; gap: 5px;">
                    <input type="date" name="date" required style="padding: 4px; font-size: 12px; flex: 1;">
                    <input type="time" name="heure" required style="padding: 4px; font-size: 12px; width: 80px;">
                </div>
                <button type="submit" style="margin-top: 5px; padding: 6px; background-color: #277d4a; color: white; border: none; border-radius: 4px; cursor: pointer; font-size: 12px;">R\xE9server</button>
                <div id="msg-resa-${restaurant.id}" style="font-size: 12px; text-align: center; margin-top: 5px; min-height: 15px;"></div>
            </form>
        </div>
    `;
  popupContent.querySelector("form")?.addEventListener("submit", (e) => {
    e.preventDefault();
    const form = e.target;
    const msgDiv = popupContent.querySelector(`#msg-resa-${restaurant.id}`);
    const btn = form.querySelector("button");
    msgDiv.textContent = "R\xE9servation en cours...";
    msgDiv.style.color = "#333";
    btn.disabled = true;
    const payload = {
      idRestaurant: restaurant.id,
      nom: form.elements.namedItem("nom").value,
      prenom: form.elements.namedItem("prenom").value,
      telephone: form.elements.namedItem("telephone").value,
      convives: parseInt(form.elements.namedItem("convives").value, 10),
      date: form.elements.namedItem("date").value,
      heure: form.elements.namedItem("heure").value
    };
    fetch(`${CONFIG.proxyUrl}/reserver`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(payload)
    }).then(verifierReponse2).then((r) => r.json()).then((res) => {
      if (res.status === "ok") {
        msgDiv.textContent = "R\xE9servation confirm\xE9e !";
        msgDiv.style.color = "green";
        form.reset();
      } else {
        msgDiv.textContent = res.message || "Erreur serveur";
        msgDiv.style.color = "red";
      }
      btn.disabled = false;
    }).catch((err) => {
      msgDiv.textContent = "Connexion au proxy \xE9chou\xE9e";
      msgDiv.style.color = "red";
      btn.disabled = false;
    });
  });
  L.marker([restaurant.lat, restaurant.lng], { icon: createRestaurantIcon() }).addTo(map).bindPopup(popupContent);
}
function afficherRestaurants(restaurants) {
  for (const restaurant of restaurants) {
    afficherRestaurant(restaurant);
  }
  console.log(`${restaurants.length} restaurants charg\xE9s.`);
}
fetch(`${CONFIG.proxyUrl}/restaurants`).then(verifierReponse2).then((r) => r.json()).then(afficherRestaurants).catch((e) => console.error("Impossible de charger les restaurants :", e));

// src/incidents.ts
function verifierReponse3(response) {
  if (!response.ok) {
    throw new Error("Erreur HTTP : " + response.status);
  }
  return response;
}
function createIncidentIcon() {
  return L.divIcon({
    className: "incident-marker",
    html: `<div class="marker-pin"><div class="marker-dot">\u26A0</div></div>`,
    iconSize: [44, 60],
    iconAnchor: [22, 60],
    popupAnchor: [0, -45]
  });
}
function afficherIncident(incident) {
  const coords = incident.location.polyline.split(" ");
  const lat = parseFloat(coords[0]);
  const lng = parseFloat(coords[1]);
  const dateDebut = new Date(incident.starttime).toLocaleDateString("fr-FR");
  const dateFin = new Date(incident.endtime).toLocaleDateString("fr-FR");
  L.marker([lat, lng], { icon: createIncidentIcon() }).addTo(map).bindPopup(`
            <strong>${incident.short_description}</strong><br><br>
            ${incident.location.location_description}<br><br>
            ${incident.description}<br><br>
            Du ${dateDebut} au ${dateFin}
        `);
}
function afficherIncidents(data) {
  for (const incident of data.incidents) {
    afficherIncident(incident);
  }
  console.log(`${data.incidents.length} incidents charg\xE9s.`);
}
function afficherErreur2(erreur) {
  if (erreur instanceof Error) {
    console.error("Impossible de charger les incidents :", erreur.message);
  }
}
fetch(`${CONFIG.proxyUrl}/incidents`).then(verifierReponse3).then((r) => r.json()).then(afficherIncidents).catch(afficherErreur2);

// src/index.ts
var map = L.map("map").setView(
  [48.6921, 6.1844],
  13
);
L.tileLayer(CONFIG.tileLayerUrl, {
  maxZoom: 19,
  attribution: '&copy; <a href="https://www.openstreetmap.org/copyright">OpenStreetMap</a>'
}).addTo(map);
export {
  map
};
