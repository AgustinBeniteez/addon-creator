package com.agustinbenitez.addoncreator.ui;

import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.scene.paint.Color;

import java.util.function.Consumer;

public class TemplateSelectionDialog extends Stage {

    private final Consumer<String> onSelect;

    public TemplateSelectionDialog(Consumer<String> onSelect) {
        this.onSelect = onSelect;
        
        initModality(Modality.APPLICATION_MODAL);
        setTitle("Select Template");
        
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dark-dialog");
        root.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 20;");
        
        // Header
        Label header = new Label("Choose a Template");
        header.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold; -fx-padding: 0 0 20 0;");
        root.setTop(header);
        BorderPane.setAlignment(header, Pos.CENTER);
        
        // Grid
        FlowPane grid = new FlowPane();
        grid.setHgap(15);
        grid.setVgap(15);
        grid.setPrefWrapLength(500);
        grid.setAlignment(Pos.CENTER);
        grid.setPadding(new Insets(10));
        
        // Add Options with correct SVG paths
        addOption(grid, "Block", "M21 16.5c0 .38-.21.71-.53.88l-7.9 4.44c-.16.12-.36.18-.57.18-.21 0-.41-.06-.57-.18l-7.9-4.44A.991.991 0 0 1 3 16.5v-9c0-.38.21-.71.53-.88l7.9-4.44c.16-.12.36-.18.57-.18.21 0 .41.06.57.18l7.9 4.44c.32.17.53.5.53.88v9zM12 4.15L6.04 7.5 12 10.85l5.96-3.35L12 4.15zM5 15.91l6 3.38v-6.71L5 9.21v6.7zM13 19.29l6-3.38v-6.7l-6 3.38v6.7z", "block");
        addOption(grid, "Item", "M12 17.27L18.18 21l-1.64-7.03L22 9.24l-7.19-.61L12 2 9.19 8.63 2 9.24l5.46 4.73L5.82 21z", "item");
        addOption(grid, "Entity", "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z", "entity");
        addOption(grid, "Recipe", "M4 11h5V5H4v6zm0 7h5v-6H4v6zm6 0h5v-6h-5v6zm6 0h5v-6h-5v6zm-6-7h5V5h-5v6zm6-6v6h5V5h-5z", "recipe");
        addOption(grid, "Script", "M20,4H4C2.89,4 2,4.89 2,6V18A2,2 0 0,0 4,20H20A2,2 0 0,0 22,18V6C22,4.89 21.1,4 20,4M20,18H4V6H20V18M7.5,15L9,16.5L12.5,13L9,9.5L7.5,11L9.5,13L7.5,15M14,15H19V17H14V15Z", "script");
        addOption(grid, "World Gen", "M14 6l-3.75 5 2.85 3.8-1.6 1.2C9.81 13.75 7 10 7 10l-6 8h22L14 6z", "worldgen");
        addOption(grid, "Armor", "M12 1L3 5v6c0 5.55 3.84 10.74 9 12 5.16-1.26 9-6.45 9-12V5l-9-4z", "armor");
        addOption(grid, "Biome", "M17 6c-3.31 0-6 2.69-6 6 0 3.31 2.69 6 6 6s6-2.69 6-6-2.69-6-6-6zM5 8c-2.21 0-4 1.79-4 4s1.79 4 4 4 4-1.79 4-4-1.79-4-4-4zm0 6c-1.1 0-2-.9-2-2s.9-2 2-2 2 .9 2 2-.9 2-2 2z", "biome");
        addOption(grid, "Function", "M14 2H6c-1.1 0-1.99.9-1.99 2L4 20c0 1.1.89 2 1.99 2H18c1.1 0 2-.9 2-2V8l-6-6zm2 16H8v-2h8v2zm0-4H8v-2h8v2zm-3-5V3.5L18.5 9H13z", "function");
        addOption(grid, "Command", "M20 2H4c-1.1 0-2 .9-2 2v18l4-4h14c1.1 0 2-.9 2-2V4c0-1.1-.9-2-2-2zM6 9h12v2H6V9zm8 5H6v-2h8v2zm4-6H6V6h12v2z", "command");
        addOption(grid, "Gamerule", "M19.43 12.98c.04-.32.07-.64.07-.98s-.03-.66-.07-.98l2.11-1.65c.19-.15.24-.42.12-.64l-2-3.46c-.12-.22-.39-.3-.61-.22l-2.49 1c-.52-.4-1.08-.73-1.69-.98l-.38-2.65C14.46 2.18 14.25 2 14 2h-4c-.25 0-.46.18-.49.42l-.38 2.65c-.61.25-1.17.59-1.69.98l-2.49-1c-.23-.09-.49 0-.61.22l-2 3.46c-.13.22-.07.49.12.64l2.11 1.65c-.04.32-.07.65-.07.98s.03.66.07.98l-2.11 1.65c-.19.15-.24.42-.12.64l2 3.46c.12.22.39.3.61.22l2.49-1c.52.4 1.08.73 1.69.98l.38 2.65c.03.24.24.42.49.42h4c.25 0 .46-.18.49-.42l.38-2.65c.61-.25 1.17-.59 1.69-.98l2.49 1c.23.09.49 0 .61-.22l2-3.46c.12-.22.07-.49-.12-.64l-2.11-1.65zM12 15.5c-1.93 0-3.5-1.57-3.5-3.5s1.57-3.5 3.5-3.5 3.5 1.57 3.5 3.5-1.57 3.5-3.5 3.5z", "gamerule");
        addOption(grid, "Tool", "M13.7 13.7c-.39.39-1.02.39-1.41 0L6.3 7.7c-.39-.39-.39-1.02 0-1.41l3.54-3.54c.39-.39 1.02-.39 1.41 0l6 6c.39.39.39 1.02 0 1.41l-3.55 3.54zM4.41 19.59c-.39.39-1.02.39-1.41 0L.59 17.17c-.39-.39-.39-1.02 0-1.41L7 9.34 10.66 13l-6.25 6.59zM19 14.5l-2.5 2.5 1 1 2.5-2.5-1-1zm-1.75 3.25l-2.5 2.5 1 1 2.5-2.5-1-1z", "tool");
        addOption(grid, "Plant", "M32 60 V28 M32 40 C22 38 18 34 16 30 C22 30 28 32 32 36 M32 44 C42 42 46 38 48 34 C42 34 36 36 32 40 M32 12 A8 8 0 1 1 32 28 A8 8 0 1 1 32 12 M32 12 C28 14 26 18 28 22 M32 12 C36 14 38 18 36 22 M26 20 C28 24 36 24 38 20", "plant");
        addOption(grid, "Painting", "M21 19V5c0-1.1-.9-2-2-2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2zM8.5 13.5l2.5 3.01L14.5 12l4.5 6H5l3.5-4.5z", "painting");
        
        ScrollPane scroll = new ScrollPane(grid);
        scroll.setFitToWidth(true);
        scroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");
        root.setCenter(scroll);
        
        // Cancel Button
        Button btnCancel = new Button("Cancel");
        btnCancel.getStyleClass().add("button-secondary");
        btnCancel.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-border-color: #555; -fx-border-radius: 3;");
        btnCancel.setOnAction(e -> close());
        
        HBox bottom = new HBox(btnCancel);
        bottom.setAlignment(Pos.CENTER_RIGHT);
        bottom.setPadding(new Insets(15, 0, 0, 0));
        root.setBottom(bottom);
        
        Scene scene = new Scene(root, 650, 500);
        
        // Load CSS
        try {
            scene.getStylesheets().add(getClass().getResource("/css/styles.css").toExternalForm());
        } catch (Exception e) {
            System.err.println("Could not load CSS: " + e.getMessage());
        }
        
        setScene(scene);
    }
    
    private void addOption(FlowPane grid, String title, String iconContent, String id) {
        Button btn = new Button();
        btn.setPrefSize(120, 120);
        
        VBox content = new VBox(10);
        content.setAlignment(Pos.CENTER);
        
        SVGPath icon = new SVGPath();
        icon.setContent(iconContent);
        
        if ("plant".equals(id)) {
            icon.setFill(null);
            icon.setStroke(Color.WHITE);
            icon.setStrokeWidth(2);
            icon.setStrokeLineCap(StrokeLineCap.ROUND);
            icon.setStrokeLineJoin(StrokeLineJoin.ROUND);
            icon.setScaleX(1.0);
            icon.setScaleY(1.0);
        } else {
            icon.setFill(Color.WHITE);
            icon.setScaleX(2.0);
            icon.setScaleY(2.0);
        }
        
        Label lbl = new Label(title);
        lbl.setStyle("-fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 14px;");
        
        content.getChildren().addAll(icon, lbl);
        btn.setGraphic(content);
        
        btn.setOnAction(e -> {
            close();
            onSelect.accept(id);
        });
        
        // Style
        String defaultStyle = "-fx-background-color: #333333; -fx-background-radius: 8; -fx-cursor: hand;";
        String hoverStyle = "-fx-background-color: #0e639c; -fx-background-radius: 8; -fx-cursor: hand; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 8, 0, 0, 0);";
        
        btn.setStyle(defaultStyle);
        btn.setOnMouseEntered(e -> btn.setStyle(hoverStyle));
        btn.setOnMouseExited(e -> btn.setStyle(defaultStyle));
        
        grid.getChildren().add(btn);
    }
}
