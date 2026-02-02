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

public class CommandCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(CommandCreatorController.class);

    @FXML private TextField nameField;
    @FXML private TextField descriptionField;
    @FXML private ComboBox<String> permissionCombo;
    @FXML private CheckBox adminOnlyCheck;
    @FXML private Button createButton;

    private Project project;

    @FXML
    public void initialize() {
        permissionCombo.getItems().addAll("All Players", "Operators Only");
        permissionCombo.getSelectionModel().selectFirst();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    @FXML
    private void handleCancel() {
        close();
    }

    @FXML
    private void handleCreate() {
        String commandName = nameField.getText().trim();
        if (commandName.isEmpty()) {
            showAlert("Error", "Command name is required.");
            return;
        }
        if (commandName.contains(" ")) {
            showAlert("Error", "Command name cannot contain spaces.");
            return;
        }

        try {
            createCommandScript(commandName);
            close();
        } catch (Exception e) {
            logger.error("Failed to create command", e);
            showAlert("Error", "Failed to create command: " + e.getMessage());
        }
    }

    private void createCommandScript(String commandName) throws IOException {
        Path scriptsPath = Paths.get(project.getRootPath(), "BP/scripts");
        Files.createDirectories(scriptsPath);
        
        String fileName = "command_" + commandName + ".js";
        Path scriptFile = scriptsPath.resolve(fileName);
        
        StringBuilder script = new StringBuilder();
        script.append("import { world, system } from '@minecraft/server';\n\n");
        script.append("// Command: /").append(commandName).append("\n");
        script.append("// Description: ").append(descriptionField.getText()).append("\n\n");
        
        script.append("world.beforeEvents.chatSend.subscribe((event) => {\n");
        script.append("    const message = event.message;\n");
        script.append("    const player = event.sender;\n\n");
        
        script.append("    if (message.startsWith('!").append(commandName).append("') || message.startsWith('/").append(commandName).append("')) {\n");
        script.append("        event.cancel = true;\n");
        
        if (adminOnlyCheck.isSelected() || permissionCombo.getSelectionModel().getSelectedIndex() == 1) {
            script.append("        if (!player.hasTag('admin') && !player.isOp()) {\n");
            script.append("            system.run(() => player.sendMessage('§cYou do not have permission to use this command.'));\n");
            script.append("            return;\n");
            script.append("        }\n");
        }
        
        script.append("        system.run(() => {\n");
        script.append("            player.sendMessage('§aCommand ").append(commandName).append(" executed!');\n");
        script.append("            // TODO: Add your command logic here\n");
        script.append("        });\n");
        script.append("    }\n");
        script.append("});\n");

        Files.write(scriptFile, script.toString().getBytes());
        
        // Try to register in main.js if it exists, or remind user
        // Simple append if main.js exists
        Path mainJs = scriptsPath.resolve("main.js");
        if (Files.exists(mainJs)) {
             String importLine = "\nimport './" + fileName + "';";
             Files.write(mainJs, importLine.getBytes(), java.nio.file.StandardOpenOption.APPEND);
        } else {
            // Create main.js
            Files.write(mainJs, ("import './" + fileName + "';").getBytes());
        }
        
        showAlert("Success", "Command script created: " + fileName);
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
