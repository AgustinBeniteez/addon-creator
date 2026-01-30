package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.GitManager;
import com.agustinbenitez.addoncreator.core.ProjectGenerator;
import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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

    private File selectedFolder;
    private File selectedLogoFile;

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

        log("Addon Creator initialized - Ready to create addons!");
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

            // Generate the project structure
            // Note: ProjectGenerator currently generates both BP and RP.
            // Future improvement: Pass projectType to generate only what's needed.
            ProjectGenerator.generateProject(rootPath, addonName, description);
            
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
}
