package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
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
    private Button createProjectButton;

    @FXML
    private Button settingsButton;

    private ProjectManager projectManager;

    @FXML
    public void initialize() {
        logger.info("Initializing HomeScreenController");

        projectManager = new ProjectManager();

        // Setup button action
        createProjectButton.setOnAction(e -> handleCreateProject());
        settingsButton.setOnAction(e -> handleSettings());

        // Load and display projects
        loadProjects();
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

        // Project icon - load from pack_icon.png
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
                // Use preset logo if pack_icon doesn't exist
                Image presetIcon = new Image(getClass().getResourceAsStream("/images/preset_logo.png"));
                iconView.setImage(presetIcon);
            }
        } catch (Exception e) {
            logger.warn("Failed to load pack icon for project: {}", project.getName());
            // Use preset logo as fallback
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
        confirmDialog
                .setContentText("Esta acción eliminará el proyecto de la lista (no elimina los archivos del disco).");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                projectManager.removeProject(project);
                logger.info("Deleted project: {}", project.getName());
                loadProjects(); // Reload projects grid
            }
        });
    }

    private void handleSettings() {
        NavigationManager.getInstance().showSettings(() -> NavigationManager.getInstance().showHomeScreen());
    }

    @FXML
    private void handleCreateProject() {
        logger.info("Create project button clicked");

        // Create dialog
        Dialog<Project> dialog = new Dialog<>();
        dialog.setTitle("Crear Nuevo Proyecto");
        dialog.setHeaderText("Ingresa los detalles del proyecto");

        // Set button types
        ButtonType createButtonType = new ButtonType("Crear", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Create form
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        TextField nameField = new TextField();
        nameField.setPromptText("Nombre del addon");

        TextArea descArea = new TextArea();
        descArea.setPromptText("Descripción del addon");
        descArea.setPrefRowCount(3);

        TextField pathField = new TextField();
        pathField.setPromptText("Selecciona la carpeta...");
        pathField.setEditable(false);

        Button browseButton = new Button("Examinar...");

        // Image selection
        TextField imageField = new TextField();
        imageField.setPromptText("Usar preset_logo.png por defecto");
        imageField.setEditable(false);

        Button browseImageButton = new Button("Seleccionar Imagen...");

        final File[] selectedFolder = { null };
        final File[] selectedImage = { null };

        browseButton.setOnAction(e -> {
            DirectoryChooser chooser = new DirectoryChooser();
            chooser.setTitle("Seleccionar Carpeta del Proyecto");

            File folder = chooser.showDialog(dialog.getOwner());
            if (folder != null) {
                selectedFolder[0] = folder;
                pathField.setText(folder.getAbsolutePath());
            }
        });

        browseImageButton.setOnAction(e -> {
            FileChooser chooser = new FileChooser();
            chooser.setTitle("Seleccionar Icono del Pack");
            chooser.getExtensionFilters().add(
                    new FileChooser.ExtensionFilter("Imágenes PNG", "*.png"));

            File image = chooser.showOpenDialog(dialog.getOwner());
            if (image != null) {
                selectedImage[0] = image;
                imageField.setText(image.getName());
            }
        });

        HBox pathBox = new HBox(10, pathField, browseButton);
        HBox.setHgrow(pathField, Priority.ALWAYS);

        HBox imageBox = new HBox(10, imageField, browseImageButton);
        HBox.setHgrow(imageField, Priority.ALWAYS);

        grid.add(new Label("Nombre:"), 0, 0);
        grid.add(nameField, 1, 0);
        grid.add(new Label("Descripción:"), 0, 1);
        grid.add(descArea, 1, 1);
        grid.add(new Label("Ubicación:"), 0, 2);
        grid.add(pathBox, 1, 2);
        grid.add(new Label("Icono:"), 0, 3);
        grid.add(imageBox, 1, 3);

        dialog.getDialogPane().setContent(grid);

        // Convert result
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                String name = nameField.getText().trim();
                String desc = descArea.getText().trim();

                if (name.isEmpty() || desc.isEmpty() || selectedFolder[0] == null) {
                    showError("Error de Validación", "Por favor completa todos los campos");
                    return null;
                }

                return new Project(name, desc, selectedFolder[0].getAbsolutePath());
            }
            return null;
        });

        // Show dialog and process result
        dialog.showAndWait().ifPresent(project -> {
            projectManager.addProject(project);
            logger.info("Created new project: {}", project.getName());

            // Copy pack icon and create manifests
            try {
                Path projectPath = Paths.get(project.getRootPath());
                Path bpPath = projectPath.resolve("BP");
                Path rpPath = projectPath.resolve("RP");
                Path bpIconPath = bpPath.resolve("pack_icon.png");
                Path rpIconPath = rpPath.resolve("pack_icon.png");

                // Ensure directories exist
                Files.createDirectories(bpPath);
                Files.createDirectories(rpPath);

                // Copy pack icons
                if (selectedImage[0] != null) {
                    // Copy selected image
                    Files.copy(selectedImage[0].toPath(), bpIconPath, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(selectedImage[0].toPath(), rpIconPath, StandardCopyOption.REPLACE_EXISTING);
                } else {
                    // Copy preset logo
                    var presetStream = getClass().getResourceAsStream("/images/preset_logo.png");
                    if (presetStream != null) {
                        Files.copy(presetStream, bpIconPath, StandardCopyOption.REPLACE_EXISTING);
                        presetStream = getClass().getResourceAsStream("/images/preset_logo.png");
                        Files.copy(presetStream, rpIconPath, StandardCopyOption.REPLACE_EXISTING);
                    }
                }

                // Create manifest.json for BP (Behavior Pack)
                String bpManifest = createManifestJson(
                        project.getName(),
                        project.getDescription(),
                        "data",
                        java.util.UUID.randomUUID().toString(),
                        java.util.UUID.randomUUID().toString());
                Files.writeString(bpPath.resolve("manifest.json"), bpManifest);

                // Create manifest.json for RP (Resource Pack)
                String rpManifest = createManifestJson(
                        project.getName(),
                        project.getDescription(),
                        "resources",
                        java.util.UUID.randomUUID().toString(),
                        java.util.UUID.randomUUID().toString());
                Files.writeString(rpPath.resolve("manifest.json"), rpManifest);

                logger.info("Created manifest files for project: {}", project.getName());

            } catch (IOException ex) {
                logger.error("Failed to create project files", ex);
                showError("Error", "No se pudieron crear los archivos del proyecto");
            }

            loadProjects(); // Reload projects grid

            showSuccess("Proyecto creado exitosamente!");
        });
    }

    private String createManifestJson(String name, String description, String type, String uuid1, String uuid2) {
        return String.format("""
                {
                  "format_version": 2,
                  "header": {
                    "name": "%s",
                    "description": "%s",
                    "uuid": "%s",
                    "version": [1, 0, 0],
                    "min_engine_version": [1, 20, 0]
                  },
                  "modules": [
                    {
                      "type": "%s",
                      "uuid": "%s",
                      "version": [1, 0, 0]
                    }
                  ]
                }
                """, name, description, uuid1, type, uuid2);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showSuccess(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Éxito");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
