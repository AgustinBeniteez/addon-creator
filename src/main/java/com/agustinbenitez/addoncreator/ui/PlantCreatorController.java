package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.paint.Color;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import javafx.util.converter.DoubleStringConverter;
import javafx.util.converter.IntegerStringConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class PlantCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(PlantCreatorController.class);

    @FXML private TextField identifierField;
    @FXML private Button btnTexture;
    @FXML private ImageView imgTexture;
    @FXML private Spinner<Double> destroyTimeSpinner;
    @FXML private Spinner<Double> explosionResistanceSpinner;
    @FXML private ColorPicker mapColorPicker;
    @FXML private ComboBox<String> soundCategoryCombo;
    @FXML private Button createButton;
    @FXML private ImageView previewImage;

    // Loot Table
    @FXML private TableView<LootDrop> lootTable;
    @FXML private TableColumn<LootDrop, String> colItem;
    @FXML private TableColumn<LootDrop, Integer> colMin;
    @FXML private TableColumn<LootDrop, Integer> colMax;
    @FXML private TableColumn<LootDrop, Double> colChance;

    private Project project;
    private File selectedTextureFile;

    public static class LootDrop {
        private final SimpleStringProperty item;
        private final SimpleIntegerProperty min;
        private final SimpleIntegerProperty max;
        private final SimpleDoubleProperty chance;

        public LootDrop(String item, int min, int max, double chance) {
            this.item = new SimpleStringProperty(item);
            this.min = new SimpleIntegerProperty(min);
            this.max = new SimpleIntegerProperty(max);
            this.chance = new SimpleDoubleProperty(chance);
        }

        public String getItem() { return item.get(); }
        public void setItem(String v) { item.set(v); }
        public SimpleStringProperty itemProperty() { return item; }

        public int getMin() { return min.get(); }
        public void setMin(int v) { min.set(v); }
        public SimpleIntegerProperty minProperty() { return min; }

        public int getMax() { return max.get(); }
        public void setMax(int v) { max.set(v); }
        public SimpleIntegerProperty maxProperty() { return max; }

        public double getChance() { return chance.get(); }
        public void setChance(double v) { chance.set(v); }
        public SimpleDoubleProperty chanceProperty() { return chance; }
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
        // Try to find a namespace from existing entities
        if (proj.getEntities() != null && !proj.getEntities().isEmpty()) {
            for (String e : proj.getEntities()) {
                if (e.contains(":"))
                    return e.split(":")[0];
            }
        }
        // Fallback to project name (sanitized)
        if (proj.getName() != null) {
            return proj.getName().toLowerCase().replaceAll("[^a-z0-9_]", "_");
        }
        return "namespace";
    }

    @FXML
    public void initialize() {
        // Spinners
        destroyTimeSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 0.1));
        explosionResistanceSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 0.0, 0.1));

        // Sound Categories
        soundCategoryCombo.getItems().addAll(
            "grass", "stone", "gravel", "wood", "sand", "glass", "cloth"
        );
        soundCategoryCombo.getSelectionModel().select("grass");

        // Loot Table
        colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
        colItem.setCellFactory(TextFieldTableCell.forTableColumn());
        colItem.setOnEditCommit(e -> e.getRowValue().setItem(e.getNewValue()));

        colMin.setCellValueFactory(new PropertyValueFactory<>("min"));
        colMin.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colMin.setOnEditCommit(e -> e.getRowValue().setMin(e.getNewValue()));

        colMax.setCellValueFactory(new PropertyValueFactory<>("max"));
        colMax.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colMax.setOnEditCommit(e -> e.getRowValue().setMax(e.getNewValue()));

        colChance.setCellValueFactory(new PropertyValueFactory<>("chance"));
        colChance.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colChance.setOnEditCommit(e -> e.getRowValue().setChance(e.getNewValue()));
    }

    @FXML
    private void handleSelectTexture() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.tga"));
        if (project != null) {
            File root = new File(project.getRootPath());
            if (root.exists()) chooser.setInitialDirectory(root);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            selectedTextureFile = f;
            Image img = new Image(f.toURI().toString());
            imgTexture.setImage(img);
            previewImage.setImage(img);
            btnTexture.setText(f.getName());
        }
    }

    @FXML
    private void handleAddDrop() {
        lootTable.getItems().add(new LootDrop("minecraft:wheat_seeds", 1, 1, 100.0));
    }

    @FXML
    private void handleRemoveDrop() {
        LootDrop selected = lootTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lootTable.getItems().remove(selected);
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    @FXML
    private void handleCreate() {
        if (identifierField.getText().isEmpty() || !identifierField.getText().contains(":")) {
            showAlert("Error", "Please enter a valid identifier (namespace:name)");
            return;
        }
        if (selectedTextureFile == null) {
            showAlert("Error", "Please select a texture");
            return;
        }

        String fullId = identifierField.getText().trim();
        String[] parts = fullId.split(":");
        String namespace = parts[0];
        String name = parts[1];

        try {
            // 1. Copy Texture
            String textureName = name + "_texture"; // unique name
            Path rpTextures = Paths.get(project.getRootPath(), "RP/textures/blocks");
            Files.createDirectories(rpTextures);
            Path destTexture = rpTextures.resolve(selectedTextureFile.getName());
            Files.copy(selectedTextureFile.toPath(), destTexture, StandardCopyOption.REPLACE_EXISTING);
            String textureShortName = selectedTextureFile.getName().substring(0, selectedTextureFile.getName().lastIndexOf('.'));

            // 2. Create Block JSON
            JsonObject blockJson = new JsonObject();
            JsonObject formatVersion = new JsonObject();
            blockJson.addProperty("format_version", "1.20.0");
            
            JsonObject def = new JsonObject();
            JsonObject desc = new JsonObject();
            desc.addProperty("identifier", fullId);
            desc.addProperty("is_experimental", false);
            def.add("description", desc);

            JsonObject components = new JsonObject();
            components.addProperty("minecraft:destructible_by_mining", destroyTimeSpinner.getValue());
            components.addProperty("minecraft:destructible_by_explosion", explosionResistanceSpinner.getValue());
            components.addProperty("minecraft:map_color", toHex(mapColorPicker.getValue()));
            components.addProperty("minecraft:geometry", "minecraft:geometry.cross"); // PLANT GEOMETRY
            
            JsonObject collision = new JsonObject();
            collision.addProperty("origin", "[-4, 0, -4]");
            collision.addProperty("size", "[8, 16, 8]");
            // components.add("minecraft:collision_box", collision); // Usually plants don't have collision, or small one
            components.addProperty("minecraft:collision_box", false); // No collision for plants usually
            components.addProperty("minecraft:selection_box", true);

            JsonObject matInstances = new JsonObject();
            JsonObject allMat = new JsonObject();
            allMat.addProperty("texture", textureShortName);
            allMat.addProperty("render_method", "alpha_test");
            allMat.addProperty("face_dimming", false);
            allMat.addProperty("ambient_occlusion", false);
            matInstances.add("*", allMat);
            components.add("minecraft:material_instances", matInstances);
            
            components.addProperty("minecraft:placement_filter", "{\"allowed_faces\": [\"up\"]}");

            def.add("components", components);
            blockJson.add("minecraft:block", def);

            Path bpBlocks = Paths.get(project.getRootPath(), "BP/blocks");
            Files.createDirectories(bpBlocks);
            try (FileWriter writer = new FileWriter(bpBlocks.resolve(name + ".json").toFile())) {
                new GsonBuilder().setPrettyPrinting().create().toJson(blockJson, writer);
            }

            // 3. Register Texture in terrain_texture.json
            updateTerrainTexture(textureShortName, "textures/blocks/" + textureShortName);

            // 4. Register Block in blocks.json
            updateBlocksJson(fullId, textureShortName);

            showAlert("Success", "Plant created successfully!");
            close();

        } catch (Exception e) {
            logger.error("Failed to create plant", e);
            showAlert("Error", "Failed to create plant: " + e.getMessage());
        }
    }

    private void updateTerrainTexture(String textureName, String texturePath) {
        // Implementation similar to BlockCreatorController
        // ... (Simplified for brevity, assuming standard helper or manual update)
        // Since I don't have the helper methods handy, I'll implement a basic update
         try {
            Path path = Paths.get(project.getRootPath(), "RP/textures/terrain_texture.json");
            JsonObject root;
            if (Files.exists(path)) {
                root = JsonParser.parseReader(new FileReader(path.toFile())).getAsJsonObject();
            } else {
                root = new JsonObject();
                root.addProperty("resource_pack_name", "vanilla");
                root.addProperty("texture_name", "atlas.terrain");
                root.addProperty("padding", 8);
                root.addProperty("num_mip_levels", 4);
            }

            if (!root.has("texture_data")) root.add("texture_data", new JsonObject());
            JsonObject textureData = root.getAsJsonObject("texture_data");
            
            JsonObject texEntry = new JsonObject();
            texEntry.addProperty("textures", texturePath);
            textureData.add(textureName, texEntry);

            try (FileWriter writer = new FileWriter(path.toFile())) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
        } catch (Exception e) {
            logger.error("Failed to update terrain_texture.json", e);
        }
    }

    private void updateBlocksJson(String fullId, String textureName) {
        try {
            Path path = Paths.get(project.getRootPath(), "RP/blocks.json");
            JsonObject root;
            if (Files.exists(path)) {
                root = JsonParser.parseReader(new FileReader(path.toFile())).getAsJsonObject();
            } else {
                root = new JsonObject();
                root.addProperty("format_version", "1.1.0");
            }

            String namespace = fullId.split(":")[0]; // "my"
            String name = fullId.split(":")[1]; // "plant"
            // blocks.json uses "namespace:name": { "textures": "shortname", "sound": "grass" }
            
            // Note: In older versions or specific setups, blocks.json might be different.
            // But standard is to map identifier to textures.
            
            if (!root.has(fullId)) {
                JsonObject entry = new JsonObject();
                entry.addProperty("textures", textureName);
                entry.addProperty("sound", soundCategoryCombo.getValue());
                root.add(fullId, entry);
            }

            try (FileWriter writer = new FileWriter(path.toFile())) {
                new GsonBuilder().setPrettyPrinting().create().toJson(root, writer);
            }
        } catch (Exception e) {
            logger.error("Failed to update blocks.json", e);
        }
    }

    private String toHex(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
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
