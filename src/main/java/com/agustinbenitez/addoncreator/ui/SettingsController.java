package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.SettingsManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.TextField;
import javafx.stage.FileChooser;
import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Controller for the Settings screen
 */
public class SettingsController {

    private static final Logger logger = LoggerFactory.getLogger(SettingsController.class);

    @FXML
    private ComboBox<String> languageComboBox;

    @FXML
    private ComboBox<String> windowSizeComboBox;

    @FXML
    private TextField blockbenchPathField;

    @FXML
    private Button btnBrowseBlockbench;

    @FXML
    private Button btnClose;

    @FXML
    public void initialize() {
        logger.info("Initializing SettingsController");

        // Setup Language Combo
        languageComboBox.getItems().addAll("English", "Español");
        String currentLang = SettingsManager.getInstance().getLanguage();
        // Simple mapping, improve if needed
        if ("Español".equals(currentLang)) {
            languageComboBox.getSelectionModel().select("Español");
        } else {
            languageComboBox.getSelectionModel().select("English");
        }

        languageComboBox.setOnAction(e -> {
            String selected = languageComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                SettingsManager.getInstance().setLanguage(selected);
                logger.info("Language changed to: {}", selected);
            }
        });

        // Setup Window Size Combo
        windowSizeComboBox.getItems().addAll(
            "800x600",
            "1024x768",
            "1280x720",
            "1366x768",
            "1600x900",
            "1920x1080",
            "Maximized"
        );

        updateWindowSizeSelection();

        windowSizeComboBox.setOnAction(e -> {
            String selected = windowSizeComboBox.getSelectionModel().getSelectedItem();
            if (selected != null) {
                handleWindowSizeChange(selected);
            }
        });

        // Setup Blockbench Path
        String bbPath = SettingsManager.getInstance().getBlockbenchPath();
        if (bbPath != null) {
            blockbenchPathField.setText(bbPath);
        }
        
        btnBrowseBlockbench.setOnAction(e -> {
            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Seleccionar Ejecutable de Blockbench");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Ejecutables", "*.exe", "*.app", "blockbench"));
            File exe = fileChooser.showOpenDialog(btnClose.getScene().getWindow());
            
            if (exe != null) {
                String path = exe.getAbsolutePath();
                SettingsManager.getInstance().setBlockbenchPath(path);
                blockbenchPathField.setText(path);
                logger.info("Blockbench path set to: {}", path);
            }
        });

        // Setup Close Button
        btnClose.setOnAction(e -> closeWindow());
    }

    private void updateWindowSizeSelection() {
        if (SettingsManager.getInstance().isWindowMaximized()) {
            windowSizeComboBox.getSelectionModel().select("Maximized");
        } else {
            double w = SettingsManager.getInstance().getWindowWidth();
            double h = SettingsManager.getInstance().getWindowHeight();
            String size = (int)w + "x" + (int)h;
            if (windowSizeComboBox.getItems().contains(size)) {
                windowSizeComboBox.getSelectionModel().select(size);
            } else {
                // If custom size, maybe add it or select closest? 
                // For now, just leave unselected or add it temporarily
                windowSizeComboBox.getItems().add(size);
                windowSizeComboBox.getSelectionModel().select(size);
            }
        }
    }

    private void handleWindowSizeChange(String selected) {
        if ("Maximized".equals(selected)) {
            SettingsManager.getInstance().setWindowMaximized(true);
        } else {
            SettingsManager.getInstance().setWindowMaximized(false);
            try {
                String[] parts = selected.split("x");
                if (parts.length == 2) {
                    double w = Double.parseDouble(parts[0]);
                    double h = Double.parseDouble(parts[1]);
                    SettingsManager.getInstance().setWindowWidth(w);
                    SettingsManager.getInstance().setWindowHeight(h);
                }
            } catch (NumberFormatException e) {
                logger.error("Invalid window size format: {}", selected);
            }
        }
        logger.info("Window size changed to: {}", selected);

        // Apply changes immediately to the main window
        if (btnClose.getScene() != null && btnClose.getScene().getWindow() != null) {
            javafx.stage.Window settingsWindow = btnClose.getScene().getWindow();
            if (settingsWindow instanceof javafx.stage.Stage) {
                javafx.stage.Window owner = ((javafx.stage.Stage) settingsWindow).getOwner();
                if (owner instanceof javafx.stage.Stage) {
                    javafx.stage.Stage mainStage = (javafx.stage.Stage) owner;
                    
                    if ("Maximized".equals(selected)) {
                        mainStage.setMaximized(true);
                    } else {
                        mainStage.setMaximized(false);
                        try {
                            String[] parts = selected.split("x");
                            if (parts.length == 2) {
                                double w = Double.parseDouble(parts[0]);
                                double h = Double.parseDouble(parts[1]);
                                mainStage.setWidth(w);
                                mainStage.setHeight(h);
                                mainStage.centerOnScreen();
                            }
                        } catch (Exception e) {
                            logger.error("Error applying window size", e);
                        }
                    }
                }
            }
        }
    }

    private void closeWindow() {
        if (btnClose.getScene() != null && btnClose.getScene().getWindow() != null) {
            btnClose.getScene().getWindow().hide();
        }
    }
}
