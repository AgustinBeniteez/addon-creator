package com.agustinbenitez.addoncreator.ui;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldListCell;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.SVGPath;
import javafx.util.StringConverter;
import javafx.geometry.Point2D;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.Stack;
import javafx.stage.FileChooser;
import javax.imageio.ImageIO;
import javafx.embed.swing.SwingFXUtils;
import java.io.File;
import java.io.IOException;
import javafx.scene.SnapshotParameters;

public class PixelArtEditorController implements Initializable {

    @FXML private ColorPicker colorPicker;
    @FXML private ColorPicker secondaryColorPicker;
    @FXML private TextField widthField;
    @FXML private TextField heightField;
    @FXML private Button btnResize;
    @FXML private Slider zoomSlider;
    @FXML private Label zoomLabel;
    @FXML private ScrollPane scrollPane;
    @FXML private StackPane canvasContainer;
    @FXML private Canvas canvas;
    @FXML private Label statusLabel;
    
    @FXML private Button btnExport;
    @FXML private Button btnSettings;
    @FXML private ComboBox<String> toolSizeCombo;
    
    // Layer UI
    @FXML private ListView<Layer> layerList;
    @FXML private Button btnAddLayer;
    @FXML private Button btnRemoveLayer;
    @FXML private Button btnLayerUp;
    @FXML private Button btnLayerDown;

    private int artWidth = 32;
    private int artHeight = 32;
    private double zoom = 10.0;
    private int toolSize = 1;
    
    // Layer System
    private ObservableList<Layer> layers = FXCollections.observableArrayList();
    private Layer activeLayer;

    // Tool states
    private enum Tool { PENCIL, ERASER, LINE, RECTANGLE, CIRCLE, FILL, PICKER, SELECT_RECT, SELECT_LASSO, GRADIENT }
    private Tool currentTool = Tool.PENCIL;
    private boolean showGrid = false;
    
    // Selection state
    private boolean isSelecting = false;
    private boolean hasSelection = false;
    private int selectStartX, selectStartY, selectEndX, selectEndY;
    private boolean isDraggingSelection = false;
    private WritableImage floatingSelection;
    private int floatingX, floatingY;
    private WritableImage clipboardImage;
    private List<Point2D> lassoPoints = new ArrayList<>();

    // Panning state
    private double lastMouseX;
    private double lastMouseY;

    // Shape drawing state
    private int dragStartX, dragStartY;
    private int currentDragX, currentDragY;
    private boolean isDraggingShape = false;
    
    // Gradient state
    private boolean isDraggingGradient = false;

    @FXML private ToggleButton btnPencil;
    @FXML private ToggleButton btnEraser;
    @FXML private ToggleButton btnFill;
    @FXML private ToggleButton btnPicker;
    @FXML private ToggleButton btnLine;
    @FXML private ToggleButton btnRect;
    @FXML private ToggleButton btnCircle;
    @FXML private ToggleButton btnGradient;
    @FXML private ToggleButton btnSelectRect;
    @FXML private ToggleButton btnSelectLasso;
    @FXML private ToggleButton btnGrid;
    @FXML private Button btnFlipH;
    @FXML private Button btnFlipV;
    @FXML private Button btnSwapColors;
    
    @FXML private javafx.scene.input.KeyEvent keyEvent; // Just to make sure we can handle keys if needed
    
    // Mouse state
    private Color currentDrawColor;
    
    // Project context
    private File projectRoot;

    public void setProjectRoot(File projectRoot) {
        this.projectRoot = projectRoot;
    }

    public void setStandaloneMode(boolean isStandalone) {
        if (isStandalone) {
            if (btnSettings != null) {
                btnSettings.setVisible(true);
                btnSettings.setManaged(true);
                
                // Add Settings Icon
                SVGPath settingsIcon = new SVGPath();
                settingsIcon.setContent("M19.14 12.94c.04-.3.06-.61.06-.94 0-.32-.02-.64-.07-.94l2.03-1.58c.18-.14.23-.41.12-.61l-1.92-3.32c-.12-.22-.37-.29-.59-.22l-2.39.96c-.5-.38-1.03-.7-1.62-.94l-.36-2.54c-.04-.24-.24-.41-.48-.41h-3.84c-.24 0-.43.17-.47.41l-.36 2.54c-.59.24-1.13.57-1.62.94l-2.39-.96c-.22-.08-.47 0-.59.22L2.74 8.87c-.12.21-.08.47.12.61l2.03 1.58c-.05.3-.09.63-.09.94s.02.64.07.94l-2.03 1.58c-.18.14-.23.41-.12.61l1.92 3.32c.12.22.37.29.59.22l2.39-.96c.5.38 1.03.7 1.62.94l.36 2.54c.04.24.24.41.48.41h-3.84c.24 0 .43-.17.47-.41l.36-2.54c.59-.24 1.13-.57 1.62-.94l2.39.96c.22.08.47 0 .59-.22l1.92-3.32c.12-.22.07-.47-.12-.61l-2.01-1.58zM12 15.6c-1.98 0-3.6-1.62-3.6-3.6s1.62-3.6 3.6-3.6 3.6 1.62 3.6 3.6-1.62 3.6-3.6 3.6z");
                settingsIcon.setFill(Color.WHITE);
                settingsIcon.setScaleX(0.8);
                settingsIcon.setScaleY(0.8);
                btnSettings.setGraphic(settingsIcon);
                
                btnSettings.setOnAction(e -> NavigationManager.getInstance().showSettingsModal());
            }
        }
    }
    
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupControls(); // Setup UI and listeners first
        setupIcons();    // Then add default layer (depends on listeners for activeLayer)
        setupCanvas();
        setupEvents();
        setupKeyEvents();
        
        // Setup Tool Size Combo
        if (toolSizeCombo != null) {
            toolSizeCombo.getItems().addAll("1px", "2px", "4px", "6px", "8px");
            toolSizeCombo.setValue("1px");
            toolSizeCombo.setOnAction(e -> {
                String val = toolSizeCombo.getValue();
                if (val != null) {
                    toolSize = Integer.parseInt(val.replace("px", ""));
                }
            });
        }
        
        // Setup Export Button Icon
        if (btnExport != null) {
            SVGPath exportIcon = new SVGPath();
            exportIcon.setContent("M19 12v7H5v-7H3v7c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2v-7h-2zm-6 .67l2.59-2.58L17 11.5l-5 5-5-5 1.41-1.41L11 12.67V3h2v9.67z");
            exportIcon.setFill(Color.WHITE);
            exportIcon.setScaleX(0.8);
            exportIcon.setScaleY(0.8);
            
            btnExport.setGraphic(exportIcon);
            btnExport.setText("Exportar");
            btnExport.setContentDisplay(ContentDisplay.LEFT);
            btnExport.setGraphicTextGap(8);
        }
    }
    
    private void setupKeyEvents() {
        canvas.setFocusTraversable(true);
        canvas.setOnKeyPressed(e -> {
            if (e.isControlDown()) {
                switch (e.getCode()) {
                    case C:
                        copySelection();
                        break;
                    case V:
                        pasteSelection();
                        break;
                    case X:
                        cutSelection();
                        break;
                    case Z:
                        undo();
                        break;
                }
            } else if (e.getCode() == KeyCode.ESCAPE) {
                commitSelection(); // Or cancel selection
            }
        });
        
        // Ensure canvas gets focus when clicked
        canvas.setOnMouseClicked(e -> canvas.requestFocus());
    }

    // Undo System
    private Stack<List<Layer>> undoStack = new Stack<>();

    private void saveUndoState() {
        System.out.println("Saving undo state...");
        List<Layer> state = new ArrayList<>();
        for (Layer layer : layers) {
            Layer copy = new Layer(layer.getName(), (int)layer.getImage().getWidth(), (int)layer.getImage().getHeight());
            copy.setVisible(layer.isVisible());
            
            // Copy Image Data
            WritableImage srcImg = layer.getImage();
            PixelReader reader = srcImg.getPixelReader();
            PixelWriter writer = copy.getImage().getPixelWriter();
            
            int w = (int) srcImg.getWidth();
            int h = (int) srcImg.getHeight();
            
            for(int x=0; x<w; x++) {
                for(int y=0; y<h; y++) {
                    writer.setArgb(x, y, reader.getArgb(x, y));
                }
            }
            state.add(copy);
        }
        undoStack.push(state);
        // Limit stack size
        if (undoStack.size() > 50) undoStack.remove(0);
        System.out.println("Undo state saved. Stack size: " + undoStack.size());
    }

    private void undo() {
        if (undoStack.isEmpty()) return;
        
        // Save current active layer index to try to restore selection
        int activeIndex = layerList.getSelectionModel().getSelectedIndex();
        
        List<Layer> state = undoStack.pop();
        layers.setAll(state); 
        
        // Restore active layer
        if (!layers.isEmpty()) {
             if (activeIndex >= 0 && activeIndex < layers.size()) {
                 layerList.getSelectionModel().select(activeIndex);
             } else {
                 layerList.getSelectionModel().selectFirst();
             }
             activeLayer = layerList.getSelectionModel().getSelectedItem();
        } else {
            activeLayer = null;
        }
        
        drawCanvas();
    }

    private void copySelection() {
        System.out.println("Copying selection... HasSelection: " + hasSelection);
        if (!hasSelection || activeLayer == null) return;
        
        int x = Math.min(selectStartX, selectEndX);
        int y = Math.min(selectStartY, selectEndY);
        int w = Math.abs(selectEndX - selectStartX) + 1;
        int h = Math.abs(selectEndY - selectStartY) + 1;
        
        System.out.println("Selection bounds: " + x + "," + y + " " + w + "x" + h);
        
        clipboardImage = new WritableImage(w, h);
        PixelReader reader = activeLayer.getImage().getPixelReader();
        PixelWriter writer = clipboardImage.getPixelWriter();
        
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int srcX = x + i;
                int srcY = y + j;
                if (srcX >= 0 && srcX < artWidth && srcY >= 0 && srcY < artHeight) {
                    if (!lassoPoints.isEmpty() && !isInsideLasso(srcX, srcY)) {
                        continue;
                    }
                    writer.setArgb(i, j, reader.getArgb(srcX, srcY));
                }
            }
        }
        System.out.println("Selection copied to clipboard.");
    }

    private void cutSelection() {
        System.out.println("Cutting selection...");
        if (!hasSelection || activeLayer == null) return;
        
        saveUndoState();
        copySelection(); // Copy first
        
        int x = Math.min(selectStartX, selectEndX);
        int y = Math.min(selectStartY, selectEndY);
        int w = Math.abs(selectEndX - selectStartX) + 1;
        int h = Math.abs(selectEndY - selectStartY) + 1;
        
        PixelWriter writer = activeLayer.getImage().getPixelWriter();
        
        for (int i = 0; i < w; i++) {
            for (int j = 0; j < h; j++) {
                int srcX = x + i;
                int srcY = y + j;
                if (srcX >= 0 && srcX < artWidth && srcY >= 0 && srcY < artHeight) {
                    if (!lassoPoints.isEmpty() && !isInsideLasso(srcX, srcY)) {
                        continue;
                    }
                    writer.setColor(srcX, srcY, Color.TRANSPARENT);
                }
            }
        }
        drawCanvas();
        System.out.println("Selection cut.");
    }
    
    private boolean isInsideLasso(int x, int y) {
        // Check if point is on the lasso path (boundary)
        for (Point2D p : lassoPoints) {
            if ((int)p.getX() == x && (int)p.getY() == y) return true;
        }

        double px = x + 0.5;
        double py = y + 0.5;
        boolean inside = false;
        int n = lassoPoints.size();
        for (int i = 0, j = n - 1; i < n; j = i++) {
            double xi = lassoPoints.get(i).getX();
            double yi = lassoPoints.get(i).getY();
            double xj = lassoPoints.get(j).getX();
            double yj = lassoPoints.get(j).getY();
            
            boolean intersect = ((yi > py) != (yj > py)) &&
                (px < (xj - xi) * (py - yi) / (yj - yi) + xi);
            if (intersect) inside = !inside;
        }
        return inside;
    }
    
    private void pasteSelection() {
        System.out.println("Pasting selection... Clipboard: " + (clipboardImage != null));
        if (clipboardImage == null) return;
        
        saveUndoState();
        commitSelection(); // Commit previous selection if any
        
        floatingSelection = clipboardImage; // For now just reference, ideally clone
        floatingX = 0; // Paste at top-left or center of view
        floatingY = 0;
        
        // Set selection bounds to match pasted image
        selectStartX = floatingX;
        selectStartY = floatingY;
        selectEndX = floatingX + (int)floatingSelection.getWidth() - 1;
        selectEndY = floatingY + (int)floatingSelection.getHeight() - 1;
        
        hasSelection = true;
        isSelecting = false;
        lassoPoints.clear();
        drawCanvas();
        System.out.println("Selection pasted.");
    }
    
    private void commitSelection() {
        if (floatingSelection != null && activeLayer != null) {
            PixelReader reader = floatingSelection.getPixelReader();
            PixelWriter writer = activeLayer.getImage().getPixelWriter();
            int w = (int)floatingSelection.getWidth();
            int h = (int)floatingSelection.getHeight();
            
            for (int i = 0; i < w; i++) {
                for (int j = 0; j < h; j++) {
                    int destX = floatingX + i;
                    int destY = floatingY + j;
                    if (destX >= 0 && destX < artWidth && destY >= 0 && destY < artHeight) {
                        Color color = reader.getColor(i, j);
                        if (color.getOpacity() > 0) {
                            writer.setColor(destX, destY, color);
                        }
                    }
                }
            }
            floatingSelection = null;
        }
        hasSelection = false;
        drawCanvas();
    }

    private void updateCanvasSize() {
        canvas.setWidth(artWidth * zoom);
        canvas.setHeight(artHeight * zoom);
        drawCanvas();
    }

    private void setupControls() {
        colorPicker.setValue(Color.BLACK);
        secondaryColorPicker.setValue(Color.TRANSPARENT);
        
        // Tool Buttons
        ToggleGroup toolGroup = new ToggleGroup();
        configureToolButton(btnPencil, toolGroup, Tool.PENCIL);
        configureToolButton(btnEraser, toolGroup, Tool.ERASER);
        configureToolButton(btnFill, toolGroup, Tool.FILL);
        configureToolButton(btnPicker, toolGroup, Tool.PICKER);
        configureToolButton(btnLine, toolGroup, Tool.LINE);
        configureToolButton(btnRect, toolGroup, Tool.RECTANGLE);
        configureToolButton(btnCircle, toolGroup, Tool.CIRCLE);
        configureToolButton(btnGradient, toolGroup, Tool.GRADIENT);
        configureToolButton(btnSelectRect, toolGroup, Tool.SELECT_RECT);
        configureToolButton(btnSelectLasso, toolGroup, Tool.SELECT_LASSO);
        
        if (btnSwapColors != null) {
            btnSwapColors.setOnAction(e -> swapColors());
        }
        
        if (btnGrid != null) {
            btnGrid.setOnAction(e -> {
                showGrid = btnGrid.isSelected();
                drawCanvas();
            });
        }
        
        if (btnFlipH != null) btnFlipH.setOnAction(e -> flipImage(true));
        if (btnFlipV != null) btnFlipV.setOnAction(e -> flipImage(false));
        
        if (btnExport != null) {
            btnExport.setOnAction(e -> exportImage());
        }
        
        btnResize.setOnAction(e -> {
            try {
                int newW = Integer.parseInt(widthField.getText());
                int newH = Integer.parseInt(heightField.getText());
                if (newW > 0 && newH > 0) {
                    resizeArt(newW, newH);
                }
            } catch (NumberFormatException ex) {
                // Ignore invalid input
            }
        });

        zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            zoom = newVal.doubleValue();
            zoomLabel.setText(String.format("%.1fx", zoom));
            updateCanvasSize();
        });
        
        setupLayerUI();
    }
    
    private void swapColors() {
        Color temp = colorPicker.getValue();
        colorPicker.setValue(secondaryColorPicker.getValue());
        secondaryColorPicker.setValue(temp);
    }

    private void setupLayerUI() {
        layerList.setItems(layers);
        layerList.setCellFactory(lv -> new ListCell<Layer>() {
            private final ToggleButton visibleBtn = new ToggleButton();
            private final TextField textField = new TextField();
            private final HBox content = new HBox(5, visibleBtn, textField);
            private final SVGPath eyeIcon = new SVGPath();
            
            {
                content.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                textField.getStyleClass().add("text-field");
                textField.setStyle("-fx-background-color: transparent; -fx-text-fill: white;");
                HBox.setHgrow(textField, javafx.scene.layout.Priority.ALWAYS);
                
                // Configure Visibility Button
                visibleBtn.getStyleClass().add("pixel-art-tool-button");
                visibleBtn.setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                visibleBtn.setStyle("-fx-padding: 2;");
                eyeIcon.setFill(Color.WHITE);
                eyeIcon.setScaleX(0.7);
                eyeIcon.setScaleY(0.7);
                visibleBtn.setGraphic(eyeIcon);
                
                // Edit name on enter or loose focus
                textField.setOnAction(e -> {
                    if (getItem() != null) getItem().setName(textField.getText());
                });
                textField.focusedProperty().addListener((obs, old, newVal) -> {
                    if (!newVal && getItem() != null) getItem().setName(textField.getText());
                });
                
                visibleBtn.setOnAction(e -> {
                    if (getItem() != null) {
                        getItem().setVisible(visibleBtn.isSelected());
                        updateEyeIcon();
                        drawCanvas();
                    }
                });
            }
            
            private void updateEyeIcon() {
                if (visibleBtn.isSelected()) {
                    // Eye Open
                    eyeIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
                    eyeIcon.setFill(Color.WHITE);
                } else {
                    // Eye Closed / Off
                    eyeIcon.setContent("M12 7c2.76 0 5 2.24 5 5 0 .65-.13 1.26-.36 1.83l2.92 2.92c1.51-1.26 2.7-2.89 3.43-4.75-1.73-4.39-6-7.5-11-7.5-1.4 0-2.74.25-4 .7l2.16 2.16C10.74 7.13 11.35 7 12 7zM2 4.27l2.28 2.28.46.46C3.08 8.3 1.78 10.02 1 12c1.73 4.39 6 7.5 11 7.5 1.55 0 3.03-.3 4.38-.84l.42.42L19.73 22 21 20.73 3.27 3 2 4.27zM7.53 9.8l1.55 1.55c-.05.21-.08.43-.08.65 0 1.66 1.34 3 3 3 .22 0 .44-.03.65-.08l1.55 1.55c-.67.33-1.41.53-2.2.53-2.76 0-5-2.24-5-5 0-.79.2-1.53.53-2.2zm4.31-.78l3.15 3.15.02-.16c0-1.66-1.34-3-3-3l-.17.01z");
                    eyeIcon.setFill(Color.GRAY);
                }
            }
            
            @Override
            protected void updateItem(Layer layer, boolean empty) {
                super.updateItem(layer, empty);
                if (empty || layer == null) {
                    setGraphic(null);
                } else {
                    visibleBtn.setSelected(layer.isVisible());
                    updateEyeIcon();
                    textField.setText(layer.getName());
                    setGraphic(content);
                }
            }
        });
        
        layerList.getSelectionModel().selectedItemProperty().addListener((obs, old, newVal) -> {
            activeLayer = newVal;
        });
        
        // Select first layer by default
        if (!layers.isEmpty()) {
            layerList.getSelectionModel().selectFirst();
        }
        
        btnAddLayer.setOnAction(e -> addLayer("Layer " + (layers.size() + 1)));
        
        btnRemoveLayer.setOnAction(e -> {
            Layer selected = layerList.getSelectionModel().getSelectedItem();
            if (selected != null && layers.size() > 1) {
                saveUndoState();
                layers.remove(selected);
                drawCanvas();
            }
        });
        
        btnLayerUp.setOnAction(e -> moveLayer(-1));
        btnLayerDown.setOnAction(e -> moveLayer(1));

        // Initialize layers if empty
        if (layers.isEmpty()) {
            addLayer("Background");
        }
    }
    
    private void addLayer(String name) {
        saveUndoState();
        Layer layer = new Layer(name, artWidth, artHeight);
        layers.add(0, layer); // Add to top
        layerList.getSelectionModel().select(layer);
        drawCanvas();
    }
    
    private void moveLayer(int direction) { // -1 up, 1 down
        int index = layerList.getSelectionModel().getSelectedIndex();
        if (index < 0) return;
        
        int newIndex = index + direction;
        if (newIndex >= 0 && newIndex < layers.size()) {
            saveUndoState();
            Layer layer = layers.remove(index);
            layers.add(newIndex, layer);
            layerList.getSelectionModel().select(newIndex);
            drawCanvas();
        }
    }

    private void configureToolButton(ToggleButton btn, ToggleGroup group, Tool tool) {
        if (btn != null) {
            btn.setToggleGroup(group);
            btn.setOnAction(e -> currentTool = tool);
        }
    }

    private void setupIcons() {
        // Pencil: M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z
        setBtnIcon(btnPencil, "M3 17.25V21h3.75L17.81 9.94l-3.75-3.75L3 17.25zM20.71 7.04c.39-.39.39-1.02 0-1.41l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-1.83 1.83 3.75 3.75 1.83-1.83z");
        
        // Eraser: M16.24 3.56l4.95 4.94c.78.79.78 2.05 0 2.84L12 20.53a4.008 4.008 0 0 1-5.66 0L2.81 17c-.78-.79-.78-2.05 0-2.84l10.6-10.6c.79-.78 2.05-.78 2.83 0zM4.22 15.59l3.54 3.53c.78.79 2.04.79 2.83 0l8.48-8.48-3.53-3.54-8.49 8.48a2.003 2.003 0 0 0-.01 2.83z
        setBtnIcon(btnEraser, "M16.24 3.56l4.95 4.94c.78.79.78 2.05 0 2.84L12 20.53a4.008 4.008 0 0 1-5.66 0L2.81 17c-.78-.79-.78-2.05 0-2.84l10.6-10.6c.79-.78 2.05-.78 2.83 0zM4.22 15.59l3.54 3.53c.78.79 2.04.79 2.83 0l8.48-8.48-3.53-3.54-8.49 8.48a2.003 2.003 0 0 0-.01 2.83z");

        // Fill: Paint Bucket
        setBtnIcon(btnFill, "M16.56 8.94L7.62 0 6.21 1.41l2.38 2.38-5.15 5.15c-.59.59-.59 1.54 0 2.12l5.5 5.5c.29.29.68.44 1.06.44s.77-.15 1.06-.44l5.5-5.5c.59-.58.59-1.53 0-2.12zM5.21 10l3.71-3.71 4.83 4.83L10.04 14.83 5.21 10zM19 11.5s-2 2.17-2 3.5c0 1.1.9 2 2 2s2-.9 2-2c0-1.33-2-3.5-2-3.5z");
        
        // Select Rect: Dashed Square
        setBtnIcon(btnSelectRect, "M3 3h18v18H3V3zm2 2v14h14V5H5z M7 7h10v10H7V7z"); // Simplified, usually dashed.
        // Better Rect Select icon:
        setBtnIcon(btnSelectRect, "M4 4h2v2H4V4zm4 0h2v2H8V4zm4 0h2v2h-2V4zm4 0h2v2h-2V4zm0 4h2v2h-2V8zm0 4h2v2h-2v-2zm0 4h2v2h-2v-2zM4 20h2v-2H4v2zm4 0h2v-2H8v2zm4 0h2v-2h-2v2zm-8-4h2v-2H4v2zm0-4h2v-2H4v2z");

        // Select Lasso: Freeform loop - Dashed/Dotted style
        setBtnIcon(btnSelectLasso, "M10 4h2v2h-2z M14 4h2v2h-2z M17 6h2v2h-2z M19 9h2v2h-2z M19 13h2v2h-2z M16 16h2v2h-2z M12 18h2v2h-2z M8 17h2v2h-2z M5 14h2v2h-2z M5 10h2v2h-2z M7 6h2v2h-2z M14 20h2v2h-2z"); 


        // Picker: Eyedropper
        setBtnIcon(btnPicker, "M20.71 5.63l-2.34-2.34c-.39-.39-1.02-.39-1.41 0l-3.12 3.12-1.93-1.91-1.41 1.41 1.42 1.42L3 16.25V21h4.75l8.92-8.92 1.42 1.42 1.41-1.41-1.92-1.92 3.12-3.12c.4-.4.4-1.03.01-1.42zM5.21 18.83l-1.41-1.41 8.06-8.06 1.41 1.41-8.06 8.06z");

        // Line: Diagonal Line (Thick)
        setBtnIcon(btnLine, "M3.41 22 L2 20.59 L20.59 2 L22 3.41 Z");
        
        // Rect: M3 3h18v18H3V3zm2 2v14h14V5H5z
        setBtnIcon(btnRect, "M3 3h18v18H3V3zm2 2v14h14V5H5z");
        
        // Circle: M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z
        setBtnIcon(btnCircle, "M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8z");
        
        // Gradient: Square with diagonal shading lines
        setBtnIcon(btnGradient, "M3 3h18v18H3V3zm2 2v14h14V5H5zm2 2l10 10H7V7zm4 0l6 6v-6H11z");
        
        // Grid: M4 4h16v16H4V4zm2 2v4h4V6H6zm6 0v4h4V6h-4zm-6 6v4h4v-4H6zm6 0v4h4v-4h-4z
        setBtnIcon(btnGrid, "M4 4h16v16H4V4zm2 2v4h4V6H6zm6 0v4h4V6h-4zm-6 6v4h4v-4H6zm6 0v4h4v-4h-4z");
        
        // Flip H: M15 21h2v-2h-2v2zm4-12h2V7h-2v2zM3 5v14c0 1.1.9 2 2 2h4v-2H5V5h4V3H5c-1.1 0-2 .9-2 2zm16-2v2h2c0-1.1-.9-2-2-2zm-8 20h2V1h-2v22zm8-6h2v-2h-2v2zM15 5h2V3h-2v2zm4 8h2v-2h-2v2zm0 8c1.1 0 2-.9 2-2h-2v2z
        // Simplified Flip H: Two arrows pointing L and R.
        setBtnIcon(btnFlipH, "M7 11H17V13H7z M7 11L10 8V14z M17 11L14 8V14z"); // This is just an arrow line.
        // Let's use a standard "Swap Horiz" icon path: M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z
        setBtnIcon(btnFlipH, "M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z");
        
        // Flip V: Swap Vert: M16 17.01V10h-2v7.01h-3L15 21l4-3.99h-3zM9 3L5 6.99h3V14h2V6.99h3L9 3z
        setBtnIcon(btnFlipV, "M16 17.01V10h-2v7.01h-3L15 21l4-3.99h-3zM9 3L5 6.99h3V14h2V6.99h3L9 3z");
        
        // Swap Colors: Swap Arrows (Curved)
        setBtnIcon(btnSwapColors, "M6.99 11L3 15l3.99 4v-3H14v-2H6.99v-3zM21 9l-3.99-4v3H10v2h7.01v3L21 9z");
    }

    private void setBtnIcon(ButtonBase btn, String path) {
        if (btn != null) {
            SVGPath svg = new SVGPath();
            svg.setContent(path);
            svg.setFill(Color.WHITE);
            svg.setScaleX(0.7);
            svg.setScaleY(0.7);
            btn.setGraphic(svg);
        }
    }
    
    private void flipImage(boolean horizontal) {
        saveUndoState();
        for (Layer layer : layers) {
            WritableImage newImg = new WritableImage(artWidth, artHeight);
            PixelWriter pw = newImg.getPixelWriter();
            javafx.scene.image.PixelReader pr = layer.getImage().getPixelReader();
            
            for (int x = 0; x < artWidth; x++) {
                for (int y = 0; y < artHeight; y++) {
                    Color c = pr.getColor(x, y);
                    if (horizontal) {
                        pw.setColor(artWidth - 1 - x, y, c);
                    } else {
                        pw.setColor(x, artHeight - 1 - y, c);
                    }
                }
            }
            layer.setImage(newImg);
        }
        drawCanvas();
    }

    private void resizeArt(int newW, int newH) {
        saveUndoState();
        for (Layer layer : layers) {
            WritableImage newImg = new WritableImage(newW, newH);
            PixelWriter pw = newImg.getPixelWriter();
            javafx.scene.image.PixelReader pr = layer.getImage().getPixelReader();
            
            for (int x = 0; x < Math.min(artWidth, newW); x++) {
                for (int y = 0; y < Math.min(artHeight, newH); y++) {
                    pw.setColor(x, y, pr.getColor(x, y));
                }
            }
            layer.setImage(newImg);
        }
        
        artWidth = newW;
        artHeight = newH;
        updateCanvasSize();
    }

    private void setupCanvas() {
        updateCanvasSize();
    }

    private void setupEvents() {
        // Drawing and Panning
        canvas.addEventHandler(MouseEvent.MOUSE_PRESSED, this::handleMousePressed);
        canvas.addEventHandler(MouseEvent.MOUSE_DRAGGED, this::handleMouseDragged);
        canvas.addEventHandler(MouseEvent.MOUSE_RELEASED, this::handleMouseReleased);
        
        // Zooming with Ctrl + Scroll
        scrollPane.addEventFilter(javafx.scene.input.ScrollEvent.SCROLL, e -> {
            if (e.isControlDown()) {
                e.consume(); // Prevent scrolling
                
                double delta = e.getDeltaY();
                double zoomFactor = 1.1;
                double newZoom = zoom;
                
                if (delta > 0) {
                    newZoom *= zoomFactor;
                } else {
                    newZoom /= zoomFactor;
                }
                
                // Clamp zoom
                newZoom = Math.max(1.0, Math.min(newZoom, 50.0));
                
                // Update slider (which updates zoom variable and canvas)
                zoomSlider.setValue(newZoom);
            }
        });
    }

    private void handleMousePressed(MouseEvent e) {
        canvas.requestFocus(); // Ensure focus for key events
        
        if (e.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
            e.consume();
        } else {
            // Determine Color based on Button
            currentDrawColor = (e.getButton() == javafx.scene.input.MouseButton.SECONDARY) ? 
                               secondaryColorPicker.getValue() : colorPicker.getValue();

            // Save Undo State for drawing tools
            if (currentTool != Tool.PICKER && currentTool != Tool.SELECT_RECT && currentTool != Tool.SELECT_LASSO) {
                 saveUndoState();
            }

            int x = (int) (e.getX() / zoom);
            int y = (int) (e.getY() / zoom);
            
            // Selection Tools Logic
            if (currentTool == Tool.SELECT_RECT || currentTool == Tool.SELECT_LASSO) {
                // Check if clicking inside existing selection to move it
                boolean insideSelection = hasSelection && 
                    x >= Math.min(selectStartX, selectEndX) && x <= Math.max(selectStartX, selectEndX) &&
                    y >= Math.min(selectStartY, selectEndY) && y <= Math.max(selectStartY, selectEndY);

                if (insideSelection) {
                    isDraggingSelection = true;
                    dragStartX = x; 
                    dragStartY = y;
                } else {
                    // Start New Selection
                    commitSelection();
                    isSelecting = true;
                    selectStartX = x;
                    selectStartY = y;
                    selectEndX = x;
                    selectEndY = y;
                    hasSelection = false;
                    lassoPoints.clear();
                    if (currentTool == Tool.SELECT_LASSO) {
                        lassoPoints.add(new Point2D(x, y));
                    }
                    drawCanvas();
                }
                return;
            }

            // Commit floating selection if clicking with another tool
            if (hasSelection && floatingSelection != null) {
                commitSelection();
            }
            
            if (x >= 0 && x < artWidth && y >= 0 && y < artHeight) {
                if (currentTool == Tool.PICKER) {
                    pickColor(x, y);
                    return;
                }
                
                if (currentTool == Tool.FILL) {
                     if (activeLayer != null && activeLayer.isVisible()) {
                         floodFill(x, y, activeLayer.getImage().getPixelReader().getColor(x, y), currentDrawColor);
                     }
                     return;
                }
            }
            
            if (currentTool == Tool.LINE || currentTool == Tool.RECTANGLE || currentTool == Tool.CIRCLE) {
                if (activeLayer != null && activeLayer.isVisible()) {
                    isDraggingShape = true;
                    dragStartX = x;
                    dragStartY = y;
                    currentDragX = x;
                    currentDragY = y;
                    drawCanvas(); // Draw initial preview
                }
            } else if (currentTool == Tool.GRADIENT) {
                 if (activeLayer != null && activeLayer.isVisible()) {
                     isDraggingGradient = true;
                     dragStartX = x;
                     dragStartY = y;
                     currentDragX = x;
                     currentDragY = y;
                     drawCanvas();
                 }
            } else {
                handleDrawing(e);
            }
        }
    }
    
    private void pickColor(int x, int y) {
        // Find top-most visible non-transparent pixel
        for (Layer layer : layers) {
            if (layer.isVisible()) {
                Color c = layer.getImage().getPixelReader().getColor(x, y);
                if (c.getOpacity() > 0) {
                    colorPicker.setValue(c);
                    // Switch back to pencil? Or stay in picker? Usually stay.
                    // But user might want to draw immediately.
                    // VS Code / standard editors usually keep tool.
                    // But often convenient to switch back.
                    // I'll keep tool for now.
                    return;
                }
            }
        }
        // If nothing found (transparent), maybe pick white or transparent?
        // Or do nothing.
    }

    private void handleMouseDragged(MouseEvent e) {
        if (e.getButton() == javafx.scene.input.MouseButton.MIDDLE) {
            double deltaX = e.getSceneX() - lastMouseX;
            double deltaY = e.getSceneY() - lastMouseY;
            
            // Adjust scrollPane
            double hMax = scrollPane.getHmax();
            double vMax = scrollPane.getVmax();
            
            double contentWidth = canvasContainer.getWidth();
            double contentHeight = canvasContainer.getHeight();
            double viewportWidth = scrollPane.getViewportBounds().getWidth();
            double viewportHeight = scrollPane.getViewportBounds().getHeight();
            
            if (contentWidth > viewportWidth) {
                scrollPane.setHvalue(scrollPane.getHvalue() - deltaX / (contentWidth - viewportWidth));
            }
            if (contentHeight > viewportHeight) {
                scrollPane.setVvalue(scrollPane.getVvalue() - deltaY / (contentHeight - viewportHeight));
            }

            lastMouseX = e.getSceneX();
            lastMouseY = e.getSceneY();
            e.consume();
        } else {
            int x = (int) (e.getX() / zoom);
            int y = (int) (e.getY() / zoom);

            // Selection Logic
            if (isDraggingSelection) {
                int dx = x - dragStartX;
                int dy = y - dragStartY;
                
                selectStartX += dx;
                selectEndX += dx;
                selectStartY += dy;
                selectEndY += dy;
                
                if (floatingSelection != null) {
                    floatingX += dx;
                    floatingY += dy;
                }
                
                dragStartX = x;
                dragStartY = y;
                drawCanvas();
                return;
            }
            
            if (isSelecting) {
                if (currentTool == Tool.SELECT_LASSO) {
                     Point2D p = new Point2D(x, y);
                     if (lassoPoints.isEmpty() || !lassoPoints.get(lassoPoints.size() - 1).equals(p)) {
                         lassoPoints.add(p);
                         // Update bounding box
                         selectStartX = (int) lassoPoints.stream().mapToDouble(Point2D::getX).min().orElse(x);
                         selectStartY = (int) lassoPoints.stream().mapToDouble(Point2D::getY).min().orElse(y);
                         selectEndX = (int) lassoPoints.stream().mapToDouble(Point2D::getX).max().orElse(x);
                         selectEndY = (int) lassoPoints.stream().mapToDouble(Point2D::getY).max().orElse(y);
                     }
                } else {
                    selectEndX = x;
                    selectEndY = y;
                }
                drawCanvas();
                return;
            }

            if (currentTool == Tool.FILL) return;
            
            if (currentTool == Tool.PICKER) {
                 if (x >= 0 && x < artWidth && y >= 0 && y < artHeight) {
                     pickColor(x, y);
                 }
                 return;
            }
            
            if (isDraggingShape) {
                currentDragX = x;
                currentDragY = y;
                drawCanvas(); // Update preview
            } else if (isDraggingGradient) {
                currentDragX = x;
                currentDragY = y;
                drawCanvas();
            } else {
                handleDrawing(e);
            }
        }
    }
    
    private void handleMouseReleased(MouseEvent e) {
        if (isSelecting) {
            isSelecting = false;
            hasSelection = true;
            drawCanvas();
        }
        if (isDraggingSelection) {
            isDraggingSelection = false;
        }
        
        if (isDraggingShape) {
            isDraggingShape = false;
            // Commit shape
            drawShape(dragStartX, dragStartY, currentDragX, currentDragY, currentTool, currentDrawColor, true);
            drawCanvas();
        }
        
        if (isDraggingGradient) {
            isDraggingGradient = false;
            drawGradient(dragStartX, dragStartY, currentDragX, currentDragY, colorPicker.getValue(), secondaryColorPicker.getValue());
            drawCanvas();
        }
    }

    private void drawGradient(int x1, int y1, int x2, int y2, Color c1, Color c2) {
        if (activeLayer == null || !activeLayer.isVisible()) return;
        
        PixelWriter pw = activeLayer.getImage().getPixelWriter();
        PixelReader pr = activeLayer.getImage().getPixelReader();
        
        double dx = x2 - x1;
        double dy = y2 - y1;
        double lenSq = dx * dx + dy * dy;
        
        for (int x = 0; x < artWidth; x++) {
            for (int y = 0; y < artHeight; y++) {
                // Check selection constraint
                if (hasSelection) {
                    if (lassoPoints.isEmpty()) { // Rect selection
                        boolean inRect = x >= Math.min(selectStartX, selectEndX) && x <= Math.max(selectStartX, selectEndX) &&
                                         y >= Math.min(selectStartY, selectEndY) && y <= Math.max(selectStartY, selectEndY);
                        if (!inRect) continue;
                    } else { // Lasso selection
                         if (!isInsideLasso(x, y)) continue;
                    }
                }
                
                double t = 0;
                if (lenSq != 0) {
                    double px = x - x1;
                    double py = y - y1;
                    t = (px * dx + py * dy) / lenSq;
                }
                
                Color color;
                if (t <= 0) color = c1;
                else if (t >= 1) color = c2;
                else color = c1.interpolate(c2, t);
                
                
                pw.setColor(x, y, color);
            }
        }
    }

    private void handleDrawing(MouseEvent e) {
        if (activeLayer == null || !activeLayer.isVisible()) return;

        // Handle drawing logic
        int x = (int) (e.getX() / zoom);
        int y = (int) (e.getY() / zoom);

        if (x >= 0 && x < artWidth && y >= 0 && y < artHeight) {
            if (currentTool == Tool.ERASER) {
                drawPixel(x, y, Color.TRANSPARENT);
            } else if (currentTool == Tool.PENCIL) {
                drawPixel(x, y, currentDrawColor);
            }
            statusLabel.setText(String.format("Pos: %d, %d", x, y));
        }
    }

    private void drawPixel(int x, int y, Color color) {
        if (activeLayer != null && activeLayer.isVisible()) {
            PixelWriter pw = activeLayer.getImage().getPixelWriter();
            
            // Draw square based on toolSize
            // Center the square on the mouse position (approximate for even sizes)
            int offset = (toolSize - 1) / 2;
            int startX = x - offset;
            int startY = y - offset;
            
            for (int i = 0; i < toolSize; i++) {
                for (int j = 0; j < toolSize; j++) {
                    int px = startX + i;
                    int py = startY + j;
                    
                    // Boundary check
                    if (px >= 0 && px < artWidth && py >= 0 && py < artHeight) {
                         // Selection check
                         if (hasSelection) {
                             if (lassoPoints.isEmpty()) { // Rect selection
                                 boolean inRect = px >= Math.min(selectStartX, selectEndX) && px <= Math.max(selectStartX, selectEndX) &&
                                                  py >= Math.min(selectStartY, selectEndY) && py <= Math.max(selectStartY, selectEndY);
                                 if (!inRect) continue;
                             } else { // Lasso selection
                                  if (!isInsideLasso(px, py)) continue;
                             }
                         }
                         
                         pw.setColor(px, py, color);
                    }
                }
            }
            drawCanvas(); 
        }
    }

    private void drawCanvas() {
        GraphicsContext gc = canvas.getGraphicsContext2D();
        
        // Clear
        gc.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
        
        // Draw checkered background
        int size = 10; // checker size
        for (int x = 0; x < canvas.getWidth(); x += size) {
            for (int y = 0; y < canvas.getHeight(); y += size) {
                if (((x / size) + (y / size)) % 2 == 0) {
                    gc.setFill(Color.LIGHTGRAY);
                } else {
                    gc.setFill(Color.WHITE);
                }
                gc.fillRect(x, y, size, size);
            }
        }
        
        // Draw layers (Bottom to Top)
        gc.setImageSmoothing(false);
        for (int i = layers.size() - 1; i >= 0; i--) {
            Layer layer = layers.get(i);
            if (layer.isVisible()) {
                gc.drawImage(layer.getImage(), 0, 0, artWidth * zoom, artHeight * zoom);
            }
        }
        
        // Draw Floating Selection
        if (floatingSelection != null) {
            gc.drawImage(floatingSelection, floatingX * zoom, floatingY * zoom, floatingSelection.getWidth() * zoom, floatingSelection.getHeight() * zoom);
        }

        // Draw Selection Border
        if (hasSelection || isSelecting) {
            if (!lassoPoints.isEmpty()) {
                 gc.beginPath();
                 gc.moveTo(lassoPoints.get(0).getX() * zoom + zoom/2, lassoPoints.get(0).getY() * zoom + zoom/2);
                 for (int i = 1; i < lassoPoints.size(); i++) {
                     gc.lineTo(lassoPoints.get(i).getX() * zoom + zoom/2, lassoPoints.get(i).getY() * zoom + zoom/2);
                 }
                 if (hasSelection) gc.closePath();
                 
                 gc.setLineWidth(1);
                 gc.setStroke(Color.WHITE);
                 gc.setLineDashes(5);
                 gc.stroke();
                 
                 gc.setStroke(Color.BLACK);
                 gc.setLineDashes(5);
                 gc.setLineDashOffset(5);
                 gc.stroke();
                 gc.setLineDashes(0);
            } else {
                double x = Math.min(selectStartX, selectEndX) * zoom;
                double y = Math.min(selectStartY, selectEndY) * zoom;
                double w = (Math.abs(selectEndX - selectStartX) + 1) * zoom;
                double h = (Math.abs(selectEndY - selectStartY) + 1) * zoom;
                
                gc.setLineWidth(1);
                gc.setStroke(Color.WHITE);
                gc.setLineDashes(5);
                gc.strokeRect(x, y, w, h);
                
                gc.setStroke(Color.BLACK);
                gc.setLineDashes(5);
                gc.setLineDashOffset(5);
                gc.strokeRect(x, y, w, h);
                
                gc.setLineDashes(0); // Reset
            }
        }
        
        // Draw shape preview
        if (isDraggingShape) {
             drawShape(dragStartX, dragStartY, currentDragX, currentDragY, currentTool, currentDrawColor, false);
        }
        
        // Draw Gradient Preview
        if (isDraggingGradient) {
            gc.setStroke(Color.BLACK);
            gc.setLineWidth(1);
            gc.strokeLine(dragStartX * zoom + zoom/2, dragStartY * zoom + zoom/2, currentDragX * zoom + zoom/2, currentDragY * zoom + zoom/2);
            
            gc.setFill(colorPicker.getValue());
            gc.fillOval(dragStartX * zoom, dragStartY * zoom, zoom, zoom);
            
            gc.setFill(secondaryColorPicker.getValue());
            gc.fillOval(currentDragX * zoom, currentDragY * zoom, zoom, zoom);
        }
        
        // Draw Grid
        if (showGrid && zoom > 4) {
            gc.setStroke(Color.LIGHTGRAY);
            gc.setLineWidth(1);
            
            for (int x = 0; x <= artWidth; x++) {
                gc.strokeLine(x * zoom, 0, x * zoom, artHeight * zoom);
            }
            for (int y = 0; y <= artHeight; y++) {
                gc.strokeLine(0, y * zoom, artWidth * zoom, y * zoom);
            }
        }
    }

    private void drawShape(int x1, int y1, int x2, int y2, Tool tool, Color color, boolean commit) {
        if (commit) {
             if (activeLayer != null && activeLayer.isVisible()) {
                 PixelWriter pw = activeLayer.getImage().getPixelWriter();
                 drawShapePixels((x, y) -> {
                     if (x >= 0 && x < artWidth && y >= 0 && y < artHeight)
                        pw.setColor(x, y, color);
                 }, x1, y1, x2, y2, tool);
             }
        } else {
             GraphicsContext gc = canvas.getGraphicsContext2D();
             gc.setFill(color);
             drawShapePixels((x, y) -> {
                 // Preview drawing
                 gc.fillRect(x * zoom, y * zoom, zoom, zoom);
             }, x1, y1, x2, y2, tool);
        }
    }
    
    interface PixelConsumer {
        void accept(int x, int y);
    }
    
    private void drawShapePixels(PixelConsumer consumer, int x1, int y1, int x2, int y2, Tool tool) {
        if (tool == Tool.LINE) {
             drawLine(consumer, x1, y1, x2, y2);
        } else if (tool == Tool.RECTANGLE) {
             drawRect(consumer, x1, y1, x2, y2);
        } else if (tool == Tool.CIRCLE) {
             drawCircle(consumer, x1, y1, x2, y2);
        }
    }

    private void drawLine(PixelConsumer consumer, int x0, int y0, int x1, int y1) {
        int dx = Math.abs(x1 - x0);
        int dy = Math.abs(y1 - y0);
        int sx = x0 < x1 ? 1 : -1;
        int sy = y0 < y1 ? 1 : -1;
        int err = dx - dy;

        while (true) {
            consumer.accept(x0, y0);
            if (x0 == x1 && y0 == y1) break;
            int e2 = 2 * err;
            if (e2 > -dy) {
                err -= dy;
                x0 += sx;
            }
            if (e2 < dx) {
                err += dx;
                y0 += sy;
            }
        }
    }

    private void drawRect(PixelConsumer consumer, int x1, int y1, int x2, int y2) {
        int left = Math.min(x1, x2);
        int top = Math.min(y1, y2);
        int right = Math.max(x1, x2);
        int bottom = Math.max(y1, y2);
        
        for (int x = left; x <= right; x++) {
            consumer.accept(x, top);
            consumer.accept(x, bottom);
        }
        for (int y = top; y <= bottom; y++) {
            consumer.accept(left, y);
            consumer.accept(right, y);
        }
    }

    private void drawCircle(PixelConsumer consumer, int x1, int y1, int x2, int y2) {
        // Calculate center and radius
        int cx = (x1 + x2) / 2;
        int cy = (y1 + y2) / 2;
        int r = (int) Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2)) / 2;
        
        int x = r;
        int y = 0;
        int err = 0;

        while (x >= y) {
            consumer.accept(cx + x, cy + y);
            consumer.accept(cx + y, cy + x);
            consumer.accept(cx - y, cy + x);
            consumer.accept(cx - x, cy + y);
            consumer.accept(cx - x, cy - y);
            consumer.accept(cx - y, cy - x);
            consumer.accept(cx + y, cy - x);
            consumer.accept(cx + x, cy - y);

            if (err <= 0) {
                y += 1;
                err += 2 * y + 1;
            }
            if (err > 0) {
                x -= 1;
                err -= 2 * x + 1;
            }
        }
    }
    
    private void floodFill(int startX, int startY, Color targetColor, Color replacementColor) {
        if (activeLayer == null || !activeLayer.isVisible()) return;
        if (targetColor.equals(replacementColor)) return;
        
        WritableImage img = activeLayer.getImage();
        
        java.util.Queue<int[]> queue = new java.util.LinkedList<>();
        queue.add(new int[]{startX, startY});
        
        PixelWriter pw = img.getPixelWriter();
        boolean[][] visited = new boolean[artWidth][artHeight];
        visited[startX][startY] = true;
        
        while (!queue.isEmpty()) {
            int[] p = queue.poll();
            int x = p[0];
            int y = p[1];
            
            Color currentColor = img.getPixelReader().getColor(x, y);
            if (currentColor.equals(targetColor)) {
                pw.setColor(x, y, replacementColor);
                
                int[][] dirs = {{1,0}, {-1,0}, {0,1}, {0,-1}};
                for (int[] dir : dirs) {
                    int nx = x + dir[0];
                    int ny = y + dir[1];
                    if (nx >= 0 && nx < artWidth && ny >= 0 && ny < artHeight && !visited[nx][ny]) {
                         Color nextColor = img.getPixelReader().getColor(nx, ny);
                         if (nextColor.equals(targetColor)) {
                             visited[nx][ny] = true;
                             queue.add(new int[]{nx, ny});
                         }
                    }
                }
            }
        }
        drawCanvas();
    }

    public void setImage(Image image) {
        artWidth = (int) image.getWidth();
        artHeight = (int) image.getHeight();
        
        layers.clear();
        Layer layer = new Layer("Image", artWidth, artHeight);
        
        javafx.scene.image.PixelReader pr = image.getPixelReader();
        PixelWriter pw = layer.getImage().getPixelWriter();
        for (int x = 0; x < artWidth; x++) {
            for (int y = 0; y < artHeight; y++) {
                pw.setColor(x, y, pr.getColor(x, y));
            }
        }
        layers.add(layer);
        activeLayer = layer;
        
        if (widthField != null) widthField.setText(String.valueOf(artWidth));
        if (heightField != null) heightField.setText(String.valueOf(artHeight));
        
        setupCanvas();
    }

    private void exportImage() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Export Image");
        fileChooser.getExtensionFilters().addAll(
            new FileChooser.ExtensionFilter("PNG Images", "*.png"),
            new FileChooser.ExtensionFilter("All Files", "*.*")
        );
        
        // Set initial directory based on project context
        if (projectRoot != null && projectRoot.exists() && projectRoot.isDirectory()) {
            fileChooser.setInitialDirectory(projectRoot);
        } else {
            // Default to user home or system default
            String userHome = System.getProperty("user.home");
            if (userHome != null) {
                fileChooser.setInitialDirectory(new File(userHome));
            }
        }
        
        File file = fileChooser.showSaveDialog(canvas.getScene().getWindow());
        if (file != null) {
            try {
                // Combine layers logic
                Canvas tempCanvas = new Canvas(artWidth, artHeight);
                GraphicsContext gc = tempCanvas.getGraphicsContext2D();
                
                for (Layer layer : layers) {
                    if (layer.isVisible()) {
                        gc.drawImage(layer.getImage(), 0, 0);
                    }
                }
                
                WritableImage snapshot = new WritableImage(artWidth, artHeight);
                
                SnapshotParameters params = new SnapshotParameters();
                params.setFill(Color.TRANSPARENT);
                
                tempCanvas.snapshot(params, snapshot);
                
                ImageIO.write(SwingFXUtils.fromFXImage(snapshot, null), "png", file);
                
                if (statusLabel != null) {
                    statusLabel.setText("Image exported to " + file.getName());
                }
                
            } catch (IOException ex) {
                ex.printStackTrace();
                if (statusLabel != null) {
                    statusLabel.setText("Error exporting image: " + ex.getMessage());
                }
            }
        }
    }
}
