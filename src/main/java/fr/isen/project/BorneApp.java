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

    private String nomClient = "";
    private String choixCouverts = "Baguettes";

    // --- VARIABLES FINANCIÈRES ---
    private double remiseTaux = 0.0;
    private String codePromoActif = "";
    private static final double TVA_TAUX = 0.15; // TVA 15%

    private static final String BASE_URL_IMG = "http://localhost:8080/images/";

    // Couleurs
    private static final String COL_BG = "#1e293b";
    private static final String COL_NAV = "#0f172a";
    private static final String COL_CARD = "#334155";
    private static final String COL_ACCENT = "#d97706";
    private static final String COL_GREEN = "#22c55e";
    private static final String COL_QR = "#3b82f6";

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        try { menuCategories = service.getMenu(); } catch (Exception e) { e.printStackTrace(); }

        rootLayout = new BorderPane();
        rootLayout.setStyle("-fx-background-color: " + COL_BG + ";");

        showEcranAccueil();

        Scene scene = new Scene(rootLayout, 1280, 800);
        primaryStage.setTitle("Borne Saveurs d'Asie - ISEN");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    // 1. ACCUEIL
    private void showEcranAccueil() {
        rootLayout.setTop(null);
        commandeEnCours.ligneCommande.clear();
        remiseTaux = 0.0;
        codePromoActif = "";
        nomClient = "";

        VBox content = new VBox(40); content.setAlignment(Pos.CENTER);

        Label titre = new Label("SAVEURS D'ASIE");
        titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 64));
        titre.setTextFill(Color.web(COL_ACCENT));
        titre.setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5);");

        TextField inputNom = new TextField();
        inputNom.setPromptText("Votre prénom...");
        inputNom.setMaxWidth(400);
        inputNom.setStyle("-fx-font-size: 22px; -fx-padding: 15; -fx-background-radius: 10;");
        inputNom.setAlignment(Pos.CENTER);

        Button btnStart = new Button("COMMENCER");
        btnStart.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white; -fx-background-color: " + COL_ACCENT + "; -fx-padding: 15 50; -fx-background-radius: 10; -fx-cursor: hand;");
        btnStart.setDisable(true);

        inputNom.textProperty().addListener((obs, old, val) -> btnStart.setDisable(val.trim().isEmpty()));

        btnStart.setOnAction(e -> {
            nomClient = inputNom.getText();
            commandeEnCours.nomClient = nomClient;
            if (!menuCategories.isEmpty()) showEcranCatalogue(menuCategories.get(0));
        });

        content.getChildren().addAll(titre, inputNom, btnStart);
        rootLayout.setCenter(content);
    }

    // 2. CATALOGUE
    private void showEcranCatalogue(Categorie catActive) {
        HBox nav = new HBox(15); nav.setPadding(new Insets(20)); nav.setStyle("-fx-background-color: " + COL_NAV + ";");
        nav.setAlignment(Pos.CENTER_LEFT);

        for (Categorie cat : menuCategories) {
            Button btnCat = new Button(cat.nom.toUpperCase());
            boolean isActive = (cat == catActive);
            btnCat.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 30; -fx-cursor: hand; -fx-text-fill: white; -fx-background-color: " + (isActive ? COL_ACCENT : COL_CARD));
            btnCat.setOnAction(e -> showEcranCatalogue(cat));
            nav.getChildren().add(btnCat);
        }

        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnPanier = new Button("PANIER (" + getPanierCount() + ")");
        btnPanier.setStyle("-fx-font-size: 18px; -fx-background-color: " + COL_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
        btnPanier.setOnAction(e -> showEcranPanier(catActive));
        nav.getChildren().addAll(spacer, btnPanier);

        rootLayout.setTop(nav);

        FlowPane grid = new FlowPane(30, 30); grid.setPadding(new Insets(40)); grid.setAlignment(Pos.TOP_CENTER);
        grid.setStyle("-fx-background-color: " + COL_BG + ";");

        for (Plat p : catActive.plats) {
            VBox card = new VBox(15);
            card.setPadding(new Insets(15));
            card.setStyle("-fx-background-color: " + COL_CARD + "; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.3), 10, 0, 0, 2);");
            card.setPrefWidth(260); card.setAlignment(Pos.TOP_CENTER);

            ImageView iv = new ImageView();
            iv.setFitWidth(230); iv.setFitHeight(160); iv.setPreserveRatio(true);
            if (p.image != null && !p.image.isEmpty()) {
                try { iv.setImage(new Image(BASE_URL_IMG + p.image, true)); } catch (Exception e) {}
            }

            Label nom = new Label(p.nom);
            nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
            nom.setWrapText(true); nom.setAlignment(Pos.CENTER);

            Label prix = new Label(String.format("%.2f €", p.prix));
            prix.setStyle("-fx-text-fill: " + COL_ACCENT + "; -fx-font-size: 20px; -fx-font-weight: bold;");

            Button btnAdd = new Button("CHOISIR");
            btnAdd.setMaxWidth(Double.MAX_VALUE);
            btnAdd.setStyle("-fx-background-color: " + COL_ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8;");
            btnAdd.setOnAction(e -> showEcranDetail(p, catActive));

            card.getChildren().addAll(iv, nom, new Region(), prix, btnAdd);
            grid.getChildren().add(card);
        }

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + COL_BG + "; -fx-border-color: " + COL_BG + ";");
        sp.getStyleClass().add("edge-to-edge");
        sp.setPannable(true);
        rootLayout.setCenter(sp);
    }

    // 3. DÉTAIL DU PLAT
    private void showEcranDetail(Plat p, Categorie catSource) {
        rootLayout.setTop(null);

        HBox container = new HBox(60); container.setAlignment(Pos.CENTER); container.setPadding(new Insets(60));
        container.setStyle("-fx-background-color: " + COL_BG + ";");

        ImageView bigIv = new ImageView();
        if (p.image != null) bigIv.setImage(new Image(BASE_URL_IMG + p.image, true));
        bigIv.setFitWidth(500); bigIv.setPreserveRatio(true);
        bigIv.setStyle("-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 20, 0, 0, 0);");

        VBox details = new VBox(25); details.setAlignment(Pos.CENTER_LEFT); details.setMaxWidth(500);

        Label nom = new Label(p.nom); nom.setStyle("-fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");
        Label desc = new Label(p.description); desc.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 20px; -fx-font-style: italic;");
        Label prix = new Label(p.prix + " €"); prix.setStyle("-fx-text-fill: " + COL_ACCENT + "; -fx-font-size: 36px; -fx-font-weight: bold;");

        VBox optionsBox = new VBox(15);
        Label lblOpt = new Label("Personnalisation :");
        lblOpt.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-underline: true;");

        ToggleGroup group = new ToggleGroup();
        String nomLower = p.nom.toLowerCase();

        if (nomLower.contains("nems") || nomLower.contains("rouleaux")) {
            optionsBox.getChildren().addAll(lblOpt,
                    createRadio("Sauce Nuoc-mâm", group, true),
                    createRadio("Sauce Aigre-douce", group, false),
                    createRadio("Sauce Soja", group, false)
            );
        } else if (nomLower.contains("bobun") || nomLower.contains("bo bun")) {
            optionsBox.getChildren().addAll(lblOpt,
                    createRadio("Sans Oignons", group, false),
                    createRadio("Sans Cacahuètes", group, false),
                    createRadio("Complet (Standard)", group, true)
            );
        } else if (nomLower.contains("riz")) {
            optionsBox.getChildren().addAll(lblOpt,
                    createRadio("Standard", group, true),
                    createRadio("Sans œuf", group, false)
            );
        } else if (nomLower.contains("nougat")) {
            optionsBox.getChildren().addAll(lblOpt,
                    createRadio("Tendre", group, true),
                    createRadio("Dur", group, false)
            );
        } else if (nomLower.contains("coco") || nomLower.contains("perle")) {
            optionsBox.getChildren().addAll(lblOpt,
                    createRadio("Chaudes (Vapeur)", group, true),
                    createRadio("Froides", group, false)
            );
        } else if (catSource.nom.equalsIgnoreCase("Desserts")) {
            optionsBox.getChildren().addAll(lblOpt,
                    createRadio("Nature", group, true),
                    createRadio("Chantilly", group, false),
                    createRadio("Coulis Chocolat", group, false)
            );
        } else if (catSource.nom.equalsIgnoreCase("Plats")) {
            Label lblAcc = new Label("Accompagnement :");
            lblAcc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 16px;");

            optionsBox.getChildren().addAll(lblOpt, lblAcc,
                    createRadio("Riz Blanc", group, true),
                    createRadio("Nouilles Sautées", group, false),
                    createRadio("Riz Cantonais (+2€)", group, false)
            );
            CheckBox cbPiment = new CheckBox("🌶️ Extra Piment");
            cbPiment.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 16px;");
            optionsBox.getChildren().add(cbPiment);
        } else {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Standard", group, true));
        }

        HBox qteBox = new HBox(15); qteBox.setAlignment(Pos.CENTER_LEFT);
        Label lblQte = new Label("Quantité :"); lblQte.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        Spinner<Integer> spinner = new Spinner<>(1, 20, 1); spinner.setStyle("-fx-font-size: 16px;");
        qteBox.getChildren().addAll(lblQte, spinner);

        HBox actions = new HBox(20);
        Button btnAdd = new Button("AJOUTER");
        btnAdd.setStyle("-fx-background-color: " + COL_GREEN + "; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15 40; -fx-background-radius: 10; -fx-cursor: hand;");
        Button btnCancel = new Button("ANNULER");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-font-size: 20px; -fx-padding: 14 30; -fx-border-radius: 10; -fx-cursor: hand;");

        btnAdd.setOnAction(e -> {
            LigneCommande ligne = new LigneCommande();
            ligne.plat = p;
            ligne.quantite = spinner.getValue();

            StringBuilder optBuild = new StringBuilder();
            if (group.getSelectedToggle() != null) optBuild.append(((RadioButton) group.getSelectedToggle()).getText());
            else optBuild.append("Standard");

            if (optionsBox.getChildren().size() > 0 && optionsBox.getChildren().get(optionsBox.getChildren().size()-1) instanceof CheckBox) {
                CheckBox cb = (CheckBox) optionsBox.getChildren().get(optionsBox.getChildren().size()-1);
                if (cb.isSelected()) optBuild.append(" + Piment");
            }

            ligne.options = optBuild.toString();
            commandeEnCours.ligneCommande.add(ligne);
            showEcranCatalogue(catSource);
        });

        btnCancel.setOnAction(e -> showEcranCatalogue(catSource));
        actions.getChildren().addAll(btnCancel, btnAdd);
        details.getChildren().addAll(nom, desc, prix, new Separator(), optionsBox, qteBox, actions);
        container.getChildren().addAll(bigIv, details);
        rootLayout.setCenter(container);
    }

    private RadioButton createRadio(String text, ToggleGroup group, boolean selected) {
        RadioButton rb = new RadioButton(text);
        rb.setToggleGroup(group); rb.setSelected(selected);
        rb.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;");
        return rb;
    }

    // 4. PANIER
    private void showEcranPanier(Categorie catSource) {
        rootLayout.setTop(null);

        VBox content = new VBox(25); content.setPadding(new Insets(30)); content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: " + COL_BG + ";");

        Label titre = new Label("VOTRE COMMANDE");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        TextArea ticket = new TextArea();
        ticket.setEditable(false); ticket.setMaxWidth(600); ticket.setPrefHeight(400);
        ticket.setStyle("-fx-control-inner-background: #fffbeb; -fx-font-family: 'Courier New'; -fx-font-size: 14px; -fx-text-fill: #333;");

        double totalHT = 0.0;
        for (LigneCommande l : commandeEnCours.ligneCommande) {
            double prixU = l.plat.prix;
            if (l.options != null && l.options.contains("Cantonais")) prixU += 2.0;
            totalHT += prixU * l.quantite;
        }

        double montantRemise = totalHT * remiseTaux;
        double netHT = totalHT - montantRemise;
        double tva = netHT * TVA_TAUX;
        double totalTTC = netHT + tva;

        StringBuilder sb = new StringBuilder();
        sb.append("******************************************\n");
        sb.append("             SAVEURS D'ASIE               \n");
        sb.append("******************************************\n");
        sb.append("Client : ").append(nomClient.toUpperCase()).append("\n");
        sb.append("Ustensiles : ").append(choixCouverts.toUpperCase()).append("\n");
        sb.append("------------------------------------------\n");

        for (LigneCommande l : commandeEnCours.ligneCommande) {
            double prixLigne = l.plat.prix;
            if (l.options != null && l.options.contains("Cantonais")) prixLigne += 2.0;

            String n = l.plat.nom.length() > 25 ? l.plat.nom.substring(0, 22) + "..." : l.plat.nom;
            sb.append(String.format("%-25s x%2d   %7.2f €\n", n, l.quantite, prixLigne * l.quantite));
            if (!l.options.equals("Standard")) sb.append("  > ").append(l.options).append("\n");
        }
        sb.append("------------------------------------------\n");
        if (remiseTaux > 0) {
            sb.append(String.format("PROMO (%s %2.0f%%) : -%6.2f €\n", codePromoActif, remiseTaux*100, montantRemise));
        }
        sb.append(String.format("TVA (%2.0f%%) :                %6.2f €\n", TVA_TAUX*100, tva));
        sb.append(String.format("NET À PAYER (TTC) :        %6.2f €\n", totalTTC));
        ticket.setText(sb.toString());

        // Ustensiles
        HBox ustensilesBox = new HBox(20); ustensilesBox.setAlignment(Pos.CENTER);
        Label lblUst = new Label("Vos ustensiles :"); lblUst.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        ToggleGroup tgUst = new ToggleGroup();
        RadioButton rb1 = new RadioButton("Baguettes"); rb1.setToggleGroup(tgUst); rb1.setStyle("-fx-text-fill: " + COL_ACCENT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        RadioButton rb2 = new RadioButton("Couverts"); rb2.setToggleGroup(tgUst); rb2.setStyle("-fx-text-fill: " + COL_ACCENT + "; -fx-font-size: 16px; -fx-font-weight: bold;");
        if (choixCouverts.equals("Baguettes")) rb1.setSelected(true); else rb2.setSelected(true);
        tgUst.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) { choixCouverts = ((RadioButton) newVal).getText(); showEcranPanier(catSource); }
        });
        ustensilesBox.getChildren().addAll(lblUst, rb1, rb2);

        // Promo
        VBox promoGlobalBox = new VBox(15); promoGlobalBox.setAlignment(Pos.CENTER);

        HBox manualPromo = new HBox(15); manualPromo.setAlignment(Pos.CENTER);
        TextField tfCode = new TextField(); tfCode.setPromptText("Code Promo (ex: BIENVENUE)");
        Button btnCode = new Button("Appliquer Code");
        btnCode.setStyle("-fx-background-color: #64748b; -fx-text-fill: white;");
        btnCode.setOnAction(e -> appliquerPromo(tfCode.getText(), catSource));
        manualPromo.getChildren().addAll(tfCode, btnCode);

        Button btnQR = new Button("📱 Scanner QR Code Client");
        btnQR.setStyle("-fx-background-color: " + COL_QR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
        btnQR.setOnAction(e -> {
            Alert loading = new Alert(Alert.AlertType.INFORMATION);
            loading.setTitle("Scan en cours"); loading.setHeaderText("Lecture du QR Code..."); loading.setContentText("Veuillez patienter."); loading.show();
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ex) {}
                javafx.application.Platform.runLater(() -> {
                    loading.close();
                    appliquerPromo("QR_FLASH", catSource);
                    new Alert(Alert.AlertType.INFORMATION, "QR Code lu avec succès !").show();
                });
            }).start();
        });

        promoGlobalBox.getChildren().addAll(manualPromo, new Label("- OU -"), btnQR);

        HBox actions = new HBox(25); actions.setAlignment(Pos.CENTER);
        Button btnBack = new Button("CONTINUER MES ACHATS");
        btnBack.setOnAction(e -> showEcranCatalogue(catSource));
        btnBack.setStyle("-fx-background-color: #64748b; -fx-text-fill: white; -fx-font-size: 18px; -fx-padding: 15 30; -fx-background-radius: 8;");

        Button btnPay = new Button("PAYER " + String.format("%.2f €", totalTTC));
        btnPay.setStyle("-fx-background-color: " + COL_GREEN + "; -fx-text-fill: white; -fx-font-size: 22px; -fx-font-weight: bold; -fx-padding: 15 50; -fx-background-radius: 8;");

        btnPay.setOnAction(e -> {
            if (commandeEnCours.ligneCommande.isEmpty()) return;
            try {
                commandeEnCours.nomClient = nomClient + " [" + choixCouverts + "]";
                String id = service.envoyerCommande(commandeEnCours);
                showEcranConfirmation(id);
            } catch (Exception ex) { ex.printStackTrace(); }
        });

        actions.getChildren().addAll(btnBack, btnPay);
        content.getChildren().addAll(titre, ticket, ustensilesBox, new Separator(), promoGlobalBox, new Separator(), actions);
        rootLayout.setCenter(content);
    }

    private void appliquerPromo(String code, Categorie catSource) {
        if (code == null || code.trim().isEmpty()) return;
        double taux = 0.0;
        switch(code.toUpperCase()) {
            case "VIP2025": taux = 0.20; break;
            case "BIENVENUE": taux = 0.10; break;
            case "ISEN_STUDENT": taux = 0.15; break;
            case "QR_FLASH": taux = 0.05; break;
            default: taux = 0.0;
        }
        if (taux > 0) {
            this.remiseTaux = taux; this.codePromoActif = code.toUpperCase(); showEcranPanier(catSource);
        } else {
            new Alert(Alert.AlertType.WARNING, "Code inconnu ou expiré").show();
        }
    }

    // 5. CONFIRMATION AVEC RECAPITULATIF ET TOTAL
    private void showEcranConfirmation(String id) {
        rootLayout.setTop(null);
        VBox c = new VBox(25); c.setAlignment(Pos.CENTER); c.setStyle("-fx-background-color: " + COL_BG + ";");
        c.setPadding(new Insets(30));

        Label m = new Label("MERCI " + nomClient.toUpperCase() + " !");
        m.setStyle("-fx-text-fill: white; -fx-font-size: 40px; -fx-font-weight: bold;");

        Label info = new Label("Cuisine informée : " + choixCouverts);
        info.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 20px;");

        Label num = new Label("COMMANDE N° " + id);
        num.setStyle("-fx-text-fill: " + COL_GREEN + "; -fx-font-size: 50px; -fx-font-weight: bold; -fx-border-color: " + COL_GREEN + "; -fx-border-width: 4; -fx-padding: 15; -fx-border-radius: 15;");

        // --- ZONE RECAPITULATIF ---
        VBox recapBox = new VBox(10);
        recapBox.setAlignment(Pos.CENTER);
        recapBox.setPadding(new Insets(20));
        recapBox.setStyle("-fx-background-color: " + COL_CARD + "; -fx-background-radius: 10;");
        recapBox.setMaxWidth(600);

        Label lblRecap = new Label("RÉCAPITULATIF :");
        lblRecap.setStyle("-fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-font-size: 18px;");
        recapBox.getChildren().add(lblRecap);

        // Recalcul du total pour affichage
        double totalHT = 0.0;
        for (LigneCommande l : commandeEnCours.ligneCommande) {
            double p = l.plat.prix;
            if (l.options != null && l.options.contains("Cantonais")) p += 2.0;
            totalHT += p * l.quantite;

            // Affichage ligne
            String txt = l.quantite + "x " + l.plat.nom;
            if (!l.options.equals("Standard")) txt += " (" + l.options + ")";
            Label lLine = new Label(txt);
            lLine.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            recapBox.getChildren().add(lLine);
        }

        // Calcul Final
        double totalTTC = (totalHT * (1 - remiseTaux)) * (1 + TVA_TAUX);

        // Affichage Total
        Label lblTotal = new Label("TOTAL RÉGLÉ : " + String.format("%.2f €", totalTTC));
        lblTotal.setStyle("-fx-text-fill: " + COL_GREEN + "; -fx-font-weight: bold; -fx-font-size: 24px; -fx-padding: 10 0 0 0;");
        recapBox.getChildren().addAll(new Separator(), lblTotal);
        // --------------------------

        Button b = new Button("NOUVELLE COMMANDE");
        b.setStyle("-fx-background-color: " + COL_ACCENT + "; -fx-text-fill: white; -fx-font-size: 24px; -fx-padding: 15 50; -fx-background-radius: 10;");
        b.setOnAction(e -> showEcranAccueil());

        c.getChildren().addAll(m, info, num, recapBox, b);
        rootLayout.setCenter(c);
    }

    private int getPanierCount() {
        return commandeEnCours.ligneCommande.stream().mapToInt(l -> l.quantite).sum();
    }
}