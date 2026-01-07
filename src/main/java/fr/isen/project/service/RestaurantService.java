package fr.isen.project.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.annotation.JsonAutoDetect.Visibility;
import fr.isen.project.model.Categorie;
import fr.isen.project.model.Commande;
import fr.isen.project.model.Plat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

public class RestaurantService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    // Connexion au nouveau backend sur le port 8080
    private final String API_URL = "http://localhost:8080";

    public RestaurantService() {
        // Configuration indispensable pour lire tes champs publics Modelio
        objectMapper.setVisibility(PropertyAccessor.FIELD, Visibility.ANY);
    }

    // Récupère le menu (Catégories + Plats)
    public List<Categorie> getMenu() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/categories"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<List<Categorie>>(){});
    }

    // Envoie l'objet Commande Modelio directement au format JSON
    public String envoyerCommande(Commande cmd) throws Exception {
        String jsonBody = objectMapper.writeValueAsString(cmd);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/commande"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                .build();

        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() == 201) {
            Map<String, Object> respMap = objectMapper.readValue(response.body(), Map.class);
            return respMap.get("id").toString();
        } else {
            throw new Exception("Erreur serveur: " + response.statusCode());
        }
    }
}