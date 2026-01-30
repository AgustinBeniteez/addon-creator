package com.agustinbenitez.addoncreator.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundFill;
import javafx.scene.layout.CornerRadii;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
import javafx.scene.input.MouseEvent;
import javafx.scene.Cursor;

public class VisualBlock extends VBox {
    private String blockType; // "EVENT", "ACTION", "CONDITION"
    private String blockName;
    private boolean isPaletteItem;

    public VisualBlock(String name, String type, Color color, boolean isPaletteItem) {
        this.blockName = name;
        this.blockType = type;
        this.isPaletteItem = isPaletteItem;

        setPadding(new Insets(10));
        setSpacing(5);
        setBackground(new Background(new BackgroundFill(color, new CornerRadii(8), Insets.EMPTY)));
        setStyle("-fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.3), 5, 0, 0, 2);");
        
        Label title = new Label(name);
        title.setTextFill(Color.WHITE);
        title.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        getChildren().add(title);
        
        // Specific fields based on type (Placeholder for now)
        if (!isPaletteItem) {
            if (type.equals("ACTION")) {
                TextField param = new TextField();
                param.setPromptText("Parameter...");
                param.setStyle("-fx-background-color: rgba(255,255,255,0.2); -fx-text-fill: white;");
                getChildren().add(param);
            }
        }

        // Drag interactions
        setCursor(Cursor.HAND);
        setOnDragDetected(this::onDragDetected);
    }

    private void onDragDetected(MouseEvent event) {
        Dragboard db = startDragAndDrop(TransferMode.ANY);
        ClipboardContent content = new ClipboardContent();
        // Format: TYPE:NAME
        content.putString(blockType + ":" + blockName);
        
        // Snapshot for drag view
        db.setDragView(this.snapshot(null, null));
        
        db.setContent(content);
        event.consume();
    }
    
    public String getBlockType() { return blockType; }
    public String getBlockName() { return blockName; }
}
