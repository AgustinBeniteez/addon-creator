package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.SettingsManager;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
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

        // Setup Close Button
        btnClose.setOnAction(e -> closeWindow());
    }

    private void closeWindow() {
        if (btnClose.getScene() != null && btnClose.getScene().getWindow() != null) {
            btnClose.getScene().getWindow().hide();
        }
    }
}
