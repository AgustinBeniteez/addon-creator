package com.agustinbenitez.addoncreator.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.agustinbenitez.addoncreator.models.Project;

import java.io.IOException;

/**
 * Manages navigation between different screens
 * 
 * @author Agustín Benítez
 */
public class NavigationManager {

    private static final Logger logger = LoggerFactory.getLogger(NavigationManager.class);
    private static NavigationManager instance;

    private Stage primaryStage;

    private NavigationManager() {
    }

    public static NavigationManager getInstance() {
        if (instance == null) {
            instance = new NavigationManager();
        }
        return instance;
    }

    public void setPrimaryStage(Stage stage) {
        this.primaryStage = stage;
    }

    /**
     * Navigate to home screen
     */
    public void showHomeScreen() {
        try {
            logger.info("Navigating to home screen");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/HomeScreen.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Addon Creator - Home");

        } catch (IOException e) {
            logger.error("Failed to load home screen", e);
            throw new RuntimeException("Failed to load home screen", e);
        }
    }

    /**
     * Navigate to editor screen
     */
    public void showEditor(Project project) {
        try {
            logger.info("Navigating to editor for project: {}", project.getName());

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Editor.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());

            // Pass project to controller
            EditorController controller = loader.getController();
            controller.setProject(project);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Addon Creator - " + project.getName());

        } catch (IOException e) {
            logger.error("Failed to load editor screen", e);
            throw new RuntimeException("Failed to load editor screen", e);
        }
    }

    /**
     * Navigate to settings screen
     * 
     * @param backAction Action to execute when back button is pressed
     */
    public void showSettings(Runnable backAction) {
        try {
            logger.info("Navigating to settings screen");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Settings.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());

            SettingsController controller = loader.getController();
            controller.setBackAction(backAction);

            primaryStage.setScene(scene);
            primaryStage.setTitle("Addon Creator - Settings");

        } catch (IOException e) {
            logger.error("Failed to load settings screen", e);
            throw new RuntimeException("Failed to load settings screen", e);
        }
    }
}
