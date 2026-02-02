package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.agustinbenitez.addoncreator.utils.BedrockModelLoader;
import com.agustinbenitez.addoncreator.utils.BedrockSamplesDownloader;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonElement;
import com.google.gson.JsonArray;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class EntityCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(EntityCreatorController.class);

    // Loot Drop Model
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

        public String getItem() {
            return item.get();
        }

        public void setItem(String v) {
            item.set(v);
        }

        public SimpleStringProperty itemProperty() {
            return item;
        }

        public int getMin() {
            return min.get();
        }

        public void setMin(int v) {
            min.set(v);
        }

        public SimpleIntegerProperty minProperty() {
            return min;
        }

        public int getMax() {
            return max.get();
        }

        public void setMax(int v) {
            max.set(v);
        }

        public SimpleIntegerProperty maxProperty() {
            return max;
        }

        public double getChance() {
            return chance.get();
        }

        public void setChance(double v) {
            chance.set(v);
        }

        public SimpleDoubleProperty chanceProperty() {
            return chance;
        }
    }

    @FXML
    private TextField identifierField;
    @FXML
    private ComboBox<String> baseModelCombo;
    @FXML
    private Button btnRefreshModels;
    @FXML
    private Button createButton;

    @FXML
    private Spinner<Double> healthSpinner;
    @FXML
    private Spinner<Double> damageSpinner;
    @FXML
    private Spinner<Double> scaleSpinner;

    @FXML
    private TableView<LootDrop> lootTable;
    @FXML
    private TableColumn<LootDrop, String> colItem;
    @FXML
    private TableColumn<LootDrop, Integer> colMin;
    @FXML
    private TableColumn<LootDrop, Integer> colMax;
    @FXML
    private TableColumn<LootDrop, Double> colChance;

    @FXML
    private ImageView texturePreview;
    @FXML
    private Label textureNameLabel;

    @FXML
    private Pane previewContainer;

    private Project project;
    private File selectedTextureFile;
    private String selectedModelPath; // Relative path in repo e.g. resource_pack/models/entity/creeper.geo.json
    private String selectedTexturePath; // Relative path in repo e.g. resource_pack/textures/entity/creeper.png
    private File localModelFile;
    private File localTextureFile;

    // 3D Preview
    private SubScene subScene;
    private Group root3D;
    private Group modelGroup;
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double mouseOldX, mouseOldY;

    // Cache of available models from GitHub
    private List<String> availableModels = new ArrayList<>();
    private List<String> availableTextures = new ArrayList<>();

    // Edit Mode
    private boolean isEditMode = false;
    private String originalIdentifier;

    @FXML
    public void initialize() {
        initialize3D();
        initializeUI();
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
    private void initializeUI() {
        healthSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(1.0, 1000.0, 20.0, 1.0));
        damageSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1000.0, 3.0, 1.0));
        scaleSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.1, 10.0, 1.0, 0.1));

        // Initialize Loot Table
        colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
        colMin.setCellValueFactory(new PropertyValueFactory<>("min"));
        colMax.setCellValueFactory(new PropertyValueFactory<>("max"));
        colChance.setCellValueFactory(new PropertyValueFactory<>("chance"));

        // Make columns editable (optional, but requested "list or write by hand")
        lootTable.setEditable(true);

        // Item Column Editing
        colItem.setCellFactory(TextFieldTableCell.forTableColumn());
        colItem.setOnEditCommit(e -> e.getRowValue().setItem(e.getNewValue()));

        // Min/Max/Chance Editing
        colMin.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colMin.setOnEditCommit(e -> e.getRowValue().setMin(e.getNewValue()));

        colMax.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colMax.setOnEditCommit(e -> e.getRowValue().setMax(e.getNewValue()));

        colChance.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colChance.setOnEditCommit(e -> e.getRowValue().setChance(e.getNewValue()));

        baseModelCombo.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                handleModelSelection(newVal);
            }
        });

        // Load cached models if any, or trigger load
        if (availableModels.isEmpty()) {
            Platform.runLater(this::refreshAvailableModels);
        }
    }

    @FXML
    private void handleAddDrop() {
        // Open Item Selection Dialog
        ItemSelectionDialog dialog = new ItemSelectionDialog(project, "");
        dialog.showAndWait().ifPresent(selectedItem -> {
            // Add to table with default values
            lootTable.getItems().add(new LootDrop(selectedItem, 1, 1, 100.0));
        });
    }

    @FXML
    private void handleRemoveDrop() {
        LootDrop selected = lootTable.getSelectionModel().getSelectedItem();
        if (selected != null) {
            lootTable.getItems().remove(selected);
        }
    }

    // ... (keep existing methods) ...

    public void loadEntityData(File entityFile) {
        if (!entityFile.exists())
            return;
        isEditMode = true;

        try {
            JsonObject root = JsonParser.parseReader(new FileReader(entityFile)).getAsJsonObject();
            JsonObject entity = root.getAsJsonObject("minecraft:entity");
            JsonObject desc = entity.getAsJsonObject("description");
            String fullId = desc.get("identifier").getAsString();
            this.originalIdentifier = fullId;
            identifierField.setText(fullId);
            identifierField.setDisable(true);

            if (entity.has("components")) {
                JsonObject comp = entity.getAsJsonObject("components");
                if (comp.has("minecraft:health")) {
                    JsonObject health = comp.getAsJsonObject("minecraft:health");
                    if (health.has("value"))
                        healthSpinner.getValueFactory().setValue(health.get("value").getAsDouble());
                }
                if (comp.has("minecraft:attack")) {
                    JsonObject attack = comp.getAsJsonObject("minecraft:attack");
                    if (attack.has("damage"))
                        damageSpinner.getValueFactory()
                                .setValue(attack.getAsJsonObject("damage").get("default").getAsDouble());
                }
                if (comp.has("minecraft:scale")) {
                    JsonObject scale = comp.getAsJsonObject("minecraft:scale");
                    if (scale.has("value"))
                        scaleSpinner.getValueFactory().setValue(scale.get("value").getAsDouble());
                }

                // Load Loot Table info if simpler structure matches
                if (comp.has("minecraft:loot")) {
                    String lootTablePath = comp.getAsJsonObject("minecraft:loot").get("table").getAsString();
                    File lootFile = new File(project.getRootPath(), "BP/" + lootTablePath);
                    if (lootFile.exists()) {
                        try {
                            JsonObject lootRoot = JsonParser.parseReader(new FileReader(lootFile)).getAsJsonObject();
                            if (lootRoot.has("pools")) {
                                JsonArray pools = lootRoot.getAsJsonArray("pools");
                                if (pools.size() > 0) {
                                    JsonObject pool = pools.get(0).getAsJsonObject();
                                    if (pool.has("entries")) {
                                        JsonArray entries = pool.getAsJsonArray("entries");
                                        for (JsonElement je : entries) {
                                            JsonObject entry = je.getAsJsonObject();
                                            String item = "";
                                            int min = 1;
                                            int max = 1;
                                            double chance = 100.0;

                                            if (entry.has("name")) {
                                                item = entry.get("name").getAsString();
                                            }

                                            if (entry.has("functions")) {
                                                JsonArray funcs = entry.getAsJsonArray("functions");
                                                for (JsonElement fe : funcs) {
                                                    JsonObject func = fe.getAsJsonObject();
                                                    if (func.has("function")
                                                            && "set_count".equals(func.get("function").getAsString())) {
                                                        JsonObject count = func.getAsJsonObject("count");
                                                        if (count.has("min"))
                                                            min = count.get("min").getAsInt();
                                                        if (count.has("max"))
                                                            max = count.get("max").getAsInt();
                                                    }
                                                }
                                            }

                                            if (entry.has("conditions")) {
                                                JsonArray conds = entry.getAsJsonArray("conditions");
                                                for (JsonElement ce : conds) {
                                                    JsonObject cond = ce.getAsJsonObject();
                                                    if (cond.has("condition") && "random_chance"
                                                            .equals(cond.get("condition").getAsString())) {
                                                        chance = cond.get("chance").getAsDouble() * 100.0;
                                                    }
                                                }
                                            }

                                            if (!item.isEmpty()) {
                                                lootTable.getItems().add(new LootDrop(item, min, max, chance));
                                            }
                                        }
                                    }
                                }
                            }
                        } catch (Exception e) {
                            logger.error("Failed to parse loot table", e);
                        }
                    }
                }
            }

            // ... (keep RP loading logic) ...

            // Try to load RP data (model & texture)
            String name = fullId.contains(":") ? fullId.split(":")[1] : fullId;
            File rpFile = new File(project.getRootPath(), "RP/entity/" + name + ".entity.json");
            if (rpFile.exists()) {
                JsonObject rpRoot = JsonParser.parseReader(new FileReader(rpFile)).getAsJsonObject();
                JsonObject clientEntity = rpRoot.getAsJsonObject("minecraft:client_entity");
                JsonObject rpDesc = clientEntity.getAsJsonObject("description");

                // Texture
                if (rpDesc.has("textures")) {
                    JsonObject textures = rpDesc.getAsJsonObject("textures");
                    if (textures.has("default")) {
                        String texPath = textures.get("default").getAsString();
                        // resolve texture file
                        File texFile = new File(project.getRootPath(), "RP/" + texPath + ".png");
                        if (texFile.exists()) {
                            selectedTextureFile = texFile;
                            textureNameLabel.setText(texFile.getName());
                            texturePreview.setImage(loadTexture(texFile));
                        }
                    }
                }

                // Geometry
                if (rpDesc.has("geometry")) {
                    JsonObject geo = rpDesc.getAsJsonObject("geometry");
                    if (geo.has("default")) {
                        String geoId = geo.get("default").getAsString();
                        // Scan for this geometry ID
                        File modelFile = BedrockModelLoader.scanForGeometry(new File(project.getRootPath()), geoId);
                        if (modelFile != null) {
                            localModelFile = modelFile;
                            updatePreview();
                        }
                    }
                }
            }

            if (createButton != null) {
                createButton.setText("Save Changes");
            }

        } catch (Exception e) {
            logger.error("Failed to load entity data", e);
        }
    }

    private void createBPEntity(String fullId, String namespace, String name) {
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.16.0");

        JsonObject entity = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", fullId);
        desc.addProperty("is_spawnable", true);
        desc.addProperty("is_summonable", true);
        desc.addProperty("is_experimental", false);
        entity.add("description", desc);

        JsonObject components = new JsonObject();

        JsonObject health = new JsonObject();
        health.addProperty("value", healthSpinner.getValue());
        health.addProperty("max", healthSpinner.getValue());
        components.add("minecraft:health", health);

        if (damageSpinner.getValue() > 0) {
            JsonObject attack = new JsonObject();
            JsonObject dmg = new JsonObject();
            dmg.addProperty("default", damageSpinner.getValue());
            attack.add("damage", dmg);
            components.add("minecraft:attack", attack);
        }

        JsonObject scale = new JsonObject();
        scale.addProperty("value", scaleSpinner.getValue());
        components.add("minecraft:scale", scale);

        JsonObject physics = new JsonObject();
        components.add("minecraft:physics", physics);

        JsonObject movement = new JsonObject();
        movement.addProperty("value", 0.3);
        components.add("minecraft:movement", movement);

        components.add("minecraft:movement.basic", new JsonObject());
        components.add("minecraft:jump.static", new JsonObject());
        components.add("minecraft:can_climb", new JsonObject());
        components.add("minecraft:collision_box", new JsonObject());

        // Loot Table
        if (!lootTable.getItems().isEmpty()) {
            String lootPath = "loot_tables/entities/" + name + ".json";

            JsonObject lootComp = new JsonObject();
            lootComp.addProperty("table", lootPath);
            components.add("minecraft:loot", lootComp);

            createLootTable(name);
        }

        entity.add("components", components);
        root.add("minecraft:entity", entity);

        saveJson(root, new File(project.getRootPath(), "BP/entities/" + name + ".json"));
    }

    private void createLootTable(String entityName) {
        JsonObject root = new JsonObject();
        JsonArray pools = new JsonArray();
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);

        JsonArray entries = new JsonArray();

        for (LootDrop drop : lootTable.getItems()) {
            JsonObject entry = new JsonObject();
            entry.addProperty("type", "item");
            entry.addProperty("name", drop.getItem());
            entry.addProperty("weight", 1);

            JsonArray functions = new JsonArray();
            JsonObject setCount = new JsonObject();
            setCount.addProperty("function", "set_count");
            JsonObject count = new JsonObject();
            count.addProperty("min", drop.getMin());
            count.addProperty("max", drop.getMax());
            setCount.add("count", count);
            functions.add(setCount);
            entry.add("functions", functions);

            if (drop.getChance() < 100.0) {
                JsonArray conditions = new JsonArray();
                JsonObject cond = new JsonObject();
                cond.addProperty("condition", "random_chance");
                cond.addProperty("chance", drop.getChance() / 100.0);
                conditions.add(cond);
                entry.add("conditions", conditions);
            }
            entries.add(entry);
        }

        pool.add("entries", entries);
        pools.add(pool);
        root.add("pools", pools);

        saveJson(root, new File(project.getRootPath(), "BP/loot_tables/entities/" + entityName + ".json"));
    }

    private void initialize3D() {
        root3D = new Group();
        modelGroup = new Group();
        root3D.getChildren().add(modelGroup);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(-50);

        subScene = new SubScene(root3D, 300, 300, true, SceneAntialiasing.DISABLED);
        subScene.setCamera(camera);
        subScene.setFill(Color.rgb(30, 30, 30));

        previewContainer.widthProperty().addListener((obs, old, val) -> subScene.setWidth(val.doubleValue()));
        previewContainer.heightProperty().addListener((obs, old, val) -> subScene.setHeight(val.doubleValue()));
        previewContainer.getChildren().add(subScene);

        modelGroup.getTransforms().addAll(rotateX, rotateY);
        rotateX.setAngle(20);
        rotateY.setAngle(-45);

        subScene.setOnMousePressed(event -> {
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - mouseOldX;
            double dy = event.getSceneY() - mouseOldY;
            if (event.isSecondaryButtonDown() || event.isPrimaryButtonDown()) {
                rotateY.setAngle(rotateY.getAngle() + dx * 0.5);
                rotateX.setAngle(rotateX.getAngle() - dy * 0.5);
            }
            mouseOldX = event.getSceneX();
            mouseOldY = event.getSceneY();
        });

        subScene.setOnScroll(event -> {
            double delta = event.getDeltaY();
            double z = camera.getTranslateZ();
            double newZ = z + delta * 0.1;
            newZ = Math.max(-200, Math.min(-2, newZ));
            camera.setTranslateZ(newZ);
        });
    }

    @FXML
    private void handleRefreshModels() {
        refreshAvailableModels();
    }

    @FXML
    private void handleImportCustomModel() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Bedrock Model", "*.json", "*.geo.json"));
        if (project != null) {
            File root = new File(project.getRootPath());
            if (root.exists())
                chooser.setInitialDirectory(root);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            localModelFile = f;
            baseModelCombo.getSelectionModel().clearSelection();
            baseModelCombo.setPromptText("Custom: " + f.getName());

            // Try to find texture automatically if in same folder
            File tex = new File(f.getParent(), f.getName().replace(".geo.json", ".png").replace(".json", ".png"));
            if (tex.exists()) {
                selectedTextureFile = tex;
                textureNameLabel.setText(tex.getName());
                texturePreview.setImage(new Image(tex.toURI().toString()));
            }

            updatePreview();
        }
    }

    private void refreshAvailableModels() {
        btnRefreshModels.setDisable(true);
        baseModelCombo.setPromptText("Loading models...");

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Fetch list only if empty
                if (availableModels.isEmpty()) {
                    List<String> models = BedrockSamplesDownloader.fetchModelList();
                    if (models != null)
                        availableModels = models;
                }
                if (availableTextures.isEmpty()) {
                    List<String> textures = BedrockSamplesDownloader.fetchTextureList();
                    if (textures != null)
                        availableTextures = textures;
                }
                return null;
            }

            @Override
            protected void succeeded() {
                updateModelCombo();
                btnRefreshModels.setDisable(false);
                baseModelCombo.setPromptText("Select Minecraft Base Model...");
            }

            @Override
            protected void failed() {
                logger.error("Failed to fetch models", getException());
                btnRefreshModels.setDisable(false);
                baseModelCombo.setPromptText("Failed to load models");
            }
        };
        new Thread(task).start();
    }

    private void updateModelCombo() {
        if (availableModels == null)
            return;
        List<String> names = availableModels.stream()
                .filter(p -> p.contains("/entity/") || p.contains("\\entity\\"))
                .map(this::extractModelName)
                .distinct()
                .sorted()
                .collect(Collectors.toList());

        baseModelCombo.setItems(FXCollections.observableArrayList(names));
    }

    private String extractModelName(String path) {
        String name = new File(path).getName();
        if (name.endsWith(".geo.json")) {
            return name.substring(0, name.length() - ".geo.json".length());
        } else if (name.endsWith(".json")) {
            return name.substring(0, name.length() - ".json".length());
        }
        return name;
    }

    private void handleModelSelection(String modelName) {
        Optional<String> modelPathOpt = availableModels.stream()
                .filter(p -> extractModelName(p).equals(modelName))
                .findFirst();

        if (modelPathOpt.isPresent()) {
            selectedModelPath = modelPathOpt.get();
            Optional<String> texPathOpt = availableTextures.stream()
                    .filter(p -> p.contains("/entity/") || p.contains("\\entity\\"))
                    .filter(p -> new File(p).getName().startsWith(modelName + "."))
                    .findFirst();

            selectedTexturePath = texPathOpt.orElse(null);
            downloadAndLoadSelectedModel(); // This must run to update preview
        }
    }

    private void downloadAndLoadSelectedModel() {
        if (project == null || selectedModelPath == null)
            return;

        // Create and show overlay
        LoadingSpinnerHelper.DownloadOverlay overlay = LoadingSpinnerHelper
                .createInteractiveDownloadOverlay("Descargando modelo...");
        
        // Find a place to show the overlay
        javafx.scene.Node rootNode = identifierField.getScene().getRoot();
        javafx.scene.layout.StackPane overlayContainer = null;
        
        if (rootNode instanceof javafx.scene.layout.StackPane) {
            overlayContainer = (javafx.scene.layout.StackPane) rootNode;
        } else if (rootNode instanceof javafx.scene.layout.BorderPane) {
             javafx.scene.layout.BorderPane bp = (javafx.scene.layout.BorderPane) rootNode;
             if (bp.getCenter() instanceof javafx.scene.layout.StackPane) {
                 overlayContainer = (javafx.scene.layout.StackPane) bp.getCenter();
             } else {
                 // Center is not a StackPane, wrap it!
                 javafx.scene.Node originalCenter = bp.getCenter();
                 javafx.scene.layout.StackPane newStack = new javafx.scene.layout.StackPane();
                 if (originalCenter != null) {
                     newStack.getChildren().add(originalCenter);
                 }
                 bp.setCenter(newStack);
                 overlayContainer = newStack;
             }
        }

        // Fallback: Try to find any StackPane parent if still null (e.g. if root is neither)
        if (overlayContainer == null) {
             javafx.scene.Parent parent = identifierField.getParent();
             while (parent != null) {
                 if (parent instanceof javafx.scene.layout.StackPane) {
                     overlayContainer = (javafx.scene.layout.StackPane) parent;
                     break;
                 }
                 parent = parent.getParent();
             }
        }

        final javafx.scene.layout.StackPane finalOverlayContainer = overlayContainer;
        if (finalOverlayContainer != null) {
            finalOverlayContainer.getChildren().add(overlay.getRoot());
        }

        java.util.concurrent.atomic.AtomicBoolean cancelled = new java.util.concurrent.atomic.AtomicBoolean(false);
        overlay.setOnCancel(() -> {
            cancelled.set(true);
            overlay.setProgress("Cancelando...");
        });

        Task<Void> task = new Task<>() {
            @Override
            protected Void call() throws Exception {
                File rpRoot = new File(project.getRootPath(), "RP");
                String modelRelPath = selectedModelPath.replace("resource_pack/", "");
                localModelFile = new File(rpRoot, modelRelPath);

                if (selectedTexturePath != null) {
                    String texRelPath = selectedTexturePath.replace("resource_pack/", "");
                    localTextureFile = new File(rpRoot, texRelPath);
                } else {
                    localTextureFile = null;
                }

                List<String> toDownload = new ArrayList<>();
                if (!localModelFile.exists())
                    toDownload.add(selectedModelPath);
                if (localTextureFile != null && !localTextureFile.exists())
                    toDownload.add(selectedTexturePath);

                if (!toDownload.isEmpty()) {
                    BedrockSamplesDownloader.downloadSpecificFiles(toDownload, Path.of(project.getRootPath()), 
                        (done, total) -> {
                            double percent = (double) done / total * 100.0;
                            overlay.setProgress(String.format("Archivo %d de %d (%.0f%%)", done, total, percent));
                        },
                        cancelled::get
                    );
                }
                return null;
            }

            @Override
            protected void succeeded() {
                if (finalOverlayContainer != null) {
                    finalOverlayContainer.getChildren().remove(overlay.getRoot());
                }
                
                if (cancelled.get()) {
                    return; 
                }

                if (localTextureFile != null && localTextureFile.exists()) {
                    selectedTextureFile = localTextureFile;
                    textureNameLabel.setText(localTextureFile.getName());
                    try {
                        texturePreview.setImage(loadTexture(localTextureFile));
                    } catch (Exception e) {
                    }
                }
                updatePreview();
            }

            @Override
            protected void failed() {
                if (finalOverlayContainer != null) {
                    finalOverlayContainer.getChildren().remove(overlay.getRoot());
                }
                if (!cancelled.get()) {
                    showAlert("Error", "Failed to download model files.");
                }
            }
        };
        new Thread(task).start();
    }

    private void updatePreview() {
        modelGroup.getChildren().clear();
        if (localModelFile != null && localModelFile.exists()) {
            try {
                Image tex = null;
                if (selectedTextureFile != null && selectedTextureFile.exists()) {
                    tex = loadTexture(selectedTextureFile);
                } else if (localTextureFile != null && localTextureFile.exists()) {
                    tex = loadTexture(localTextureFile);
                }
                Group model = BedrockModelLoader.loadModel(localModelFile, null, tex);
                modelGroup.getChildren().add(model);
            } catch (Exception e) {
                logger.error("Error loading model", e);
            }
        } else {
            Box box = new Box(16, 32, 16);
            box.setMaterial(new PhongMaterial(Color.RED));
            modelGroup.getChildren().add(box);
        }
    }

    @FXML
    private void handleSelectTexture() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.tga"));
        if (project != null) {
            File root = new File(project.getRootPath());
            if (root.exists())
                chooser.setInitialDirectory(root);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            selectedTextureFile = f;
            textureNameLabel.setText(f.getName());
            texturePreview.setImage(loadTexture(f));
            updatePreview();
        }
    }

    @FXML
    private void handleCreate() {
        if (project == null) {
            showAlert("Error", "No project loaded.");
            return;
        }
        String fullId = identifierField.getText().trim();
        if (fullId.isEmpty() || !fullId.contains(":")) {
            showAlert("Error", "Invalid identifier. Use format 'namespace:entityname'.");
            return;
        }
        String[] parts = fullId.split(":");
        String namespace = parts[0];
        String name = parts[1];

        createBPEntity(fullId, namespace, name);
        createRPEntity(fullId, namespace, name);

        showAlert("Success", isEditMode ? "Entity updated successfully!" : "Entity created successfully!");
        handleCancel();
    }

    private void createRPEntity(String fullId, String namespace, String name) {
        String textureName = "textures/entity/" + name + "/"
                + (selectedTextureFile != null ? selectedTextureFile.getName().replace(".png", "") : name);
        // Correct path construction
        // Actually, best to just put in textures/entity/name/name.png

        File texDir = new File(project.getRootPath(), "RP/textures/entity/" + name);
        if (!texDir.exists())
            texDir.mkdirs();

        if (selectedTextureFile != null) {
            try {
                File finalTextureFile = new File(texDir, selectedTextureFile.getName());
                Files.copy(selectedTextureFile.toPath(), finalTextureFile.toPath(),
                        java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                textureName = "textures/entity/" + name + "/" + selectedTextureFile.getName().replace(".png", "");
            } catch (IOException e) {
                logger.error("Failed to copy texture", e);
            }
        }
        // If we downloaded a custom model, we should copy it to project
        if (localModelFile != null) {
            File modelsDir = new File(project.getRootPath(), "RP/models/entity");
            if (!modelsDir.exists())
                modelsDir.mkdirs();
            // Only copy if it's not already there (e.g. downloaded to standard location)
            // But Custom Model Import might be from anywhere.
            // If downloaded from GitHub, it went to RP/models/entity/...
            // If imported custom, it might be anywhere.

            // Check if file is already inside project/RP
            if (!localModelFile.getAbsolutePath().startsWith(new File(project.getRootPath(), "RP").getAbsolutePath())) {
                File targetModel = new File(modelsDir, localModelFile.getName());
                try {
                    Files.copy(localModelFile.toPath(), targetModel.toPath(),
                            java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    localModelFile = targetModel;
                } catch (Exception e) {
                }
            }
        }

        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.10.0");

        JsonObject clientEntity = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", fullId);

        JsonObject materials = new JsonObject();
        materials.addProperty("default", "entity_alphatest");
        desc.add("materials", materials);

        JsonObject textures = new JsonObject();
        textures.addProperty("default", textureName);
        desc.add("textures", textures);

        JsonObject geometry = new JsonObject();
        String geoId = "geometry." + name;

        if (localModelFile != null && localModelFile.exists()) {
            try {
                JsonObject modelJson = JsonParser.parseReader(new FileReader(localModelFile)).getAsJsonObject();
                if (modelJson.has("minecraft:geometry")) {
                    JsonElement geo = modelJson.get("minecraft:geometry");
                    if (geo.isJsonArray() && geo.getAsJsonArray().size() > 0) {
                        geoId = geo.getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject("description")
                                .get("identifier").getAsString();
                    }
                }
            } catch (Exception e) {
            }
        }

        geometry.addProperty("default", geoId);
        desc.add("geometry", geometry);

        JsonArray renderControllers = new JsonArray();
        renderControllers.add("controller.render.default");
        desc.add("render_controllers", renderControllers);

        clientEntity.add("description", desc);
        root.add("minecraft:client_entity", clientEntity);

        saveJson(root, new File(project.getRootPath(), "RP/entity/" + name + ".entity.json"));
    }

    private Image loadTexture(File file) {
        if (file == null)
            return null;

        try {
            java.awt.image.BufferedImage bImg;
            if (file.getName().toLowerCase().endsWith(".tga")) {
                // Load TGA
                javafx.scene.image.WritableImage tgaImg = com.agustinbenitez.addoncreator.utils.TgaImageLoader
                        .loadTga(file);
                bImg = javafx.embed.swing.SwingFXUtils.fromFXImage(tgaImg, null);
            } else {
                bImg = javax.imageio.ImageIO.read(file);
            }

            if (bImg != null) {
                // Check if small (e.g. <= 128px) and upscale to ensure sharpness
                int w = bImg.getWidth();
                int h = bImg.getHeight();

                if (w <= 128 || h <= 128) {
                    int scale = 16; // Upscale significantly
                    int newW = w * scale;
                    int newH = h * scale;

                    java.awt.image.BufferedImage scaledImg = new java.awt.image.BufferedImage(newW, newH,
                            java.awt.image.BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = scaledImg.createGraphics();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.drawImage(bImg, 0, 0, newW, newH, null);
                    g2.dispose();
                    bImg = scaledImg;
                }

                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(bImg, "png", out);
                return new Image(new java.io.ByteArrayInputStream(out.toByteArray()), 0, 0, true, false);
            }

        } catch (Exception e) {
            logger.error("Failed to load texture: " + file.getName(), e);
        }

        // Fallback
        return new Image(file.toURI().toString(), 0, 0, true, false);
    }

    private void saveJson(JsonObject json, File file) {
        try {
            if (!file.getParentFile().exists())
                file.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(file)) {
                Gson gson = new GsonBuilder().setPrettyPrinting().create();
                gson.toJson(json, writer);
            }
        } catch (IOException e) {
            logger.error("Failed to save JSON", e);
            showAlert("Error", "Failed to save file: " + file.getName());
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) identifierField.getScene().getWindow();
        stage.close();
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
