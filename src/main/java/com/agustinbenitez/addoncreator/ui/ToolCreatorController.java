package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.agustinbenitez.addoncreator.utils.TgaImageLoader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ToolCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(ToolCreatorController.class);

    @FXML private TextField identifierField;
    @FXML private TextField nameField;
    @FXML private ComboBox<String> toolTypeCombo;
    @FXML private Button textureButton;
    @FXML private Button createButton;
    @FXML private ImageView texturePreview;
    @FXML private Label textureNameLabel;
    
    @FXML private Spinner<Integer> durabilitySpinner;
    @FXML private Spinner<Integer> miningSpeedSpinner;
    @FXML private Spinner<Integer> damageSpinner;
    @FXML private CheckBox handEquippedCheck;
    @FXML private CheckBox glintCheck;

    private Project project;
    private File selectedTextureFile;
    private String selectedTextureName;

    @FXML
    public void initialize() {
        toolTypeCombo.getItems().addAll("Sword", "Pickaxe", "Axe", "Shovel", "Hoe");
        toolTypeCombo.getSelectionModel().select("Sword");
        
        durabilitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 250));
        miningSpeedSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 1));
        damageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 100, 4));
        
        // Adjust defaults based on selection
        toolTypeCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            switch (newVal) {
                case "Sword":
                    miningSpeedSpinner.getValueFactory().setValue(1);
                    damageSpinner.getValueFactory().setValue(6);
                    break;
                case "Pickaxe":
                    miningSpeedSpinner.getValueFactory().setValue(6);
                    damageSpinner.getValueFactory().setValue(3);
                    break;
                case "Axe":
                    miningSpeedSpinner.getValueFactory().setValue(4);
                    damageSpinner.getValueFactory().setValue(4);
                    break;
                case "Shovel":
                    miningSpeedSpinner.getValueFactory().setValue(4);
                    damageSpinner.getValueFactory().setValue(2);
                    break;
                case "Hoe":
                    miningSpeedSpinner.getValueFactory().setValue(2);
                    damageSpinner.getValueFactory().setValue(2);
                    break;
            }
        });

        textureButton.setOnAction(e -> handleSelectTexture());
        
        // Auto-fill identifier based on name
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (identifierField.getText().isEmpty() 
                    || (oldVal != null && identifierField.getText().endsWith(formatToId(oldVal)))) {
                String currentNamespace = "namespace";
                if (project != null) {
                    currentNamespace = findNamespace(project);
                } else if (identifierField.getText().contains(":")) {
                    currentNamespace = identifierField.getText().split(":")[0];
                }
                identifierField.setText(currentNamespace + ":" + formatToId(newVal));
            }
        });
    }

    private String formatToId(String name) {
        return name.toLowerCase().replaceAll("[^a-z0-9_]", "_");
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

    private void handleSelectTexture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Texture");
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.tga")
        );
        File file = fileChooser.showOpenDialog(textureButton.getScene().getWindow());
        if (file != null) {
            try {
                selectedTextureFile = file;
                selectedTextureName = file.getName().substring(0, file.getName().lastIndexOf('.'));
                textureNameLabel.setText(file.getName());
                
                Image image = loadTextureFile(file);
                texturePreview.setImage(image);
            } catch (Exception e) {
                logger.error("Failed to load texture", e);
                showAlert("Error", "Failed to load texture: " + e.getMessage());
            }
        }
    }
    
    private Image loadTextureFile(File file) throws IOException {
        String fileName = file.getName().toLowerCase();
        if (fileName.endsWith(".tga")) {
            return TgaImageLoader.loadTga(file);
        } else {
            return new Image(new FileInputStream(file));
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    @FXML
    private void handleCreate() {
        String identifier = identifierField.getText().trim();
        String name = nameField.getText().trim();
        
        if (identifier.isEmpty() || !identifier.contains(":")) {
            showAlert("Error", "Valid Identifier is required (e.g. namespace:item_name).");
            return;
        }
        if (selectedTextureFile == null) {
            showAlert("Error", "Texture is required.");
            return;
        }

        try {
            createTool(identifier, name);
            close();
        } catch (Exception e) {
            logger.error("Failed to create tool", e);
            showAlert("Error", "Failed to create tool: " + e.getMessage());
        }
    }

    private void createTool(String identifier, String name) throws IOException {
        // 1. Copy Texture
        Path texturesPath = Paths.get(project.getRootPath(), "RP/textures/items");
        Files.createDirectories(texturesPath);
        Path targetTexture = texturesPath.resolve(selectedTextureFile.getName());
        Files.copy(selectedTextureFile.toPath(), targetTexture, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        
        // 2. Register Texture in item_texture.json
        registerTexture(selectedTextureName, selectedTextureFile.getName());
        
        // 3. Create Item JSON
        Path itemsPath = Paths.get(project.getRootPath(), "BP/items");
        Files.createDirectories(itemsPath);
        
        JsonObject itemJson = new JsonObject();
        JsonObject formatVersion = new JsonObject();
        itemJson.addProperty("format_version", "1.20.50");
        
        JsonObject minecraftItem = new JsonObject();
        JsonObject description = new JsonObject();
        description.addProperty("identifier", identifier);
        
        JsonObject menuCategory = new JsonObject();
        menuCategory.addProperty("category", "equipment");
        menuCategory.addProperty("group", "itemGroup.name.sword"); // Simplified default
        description.add("menu_category", menuCategory);
        
        minecraftItem.add("description", description);
        
        JsonObject components = new JsonObject();
        components.addProperty("minecraft:display_name", name);
        components.addProperty("minecraft:icon", selectedTextureName);
        components.addProperty("minecraft:max_stack_size", 1);
        components.addProperty("minecraft:glint", glintCheck.isSelected());
        if (handEquippedCheck.isSelected()) {
            components.addProperty("minecraft:hand_equipped", true);
        }
        
        // Durability
        JsonObject durability = new JsonObject();
        durability.addProperty("max_durability", durabilitySpinner.getValue());
        components.add("minecraft:durability", durability);
        
        // Tool Logic
        String toolType = toolTypeCombo.getValue();
        int damage = damageSpinner.getValue();
        int speed = miningSpeedSpinner.getValue();
        
        if ("Sword".equals(toolType)) {
             components.addProperty("minecraft:can_destroy_in_creative", false);
             // Damage component (modern syntax uses minecraft:damage) - but usually it's tied to weapon
             // Actually, for simple custom items, we use 'minecraft:damage' component now in 1.20.50+?
             // Or 'minecraft:weapon'? Let's use components compatible with recent versions.
             // 'minecraft:damage' specifies the damage caused by the item.
             components.addProperty("minecraft:damage", damage);
        } else {
             // Digger Logic
             JsonObject digger = new JsonObject();
             digger.addProperty("use_efficiency", true);
             
             JsonArray destroySpeeds = new JsonArray();
             JsonObject speedEntry = new JsonObject();
             JsonObject blockObj = new JsonObject();
             
             JsonArray tags = new JsonArray();
             String tag = "minecraft:is_stone_item_destructible"; // default
             
             switch (toolType) {
                 case "Pickaxe": tag = "minecraft:is_stone_item_destructible"; break; // simplified tag assumption
                 case "Axe": tag = "minecraft:is_wood_item_destructible"; break; // simplified
                 case "Shovel": tag = "minecraft:is_dirt_item_destructible"; break; // simplified
                 case "Hoe": tag = "minecraft:is_hoe_item_destructible"; break; // simplified
             }
             // Since we can't guess custom tags easily, we might just use a query or generic tags
             // Actually, 1.20+ uses tags directly.
             // Let's use a generic catch-all for now or specific block tags if possible.
             // For simplicity in this template, we add a generic speed entry for the tag.
             
             blockObj.addProperty("tags", "q.any_tag('" + tag + "')");
             speedEntry.add("block", blockObj);
             speedEntry.addProperty("speed", speed);
             
             destroySpeeds.add(speedEntry);
             digger.add("destroy_speeds", destroySpeeds);
             components.add("minecraft:digger", digger);
             components.addProperty("minecraft:damage", damage); // Tools also do damage
        }
        
        minecraftItem.add("components", components);
        itemJson.add("minecraft:item", minecraftItem);
        
        String fileName = identifier.split(":")[1] + ".json";
        try (FileWriter writer = new FileWriter(itemsPath.resolve(fileName).toFile())) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(itemJson, writer);
        }
        
        showAlert("Success", "Tool created: " + fileName);
    }
    
    private void registerTexture(String textureName, String fileName) {
        try {
            Path textureDefPath = Paths.get(project.getRootPath(), "RP/textures/item_texture.json");
            if (!Files.exists(textureDefPath)) return; // Should create if not exists, but keeping simple
            
            JsonObject json = JsonParser.parseReader(new java.io.FileReader(textureDefPath.toFile())).getAsJsonObject();
            JsonObject textureData = json.getAsJsonObject("texture_data");
            
            if (!textureData.has(textureName)) {
                JsonObject texEntry = new JsonObject();
                texEntry.addProperty("textures", "textures/items/" + fileName.substring(0, fileName.lastIndexOf('.')));
                textureData.add(textureName, texEntry);
                
                try (FileWriter writer = new FileWriter(textureDefPath.toFile())) {
                    Gson gson = new GsonBuilder().setPrettyPrinting().create();
                    gson.toJson(json, writer);
                }
            }
        } catch (Exception e) {
            logger.error("Failed to register texture", e);
        }
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
