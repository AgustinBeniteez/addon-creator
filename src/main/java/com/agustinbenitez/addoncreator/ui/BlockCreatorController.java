package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;

public class BlockCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(BlockCreatorController.class);

    @FXML
    private TextField identifierField;
    @FXML
    private RadioButton rbCubeAll;
    @FXML
    private RadioButton rbCubeSided;
    @FXML
    private RadioButton rbCustom;
    @FXML
    private ToggleGroup modelTypeGroup;

    @FXML
    private VBox boxTextureAll;
    @FXML
    private Button btnTextureAll;
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
    private VBox boxCustomModel;
    @FXML
    private TextField modelPathField;
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
    private TextField lootTableField;
    @FXML
    private ComboBox<String> toolCombo;
    @FXML
    private TextField dropItemField;

    @FXML
    private Pane previewContainer;

    private Project project;
    private File fileTextureAll;
    private File fileTextureTop;
    private File fileTextureBottom;
    private File fileTextureSide;
    private File fileTextureCustom;
    private File fileCustomModel;

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
    }

    private void initializeUI() {
        modelTypeGroup = new ToggleGroup();
        rbCubeAll.setToggleGroup(modelTypeGroup);
        rbCubeSided.setToggleGroup(modelTypeGroup);
        rbCustom.setToggleGroup(modelTypeGroup);

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

        updateVisibility();
    }

    private void updateVisibility() {
        boxTextureAll.setVisible(rbCubeAll.isSelected());
        boxTextureAll.setManaged(rbCubeAll.isSelected());

        boxTextureSided.setVisible(rbCubeSided.isSelected());
        boxTextureSided.setManaged(rbCubeSided.isSelected());

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
            Box box = new Box(16, 16, 16);
            PhongMaterial material = new PhongMaterial();
            if (fileTextureAll != null) {
                material.setDiffuseMap(loadTexture(fileTextureAll));
            } else {
                material.setDiffuseColor(Color.GRAY);
            }
            box.setMaterial(material);
            modelGroup.getChildren().add(box);
        } else if (rbCubeSided.isSelected()) {
            // Build a cube from 6 planes to support different textures
            double size = 16;
            double half = size / 2;

            PhongMaterial matTop = new PhongMaterial(Color.GRAY);
            if (fileTextureTop != null)
                matTop.setDiffuseMap(loadTexture(fileTextureTop));

            PhongMaterial matBottom = new PhongMaterial(Color.GRAY);
            if (fileTextureBottom != null)
                matBottom.setDiffuseMap(loadTexture(fileTextureBottom));

            PhongMaterial matSide = new PhongMaterial(Color.GRAY);
            if (fileTextureSide != null)
                matSide.setDiffuseMap(loadTexture(fileTextureSide));

            // Top
            Box top = new Box(size, 0.1, size);
            top.setTranslateY(-half);
            top.setMaterial(matTop);

            // Bottom
            Box bottom = new Box(size, 0.1, size);
            bottom.setTranslateY(half);
            bottom.setMaterial(matBottom);

            // North
            Box north = new Box(size, size, 0.1);
            north.setTranslateZ(half);
            north.setMaterial(matSide);

            // South
            Box south = new Box(size, size, 0.1);
            south.setTranslateZ(-half);
            south.setMaterial(matSide);

            // East
            Box east = new Box(0.1, size, size);
            east.setTranslateX(half);
            east.setMaterial(matSide);

            // West
            Box west = new Box(0.1, size, size);
            west.setTranslateX(-half);
            west.setMaterial(matSide);

            modelGroup.getChildren().addAll(top, bottom, north, south, east, west);
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
        JsonObject root = JsonParser.parseReader(new FileReader(jsonFile)).getAsJsonObject();

        // Find geometry
        JsonObject geometry = null;
        if (root.has("minecraft:geometry")) {
            JsonElement geo = root.get("minecraft:geometry");
            if (geo.isJsonArray()) {
                geometry = geo.getAsJsonArray().get(0).getAsJsonObject();
            }
        }

        if (geometry == null)
            return;

        PhongMaterial material = new PhongMaterial();
        if (fileTextureCustom != null) {
            material.setDiffuseMap(loadTexture(fileTextureCustom));
        } else {
            material.setDiffuseColor(Color.WHITE);
        }

        if (geometry.has("bones")) {
            JsonArray bones = geometry.getAsJsonArray("bones");
            for (JsonElement boneElem : bones) {
                JsonObject bone = boneElem.getAsJsonObject();
                if (bone.has("cubes")) {
                    JsonArray cubes = bone.getAsJsonArray("cubes");
                    for (JsonElement cubeElem : cubes) {
                        JsonObject cube = cubeElem.getAsJsonObject();

                        // Parse Origin
                        JsonArray origin = cube.getAsJsonArray("origin");
                        double ox = origin.get(0).getAsDouble();
                        double oy = origin.get(1).getAsDouble();
                        double oz = origin.get(2).getAsDouble();

                        // Parse Size
                        JsonArray size = cube.getAsJsonArray("size");
                        double sx = size.get(0).getAsDouble();
                        double sy = size.get(1).getAsDouble();
                        double sz = size.get(2).getAsDouble();

                        // JavaFX Box is centered at 0,0,0
                        // Bedrock Origin is bottom-north-west corner relative to pivot?
                        // This is a rough approximation.

                        Box box = new Box(sx, sy, sz);
                        box.setMaterial(material);

                        // Position
                        // JavaFX Y is down, Bedrock Y is up.
                        // We need to invert Y.

                        box.setTranslateX(ox + sx / 2 - 8); // -8 to center in 16x16 grid
                        box.setTranslateY(-(oy + sy / 2)); // Invert Y
                        box.setTranslateZ(oz + sz / 2 - 8);

                        // Rotation (Pivot) - Skipping complex rotation for now

                        modelGroup.getChildren().add(box);
                    }
                }
            }
        }
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
                destroyTimeSpinner.getValueFactory()
                        .setValue(components.get("minecraft:destructible_by_mining").getAsDouble());
            }
            if (components.has("minecraft:friction")) {
                frictionSpinner.getValueFactory().setValue(components.get("minecraft:friction").getAsDouble());
            }
            if (components.has("minecraft:light_emission")) {
                lightEmissionSpinner.getValueFactory().setValue(components.get("minecraft:light_emission").getAsInt());
            }
            if (components.has("minecraft:explosion_resistance")) {
                explosionResistanceSpinner.getValueFactory()
                        .setValue(components.get("minecraft:explosion_resistance").getAsDouble());
            }
            if (components.has("minecraft:map_color")) {
                mapColorPicker.setValue(Color.web(components.get("minecraft:map_color").getAsString()));
            }
            if (components.has("minecraft:loot")) {
                lootTableField.setText(components.get("minecraft:loot").getAsString());
            }

            // Detect Model Type and Load Textures
            String name = fullId.contains(":") ? fullId.split(":")[1] : fullId;
            File rpTexturesDir = new File(project.getRootPath(), "RP/textures/blocks");

            File texAll = findTextureFile(rpTexturesDir, name);
            File texTop = findTextureFile(rpTexturesDir, name + "_top");

            if (texTop != null && texTop.exists()) {
                rbCubeSided.setSelected(true);
                fileTextureTop = texTop;
                imgTextureTop.setImage(loadTexture(texTop));
                imgTextureTop.setSmooth(false);

                File texBottom = findTextureFile(rpTexturesDir, name + "_bottom");
                if (texBottom != null && texBottom.exists()) {
                    fileTextureBottom = texBottom;
                    imgTextureBottom.setImage(loadTexture(texBottom));
                    imgTextureBottom.setSmooth(false);
                }

                File texSide = findTextureFile(rpTexturesDir, name + "_side");
                if (texSide != null && texSide.exists()) {
                    fileTextureSide = texSide;
                    imgTextureSide.setImage(loadTexture(texSide));
                    imgTextureSide.setSmooth(false);
                }
            } else if (texAll != null && texAll.exists()) {
                // Could be Cube All or Custom
                // Check geometry
                if (components.has("minecraft:geometry")
                        && !components.get("minecraft:geometry").getAsString().equals("geometry.box")) {
                    rbCustom.setSelected(true);
                    fileTextureCustom = texAll;
                    imgTextureCustom.setImage(loadTexture(texAll));
                    imgTextureCustom.setSmooth(false);
                } else {
                    rbCubeAll.setSelected(true);
                    fileTextureAll = texAll;
                    imgTextureAll.setImage(loadTexture(texAll));
                    imgTextureAll.setSmooth(false);
                }
            }

            updatePreview();

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
    private void handleSelectTextureCustom() {
        fileTextureCustom = selectImage(imgTextureCustom);
        updatePreview();
    }

    private Image loadTexture(File file) {
        if (file == null)
            return null;

        try {
            if (file.getName().toLowerCase().endsWith(".tga")) {
                // Load TGA
                javafx.scene.image.WritableImage tgaImg = com.agustinbenitez.addoncreator.utils.TgaImageLoader
                        .loadTga(file);

                // Convert to sharp Image via stream to enforce smooth=false
                java.awt.image.BufferedImage bImg = javafx.embed.swing.SwingFXUtils.fromFXImage(tgaImg, null);
                java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
                javax.imageio.ImageIO.write(bImg, "png", out);
                return new Image(new java.io.ByteArrayInputStream(out.toByteArray()), 0, 0, true, false);
            }
        } catch (Exception e) {
            logger.error("Failed to load texture: " + file.getName(), e);
        }

        // smooth=false is crucial for pixel art to appear sharp (Nearest Neighbor)
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
        if (lootTableField.getText() != null && !lootTableField.getText().isEmpty()) {
            components.addProperty("minecraft:loot", lootTableField.getText());
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

    private File findTextureFile(File dir, String baseName) {
        if (!dir.exists())
            return null;
        File png = new File(dir, baseName + ".png");
        if (png.exists())
            return png;
        File tga = new File(dir, baseName + ".tga");
        if (tga.exists())
            return tga;
        File jpg = new File(dir, baseName + ".jpg");
        if (jpg.exists())
            return jpg;
        File jpeg = new File(dir, baseName + ".jpeg");
        if (jpeg.exists())
            return jpeg;
        return null;
    }

    private void showAlert(String title, String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}
