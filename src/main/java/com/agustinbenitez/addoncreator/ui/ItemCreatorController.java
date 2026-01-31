package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.awt.image.BufferedImage;
import javafx.embed.swing.SwingFXUtils;
import javax.imageio.ImageIO;
import com.agustinbenitez.addoncreator.utils.TgaImageLoader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class ItemCreatorController {

    private static final Logger logger = LoggerFactory.getLogger(ItemCreatorController.class);

    @FXML
    private TextField identifierField;
    @FXML
    private TextField nameField;

    @FXML
    private Button textureButton;
    @FXML
    private Button createButton;
    @FXML
    private StackPane texturePreviewPane;
    @FXML
    private ImageView texturePreview;
    @FXML
    private Label textureNameLabel;

    @FXML
    private CheckBox glintCheck;
    @FXML
    private Spinner<Integer> stackSizeSpinner;
    @FXML
    private CheckBox handEquippedCheck;
    @FXML
    private Spinner<Integer> damageSpinner;

    @FXML
    private CheckBox isFoodCheck;
    @FXML
    private GridPane foodGrid;
    @FXML
    private Spinner<Integer> nutritionSpinner;
    @FXML
    private ComboBox<String> saturationCombo;
    @FXML
    private CheckBox canAlwaysEatCheck;

    @FXML
    private ImageView largeTexturePreview;
    @FXML
    private Label previewNameLabel;
    @FXML
    private Label previewStackLabel;
    @FXML
    private Label previewIdLabel;

    private Project project;
    private File selectedTextureFile;
    private String selectedTextureName;

    private String projectNamespace = "mypack";

    @FXML
    public void initialize() {
        stackSizeSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 64, 64));
        damageSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 100, 0));
        nutritionSpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 20, 4));

        saturationCombo.getItems().addAll("poor", "low", "normal", "good", "supernatural");
        saturationCombo.getSelectionModel().select("normal");

        isFoodCheck.selectedProperty().addListener((obs, oldVal, newVal) -> {
            foodGrid.setDisable(!newVal);
        });

        // Bind preview labels
        previewNameLabel.textProperty().bind(nameField.textProperty());
        previewIdLabel.textProperty().bind(identifierField.textProperty());

        // Initialize stack size preview
        previewStackLabel.setText(String.valueOf(stackSizeSpinner.getValue()));
        stackSizeSpinner.valueProperty().addListener((obs, oldVal, newVal) -> {
            previewStackLabel.setText(String.valueOf(newVal));
        });

        // Auto-fill identifier based on name
        nameField.textProperty().addListener((obs, oldVal, newVal) -> {
            if (identifierField.getText().isEmpty()
                    || (oldVal != null && identifierField.getText().endsWith(formatToId(oldVal)))) {
                // If identifier is empty or looks like it was auto-generated from previous name
                // Try to preserve namespace if it exists
                String currentNamespace = projectNamespace;
                if (identifierField.getText().contains(":")) {
                    currentNamespace = identifierField.getText().split(":")[0];
                }
                identifierField.setText(currentNamespace + ":" + formatToId(newVal));
            }
        });
    }

    public void setProject(Project project) {
        this.project = project;
        if (project != null && project.getName() != null) {
            this.projectNamespace = formatToId(project.getName());
            // Update identifier immediately if name is empty
            if (nameField.getText().isEmpty()) {
                identifierField.setText(this.projectNamespace + ":");
            }
        }
    }

    public void loadItem(File file) {
        try (FileReader reader = new FileReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            JsonObject item = root.getAsJsonObject("minecraft:item");
            if (item == null)
                return;

            JsonObject description = item.getAsJsonObject("description");
            JsonObject components = item.getAsJsonObject("components");

            if (description != null && description.has("identifier")) {
                identifierField.setText(description.get("identifier").getAsString());
            }

            if (components != null) {
                if (components.has("minecraft:display_name")) {
                    JsonObject dn = components.getAsJsonObject("minecraft:display_name");
                    if (dn.has("value"))
                        nameField.setText(dn.get("value").getAsString());
                }

                if (components.has("minecraft:max_stack_size")) {
                    stackSizeSpinner.getValueFactory().setValue(components.get("minecraft:max_stack_size").getAsInt());
                }

                if (components.has("minecraft:foil")) {
                    glintCheck.setSelected(components.get("minecraft:foil").getAsBoolean());
                }

                if (components.has("minecraft:hand_equipped")) {
                    handEquippedCheck.setSelected(components.get("minecraft:hand_equipped").getAsBoolean());
                }

                if (components.has("minecraft:damage")) {
                    damageSpinner.getValueFactory().setValue(components.get("minecraft:damage").getAsInt());
                }

                if (components.has("minecraft:food")) {
                    isFoodCheck.setSelected(true);
                    JsonObject food = components.getAsJsonObject("minecraft:food");
                    if (food.has("nutrition"))
                        nutritionSpinner.getValueFactory().setValue(food.get("nutrition").getAsInt());
                    if (food.has("saturation_modifier"))
                        saturationCombo.setValue(food.get("saturation_modifier").getAsString());
                    if (food.has("can_always_eat"))
                        canAlwaysEatCheck.setSelected(food.get("can_always_eat").getAsBoolean());
                }

                if (components.has("minecraft:icon")) {
                    String texture = null;
                    if (components.get("minecraft:icon").isJsonPrimitive()) {
                        texture = components.get("minecraft:icon").getAsString();
                    } else {
                        JsonObject iconObj = components.getAsJsonObject("minecraft:icon");
                        if (iconObj.has("texture")) {
                            texture = iconObj.get("texture").getAsString();
                        }
                    }
                    if (texture != null)
                        resolveTexture(texture);
                }
            }

            if (createButton != null) {
                createButton.setText("Save Changes");
            }
        } catch (Exception e) {
            logger.error("Failed to load item", e);
        }
    }

    private void resolveTexture(String textureName) {
        if (project == null)
            return;
        // Search in RP/textures/items with multiple extensions
        String[] extensions = { ".png", ".tga", ".jpg", ".jpeg" };
        Path texturePath = null;

        for (String ext : extensions) {
            Path p = Paths.get(project.getRootPath(), "RP/textures/items", textureName + ext);
            if (Files.exists(p)) {
                texturePath = p;
                break;
            }
        }

        if (texturePath != null) {
            selectedTextureFile = texturePath.toFile();
            selectedTextureName = textureName;
            try {
                // Use robust loader
                Image img = loadTextureFile(selectedTextureFile);
                if (img != null) {
                    texturePreview.setImage(img);
                    texturePreview.setSmooth(false); // Pixel art optimization
                    if (largeTexturePreview != null) {
                        largeTexturePreview.setImage(img);
                        largeTexturePreview.setSmooth(false); // Pixel art optimization
                    }
                    textureNameLabel.setText(texturePath.getFileName().toString());
                }
            } catch (Exception e) {
                logger.error("Failed to load texture image", e);
            }
        }
    }

    private String formatToId(String text) {
        return text.toLowerCase().replaceAll("[^a-z0-9_]", "_");
    }

    @FXML
    private void handleSelectTexture() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Select Texture");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.tga"));

        // Try to start in project textures folder
        if (project != null) {
            Path texturesPath = Paths.get(project.getRootPath(), "RP/textures");
            if (Files.exists(texturesPath)) {
                fileChooser.setInitialDirectory(texturesPath.toFile());
            }
        }

        File file = fileChooser.showOpenDialog(identifierField.getScene().getWindow());
        if (file != null) {
            selectedTextureFile = file;
            selectedTextureName = file.getName().replaceFirst("[.][^.]+$", "");

            try {
                // Use robust loader
                Image image = loadTextureFile(file);
                if (image != null) {
                    texturePreview.setImage(image);
                    texturePreview.setSmooth(false);
                    if (largeTexturePreview != null) {
                        largeTexturePreview.setImage(image);
                        largeTexturePreview.setSmooth(false);
                    }
                    textureNameLabel.setText(file.getName());
                }
            } catch (Exception e) {
                logger.error("Failed to load image preview", e);
            }
        }
    }

    @FXML
    private void handleCancel() {
        close();
    }

    @FXML
    private void handleCreate() {
        if (identifierField.getText().trim().isEmpty()) {
            showAlert("Error", "Identifier is required.");
            return;
        }
        if (!identifierField.getText().contains(":")) {
            showAlert("Error", "Identifier must contain a namespace (e.g. namespace:item_name).");
            return;
        }
        if (nameField.getText().trim().isEmpty()) {
            showAlert("Error", "Display Name is required.");
            return;
        }
        if (selectedTextureFile == null) {
            showAlert("Error", "Please select a texture.");
            return;
        }

        try {
            createItem();
            close();
        } catch (Exception e) {
            logger.error("Failed to create item", e);
            showAlert("Error", "Failed to create item: " + e.getMessage());
        }
    }

    private void createItem() throws IOException {
        Path root = Paths.get(project.getRootPath());

        // 1. Handle Texture
        // Copy texture to RP/textures/items if it's not already there
        Path rpTexturesItems = root.resolve("RP/textures/items");
        if (!Files.exists(rpTexturesItems)) {
            Files.createDirectories(rpTexturesItems);
        }

        String textureFileName = selectedTextureFile.getName();
        Path targetTexturePath = rpTexturesItems.resolve(textureFileName);

        // Only copy if source is different from target
        if (!selectedTextureFile.toPath().toAbsolutePath().equals(targetTexturePath.toAbsolutePath())) {
            Files.copy(selectedTextureFile.toPath(), targetTexturePath,
                    java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        }

        // 2. Update item_texture.json
        updateItemTextureJson(root, selectedTextureName, "textures/items/" + selectedTextureName);

        // 3. Create Item JSON
        createItemJson(root);

        // 4. Update Project Model
        if (!project.getItems().contains(identifierField.getText())) {
            project.getItems().add(identifierField.getText());
            // Need to save project... normally handled by manager but we might need to
            // trigger it
            // For now, EditorController handles project updates when refreshing/adding.
            // We should ideally callback or return the new item name.
        }
    }

    private void updateItemTextureJson(Path root, String textureKey, String texturePath) throws IOException {
        Path jsonPath = root.resolve("RP/textures/item_texture.json");
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        JsonObject rootObj;

        if (Files.exists(jsonPath)) {
            try (FileReader reader = new FileReader(jsonPath.toFile())) {
                rootObj = JsonParser.parseReader(reader).getAsJsonObject();
            } catch (Exception e) {
                // If corrupt or empty, start fresh
                rootObj = new JsonObject();
            }
        } else {
            rootObj = new JsonObject();
            rootObj.addProperty("resource_pack_name", "vanilla");
            rootObj.addProperty("texture_name", "atlas.items");
            rootObj.add("texture_data", new JsonObject());
        }

        if (!rootObj.has("texture_data")) {
            rootObj.add("texture_data", new JsonObject());
        }

        JsonObject textureData = rootObj.getAsJsonObject("texture_data");
        JsonObject entry = new JsonObject();
        entry.addProperty("textures", texturePath);

        textureData.add(textureKey, entry);

        try (FileWriter writer = new FileWriter(jsonPath.toFile())) {
            gson.toJson(rootObj, writer);
        }
    }

    private void createItemJson(Path root) throws IOException {
        String identifier = identifierField.getText();
        String name = nameField.getText();

        JsonObject rootObj = new JsonObject();
        rootObj.addProperty("format_version", "1.20.80");

        JsonObject itemObj = new JsonObject();
        JsonObject descObj = new JsonObject();
        descObj.addProperty("identifier", identifier);

        JsonObject menuCategory = new JsonObject();
        menuCategory.addProperty("category", "items");
        descObj.add("menu_category", menuCategory);

        itemObj.add("description", descObj);

        JsonObject components = new JsonObject();
        components.addProperty("minecraft:icon", selectedTextureName);
        components.add("minecraft:display_name", createDisplayName(name));
        components.addProperty("minecraft:max_stack_size", stackSizeSpinner.getValue());

        if (glintCheck.isSelected()) {
            components.addProperty("minecraft:foil", true);
        }

        if (handEquippedCheck.isSelected()) {
            components.addProperty("minecraft:hand_equipped", true);
        }

        int damage = damageSpinner.getValue();
        if (damage > 0) {
            components.addProperty("minecraft:damage", damage);
        }

        if (isFoodCheck.isSelected()) {
            JsonObject food = new JsonObject();
            food.addProperty("nutrition", nutritionSpinner.getValue());
            food.addProperty("saturation_modifier", saturationCombo.getValue());
            if (canAlwaysEatCheck.isSelected()) {
                food.addProperty("can_always_eat", true);
            }
            components.add("minecraft:food", food);

            // Usually food has use_duration
            components.addProperty("minecraft:use_duration", 32);
        }

        itemObj.add("components", components);
        rootObj.add("minecraft:item", itemObj);

        Path itemsDir = root.resolve("BP/items");
        if (!Files.exists(itemsDir)) {
            Files.createDirectories(itemsDir);
        }

        String filename = identifier.split(":")[1] + ".json";
        try (FileWriter writer = new FileWriter(itemsDir.resolve(filename).toFile())) {
            new GsonBuilder().setPrettyPrinting().create().toJson(rootObj, writer);
        }
    }

    private JsonObject createDisplayName(String value) {
        JsonObject obj = new JsonObject();
        obj.addProperty("value", value);
        return obj;
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private Image loadTextureFile(File file) {
        if (file == null)
            return null;

        try {
            BufferedImage bImg;
            if (file.getName().toLowerCase().endsWith(".tga")) {
                // Load TGA
                javafx.scene.image.WritableImage tgaImg = TgaImageLoader.loadTga(file);
                bImg = SwingFXUtils.fromFXImage(tgaImg, null);
            } else {
                bImg = ImageIO.read(file);
            }

            if (bImg != null) {
                // Check if small (e.g. <= 64px) and upscale to ensure sharpness
                int w = bImg.getWidth();
                int h = bImg.getHeight();

                if (w <= 128 || h <= 128) {
                    int scale = 16; // Upscale significantly
                    int newW = w * scale;
                    int newH = h * scale;

                    BufferedImage scaledImg = new BufferedImage(newW, newH,
                            BufferedImage.TYPE_INT_ARGB);
                    java.awt.Graphics2D g2 = scaledImg.createGraphics();
                    g2.setRenderingHint(java.awt.RenderingHints.KEY_INTERPOLATION,
                            java.awt.RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
                    g2.drawImage(bImg, 0, 0, newW, newH, null);
                    g2.dispose();
                    bImg = scaledImg;
                }

                ByteArrayOutputStream out = new ByteArrayOutputStream();
                ImageIO.write(bImg, "png", out);
                return new Image(new ByteArrayInputStream(out.toByteArray()), 0, 0, true, false);
            }

        } catch (Exception e) {
            logger.error("Failed to load texture: " + file.getName(), e);
        }

        // Fallback
        return new Image(file.toURI().toString(), 0, 0, true, false);
    }

    private void close() {
        Stage stage = (Stage) identifierField.getScene().getWindow();
        stage.close();
    }
}
