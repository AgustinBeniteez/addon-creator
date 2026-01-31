package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class WorldGenCreatorController {

    @FXML
    private ComboBox<String> categorySelector;
    @FXML
    private Button btnCancel;
    @FXML
    private Button btnSave;

    // Biome Editor
    @FXML
    private VBox biomeEditor;
    @FXML
    private TextField biomeNameField;
    @FXML
    private ColorPicker skyColorPicker;
    @FXML
    private ColorPicker waterColorPicker;
    @FXML
    private ColorPicker fogColorPicker;
    @FXML
    private ComboBox<String> surfaceSelector;
    @FXML
    private CheckBox precipitationToggle;

    // Feature Editor
    @FXML
    private VBox featureEditor;
    @FXML
    private TextField featureNameField;
    @FXML
    private ComboBox<String> featureTypeSelector;
    @FXML
    private TextField blockField;
    @FXML
    private Slider clusterSizeSlider;

    // Feature Rule Editor
    @FXML
    private VBox featureRuleEditor;
    @FXML
    private TextField ruleNameField;
    @FXML
    private TextField targetFeatureField;
    @FXML
    private VBox biomesContainer;
    @FXML
    private Slider minHeightSlider;
    @FXML
    private Slider maxHeightSlider;
    @FXML
    private TextField densityField;
    @FXML
    private Label minHeightLabel;
    @FXML
    private Label maxHeightLabel;

    // Preview Labels
    @FXML
    private Label previewStartLayer;
    @FXML
    private Label previewEndLayer;
    @FXML
    private Label previewFrequency;

    private Project currentProject;
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @FXML
    public void initialize() {
        // Setup Category Selector
        categorySelector.getItems().addAll("Biome Editor", "Feature Editor", "Feature Rules Editor");
        categorySelector.setValue("Biome Editor");
        categorySelector.setOnAction(e -> updateView());

        // Setup Biome/Feature/Rule Views
        updateView();

        // Setup Buttons
        btnCancel.setOnAction(e -> close());
        btnSave.setOnAction(e -> saveScript());

        // Setup Feature Type
        featureTypeSelector.getItems().addAll("Ore", "Single Block", "Tree", "Structure");

        // Setup Surface
        surfaceSelector.getItems().addAll("minecraft:grass", "minecraft:sand", "minecraft:snow", "minecraft:stone");

        // Setup Sliders
        minHeightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            minHeightLabel.setText(String.format("%.0f", newVal));
            updatePreview();
        });

        maxHeightSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            maxHeightLabel.setText(String.format("%.0f", newVal));
            updatePreview();
        });

        densityField.textProperty().addListener((obs, oldVal, newVal) -> updatePreview());
    }

    public void setProject(Project project) {
        this.currentProject = project;
    }

    private void updateView() {
        biomeEditor.setVisible(false);
        featureEditor.setVisible(false);
        featureRuleEditor.setVisible(false);

        String selected = categorySelector.getValue();
        if ("Biome Editor".equals(selected)) {
            biomeEditor.setVisible(true);
        } else if ("Feature Editor".equals(selected)) {
            featureEditor.setVisible(true);
        } else if ("Feature Rules Editor".equals(selected)) {
            featureRuleEditor.setVisible(true);
        }
    }

    private void updatePreview() {
        previewStartLayer.setText("Start Layer: " + (int) minHeightSlider.getValue());
        previewEndLayer.setText("End Layer: " + (int) maxHeightSlider.getValue());
        previewFrequency.setText("Frequency: " + densityField.getText());
    }

    private void saveScript() {
        if (currentProject == null)
            return;

        try {
            String selected = categorySelector.getValue();
            if ("Biome Editor".equals(selected)) {
                saveBiome();
            } else if ("Feature Editor".equals(selected)) {
                saveFeature();
            } else if ("Feature Rules Editor".equals(selected)) {
                saveFeatureRule();
            }
            close();
        } catch (Exception e) {
            e.printStackTrace();
            // Show error
            Alert alert = new Alert(Alert.AlertType.ERROR, "Failed to save script: " + e.getMessage());
            alert.showAndWait();
        }
    }

    private void saveBiome() throws IOException {
        String name = biomeNameField.getText().trim();
        if (name.isEmpty())
            return;

        JsonObject root = new JsonObject();
        JsonObject biome = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", "custom:" + name);
        biome.add("description", desc);

        // Components (simplified for prototype)
        JsonObject components = new JsonObject();

        // Climate
        JsonObject climate = new JsonObject();
        climate.addProperty("downfall", precipitationToggle.isSelected() ? 0.8 : 0.0);
        climate.addProperty("temperature", 0.5); // Default
        components.add("minecraft:climate", climate);

        // Surface
        if (surfaceSelector.getValue() != null) {
            JsonObject surface = new JsonObject();
            surface.addProperty("top_material", surfaceSelector.getValue());
            components.add("minecraft:surface_parameters", surface);
        }

        biome.add("components", components);
        root.add("minecraft:biome", biome);

        saveJsonFile("BP/biomes", name + ".json", root);
    }

    private void saveFeature() throws IOException {
        String name = featureNameField.getText().trim();
        if (name.isEmpty())
            return;

        JsonObject root = new JsonObject();
        JsonObject feature = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", currentProject.getName().toLowerCase().replace(" ", "_") + ":" + name);
        feature.add("description", desc);

        String type = featureTypeSelector.getValue();
        if ("Ore".equals(type)) {
            JsonObject ore = new JsonObject();
            ore.addProperty("count", (int) clusterSizeSlider.getValue());
            ore.addProperty("places_block", blockField.getText());
            ore.addProperty("may_replace", "minecraft:stone"); // Default
            feature.add("minecraft:ore_feature", ore);
        } else {
            // Basic fallback for other types
            JsonObject single = new JsonObject();
            single.addProperty("places_block", blockField.getText());
            feature.add("minecraft:single_block_feature", single);
        }

        root.add("minecraft:feature", feature);
        saveJsonFile("BP/features", name + ".json", root);
    }

    // The specific JSON structure requested by user for Feature Rules
    private void saveFeatureRule() throws IOException {
        String name = ruleNameField.getText().trim();
        if (name.isEmpty())
            return;

        JsonObject root = new JsonObject();
        JsonObject rule = new JsonObject();

        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", currentProject.getName().toLowerCase().replace(" ", "_") + ":" + name);
        rule.add("description", desc);

        JsonObject conditions = new JsonObject();
        conditions.addProperty("placement_pass", "underground_pass");

        JsonObject biomeFilter = new JsonObject();
        biomeFilter.addProperty("test", "has_biome_tag");
        biomeFilter.addProperty("operator", "==");
        biomeFilter.addProperty("value", "overworld");
        conditions.add("biome_filter", biomeFilter);

        rule.add("conditions", conditions);

        JsonObject distribution = new JsonObject();
        try {
            distribution.addProperty("iterations", Integer.parseInt(densityField.getText()));
        } catch (NumberFormatException e) {
            distribution.addProperty("iterations", 1);
        }

        distribution.addProperty("coordinate_evaluator", "uniform");

        JsonObject x = new JsonObject();
        x.addProperty("distribution", "uniform");
        JsonArray xExtent = new JsonArray();
        xExtent.add(0);
        xExtent.add(16);
        x.add("extent", xExtent);

        JsonObject y = new JsonObject();
        y.addProperty("distribution", "uniform");
        JsonArray yExtent = new JsonArray();
        yExtent.add((int) minHeightSlider.getValue());
        yExtent.add((int) maxHeightSlider.getValue());
        y.add("extent", yExtent);

        JsonObject z = new JsonObject();
        z.addProperty("distribution", "uniform");
        JsonArray zExtent = new JsonArray();
        zExtent.add(0);
        zExtent.add(16);
        z.add("extent", zExtent);

        distribution.add("x", x);
        distribution.add("y", y); // User specifically emphasized this part
        distribution.add("z", z);

        rule.add("distribution", distribution);

        root.add("minecraft:feature_rules", rule);
        saveJsonFile("BP/feature_rules", name + ".json", root);
    }

    private void saveJsonFile(String subDir, String fileName, JsonObject json) throws IOException {
        Path root = Paths.get(currentProject.getRootPath());
        Path dir = root.resolve(subDir);
        if (!Files.exists(dir))
            Files.createDirectories(dir);

        try (FileWriter writer = new FileWriter(dir.resolve(fileName).toFile())) {
            gson.toJson(json, writer);
        }
    }

    private void close() {
        Stage stage = (Stage) btnCancel.getScene().getWindow();
        stage.close();
    }
}
