package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.ProjectGenerator;
import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Path;

/**
 * Controller for the main application window
 * 
 * @author Agustín Benítez
 */
public class MainWindowController {

    private static final Logger logger = LoggerFactory.getLogger(MainWindowController.class);

    @FXML
    private TextField addonNameField;

    @FXML
    private TextArea descriptionArea;

    @FXML
    private TextField folderPathField;

    @FXML
    private Button browseFolderButton;

    @FXML
    private Button generateButton;

    @FXML
    private TextField versionMajorField;

    @FXML
    private TextField versionMinorField;

    @FXML
    private TextField versionPatchField;

    @FXML
    private TextField minEngineMajorField;

    @FXML
    private TextField minEngineMinorField;

    @FXML
    private TextField minEnginePatchField;

    @FXML
    private TextArea logArea;

    private File selectedFolder;

    @FXML
    public void initialize() {
        logger.info("Initializing MainWindowController");

        // Set default values
        versionMajorField.setText("1");
        versionMinorField.setText("0");
        versionPatchField.setText("0");

        minEngineMajorField.setText("1");
        minEngineMinorField.setText("20");
        minEnginePatchField.setText("0");

        // Setup button actions
        browseFolderButton.setOnAction(e -> handleBrowseFolder());
        generateButton.setOnAction(e -> handleGenerate());

        log("Addon Creator initialized - Ready to create addons!");
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

            log("Generating addon: " + addonName);
            log("Location: " + rootPath);

            // Generate the project
            ProjectGenerator.generateProject(rootPath, addonName, description);

            log("✓ Addon generated successfully!");
            log("✓ BP folder created at: " + rootPath.resolve("BP"));
            log("✓ RP folder created at: " + rootPath.resolve("RP"));
            log("✓ Manifests generated with unique UUIDs");

            // Register project
            Project newProject = new Project(addonName, rootPath.toString(), description);
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
        logArea.appendText(message + "\n");
        logger.info(message);
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
        alert.setTitle("Success");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
