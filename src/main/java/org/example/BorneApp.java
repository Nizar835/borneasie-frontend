package org.example;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;
import org.example.model.LignePanier;
import org.example.model.Plat;
import org.example.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class BorneApp extends Application {

    // On utilise le Service pour parler au backend
    private final RestaurantService service = new RestaurantService();
    private List<Plat> menuGlobal = new ArrayList<>();
    private final List<LignePanier> panier = new ArrayList<>();
    private BorderPane rootLayout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Chargement des données via le Service au démarrage
        try {
            menuGlobal = service.getMenu();
        } catch (Exception e) {
            System.err.println("Impossible de charger le menu: " + e.getMessage());
        }

        // Configuration de la fenêtre principale
        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #1e293b;");

        showEcranAccueil();

        // --- CORRECTION ECRAN ---
        // On ne met pas de taille fixe (comme 1280x800) pour éviter que ça dépasse
        Scene scene = new Scene(rootLayout);

        primaryStage.setTitle("Borne Commande Asie");
        primaryStage.setScene(scene);

        // On maximise la fenêtre pour qu'elle prenne tout l'écran disponible
        primaryStage.setMaximized(true);

        primaryStage.show();
    }

    // --- VUES (ECRANS) ---

    private void showEcranAccueil() {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Label titre = new Label("SAVEURS D'ASIE");
        titre.setFont(Font.font("Arial", 48));
        titre.setTextFill(Color.web("#d97706"));

        Button btnStart = new Button("COMMENCER LA COMMANDE");
        btnStart.setStyle("-fx-font-size: 24px; -fx-padding: 20 50; -fx-background-color: #d97706; -fx-text-fill: white; -fx-cursor: hand;");
        btnStart.setOnAction(e -> showEcranCatalogue("Entrées"));

        content.getChildren().addAll(titre, btnStart);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    private void showEcranCatalogue(String categorieSelectionnee) {
        // Barre de navigation (Haut)
        HBox nav = new HBox(15);
        nav.setPadding(new Insets(20));
        nav.setStyle("-fx-background-color: #0f172a;");
        nav.setAlignment(Pos.CENTER_LEFT);

        String[] categories = {"Entrées", "Plats", "Desserts"};
        for (String cat : categories) {
            Button btnCat = new Button(cat);
            String style = "-fx-font-size: 18px; -fx-padding: 10 20; -fx-text-fill: white; -fx-cursor: hand; ";
            // Style différent si la catégorie est active
            if (cat.equalsIgnoreCase(categorieSelectionnee)) {
                btnCat.setStyle(style + "-fx-background-color: #d97706;");
            } else {
                btnCat.setStyle(style + "-fx-background-color: #334155;");
            }
            btnCat.setOnAction(e -> showEcranCatalogue(cat));
            nav.getChildren().add(btnCat);
        }

        // Bouton Panier à droite
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnPanier = new Button("Panier (" + getPanierCount() + ")");
        btnPanier.setStyle("-fx-font-size: 18px; -fx-background-color: #22c55e; -fx-text-fill: white; -fx-cursor: hand;");
        btnPanier.setOnAction(e -> showEcranPanier());
        nav.getChildren().addAll(spacer, btnPanier);

        rootLayout.setTop(nav);

        // Grille des produits (Centre)
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e293b; -fx-border-color: #1e293b;"); // Enlever les bordures blanches du scrollpane

        FlowPane grid = new FlowPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setStyle("-fx-background-color: #1e293b;");
        grid.setAlignment(Pos.TOP_CENTER); // Centrer les cartes

        // Filtrage des plats
        List<Plat> platsFiltres = menuGlobal.stream()
                .filter(p -> p.categorie.equalsIgnoreCase(categorieSelectionnee))
                .collect(Collectors.toList());

        for (Plat p : platsFiltres) {
            grid.getChildren().add(creerCartePlat(p));
        }

        scroll.setContent(grid);
        rootLayout.setCenter(scroll);
    }

    private VBox creerCartePlat(Plat p) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #334155; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        card.setPrefWidth(250);
        card.setAlignment(Pos.CENTER_LEFT);

        Label nom = new Label(p.nom);
        nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        nom.setWrapText(true);

        Label desc = new Label(p.description);
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #cbd5e1;");

        // Espace flexible pour pousser le prix et le bouton vers le bas
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label prix = new Label(String.format("%.2f €", p.prix));
        prix.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-font-size: 16px;");

        Button btnAdd = new Button("Ajouter");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> showEcranDetail(p));

        card.getChildren().addAll(nom, desc, spacer, prix, btnAdd);
        return card;
    }

    private void showEcranDetail(Plat p) {
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #1e293b;");
        content.setPadding(new Insets(50));

        Label nom = new Label(p.nom);
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        Label desc = new Label(p.description);
        desc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 18px;");

        // Options
        ToggleGroup group = new ToggleGroup();
        RadioButton rbStandard = new RadioButton("Standard");
        rbStandard.setToggleGroup(group);
        rbStandard.setSelected(true);
        rbStandard.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        RadioButton rbSansPiment = new RadioButton("Sans piment");
        rbSansPiment.setToggleGroup(group);
        rbSansPiment.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");

        HBox options = new HBox(30, rbStandard, rbSansPiment);
        options.setAlignment(Pos.CENTER);

        // Quantité
        Label lblQte = new Label("Quantité :");
        lblQte.setStyle("-fx-text-fill: white;");
        Spinner<Integer> spinner = new Spinner<>(1, 10, 1);
        spinner.setStyle("-fx-font-size: 16px;");

        HBox qteBox = new HBox(15, lblQte, spinner);
        qteBox.setAlignment(Pos.CENTER);

        // Boutons actions
        Button btnAdd = new Button("Confirmer l'ajout");
        btnAdd.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 30; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> {
            String opt = ((RadioButton) group.getSelectedToggle()).getText();
            panier.add(new LignePanier(p, spinner.getValue(), opt));
            showEcranCatalogue(p.categorie);
        });

        Button btnBack = new Button("Annuler");
        btnBack.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 30; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showEcranCatalogue(p.categorie));

        HBox actions = new HBox(20, btnBack, btnAdd);
        actions.setAlignment(Pos.CENTER);

        content.getChildren().addAll(nom, desc, options, qteBox, actions);
        rootLayout.setCenter(content);
        rootLayout.setTop(null); // On cache la barre de navigation
    }

    private void showEcranPanier() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #1e293b;");

        Label titre = new Label("Votre Panier");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 32px;");

        ListView<String> list = new ListView<>();
        list.setStyle("-fx-control-inner-background: #334155; -fx-background-color: #334155; -fx-text-fill: white;");

        for (LignePanier l : panier) {
            list.getItems().add(l.plat.nom + " x" + l.quantite + " (" + l.options + ") - " + String.format("%.2f €", l.getTotal()));
        }

        Label total = new Label("Total: " + String.format("%.2f €", calculerTotal()));
        total.setStyle("-fx-text-fill: #d97706; -fx-font-size: 28px; -fx-font-weight: bold;");

        Button btnPay = new Button("Payer et Envoyer");
        btnPay.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 15 40; -fx-cursor: hand;");
        btnPay.setOnAction(e -> {
            if (panier.isEmpty()) {
                Alert alert = new Alert(Alert.AlertType.WARNING, "Votre panier est vide !");
                alert.show();
                return;
            }
            try {
                // Appel au service pour envoyer la commande au backend
                String id = service.envoyerCommande(panier, calculerTotal(), "Client Borne");
                panier.clear();
                showEcranConfirmation(id);
            } catch (Exception ex) {
                Alert alert = new Alert(Alert.AlertType.ERROR, "Erreur réseau: " + ex.getMessage());
                alert.show();
            }
        });

        Button btnBack = new Button("Retour au menu");
        btnBack.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 30; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showEcranCatalogue("Entrées"));

        HBox actions = new HBox(20, btnBack, btnPay);
        actions.setAlignment(Pos.CENTER);

        content.getChildren().addAll(titre, list, total, actions);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    private void showEcranConfirmation(String id) {
        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #1e293b;");

        Label msg = new Label("Merci pour votre commande !");
        msg.setStyle("-fx-text-fill: white; -fx-font-size: 32px;");

        Label numCmd = new Label("Numéro : #" + id);
        numCmd.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 48px; -fx-font-weight: bold;");

        Label info = new Label("Veuillez récupérer votre ticket.");
        info.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 18px;");

        Button btnHome = new Button("Retour Accueil");
        btnHome.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 15 40; -fx-cursor: hand;");
        btnHome.setOnAction(e -> showEcranAccueil());

        content.getChildren().addAll(msg, numCmd, info, btnHome);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    private double calculerTotal() {
        return panier.stream().mapToDouble(LignePanier::getTotal).sum();
    }

    private int getPanierCount() {
        return panier.stream().mapToInt(l -> l.quantite).sum();
    }
}