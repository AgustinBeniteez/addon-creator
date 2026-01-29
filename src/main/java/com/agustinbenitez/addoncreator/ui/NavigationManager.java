package com.agustinbenitez.addoncreator.ui;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.agustinbenitez.addoncreator.models.Project;

import java.io.IOException;

import javafx.stage.Modality;

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
     * Show create project screen
     */
    public void showCreateProject() {
        try {
            logger.info("Navigating to create project screen");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/MainWindow.fxml"));
            Scene scene = new Scene(loader.load(), 1200, 800);
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());

            primaryStage.setScene(scene);
            primaryStage.setTitle("Addon Creator - New Project");

        } catch (IOException e) {
            logger.error("Failed to load create project screen", e);
            throw new RuntimeException("Failed to load create project screen", e);
        }
    }

    /**
     * Show settings as a modal dialog
     */
    public void showSettingsModal() {
        try {
            logger.info("Opening settings modal");

            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/fxml/Settings.fxml"));
            Scene scene = new Scene(loader.load());
            scene.getStylesheets().add(
                    getClass().getResource("/css/styles.css").toExternalForm());

            Stage modalStage = new Stage();
            modalStage.initModality(Modality.APPLICATION_MODAL);
            modalStage.initOwner(primaryStage);
            modalStage.setTitle("Settings");
            modalStage.setScene(scene);
            modalStage.setResizable(false);
            
            modalStage.showAndWait();

        } catch (IOException e) {
            logger.error("Failed to load settings modal", e);
            throw new RuntimeException("Failed to load settings modal", e);
        }
    }
}
