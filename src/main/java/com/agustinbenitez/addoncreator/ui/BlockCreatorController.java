package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.converter.IntegerStringConverter;
import javafx.util.converter.DoubleStringConverter;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleDoubleProperty;

public class BlockCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(BlockCreatorController.class);

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
    private RadioButton rbCubeAll;
    @FXML
    private RadioButton rbCubeSided;
    @FXML
    private RadioButton rbCubeSix;
    @FXML
    private RadioButton rbCustom;
    @FXML
    private ToggleGroup modelTypeGroup;

    @FXML
    private VBox boxTextureAll;
    @FXML
    private Button btnTextureAll;
    @FXML
    private Button createButton;
    @FXML
    private ImageView imgTextureAll;

    @FXML
    private VBox boxTextureSided;
    @FXML
    private Button btnTextureTop;
    @FXML
    private ImageView imgTextureTop;
    @FXML
    private Button btnTextureBottom;
    @FXML
    private ImageView imgTextureBottom;
    @FXML
    private Button btnTextureSide;
    @FXML
    private ImageView imgTextureSide;

    @FXML
    private VBox boxTextureSix;
    @FXML
    private Button btnTextureUp;
    @FXML
    private ImageView imgTextureUp;
    @FXML
    private Button btnTextureDown;
    @FXML
    private ImageView imgTextureDown;
    @FXML
    private Button btnTextureNorth;
    @FXML
    private ImageView imgTextureNorth;
    @FXML
    private Button btnTextureSouth;
    @FXML
    private ImageView imgTextureSouth;
    @FXML
    private Button btnTextureEast;
    @FXML
    private ImageView imgTextureEast;
    @FXML
    private Button btnTextureWest;
    @FXML
    private ImageView imgTextureWest;

    @FXML
    private VBox boxCustomModel;
    @FXML
    private TextField modelPathField;
    @FXML
    private Button btnSelectModel;
    @FXML
    private Button btnTextureCustom;
    @FXML
    private ImageView imgTextureCustom;

    @FXML
    private Spinner<Double> destroyTimeSpinner;
    @FXML
    private Spinner<Double> explosionResistanceSpinner;
    @FXML
    private Spinner<Double> frictionSpinner;
    @FXML
    private Spinner<Integer> lightEmissionSpinner;
    @FXML
    private ColorPicker mapColorPicker;
    @FXML
    private ComboBox<String> soundCategoryCombo;
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
    private ComboBox<String> toolCombo;

    @FXML
    private Pane previewContainer;

    private Project project;
    private File fileTextureAll;
    private File fileTextureTop;
    private File fileTextureBottom;
    private File fileTextureSide;

    private File fileTextureUp;
    private File fileTextureDown;
    private File fileTextureNorth;
    private File fileTextureSouth;
    private File fileTextureEast;
    private File fileTextureWest;

    private File fileTextureCustom;
    private File fileCustomModel;
    private String customModelGeometryId;

    // 3D Preview
    private SubScene subScene;
    private Group root3D;
    private Group modelGroup;
    private Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private double mouseOldX, mouseOldY;

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

    private void initializeUI() {
        modelTypeGroup = new ToggleGroup();
        rbCubeAll.setToggleGroup(modelTypeGroup);
        rbCubeSided.setToggleGroup(modelTypeGroup);
        rbCubeSix.setToggleGroup(modelTypeGroup);
        rbCustom.setToggleGroup(modelTypeGroup);

        if (btnSelectModel != null) {
            btnSelectModel.setDisable(false);
        }

        modelTypeGroup.selectedToggleProperty().addListener((obs, oldVal, newVal) -> {
            updateVisibility();
            updatePreview();
        });

        // Spinners
        destroyTimeSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 1.5, 0.1));
        explosionResistanceSpinner
                .setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 100.0, 3.0, 0.1));
        frictionSpinner.setValueFactory(new SpinnerValueFactory.DoubleSpinnerValueFactory(0.0, 1.0, 0.6, 0.1));
        lightEmissionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 15, 0));

        // Sound Categories
        soundCategoryCombo.getItems().addAll("stone", "wood", "gravel", "grass", "metal", "glass", "cloth", "sand",
                "snow");
        soundCategoryCombo.getSelectionModel().select("stone");

        mapColorPicker.setValue(Color.GRAY);

        // Initialize Loot Table
        colItem.setCellValueFactory(new PropertyValueFactory<>("item"));
        colMin.setCellValueFactory(new PropertyValueFactory<>("min"));
        colMax.setCellValueFactory(new PropertyValueFactory<>("max"));
        colChance.setCellValueFactory(new PropertyValueFactory<>("chance"));

        lootTable.setEditable(true);

        colItem.setCellFactory(TextFieldTableCell.forTableColumn());
        colItem.setOnEditCommit(e -> e.getRowValue().setItem(e.getNewValue()));

        colMin.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colMin.setOnEditCommit(e -> e.getRowValue().setMin(e.getNewValue()));

        colMax.setCellFactory(TextFieldTableCell.forTableColumn(new IntegerStringConverter()));
        colMax.setOnEditCommit(e -> e.getRowValue().setMax(e.getNewValue()));

        colChance.setCellFactory(TextFieldTableCell.forTableColumn(new DoubleStringConverter()));
        colChance.setOnEditCommit(e -> e.getRowValue().setChance(e.getNewValue()));

        updateVisibility();
    }

    @FXML
    private void handleAddDrop() {
        ItemSelectionDialog dialog = new ItemSelectionDialog(project, "");
        dialog.showAndWait().ifPresent(selectedItem -> {
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

    private void updateVisibility() {
        boxTextureAll.setVisible(rbCubeAll.isSelected());
        boxTextureAll.setManaged(rbCubeAll.isSelected());

        boxTextureSided.setVisible(rbCubeSided.isSelected());
        boxTextureSided.setManaged(rbCubeSided.isSelected());

        boxTextureSix.setVisible(rbCubeSix.isSelected());
        boxTextureSix.setManaged(rbCubeSix.isSelected());

        boxCustomModel.setVisible(rbCustom.isSelected());
        boxCustomModel.setManaged(rbCustom.isSelected());
    }

    private void initialize3D() {
        root3D = new Group();
        modelGroup = new Group();
        root3D.getChildren().add(modelGroup);

        // Camera
        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(1000.0);
        camera.setTranslateZ(-50); // Move back to see the object

        // Use SceneAntialiasing.DISABLED for pixel art style
        subScene = new SubScene(root3D, 300, 300, true, SceneAntialiasing.DISABLED);
        subScene.setCamera(camera);
        subScene.setFill(Color.rgb(30, 30, 30));

        // Resize logic
        previewContainer.widthProperty().addListener((obs, old, val) -> subScene.setWidth(val.doubleValue()));
        previewContainer.heightProperty().addListener((obs, old, val) -> subScene.setHeight(val.doubleValue()));
        previewContainer.getChildren().add(subScene);

        // Rotation
        modelGroup.getTransforms().addAll(rotateX, rotateY);

        // Default Isometric View
        rotateX.setAngle(30);
        rotateY.setAngle(45);

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
            // Limit zoom range
            newZ = Math.max(-200, Math.min(-2, newZ));
            camera.setTranslateZ(newZ);
        });

        updatePreview();
    }

    private void updatePreview() {
        modelGroup.getChildren().clear();

        if (rbCubeAll.isSelected()) {
            modelGroup.getChildren().add(com.agustinbenitez.addoncreator.utils.BlockGeometryFactory.createCubeAll(16,
                    loadTexture(fileTextureAll)));
        } else if (rbCubeSided.isSelected()) {
            modelGroup.getChildren().add(com.agustinbenitez.addoncreator.utils.BlockGeometryFactory.createCubeSided(16,
                    loadTexture(fileTextureTop),
                    loadTexture(fileTextureBottom),
                    loadTexture(fileTextureSide)));
        } else if (rbCubeSix.isSelected()) {
            modelGroup.getChildren().add(com.agustinbenitez.addoncreator.utils.BlockGeometryFactory.createCubeSix(16,
                    loadTexture(fileTextureUp),
                    loadTexture(fileTextureDown),
                    loadTexture(fileTextureNorth),
                    loadTexture(fileTextureSouth),
                    loadTexture(fileTextureEast),
                    loadTexture(fileTextureWest)));
        } else if (rbCustom.isSelected()) {
            if (fileCustomModel != null) {
                try {
                    loadCustomModel(fileCustomModel);
                } catch (Exception e) {
                    logger.error("Failed to load custom model", e);
                    // Fallback
                    Box box = new Box(16, 16, 16);
                    box.setMaterial(new PhongMaterial(Color.RED));
                    modelGroup.getChildren().add(box);
                }
            } else {
                Box box = new Box(16, 16, 16);
                box.setMaterial(new PhongMaterial(Color.BLUE)); // Placeholder
                modelGroup.getChildren().add(box);
            }
        }
    }

    private void loadCustomModel(File jsonFile) throws IOException {
        Image texture = null;
        if (fileTextureCustom != null) {
            texture = loadTexture(fileTextureCustom);
        }

        Group loadedModel = com.agustinbenitez.addoncreator.utils.BedrockModelLoader.loadModel(jsonFile,
                customModelGeometryId, texture);

        // Center the model roughly
        // Bedrock models are usually centered around origin, but JavaFX 3D origin is at
        // center of container?
        // In initialize3D, we set camera TranslateZ -50.
        // And we rotate modelGroup.

        // Bedrock models can be large or offset.
        // Let's just add it.
        modelGroup.getChildren().add(loadedModel);
    }

    // Handlers
    public void setBlockData(File blockFile) {
        try {
            JsonObject root = JsonParser.parseReader(new FileReader(blockFile)).getAsJsonObject();
            JsonObject block = root.getAsJsonObject("minecraft:block");
            JsonObject desc = block.getAsJsonObject("description");
            String fullId = desc.get("identifier").getAsString();
            identifierField.setText(fullId);
            identifierField.setDisable(true); // Disable ID editing to avoid duplicates/renaming issues for now

            JsonObject components = block.getAsJsonObject("components");

            if (components.has("minecraft:destructible_by_mining")) {
                JsonElement elem = components.get("minecraft:destructible_by_mining");
                if (elem.isJsonPrimitive()) {
                    destroyTimeSpinner.getValueFactory().setValue(elem.getAsDouble());
                } else if (elem.isJsonObject() && elem.getAsJsonObject().has("value")) {
                    destroyTimeSpinner.getValueFactory().setValue(elem.getAsJsonObject().get("value").getAsDouble());
                }
            }
            if (components.has("minecraft:friction")) {
                JsonElement elem = components.get("minecraft:friction");
                if (elem.isJsonPrimitive()) {
                    frictionSpinner.getValueFactory().setValue(elem.getAsDouble());
                } else if (elem.isJsonObject() && elem.getAsJsonObject().has("value")) {
                    frictionSpinner.getValueFactory().setValue(elem.getAsJsonObject().get("value").getAsDouble());
                }
            }
            if (components.has("minecraft:light_emission")) {
                JsonElement elem = components.get("minecraft:light_emission");
                if (elem.isJsonPrimitive()) {
                    lightEmissionSpinner.getValueFactory().setValue(elem.getAsInt());
                } else if (elem.isJsonObject() && elem.getAsJsonObject().has("value")) {
                    lightEmissionSpinner.getValueFactory().setValue(elem.getAsJsonObject().get("value").getAsInt());
                }
            }
            if (components.has("minecraft:explosion_resistance")) {
                JsonElement elem = components.get("minecraft:explosion_resistance");
                if (elem.isJsonPrimitive()) {
                    explosionResistanceSpinner.getValueFactory().setValue(elem.getAsDouble());
                } else if (elem.isJsonObject() && elem.getAsJsonObject().has("value")) {
                    explosionResistanceSpinner.getValueFactory()
                            .setValue(elem.getAsJsonObject().get("value").getAsDouble());
                }
            }
            if (components.has("minecraft:map_color")) {
                JsonElement elem = components.get("minecraft:map_color");
                if (elem.isJsonPrimitive()) {
                    mapColorPicker.setValue(Color.web(elem.getAsString()));
                } else if (elem.isJsonObject() && elem.getAsJsonObject().has("value")) {
                    mapColorPicker.setValue(Color.web(elem.getAsJsonObject().get("value").getAsString()));
                }
            }
            if (components.has("minecraft:loot")) {
                JsonElement elem = components.get("minecraft:loot");
                String lootTablePath = null;
                if (elem.isJsonPrimitive()) {
                    lootTablePath = elem.getAsString();
                }

                if (lootTablePath != null) {
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

            // Detect Model Type and Load Textures
            String name = fullId.contains(":") ? fullId.split(":")[1] : fullId;
            File rpTexturesDir = new File(project.getRootPath(), "RP/textures/blocks");

            // Try to resolve textures
            // We need to check if it's sided, 6-faced, or all-same.
            // Check resolveTextureFromConfig logic again. It checks blocks.json.

            // Let's inspect blocks.json directly if possible or infer from keys.
            // But resolveTextureFromConfig returns a FILE given a key suffix.

            // Check for 6-faced keys
            File texUp = resolveTextureFromConfig(fullId, "up");
            File texDown = resolveTextureFromConfig(fullId, "down");
            File texNorth = resolveTextureFromConfig(fullId, "north");
            File texSouth = resolveTextureFromConfig(fullId, "south");
            File texEast = resolveTextureFromConfig(fullId, "east");
            File texWest = resolveTextureFromConfig(fullId, "west");

            // Check for Sided keys (up/down/side)
            File texSide = resolveTextureFromConfig(fullId, "side");

            // Check for All (null suffix)
            File texAll = resolveTextureFromConfig(fullId, null);

            if (texNorth != null || texSouth != null || texEast != null || texWest != null) {
                // It's 6-faced (or partial 6-faced)
                rbCubeSix.setSelected(true);

                if (texUp != null) {
                    fileTextureUp = texUp;
                    imgTextureUp.setImage(loadTexture(texUp));
                    imgTextureUp.setSmooth(false);
                }
                if (texDown != null) {
                    fileTextureDown = texDown;
                    imgTextureDown.setImage(loadTexture(texDown));
                    imgTextureDown.setSmooth(false);
                }
                if (texNorth != null) {
                    fileTextureNorth = texNorth;
                    imgTextureNorth.setImage(loadTexture(texNorth));
                    imgTextureNorth.setSmooth(false);
                }
                if (texSouth != null) {
                    fileTextureSouth = texSouth;
                    imgTextureSouth.setImage(loadTexture(texSouth));
                    imgTextureSouth.setSmooth(false);
                }
                if (texEast != null) {
                    fileTextureEast = texEast;
                    imgTextureEast.setImage(loadTexture(texEast));
                    imgTextureEast.setSmooth(false);
                }
                if (texWest != null) {
                    fileTextureWest = texWest;
                    imgTextureWest.setImage(loadTexture(texWest));
                    imgTextureWest.setSmooth(false);
                }

            } else if (texSide != null || (texUp != null && texDown != null)) {
                // It's Sided (Top/Bottom/Side)
                rbCubeSided.setSelected(true);

                if (texUp != null) {
                    fileTextureTop = texUp;
                    imgTextureTop.setImage(loadTexture(texUp));
                    imgTextureTop.setSmooth(false);
                }
                if (texDown != null) {
                    fileTextureBottom = texDown;
                    imgTextureBottom.setImage(loadTexture(texDown));
                    imgTextureBottom.setSmooth(false);
                }
                if (texSide != null) {
                    fileTextureSide = texSide;
                    imgTextureSide.setImage(loadTexture(texSide));
                    imgTextureSide.setSmooth(false);
                }

            } else {
                // Check geometry before assuming Cube All
                boolean isCustom = false;
                if (components.has("minecraft:geometry")) {
                    String geoId = components.get("minecraft:geometry").getAsString();
                    if (!geoId.equals("geometry.box")) {
                        isCustom = true;
                        customModelGeometryId = geoId;
                        File modelFile = findGeometryFile(geoId);
                        if (modelFile != null) {
                            fileCustomModel = modelFile;
                            modelPathField.setText(modelFile.getName());
                        }
                        
                        if (btnSelectModel != null) {
                            btnSelectModel.setDisable(true);
                        }
                    }
                }

                if (isCustom) {
                    rbCustom.setSelected(true);
                    if (texAll != null) {
                        fileTextureCustom = texAll;
                        imgTextureCustom.setImage(loadTexture(texAll));
                        imgTextureCustom.setSmooth(false);
                    }
                } else {
                    rbCubeAll.setSelected(true);
                    if (texAll != null) {
                        fileTextureAll = texAll;
                        imgTextureAll.setImage(loadTexture(texAll));
                        imgTextureAll.setSmooth(false);
                    }
                }
            }

            updatePreview();

            updatePreview();

            if (createButton != null) {
                createButton.setText("Save Changes");
            }

        } catch (Exception e) {
            logger.error("Error loading block data", e);
            showAlert("Error", "Failed to load block data: " + e.getMessage());
        }
    }

    @FXML
    private void handleSelectTextureAll() {
        fileTextureAll = selectImage(imgTextureAll);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureTop() {
        fileTextureTop = selectImage(imgTextureTop);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureBottom() {
        fileTextureBottom = selectImage(imgTextureBottom);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureSide() {
        fileTextureSide = selectImage(imgTextureSide);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureUp() {
        fileTextureUp = selectImage(imgTextureUp);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureDown() {
        fileTextureDown = selectImage(imgTextureDown);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureNorth() {
        fileTextureNorth = selectImage(imgTextureNorth);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureSouth() {
        fileTextureSouth = selectImage(imgTextureSouth);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureEast() {
        fileTextureEast = selectImage(imgTextureEast);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureWest() {
        fileTextureWest = selectImage(imgTextureWest);
        updatePreview();
    }

    @FXML
    private void handleSelectTextureCustom() {
        fileTextureCustom = selectImage(imgTextureCustom);
        updatePreview();
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
                // Check if small (e.g. <= 64px) and upscale to ensure sharpness
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

    private File selectImage(ImageView view) {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Images", "*.png", "*.jpg", "*.tga"));
        if (project != null) {
            File root = new File(project.getRootPath());
            if (root.exists())
                chooser.setInitialDirectory(root);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            view.setImage(loadTexture(f));
            view.setSmooth(false); // Disable smoothing for the preview image
            return f;
        }
        return null;
    }

    @FXML
    private void handleSelectModel() {
        FileChooser chooser = new FileChooser();
        chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("JSON Geometry", "*.json"));
        if (project != null) {
            File root = new File(project.getRootPath());
            if (root.exists())
                chooser.setInitialDirectory(root);
        }
        File f = chooser.showOpenDialog(null);
        if (f != null) {
            fileCustomModel = f;
            modelPathField.setText(f.getName());
            updatePreview();
        }
    }

    @FXML
    private void handleCancel() {
        Stage stage = (Stage) identifierField.getScene().getWindow();
        stage.close();
    }

    @FXML
    private void handleCreate() {
        if (project == null) {
            showAlert("Error", "No project loaded.");
            return;
        }
        String fullId = identifierField.getText().trim();
        if (fullId.isEmpty() || !fullId.contains(":")) {
            showAlert("Error", "Invalid identifier. Use format 'namespace:blockname'.");
            return;
        }
        String[] parts = fullId.split(":");
        String namespace = parts[0];
        String name = parts[1];

        // 1. Create BP Block JSON
        createBPBlock(fullId, namespace, name);

        // 2. Create RP Textures & Definitions
        createRPBlock(fullId, namespace, name);

        showAlert("Success", "Block created successfully!");
        handleCancel();
    }

    private void createLootTable(String blockName) {
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

        File lootFile = new File(project.getRootPath(), "BP/loot_tables/blocks/" + blockName + ".json");
        if (lootFile.getParentFile() != null)
            lootFile.getParentFile().mkdirs();
        saveJson(root, lootFile);
    }

    private void createBPBlock(String fullId, String namespace, String name) {
        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.20.0");

        JsonObject block = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", fullId);
        block.add("description", desc);

        JsonObject components = new JsonObject();
        components.addProperty("minecraft:destructible_by_mining", destroyTimeSpinner.getValue());
        components.addProperty("minecraft:friction", frictionSpinner.getValue());
        components.addProperty("minecraft:light_emission", lightEmissionSpinner.getValue());
        components.addProperty("minecraft:explosion_resistance", explosionResistanceSpinner.getValue());

        // Map Color
        Color c = mapColorPicker.getValue();
        String hex = String.format("#%02X%02X%02X",
                (int) (c.getRed() * 255), (int) (c.getGreen() * 255), (int) (c.getBlue() * 255));
        components.addProperty("minecraft:map_color", hex);

        // Loot Table
        if (!lootTable.getItems().isEmpty()) {
            createLootTable(name);
            components.addProperty("minecraft:loot", "loot_tables/blocks/" + name + ".json");
        }

        // Geometry (if custom)
        if (rbCustom.isSelected() && fileCustomModel != null) {
            // Need to check geometry identifier in file
            try {
                JsonObject geoRoot = JsonParser.parseReader(new FileReader(fileCustomModel)).getAsJsonObject();
                if (geoRoot.has("minecraft:geometry")) {
                    JsonElement geo = geoRoot.get("minecraft:geometry");
                    String geoId = "";
                    if (geo.isJsonArray()) {
                        geoId = geo.getAsJsonArray().get(0).getAsJsonObject().getAsJsonObject("description")
                                .get("identifier").getAsString();
                    }
                    if (!geoId.isEmpty()) {
                        components.addProperty("minecraft:geometry", geoId);

                        // Also components needs material instances?
                        // For blocks, geometry is usually enough if it matches resource pack
                        // definition.
                        // Actually, components: {"minecraft:geometry": "geometry.foo"}
                    }
                }
            } catch (Exception e) {
                logger.warn("Could not read geometry id", e);
            }
        } else {
            components.addProperty("minecraft:geometry", "geometry.box"); // Default
        }

        block.add("components", components);
        root.add("minecraft:block", block);

        saveJson(root, new File(project.getRootPath(), "BP/blocks/" + name + ".json"));
    }

    private void createSimpleLootTable(String path, String item) {
        JsonObject root = new JsonObject();
        JsonObject pools = new JsonObject();
        JsonArray poolArray = new JsonArray();
        JsonObject pool = new JsonObject();
        pool.addProperty("rolls", 1);
        JsonArray entries = new JsonArray();
        JsonObject entry = new JsonObject();
        entry.addProperty("type", "item");
        entry.addProperty("name", item);
        entry.addProperty("weight", 1);
        entries.add(entry);
        pool.add("entries", entries);
        poolArray.add(pool);
        root.add("pools", poolArray);

        File f = new File(project.getRootPath(), "BP/" + path);
        if (!f.getParentFile().exists())
            f.getParentFile().mkdirs();
        saveJson(root, f);
    }

    private void createRPBlock(String fullId, String namespace, String name) {
        // 1. Copy textures
        File texturesDir = new File(project.getRootPath(), "RP/textures/blocks");
        if (!texturesDir.exists())
            texturesDir.mkdirs();

        String textureNameMain = name;
        String textureNameTop = name + "_top";
        String textureNameBottom = name + "_bottom";
        String textureNameSide = name + "_side";

        if (rbCubeAll.isSelected() && fileTextureAll != null) {
            String ext = getExtension(fileTextureAll);
            copyFile(fileTextureAll, new File(texturesDir, name + ext));
        } else if (rbCubeSided.isSelected()) {
            if (fileTextureTop != null) {
                String ext = getExtension(fileTextureTop);
                copyFile(fileTextureTop, new File(texturesDir, textureNameTop + ext));
            }
            if (fileTextureBottom != null) {
                String ext = getExtension(fileTextureBottom);
                copyFile(fileTextureBottom, new File(texturesDir, textureNameBottom + ext));
            }
            if (fileTextureSide != null) {
                String ext = getExtension(fileTextureSide);
                copyFile(fileTextureSide, new File(texturesDir, textureNameSide + ext));
            }
        } else if (rbCubeSix.isSelected()) {
            if (fileTextureUp != null)
                copyFile(fileTextureUp, new File(texturesDir, name + "_up" + getExtension(fileTextureUp)));
            if (fileTextureDown != null)
                copyFile(fileTextureDown, new File(texturesDir, name + "_down" + getExtension(fileTextureDown)));
            if (fileTextureNorth != null)
                copyFile(fileTextureNorth, new File(texturesDir, name + "_north" + getExtension(fileTextureNorth)));
            if (fileTextureSouth != null)
                copyFile(fileTextureSouth, new File(texturesDir, name + "_south" + getExtension(fileTextureSouth)));
            if (fileTextureEast != null)
                copyFile(fileTextureEast, new File(texturesDir, name + "_east" + getExtension(fileTextureEast)));
            if (fileTextureWest != null)
                copyFile(fileTextureWest, new File(texturesDir, name + "_west" + getExtension(fileTextureWest)));
        } else if (rbCustom.isSelected() && fileTextureCustom != null) {
            String ext = getExtension(fileTextureCustom);
            copyFile(fileTextureCustom, new File(texturesDir, name + ext));
        }

        // 2. blocks.json (Register texture mapping)
        File blocksJsonFile = new File(project.getRootPath(), "RP/blocks.json");
        JsonObject blocksJsonRoot = loadJsonOrNew(blocksJsonFile);
        if (!blocksJsonRoot.has("format_version"))
            blocksJsonRoot.addProperty("format_version", "1.8.0");

        // This is usually a root object where keys are block identifiers.
        // Wait, format 1.8.0 structure:
        // { "format_version": "1.8.0", "mypack:block": { "textures": "..." } }

        JsonObject blockDef = new JsonObject();
        blockDef.addProperty("sound", soundCategoryCombo.getValue());

        if (rbCubeAll.isSelected()) {
            blockDef.addProperty("textures", textureNameMain);
        } else if (rbCubeSided.isSelected()) {
            JsonObject textures = new JsonObject();
            textures.addProperty("up", textureNameTop);
            textures.addProperty("down", textureNameBottom);
            textures.addProperty("side", textureNameSide);
            blockDef.add("textures", textures);
        } else if (rbCubeSix.isSelected()) {
            JsonObject textures = new JsonObject();
            if (fileTextureUp != null)
                textures.addProperty("up", name + "_up");
            if (fileTextureDown != null)
                textures.addProperty("down", name + "_down");
            if (fileTextureNorth != null)
                textures.addProperty("north", name + "_north");
            if (fileTextureSouth != null)
                textures.addProperty("south", name + "_south");
            if (fileTextureEast != null)
                textures.addProperty("east", name + "_east");
            if (fileTextureWest != null)
                textures.addProperty("west", name + "_west");
            blockDef.add("textures", textures);
        } else if (rbCustom.isSelected()) {
            blockDef.addProperty("textures", textureNameMain);
        }

        blocksJsonRoot.add(fullId, blockDef);
        saveJson(blocksJsonRoot, blocksJsonFile);

        // 3. terrain_texture.json (Register texture paths)
        File terrainFile = new File(project.getRootPath(), "RP/textures/terrain_texture.json");
        JsonObject terrainRoot = loadJsonOrNew(terrainFile);
        if (!terrainRoot.has("resource_pack_name"))
            terrainRoot.addProperty("resource_pack_name", "vanilla");
        if (!terrainRoot.has("texture_name"))
            terrainRoot.addProperty("texture_name", "atlas.terrain");
        if (!terrainRoot.has("padding"))
            terrainRoot.addProperty("padding", 8);
        if (!terrainRoot.has("num_mip_levels"))
            terrainRoot.addProperty("num_mip_levels", 4);

        JsonObject textureData = terrainRoot.has("texture_data") ? terrainRoot.getAsJsonObject("texture_data")
                : new JsonObject();

        if (rbCubeAll.isSelected() || rbCustom.isSelected()) {
            addTextureData(textureData, textureNameMain, "textures/blocks/" + name);
        } else if (rbCubeSided.isSelected()) {
            addTextureData(textureData, textureNameTop, "textures/blocks/" + textureNameTop);
            addTextureData(textureData, textureNameBottom, "textures/blocks/" + textureNameBottom);
            addTextureData(textureData, textureNameSide, "textures/blocks/" + textureNameSide);
        } else if (rbCubeSix.isSelected()) {
            if (fileTextureUp != null)
                addTextureData(textureData, name + "_up", "textures/blocks/" + name + "_up");
            if (fileTextureDown != null)
                addTextureData(textureData, name + "_down", "textures/blocks/" + name + "_down");
            if (fileTextureNorth != null)
                addTextureData(textureData, name + "_north", "textures/blocks/" + name + "_north");
            if (fileTextureSouth != null)
                addTextureData(textureData, name + "_south", "textures/blocks/" + name + "_south");
            if (fileTextureEast != null)
                addTextureData(textureData, name + "_east", "textures/blocks/" + name + "_east");
            if (fileTextureWest != null)
                addTextureData(textureData, name + "_west", "textures/blocks/" + name + "_west");
        }

        terrainRoot.add("texture_data", textureData);
        saveJson(terrainRoot, terrainFile);

        // 4. If Custom Model, copy geometry file
        if (rbCustom.isSelected() && fileCustomModel != null) {
            File modelsDir = new File(project.getRootPath(), "RP/models/blocks");
            if (!modelsDir.exists())
                modelsDir.mkdirs();
            copyFile(fileCustomModel, new File(modelsDir, fileCustomModel.getName()));
        }
    }

    private void addTextureData(JsonObject data, String key, String path) {
        JsonObject tex = new JsonObject();
        tex.addProperty("textures", path);
        data.add(key, tex);
    }

    private JsonObject loadJsonOrNew(File file) {
        if (file.exists()) {
            try {
                return JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
            } catch (Exception e) {
                return new JsonObject();
            }
        }
        return new JsonObject();
    }

    private void saveJson(JsonObject json, File file) {
        try (FileWriter writer = new FileWriter(file)) {
            new GsonBuilder().setPrettyPrinting().create().toJson(json, writer);
        } catch (IOException e) {
            logger.error("Failed to save JSON", e);
        }
    }

    private void copyFile(File src, File dest) {
        try {
            Files.copy(src.toPath(), dest.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            logger.error("Failed to copy file", e);
        }
    }

    private String getExtension(File file) {
        String name = file.getName();
        int lastIndexOf = name.lastIndexOf(".");
        if (lastIndexOf == -1) {
            return "";
        }
        return name.substring(lastIndexOf);
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private File findGeometryFile(String geometryId) {
        if (project == null)
            return null;
        // Start scanning from RP/models to catch files in root or subfolders (like
        // blocks/)
        File modelsDir = new File(project.getRootPath(), "RP/models");
        if (!modelsDir.exists() || !modelsDir.isDirectory())
            return null;

        return scanForGeometryRecursively(modelsDir, geometryId);
    }

    private File scanForGeometryRecursively(File dir, String geometryId) {
        File[] files = dir.listFiles();
        if (files == null)
            return null;

        for (File f : files) {
            if (f.isDirectory()) {
                File found = scanForGeometryRecursively(f, geometryId);
                if (found != null)
                    return found;
            } else if (f.getName().toLowerCase().endsWith(".json")) {
                try {
                    JsonObject json = JsonParser.parseReader(new FileReader(f)).getAsJsonObject();
                    if (json.has("minecraft:geometry")) {
                        JsonElement geo = json.get("minecraft:geometry");
                        if (geo.isJsonArray()) {
                            for (JsonElement e : geo.getAsJsonArray()) {
                                if (e.isJsonObject() && e.getAsJsonObject().has("description")) {
                                    JsonObject desc = e.getAsJsonObject().getAsJsonObject("description");
                                    if (desc.has("identifier")
                                            && desc.get("identifier").getAsString().equals(geometryId)) {
                                        return f;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore invalid files
                }
            }
        }
        return null;
    }

    private File resolveTextureFromConfig(String blockId, String suffix) {
        if (project == null)
            return null;
        try {
            File blocksJson = new File(project.getRootPath(), "RP/blocks.json");
            if (!blocksJson.exists())
                return null;

            JsonObject blocksRoot = JsonParser.parseReader(new FileReader(blocksJson)).getAsJsonObject();
            if (!blocksRoot.has(blockId))
                return null;

            JsonObject def = blocksRoot.getAsJsonObject(blockId);
            if (!def.has("textures"))
                return null;

            JsonElement textures = def.get("textures");
            String textureKey = null;

            if (textures.isJsonPrimitive()) {
                textureKey = textures.getAsString();
            } else if (textures.isJsonObject()) {
                JsonObject tObj = textures.getAsJsonObject();
                if (suffix != null && tObj.has(suffix)) {
                    textureKey = tObj.get(suffix).getAsString();
                } else if (suffix == null) {
                    // Try "up", "down", "side" order or just pick one
                    if (tObj.has("up"))
                        textureKey = tObj.get("up").getAsString();
                    else if (tObj.has("down"))
                        textureKey = tObj.get("down").getAsString();
                    else if (tObj.has("side"))
                        textureKey = tObj.get("side").getAsString();
                    else if (tObj.keySet().size() > 0)
                        textureKey = tObj.get(tObj.keySet().iterator().next()).getAsString();
                }
            }

            if (textureKey == null)
                return null;

            File terrainFile = new File(project.getRootPath(), "RP/textures/terrain_texture.json");
            if (!terrainFile.exists())
                return null;

            JsonObject terrainRoot = JsonParser.parseReader(new FileReader(terrainFile)).getAsJsonObject();
            JsonObject data = null;
            if (terrainRoot.has("texture_data")) {
                data = terrainRoot.getAsJsonObject("texture_data");
            } else {
                return null;
            }

            if (!data.has(textureKey))
                return null;

            JsonObject texEntry = data.getAsJsonObject(textureKey);
            if (!texEntry.has("textures"))
                return null;

            JsonElement pathElem = texEntry.get("textures");
            String path = null;
            if (pathElem.isJsonPrimitive()) {
                path = pathElem.getAsString();
            } else if (pathElem.isJsonArray()) {
                path = pathElem.getAsJsonArray().get(0).getAsString();
            } else if (pathElem.isJsonObject()) {
                if (pathElem.getAsJsonObject().has("path"))
                    path = pathElem.getAsJsonObject().get("path").getAsString();
            }

            if (path == null)
                return null;

            File rpDir = new File(project.getRootPath(), "RP");
            File texFile = new File(rpDir, path + ".png");
            if (texFile.exists())
                return texFile;

            texFile = new File(rpDir, path + ".tga");
            if (texFile.exists())
                return texFile;

            texFile = new File(rpDir, path + ".jpg");
            if (texFile.exists())
                return texFile;

            texFile = new File(rpDir, path + ".jpeg");
            if (texFile.exists())
                return texFile;

        } catch (Exception e) {
            logger.error("Error resolving texture", e);
        }
        return null;
    }
}
