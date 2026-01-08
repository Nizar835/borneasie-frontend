package fr.isen.project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import fr.isen.project.model.Categorie;
import fr.isen.project.model.Commande;
import fr.isen.project.model.LigneCommande;
import fr.isen.project.model.Plat;
import fr.isen.project.service.RestaurantService;

import java.util.ArrayList;
import java.util.List;

public class BorneApp extends Application {

    private final RestaurantService service = new RestaurantService();
    private List<Categorie> menuCategories = new ArrayList<>();
    private final Commande commandeEnCours = new Commande();
    private BorderPane rootLayout;

    // Variables pour les fonctionnalités améliorées
    private String nomClient = "";
    private double remiseTaux = 0.0;
    private static final double TVA_TAUX = 0.10; // TVA à 10%
    private static final String BASE_URL_IMG = "http://localhost:8080/images/"; // URL du backend

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        try {
            menuCategories = service.getMenu();
        } catch (Exception e) {
            System.err.println("Erreur chargement menu: " + e.getMessage());
        }

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #1e293b;");

        showEcranAccueil();

        Scene scene = new Scene(rootLayout, 1280, 800);
        primaryStage.setTitle("Borne Saveurs d'Asie - ISEN");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // --- 1. ECRAN ACCUEIL (DEMANDE DU NOM) ---
    private void showEcranAccueil() {
        // Reset de la commande
        commandeEnCours.ligneCommande.clear();
        remiseTaux = 0.0;

        VBox content = new VBox(30);
        content.setAlignment(Pos.CENTER);

        Label titre = new Label("SAVEURS D'ASIE");
        titre.setFont(Font.font("Arial", FontWeight.BOLD, 60));
        titre.setTextFill(Color.web("#d97706"));

        TextField inputNom = new TextField();
        inputNom.setPromptText("Entrez votre prénom...");
        inputNom.setMaxWidth(400);
        inputNom.setStyle("-fx-font-size: 20px; -fx-padding: 10;");

        Button btnStart = new Button("COMMENCER");
        btnStart.setStyle("-fx-font-size: 24px; -fx-padding: 15 60; -fx-background-color: #d97706; -fx-text-fill: white; -fx-cursor: hand;");
        btnStart.setDisable(true);

        // Active le bouton seulement si un nom est saisi
        inputNom.textProperty().addListener((obs, old, val) -> btnStart.setDisable(val.trim().isEmpty()));

        btnStart.setOnAction(e -> {
            nomClient = inputNom.getText();
            commandeEnCours.nomClient = nomClient; // Champ Modelio
            if (!menuCategories.isEmpty()) showEcranCatalogue(menuCategories.get(0));
        });

        content.getChildren().addAll(titre, inputNom, btnStart);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    // --- 2. ECRAN CATALOGUE (AVEC IMAGES JPG) ---
    private void showEcranCatalogue(Categorie catActive) {
        // Navigation
        HBox nav = new HBox(15);
        nav.setPadding(new Insets(20));
        nav.setStyle("-fx-background-color: #0f172a;");
        nav.setAlignment(Pos.CENTER_LEFT);

        for (Categorie cat : menuCategories) {
            Button btnCat = new Button(cat.nom.toUpperCase());
            String style = "-fx-font-size: 18px; -fx-padding: 10 25; -fx-text-fill: white; -fx-cursor: hand; -fx-background-radius: 5; ";
            btnCat.setStyle(style + (cat == catActive ? "-fx-background-color: #d97706;" : "-fx-background-color: #334155;"));
            btnCat.setOnAction(e -> showEcranCatalogue(cat));
            nav.getChildren().add(btnCat);
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnPanier = new Button("PANIER (" + getPanierCount() + ")");
        btnPanier.setStyle("-fx-font-size: 18px; -fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-weight: bold;");
        btnPanier.setOnAction(e -> showEcranPanier(catActive));
        nav.getChildren().addAll(spacer, btnPanier);

        rootLayout.setTop(nav);

        // Grille de produits
        FlowPane grid = new FlowPane(25, 25);
        grid.setPadding(new Insets(30));
        grid.setAlignment(Pos.TOP_CENTER);
        grid.setStyle("-fx-background-color: #1e293b;");

        for (Plat p : catActive.plats) {
            grid.getChildren().add(creerCartePlat(p, catActive));
        }

        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e293b; -fx-border-color: #1e293b;");
        rootLayout.setCenter(scroll);
    }

    private VBox creerCartePlat(Plat p, Categorie catSource) {
        VBox card = new VBox(12);
        card.setPadding(new Insets(10));
        card.setStyle("-fx-background-color: #334155; -fx-background-radius: 12; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.4), 10, 0, 0, 4);");
        card.setPrefWidth(240);
        card.setAlignment(Pos.TOP_CENTER);

        // Chargement de l'image JPG depuis le backend
        ImageView iv = new ImageView();
        try {
            String imgFile = (p.image != null && !p.image.isEmpty()) ? p.image : "placeholder.jpg";
            Image img = new Image(BASE_URL_IMG + imgFile, true);
            iv.setImage(img);
            iv.setFitWidth(220);
            iv.setFitHeight(150);
            iv.setPreserveRatio(true);
        } catch (Exception e) { /* Image par défaut */ }

        Label nom = new Label(p.nom);
        nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 17px;");
        nom.setWrapText(true);

        Label prix = new Label(String.format("%.2f €", p.prix));
        prix.setStyle("-fx-text-fill: #fbbf24; -fx-font-size: 18px; -fx-font-weight: bold;");

        Button btnAdd = new Button("CHOISIR");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> showEcranDetail(p, catSource));

        card.getChildren().addAll(iv, nom, prix, btnAdd);
        return card;
    }

    // --- 3. ECRAN DETAIL (IMAGE GRANDE + OPTIONS) ---
    private void showEcranDetail(Plat p, Categorie catSource) {
        HBox content = new HBox(50);
        content.setAlignment(Pos.CENTER);
        content.setPadding(new Insets(50));
        content.setStyle("-fx-background-color: #1e293b;");

        // Grande Image
        ImageView bigIv = new ImageView(new Image(BASE_URL_IMG + p.image));
        bigIv.setFitWidth(450);
        bigIv.setPreserveRatio(true);

        VBox infos = new VBox(25);
        infos.setAlignment(Pos.CENTER_LEFT);
        infos.setMaxWidth(500);

        Label nom = new Label(p.nom);
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");

        Label desc = new Label(p.description);
        desc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 20px; -fx-font-style: italic;");
        desc.setWrapText(true);

        // Options
        ToggleGroup group = new ToggleGroup();
        RadioButton rb1 = new RadioButton("Standard"); rb1.setToggleGroup(group); rb1.setSelected(true);
        RadioButton rb2 = new RadioButton("Sans piment"); rb2.setToggleGroup(group);
        rb1.setStyle("-fx-text-fill: white;"); rb2.setStyle("-fx-text-fill: white;");
        HBox opts = new HBox(20, rb1, rb2);

        Spinner<Integer> spinner = new Spinner<>(1, 10, 1);
        spinner.setPrefWidth(100);

        Button btnAdd = new Button("AJOUTER AU PANIER - " + p.prix + "€");
        btnAdd.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 12 30;");
        btnAdd.setOnAction(e -> {
            LigneCommande l = new LigneCommande();
            l.plat = p;
            l.quantite = spinner.getValue();
            l.options = ((RadioButton)group.getSelectedToggle()).getText();
            commandeEnCours.ligneCommande.add(l);
            showEcranCatalogue(catSource);
        });

        Button btnBack = new Button("ANNULER");
        btnBack.setOnAction(e -> showEcranCatalogue(catSource));
        btnBack.setStyle("-fx-text-fill: #94a3b8; -fx-background-color: transparent; -fx-border-color: #94a3b8;");

        infos.getChildren().addAll(nom, desc, new Separator(), opts, new Label("Quantité :"), spinner, btnAdd, btnBack);
        content.getChildren().addAll(bigIv, infos);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    // --- 4. ECRAN PANIER (TICKET FINAL + TAXES + PROMO) ---
    private void showEcranPanier(Categorie catSource) {
        VBox content = new VBox(25);
        content.setPadding(new Insets(40));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #0f172a;");

        Label titre = new Label("VOTRE TICKET - " + nomClient.toUpperCase());
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 30px; -fx-font-weight: bold;");

        // Zone de texte façon ticket de caisse
        TextArea ticketArea = new TextArea();
        ticketArea.setEditable(false);
        ticketArea.setMaxWidth(600);
        ticketArea.setPrefHeight(400);
        ticketArea.setStyle("-fx-font-family: 'Courier New'; -fx-font-size: 16px;");

        // Calculs financiers
        double totalHT = calculerTotalHT();
        double montantRemise = totalHT * remiseTaux;
        double totalApresRemise = totalHT - montantRemise;
        double montantTVA = totalApresRemise * TVA_TAUX; //
        double totalTTC = totalApresRemise + montantTVA;

        StringBuilder sb = new StringBuilder();
        sb.append("      SAVEURS D'ASIE - RECAPITULATIF\n");
        sb.append("------------------------------------------\n");
        for (LigneCommande l : commandeEnCours.ligneCommande) {
            sb.append(String.format("%-25s x%d  %7.2f€\n", l.plat.nom, l.quantite, l.plat.prix * l.quantite));
            sb.append("  > ").append(l.options).append("\n");
        }
        sb.append("------------------------------------------\n");
        sb.append(String.format("SOUS-TOTAL HT :           %10.2f€\n", totalHT));
        if (remiseTaux > 0) sb.append(String.format("REMISE (%d%%) :           %10.2f€\n", (int)(remiseTaux*100), -montantRemise));
        sb.append(String.format("TVA (%d%%) :               %10.2f€\n", (int)(TVA_TAUX*100), montantTVA));
        sb.append("==========================================\n");
        sb.append(String.format("TOTAL TTC A PAYER :       %10.2f€\n", totalTTC));
        ticketArea.setText(sb.toString());

        // Zone Code Promo
        HBox promoBox = new HBox(10);
        promoBox.setAlignment(Pos.CENTER);
        TextField tfPromo = new TextField(); tfPromo.setPromptText("Code Promo ?");
        Button btnPromo = new Button("APPLIQUER");
        btnPromo.setOnAction(e -> {
            if (tfPromo.getText().equalsIgnoreCase("VIP2025")) { //
                remiseTaux = 0.20;
                showEcranPanier(catSource); // Rafraîchit le ticket
            }
        });
        promoBox.getChildren().addAll(tfPromo, btnPromo);

        Button btnPay = new Button("VALIDER ET PAYER " + String.format("%.2f€", totalTTC));
        btnPay.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-padding: 15 50;");
        btnPay.setOnAction(e -> {
            try {
                String id = service.envoyerCommande(commandeEnCours);
                showEcranConfirmation(id);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        Button btnBack = new Button("RETOUR AU MENU");
        btnBack.setOnAction(e -> showEcranCatalogue(catSource));

        content.getChildren().addAll(titre, ticketArea, promoBox, btnPay, btnBack);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    private void showEcranConfirmation(String id) {
        VBox c = new VBox(30); c.setAlignment(Pos.CENTER);
        Label m = new Label("MERCI " + nomClient.toUpperCase() + " !");
        m.setStyle("-fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        Label cmd = new Label("COMMANDE #" + id + " EN CUISINE");
        cmd.setStyle("-fx-text-fill: #22c55e; -fx-font-size: 30px;");
        Button b = new Button("RETOUR ACCUEIL");
        b.setOnAction(e -> showEcranAccueil());
        c.getChildren().addAll(m, cmd, b);
        rootLayout.setCenter(c);
    }

    private double calculerTotalHT() {
        return commandeEnCours.ligneCommande.stream().mapToDouble(l -> l.plat.prix * l.quantite).sum();
    }

    private int getPanierCount() {
        return commandeEnCours.ligneCommande.stream().mapToInt(l -> l.quantite).sum();
    }
}