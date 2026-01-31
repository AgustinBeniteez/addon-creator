package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RecipeCreatorController {

    @FXML private TextField identifierField;
    @FXML private TextField descriptionField;
    @FXML private TextField groupField;
    @FXML private ComboBox<String> typeCombo;
    @FXML private GridPane craftingGrid;
    @FXML private Button resultSlot;
    @FXML private Spinner<Integer> resultCount;

    private Project project;
    private final Button[][] gridButtons = new Button[3][3];
    private final String[][] gridItems = new String[3][3]; // Stores item identifiers
    private String resultItem = "minecraft:air";

    @FXML
    public void initialize() {
        typeCombo.getItems().addAll("Crafting Table (Shaped)", "Crafting Table (Shapeless)", "Furnace", "Blast Furnace", "Smoker", "Campfire", "Stonecutter");
        typeCombo.getSelectionModel().select(0);

        resultCount.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 64, 1));

        initializeGrid();
        initializeResultSlot();
    }

    public void setProject(Project project) {
        this.project = project;
    }

    public void loadRecipe(File file) {
        try {
            JsonObject root = com.google.gson.JsonParser.parseReader(new java.io.FileReader(file)).getAsJsonObject();
            
            // Check format version
            if (!root.has("format_version") || !root.get("format_version").getAsString().equals("1.20.0")) {
                // Warning or try to parse anyway
            }
            
            // Assuming minecraft:recipe_shaped
            if (root.has("minecraft:recipe_shaped")) {
                JsonObject recipe = root.getAsJsonObject("minecraft:recipe_shaped");
                
                // Description
                if (recipe.has("description")) {
                    JsonObject desc = recipe.getAsJsonObject("description");
                    if (desc.has("identifier")) {
                        identifierField.setText(desc.get("identifier").getAsString());
                    }
                }
                
                // Tags (to set type)
                if (recipe.has("tags")) {
                    JsonArray tags = recipe.getAsJsonArray("tags");
                    for (com.google.gson.JsonElement tag : tags) {
                        String t = tag.getAsString();
                        if (t.equals("crafting_table")) typeCombo.getSelectionModel().select(0);
                        // Add other types mapping if needed
                    }
                }
                
                // Result
                if (recipe.has("result")) {
                    JsonObject result = recipe.getAsJsonObject("result");
                    if (result.has("item")) {
                        resultItem = result.get("item").getAsString();
                        resultSlot.setText(resultItem.isEmpty() ? "?" : resultItem.substring(0, Math.min(4, resultItem.length())));
                        resultSlot.setTooltip(new Tooltip(resultItem));
                    }
                    if (result.has("count")) {
                        resultCount.getValueFactory().setValue(result.get("count").getAsInt());
                    }
                }
                
                // Pattern and Key (Reverse engineer grid)
                if (recipe.has("pattern") && recipe.has("key")) {
                    JsonArray pattern = recipe.getAsJsonArray("pattern");
                    JsonObject key = recipe.getAsJsonObject("key");
                    
                    Map<Character, String> charToItem = new HashMap<>();
                    for (String k : key.keySet()) {
                        JsonObject itemObj = key.getAsJsonObject(k);
                        if (itemObj.has("item")) {
                            charToItem.put(k.charAt(0), itemObj.get("item").getAsString());
                        }
                    }
                    
                    // Clear grid
                    for(int r=0; r<3; r++) for(int c=0; c<3; c++) {
                        gridItems[r][c] = null;
                        gridButtons[r][c].setText("");
                        gridButtons[r][c].setTooltip(null);
                    }
                    
                    for (int r = 0; r < pattern.size() && r < 3; r++) {
                        String rowStr = pattern.get(r).getAsString();
                        for (int c = 0; c < rowStr.length() && c < 3; c++) {
                            char ch = rowStr.charAt(c);
                            if (ch != ' ' && charToItem.containsKey(ch)) {
                                String item = charToItem.get(ch);
                                gridItems[r][c] = item;
                                gridButtons[r][c].setText(item.substring(0, Math.min(3, item.length())));
                                gridButtons[r][c].setTooltip(new Tooltip(item));
                            }
                        }
                    }
                }
            }
            
        } catch (Exception e) {
            showAlert("Error", "Failed to load recipe: " + e.getMessage());
        }
    }

    private void initializeGrid() {
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                Button btn = new Button();
                btn.setPrefSize(50, 50);
                btn.setStyle("-fx-base: #3c3f41;");
                
                int r = row;
                int c = col;
                btn.setOnAction(e -> handleSlotClick(r, c));
                
                gridButtons[row][col] = btn;
                craftingGrid.add(btn, col, row);
            }
        }
    }

    private void initializeResultSlot() {
        resultSlot.setOnAction(e -> handleResultClick());
        resultSlot.setText("?");
    }

    private void handleSlotClick(int row, int col) {
        String current = gridItems[row][col];
        String newItem = showItemSelectionDialog(current != null ? current : "");
        
        if (newItem != null) {
            gridItems[row][col] = newItem;
            gridButtons[row][col].setText(newItem.isEmpty() ? "" : newItem.substring(0, Math.min(3, newItem.length())));
            gridButtons[row][col].setTooltip(new Tooltip(newItem));
        }
    }

    private void handleResultClick() {
        String newItem = showItemSelectionDialog(resultItem);
        if (newItem != null) {
            resultItem = newItem;
            resultSlot.setText(newItem.isEmpty() ? "?" : newItem.substring(0, Math.min(4, newItem.length())));
            resultSlot.setTooltip(new Tooltip(newItem));
        }
    }

    private String showItemSelectionDialog(String initialValue) {
        TextInputDialog dialog = new TextInputDialog(initialValue);
        dialog.setTitle("Select Item");
        dialog.setHeaderText("Enter Item Identifier (e.g. minecraft:apple)");
        dialog.setContentText("Identifier:");
        
        // Use current stage style
        DialogPane dialogPane = dialog.getDialogPane();
        dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        dialogPane.getStyleClass().add("dialog-pane");

        Optional<String> result = dialog.showAndWait();
        return result.orElse(null);
    }

    @FXML
    private void handlePreview() {
        String json = generateJson();
        if (json == null) return;

        TextArea textArea = new TextArea(json);
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("JSON Preview");
        alert.setHeaderText("Generated Recipe JSON");
        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.getDialogPane().setPrefSize(500, 400);
        alert.showAndWait();
    }

    @FXML
    private void handleCreate() {
        if (identifierField.getText().isEmpty()) {
            showAlert("Error", "Identifier is required.");
            return;
        }

        String json = generateJson();
        if (json == null) return;

        if (project == null) {
            showAlert("Error", "No project loaded.");
            return;
        }

        try {
            File recipesDir = new File(project.getRootPath(), "BP/recipes");
            if (!recipesDir.exists()) recipesDir.mkdirs();

            String filename = identifierField.getText().replace(":", "_") + ".json";
            File file = new File(recipesDir, filename);

            try (FileWriter writer = new FileWriter(file)) {
                writer.write(json);
            }

            showAlert("Success", "Recipe created: " + file.getName());
            
            // Close window if opened as separate stage
            Stage stage = (Stage) identifierField.getScene().getWindow();
            stage.close();

        } catch (IOException e) {
            showAlert("Error", "Failed to save file: " + e.getMessage());
        }
    }

    private String generateJson() {
        String identifier = identifierField.getText().trim();
        String description = descriptionField.getText().trim();
        
        if (identifier.isEmpty()) {
            showAlert("Validation Error", "Identifier cannot be empty.");
            return null;
        }

        JsonObject root = new JsonObject();
        root.addProperty("format_version", "1.20.0");

        JsonObject recipe = new JsonObject();
        JsonObject desc = new JsonObject();
        desc.addProperty("identifier", identifier);
        recipe.add("description", desc);

        JsonArray tags = new JsonArray();
        tags.add("crafting_table");
        recipe.add("tags", tags);

        // Pattern and Key generation
        JsonArray pattern = new JsonArray();
        JsonObject key = new JsonObject();
        Map<String, Character> itemToChar = new HashMap<>();
        char currentChar = 'A';

        // 3x3 Grid processing
        // Simplify pattern: Remove empty rows/cols if possible? 
        // For now, let's output full 3x3 or trim empty lines.
        // User requested standard shaped recipe.

        String[] rows = new String[3];
        for (int r = 0; r < 3; r++) {
            StringBuilder sb = new StringBuilder();
            for (int c = 0; c < 3; c++) {
                String item = gridItems[r][c];
                if (item == null || item.isEmpty() || item.equals("minecraft:air")) {
                    sb.append(" ");
                } else {
                    if (!itemToChar.containsKey(item)) {
                        itemToChar.put(item, currentChar++);
                    }
                    sb.append(itemToChar.get(item));
                }
            }
            rows[r] = sb.toString();
        }

        // Add pattern rows (skipping empty surrounding rows is better practice but full 3x3 is valid)
        for (String row : rows) {
            pattern.add(row);
        }
        recipe.add("pattern", pattern);

        // Add Keys
        for (Map.Entry<String, Character> entry : itemToChar.entrySet()) {
            JsonObject itemObj = new JsonObject();
            itemObj.addProperty("item", entry.getKey());
            key.add(String.valueOf(entry.getValue()), itemObj);
        }
        recipe.add("key", key);

        // Result
        JsonObject resultObj = new JsonObject();
        resultObj.addProperty("item", resultItem);
        resultObj.addProperty("count", resultCount.getValue());
        recipe.add("result", resultObj);

        root.add("minecraft:recipe_shaped", recipe);

        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        return gson.toJson(root);
    }

    private void showAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
