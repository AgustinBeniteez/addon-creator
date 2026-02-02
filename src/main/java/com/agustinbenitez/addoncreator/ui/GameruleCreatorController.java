package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class GameruleCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(GameruleCreatorController.class);

    @FXML private TextField identifierField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private TextField defaultValueField;
    @FXML private CheckBox defaultBoolCheck;
    @FXML private Button createButton;

    private Project project;

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("Boolean", "Integer");
        typeCombo.getSelectionModel().selectFirst();
        
        typeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if ("Boolean".equals(newVal)) {
                defaultValueField.setVisible(false);
                defaultValueField.setManaged(false);
                defaultBoolCheck.setVisible(true);
                defaultBoolCheck.setManaged(true);
            } else {
                defaultValueField.setVisible(true);
                defaultValueField.setManaged(true);
                defaultBoolCheck.setVisible(false);
                defaultBoolCheck.setManaged(false);
            }
        });
        
        // Trigger initial state
        typeCombo.getSelectionModel().select("Boolean");
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null) {
            String ns = findNamespace(project);
            if (ns != null && identifierField.getText().isEmpty()) {
                identifierField.setText(ns + ":");
                identifierField.positionCaret(ns.length() + 1);
            }
        }
    }

    private String findNamespace(Project proj) {
        if (proj.getEntities() != null && !proj.getEntities().isEmpty()) {
            for (String e : proj.getEntities()) {
                if (e.contains(":"))
                    return e.split(":")[0];
            }
        }
        if (proj.getName() != null) {
            return proj.getName().toLowerCase().replaceAll("[^a-z0-9_]", "_");
        }
        return "namespace";
    }

    @FXML
    private void handleCancel() {
        close();
    }

    @FXML
    private void handleCreate() {
        String ruleId = identifierField.getText().trim();
        if (ruleId.isEmpty()) {
            showAlert("Error", "Gamerule Identifier is required.");
            return;
        }

        try {
            createGameruleScript(ruleId);
            close();
        } catch (Exception e) {
            logger.error("Failed to create gamerule", e);
            showAlert("Error", "Failed to create gamerule: " + e.getMessage());
        }
    }

    private void createGameruleScript(String ruleId) throws IOException {
        Path scriptsPath = Paths.get(project.getRootPath(), "BP/scripts");
        Files.createDirectories(scriptsPath);
        
        String fileName = "gamerule_" + ruleId + ".js";
        Path scriptFile = scriptsPath.resolve(fileName);
        
        boolean isBool = "Boolean".equals(typeCombo.getValue());
        String defaultVal = isBool ? String.valueOf(defaultBoolCheck.isSelected()) : defaultValueField.getText();
        if (defaultVal.isEmpty()) defaultVal = "0";

        StringBuilder script = new StringBuilder();
        script.append("import { world, system } from '@minecraft/server';\n\n");
        script.append("// Gamerule: ").append(ruleId).append("\n");
        script.append("// Type: ").append(typeCombo.getValue()).append("\n\n");
        
        script.append("const RULE_ID = '").append(ruleId).append("';\n\n");
        
        script.append("// Initialize rule if not exists\n");
        script.append("system.run(() => {\n");
        script.append("    if (world.getDynamicProperty(RULE_ID) === undefined) {\n");
        if (isBool) {
            script.append("        world.setDynamicProperty(RULE_ID, ").append(defaultVal).append(");\n");
        } else {
            script.append("        world.setDynamicProperty(RULE_ID, ").append(Integer.parseInt(defaultVal)).append(");\n");
        }
        script.append("        console.log(`Initialized gamerule ${RULE_ID} to ").append(defaultVal).append("`);\n");
        script.append("    }\n");
        script.append("});\n\n");
        
        script.append("// Helper function to get rule value\n");
        script.append("export function get").append(capitalize(ruleId)).append("() {\n");
        script.append("    return world.getDynamicProperty(RULE_ID);\n");
        script.append("}\n\n");
        
        script.append("// Command to set rule: /scriptevent setrule ").append(ruleId).append(" <value>\n");
        script.append("system.afterEvents.scriptEventReceive.subscribe((event) => {\n");
        script.append("    if (event.id === 'setrule' && event.message.startsWith(RULE_ID)) {\n");
        script.append("        const value = event.message.split(' ')[1];\n");
        if (isBool) {
            script.append("        const boolValue = value === 'true';\n");
            script.append("        world.setDynamicProperty(RULE_ID, boolValue);\n");
        } else {
             script.append("        const intValue = parseInt(value);\n");
             script.append("        if (!isNaN(intValue)) world.setDynamicProperty(RULE_ID, intValue);\n");
        }
        script.append("        world.sendMessage(`Gamerule ${RULE_ID} updated.`);\n");
        script.append("    }\n");
        script.append("});\n");

        Files.write(scriptFile, script.toString().getBytes());
        
        // Append to main.js
        Path mainJs = scriptsPath.resolve("main.js");
        if (Files.exists(mainJs)) {
             String importLine = "\nimport './" + fileName + "';";
             Files.write(mainJs, importLine.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        } else {
            Files.write(mainJs, ("import './" + fileName + "';").getBytes());
        }
        
        showAlert("Success", "Gamerule script created: " + fileName);
    }
    
    private String capitalize(String str) {
        if (str == null || str.isEmpty()) return str;
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private void close() {
        Stage stage = (Stage) createButton.getScene().getWindow();
        stage.close();
    }
}
