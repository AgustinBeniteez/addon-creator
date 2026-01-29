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
    private Button btnBack;

    private Runnable backAction;

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

        // Setup Back Button
        btnBack.setOnAction(e -> {
            if (backAction != null) {
                backAction.run();
            } else {
                logger.warn("No back action defined");
                NavigationManager.getInstance().showHomeScreen(); // Fallback
            }
        });
    }

    public void setBackAction(Runnable backAction) {
        this.backAction = backAction;
    }
}
