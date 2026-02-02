package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;

public class PaintingCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(PaintingCreatorController.class);

    @FXML private TextField nameField;
    @FXML private ComboBox<String> dimensionCombo;
    @FXML private Button createButton;

    private Project project;
    private File createdFile;

    @FXML
    public void initialize() {
        dimensionCombo.getItems().addAll(
            "1x1 (16x16)", 
            "2x1 (32x16)", 
            "1x2 (16x32)", 
            "2x2 (32x32)", 
            "4x2 (64x32)", 
            "4x3 (64x48)", 
            "4x4 (64x64)"
        );
        dimensionCombo.getSelectionModel().select(0);
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public File getCreatedFile() {
        return createdFile;
    }

    @FXML
    private void handleCancel() {
        close();
    }

    @FXML
    private void handleCreate() {
        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Error", "Please enter a file name.");
            return;
        }
        
        // Sanitize name
        name = name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
        
        String dimSelection = dimensionCombo.getValue();
        int width = 16;
        int height = 16;
        
        if (dimSelection.contains("1x1")) { width = 16; height = 16; }
        else if (dimSelection.contains("2x1")) { width = 32; height = 16; }
        else if (dimSelection.contains("1x2")) { width = 16; height = 32; }
        else if (dimSelection.contains("2x2")) { width = 32; height = 32; }
        else if (dimSelection.contains("4x2")) { width = 64; height = 32; }
        else if (dimSelection.contains("4x3")) { width = 64; height = 48; }
        else if (dimSelection.contains("4x4")) { width = 64; height = 64; }
        
        try {
            if (project != null) {
                Path root = Paths.get(project.getRootPath());
                Path paintingsDir = root.resolve("RP/textures/painting");
                if (!Files.exists(paintingsDir)) {
                    Files.createDirectories(paintingsDir);
                }
                
                Path file = paintingsDir.resolve(name + ".png");
                if (Files.exists(file)) {
                     showAlert("Error", "File already exists: " + name + ".png");
                     return;
                }
                
                // Create blank image
                BufferedImage bImg = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
                // Maybe fill with transparent white or just transparent? Transparent is standard.
                
                ImageIO.write(bImg, "png", file.toFile());
                
                createdFile = file.toFile();
                close();
            }
        } catch (Exception e) {
            logger.error("Failed to create painting", e);
            showAlert("Error", "Failed to create painting: " + e.getMessage());
        }
    }

    private void close() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.initOwner(createButton.getScene().getWindow());
        alert.showAndWait();
    }
}
