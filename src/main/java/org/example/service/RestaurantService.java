package org.example.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.model.CommandeDTO;
import org.example.model.LignePanier;
import org.example.model.Plat;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final String API_URL = "http://localhost:7000";

    public List<Plat> getMenu() throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL + "/menu"))
                .GET()
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        return objectMapper.readValue(response.body(), new TypeReference<List<Plat>>(){});
    }

    public String envoyerCommande(List<LignePanier> panier, double total, String clientNom) throws Exception {
        CommandeDTO cmd = new CommandeDTO();
        cmd.total = total;
        cmd.clientNom = clientNom;

        for (LignePanier l : panier) {
            Map<String, Object> item = new HashMap<>();
            // CORRECTION ICI : on accède à l'ID via l'objet plat
            item.put("platId", l.plat.id);
            item.put("platNom", l.plat.nom);
            item.put("quantite", l.quantite);
            item.put("prixUnitaire", l.plat.prix);
            item.put("options", l.options);
            cmd.items.add(item);
        }

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