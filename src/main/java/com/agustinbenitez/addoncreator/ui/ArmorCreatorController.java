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

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ArmorCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(ArmorCreatorController.class);

    @FXML private TextField identifierField;
    @FXML private TextField nameField;

    @FXML private CheckBox helmetCheck;
    @FXML private CheckBox chestplateCheck;
    @FXML private CheckBox leggingsCheck;
    @FXML private CheckBox bootsCheck;

    @FXML private Label helmetIconLabel;
    @FXML private ImageView helmetIconPreview;
    @FXML private Label chestIconLabel;
    @FXML private ImageView chestIconPreview;
    @FXML private Label leggingsIconLabel;
    @FXML private ImageView leggingsIconPreview;
    @FXML private Label bootsIconLabel;
    @FXML private ImageView bootsIconPreview;

    @FXML private Label layer1Label;
    @FXML private ImageView layer1Preview;
    @FXML private Label layer2Label;
    @FXML private ImageView layer2Preview;

    @FXML private Spinner<Integer> durabilitySpinner;
    @FXML private Spinner<Integer> protectionSpinner;
    
    @FXML private Button createButton;

    private Project project;
    
    private File helmetIconFile;
    private File chestIconFile;
    private File leggingsIconFile;
    private File bootsIconFile;
    
    private File layer1File;
    private File layer2File;

    @FXML
    public void initialize() {
        durabilitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 2000, 200));
        protectionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 3));
        
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

    @FXML
    private void handleSelectHelmetIcon() {
        helmetIconFile = selectImageFile("Select Helmet Icon");
        updatePreview(helmetIconFile, helmetIconPreview, helmetIconLabel);
    }

    @FXML
    private void handleSelectChestIcon() {
        chestIconFile = selectImageFile("Select Chestplate Icon");
        updatePreview(chestIconFile, chestIconPreview, chestIconLabel);
    }

    @FXML
    private void handleSelectLeggingsIcon() {
        leggingsIconFile = selectImageFile("Select Leggings Icon");
        updatePreview(leggingsIconFile, leggingsIconPreview, leggingsIconLabel);
    }

    @FXML
    private void handleSelectBootsIcon() {
        bootsIconFile = selectImageFile("Select Boots Icon");
        updatePreview(bootsIconFile, bootsIconPreview, bootsIconLabel);
    }

    @FXML
    private void handleSelectLayer1() {
        layer1File = selectImageFile("Select Layer 1 Skin");
        updatePreview(layer1File, layer1Preview, layer1Label);
    }

    @FXML
    private void handleSelectLayer2() {
        layer2File = selectImageFile("Select Layer 2 Skin");
        updatePreview(layer2File, layer2Preview, layer2Label);
    }

    private File selectImageFile(String title) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle(title);
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.tga"));
        if (project != null) {
            Path texturesPath = Paths.get(project.getRootPath(), "RP/textures");
            if (Files.exists(texturesPath)) {
                fileChooser.setInitialDirectory(texturesPath.toFile());
            }
        }
        return fileChooser.showOpenDialog(createButton.getScene().getWindow());
    }

    private void updatePreview(File file, ImageView preview, Label label) {
        if (file != null) {
            label.setText(file.getName());
            try {
                Image img = loadTextureFile(file);
                preview.setImage(img);
            } catch (Exception e) {
                logger.error("Failed to load image", e);
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
    private void handleCreate() {
        String fullId = identifierField.getText().trim();
        String displayName = nameField.getText().trim();

        if (fullId.isEmpty() || !fullId.contains(":")) {
            showAlert("Error", "Invalid Identifier. Must contain namespace (e.g. test:ruby).");
            return;
        }
        if (displayName.isEmpty()) {
            showAlert("Error", "Display Name is required.");
            return;
        }

        String[] parts = fullId.split(":");
        String namespace = parts[0];
        String name = parts[1];

        try {
            // Create pieces
            if (helmetCheck.isSelected()) createPiece(namespace, name, "helmet", displayName + " Helmet", helmetIconFile, "slot.armor.head", 1);
            if (chestplateCheck.isSelected()) createPiece(namespace, name, "chestplate", displayName + " Chestplate", chestIconFile, "slot.armor.chest", 1);
            if (leggingsCheck.isSelected()) createPiece(namespace, name, "leggings", displayName + " Leggings", leggingsIconFile, "slot.armor.legs", 2);
            if (bootsCheck.isSelected()) createPiece(namespace, name, "boots", displayName + " Boots", bootsIconFile, "slot.armor.feet", 1);

            // Copy Skins
            Path armorTexturePath = Paths.get(project.getRootPath(), "RP/textures/models/armor");
            Files.createDirectories(armorTexturePath);

            if (layer1File != null) {
                Files.copy(layer1File.toPath(), armorTexturePath.resolve(name + "_layer_1.png"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }
            if (layer2File != null) {
                Files.copy(layer2File.toPath(), armorTexturePath.resolve(name + "_layer_2.png"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            }

            showAlert("Success", "Armor set created successfully!");
            close();

        } catch (Exception e) {
            logger.error("Failed to create armor", e);
            showAlert("Error", "Failed to create armor: " + e.getMessage());
        }
    }

    private void createPiece(String namespace, String name, String type, String displayName, File iconFile, String slot, int layer) throws IOException {
        String itemId = name + "_" + type;
        String fullItemId = namespace + ":" + itemId;

        // 1. Create BP Item
        createBpItem(namespace, itemId, displayName, slot);

        // 2. Create Attachable
        createAttachable(namespace, itemId, name, type, layer);

        // 3. Handle Icon
        if (iconFile != null) {
            Path iconPath = Paths.get(project.getRootPath(), "RP/textures/items", itemId + ".png");
            Files.createDirectories(iconPath.getParent());
            Files.copy(iconFile.toPath(), iconPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            // Register in item_texture.json
            registerItemTexture(itemId, "textures/items/" + itemId);
        }
        
        // 4. Add Name to en_US.lang
        // This is tricky without a proper lang manager, but let's try appending
        // We skip this for now to avoid breaking file if not parsed correctly, or just append
        appendLang(fullItemId, displayName);
    }

    private void createBpItem(String namespace, String itemId, String displayName, String slot) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.20.50");
        
        JsonObject item = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", namespace + ":" + itemId);
        JsonObject category = new JsonObject();
        category.addProperty("category", "equipment");
        category.addProperty("group", "itemGroup.name." + itemId.split("_")[1]); // approximation
        desc.add("menu_category", category);
        item.add("description", desc);

        JsonObject components = new JsonObject();
        components.addProperty("minecraft:icon", itemId);
        
        JsonObject nameComp = new JsonObject();
        nameComp.addProperty("value", displayName);
        components.add("minecraft:display_name", nameComp);
        
        JsonObject durability = new JsonObject();
        durability.addProperty("max_durability", durabilitySpinner.getValue());
        components.add("minecraft:durability", durability);
        
        JsonObject armor = new JsonObject();
        armor.addProperty("protection", protectionSpinner.getValue());
        components.add("minecraft:armor", armor);
        
        JsonObject wearable = new JsonObject();
        wearable.addProperty("slot", slot);
        components.add("minecraft:wearable", wearable);

        item.add("components", components);
        root.add("minecraft:item", item);

        Path path = Paths.get(project.getRootPath(), "BP/items", itemId + ".json");
        Files.createDirectories(path.getParent());
        writeJson(path, root);
    }

    private void createAttachable(String namespace, String itemId, String baseName, String type, int layer) throws IOException {
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.10.0");
        
        JsonObject attachable = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", namespace + ":" + itemId);
        
        JsonObject itemObj = new JsonObject();
        itemObj.addProperty(namespace + ":" + itemId, "query.owner_identifier == 'minecraft:player'");
        desc.add("item", itemObj);
        
        JsonObject materials = new JsonObject();
        materials.addProperty("default", "armor");
        materials.addProperty("enchanted", "armor_enchanted");
        desc.add("materials", materials);
        
        JsonObject textures = new JsonObject();
        textures.addProperty("default", "textures/models/armor/" + baseName + "_layer_" + layer);
        textures.addProperty("enchanted", "textures/misc/enchanted_item_glint");
        desc.add("textures", textures);
        
        JsonObject geometry = new JsonObject();
        geometry.addProperty("default", "geometry.humanoid.armor." + type);
        desc.add("geometry", geometry);
        
        JsonArray controllers = new JsonArray();
        controllers.add("controller.render.armor");
        desc.add("render_controllers", controllers);
        
        attachable.add("description", desc);
        root.add("minecraft:attachable", attachable);

        Path path = Paths.get(project.getRootPath(), "RP/attachables", itemId + ".json");
        Files.createDirectories(path.getParent());
        writeJson(path, root);
    }

    private void registerItemTexture(String textureName, String texturePath) {
        Path jsonPath = Paths.get(project.getRootPath(), "RP/textures/item_texture.json");
        try {
            JsonObject root;
            if (Files.exists(jsonPath)) {
                root = JsonParser.parseReader(new FileReader(jsonPath.toFile())).getAsJsonObject();
            } else {
                root = new JsonObject();
                root.addProperty("resource_pack_name", "vanilla");
                root.addProperty("texture_name", "atlas.items");
                root.add("texture_data", new JsonObject());
            }
            
            JsonObject textureData = root.getAsJsonObject("texture_data");
            JsonObject entry = new JsonObject();
            entry.addProperty("textures", texturePath);
            textureData.add(textureName, entry);
            
            writeJson(jsonPath, root);
        } catch (Exception e) {
            logger.error("Failed to update item_texture.json", e);
        }
    }
    
    private void appendLang(String id, String name) {
        Path langPath = Paths.get(project.getRootPath(), "RP/texts/en_US.lang");
        try {
            String line = "item." + id + "=" + name + "\n";
            Files.write(langPath, line.getBytes(), java.nio.file.StandardOpenOption.CREATE, java.nio.file.StandardOpenOption.APPEND);
        } catch (IOException e) {
             logger.error("Failed to update en_US.lang", e);
        }
    }

    private void writeJson(Path path, JsonObject json) throws IOException {
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(path.toFile())) {
            gson.toJson(json, writer);
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
