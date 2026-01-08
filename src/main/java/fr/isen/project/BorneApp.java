package fr.isen.project;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import fr.isen.project.model.Categorie;
import fr.isen.project.model.Commande;
import fr.isen.project.model.LigneCommande;
import fr.isen.project.model.Plat;
import fr.isen.project.service.RestaurantService;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

public class BorneApp extends Application {

    private final RestaurantService service = new RestaurantService();
    private List<Categorie> menuCategories = new ArrayList<>();
    private final Commande commandeEnCours = new Commande();

    // --- UI ---
    private StackPane rootStack;
    private BorderPane mainLayout;
    private VBox overlaySuivi;

    private String nomClient = "";
    private String choixCouverts = "Baguettes";

    // --- FINANCES ---
    private double remiseTaux = 0.0;
    private String codePromoActif = "";
    private static final double TVA_TAUX = 0.15;

    // --- CONFIG ---
    private static final String BASE_URL_IMG = "http://localhost:8080/images/";
    private static final String API_SUIVI = "http://localhost:8080/api/commande/";

    // --- COULEURS ---
    private static final String COL_BG = "#1e293b";
    private static final String COL_NAV = "#0f172a";
    private static final String COL_CARD = "#334155";
    private static final String COL_ACCENT = "#d97706";
    private static final String COL_GREEN = "#22c55e";
    private static final String COL_QR = "#3b82f6";
    private static final String COL_RED = "#ef4444";

    public static void main(String[] args) { launch(args); }

    @Override
    public void start(Stage primaryStage) {
        try {
            menuCategories = service.getMenu();
        } catch (Exception e) {
            menuCategories = new ArrayList<>();
        }

        rootStack = new StackPane();
        mainLayout = new BorderPane();
        mainLayout.setStyle("-fx-background-color: " + COL_BG + ";");

        initOverlaySuivi();
        rootStack.getChildren().addAll(mainLayout, overlaySuivi);

        showEcranAccueil();

        Scene scene = new Scene(rootStack, 1920, 1080);
        primaryStage.setTitle("Borne Saveurs d'Asie - ISEN");
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
        primaryStage.show();
    }

    private void initOverlaySuivi() {
        overlaySuivi = new VBox(20);
        overlaySuivi.setAlignment(Pos.CENTER);
        overlaySuivi.setStyle("-fx-background-color: rgba(0, 0, 0, 0.85);");
        overlaySuivi.setVisible(false);

        VBox card = new VBox(20);
        card.setMaxWidth(500); card.setPadding(new Insets(40));
        card.setStyle("-fx-background-color: " + COL_CARD + "; -fx-background-radius: 15; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");
        card.setAlignment(Pos.CENTER);

        Label title = new Label("SUIVI DE COMMANDE");
        title.setStyle("-fx-text-fill: white; -fx-font-size: 24px; -fx-font-weight: bold;");

        TextField tfId = new TextField();
        tfId.setPromptText("Numéro de commande");
        tfId.setStyle("-fx-font-size: 20px;");

        Label lblResultat = new Label("Entrez votre numéro");
        lblResultat.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 18px;");

        Button btnCheck = new Button("VÉRIFIER STATUT");
        btnCheck.setStyle("-fx-background-color: " + COL_ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px; -fx-padding: 10 30;");

        btnCheck.setOnAction(e -> {
            String id = tfId.getText(); if(id.isEmpty()) return;
            lblResultat.setText("Recherche...");
            new Thread(() -> {
                try {
                    URL url = new URL(API_SUIVI + id);
                    HttpURLConnection con = (HttpURLConnection) url.openConnection();
                    con.setRequestMethod("GET");
                    if (con.getResponseCode() == 200) {
                        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
                        String content = in.readLine(); in.close();
                        String status = content.contains("\"status\":\"") ? content.split("\"status\":\"")[1].split("\"")[0] : "Inconnu";
                        Platform.runLater(() -> {
                            lblResultat.setText("STATUT : " + status);
                            lblResultat.setStyle("-fx-text-fill: " + COL_GREEN + "; -fx-font-size: 22px; -fx-font-weight: bold;");
                        });
                    } else {
                        Platform.runLater(() -> {
                            lblResultat.setText("Commande introuvable.");
                            lblResultat.setStyle("-fx-text-fill: #ef4444; -fx-font-size: 18px;");
                        });
                    }
                } catch (Exception ex) { Platform.runLater(() -> lblResultat.setText("Erreur connexion.")); }
            }).start();
        });

        Button btnClose = new Button("FERMER");
        btnClose.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: white; -fx-border-radius: 5;");
        btnClose.setOnAction(e -> { overlaySuivi.setVisible(false); mainLayout.setEffect(null); });

        card.getChildren().addAll(title, tfId, btnCheck, lblResultat, new Separator(), btnClose);
        overlaySuivi.getChildren().add(card);
    }

    private void showEcranAccueil() {
        mainLayout.setTop(null);
        commandeEnCours.ligneCommande.clear();
        remiseTaux = 0.0; codePromoActif = ""; nomClient = "";

        VBox content = new VBox(40); content.setAlignment(Pos.CENTER);
        Label titre = new Label("SAVEURS D'ASIE"); titre.setFont(Font.font("Segoe UI", FontWeight.BOLD, 64)); titre.setTextFill(Color.web(COL_ACCENT));

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
            else {
                Alert err = new Alert(Alert.AlertType.ERROR, "Impossible de charger le menu. Vérifiez le Backend.");
                err.show();
            }
        });

        Button btnSuivi = new Button("🔍 SUIVRE MA COMMANDE");
        btnSuivi.setStyle("-fx-font-size: 18px; -fx-text-fill: #94a3b8; -fx-background-color: transparent; -fx-border-color: #94a3b8; -fx-border-radius: 10; -fx-cursor: hand;");
        btnSuivi.setOnAction(e -> {
            overlaySuivi.setVisible(true);
            mainLayout.setEffect(new GaussianBlur(10));
        });

        content.getChildren().addAll(titre, inputNom, btnStart, new Separator(), btnSuivi);
        mainLayout.setCenter(content);
    }

    private void showEcranCatalogue(Categorie catActive) {
        HBox nav = new HBox(15); nav.setPadding(new Insets(20)); nav.setStyle("-fx-background-color: " + COL_NAV + ";"); nav.setAlignment(Pos.CENTER_LEFT);
        for (Categorie cat : menuCategories) {
            Button btnCat = new Button(cat.nom.toUpperCase()); boolean isActive = (cat == catActive);
            btnCat.setStyle("-fx-font-size: 16px; -fx-font-weight: bold; -fx-padding: 12 25; -fx-background-radius: 30; -fx-cursor: hand; -fx-text-fill: white; -fx-background-color: " + (isActive ? COL_ACCENT : COL_CARD));
            btnCat.setOnAction(e -> showEcranCatalogue(cat)); nav.getChildren().add(btnCat);
        }
        Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);
        Button btnPanier = new Button("PANIER (" + getPanierCount() + ")");
        btnPanier.setStyle("-fx-font-size: 18px; -fx-background-color: " + COL_GREEN + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-padding: 10 30; -fx-background-radius: 8; -fx-cursor: hand;");
        btnPanier.setOnAction(e -> showEcranPanier(catActive));
        nav.getChildren().addAll(spacer, btnPanier);

        mainLayout.setTop(nav);

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

            if (!p.estDisponible) {
                ColorAdjust grayscale = new ColorAdjust();
                grayscale.setSaturation(-1); grayscale.setBrightness(-0.3);
                iv.setEffect(grayscale);
            }

            Label nom = new Label(p.nom);
            nom.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 18px;");
            nom.setWrapText(true); nom.setAlignment(Pos.CENTER);

            Label prix = new Label(String.format("%.2f €", p.prix));
            prix.setStyle("-fx-text-fill: " + COL_ACCENT + "; -fx-font-size: 20px; -fx-font-weight: bold;");

            Button btnAdd = new Button("CHOISIR");
            btnAdd.setMaxWidth(Double.MAX_VALUE);

            if (p.estDisponible) {
                btnAdd.setStyle("-fx-background-color: " + COL_ACCENT + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-background-radius: 8;");
                btnAdd.setOnAction(e -> showEcranDetail(p, catActive));
            } else {
                btnAdd.setText("RUPTURE");
                btnAdd.setStyle("-fx-background-color: #475569; -fx-text-fill: #94a3b8; -fx-font-weight: bold; -fx-background-radius: 8; -fx-border-color: #cbd5e1;");
                btnAdd.setDisable(true);
            }

            card.getChildren().addAll(iv, nom, new Region(), prix, btnAdd);
            grid.getChildren().add(card);
        }

        ScrollPane sp = new ScrollPane(grid);
        sp.setFitToWidth(true);
        sp.setStyle("-fx-background: " + COL_BG + "; -fx-border-color: " + COL_BG + ";");
        sp.getStyleClass().add("edge-to-edge");
        sp.setPannable(true);
        mainLayout.setCenter(sp);
    }

    private void showEcranDetail(Plat p, Categorie catSource) {
        mainLayout.setTop(null);
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
        Label lblOpt = new Label("Personnalisation :"); lblOpt.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-underline: true;");
        ToggleGroup group = new ToggleGroup();
        String n = p.nom.toLowerCase();

        if (n.contains("nems") || n.contains("rouleaux")) {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Sauce Nuoc-mâm", group, true), createRadio("Sauce Aigre-douce", group, false), createRadio("Sauce Soja", group, false));
        } else if (n.contains("bobun")) {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Complet", group, true), createRadio("Sans Oignons", group, false), createRadio("Sans Cacahuètes", group, false));
            CheckBox cbBoeuf = new CheckBox("🥩 Extra Bœuf (+4€)"); cbBoeuf.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            CheckBox cbNems = new CheckBox("🥢 Supplément Nems (+3€)"); cbNems.setStyle("-fx-text-fill: white; -fx-font-size: 16px;");
            CheckBox cbPiment = new CheckBox("🌶️ Extra Piment (Gratuit)"); cbPiment.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 16px;");
            optionsBox.getChildren().addAll(cbBoeuf, cbNems, cbPiment);
        } else if (n.contains("riz") && !n.contains("cantonais")) {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Standard", group, true), createRadio("Sans œuf", group, false));
        } else if (n.contains("nougat")) {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Tendre", group, true), createRadio("Dur", group, false));
        } else if (n.contains("coco")) {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Chaudes", group, true), createRadio("Froides", group, false));
        } else if (catSource.nom.equalsIgnoreCase("Desserts")) {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Nature", group, true), createRadio("Chantilly", group, false));
        } else if (catSource.nom.equalsIgnoreCase("Plats")) {
            Label lblAcc = new Label("Accompagnement :"); lblAcc.setStyle("-fx-text-fill: #cbd5e1; -fx-font-size: 16px;");
            optionsBox.getChildren().addAll(lblOpt, lblAcc, createRadio("Riz Blanc", group, true), createRadio("Nouilles Sautées", group, false), createRadio("Riz Cantonais (+2€)", group, false));
            CheckBox cbPiment = new CheckBox("🌶️ Extra Piment"); cbPiment.setStyle("-fx-text-fill: #fca5a5; -fx-font-size: 16px;");
            optionsBox.getChildren().add(cbPiment);
        } else {
            optionsBox.getChildren().addAll(lblOpt, createRadio("Standard", group, true));
        }

        HBox qteBox = new HBox(15); Label lblQte = new Label("Quantité :"); lblQte.setStyle("-fx-text-fill: white; -fx-font-size: 18px;");
        Spinner<Integer> spinner = new Spinner<>(1, 20, 1); spinner.setStyle("-fx-font-size: 16px;");
        qteBox.getChildren().addAll(lblQte, spinner);

        HBox actions = new HBox(20);
        Button btnAdd = new Button("AJOUTER");
        btnAdd.setStyle("-fx-background-color: " + COL_GREEN + "; -fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15 40; -fx-background-radius: 10; -fx-cursor: hand;");
        Button btnCancel = new Button("ANNULER");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-border-color: #ef4444; -fx-text-fill: #ef4444; -fx-font-size: 20px; -fx-padding: 14 30; -fx-border-radius: 10; -fx-cursor: hand;");

        btnAdd.setOnAction(e -> {
            LigneCommande ligne = new LigneCommande();
            ligne.plat = p; ligne.quantite = spinner.getValue();
            StringBuilder optBuild = new StringBuilder();
            if (group.getSelectedToggle() != null) optBuild.append(((RadioButton) group.getSelectedToggle()).getText()); else optBuild.append("Standard");
            for(javafx.scene.Node node : optionsBox.getChildren()) {
                if(node instanceof CheckBox) {
                    CheckBox cb = (CheckBox) node;
                    if(cb.isSelected()) optBuild.append(" + ").append(cb.getText());
                }
            }
            ligne.options = optBuild.toString();
            commandeEnCours.ligneCommande.add(ligne);
            showEcranCatalogue(catSource);
        });

        btnCancel.setOnAction(e -> showEcranCatalogue(catSource));
        actions.getChildren().addAll(btnCancel, btnAdd);
        details.getChildren().addAll(nom, desc, prix, new Separator(), optionsBox, qteBox, actions);
        container.getChildren().addAll(bigIv, details);
        mainLayout.setCenter(container);
    }

    private RadioButton createRadio(String text, ToggleGroup group, boolean selected) {
        RadioButton rb = new RadioButton(text); rb.setToggleGroup(group); rb.setSelected(selected);
        rb.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-cursor: hand;"); return rb;
    }

    // --- 4. PANIER ---
    private void showEcranPanier(Categorie catSource) {
        mainLayout.setTop(null);
        VBox content = new VBox(20); content.setPadding(new Insets(30)); content.setAlignment(Pos.CENTER);
        content.setStyle("-fx-background-color: " + COL_BG + ";");

        Label titre = new Label("VOTRE COMMANDE");
        titre.setStyle("-fx-text-fill: white; -fx-font-size: 36px; -fx-font-weight: bold;");

        VBox listItems = new VBox(10);
        listItems.setStyle("-fx-background-color: #fffbeb; -fx-padding: 15; -fx-background-radius: 5;");

        double totalHT = 0.0;

        if (commandeEnCours.ligneCommande.isEmpty()) {
            Label empty = new Label("Votre panier est vide.");
            empty.setStyle("-fx-text-fill: #555; -fx-font-size: 16px;");
            listItems.getChildren().add(empty);
        } else {
            listItems.getChildren().add(new Label("Client : " + nomClient));
            listItems.getChildren().add(new Separator());

            for (LigneCommande l : new ArrayList<>(commandeEnCours.ligneCommande)) {
                double prixU = l.plat.prix;
                String opts = l.options;
                if (opts != null) {
                    if(opts.contains("Cantonais")) prixU += 2.0;
                    if(opts.contains("Extra Bœuf")) prixU += 4.0;
                    if(opts.contains("Supplément Nems")) prixU += 3.0;
                }
                double ligneTotal = prixU * l.quantite;
                totalHT += ligneTotal;

                HBox row = new HBox(15);
                row.setAlignment(Pos.CENTER_LEFT);

                VBox infoBox = new VBox(2);
                Label lblNom = new Label(l.quantite + "x " + l.plat.nom);
                lblNom.setStyle("-fx-font-weight: bold; -fx-text-fill: #333; -fx-font-size: 14px;");
                Label lblOpt = new Label(l.options.equals("Standard") ? "" : l.options);
                lblOpt.setStyle("-fx-text-fill: #666; -fx-font-size: 12px; -fx-font-style: italic;");
                infoBox.getChildren().addAll(lblNom, lblOpt);

                Region spacer = new Region(); HBox.setHgrow(spacer, Priority.ALWAYS);

                Label lblPrix = new Label(String.format("%.2f €", ligneTotal));
                lblPrix.setStyle("-fx-text-fill: #333; -fx-font-weight: bold;");

                Button btnMinus = new Button("➖");
                btnMinus.setStyle("-fx-background-color: " + COL_RED + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-font-size: 10px;");
                btnMinus.setOnAction(e -> {
                    l.quantite--;
                    if (l.quantite <= 0) {
                        commandeEnCours.ligneCommande.remove(l);
                    }
                    showEcranPanier(catSource);
                });

                row.getChildren().addAll(infoBox, spacer, lblPrix, btnMinus);
                listItems.getChildren().add(row);
                listItems.getChildren().add(new Separator());
            }
        }

        double montantRemise = totalHT * remiseTaux;
        double netHT = totalHT - montantRemise;
        double tva = netHT * TVA_TAUX;
        double totalTTC = netHT + tva;

        VBox resumeBox = new VBox(5);
        resumeBox.setStyle("-fx-background-color: #fffbeb; -fx-padding: 10;");
        if (remiseTaux > 0) resumeBox.getChildren().add(new Label(String.format("PROMO (%s): -%.2f €", codePromoActif, montantRemise)));
        resumeBox.getChildren().add(new Label(String.format("TVA (15%%): %.2f €", tva)));
        Label lblTotal = new Label(String.format("NET À PAYER : %.2f €", totalTTC));
        lblTotal.setStyle("-fx-font-weight: bold; -fx-font-size: 18px;");
        resumeBox.getChildren().add(lblTotal);

        listItems.getChildren().add(resumeBox);

        ScrollPane scrollTicket = new ScrollPane(listItems);
        scrollTicket.setMaxWidth(600);
        scrollTicket.setPrefHeight(400);
        scrollTicket.setFitToWidth(true);
        scrollTicket.setStyle("-fx-background: " + COL_BG + "; -fx-border-color: transparent;");

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

        VBox promoGlobalBox = new VBox(15); promoGlobalBox.setAlignment(Pos.CENTER);
        HBox manualPromo = new HBox(15); manualPromo.setAlignment(Pos.CENTER);
        TextField tfCode = new TextField(); tfCode.setPromptText("Code Promo (ex: VIP2025)");
        Button btnCode = new Button("Appliquer"); btnCode.setStyle("-fx-background-color: #64748b; -fx-text-fill: white;");
        btnCode.setOnAction(e -> appliquerPromo(tfCode.getText(), catSource));
        manualPromo.getChildren().addAll(tfCode, btnCode);

        Button btnQR = new Button("📱 Scanner QR Code Client");
        btnQR.setStyle("-fx-background-color: " + COL_QR + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 16px; -fx-cursor: hand;");
        btnQR.setOnAction(e -> {
            Alert loading = new Alert(Alert.AlertType.INFORMATION); loading.setTitle("Scan en cours"); loading.setHeaderText("Lecture..."); loading.show();
            new Thread(() -> {
                try { Thread.sleep(1500); } catch (InterruptedException ex) {}
                Platform.runLater(() -> { loading.close(); appliquerPromo("QR_FLASH", catSource); new Alert(Alert.AlertType.INFORMATION, "Succès !").show(); });
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
                // --- CRUCIAL: ON RECHARGE LE MENU ICI POUR METTRE A JOUR LE STOCK ---
                try { menuCategories = service.getMenu(); } catch (Exception ex) { }
                // --------------------------------------------------------------------
                showEcranConfirmation(id);
            } catch (Exception ex) {
                Alert error = new Alert(Alert.AlertType.ERROR);
                error.setTitle("Commande refusée");
                error.setHeaderText("Stock insuffisant");
                error.setContentText("Le serveur a refusé la commande car certains plats ne sont plus disponibles en quantité suffisante. Veuillez modifier votre panier.");
                error.show();
            }
        });

        actions.getChildren().addAll(btnBack, btnPay);
        content.getChildren().addAll(titre, scrollTicket, ustensilesBox, new Separator(), promoGlobalBox, new Separator(), actions);
        mainLayout.setCenter(content);
    }

    private void appliquerPromo(String code, Categorie catSource) {
        if (code == null || code.trim().isEmpty()) return;
        double taux = 0.0;
        switch(code.toUpperCase()) {
            case "VIP2025": taux = 0.20; break; case "BIENVENUE": taux = 0.10; break; case "ISEN_STUDENT": taux = 0.15; break; case "QR_FLASH": taux = 0.05; break; default: taux = 0.0;
        }
        if (taux > 0) { this.remiseTaux = taux; this.codePromoActif = code.toUpperCase(); showEcranPanier(catSource); } else { new Alert(Alert.AlertType.WARNING, "Code inconnu").show(); }
    }

    // --- 5. CONFIRMATION (FORMAT TICKET CAISSE) ---
    private void showEcranConfirmation(String id) {
        mainLayout.setTop(null);
        VBox c = new VBox(25); c.setAlignment(Pos.CENTER); c.setStyle("-fx-background-color: " + COL_BG + ";"); c.setPadding(new Insets(30));

        Label m = new Label("COMMANDE VALIDÉE !");
        m.setStyle("-fx-text-fill: " + COL_GREEN + "; -fx-font-size: 40px; -fx-font-weight: bold;");

        // --- TICKET DE CAISSE RÉALISTE ---
        VBox ticket = new VBox(5);
        ticket.setMaxWidth(400);
        ticket.setPadding(new Insets(20));
        ticket.setAlignment(Pos.TOP_CENTER);
        // Style papier blanc avec ombre portée
        ticket.setStyle("-fx-background-color: white; -fx-background-radius: 5; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.5), 10, 0, 0, 5);");

        // Header Ticket
        Label tHeader = new Label("SAVEURS D'ASIE");
        tHeader.setFont(Font.font("Courier New", FontWeight.BOLD, 24));
        Label tSub = new Label("123 Avenue de l'ISEN\n83000 TOULON");
        tSub.setTextAlignment(TextAlignment.CENTER);
        tSub.setFont(Font.font("Courier New", 12));
        Label tDate = new Label(new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
        tDate.setFont(Font.font("Courier New", 12));
        Label tOrder = new Label("Ticket #" + id + " | Client: " + nomClient);
        tOrder.setFont(Font.font("Courier New", FontWeight.BOLD, 14));

        ticket.getChildren().addAll(tHeader, tSub, tDate, new Separator(), tOrder, new Separator());

        // Items Ticket
        VBox itemsBox = new VBox(5);
        itemsBox.setAlignment(Pos.TOP_LEFT);
        double totalHT = 0.0;

        for (LigneCommande l : commandeEnCours.ligneCommande) {
            double p = l.plat.prix;
            if (l.options.contains("Cantonais")) p += 2.0;
            if (l.options.contains("Extra Bœuf")) p += 4.0;
            if (l.options.contains("Supplément Nems")) p += 3.0;
            totalHT += p * l.quantite;

            // Ligne Produit (Format: Qty x Nom ........ Prix)
            HBox line = new HBox();
            Label lName = new Label(l.quantite + "x " + l.plat.nom);
            lName.setFont(Font.font("Courier New", 12));
            Region space = new Region(); HBox.setHgrow(space, Priority.ALWAYS);
            Label lPrice = new Label(String.format("%.2f", p * l.quantite));
            lPrice.setFont(Font.font("Courier New", 12));
            line.getChildren().addAll(lName, space, lPrice);
            itemsBox.getChildren().add(line);

            // Ligne Option (si existe)
            if(!l.options.equals("Standard")) {
                Label lOpt = new Label("  (" + l.options + ")");
                lOpt.setFont(Font.font("Courier New", 10));
                itemsBox.getChildren().add(lOpt);
            }
        }
        ticket.getChildren().addAll(itemsBox, new Separator());

        // Totaux Ticket
        double montantRemise = totalHT * remiseTaux;
        double netHT = totalHT - montantRemise;
        double tva = netHT * TVA_TAUX;
        double totalTTC = netHT + tva;

        VBox totals = new VBox(2);
        totals.setAlignment(Pos.TOP_RIGHT);
        if(remiseTaux > 0) totals.getChildren().add(makeTicketLine("Remise:", String.format("-%.2f", montantRemise)));
        totals.getChildren().add(makeTicketLine("TVA (15%):", String.format("%.2f", tva)));

        Label lTotalBig = new Label("TOTAL TTC : " + String.format("%.2f €", totalTTC));
        lTotalBig.setFont(Font.font("Courier New", FontWeight.BOLD, 18));
        lTotalBig.setPadding(new Insets(10, 0, 0, 0));

        totals.getChildren().add(lTotalBig);
        ticket.getChildren().addAll(totals, new Separator(), new Label("Merci de votre visite !"));
        // ---------------------------------

        Button b = new Button("NOUVELLE COMMANDE");
        b.setStyle("-fx-background-color: " + COL_ACCENT + "; -fx-text-fill: white; -fx-font-size: 24px; -fx-padding: 15 50; -fx-background-radius: 10; -fx-cursor: hand;");
        b.setOnAction(e -> showEcranAccueil());

        c.getChildren().addAll(m, ticket, b);
        mainLayout.setCenter(c);
    }

    // Helper pour lignes de ticket alignées
    private HBox makeTicketLine(String label, String value) {
        HBox hb = new HBox();
        Label l = new Label(label); l.setFont(Font.font("Courier New", 12));
        Region r = new Region(); HBox.setHgrow(r, Priority.ALWAYS);
        Label v = new Label(value); v.setFont(Font.font("Courier New", 12));
        hb.getChildren().addAll(l, r, v);
        return hb;
    }

    private int getPanierCount() { return commandeEnCours.ligneCommande.stream().mapToInt(l -> l.quantite).sum(); }
}