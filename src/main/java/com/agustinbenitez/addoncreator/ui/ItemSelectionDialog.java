package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.models.Project;
import com.agustinbenitez.addoncreator.utils.MinecraftItems;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Window;
import javafx.util.Callback;

import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ItemSelectionDialog extends Dialog<String> {

    private final Project project;
    private final TextField searchField;
    private final ListView<String> listView;
    private final ObservableList<String> allItems;
    private final FilteredList<String> filteredItems;

    public ItemSelectionDialog(Project project, String initialValue) {
        this.project = project;

        setTitle("Select Item");
        setHeaderText("Select an item from the list or enter a custom identifier.");
        
        // Set the button types
        ButtonType selectButtonType = new ButtonType("Select", ButtonBar.ButtonData.OK_DONE);
        getDialogPane().getButtonTypes().addAll(selectButtonType, ButtonType.CANCEL);

        // UI Components
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        searchField = new TextField();
        searchField.setPromptText("Search...");
        searchField.setText(initialValue);

        listView = new ListView<>();
        listView.setPrefHeight(300);
        listView.setPrefWidth(400);

        // Load Items
        allItems = FXCollections.observableArrayList();
        loadItems();
        
        filteredItems = new FilteredList<>(allItems, p -> true);
        listView.setItems(filteredItems);

        // Search Logic
        searchField.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredItems.setPredicate(item -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return item.toLowerCase().contains(lowerCaseFilter);
            });
        });

        // Selection Logic
        listView.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                searchField.setText(newVal);
            }
        });
        
        // Double click to select
        listView.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && listView.getSelectionModel().getSelectedItem() != null) {
                searchField.setText(listView.getSelectionModel().getSelectedItem());
                // Trigger OK button
                 Node okButton = getDialogPane().lookupButton(selectButtonType);
                 if (okButton instanceof Button) {
                     ((Button) okButton).fire();
                 }
            }
        });

        grid.add(new Label("Search / Custom:"), 0, 0);
        grid.add(searchField, 1, 0);
        grid.add(listView, 0, 1, 2, 1);
        GridPane.setHgrow(searchField, Priority.ALWAYS);
        GridPane.setHgrow(listView, Priority.ALWAYS);
        GridPane.setVgrow(listView, Priority.ALWAYS);

        getDialogPane().setContent(grid);
        
        // Styles
        DialogPane dialogPane = getDialogPane();
        try {
            dialogPane.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
            dialogPane.getStyleClass().add("dialog-pane");
        } catch (Exception e) {
            // Ignore if css not found
        }

        Platform.runLater(searchField::requestFocus);

        setResultConverter(dialogButton -> {
            if (dialogButton == selectButtonType) {
                return searchField.getText();
            }
            return null;
        });
    }

    private void loadItems() {
        // 1. Minecraft Items
        allItems.addAll(MinecraftItems.getAllItems());

        // 2. Project Items (if any)
        if (project != null) {
            File root = new File(project.getRootPath());
            
            // BP/items
            scanDirectory(new File(root, "BP/items"), "minecraft:item");
            // BP/blocks
            scanDirectory(new File(root, "BP/blocks"), "minecraft:block");
        }
        
        Collections.sort(allItems);
    }

    private void scanDirectory(File dir, String jsonKey) {
        if (!dir.exists() || !dir.isDirectory()) return;

        File[] files = dir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                scanDirectory(file, jsonKey);
            } else if (file.getName().endsWith(".json")) {
                try {
                    JsonObject root = JsonParser.parseReader(new FileReader(file)).getAsJsonObject();
                    if (root.has("format_version")) { // Simple check
                         // Try to find identifier
                         // Structure varies by version, but usually:
                         // minecraft:item -> description -> identifier
                         if (root.has(jsonKey)) {
                             JsonObject obj = root.getAsJsonObject(jsonKey);
                             if (obj.has("description")) {
                                 JsonObject desc = obj.getAsJsonObject("description");
                                 if (desc.has("identifier")) {
                                     String id = desc.get("identifier").getAsString();
                                     if (!allItems.contains(id)) {
                                         allItems.add(id);
                                     }
                                 }
                             }
                         }
                    }
                } catch (Exception e) {
                    // Ignore malformed files
                }
            }
        }
    }
}
