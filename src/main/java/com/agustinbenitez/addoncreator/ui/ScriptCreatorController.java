package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

public class ScriptCreatorController {

    @FXML
    private TextField nameField;
    @FXML
    private ComboBox<String> templateSelector;
    @FXML
    private TextArea previewArea;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnCreate;

    private Project currentProject;
    private Runnable onClose;
    private final Map<String, String> templates = new HashMap<>();

    @FXML
    public void initialize() {
        setupTemplates();

        templateSelector.getItems().addAll(templates.keySet());
        templateSelector.setValue("Empty Script");

        templateSelector.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                previewArea.setText(templates.get(newVal));
            }
        });

        previewArea.setText(templates.get("Empty Script"));

        btnCancel.setOnAction(e -> close());
        btnCreate.setOnAction(e -> createScript());
    }

    public void setProject(Project project) {
        this.currentProject = project;
    }

    public void setOnClose(Runnable onClose) {
        this.onClose = onClose;
    }

    private void setupTemplates() {
        templates.put("Empty Script", "// New script file\nconsole.log(\"Hello World!\");");

        templates.put("System Event Listener",
                "import { system, world } from \"@minecraft/server\";\n\n" +
                        "system.runInterval(() => {\n" +
                        "    // Runs every tick\n" +
                        "}, 0);\n");

        templates.put("Chat Command",
                "import { world } from \"@minecraft/server\";\n\n" +
                        "world.beforeEvents.chatSend.subscribe((event) => {\n" +
                        "    if (event.message === \"!ping\") {\n" +
                        "        event.cancel = true;\n" +
                        "        world.sendMessage(\"Pong!\");\n" +
                        "    }\n" +
                        "});\n");

        templates.put("Entity Spawn Event",
                "import { world } from \"@minecraft/server\";\n\n" +
                        "world.afterEvents.entitySpawn.subscribe((event) => {\n" +
                        "    console.warn(`Entity spawned: ${event.entity.typeId}`);\n" +
                        "});\n");
    }

    private void createScript() {
        if (currentProject == null)
            return;

        String name = nameField.getText().trim();
        if (name.isEmpty()) {
            showAlert("Error", "Please enter a script name.");
            return;
        }

        if (!name.endsWith(".js") && !name.endsWith(".ts")) {
            name += ".js";
        }

        try {
            Path root = Paths.get(currentProject.getRootPath());
            Path scriptsDir = root.resolve("BP/scripts");
            if (!Files.exists(scriptsDir)) {
                Files.createDirectories(scriptsDir);
            }

            Path file = scriptsDir.resolve(name);
            if (Files.exists(file)) {
                showAlert("Error", "A file with this name already exists.");
                return;
            }

            String content = previewArea.getText();
            Files.writeString(file, content);

            if (onClose != null)
                onClose.run();
            close();

        } catch (IOException e) {
            e.printStackTrace();
            showAlert("Error", "Failed to create script: " + e.getMessage());
        }
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
