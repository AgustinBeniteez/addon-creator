package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.AddonTemplate;
import com.agustinbenitez.addoncreator.core.GitManager;
import com.agustinbenitez.addoncreator.core.ProjectGenerator;
import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Controller for the main application window
 * 
 * @author Agustín Benítez
 */
public class MainWindowController {

    private static final Logger logger = LoggerFactory.getLogger(MainWindowController.class);

    @FXML
    private Button btnBack;

    @FXML
    private Label titleLabel;

    @FXML
    private TextField addonNameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private ImageView logoImageView;
    
    @FXML
    private Button btnUploadLogo;

    @FXML
    private TextField versionMajorField;

    @FXML
    private TextField versionMinorField;

    @FXML
    private TextField versionPatchField;

    @FXML
    private ComboBox<String> projectTypeCombo;

    @FXML
    private TextField folderPathField;

    @FXML
    private Button browseFolderButton;

    @FXML
    private CheckBox gitConnectCheck;

    @FXML
    private TextField gitRepoField;

    @FXML
    private Button generateButton;

    @FXML
    private TextArea logArea;

    @FXML
    private FlowPane templatesContainer;
    
    @FXML
    private VBox templatesSection;

    private File selectedFolder;
    private File selectedLogoFile;
    private Project editingProject;
    private List<AddonTemplate> allTemplates;
    private AddonTemplate selectedTemplate;

    @FXML
    public void initialize() {
        logger.info("Initializing MainWindowController");

        // Set default values
        versionMajorField.setText("1");
        versionMinorField.setText("0");
        versionPatchField.setText("0");

        // Initialize Project Types
        projectTypeCombo.getItems().addAll(
            "Resource Pack",
            "Behavior Pack",
            "Both (Addon)"
        );
        projectTypeCombo.getSelectionModel().select("Both (Addon)");

        // Git repo field is disabled unless checkbox is checked
        gitRepoField.disableProperty().bind(gitConnectCheck.selectedProperty().not());

        // Setup button actions
        if (btnBack != null) {
            btnBack.setOnAction(e -> handleBack());
        }
        browseFolderButton.setOnAction(e -> handleBrowseFolder());
        btnUploadLogo.setOnAction(e -> handleUploadLogo());
        generateButton.setOnAction(e -> handleGenerate());

        // Load default logo if available (optional)
        try {
             Image defaultLogo = new Image(getClass().getResourceAsStream("/images/preset_logo.png"));
             if (!defaultLogo.isError()) {
                 logoImageView.setImage(defaultLogo);
             }
        } catch (Exception e) {
            // Ignore if default logo missing
        }

        // Initialize Templates
        initializeTemplates();
        if (templatesContainer != null) {
            renderTemplates("All");
        }

        log("Addon Creator initialized - Ready to create addons!");
    }

    private void initializeTemplates() {
        allTemplates = new ArrayList<>();
        // "herramientas, block, HUD, texture pack"
        allTemplates.add(new AddonTemplate("tools", "Mining Tools Addon", "Adds new pickaxes and drills.", "Both (Addon)", "Tools", null));
        allTemplates.add(new AddonTemplate("blocks", "Custom Blocks", "Adds decorative blocks.", "Both (Addon)", "Blocks", null));
        allTemplates.add(new AddonTemplate("hud", "Custom HUD", "Modifies the game HUD.", "Resource Pack", "Utility", null));
        allTemplates.add(new AddonTemplate("texture", "Texture Pack", "Changes vanilla textures.", "Resource Pack", "Utility", null));
        
        // Add a few more for the mockup feel
        allTemplates.add(new AddonTemplate("mobs", "Knight Armor Mod", "Adds new armor sets.", "Both (Addon)", "Armor", null));
        allTemplates.add(new AddonTemplate("magic", "Magic Spells", "Learn powerful spells.", "Both (Addon)", "Magic", null));
    }

    private void renderTemplates(String categoryFilter) {
        templatesContainer.getChildren().clear();

        List<AddonTemplate> filtered = allTemplates.stream()
            .filter(t -> categoryFilter.equals("All") || t.getCategory().equalsIgnoreCase(categoryFilter))
            .collect(Collectors.toList());

        for (AddonTemplate template : filtered) {
            VBox card = createTemplateCard(template);
            templatesContainer.getChildren().add(card);
        }
    }

    private VBox createTemplateCard(AddonTemplate template) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(200);
        card.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0);");
        
        // Placeholder Icon
        HBox iconContainer = new HBox();
        iconContainer.setAlignment(Pos.CENTER_LEFT);
        Rectangle placeholderIcon = new Rectangle(40, 40, Color.web("#3c3c3c"));
        placeholderIcon.setArcWidth(10);
        placeholderIcon.setArcHeight(10);
        
        VBox textContainer = new VBox(5);
        Label title = new Label(template.getName());
        title.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        title.setWrapText(true);
        
        Label desc = new Label(template.getDescription());
        desc.setStyle("-fx-text-fill: #888; -fx-font-size: 11px;");
        desc.setWrapText(true);
        desc.setPrefHeight(40); // Fixed height for alignment
        
        textContainer.getChildren().addAll(title, desc);
        
        HBox header = new HBox(10);
        header.getChildren().addAll(placeholderIcon, textContainer);
        
        Button useBtn = new Button("Use Template");
        useBtn.setMaxWidth(Double.MAX_VALUE);
        useBtn.getStyleClass().add("button-primary");
        useBtn.setStyle("-fx-background-color: #0e639c; -fx-text-fill: white; -fx-font-size: 11px;");
        
        useBtn.setOnAction(e -> selectTemplate(template));

        card.getChildren().addAll(header, useBtn);
        
        // Hover effect
        card.setOnMouseEntered(e -> card.setStyle("-fx-background-color: #333; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 0);"));
        card.setOnMouseExited(e -> {
            if (selectedTemplate != template) {
                card.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 8; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 0);");
            } else {
                card.setStyle("-fx-background-color: #333; -fx-background-radius: 8; -fx-border-color: #007acc; -fx-border-width: 2;");
            }
        });

        // Selection style
        if (selectedTemplate == template) {
            card.setStyle("-fx-background-color: #333; -fx-background-radius: 8; -fx-border-color: #007acc; -fx-border-width: 2;");
        }

        return card;
    }

    private void selectTemplate(AddonTemplate template) {
        this.selectedTemplate = template;
        
        // Update UI to reflect selection
        projectTypeCombo.setValue(template.getProjectType());
        
        // Optionally update description if empty
        if (descriptionArea.getText().isEmpty()) {
            descriptionArea.setText(template.getDescription());
        }
        
        // Re-render to show selection state
        renderTemplates("All"); // Or keep current filter
        
        log("Selected template: " + template.getName());
    }

    @FXML
    private void handleBack() {
        NavigationManager.getInstance().showHomeScreen();
    }

    @FXML
    private void handleUploadLogo() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Project Logo");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(btnUploadLogo.getScene().getWindow());
        if (file != null) {
            selectedLogoFile = file;
            try {
                Image image = new Image(file.toURI().toString());
                logoImageView.setImage(image);
                log("Selected logo: " + file.getName());
            } catch (Exception e) {
                log("Error loading image: " + e.getMessage());
            }
        }
    }

    @FXML
    private void handleBrowseFolder() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle("Select Addon Root Folder");

        if (selectedFolder != null && selectedFolder.exists()) {
            chooser.setInitialDirectory(selectedFolder);
        }

        File folder = chooser.showDialog(browseFolderButton.getScene().getWindow());

        if (folder != null) {
            selectedFolder = folder;
            folderPathField.setText(folder.getAbsolutePath());
            log("Selected folder: " + folder.getAbsolutePath());
        }
    }

    public void setProjectToEdit(Project project) {
        this.editingProject = project;
        this.selectedFolder = new File(project.getRootPath());
        
        // Update UI for Edit Mode
        if (titleLabel != null) titleLabel.setText("Edit Project");
        addonNameField.setText(project.getName());
        descriptionArea.setText(project.getDescription());
        folderPathField.setText(project.getRootPath());
        
        // Hide/Disable immutable fields
        if (projectTypeCombo != null) {
            projectTypeCombo.getParent().setVisible(false);
            projectTypeCombo.getParent().setManaged(false);
        }
        
        if (folderPathField != null) {
            folderPathField.getParent().getParent().setVisible(false);
            folderPathField.getParent().getParent().setManaged(false);
        }
        
        // Hide Templates Section in Edit Mode
        if (templatesSection != null) {
            templatesSection.setVisible(false);
            templatesSection.setManaged(false);
        }
        
        generateButton.setText("Save Changes");
        
        // Load existing logo
        try {
            Path iconPath = selectedFolder.toPath().resolve("BP/pack_icon.png");
            if (Files.exists(iconPath)) {
                Image icon = new Image(iconPath.toUri().toString());
                logoImageView.setImage(icon);
            }
        } catch (Exception e) {
            log("Failed to load existing logo: " + e.getMessage());
        }
    }

    @FXML
    private void handleGenerate() {
        // Validate inputs
        if (!validateInputs()) {
            return;
        }

        try {
            String addonName = addonNameField.getText().trim();
            String description = descriptionArea.getText().trim();
            Path rootPath = selectedFolder.toPath();
            
            // If Editing, just update metadata and logo
            if (editingProject != null) {
                log("Updating project: " + addonName);
                
                // Update Logo if changed
                if (selectedLogoFile != null) {
                    Path packIconPathBP = rootPath.resolve("BP/pack_icon.png");
                    Path packIconPathRP = rootPath.resolve("RP/pack_icon.png");
                    Files.copy(selectedLogoFile.toPath(), packIconPathBP, StandardCopyOption.REPLACE_EXISTING);
                    Files.copy(selectedLogoFile.toPath(), packIconPathRP, StandardCopyOption.REPLACE_EXISTING);
                    log("✓ Logo updated");
                }
                
                // Update Project Metadata
                editingProject.setName(addonName);
                editingProject.setDescription(description);
                // Note: Not updating rootPath as it is fixed in edit mode
                
                ProjectManager projectManager = new ProjectManager();
                projectManager.updateProject(editingProject);
                
                log("✓ Project updated successfully!");
                
                // Navigate back
                NavigationManager.getInstance().showHomeScreen();
                return;
            }

            String projectType = projectTypeCombo.getValue();
            boolean initGit = gitConnectCheck.isSelected();
            String gitRemote = gitRepoField.getText().trim();
            
            // Build version string
            String version = String.format("%s.%s.%s", 
                versionMajorField.getText().trim(),
                versionMinorField.getText().trim(),
                versionPatchField.getText().trim()
            );

            log("Generating project: " + addonName);
            log("Type: " + projectType);
            log("Version: " + version);
            log("Location: " + rootPath);
            if (selectedTemplate != null) {
                log("Using Template: " + selectedTemplate.getName());
            }

            // Generate the project structure
            // Note: ProjectGenerator currently generates both BP and RP.
            // Future improvement: Pass projectType to generate only what's needed.
            ProjectGenerator.generateProject(rootPath, addonName, description);
            
            // Handle Template Generation (Simple File Stubs for now)
            if (selectedTemplate != null) {
                try {
                    if (selectedTemplate.getId().equals("tools")) {
                         // Add a sample pickaxe JSON
                         ProjectGenerator.createItemFolder(rootPath);
                         // TODO: Write actual JSON content
                         log("✓ Added sample tool files");
                    } else if (selectedTemplate.getId().equals("blocks")) {
                         // Add a sample block JSON
                         ProjectGenerator.createBlockFolder(rootPath);
                         log("✓ Added sample block files");
                    }
                } catch (Exception e) {
                    logger.error("Failed to apply template", e);
                    log("⚠ Failed to apply template files: " + e.getMessage());
                }
            }
            
            // Handle Logo Copy
            if (selectedLogoFile != null) {
                Path packIconPathBP = rootPath.resolve("BP/pack_icon.png");
                Path packIconPathRP = rootPath.resolve("RP/pack_icon.png");
                Files.copy(selectedLogoFile.toPath(), packIconPathBP, StandardCopyOption.REPLACE_EXISTING);
                Files.copy(selectedLogoFile.toPath(), packIconPathRP, StandardCopyOption.REPLACE_EXISTING);
                log("✓ Logo applied");
            }

            log("✓ Project generated successfully!");

            // Initialize Git if requested
            if (initGit) {
                try {
                    GitManager gitManager = new GitManager();
                    gitManager.initRepository(rootPath.toFile());
                    log("✓ Git repository initialized");
                    
                    if (!gitRemote.isEmpty()) {
                        gitManager.addRemote("origin", gitRemote);
                        log("✓ Remote 'origin' added: " + gitRemote);
                    }
                    
                    // Initial commit
                    gitManager.addAll();
                    gitManager.commit("Initial commit: " + addonName + " v" + version);
                    log("✓ Initial commit created");
                    
                } catch (Exception e) {
                    logger.error("Git initialization failed", e);
                    log("⚠ Git initialization failed: " + e.getMessage());
                }
            }

            // Register project
            Project newProject = new Project(addonName, rootPath.toString(), description);
            // TODO: Set version and type in Project model if supported in future
            
            ProjectManager projectManager = new ProjectManager();
            projectManager.addProject(newProject);

            // Auto-Navigate to Editor
            NavigationManager.getInstance().showEditor(newProject);

        } catch (Exception e) {
            logger.error("Failed to generate addon", e);
            log("✗ Error: " + e.getMessage());
            showError("Failed to generate addon", e.getMessage());
        }
    }

    private boolean validateInputs() {
        if (addonNameField.getText().trim().isEmpty()) {
            showError("Validation Error", "Please enter an addon name");
            return false;
        }

        if (descriptionArea.getText().trim().isEmpty()) {
            showError("Validation Error", "Please enter a description");
            return false;
        }

        if (selectedFolder == null) {
            showError("Validation Error", "Please select a folder");
            return false;
        }

        return true;
    }

    private void log(String message) {
        if (logArea != null) {
            logArea.appendText(message + "\n");
        }
        logger.info(message);
    }

    private void showError(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML private void handleFilterAll() { renderTemplates("All"); }
    @FXML private void handleFilterTools() { renderTemplates("Tools"); }
    @FXML private void handleFilterBlocks() { renderTemplates("Blocks"); }
    @FXML private void handleFilterArmor() { renderTemplates("Armor"); }
    @FXML private void handleFilterMagic() { renderTemplates("Magic"); }
    @FXML private void handleFilterUtility() { renderTemplates("Utility"); }
}
