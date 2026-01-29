package com.agustinbenitez.addoncreator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.BiConsumer;

public class LoginDialogHelper {

    private static final Logger logger = LoggerFactory.getLogger(LoginDialogHelper.class);

    public static void showLoginDialog(BiConsumer<String, String> action) {
        Dialog<javafx.util.Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Sign In");
        dialog.setHeaderText(null);

        // Main Container
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.setMinWidth(350);

        // StackPane to switch views
        StackPane root = new StackPane();
        root.getChildren().add(mainContent);

        // --- VIEW 1: Provider Selection ---
        Label title = new Label("Choose your login method");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // GitHub Button
        Button btnGithub = new Button("Sign in with GitHub");
        btnGithub.setMaxWidth(Double.MAX_VALUE);
        btnGithub.setStyle("-fx-background-color: #24292e; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10;");
        SVGPath githubIcon = new SVGPath();
        githubIcon.setContent("M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12");
        githubIcon.setFill(Color.WHITE);
        githubIcon.setScaleX(0.7); githubIcon.setScaleY(0.7);
        btnGithub.setGraphic(new Group(githubIcon));
        btnGithub.setGraphicTextGap(15);
        
        // Google Button
        Button btnGoogle = new Button("Sign in with Google");
        btnGoogle.setMaxWidth(Double.MAX_VALUE);
        btnGoogle.setStyle("-fx-background-color: white; -fx-text-fill: #757575; -fx-border-color: #dadce0; -fx-alignment: CENTER_LEFT; -fx-padding: 10;");
        SVGPath googleIcon = new SVGPath();
        googleIcon.setContent("M12.48 10.92v3.28h7.84c-.24 1.84-.853 3.187-1.787 4.133-1.147 1.147-2.933 2.4-6.053 2.4-4.827 0-8.6-3.893-8.6-8.72s3.773-8.72 8.6-8.72c2.6 0 4.507 1.027 5.907 2.347l2.307-2.307C18.747 1.44 16.133 0 12.48 0 5.867 0 .533 5.333 .533 12S5.867 24 12.48 24c3.44 0 6.053-1.147 8.2-3.387 2.187-2.187 2.853-5.413 2.853-8.12 0-.8-.08-1.56-.24-2.293h-10.813z");
        googleIcon.setFill(Color.web("#4285F4"));
        googleIcon.setScaleX(0.7); googleIcon.setScaleY(0.7);
        btnGoogle.setGraphic(new Group(googleIcon));
        btnGoogle.setGraphicTextGap(15);

        // Manual Button
        Button btnManual = new Button("Manual Credentials");
        btnManual.setMaxWidth(Double.MAX_VALUE);
        btnManual.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 10;");

        // Cancel Button
        Button btnCancelMain = new Button("Cancel");
        btnCancelMain.setMaxWidth(Double.MAX_VALUE);
        btnCancelMain.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 10; -fx-background-color: transparent; -fx-text-fill: #999;");
        btnCancelMain.setOnAction(e -> ((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow()).close());

        mainContent.getChildren().addAll(title, btnGithub, btnGoogle, new Separator(), btnManual, btnCancelMain);

        // --- VIEW 2: Credentials Form ---
        VBox formContent = new VBox(10);
        formContent.setPadding(new Insets(20));
        formContent.setVisible(false);
        formContent.setMinWidth(350);
        
        Label lblFormTitle = new Label("Enter Credentials");
        lblFormTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label lblUser = new Label("Username");
        TextField txtUser = new TextField();
        
        Label lblPass = new Label("Password / Token");
        PasswordField txtPass = new PasswordField();
        
        Hyperlink linkHelp = new Hyperlink("Get Token");
        linkHelp.setVisible(false);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnBack = new Button("Back");
        Button btnSubmit = new Button("Login");
        btnSubmit.setDefaultButton(true);
        buttonBox.getChildren().addAll(btnBack, btnSubmit);
        
        formContent.getChildren().addAll(lblFormTitle, lblUser, txtUser, lblPass, txtPass, linkHelp, buttonBox);
        
        // Add form to root (but make sure it covers main)
        root.getChildren().add(formContent);

        // --- Logic ---
        
        Runnable showMain = () -> {
            mainContent.setVisible(true);
            formContent.setVisible(false);
            dialog.getDialogPane().getButtonTypes().clear(); 
            // Add CANCEL button to enable window close 'X' button, but hide it from view
            dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
            javafx.scene.Node closeButton = dialog.getDialogPane().lookupButton(ButtonType.CANCEL);
            if (closeButton != null) {
                closeButton.setVisible(false);
                closeButton.setManaged(false);
            }
        };

        Runnable showForm = () -> {
            mainContent.setVisible(false);
            formContent.setVisible(true);
        };
        
        btnBack.setOnAction(e -> showMain.run());
        
        btnGithub.setOnAction(e -> {
            lblFormTitle.setText("Sign in with GitHub");
            lblUser.setText("GitHub Username");
            txtUser.setPromptText("username");
            lblPass.setText("Personal Access Token");
            txtPass.setPromptText("ghp_...");
            linkHelp.setText("Generate GitHub Token");
            linkHelp.setVisible(true);
            String url = "https://github.com/settings/tokens/new?scopes=repo,read:user&description=AddonCreator";
            linkHelp.setOnAction(ev -> openUrl(url));
            openUrl(url); // Auto-open browser
            showForm.run();
        });

        btnGoogle.setOnAction(e -> {
            lblFormTitle.setText("Sign in with Google");
            lblUser.setText("Gmail Address");
            txtUser.setPromptText("user@gmail.com");
            lblPass.setText("App Password");
            txtPass.setPromptText("16-character app password");
            linkHelp.setText("Get App Password");
            linkHelp.setVisible(true);
            String url = "https://myaccount.google.com/apppasswords";
            linkHelp.setOnAction(ev -> openUrl(url));
            openUrl(url); // Auto-open browser
            showForm.run();
        });

        btnManual.setOnAction(e -> {
            lblFormTitle.setText("Manual Login");
            lblUser.setText("Username");
            txtUser.setPromptText("");
            lblPass.setText("Password / Token");
            txtPass.setPromptText("");
            linkHelp.setVisible(false);
            showForm.run();
        });

        btnSubmit.setOnAction(e -> {
             String u = txtUser.getText().trim();
             String p = txtPass.getText().trim();
             if (!u.isEmpty() && !p.isEmpty()) {
                 action.accept(u, p);
                 // Close dialog manually since we aren't using ButtonTypes for result
                 ((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow()).close();
             } else {
                 Alert alert = new Alert(Alert.AlertType.ERROR);
                 alert.setTitle("Login Error");
                 alert.setHeaderText(null);
                 alert.setContentText("Please fill in all fields");
                 alert.showAndWait();
             }
        });

        // Initialize state
        showMain.run();
        
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    private static void openUrl(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception e) {
            logger.error("Failed to open URL: " + url, e);
        }
    }
}