package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.GitManager;
import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.core.SettingsManager;
import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Circle;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

/**
 * Controller for the home screen
 * 
 * @author Agustín Benítez
 */
public class HomeScreenController {

    private static final Logger logger = LoggerFactory.getLogger(HomeScreenController.class);

    @FXML
    private FlowPane projectsGrid;

    @FXML
    private ScrollPane projectsScrollPane;

    @FXML
    private Button createProjectButton;

    @FXML
    private Button openProjectButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button loginButton;

    @FXML
    private VBox headerContainer;

    @FXML
    private ImageView headerImageView;

    private ProjectManager projectManager;
    private GitManager gitManager;

    @FXML
    public void initialize() {
        logger.info("Initializing HomeScreenController");

        projectManager = new ProjectManager();
        gitManager = new GitManager();

        // Make FlowPane responsive
        if (projectsScrollPane != null) {
            projectsGrid.prefWrapLengthProperty().bind(projectsScrollPane.widthProperty().subtract(50));
        } else {
             projectsGrid.prefWrapLengthProperty().bind(projectsGrid.widthProperty());
        }
        
        // Make Header Image responsive
        if (headerImageView != null && headerContainer != null) {
            headerImageView.fitWidthProperty().bind(headerContainer.widthProperty());
        }

        // Setup button action
        createProjectButton.setOnAction(e -> handleCreateProject());
        openProjectButton.setOnAction(e -> handleOpenProject());
        settingsButton.setOnAction(e -> handleSettings());
        if (loginButton != null) {
            loginButton.setOnAction(e -> handleLogin());
        }

        // Check for saved credentials and update avatar
        String savedUser = SettingsManager.getInstance().getGitUser();
        if (savedUser != null && !savedUser.isEmpty()) {
            updateUserAvatar(savedUser);
        }

        // Load and display projects
        loadProjects();
    }

    private void handleLogin() {
        // Prevent login dialog if already logged in
        if (SettingsManager.getInstance().getGitUser() != null && !SettingsManager.getInstance().getGitUser().isEmpty()) {
            return;
        }

        LoginDialogHelper.showLoginDialog((user, token) -> {
            SettingsManager.getInstance().setGitCredentials(user, token);
            updateUserAvatar(user);
            Alert alert = new Alert(Alert.AlertType.INFORMATION);
            alert.setTitle("Login Exitoso");
            alert.setHeaderText(null);
            alert.setContentText("Sesión iniciada correctamente como: " + user);
            alert.showAndWait();
        });
    }

    private void updateUserAvatar(String user) {
        try {
            String avatarUrl = "https://github.com/" + user + ".png";
            Image image = new Image(avatarUrl, true);
            
            image.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() == 1.0 && !image.isError()) {
                    javafx.application.Platform.runLater(() -> {
                         // Create Avatar
                         Circle avatar = new Circle(15, 15, 15);
                         avatar.setFill(new ImagePattern(image));
                         
                         // Create Git Icon
                         SVGPath gitIcon = new SVGPath();
                         gitIcon.setContent("M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12");
                         gitIcon.setFill(Color.WHITE);
                         gitIcon.setScaleX(0.8);
                         gitIcon.setScaleY(0.8);
                         
                         // Create Container
                         HBox container = new HBox(8);
                         container.setAlignment(Pos.CENTER);
                         container.getChildren().addAll(gitIcon, avatar);
                         
                         if (loginButton != null) {
                            loginButton.setGraphic(container);
                            
                            // Setup Context Menu for Logout
                            ContextMenu contextMenu = new ContextMenu();
                            MenuItem logoutItem = new MenuItem("Cerrar sesión");
                            logoutItem.setOnAction(e -> handleLogout());
                            contextMenu.getItems().add(logoutItem);
                            
                            loginButton.setContextMenu(contextMenu);
                         }
                    });
                }
            });
        } catch (Exception e) {
            logger.error("Failed to load avatar", e);
        }
    }

    private void handleLogout() {
        SettingsManager.getInstance().clearGitCredentials();
        resetLoginButton();
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sesión Cerrada");
        alert.setHeaderText(null);
        alert.setContentText("Has cerrado sesión correctamente.");
        alert.showAndWait();
    }

    private void resetLoginButton() {
        if (loginButton != null) {
            // Remove Context Menu
            loginButton.setContextMenu(null);
            
            // Restore default icon
            SVGPath icon = new SVGPath();
            icon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z");
            icon.setFill(Color.WHITE);
            icon.setScaleX(1.2);
            icon.setScaleY(1.2);
            
            loginButton.setGraphic(icon);
        }
    }

    private void loadProjects() {
        projectsGrid.getChildren().clear();

        List<Project> projects = projectManager.loadProjects();
        logger.info("Loading {} projects", projects.size());

        for (Project project : projects) {
            VBox projectCard = createProjectCard(project);
            projectsGrid.getChildren().add(projectCard);
        }
    }

    private VBox createProjectCard(Project project) {
        VBox card = new VBox(10);
        card.getStyleClass().add("project-card");
        card.setPadding(new Insets(15));
        card.setAlignment(Pos.TOP_LEFT);
        card.setPrefWidth(250);
        card.setPrefHeight(200);

        // Project icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(64);
        iconView.setFitHeight(64);
        iconView.setPreserveRatio(true);

        try {
            Path iconPath = Paths.get(project.getRootPath(), "BP", "pack_icon.png");
            if (Files.exists(iconPath)) {
                Image icon = new Image(iconPath.toUri().toString());
                iconView.setImage(icon);
            } else {
                Image presetIcon = new Image(getClass().getResourceAsStream("/images/preset_logo.png"));
                iconView.setImage(presetIcon);
            }
        } catch (Exception e) {
            logger.warn("Failed to load pack icon for project: {}", project.getName());
            try {
                Image presetIcon = new Image(getClass().getResourceAsStream("/images/preset_logo.png"));
                iconView.setImage(presetIcon);
            } catch (Exception ex) {
                logger.error("Failed to load preset icon", ex);
            }
        }

        // Project name
        Label nameLabel = new Label(project.getName());
        nameLabel.getStyleClass().add("project-card-title");
        nameLabel.setWrapText(true);

        // Project description
        Label descLabel = new Label(project.getDescription());
        descLabel.getStyleClass().add("project-card-description");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(60);

        // Last modified date
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label("Modificado: " + project.getLastModified().format(formatter));
        dateLabel.getStyleClass().add("project-card-date");

        // Add spacer
        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        card.getChildren().addAll(iconView, nameLabel, descLabel, spacer, dateLabel);

        // Click handler to open editor
        card.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                logger.info("Opening project: {}", project.getName());
                NavigationManager.getInstance().showEditor(project);
            }
        });

        // Context menu for delete
        card.setOnContextMenuRequested(e -> {
            ContextMenu menu = new ContextMenu();
            MenuItem deleteItem = new MenuItem("🗑 Eliminar Proyecto");
            deleteItem.setOnAction(event -> handleDeleteProject(project));
            menu.getItems().add(deleteItem);
            menu.show(card, e.getScreenX(), e.getScreenY());
        });

        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-cursor: hand;"));
        card.setOnMouseExited(e -> card.setStyle("-fx-cursor: default;"));

        return card;
    }

    private void handleDeleteProject(Project project) {
        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar eliminación");
        confirmDialog.setHeaderText("¿Eliminar proyecto '" + project.getName() + "'?");
        confirmDialog.setContentText("Esta acción no se puede deshacer.");
        
        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                projectManager.deleteProject(project.getId());
                loadProjects();
            }
        });
    }

    private void handleCreateProject() {
        NavigationManager.getInstance().showCreateProject();
    }

    private void handleOpenProject() {
        DirectoryChooser directoryChooser = new DirectoryChooser();
        directoryChooser.setTitle("Abrir Proyecto Existente");
        File selectedDirectory = directoryChooser.showDialog(projectsGrid.getScene().getWindow());
        
        if (selectedDirectory != null) {
             Project project = new Project(selectedDirectory.getName(), "Imported Project", selectedDirectory.getAbsolutePath());
             NavigationManager.getInstance().showEditor(project);
        }
    }

    private void handleSettings() {
        NavigationManager.getInstance().showSettingsModal();
    }
}