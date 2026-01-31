package com.agustinbenitez.addoncreator;

import com.agustinbenitez.addoncreator.ui.NavigationManager;
import com.agustinbenitez.addoncreator.core.SettingsManager;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * Main application class for Addon Creator
 * 
 * @author Agustín Benítez
 */
public class AddonCreatorApp extends Application {

    private static final Logger logger = LoggerFactory.getLogger(AddonCreatorApp.class);
    private static final String APP_TITLE = "Addon Creator - Minecraft Bedrock";

    @Override
    public void start(Stage primaryStage) {
        try {
            logger.info("Starting Addon Creator application...");

            // Initialize NavigationManager
            NavigationManager.getInstance().setPrimaryStage(primaryStage);

            // Set app icon
            try {
                javafx.scene.image.Image icon = new javafx.scene.image.Image(
                        getClass().getResourceAsStream("/images/addoncreator.png"));
                primaryStage.getIcons().add(icon);
            } catch (Exception e) {
                logger.warn("Failed to load app icon", e);
            }

            // Load Home Screen
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/HomeScreen.fxml"));
            BorderPane root = loader.load();

            // Create scene with saved dimensions
            double width = SettingsManager.getInstance().getWindowWidth();
            double height = SettingsManager.getInstance().getWindowHeight();
            Scene scene = new Scene(root, width, height);
            
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());

            // Set dark title bar (Windows 11 style)
            scene.setFill(javafx.scene.paint.Color.rgb(45, 45, 45));

            // Configure stage
            primaryStage.setTitle(APP_TITLE);
            primaryStage.setScene(scene);
            primaryStage.setMinWidth(800);
            primaryStage.setMinHeight(600);
            
            if (SettingsManager.getInstance().isWindowMaximized()) {
                primaryStage.setMaximized(true);
            }
            
            primaryStage.show();

            logger.info("Application started successfully");

        } catch (Exception e) {
            logger.error("Failed to load application UI", e);
            e.printStackTrace();
            showErrorAndExit("Failed to start application: " + e.getMessage());
        }
    }

    @Override
    public void stop() {
        logger.info("Shutting down Addon Creator application");
        System.exit(0);
    }

    private void showErrorAndExit(String message) {
        System.err.println(message);
        System.exit(1);
    }

    public static void main(String[] args) {
        logger.info("Addon Creator v1.0.0 - Created by Agustín Benítez");
        launch(args);
    }
}
