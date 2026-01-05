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
import java.util.ArrayList; // Important pour initialiser la liste si besoin
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RestaurantService {

    private final HttpClient httpClient = HttpClient.newHttpClient();
    private final ObjectMapper objectMapper = new ObjectMapper();
    // Vérifie bien que c'est le bon port (7000 d'après tes logs backend)
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
        // On s'assure que la liste est bien créée pour éviter un autre crash
        if (cmd.items == null) {
            cmd.items = new ArrayList<>();
        }

        for (LignePanier l : panier) {
            Map<String, Object> item = new HashMap<>();
            // Sécurité : on vérifie que l'objet plat n'est pas null
            if (l.plat != null) {
                item.put("platId", l.plat.id);
                item.put("platNom", l.plat.nom);
                item.put("prixUnitaire", l.plat.prix);
            }
            item.put("quantite", l.quantite);
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
            // Lecture de la réponse du serveur
            Map<String, Object> respMap = objectMapper.readValue(response.body(), Map.class);

            // --- CORRECTION DU BUG "MAP.GET IS NULL" ---
            // On vérifie si "id" existe AVANT de faire toString()
            if (respMap.containsKey("id") && respMap.get("id") != null) {
                return respMap.get("id").toString();
            }
            // Sinon, on cherche si y'a un message
            else if (respMap.containsKey("message")) {
                return respMap.get("message").toString();
            }
            // Sinon, on renvoie un truc par défaut pour ne pas planter
            else {
                return "Commande validée (Sans ID)";
            }
            // -------------------------------------------

        } else {
            throw new Exception("Erreur serveur: " + response.statusCode());
        }
    }
}