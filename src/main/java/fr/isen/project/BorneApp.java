package fr.isen.project;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.stage.Stage;

// Importation de tes modèles Modelio et du service ISEN
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
    private final Commande commandeEnCours = new Commande(); // Utilise le modèle Commande de Modelio
    private BorderPane rootLayout;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        // Chargement des catégories et plats via le Service (Port 8080)
        try {
            menuCategories = service.getMenu();
        } catch (Exception e) {
            System.err.println("Impossible de charger le menu: " + e.getMessage());
        }

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: #1e293b;");

        showEcranAccueil();

        Scene scene = new Scene(rootLayout);
        primaryStage.setTitle("Borne Commande Asie - ISEN");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true); // Plein écran pour le rendu borne
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

        // Navigation vers la première catégorie récupérée du serveur
        btnStart.setOnAction(e -> {
            if (!menuCategories.isEmpty()) {
                showEcranCatalogue(menuCategories.get(0));
            }
        });

        content.getChildren().addAll(titre, btnStart);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    private void showEcranCatalogue(Categorie catActive) {
        // Barre de navigation dynamique basée sur tes catégories Modelio
        HBox nav = new HBox(15);
        nav.setPadding(new Insets(20));
        nav.setStyle("-fx-background-color: #0f172a;");
        nav.setAlignment(Pos.CENTER_LEFT);

        for (Categorie cat : menuCategories) {
            Button btnCat = new Button(cat.nom); // Champ public 'nom'
            String style = "-fx-font-size: 18px; -fx-padding: 10 20; -fx-text-fill: white; -fx-cursor: hand; ";

            if (cat == catActive) {
                btnCat.setStyle(style + "-fx-background-color: #d97706;");
            } else {
                btnCat.setStyle(style + "-fx-background-color: #334155;");
            }
            btnCat.setOnAction(e -> showEcranCatalogue(cat));
            nav.getChildren().add(btnCat);
        }

        // Bouton Panier
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnPanier = new Button("Panier (" + getPanierCount() + ")");
        btnPanier.setStyle("-fx-font-size: 18px; -fx-background-color: #22c55e; -fx-text-fill: white; -fx-cursor: hand;");
        btnPanier.setOnAction(e -> showEcranPanier(catActive));
        nav.getChildren().addAll(spacer, btnPanier);

        rootLayout.setTop(nav);

        // Grille des produits
        ScrollPane scroll = new ScrollPane();
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background: #1e293b; -fx-border-color: #1e293b;");

        FlowPane grid = new FlowPane();
        grid.setPadding(new Insets(20));
        grid.setHgap(20);
        grid.setVgap(20);
        grid.setStyle("-fx-background-color: #1e293b;");
        grid.setAlignment(Pos.TOP_CENTER);

        // Affiche les plats de la catégorie courante
        for (Plat p : catActive.plats) {
            grid.getChildren().add(creerCartePlat(p, catActive));
        }

        scroll.setContent(grid);
        rootLayout.setCenter(scroll);
    }

    private VBox creerCartePlat(Plat p, Categorie catSource) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setStyle("-fx-background-color: #334155; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 10, 0, 0, 0);");
        card.setPrefWidth(250);
        card.setAlignment(Pos.CENTER_LEFT);

        Label nom = new Label(p.nom); // Accès direct au champ public
        nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
        nom.setWrapText(true);

        Label desc = new Label(p.description);
        desc.setWrapText(true);
        desc.setStyle("-fx-text-fill: #cbd5e1;");

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        Label prix = new Label(String.format("%.2f €", p.prix)); // Champ public float
        prix.setStyle("-fx-text-fill: #d97706; -fx-font-weight: bold; -fx-font-size: 16px;");

        Button btnAdd = new Button("Ajouter");
        btnAdd.setMaxWidth(Double.MAX_VALUE);
        btnAdd.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-cursor: hand;");
        btnAdd.setOnAction(e -> showEcranDetail(p, catSource));

        card.getChildren().addAll(nom, desc, spacer, prix, btnAdd);
        return card;
    }

    private void showEcranDetail(Plat p, Categorie catSource) {
        VBox content = new VBox(25);
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #1e293b;");
        content.setPadding(new Insets(50));

        Label nom = new Label(p.nom);
        nom.setStyle("-fx-text-fill: white; -fx-font-size: 32px; -fx-font-weight: bold;");

        Label desc = new Label(p.description);
        desc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 18px;");

        // Options (LigneCommande)
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

            // Création d'une LigneCommande Modelio
            LigneCommande ligne = new LigneCommande();
            ligne.plat = p;
            ligne.quantite = spinner.getValue();
            ligne.options = opt;

            // Ajout à la liste générée par Modelio
            commandeEnCours.ligneCommande.add(ligne);
            showEcranCatalogue(catSource);
        });

        Button btnBack = new Button("Annuler");
        btnBack.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 30; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showEcranCatalogue(catSource));

        HBox actions = new HBox(20, btnBack, btnAdd);
        actions.setAlignment(Pos.CENTER);

        content.getChildren().addAll(nom, desc, options, qteBox, actions);
        rootLayout.setCenter(content);
        rootLayout.setTop(null);
    }

    private void showEcranPanier(Categorie catSource) {
        VBox content = new VBox(20);
        content.setPadding(new Insets(30));
        content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: #1e293b;");

        Label titre = new Label("Votre Panier");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 32px;");

        ListView<String> list = new ListView<>();
        list.setStyle("-fx-control-inner-background: #334155; -fx-background-color: #334155; -fx-text-fill: white;");

        for (LigneCommande l : commandeEnCours.ligneCommande) {
            double totalLigne = l.plat.prix * l.quantite;
            list.getItems().add(l.plat.nom + " x" + l.quantite + " (" + l.options + ") - " + String.format("%.2f €", totalLigne));
        }

        Label totalLabel = new Label("Total: " + String.format("%.2f €", calculerTotal()));
        totalLabel.setStyle("-fx-text-fill: #d97706; -fx-font-size: 28px; -fx-font-weight: bold;");

        Button btnPay = new Button("Payer et Envoyer");
        btnPay.setStyle("-fx-background-color: #22c55e; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 15 40; -fx-cursor: hand;");
        btnPay.setOnAction(e -> {
            if (commandeEnCours.ligneCommande.isEmpty()) {
                new Alert(Alert.AlertType.WARNING, "Votre panier est vide !").show();
                return;
            }
            try {
                commandeEnCours.nomClient = "Borne ISEN";
                String id = service.envoyerCommande(commandeEnCours);
                commandeEnCours.ligneCommande.clear();
                showEcranConfirmation(id);
            } catch (Exception ex) {
                new Alert(Alert.AlertType.ERROR, "Erreur réseau: " + ex.getMessage()).show();
            }
        });

        Button btnBack = new Button("Retour au menu");
        btnBack.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 10 30; -fx-cursor: hand;");
        btnBack.setOnAction(e -> showEcranCatalogue(catSource));

        HBox actions = new HBox(20, btnBack, btnPay);
        actions.setAlignment(Pos.CENTER);

        content.getChildren().addAll(titre, list, totalLabel, actions);
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

        Button btnHome = new Button("Retour Accueil");
        btnHome.setStyle("-fx-background-color: #d97706; -fx-text-fill: white; -fx-font-size: 20px; -fx-padding: 15 40; -fx-cursor: hand;");
        btnHome.setOnAction(e -> showEcranAccueil());

        content.getChildren().addAll(msg, numCmd, btnHome);
        rootLayout.setCenter(content);
    }

    private double calculerTotal() {
        return commandeEnCours.ligneCommande.stream()
                .mapToDouble(l -> l.plat.prix * l.quantite)
                .sum();
    }

    private int getPanierCount() {
        return commandeEnCours.ligneCommande.stream()
                .mapToInt(l -> l.quantite)
                .sum();
    }
}