package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.GitManager;
import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.core.SettingsManager;
import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.Group;
import javafx.scene.paint.ImagePattern;
import javafx.scene.paint.Color;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.geometry.Pos;
import javafx.geometry.Insets;
import javafx.animation.PauseTransition;
import javafx.animation.ScaleTransition;
import javafx.scene.Node;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;
import com.agustinbenitez.addoncreator.utils.ZipUtils;
import javafx.stage.FileChooser;

/**
 * Controller for the home screen
 * 
 * @author Agustín Benítez
 */
public class HomeScreenController {

    private static final Logger logger = LoggerFactory.getLogger(HomeScreenController.class);

    // SVG Paths for sort icons
    // Descending (Newest First): Calendar at TOP
    private static final String SORT_DESCENDING_PATH = "M3 18h12v-2H3v2zM3 13h10v-2H3v2zM3 8h8v-2H3v2zM15 3h1V1h2v2h2V1h1v2h1c1.1 0 2 .9 2 2v6c0 1.1-.9 2-2 2h-8c-1.1 0-2-.9-2-2V5c0-1.1.9-2 2-2h1V3zm7 8V7h-8v4h8z";

    // Ascending (Oldest First): Calendar at BOTTOM
    private static final String SORT_ASCENDING_PATH = "M3 6h12v2H3V6zM3 11h10v2H3v-2zM3 16h8v2H3v-2zM15 13h1v-2h2v2h2v-2h1v2h1c1.1 0 2 .9 2 2v6c0 1.1-.9 2-2 2h-8c-1.1 0-2-.9-2-2v-6c0-1.1.9-2 2-2h1v-2zm7 8v-4h-8v4h8z";

    // View Mode Icons
    private static final String VIEW_CARDS_PATH = "M4.5 3h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1 -1.5 1.5h-4a1.5 1.5 0 0 1 -1.5 -1.5v-4a1.5 1.5 0 0 1 1.5 -1.5z M15.5 3h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1 -1.5 1.5h-4a1.5 1.5 0 0 1 -1.5 -1.5v-4a1.5 1.5 0 0 1 1.5 -1.5z M4.5 14h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1 -1.5 1.5h-4a1.5 1.5 0 0 1 -1.5 -1.5v-4a1.5 1.5 0 0 1 1.5 -1.5z M15.5 14h4a1.5 1.5 0 0 1 1.5 1.5v4a1.5 1.5 0 0 1 -1.5 1.5h-4a1.5 1.5 0 0 1 -1.5 -1.5v-4a1.5 1.5 0 0 1 1.5 -1.5z";
    private static final String VIEW_LIST_PATH = "M4 5h1a1 1 0 0 1 1 1v1a1 1 0 0 1 -1 1h-1a1 1 0 0 1 -1 -1v-1a1 1 0 0 1 1 -1z M9 5.5h11a1 1 0 0 1 1 1v0a1 1 0 0 1 -1 1h-11a1 1 0 0 1 -1 -1v0a1 1 0 0 1 1 -1z M4 10.5h1a1 1 0 0 1 1 1v1a1 1 0 0 1 -1 1h-1a1 1 0 0 1 -1 -1v-1a1 1 0 0 1 1 -1z M9 11h11a1 1 0 0 1 1 1v0a1 1 0 0 1 -1 1h-11a1 1 0 0 1 -1 -1v0a1 1 0 0 1 1 -1z M4 16h1a1 1 0 0 1 1 1v1a1 1 0 0 1 -1 1h-1a1 1 0 0 1 -1 -1v-1a1 1 0 0 1 1 -1z M9 16.5h11a1 1 0 0 1 1 1v0a1 1 0 0 1 -1 1h-11a1 1 0 0 1 -1 -1v0a1 1 0 0 1 1 -1z";

    @FXML
    private FlowPane projectsGrid;

    @FXML
    private ScrollPane projectsScrollPane;

    @FXML
    private Button createProjectButton;

    @FXML
    private Button openProjectButton;

    @FXML
    private Button pixelArtButton;

    @FXML
    private Button settingsButton;

    @FXML
    private Button sortProjectsButton;

    @FXML
    private Button viewModeButton;

    @FXML
    private SVGPath viewModeIcon;

    @FXML
    private Button loginButton;

    @FXML
    private VBox headerContainer;

    @FXML
    private ImageView headerImageView;

    @FXML
    private TextField searchField;

    @FXML
    private StackPane rootStackPane;

    private List<Project> allProjects;

    private ProjectManager projectManager;
    private GitManager gitManager;
    private boolean isSortAscending = false;
    private boolean isListMode = false;

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

        if (pixelArtButton != null) {
            pixelArtButton.setOnAction(e -> handleOpenPixelArtEditor());

            // Create Pixel Art Icon
            Group group = new Group();

            // Frame: <rect x="1" y="1" width="22" height="22" fill="none" stroke="#9A9A9A"
            // stroke-width="2"/>
            Rectangle frame = new Rectangle(1, 1, 20, 20);
            frame.setFill(Color.TRANSPARENT);
            frame.setStroke(Color.WHITE);
            frame.setStrokeWidth(2);

            // Sun: <rect x="15" y="5" width="3" height="3" fill="#9A9A9A"/>
            Rectangle sun = new Rectangle(15, 5, 2, 2);
            sun.setFill(Color.WHITE);

            // Mountain: <path d="M4 17 L9 11 L13 15 L16 12 L20 17 Z" fill="#9A9A9A"/>
            SVGPath mountain = new SVGPath();
            mountain.setContent("M4 17 L9 11 L13 15 L16 12 L20 17 Z");
            mountain.setFill(Color.WHITE);

            group.getChildren().addAll(frame, sun, mountain);

            // Scale to fit (0.8)
            group.setScaleX(0.5);
            group.setScaleY(0.5);

            pixelArtButton.setGraphic(group);
        }

        settingsButton.setOnAction(e -> handleSettings());
        if (sortProjectsButton != null) {
            logger.info("Sort button initialized");
            sortProjectsButton.setOnAction(e -> handleSortProjects());
        } else {
            logger.error("Sort button failed to inject!");
        }

        if (viewModeButton != null) {
            viewModeButton.setOnAction(e -> handleToggleViewMode());
            // Load saved view mode
            String savedMode = SettingsManager.getInstance().getProjectViewMode();
            isListMode = "list".equals(savedMode);
            updateViewModeIcon();
        }

        if (loginButton != null) {
            loginButton.setOnAction(e -> handleLogin());
        }

        if (searchField != null) {
            searchField.textProperty().addListener((obs, oldVal, newVal) -> filterProjects(newVal));
        }

        // Check for saved credentials and update avatar
        String savedUser = SettingsManager.getInstance().getGitUser();
        if (savedUser != null && !savedUser.isEmpty()) {
            updateUserAvatar(savedUser);
        }

        // Load and display projects
        loadProjects();
    }

    private void handleToggleViewMode() {
        isListMode = !isListMode;
        SettingsManager.getInstance().setProjectViewMode(isListMode ? "list" : "cards");
        updateViewModeIcon();

        // Re-filter if search is active
        if (searchField != null && !searchField.getText().isEmpty()) {
            filterProjects(searchField.getText());
        } else {
            displayProjects(allProjects);
        }
    }

    private void updateViewModeIcon() {
        if (viewModeIcon != null) {
            viewModeIcon.setContent(isListMode ? VIEW_LIST_PATH : VIEW_CARDS_PATH);
        }
        if (viewModeButton != null && viewModeButton.getTooltip() != null) {
            viewModeButton.getTooltip().setText(isListMode ? "Vista: Lista" : "Vista: Tarjetas");
        }
    }

    private void handleLogin() {
        // Prevent login dialog if already logged in
        if (SettingsManager.getInstance().getGitUser() != null
                && !SettingsManager.getInstance().getGitUser().isEmpty()) {
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
                        gitIcon.setContent(
                                "M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12");
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
            icon.setContent(
                    "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z");
            icon.setFill(Color.WHITE);
            icon.setScaleX(1.2);
            icon.setScaleY(1.2);

            loginButton.setGraphic(icon);
        }
    }

    private void loadProjects() {
        allProjects = projectManager.loadProjects();
        logger.info("Loading {} projects", allProjects.size());
        sortProjects();
        displayProjects(allProjects);
    }

    private void handleSortProjects() {
        isSortAscending = !isSortAscending;
        sortProjects();

        // Update tooltip and icon to reflect current state
        if (sortProjectsButton != null) {
            // Update Tooltip
            if (sortProjectsButton.getTooltip() != null) {
                sortProjectsButton.getTooltip()
                        .setText(isSortAscending ? "Ordenar: Más antiguos primero" : "Ordenar: Más recientes primero");
            }

            // Update Icon
            if (sortProjectsButton.getGraphic() instanceof SVGPath) {
                SVGPath icon = (SVGPath) sortProjectsButton.getGraphic();
                icon.setContent(isSortAscending ? SORT_ASCENDING_PATH : SORT_DESCENDING_PATH);
            }
        }

        // Re-filter if search is active
        if (searchField != null && !searchField.getText().isEmpty()) {
            filterProjects(searchField.getText());
        } else {
            displayProjects(allProjects);
        }
    }

    private void sortProjects() {
        if (allProjects == null)
            return;

        allProjects.sort((p1, p2) -> {
            if (p1.getLastModified() == null || p2.getLastModified() == null)
                return 0;
            if (isSortAscending) {
                return p1.getLastModified().compareTo(p2.getLastModified());
            } else {
                return p2.getLastModified().compareTo(p1.getLastModified());
            }
        });
    }

    private void filterProjects(String query) {
        if (allProjects == null)
            return;

        if (query == null || query.trim().isEmpty()) {
            displayProjects(allProjects);
            return;
        }

        String lowerQuery = query.toLowerCase();
        List<Project> filtered = allProjects.stream()
                .filter(p -> p.getName().toLowerCase().contains(lowerQuery))
                .collect(java.util.stream.Collectors.toList());
        displayProjects(filtered);
    }

    private void displayProjects(List<Project> projects) {
        projectsGrid.getChildren().clear();

        if (isListMode) {
            projectsGrid.setHgap(0);
            projectsGrid.setVgap(10);
            projectsGrid.setAlignment(Pos.TOP_CENTER);
            for (Project project : projects) {
                projectsGrid.getChildren().add(createProjectListItem(project));
            }
        } else {
            projectsGrid.setHgap(20);
            projectsGrid.setVgap(20);
            projectsGrid.setAlignment(Pos.TOP_LEFT);
            for (Project project : projects) {
                projectsGrid.getChildren().add(createProjectCard(project));
            }
        }
    }

    private HBox createProjectListItem(Project project) {
        HBox item = new HBox(15);
        item.getStyleClass().add("project-list-item");
        item.setAlignment(Pos.CENTER_LEFT);
        item.setPadding(new Insets(10, 15, 10, 15));

        // Make full width
        if (projectsScrollPane != null) {
            item.prefWidthProperty().bind(projectsScrollPane.widthProperty().subtract(100));
        } else {
            item.setPrefWidth(800);
        }

        // Project icon
        ImageView iconView = new ImageView();
        iconView.setFitWidth(48);
        iconView.setFitHeight(48);
        iconView.setPreserveRatio(true);
        setProjectIcon(project, iconView);

        // Name and Description (Vertical)
        VBox infoBox = new VBox(5);
        infoBox.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(infoBox, Priority.ALWAYS);

        Label nameLabel = new Label(project.getName());
        nameLabel.getStyleClass().add("project-card-title");
        nameLabel.setStyle("-fx-font-size: 16px;"); // Slightly smaller than card title

        Label descLabel = new Label(project.getDescription());
        descLabel.getStyleClass().add("project-card-description");
        descLabel.setMaxHeight(40);

        infoBox.getChildren().addAll(nameLabel, descLabel);

        // Right side: Date + Actions
        HBox rightBox = new HBox(15);
        rightBox.setAlignment(Pos.CENTER_RIGHT);

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        Label dateLabel = new Label(project.getLastModified().format(formatter));
        dateLabel.getStyleClass().add("project-card-date");

        // Edit Button
        Button editBtn = new Button();
        SVGPath editIcon = new SVGPath();
        editIcon.setContent(
                "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        editIcon.setFill(Color.WHITE);
        editIcon.setScaleX(0.8);
        editIcon.setScaleY(0.8);
        editBtn.setGraphic(editIcon);
        editBtn.getStyleClass().add("icon-button");
        editBtn.setTooltip(new Tooltip("Editar"));
        editBtn.setOnAction(e -> {
            e.consume();
            NavigationManager.getInstance().showEditProject(project);
        });

        // Delete Button
        Button deleteBtn = new Button();
        SVGPath deleteIcon = new SVGPath();
        deleteIcon.setContent("M6 19c0 1.1.9 2 2 2h8c1.1 0 2-.9 2-2V7H6v12zM19 4h-3.5l-1-1h-5l-1 1H5v2h14V4z");
        deleteIcon.setFill(Color.WHITE);
        deleteIcon.setScaleX(0.8);
        deleteIcon.setScaleY(0.8);
        deleteBtn.setGraphic(deleteIcon);
        deleteBtn.getStyleClass().add("icon-button");
        deleteBtn.setTooltip(new Tooltip("Eliminar"));
        deleteBtn.setOnAction(e -> {
            e.consume();
            handleDeleteProject(project);
        });

        rightBox.getChildren().addAll(dateLabel, editBtn, deleteBtn);

        item.getChildren().addAll(iconView, infoBox, rightBox);

        // Interactions
        item.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                // Ignore clicks on buttons (handled by their actions)
                if (!(e.getTarget() instanceof Button) && !(e.getTarget() instanceof SVGPath)) {
                    handleProjectClick(project, item);
                }
            }
        });

        return item;
    }

    private Pane createProjectCard(Project project) {
        StackPane card = new StackPane();
        card.getStyleClass().add("project-card-modern");
        card.setPrefWidth(280);
        card.setPrefHeight(180);

        // Inner Container for Clipped Content (Image + Overlay + BottomBg)
        StackPane innerContent = new StackPane();
        innerContent.setPrefSize(280, 180);

        // Background Image (Blurred)
        ImageView bgView = new ImageView();
        bgView.setFitWidth(280);
        bgView.setFitHeight(180);
        bgView.setPreserveRatio(false);
        setProjectIcon(project, bgView);
        bgView.setEffect(new javafx.scene.effect.GaussianBlur(10));

        // Dark Overlay
        Rectangle overlay = new Rectangle(280, 180);
        overlay.setFill(Color.rgb(0, 0, 0, 0.6));

        // Bottom Background (Gray) - Reduced height to 75
        Rectangle bottomBg = new Rectangle(280, 75);
        bottomBg.setFill(Color.web("#252526"));
        StackPane.setAlignment(bottomBg, Pos.BOTTOM_CENTER);

        // Clip to rounded corners (Applied to inner container)
        Rectangle clip = new Rectangle(280, 180);
        clip.setArcWidth(30);
        clip.setArcHeight(30);
        innerContent.setClip(clip);

        // Content Container (Text + Icon) - Not clipped by inner container to allow
        // potential overlaps if needed,
        // but here it fits inside. Actually, text should be inside innerContent if we
        // want it to be clipped?
        // Usually text doesn't need clipping unless it overflows.
        // But the bottomBg IS inside innerContent, so it gets clipped.

        HBox content = new HBox(15);
        content.setAlignment(Pos.BOTTOM_LEFT);
        content.setPadding(new Insets(15));

        // Larger Icon
        ImageView icon = new ImageView();
        icon.setFitWidth(58);
        icon.setFitHeight(58);
        setProjectIcon(project, icon);

        // Text Info (Title + Description)
        VBox textInfo = new VBox(2);
        textInfo.setAlignment(Pos.BOTTOM_LEFT);

        Label nameLabel = new Label(project.getName());
        nameLabel.getStyleClass().add("project-card-modern-title");
        nameLabel.setWrapText(true);
        nameLabel.setMaxWidth(180);

        Label descLabel = new Label(project.getDescription());
        descLabel.getStyleClass().add("project-card-modern-subtitle");
        descLabel.setWrapText(true);
        descLabel.setMaxHeight(35);
        descLabel.setMaxWidth(180);

        textInfo.getChildren().addAll(nameLabel, descLabel);

        content.getChildren().addAll(icon, textInfo);

        // Add layers to inner content
        innerContent.getChildren().addAll(bgView, overlay, bottomBg);

        // Add inner content and text content to card
        // Note: 'content' (Text/Icon) is added after innerContent so it sits on top.
        // If we want 'content' to be clipped too, add it to innerContent.
        // Let's add it to innerContent to be safe with rounded corners.
        innerContent.getChildren().add(content);

        card.getChildren().add(innerContent);

        // Interactions
        card.setOnMouseClicked(e -> {
            if (e.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                handleProjectClick(project, card);
            }
        });

        // Context Menu
        card.setOnContextMenuRequested(e -> {
            ContextMenu menu = new ContextMenu();

            MenuItem editItem = new MenuItem("✎ Editar Proyecto");
            editItem.setOnAction(event -> NavigationManager.getInstance().showEditProject(project));
            menu.getItems().add(editItem);

            MenuItem exportItem = new MenuItem("⬇ Exportar ZIP");
            exportItem.setOnAction(event -> handleDownloadProject(project));
            menu.getItems().add(exportItem);

            MenuItem deleteItem = new MenuItem("🗑 Eliminar Proyecto");
            deleteItem.setOnAction(event -> handleDeleteProject(project));
            menu.getItems().add(deleteItem);

            menu.show(card, e.getScreenX(), e.getScreenY());
        });

        return card;
    }

    private void setProjectIcon(Project project, ImageView iconView) {
        try {
            // Check BP first, then RP
            Path iconPath = Paths.get(project.getRootPath(), "BP", "pack_icon.png");
            if (!Files.exists(iconPath)) {
                iconPath = Paths.get(project.getRootPath(), "RP", "pack_icon.png");
            }

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
    }

    private void handleProjectClick(Project project, Pane cardNode) {
        // Animation feedback
        javafx.animation.ScaleTransition st = new javafx.animation.ScaleTransition(javafx.util.Duration.millis(100),
                cardNode);
        st.setFromX(1.0);
        st.setFromY(1.0);
        st.setToX(0.95);
        st.setToY(0.95);
        st.setAutoReverse(true);
        st.setCycleCount(2);
        st.setOnFinished(ev -> {
            File projectDir = new File(project.getRootPath());
            if (!projectDir.exists()) {
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Proyecto no encontrado");
                alert.setContentText("La carpeta del proyecto no existe: " + project.getRootPath());
                alert.showAndWait();
                return;
            }

            if (rootStackPane != null) {
                Node overlay = LoadingSpinnerHelper.createLoadingOverlay("Cargando proyecto...", "code");
                rootStackPane.getChildren().add(overlay);
            }

            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> NavigationManager.getInstance().showEditor(project));
            pause.play();
        });
        st.play();
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
            String path = selectedDirectory.getAbsolutePath();

            // Check if project is already known to avoid duplicates
            java.util.Optional<Project> existingProject = allProjects.stream()
                    .filter(p -> p.getRootPath().equals(path))
                    .findFirst();

            // Show Loading Overlay
            if (rootStackPane != null) {
                Node overlay = LoadingSpinnerHelper.createLoadingOverlay("Cargando proyecto...", "code");
                rootStackPane.getChildren().add(overlay);
            }

            PauseTransition pause = new PauseTransition(Duration.millis(100));
            pause.setOnFinished(event -> {
                if (existingProject.isPresent()) {
                    NavigationManager.getInstance().showEditor(existingProject.get());
                } else {
                    // Create new project entry and save it
                    Project project = new Project(selectedDirectory.getName(), "Imported Project", path);
                    projectManager.addProject(project);
                    NavigationManager.getInstance().showEditor(project);
                }
            });
            pause.play();
        }
    }

    private void handleOpenPixelArtEditor() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/fxml/PixelArtEditor.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Pixel Art Editor - Standalone");
            stage.setScene(new Scene(root));

            // Set Application Icon
            try {
                stage.getIcons().add(new Image(getClass().getResourceAsStream("/images/addoncreator.png")));
            } catch (Exception ex) {
                // Ignore if icon not found
            }

            stage.setWidth(1000);
            stage.setHeight(800);

            // Enable Standalone Mode (shows Settings button)
            PixelArtEditorController controller = loader.getController();
            controller.setStandaloneMode(true);

            // Handle Unsaved Changes on Close
            stage.setOnCloseRequest(e -> {
                if (controller.isDirty()) {
                    Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
                    alert.setTitle("Cambios sin guardar");
                    alert.setHeaderText("Cambios sin guardar detectados");
                    alert.setContentText("¿Estás seguro de que quieres salir? Perderás los cambios no guardados.");
                    
                    // Apply CSS
                    DialogPane dialogPane = alert.getDialogPane();
                    dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
                    dialogPane.getStyleClass().add("dark-dialog");
                    
                    java.util.Optional<ButtonType> result = alert.showAndWait();
                    if (!result.isPresent() || result.get() != ButtonType.OK) {
                        e.consume();
                    }
                }
            });

            stage.show();
        } catch (Exception e) {
            logger.error("Failed to open Pixel Art Editor", e);
            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Error");
            alert.setHeaderText("Error al abrir Editor PixelArt");
            alert.setContentText(e.getMessage());
            alert.showAndWait();
        }
    }

    private void handleBlockbench() {
        String path = SettingsManager.getInstance().getBlockbenchPath();

        if (path == null || path.isEmpty() || !new File(path).exists()) {
            Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
            alert.setTitle("Configurar Blockbench");
            alert.setHeaderText("Blockbench no está configurado");
            alert.setContentText("Selecciona el ejecutable de Blockbench para continuar.");

            alert.showAndWait().ifPresent(response -> {
                if (response == ButtonType.OK) {
                    FileChooser fileChooser = new FileChooser();
                    fileChooser.setTitle("Seleccionar Ejecutable de Blockbench");
                    fileChooser.getExtensionFilters()
                            .add(new FileChooser.ExtensionFilter("Ejecutables", "*.exe", "*.app", "*.sh"));
                    File file = fileChooser.showOpenDialog(pixelArtButton.getScene().getWindow());

                    if (file != null) {
                        SettingsManager.getInstance().setBlockbenchPath(file.getAbsolutePath());
                        // Try again recursively
                        handleBlockbench();
                    }
                }
            });
            return;
        }

        // Launch Blockbench
        new Thread(() -> {
            try {
                if (java.awt.Desktop.isDesktopSupported()
                        && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.OPEN)) {
                    java.awt.Desktop.getDesktop().open(new File(path));
                } else {
                    // Fallback using ProcessBuilder
                    new ProcessBuilder(path).start();
                }
            } catch (java.io.IOException ex) {
                javafx.application.Platform.runLater(() -> {
                    logger.error("Error al abrir Blockbench", ex);
                    Alert alert = new Alert(Alert.AlertType.ERROR);
                    alert.setTitle("Error");
                    alert.setHeaderText("Error al iniciar Blockbench");
                    alert.setContentText(ex.getMessage());
                    alert.showAndWait();
                });
            }
        }).start();
    }

    private void handleDownloadProject(Project project) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Guardar Proyecto como ZIP");
        fileChooser.setInitialFileName(project.getName() + ".zip");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("ZIP files (*.zip)", "*.zip"));

        File destFile = fileChooser.showSaveDialog(projectsGrid.getScene().getWindow());

        if (destFile != null) {
            try {
                // Show loading
                Alert loadingAlert = new Alert(Alert.AlertType.INFORMATION);
                loadingAlert.setTitle("Exportando");
                loadingAlert.setHeaderText(null);
                loadingAlert.setContentText("Comprimiendo proyecto...");
                loadingAlert.show();

                Path sourceDir = Paths.get(project.getRootPath());
                ZipUtils.zipDirectory(sourceDir, destFile.toPath());

                loadingAlert.close();

                Alert alert = new Alert(Alert.AlertType.INFORMATION);
                alert.setTitle("Exportación Exitosa");
                alert.setHeaderText(null);
                alert.setContentText("El proyecto se ha exportado correctamente a:\n" + destFile.getAbsolutePath());
                alert.showAndWait();

            } catch (Exception e) {
                logger.error("Failed to zip project", e);
                Alert alert = new Alert(Alert.AlertType.ERROR);
                alert.setTitle("Error");
                alert.setHeaderText("Error al exportar proyecto");
                alert.setContentText(e.getMessage());
                alert.showAndWait();
            }
        }
    }

    private void handleSettings() {
        NavigationManager.getInstance().showSettingsModal();
    }
}