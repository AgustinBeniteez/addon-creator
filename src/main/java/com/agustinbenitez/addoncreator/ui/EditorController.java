package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.FileTreeManager;
import com.agustinbenitez.addoncreator.core.GitManager;
import com.agustinbenitez.addoncreator.core.ProjectGenerator;
import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.core.TodoManager;
import com.agustinbenitez.addoncreator.models.Project;
import org.eclipse.jgit.api.errors.GitAPIException;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCombination;
import javafx.scene.input.KeyCodeCombination;
import javafx.scene.input.TransferMode;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.scene.input.DragEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;
import javafx.scene.shape.*;
import javafx.scene.text.*;
import javafx.scene.Group;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
import javafx.scene.PerspectiveCamera;
import javafx.scene.SubScene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.scene.media.MediaView;
import javafx.util.Duration;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import netscape.javascript.JSObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.List;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;

import java.util.zip.ZipFile;
import java.util.zip.ZipEntry;
import java.io.InputStream;
import java.awt.Desktop;

import javafx.animation.PauseTransition;
import javafx.animation.FadeTransition;
import javafx.util.Duration;
import javafx.scene.Node;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.io.UncheckedIOException;

import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.ext.gfm.tables.TablesExtension;

import javafx.scene.control.ScrollPane;
import javafx.scene.layout.Priority;
import javafx.scene.text.TextFlow;
import javafx.scene.text.Text;
import javafx.geometry.Pos;
import javafx.geometry.Insets;

/**
 * Controller for the IDE-style editor screen
 * 
 * @author Agustín Benítez
 */
import javafx.scene.shape.Circle;
import javafx.scene.paint.Color;
import javafx.scene.image.Image;
import javafx.scene.paint.ImagePattern;

import com.agustinbenitez.addoncreator.core.SettingsManager;

public class EditorController {

    private static final Logger logger = LoggerFactory.getLogger(EditorController.class);

    // Menu items
    @FXML
    private MenuItem menuNewFile;
    @FXML
    private MenuItem menuSave;
    @FXML
    private MenuItem menuSaveAll;
    @FXML
    private CheckMenuItem menuAutoSave;
    @FXML
    private MenuItem menuExport;
    @FXML
    private MenuItem menuClose;
    @FXML
    private MenuItem menuAddEntity;
    @FXML
    private MenuItem menuAddItem;
    @FXML
    private MenuItem menuAddBlock;
    @FXML
    private MenuItem menuTest;
    @FXML
    private CheckMenuItem menuToggleConsole;
    @FXML
    private RadioMenuItem menuSidebarRight;
    @FXML
    private MenuItem menuAbout;
    @FXML
    private MenuItem menuDocs;
    @FXML
    private MenuItem menuLicenses;
    
    @FXML
    private RadioMenuItem menuSidebarLeft;

    // Toolbar
    @FXML
    private Button btnBack;
    @FXML
    private Button btnModeSwitch; // New Toggle Button
    @FXML
    private Button btnAddEz; // New Add Button for Ez Mode
    @FXML
    private Button btnExplorer;
    @FXML
    private Button btnSearch;
    @FXML
    private Button btnTodo;
    @FXML
    private Button btnSave;
    @FXML
    private Button btnFormat;
    @FXML
    private Button btnExport;
    @FXML
    private Button btnTest;
    @FXML
    private Button btnSettings;
    @FXML
    private Label projectNameToolbar;

    // Sidebar
    @FXML
    private StackPane sidebarContainer;
    @FXML
    private BorderPane mainLayout;
    @FXML
    private VBox activityBar;

    @FXML
    private VBox projectExplorerView;
    @FXML
    private BorderPane uiEzModeView; // New Ez Mode View
    @FXML
    private VBox ezLogicPalette;
    @FXML
    private Pane ezCanvas;
    @FXML
    private SplitPane ideSplitPane; // Code Mode View
    @FXML
    private ToggleButton btnEzEntities;
    @FXML
    private ToggleButton btnEzItems;
    @FXML
    private ToggleButton btnEzBlocks;
    @FXML
    private VBox ezEntitiesContainer;
    @FXML
    private VBox ezItemsContainer;
    @FXML
    private VBox ezBlocksContainer;
    @FXML
    private ListView<String> ezEntitiesList;
    @FXML
    private ListView<String> ezItemsList;
    @FXML
    private ListView<String> ezBlocksList;

    @FXML
    private VBox searchView;
    @FXML
    private TextField searchField;
    @FXML
    private Button btnDoSearch;
    @FXML
    private Label searchStatusLabel;
    @FXML
    private ListView<String> searchResultsList;
    
    @FXML
    private VBox todoView;
    @FXML
    private StackPane todoContentArea;
    
    // Git
    @FXML
    private Button btnGit;
    @FXML
    private VBox gitView;
    @FXML
    private VBox noGitRepoContent;
    @FXML
    private VBox gitRepoContent;
    @FXML
    private Button btnInitGit;
    @FXML
    private Button btnLinkRemote;
    @FXML
    private Label gitBranchLabel;
    @FXML
    private ListView<GitManager.GitChange> gitChangesList;
    @FXML
    private TextField commitTitleField;
    @FXML
    private TextArea commitDescriptionArea;
    @FXML
    private Button commitButton;
    @FXML
    private Button btnGitPush;
    @FXML
    private Button btnGitFetch;
    @FXML
    private Label gitStatusLabel;
    @FXML
    private Button btnUserProfile;
    @FXML
    private Group userProfileIcon;
    @FXML
    private Label errorStatusLabel;
    @FXML
    private Label languageStatusLabel;
    @FXML
    private Button btnGitSync;

    @FXML
    private Button btnAddElement;
    @FXML
    private Button btnNewFile;
    @FXML
    private TreeView<String> fileTree;

    // Editor
    @FXML
    private TabPane editorTabs;

    // Console
    @FXML
    private VBox consoleContainer;
    @FXML
    private Button btnClearConsole;
    @FXML
    private Button btnToggleConsole;
    @FXML
    private TextArea consoleOutput;
    @FXML
    private TabPane bottomTabPane; // Added reference
    @FXML
    private TextArea terminalOutput;
    @FXML
    private TextField terminalInput;
    
    private Process terminalProcess;
    private java.io.BufferedWriter terminalWriter;

    private Project currentProject;
    private ProjectManager projectManager;
    private GitManager gitManager;
    private TodoManager todoManager;
    private Map<Tab, Path> tabFileMap; // Map tabs to their file paths
    private Map<Tab, Boolean> tabDirtyMap; // Map tabs to their dirty state
    private boolean consoleVisible = true;
    private int errorCount = 0;
    private String lastErrorSource = "log";
    private boolean autoSaveEnabled = false;
    private PauseTransition searchDebounce;
    private Set<String> stagedFiles = new HashSet<>();
    
    // Drag variables
    private double mouseAnchorX, mouseAnchorY;
    private double initialNodeX, initialNodeY;

    @FXML
    public void initialize() {
        logger.info("Initializing IDE EditorController");

        projectManager = new ProjectManager();
        gitManager = new GitManager();
        tabFileMap = new HashMap<>();
        tabDirtyMap = new HashMap<>();

        setupMenuActions();
        setupToolbarActions();
        setupModeSwitch();
        setupAddEzButton();
        setupEzModeNavigation();
        setupVisualEditor();
        setupFileTree();
        setupConsole();
        setupTerminal();
        setupAddElementButton();
        setupNewFileButton();
        setupSearch();
        setupTodo();
        setupGitList();
        setupLanguageStatus();
        setupErrorStatus();
        setupUserProfile();
        
        // Tab selection listener to update save button state
        editorTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateSaveButtonState();
        });
        
        // Initial state
        updateSaveButtonState();

        log("IDE initialized successfully");
    }

    private void setupModeSwitch() {
        if (btnModeSwitch != null) {
            btnModeSwitch.setOnAction(e -> toggleMode());
            
            // Set initial icon based on initial state (Code Mode)
            // Initial state is Code Mode, so button should show "Easy Mode" icon (to switch to it)
            // But wait, toggleMode logic says:
            // if (isEzMode) { switch to Code } else { switch to Ez }
            // Initially uiEzModeView is visible=false (Code Mode).
            // So clicking will go to Ez Mode.
            // The button should indicate "Switch to Easy Mode", so it should show the Easy Mode Icon.
            // Wait, previous code:
            // if (isEzMode) { ... setGraphic(createEasyModeIcon()) ... tooltip("Switch to Easy Mode") }
            // This means if we are currently in Easy Mode, show the icon to go back to Easy Mode? No.
            // if (isEzMode) -> We are IN Easy Mode. We want to switch to Code Mode. So show Code Mode Icon.
            // My previous change:
            // if (isEzMode) { ... switch to Code ... setGraphic(createEasyModeIcon()) } -> This sets icon AFTER switching to Code.
            // So if I am in Code Mode, I see Easy Mode Icon. Correct.
            
            // So initially (Code Mode), I should set Easy Mode Icon.
            btnModeSwitch.setGraphic(createEasyModeIcon());
            btnModeSwitch.getTooltip().setText("Switch to Easy Mode");
        }
    }

    private void setupAddEzButton() {
        if (btnAddEz != null) {
            btnAddEz.setOnAction(e -> {
                ContextMenu contextMenu = new ContextMenu();
                
                MenuItem addEntity = new MenuItem("Entity");
                addEntity.setOnAction(ev -> {
                    if (menuAddEntity != null) menuAddEntity.fire();
                });
                
                MenuItem addItem = new MenuItem("Item");
                addItem.setOnAction(ev -> {
                    if (menuAddItem != null) menuAddItem.fire();
                });
                
                MenuItem addBlock = new MenuItem("Block");
                addBlock.setOnAction(ev -> {
                    if (menuAddBlock != null) menuAddBlock.fire();
                });
                
                contextMenu.getItems().addAll(addEntity, addItem, addBlock);
                
                // Show to the right of the button
                javafx.geometry.Bounds bounds = btnAddEz.localToScreen(btnAddEz.getBoundsInLocal());
                contextMenu.show(btnAddEz, bounds.getMaxX(), bounds.getMinY());
            });
        }
    }

    private void setupEzModeNavigation() {
        ToggleGroup group = new ToggleGroup();
        if (btnEzEntities != null) {
            btnEzEntities.setToggleGroup(group);
            btnEzEntities.setOnAction(e -> showEzSection("entities"));
        }
        if (btnEzItems != null) {
            btnEzItems.setToggleGroup(group);
            btnEzItems.setOnAction(e -> showEzSection("items"));
        }
        if (btnEzBlocks != null) {
            btnEzBlocks.setToggleGroup(group);
            btnEzBlocks.setOnAction(e -> showEzSection("blocks"));
        }
    }

    private void showEzSection(String section) {
        if (ezEntitiesContainer != null) ezEntitiesContainer.setVisible("entities".equals(section));
        if (ezItemsContainer != null) ezItemsContainer.setVisible("items".equals(section));
        if (ezBlocksContainer != null) ezBlocksContainer.setVisible("blocks".equals(section));
    }

    private void setupVisualEditor() {
        if (ezLogicPalette != null) {
            // Add sample blocks to palette
            ezLogicPalette.getChildren().clear();
            ezLogicPalette.getChildren().add(new VisualBlock("On Interact", "EVENT", Color.web("#FFC107"), true));
            ezLogicPalette.getChildren().add(new VisualBlock("On Tick", "EVENT", Color.web("#FFC107"), true));
            ezLogicPalette.getChildren().add(new VisualBlock("Spawn Entity", "ACTION", Color.web("#2196F3"), true));
            ezLogicPalette.getChildren().add(new VisualBlock("Give Item", "ACTION", Color.web("#2196F3"), true));
            ezLogicPalette.getChildren().add(new VisualBlock("Play Sound", "ACTION", Color.web("#2196F3"), true));
            ezLogicPalette.getChildren().add(new VisualBlock("Has Tag", "CONDITION", Color.web("#4CAF50"), true));
        }

        if (ezCanvas != null) {
            ezCanvas.setOnDragOver(event -> {
                if (event.getGestureSource() != ezCanvas && event.getDragboard().hasString()) {
                    event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                }
                event.consume();
            });

            ezCanvas.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String content = db.getString();
                    String[] parts = content.split(":");
                    if (parts.length >= 2) {
                        String type = parts[0];
                        String name = parts[1];
                        
                        Color color = Color.GRAY;
                        if (type.equals("EVENT")) color = Color.web("#FFC107");
                        else if (type.equals("ACTION")) color = Color.web("#2196F3");
                        else if (type.equals("CONDITION")) color = Color.web("#4CAF50");

                        VisualBlock newBlock = new VisualBlock(name, type, color, false);
                        newBlock.setLayoutX(event.getX());
                        newBlock.setLayoutY(event.getY());
                        
                        // Make draggable on canvas
                        makeDraggable(newBlock);

                        ezCanvas.getChildren().add(newBlock);
                        success = true;
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });
        }
    }

    private void makeDraggable(Node node) {
        node.setOnMousePressed(e -> {
            mouseAnchorX = e.getSceneX();
            mouseAnchorY = e.getSceneY();
            initialNodeX = node.getLayoutX();
            initialNodeY = node.getLayoutY();
            node.toFront();
        });
        node.setOnMouseDragged(e -> {
            node.setLayoutX(initialNodeX + e.getSceneX() - mouseAnchorX);
            node.setLayoutY(initialNodeY + e.getSceneY() - mouseAnchorY);
        });
    }


    private void toggleMode() {
        boolean isEzMode = uiEzModeView.isVisible();
        if (isEzMode) {
            // Switch to Code Mode
            uiEzModeView.setVisible(false);
            ideSplitPane.setVisible(true);
            
            // Sidebar buttons management
            if (btnExplorer != null) {
                btnExplorer.setVisible(true);
                btnExplorer.setManaged(true);
            }
            if (btnSearch != null) {
                btnSearch.setVisible(true);
                btnSearch.setManaged(true);
            }
            if (btnFormat != null) {
                btnFormat.setVisible(true);
                btnFormat.setManaged(true);
            }
            if (btnAddEz != null) {
                btnAddEz.setVisible(false);
                btnAddEz.setManaged(false);
            }
            
            // Set Icon to "UI Ez" - indicating click to go to Ez Mode
            btnModeSwitch.setGraphic(createEasyModeIcon());
            btnModeSwitch.getTooltip().setText("Switch to Easy Mode");
        } else {
            // Switch to Ez Mode
            uiEzModeView.setVisible(true);
            ideSplitPane.setVisible(false);

            // Sidebar buttons management
            if (btnExplorer != null) {
                btnExplorer.setVisible(false);
                btnExplorer.setManaged(false);
            }
            if (btnSearch != null) {
                btnSearch.setVisible(false);
                btnSearch.setManaged(false);
            }
            if (btnFormat != null) {
                btnFormat.setVisible(false);
                btnFormat.setManaged(false);
            }
            if (btnAddEz != null) {
                btnAddEz.setVisible(true);
                btnAddEz.setManaged(true);
            }
            
            // Set Icon to "Code" - indicating click to go to Code Mode
            btnModeSwitch.setGraphic(createCodeModeIcon());
            btnModeSwitch.getTooltip().setText("Switch to Code Mode");

            // Populate Lists
            populateEzLists();
        }
    }

    private Node createEasyModeIcon() {
        Pane icon = new Pane();
        icon.setPrefSize(64, 64);
        icon.setMaxSize(64, 64);
        // Center content if parent allows, but Pane doesn't center children automatically.
        // We draw at absolute coordinates.
        
        // Container
        Rectangle bg = new Rectangle(4, 4, 56, 56);
        bg.setArcWidth(16);
        bg.setArcHeight(16);
        bg.setFill(Color.web("#111"));
        bg.setStroke(Color.WHITE);
        bg.setStrokeWidth(4); // Thicker border
        icon.getChildren().add(bg);

        // Big Block
        Rectangle bigBlock = new Rectangle(18, 16, 22, 22); // Adjusted for centering approx
        bigBlock.setArcWidth(6);
        bigBlock.setArcHeight(6);
        bigBlock.setFill(Color.WHITE);
        
        // Group blocks to center them together maybe?
        // Let's manually adjust coordinates to look centered in 64x64.
        // Center is 32,32.
        // Container 56x56 at 4,4 is centered.
        
        // Original SVG:
        // ViewBox 0 0 64 64.
        // Rect x=2 y=2 w=60 h=60 (Stroke=2) -> Centered.
        // Big Block x=16 y=14 w=22 h=22.
        // Small Blocks y=38 (row). x=16, 28, 40.
        
        // My implementation with stroke 4:
        // Rect x=4 y=4 w=56 h=56. (Stroke center is at boundary, so 2px out, 2px in. 4-2=2 (outer edge). 4+56+2=62 (outer edge). Matches 2 to 62 range = 60px wide visual? No.
        // StrokeType.CENTERED is default.
        // If Rect is 4,4 56x56. Left=4, Right=60. Center=32.
        // Stroke 4: Extends from 2 to 62. Perfect for 64x64 bounds (1px margin).
        
        // Big Block: x=16 y=14. CenterX = 16+11 = 27. Not 32.
        // SVG was: x=16 w=22. Center = 27. It was left aligned?
        // User said "no sale ni centrado".
        // Let's center the blocks horizontally.
        // Total width of blocks: 
        // Small row: 16 to 40+10=50. Width = 34. Center = 16+17 = 33. Close to 32.
        // Big Block: x=16 w=22. Center=27.
        // Let's center everything around x=32.
        
        // Big Block (w=22): x = 32 - 11 = 21.
        // Small Blocks (3 blocks, w=10, gap=2? No gap=2 in SVG).
        // SVG: x=16, 28, 40. 16->26, 28->38, 40->50. Gaps: 28-26=2. 40-38=2.
        // Total width: 10+2+10+2+10 = 34.
        // Start x = 32 - 17 = 15.
        // So x=15, 27, 39.
        
        // Y positions:
        // SVG: Big y=14. Small y=38.
        // Let's keep Y relative.
        
        // Update Big Block
        bigBlock.setX(21); 
        bigBlock.setY(14);
        icon.getChildren().add(bigBlock);

        // Small Blocks
        // In SVG there were 4 small blocks? 
        // SVG: <rect x="40" y="14" ...> (Beside big block?)
        // <rect x="16" y="38"> <rect x="28" y="38"> <rect x="40" y="38">
        // So 1 small block beside big block, 3 below.
        // My previous code: createSmallBlock(icon, 40, 14); and 3 bottom.
        // Let's center that too.
        // Top Row: Big(22) + Gap(2) + Small(10) = 34.
        // Center x=32. Start x = 32 - 17 = 15.
        // Big: x=15. Small: x=15+22+2 = 39.
        
        // Bottom Row: 3 Smalls (w=34). Start x=15.
        // x=15, 27, 39.
        
        bigBlock.setX(15);
        
        createSmallBlock(icon, 39, 14);
        createSmallBlock(icon, 15, 38);
        createSmallBlock(icon, 27, 38);
        createSmallBlock(icon, 39, 38);

        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        return icon;
    }

    private void createSmallBlock(Pane parent, double x, double y) {
        Rectangle rect = new Rectangle(x, y, 10, 10);
        rect.setArcWidth(4);
        rect.setArcHeight(4);
        rect.setFill(Color.WHITE);
        parent.getChildren().add(rect);
    }

    private Node createCodeModeIcon() {
        Pane icon = new Pane();
        icon.setPrefSize(64, 64);
        icon.setMaxSize(64, 64);

        // Container
        Rectangle bg = new Rectangle(4, 4, 56, 56);
        bg.setArcWidth(16);
        bg.setArcHeight(16);
        bg.setFill(Color.web("#111"));
        bg.setStroke(Color.WHITE);
        bg.setStrokeWidth(4); // Thicker border
        icon.getChildren().add(bg);

        // Text
        Text text = new Text("<>");
        text.setFill(Color.WHITE);
        text.setFont(Font.font("Monospace", 28));
        
        // Center text using StackPane wrapper inside the Pane
        StackPane textStack = new StackPane(text);
        textStack.setPrefSize(64, 64);
        textStack.setAlignment(Pos.CENTER);
        // textStack.setPadding(new Insets(8, 0, 0, 0)); // Remove padding if not needed or adjust
        // Font 28 might need visual centering.
        // Let's try without padding first or slight adjustment.
        
        icon.getChildren().add(textStack);

        icon.setScaleX(0.8);
        icon.setScaleY(0.8);
        return icon;
    }

    private void populateEzLists() {
        if (currentProject == null) return;
        
        if (ezEntitiesList != null) {
            ezEntitiesList.getItems().setAll(currentProject.getEntities());
        }
        if (ezItemsList != null) {
            ezItemsList.getItems().setAll(currentProject.getItems());
        }
        if (ezBlocksList != null) {
            ezBlocksList.getItems().setAll(currentProject.getBlocks());
        }
    }

    private void setupTodo() {
        if (btnTodo != null) {
            btnTodo.setOnAction(e -> toggleTodoView());
        }
    }

    private void toggleTodoView() {
        if (todoView == null) return;
        
        boolean isVisible = todoView.isVisible();
        
        // Hide all views first
        projectExplorerView.setVisible(false);
        projectExplorerView.setManaged(false);
        searchView.setVisible(false);
        searchView.setManaged(false);
        if (gitView != null) {
            gitView.setVisible(false);
            gitView.setManaged(false);
        }
        todoView.setVisible(false);
        todoView.setManaged(false);

        // Toggle requested view
        if (!isVisible) {
            todoView.setVisible(true);
            todoView.setManaged(true);
            updateTodoView();
        } else {
            // Default back to explorer if closing
            projectExplorerView.setVisible(true);
            projectExplorerView.setManaged(true);
        }
    }

    private void updateTodoView() {
        if (todoContentArea == null) return;
        
        if (currentProject == null) {
             todoContentArea.getChildren().clear();
             Label label = new Label("Open a project to view tasks");
             label.setTextFill(Color.GRAY);
             todoContentArea.getChildren().add(label);
             return;
        }

        if (todoManager == null) {
             todoManager = new TodoManager(Paths.get(currentProject.getRootPath()));
        }
        
        // Check if todo is initialized
        if (!todoManager.isInitialized()) {
            todoContentArea.getChildren().clear();
            
            VBox box = new VBox(15);
            box.setAlignment(Pos.CENTER);
            
            Label label = new Label("No task list found");
            label.setTextFill(Color.WHITE);
            
            Button createBtn = new Button("Create Task List");
            createBtn.getStyleClass().add("primary-button");
            createBtn.setOnAction(e -> {
                try {
                    todoManager.initialize();
                    updateTodoView();
                } catch (IOException ex) {
                    logger.error("Failed to initialize todo list", ex);
                }
            });
            
            box.getChildren().addAll(label, createBtn);
            todoContentArea.getChildren().add(box);
        } else {
            // Show Task List
            try {
                todoManager.loadTasks();
                showTaskList();
            } catch (IOException e) {
                 logger.error("Failed to load tasks", e);
            }
        }
    }
    
    private void showTaskList() {
        todoContentArea.getChildren().clear();
        
        VBox mainBox = new VBox(10);
        mainBox.setPadding(new Insets(10));
        VBox.setVgrow(mainBox, Priority.ALWAYS);
        
        // Input for new task
        HBox inputBox = new HBox(5);
        TextField taskInput = new TextField();
        taskInput.setPromptText("Add a new task...");
        taskInput.setStyle("-fx-background-color: #333333; -fx-text-fill: white; -fx-background-radius: 4;");
        HBox.setHgrow(taskInput, Priority.ALWAYS);
        
        Button addBtn = new Button("+");
        addBtn.getStyleClass().add("primary-button");
        addBtn.setOnAction(e -> {
            String text = taskInput.getText().trim();
            if (!text.isEmpty()) {
                try {
                    todoManager.addTask(text);
                    taskInput.clear();
                    showTaskList(); // Refresh
                } catch (IOException ex) {
                    logger.error("Failed to add task", ex);
                }
            }
        });
        
        taskInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                addBtn.fire();
            }
        });
        
        inputBox.getChildren().addAll(taskInput, addBtn);
        
        // List of tasks
        ListView<TodoManager.Task> taskListView = new ListView<>();
        taskListView.setStyle("-fx-background-color: transparent;");
        taskListView.getItems().addAll(todoManager.getTasks());
        VBox.setVgrow(taskListView, Priority.ALWAYS);
        
        taskListView.setCellFactory(lv -> new ListCell<TodoManager.Task>() {
             @Override
             protected void updateItem(TodoManager.Task task, boolean empty) {
                 super.updateItem(task, empty);
                 if (empty || task == null) {
                     setGraphic(null);
                     setStyle("-fx-background-color: transparent;");
                 } else {
                     CheckBox cb = new CheckBox(task.getDescription());
                     cb.setSelected(task.isCompleted());
                     // Style
                     if (task.isCompleted()) {
                         cb.setStyle("-fx-opacity: 0.6; -fx-text-fill: #888888;");
                     } else {
                         cb.setStyle("-fx-text-fill: #cccccc;");
                     }
                     
                     cb.setOnAction(e -> {
                         task.setCompleted(cb.isSelected());
                         try {
                             todoManager.updateTask(task);
                             // Refresh just the style of this cell if possible, or reload list
                             if (task.isCompleted()) {
                                 cb.setStyle("-fx-opacity: 0.6; -fx-text-fill: #888888;");
                             } else {
                                 cb.setStyle("-fx-text-fill: #cccccc;");
                             }
                         } catch (IOException ex) {
                             logger.error("Failed to update task", ex);
                         }
                     });
                     
                     setGraphic(cb);
                     setStyle("-fx-background-color: transparent; -fx-padding: 5;");
                 }
             }
        });
        
        mainBox.getChildren().addAll(inputBox, taskListView);
        todoContentArea.getChildren().add(mainBox);
    }

    private void setupGitList() {
        // Setup branch selection for labels
        if (gitStatusLabel != null) {
            gitStatusLabel.setOnMouseClicked(e -> showBranchSelection(gitStatusLabel));
            gitStatusLabel.setCursor(javafx.scene.Cursor.HAND);
        }
        if (gitBranchLabel != null) {
            gitBranchLabel.setOnMouseClicked(e -> showBranchSelection(gitBranchLabel));
            gitBranchLabel.setCursor(javafx.scene.Cursor.HAND);
        }

        gitChangesList.setCellFactory(lv -> new ListCell<GitManager.GitChange>() {
            private final CheckBox checkBox = new CheckBox();
            private final Label pathLabel = new Label();
            private final Label statusLabel = new Label();
            private final HBox root = new HBox(5, checkBox, statusLabel, pathLabel);

            {
                checkBox.setOnAction(e -> {
                    GitManager.GitChange item = getItem();
                    if (item != null) {
                        if (checkBox.isSelected()) {
                            stagedFiles.add(item.filePath);
                        } else {
                            stagedFiles.remove(item.filePath);
                        }
                        updateCommitButtonText();
                    }
                });
                
                // Clicking the cell (but not checkbox) should show diff
                root.setOnMouseClicked(e -> {
                    if (e.getClickCount() == 1 && getItem() != null) {
                        showDiff(getItem());
                    }
                });
            }

            @Override
            protected void updateItem(GitManager.GitChange item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    pathLabel.setText(item.filePath);
                    statusLabel.setText("[" + item.type.substring(0, 1) + "]");
                    
                    // Style status
                    switch (item.type) {
                        case "MODIFIED": statusLabel.setStyle("-fx-text-fill: #E2C08D;"); break; // Yellowish
                        case "ADDED": statusLabel.setStyle("-fx-text-fill: #73C991;"); break; // Greenish
                        case "DELETED": statusLabel.setStyle("-fx-text-fill: #FF6B6B;"); break; // Reddish
                        case "UNTRACKED": statusLabel.setStyle("-fx-text-fill: #888888;"); break; // Gray
                    }

                    checkBox.setSelected(stagedFiles.contains(item.filePath));
                    setGraphic(root);
                }
            }
        });
    }

    private void updateCommitButtonText() {
        if (commitButton == null) return;
        
        try {
            String branch = gitManager != null ? gitManager.getCurrentBranch() : "HEAD";
            if (branch == null) branch = "HEAD";
            
            int count = stagedFiles.size();
            commitButton.setText("Commit " + count + " files to " + branch);
        } catch (Exception e) {
            commitButton.setText("Commit");
        }
    }

    private void showDiff(GitManager.GitChange change) {
        // Create or select diff tab
        String tabTitle = "Diff: " + change.filePath;
        
        for (Tab tab : editorTabs.getTabs()) {
            if (tab.getText().equals(tabTitle)) {
                editorTabs.getSelectionModel().select(tab);
                return;
            }
        }

        // Create new tab with SplitPane
        Tab tab = new Tab(tabTitle);
        SplitPane splitPane = new SplitPane();
        
        // Left Side: Old Content (HEAD)
        String oldContent = gitManager.getFileContentFromHead(change.filePath);
        VBox leftBox = createDiffSide("HEAD (Old)", oldContent, true);
        
        // Right Side: New Content (Working Tree)
        String newContent = "";
        try {
            // Read current file content
            Path path = Paths.get(currentProject.getRootPath(), change.filePath);
            if (Files.exists(path)) {
                newContent = Files.readString(path);
            }
        } catch (IOException e) {
            newContent = "Error reading file: " + e.getMessage();
        }
        
        VBox rightBox = createDiffSide("Working Tree (New)", newContent, false);
        
        // Compute Diff and highlight
        highlightDiff(leftBox, rightBox, oldContent, newContent, change.filePath);

        splitPane.getItems().addAll(leftBox, rightBox);
        splitPane.setDividerPositions(0.5);
        
        tab.setContent(splitPane);
        editorTabs.getTabs().add(tab);
        editorTabs.getSelectionModel().select(tab);
    }
    
    private VBox createDiffSide(String title, String content, boolean isOld) {
        VBox container = new VBox();
        container.getStyleClass().add("diff-container");
        VBox.setVgrow(container, Priority.ALWAYS);
        
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-weight: bold; -fx-padding: 5; -fx-background-color: #2d2d2d; -fx-text-fill: #cccccc;");
        titleLabel.setMaxWidth(Double.MAX_VALUE);
        
        ListView<HBox> listView = new ListView<>();
        listView.getStyleClass().add("diff-list-view");
        listView.setStyle("-fx-font-family: 'Consolas', monospace; -fx-font-size: 12px; -fx-background-color: #1e1e1e;");
        VBox.setVgrow(listView, Priority.ALWAYS);
        
        container.getChildren().addAll(titleLabel, listView);
        
        // Store the ListView in properties for synchronization later if needed
        container.getProperties().put("listView", listView);
        
        return container;
    }
    
    private void highlightDiff(VBox leftBox, VBox rightBox, String oldContent, String newContent, String filePath) {
        ListView<HBox> leftList = (ListView<HBox>) leftBox.getProperties().get("listView");
        ListView<HBox> rightList = (ListView<HBox>) rightBox.getProperties().get("listView");
        
        List<String> oldLines = Arrays.asList(oldContent.split("\\r?\\n", -1));
        List<String> newLines = Arrays.asList(newContent.split("\\r?\\n", -1));
        
        // Parse Git Patch to identify changed lines
        Set<Integer> deletedLineIndices = new HashSet<>();
        Set<Integer> addedLineIndices = new HashSet<>();
        
        String patch = gitManager.getDiff(filePath);
        if (patch != null && !patch.isEmpty()) {
            String[] patchLines = patch.split("\\r?\\n");
            int currentOldLine = 0;
            int currentNewLine = 0;
            
            for (String line : patchLines) {
                if (line.startsWith("@@")) {
                    // Parse header like @@ -1,4 +1,5 @@
                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("@@ -(\\d+)(?:,\\d+)? \\+(\\d+)(?:,\\d+)? @@").matcher(line);
                    if (m.find()) {
                        currentOldLine = Integer.parseInt(m.group(1));
                        currentNewLine = Integer.parseInt(m.group(2));
                    }
                } else if (line.startsWith("-") && !line.startsWith("---")) {
                    deletedLineIndices.add(currentOldLine);
                    currentOldLine++;
                } else if (line.startsWith("+") && !line.startsWith("+++")) {
                    addedLineIndices.add(currentNewLine);
                    currentNewLine++;
                } else if (line.startsWith(" ")) {
                    currentOldLine++;
                    currentNewLine++;
                }
            }
        }
        
        // Populate Left (Old)
        for (int i = 0; i < oldLines.size(); i++) {
            String styleClass = deletedLineIndices.contains(i + 1) ? "diff-line-deleted" : null;
            leftList.getItems().add(createDiffLine((i+1) + "", oldLines.get(i), styleClass));
        }
        
        // Populate Right (New)
        for (int i = 0; i < newLines.size(); i++) {
             String styleClass = addedLineIndices.contains(i + 1) ? "diff-line-added" : null;
             rightList.getItems().add(createDiffLine((i+1) + "", newLines.get(i), styleClass));
        }
        
        // Synchronize scrolling
        ScrollBarSkinProxy.bindScrollBars(leftList, rightList);
    }
    
    private HBox createDiffLine(String lineNum, String content, String styleClass) {
        HBox box = new HBox(10);
        box.setAlignment(Pos.CENTER_LEFT);
        
        Label num = new Label(lineNum);
        num.setStyle("-fx-text-fill: #666666; -fx-min-width: 30; -fx-alignment: center-right;");
        
        Label text = new Label(content);
        text.setStyle("-fx-text-fill: #cccccc; -fx-font-family: 'Consolas', monospace;");
        
        if (styleClass != null) {
            box.getStyleClass().add(styleClass);
        }
        
        box.getChildren().addAll(num, text);
        return box;
    }
    
    // Helper class for scrolling (Inner class or static)
    private static class ScrollBarSkinProxy {
        static void bindScrollBars(ListView<?> lv1, ListView<?> lv2) {
             // This requires access to the VirtualFlow or ScrollBar, which is only available after layout.
             // We can use a simpler approach: bind formatted positions?
             // Not easy in standard JavaFX API.
             // We'll skip sync scrolling for this iteration to avoid crashes/complexity.
        }
    }

    private void setupSearch() {
        // Debounce setup: wait 400ms after typing stops before searching
        searchDebounce = new PauseTransition(Duration.millis(400));
        searchDebounce.setOnFinished(e -> performSearch());

        // Listen for text changes
        searchField.textProperty().addListener((obs, oldVal, newVal) -> {
            searchDebounce.playFromStart();
        });

        // Highlight matching text in results
        searchResultsList.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                    setText(null);
                } else {
                    // Item format: "filename:line - content |fullpath"
                    String displayPart = item;
                    String fullPath = "";
                    if (item.contains("|")) {
                        int pipeIndex = item.lastIndexOf("|");
                        displayPart = item.substring(0, pipeIndex);
                        fullPath = item.substring(pipeIndex + 1);
                    }

                    // Create TextFlow for highlighting
                    TextFlow textFlow = new TextFlow();
                    String lowerDisplay = displayPart.toLowerCase();
                    String query = searchField.getText().trim().toLowerCase();
                    
                    if (!query.isEmpty() && lowerDisplay.contains(query)) {
                        int index = lowerDisplay.indexOf(query);
                        while (index >= 0) {
                            // Text before match
                            if (index > 0) {
                                Text before = new Text(displayPart.substring(0, index));
                                before.setFill(Color.WHITE);
                                textFlow.getChildren().add(before);
                            }
                            
                            // Matched text
                            Text match = new Text(displayPart.substring(index, index + query.length()));
                            match.setFill(Color.web("#3B82F6")); // Blue highlight
                            match.setStyle("-fx-font-weight: bold;");
                            textFlow.getChildren().add(match);
                            
                            // Prepare for next iteration
                            displayPart = displayPart.substring(index + query.length());
                            lowerDisplay = displayPart.toLowerCase();
                            index = lowerDisplay.indexOf(query);
                        }
                        // Remaining text
                        if (!displayPart.isEmpty()) {
                            Text after = new Text(displayPart);
                            after.setFill(Color.WHITE);
                            textFlow.getChildren().add(after);
                        }
                    } else {
                        // No match or empty query
                        Text text = new Text(displayPart);
                        text.setFill(Color.WHITE);
                        textFlow.getChildren().add(text);
                    }
                    
                    // Add Icon
                    HBox hbox = new HBox(6);
                    hbox.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
                    
                    if (!fullPath.isEmpty()) {
                         try {
                             Path path = Paths.get(fullPath);
                             String fileName = path.getFileName().toString();
                             javafx.scene.Node icon = FileIconFactory.createIcon(fileName, false);
                             hbox.getChildren().add(icon);
                         } catch (Exception e) {
                             // Fallback if path is invalid
                         }
                    }
                    
                    hbox.getChildren().add(textFlow);
                    
                    setGraphic(hbox);
                    setText(null);
                }
            }
        });
    }

    public void setProject(Project project) {
        this.currentProject = project;
        this.todoManager = null; // Reset todo manager for new project
        if (todoContentArea != null) todoContentArea.getChildren().clear();
        
        projectNameToolbar.setText(project.getName());

        // Build file tree
        refreshFileTree();

        // Initialize Git
        try {
            gitManager.openRepository(new File(project.getRootPath()));
            refreshGitStatus();
        } catch (Exception e) {
            // Not a git repo or error
            if (gitBranchLabel != null) gitBranchLabel.setText("No Repo");
            if (gitStatusLabel != null) gitStatusLabel.setText("Disconnected Repo");
        }

        log("Proyecto cargado: " + project.getName());
        log("Ubicación: " + project.getRootPath());

        if (!project.hasContent()) {
            log("Proyecto vacío - Usa el botón '+' para añadir elementos");
        }
        
        // Restart terminal in project directory
        startTerminalProcess();
    }

    private void setupMenuActions() {
        menuNewFile.setOnAction(e -> showNewFileMenu(btnNewFile, false)); // Reuse btnNewFile as anchor but exclude folders
        menuSave.setOnAction(e -> handleSave());
        menuSaveAll.setOnAction(e -> handleSaveAll());
        menuAutoSave.setOnAction(e -> {
            autoSaveEnabled = menuAutoSave.isSelected();
            if (autoSaveEnabled) {
                handleSaveAll(); // Save everything when enabling
                log("Auto Save habilitado");
            } else {
                log("Auto Save deshabilitado");
            }
        });
        menuExport.setOnAction(e -> handleExport());
        menuClose.setOnAction(e -> handleClose());

        menuAddEntity.setOnAction(e -> handleAddEntity());
        menuAddItem.setOnAction(e -> handleAddItem());
        menuAddBlock.setOnAction(e -> handleAddBlock());
        menuTest.setOnAction(e -> handleTest());

        menuToggleConsole.setOnAction(e -> toggleConsole());
        
        if (menuSidebarLeft != null) {
            menuSidebarLeft.setOnAction(e -> moveSidebarToLeft());
        }
        if (menuSidebarRight != null) {
            menuSidebarRight.setOnAction(e -> moveSidebarToRight());
        }

        menuAbout.setOnAction(e -> showAbout());
        menuDocs.setOnAction(e -> showDocs());
        menuLicenses.setOnAction(e -> showLicenses());
    }

    private void setupToolbarActions() {
        btnBack.setOnAction(e -> handleClose());
        btnExplorer.setOnAction(e -> handleExplorer());
        btnSearch.setOnAction(e -> handleSearch());
        if (btnGit != null) btnGit.setOnAction(e -> handleGit());
        if (btnSave != null) btnSave.setOnAction(e -> handleSave());
        btnFormat.setOnAction(e -> handleFormat());
        btnExport.setOnAction(e -> handleExport());
        btnTest.setOnAction(e -> handleTest());
        btnSettings.setOnAction(e -> handleSettings());
        
        // Git Actions
        if (btnInitGit != null) {
            btnInitGit.setOnAction(e -> handleInitGit());
        }
        if (btnLinkRemote != null) {
            btnLinkRemote.setOnAction(e -> handleLinkRemote());
        }
        if (btnGitPush != null) btnGitPush.setOnAction(e -> handleGitPush());
        if (btnGitFetch != null) btnGitFetch.setOnAction(e -> handleGitFetch());
        
        if (commitButton != null) {
            commitButton.setOnAction(e -> handleCommit());
        }
        if (commitTitleField != null) {
            commitTitleField.setOnKeyPressed(e -> {
                if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                    handleCommit();
                }
            });
        }
        if (commitDescriptionArea != null) {
            commitDescriptionArea.setOnKeyPressed(e -> {
                if (e.isControlDown() && e.getCode() == KeyCode.ENTER) {
                    handleCommit();
                }
            });
        }
        
        // Search View Actions
        btnDoSearch.setOnAction(e -> performSearch());
        searchField.setOnAction(e -> performSearch()); // Enter key
        
        searchResultsList.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2) {
                String selected = searchResultsList.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    openSearchResult(selected);
                }
            }
        });
    }

    private void handleExplorer() {
        projectExplorerView.setVisible(true);
        projectExplorerView.setManaged(true);
        searchView.setVisible(false);
        searchView.setManaged(false);
        if (gitView != null) {
            gitView.setVisible(false);
            gitView.setManaged(false);
        }
        if (todoView != null) {
            todoView.setVisible(false);
            todoView.setManaged(false);
        }
    }

    private void handleSearch() {
        projectExplorerView.setVisible(false);
        projectExplorerView.setManaged(false);
        if (gitView != null) {
            gitView.setVisible(false);
            gitView.setManaged(false);
        }
        if (todoView != null) {
            todoView.setVisible(false);
            todoView.setManaged(false);
        }
        searchView.setVisible(true);
        searchView.setManaged(true);
        searchField.requestFocus();
    }
    
    private void handleGit() {
        projectExplorerView.setVisible(false);
        projectExplorerView.setManaged(false);
        searchView.setVisible(false);
        searchView.setManaged(false);
        if (todoView != null) {
            todoView.setVisible(false);
            todoView.setManaged(false);
        }
        if (gitView != null) {
            gitView.setVisible(true);
            gitView.setManaged(true);
        }
        refreshGitStatus();
    }
    
    private void showBranchSelection(javafx.scene.Node anchor) {
        if (gitManager == null || !gitManager.isRepositoryOpen()) return;
        
        try {
            java.util.List<String> branches = gitManager.getLocalBranches();
            String currentBranch = gitManager.getCurrentBranch();
            
            ContextMenu menu = new ContextMenu();
            
            // Title item
            MenuItem title = new MenuItem("Switch Branch");
            title.setDisable(true);
            title.setStyle("-fx-font-weight: bold; -fx-text-fill: black;");
            menu.getItems().add(title);
            menu.getItems().add(new SeparatorMenuItem());
            
            for (String branch : branches) {
                MenuItem item = new MenuItem(branch);
                if (branch.equals(currentBranch)) {
                    item.setStyle("-fx-font-weight: bold;");
                    item.setGraphic(new Label("✓"));
                }
                
                item.setOnAction(e -> {
                    try {
                        if (!branch.equals(currentBranch)) {
                            gitManager.checkoutBranch(branch);
                            log("Switched to branch: " + branch);
                            refreshGitStatus();
                        }
                    } catch (Exception ex) {
                        logger.error("Failed to switch branch", ex);
                        showError("Git Error", "Failed to checkout branch: " + ex.getMessage());
                    }
                });
                
                menu.getItems().add(item);
            }
            
            menu.show(anchor, javafx.geometry.Side.TOP, 0, 0);
            
        } catch (Exception e) {
            logger.error("Failed to list branches", e);
            showError("Git Error", "Failed to list branches: " + e.getMessage());
        }
    }

    private void refreshGitStatus() {
        if (gitManager == null || !gitManager.isRepositoryOpen()) {
            if (gitBranchLabel != null) gitBranchLabel.setText("No Repo");
            if (gitStatusLabel != null) gitStatusLabel.setText("Disconnected Repo");
            if (btnGitSync != null) btnGitSync.setVisible(false);
            if (gitChangesList != null) gitChangesList.getItems().clear();
            stagedFiles.clear();
            
            // Toggle view visibility
            if (noGitRepoContent != null) {
                noGitRepoContent.setVisible(true);
                noGitRepoContent.setManaged(true);
            }
            if (gitRepoContent != null) {
                gitRepoContent.setVisible(false);
                gitRepoContent.setManaged(false);
            }
            return;
        }

        // Show repo content
        if (noGitRepoContent != null) {
            noGitRepoContent.setVisible(false);
            noGitRepoContent.setManaged(false);
        }
        if (gitRepoContent != null) {
            gitRepoContent.setVisible(true);
            gitRepoContent.setManaged(true);
        }

        try {
            String branch = gitManager.getCurrentBranch();
            if (gitBranchLabel != null) gitBranchLabel.setText(branch != null ? branch : "HEAD");

            if (gitStatusLabel != null) {
                String repoName = currentProject != null ? currentProject.getName() : "Unknown Repo";
                gitStatusLabel.setText((branch != null ? branch : "HEAD") + " - " + repoName);
            }
            
            if (btnGitSync != null) btnGitSync.setVisible(true);

            if (gitChangesList != null) {
                var changes = gitManager.getChanges();
                gitChangesList.getItems().clear();
                gitChangesList.getItems().addAll(changes);
                
                // Keep previously staged files if they are still present
                // (Though usually we clear selection on refresh, but for UX let's keep valid ones)
                stagedFiles.retainAll(changes.stream().map(c -> c.filePath).collect(java.util.stream.Collectors.toSet()));
                updateCommitButtonText();
            }

            // Update Push/Fetch buttons
            if (btnGitPush != null && btnGitFetch != null) {
                int[] counts = gitManager.getAheadBehindCounts();
                btnGitPush.setText("Push (" + counts[0] + ")");
                btnGitFetch.setText("Fetch (" + counts[1] + ")");
            }
        } catch (Exception e) {
            logger.error("Failed to refresh git status", e);
        }
    }

    private void handleInitGit() {
        if (currentProject == null) return;
        
        try {
            File root = new File(currentProject.getRootPath());
            gitManager.initRepository(root);
            gitManager.addAll();
            gitManager.commit("Initial commit");
            
            log("Git repository initialized successfully");
            refreshGitStatus();
        } catch (Exception e) {
            logger.error("Failed to initialize git repository", e);
            showError("Git Error", "Failed to initialize repository: " + e.getMessage());
        }
    }

    private void handleLinkRemote() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Link Remote Repository");
        dialog.setHeaderText("Enter Git Remote URL");
        dialog.setContentText("URL:");

        dialog.showAndWait().ifPresent(url -> {
            if (url.trim().isEmpty()) return;
            try {
                gitManager.addRemote("origin", url.trim());
                log("Remote 'origin' added: " + url);
            } catch (Exception e) {
                logger.error("Failed to add remote", e);
                showError("Git Error", "Failed to add remote: " + e.getMessage());
            }
        });
    }

    private void handleCommit() {
        if (commitTitleField == null) return;

        String title = commitTitleField.getText().trim();
        if (title.isEmpty()) {
            showError("Commit Error", "Commit title cannot be empty");
            return;
        }
        
        String desc = commitDescriptionArea != null ? commitDescriptionArea.getText().trim() : "";
        String message = title + (desc.isEmpty() ? "" : "\n\n" + desc);
        
        if (stagedFiles.isEmpty()) {
            showError("Commit Error", "No files selected for commit");
            return;
        }

        try {
            if (!gitManager.isRepositoryOpen()) {
                 // Should be handled by init button now, but keep as fallback
                 handleInitGit();
                 return;
            }

            gitManager.add(stagedFiles);
            gitManager.commit(message);
            commitTitleField.clear();
            if (commitDescriptionArea != null) commitDescriptionArea.clear();
            stagedFiles.clear();
            log("✓ Committed: " + message);
            refreshGitStatus();
        } catch (Exception e) {
            logger.error("Commit failed", e);
            showError("Commit Error", "Failed to commit: " + e.getMessage());
        }
    }
    
    private void handleGitPush() {
        showLoginDialogAndExecute((user, pass) -> {
            try {
                gitManager.push(user, pass);
                log("Push successful");
            } catch (Exception e) {
                logger.error("Push failed", e);
                showError("Push Error", e.getMessage());
            }
        });
    }

    private void handleGitFetch() {
        showLoginDialogAndExecute((user, pass) -> {
            try {
                gitManager.fetch(user, pass);
                refreshGitStatus(); // Updates might show need to pull/merge
                log("Fetch successful");
            } catch (Exception e) {
                logger.error("Fetch failed", e);
                showError("Fetch Error", e.getMessage());
            }
        });
    }

    private void showLoginDialogAndExecute(java.util.function.BiConsumer<String, String> action) {
        Dialog<javafx.util.Pair<String, String>> dialog = new Dialog<>();
        dialog.setTitle("Sign In");
        dialog.setHeaderText(null);

        // Main Container
        VBox mainContent = new VBox(15);
        mainContent.setPadding(new Insets(20));
        mainContent.setMinWidth(350);

        // StackPane to switch views
        StackPane root = new StackPane();
        root.getChildren().add(mainContent);

        // --- VIEW 1: Provider Selection ---
        Label title = new Label("Choose your login method");
        title.setStyle("-fx-font-size: 14px; -fx-font-weight: bold;");
        
        // GitHub Button
        Button btnGithub = new Button("Sign in with GitHub");
        btnGithub.setMaxWidth(Double.MAX_VALUE);
        btnGithub.setStyle("-fx-background-color: #24292e; -fx-text-fill: white; -fx-alignment: CENTER_LEFT; -fx-padding: 10;");
        SVGPath githubIcon = new SVGPath();
        githubIcon.setContent("M12 .297c-6.63 0-12 5.373-12 12 0 5.303 3.438 9.8 8.205 11.385.6.113.82-.258.82-.577 0-.285-.01-1.04-.015-2.04-3.338.724-4.042-1.61-4.042-1.61C4.422 18.07 3.633 17.7 3.633 17.7c-1.087-.744.084-.729.084-.729 1.205.084 1.838 1.236 1.838 1.236 1.07 1.835 2.809 1.305 3.495.998.108-.776.417-1.305.76-1.605-2.665-.3-5.466-1.332-5.466-5.93 0-1.31.465-2.38 1.235-3.22-.135-.303-.54-1.523.105-3.176 0 0 1.005-.322 3.3 1.23.96-.267 1.98-.399 3-.405 1.02.006 2.04.138 3 .405 2.28-1.552 3.285-1.23 3.285-1.23.645 1.653.24 2.873.12 3.176.765.84 1.23 1.91 1.23 3.22 0 4.61-2.805 5.625-5.475 5.92.42.36.81 1.096.81 2.22 0 1.606-.015 2.896-.015 3.286 0 .315.21.69.825.57C20.565 22.092 24 17.592 24 12.297c0-6.627-5.373-12-12-12");
        githubIcon.setFill(Color.WHITE);
        githubIcon.setScaleX(0.7); githubIcon.setScaleY(0.7);
        btnGithub.setGraphic(new Group(githubIcon));
        btnGithub.setGraphicTextGap(15);
        
        // Google Button
        Button btnGoogle = new Button("Sign in with Google");
        btnGoogle.setMaxWidth(Double.MAX_VALUE);
        btnGoogle.setStyle("-fx-background-color: white; -fx-text-fill: #757575; -fx-border-color: #dadce0; -fx-alignment: CENTER_LEFT; -fx-padding: 10;");
        SVGPath googleIcon = new SVGPath();
        googleIcon.setContent("M12.48 10.92v3.28h7.84c-.24 1.84-.853 3.187-1.787 4.133-1.147 1.147-2.933 2.4-6.053 2.4-4.827 0-8.6-3.893-8.6-8.72s3.773-8.72 8.6-8.72c2.6 0 4.507 1.027 5.907 2.347l2.307-2.307C18.747 1.44 16.133 0 12.48 0 5.867 0 .533 5.333 .533 12S5.867 24 12.48 24c3.44 0 6.053-1.147 8.2-3.387 2.187-2.187 2.853-5.413 2.853-8.12 0-.8-.08-1.56-.24-2.293h-10.813z");
        googleIcon.setFill(Color.web("#4285F4"));
        googleIcon.setScaleX(0.7); googleIcon.setScaleY(0.7);
        btnGoogle.setGraphic(new Group(googleIcon));
        btnGoogle.setGraphicTextGap(15);

        // Manual Button
        Button btnManual = new Button("Manual Credentials");
        btnManual.setMaxWidth(Double.MAX_VALUE);
        btnManual.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 10;");

        // Cancel Button
        Button btnCancelMain = new Button("Cancel");
        btnCancelMain.setMaxWidth(Double.MAX_VALUE);
        btnCancelMain.setStyle("-fx-alignment: CENTER_LEFT; -fx-padding: 10; -fx-background-color: transparent; -fx-text-fill: #999;");
        btnCancelMain.setOnAction(e -> ((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow()).close());

        mainContent.getChildren().addAll(title, btnGithub, btnGoogle, new Separator(), btnManual, btnCancelMain);

        // --- VIEW 2: Credentials Form ---
        VBox formContent = new VBox(10);
        formContent.setPadding(new Insets(20));
        formContent.setVisible(false);
        formContent.setMinWidth(350);
        
        Label lblFormTitle = new Label("Enter Credentials");
        lblFormTitle.setStyle("-fx-font-weight: bold; -fx-font-size: 14px;");
        
        Label lblUser = new Label("Username");
        TextField txtUser = new TextField();
        
        Label lblPass = new Label("Password / Token");
        PasswordField txtPass = new PasswordField();
        
        Hyperlink linkHelp = new Hyperlink("Get Token");
        linkHelp.setVisible(false);
        
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        Button btnBack = new Button("Back");
        Button btnSubmit = new Button("Login");
        btnSubmit.setDefaultButton(true);
        buttonBox.getChildren().addAll(btnBack, btnSubmit);
        
        formContent.getChildren().addAll(lblFormTitle, lblUser, txtUser, lblPass, txtPass, linkHelp, buttonBox);
        
        // Add form to root (but make sure it covers main)
        root.getChildren().add(formContent);

        // --- Logic ---
        
        Runnable showMain = () -> {
            mainContent.setVisible(true);
            formContent.setVisible(false);
            dialog.getDialogPane().getButtonTypes().clear(); // Custom buttons
        };

        Runnable showForm = () -> {
            mainContent.setVisible(false);
            formContent.setVisible(true);
        };
        
        btnBack.setOnAction(e -> showMain.run());
        
        btnGithub.setOnAction(e -> {
            lblFormTitle.setText("Sign in with GitHub");
            lblUser.setText("GitHub Username");
            txtUser.setPromptText("username");
            lblPass.setText("Personal Access Token");
            txtPass.setPromptText("ghp_...");
            linkHelp.setText("Generate GitHub Token");
            linkHelp.setVisible(true);
            String url = "https://github.com/settings/tokens/new?scopes=repo,read:user&description=AddonCreator";
            linkHelp.setOnAction(ev -> openUrl(url));
            openUrl(url); // Auto-open browser
            showForm.run();
        });

        btnGoogle.setOnAction(e -> {
            lblFormTitle.setText("Sign in with Google");
            lblUser.setText("Gmail Address");
            txtUser.setPromptText("user@gmail.com");
            lblPass.setText("App Password");
            txtPass.setPromptText("16-character app password");
            linkHelp.setText("Get App Password");
            linkHelp.setVisible(true);
            String url = "https://myaccount.google.com/apppasswords";
            linkHelp.setOnAction(ev -> openUrl(url));
            openUrl(url); // Auto-open browser
            showForm.run();
        });

        btnManual.setOnAction(e -> {
            lblFormTitle.setText("Manual Login");
            lblUser.setText("Username");
            txtUser.setPromptText("");
            lblPass.setText("Password / Token");
            txtPass.setPromptText("");
            linkHelp.setVisible(false);
            showForm.run();
        });

        btnSubmit.setOnAction(e -> {
             String u = txtUser.getText().trim();
             String p = txtPass.getText().trim();
             if (!u.isEmpty() && !p.isEmpty()) {
                 action.accept(u, p);
                 // Close dialog manually since we aren't using ButtonTypes for result
                 ((javafx.stage.Stage) dialog.getDialogPane().getScene().getWindow()).close();
             } else {
                 showError("Login Error", "Please fill in all fields");
             }
        });

        // Initialize state
        showMain.run();
        
        dialog.getDialogPane().setContent(root);
        dialog.showAndWait();
    }

    private void openUrl(String url) {
        try {
            String os = System.getProperty("os.name").toLowerCase();
            if (os.contains("win")) {
                new ProcessBuilder("rundll32", "url.dll,FileProtocolHandler", url).start();
            } else if (os.contains("mac")) {
                new ProcessBuilder("open", url).start();
            } else {
                new ProcessBuilder("xdg-open", url).start();
            }
        } catch (Exception e) {
            logger.error("Failed to open URL: " + url, e);
        }
    }

    private void performSearch() {
        if (currentProject == null) {
            searchStatusLabel.setText("No project loaded");
            return;
        }

        String query = searchField.getText().trim();
        if (query.isEmpty()) {
            searchResultsList.getItems().clear();
            searchStatusLabel.setText("Ready to search");
            return;
        }
        
        searchResultsList.getItems().clear();
        searchStatusLabel.setText("Searching in project...");
        
        // Capture root path outside the thread
        String rootPath = currentProject.getRootPath();
        
        new Thread(() -> {
            try {
                Path root = Paths.get(rootPath);
                if (!Files.exists(root)) {
                     javafx.application.Platform.runLater(() -> 
                        searchStatusLabel.setText("Project root not found")
                     );
                     return;
                }

                AtomicInteger resultsCount = new AtomicInteger(0);
                int MAX_RESULTS = 500;

                try {
                    Files.walk(root)
                        .filter(Files::isRegularFile)
                        .forEach(file -> {
                            // Stop processing if we have enough results (optimization)
                            if (resultsCount.get() >= MAX_RESULTS) return;

                            String fileName = file.getFileName().toString();
                            boolean foundInFile = false;

                            // 1. Filename Match
                            if (fileName.toLowerCase().contains(query.toLowerCase())) {
                                String result = fileName + " (File Match)";
                                String fullPath = file.toAbsolutePath().toString();
                                String displayStr = result + " |" + fullPath;
                                
                                if (resultsCount.incrementAndGet() <= MAX_RESULTS) {
                                    javafx.application.Platform.runLater(() -> searchResultsList.getItems().add(displayStr));
                                }
                                foundInFile = true;
                            }

                            // 2. Content Match (skip binaries)
                            if (!isBinary(fileName)) {
                                try (Stream<String> stream = Files.lines(file)) {
                                    AtomicInteger lineNum = new AtomicInteger(0);
                                    stream.forEach(line -> {
                                        if (resultsCount.get() >= MAX_RESULTS) return;

                                        int currentLine = lineNum.incrementAndGet();
                                        if (line.toLowerCase().contains(query.toLowerCase())) {
                                            // Limit line length for display
                                            String displayLine = line.trim();
                                            if (displayLine.length() > 80) displayLine = displayLine.substring(0, 80) + "...";
                                            
                                            String result = fileName + ":" + currentLine + " - " + displayLine;
                                            String fullPath = file.toAbsolutePath().toString();
                                            String displayStr = result + " |" + fullPath;
                                            
                                            if (resultsCount.incrementAndGet() <= MAX_RESULTS) {
                                                javafx.application.Platform.runLater(() -> searchResultsList.getItems().add(displayStr));
                                            }
                                        }
                                    });
                                } catch (IOException | UncheckedIOException e) {
                                    // Ignore read errors
                                }
                            }
                        });
                } catch (IOException | UncheckedIOException e) {
                    logger.error("Error walking file tree", e);
                }
                    
                javafx.application.Platform.runLater(() -> {
                    int count = resultsCount.get();
                    String status = "Found " + count + (count >= MAX_RESULTS ? "+" : "") + " results";
                    searchStatusLabel.setText(status);
                });
                
            } catch (Exception e) {
                logger.error("Search failed", e);
                javafx.application.Platform.runLater(() -> searchStatusLabel.setText("Search error: " + e.getMessage()));
            }
        }).start();
    }

    private boolean isBinary(String fileName) {
        String lower = fileName.toLowerCase();
        return lower.endsWith(".png") || lower.endsWith(".jpg") || lower.endsWith(".jpeg") || 
               lower.endsWith(".gif") || lower.endsWith(".jar") || lower.endsWith(".zip") || 
               lower.endsWith(".exe") || lower.endsWith(".dll") || lower.endsWith(".class") ||
               lower.endsWith(".pdf");
    }
    
    private void openSearchResult(String resultItem) {
        // Format: "filename:line - content |fullpath"
        if (resultItem.contains("|")) {
            String fullPath = resultItem.substring(resultItem.lastIndexOf("|") + 1);
            Path path = Paths.get(fullPath);
            if (Files.exists(path)) {
                openFileByPath(path);
            }
        }
    }
    
    private void openMcPackPreview(Path filePath) {
        Tab tab = new Tab(filePath.getFileName().toString());
        setupTab(tab, filePath);

        VBox contentContainer = new VBox();
        contentContainer.setAlignment(Pos.CENTER);
        contentContainer.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 20;");

        // Card Container
        VBox card = new VBox(20);
        card.setMaxWidth(600);
        card.setStyle("-fx-background-color: #252526; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5); -fx-padding: 30;");

        // Top Section: Icon + Info
        HBox topSection = new HBox(20);
        topSection.setAlignment(Pos.CENTER_LEFT);

        // Icon Container
        StackPane iconContainer = new StackPane();
        iconContainer.setPrefSize(100, 100);
        iconContainer.setMinSize(100, 100);
        iconContainer.setMaxSize(100, 100);
        
        String name = "Unknown Pack";
        String description = "No description available";
        String version = "0.0.0";
        String uuid = "Unknown UUID";
        String type = "Unknown Type";
        javafx.scene.image.Image loadedIcon = null;

        try (ZipFile zip = new ZipFile(filePath.toFile())) {
            ZipEntry finalEntry = null;
            JsonObject finalJson = null;
            
            // Temporary storage for Priority
            ZipEntry bpEntry = null;
            JsonObject bpJson = null;
            ZipEntry rpEntry = null;
            JsonObject rpJson = null;
            ZipEntry otherEntry = null;
            JsonObject otherJson = null;

            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (!entry.isDirectory() && entry.getName().endsWith("manifest.json")) {
                    try (InputStream is = zip.getInputStream(entry)) {
                        String jsonTxt = new String(is.readAllBytes());
                        JsonObject json = JsonParser.parseString(jsonTxt).getAsJsonObject();
                        
                        String modType = "";
                        if (json.has("modules")) {
                            JsonArray modules = json.getAsJsonArray("modules");
                            if (modules.size() > 0) {
                                JsonObject module = modules.get(0).getAsJsonObject();
                                if (module.has("type")) {
                                    modType = module.get("type").getAsString();
                                }
                            }
                        }
                        
                        if ("data".equals(modType)) {
                            bpEntry = entry;
                            bpJson = json;
                            break; // Priority 1: BP found, stop searching (optimization)
                        } else if ("resources".equals(modType)) {
                            if (rpEntry == null) {
                                rpEntry = entry;
                                rpJson = json;
                            }
                        } else {
                            if (otherEntry == null) {
                                otherEntry = entry;
                                otherJson = json;
                            }
                        }
                    } catch (Exception ignore) {}
                }
            }
            
            // Selection Logic: BP > RP > Other
            if (bpEntry != null) {
                finalEntry = bpEntry;
                finalJson = bpJson;
            } else if (rpEntry != null) {
                finalEntry = rpEntry;
                finalJson = rpJson;
            } else {
                finalEntry = otherEntry;
                finalJson = otherJson;
            }

            // Extract Data
            if (finalJson != null) {
                if (finalJson.has("header")) {
                    JsonObject header = finalJson.getAsJsonObject("header");
                    if (header.has("name")) name = header.get("name").getAsString();
                    if (header.has("description")) description = header.get("description").getAsString();
                    if (header.has("version")) version = header.get("version").toString();
                    if (header.has("uuid")) uuid = header.get("uuid").getAsString();
                }
                
                if (finalJson.has("modules")) {
                    JsonArray modules = finalJson.getAsJsonArray("modules");
                    if (modules.size() > 0) {
                        String modType = modules.get(0).getAsJsonObject().get("type").getAsString();
                        type = modType.equals("resources") ? "Resource Pack" : 
                               modType.equals("data") ? "Behavior Pack" : modType;
                    }
                }
                
                // Find Icon relative to manifest
                if (finalEntry != null) {
                    String baseDir = "";
                    String entryName = finalEntry.getName();
                    if (entryName.contains("/")) {
                        baseDir = entryName.substring(0, entryName.lastIndexOf("/") + 1);
                    }
                    
                    String iconPath = baseDir + "pack_icon.png";
                    ZipEntry iconEntry = zip.getEntry(iconPath);
                    if (iconEntry != null) {
                        try (InputStream is = zip.getInputStream(iconEntry)) {
                            loadedIcon = new javafx.scene.image.Image(is);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error reading mcpack", e);
            name = "Error reading pack";
            description = e.getMessage();
        }
        
        if (loadedIcon != null) {
            javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(loadedIcon);
            iv.setFitWidth(100);
            iv.setFitHeight(100);
            iv.setPreserveRatio(true);
            iconContainer.getChildren().add(iv);
        } else {
             // Use fallback icon scaled up
             javafx.scene.Node fallback = FileIconFactory.createMcPackIcon();
             fallback.setScaleX(4.0);
             fallback.setScaleY(4.0);
             iconContainer.getChildren().add(fallback);
        }

        // Info Box
        VBox infoBox = new VBox(10);
        Label nameLabel = new Label(name);
        nameLabel.setStyle("-fx-font-size: 24px; -fx-font-weight: bold; -fx-text-fill: white;");
        nameLabel.setWrapText(true);
        
        Label descLabel = new Label(description);
        descLabel.setStyle("-fx-font-size: 14px; -fx-text-fill: #cccccc;");
        descLabel.setWrapText(true);
        
        Label dataLabel = new Label("Version: " + version + "\nUUID: " + uuid + "\nType: " + type);
        dataLabel.setStyle("-fx-text-fill: #888888; -fx-font-size: 12px; -fx-font-family: 'Consolas', monospace;");
        
        infoBox.getChildren().addAll(nameLabel, descLabel, dataLabel);
        HBox.setHgrow(infoBox, Priority.ALWAYS);
        
        topSection.getChildren().addAll(iconContainer, infoBox);
        
        // Bottom Section
        HBox bottomSection = new HBox();
        bottomSection.setAlignment(Pos.CENTER_LEFT);
        
        // Open Button
        Button openBtn = new Button("Abrir");
        openBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;");
        openBtn.setOnMouseEntered(e -> openBtn.setStyle("-fx-background-color: #1177BB; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;"));
        openBtn.setOnMouseExited(e -> openBtn.setStyle("-fx-background-color: #0E639C; -fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 8 20; -fx-background-radius: 4; -fx-cursor: hand;"));
        
        openBtn.setOnAction(e -> {
            try {
                Desktop.getDesktop().open(filePath.toFile());
            } catch (IOException ex) {
                showError("Error", "No se pudo abrir el archivo: " + ex.getMessage());
            }
        });
        
        // Extension Badge
        HBox extBadge = new HBox(8);
        extBadge.setAlignment(Pos.CENTER_RIGHT);
        
        String extName = filePath.getFileName().toString();
        String ext = extName.contains(".") ? extName.substring(extName.lastIndexOf('.')) : "";
        
        Label extLabel = new Label(ext);
        extLabel.setStyle("-fx-text-fill: white; -fx-font-size: 16px; -fx-font-weight: bold;");
        
        javafx.scene.Node badgeIcon = FileIconFactory.createMcPackIcon();
        badgeIcon.setScaleX(1.5);
        badgeIcon.setScaleY(1.5);
        
        extBadge.getChildren().addAll(badgeIcon, extLabel);
        
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        
        bottomSection.getChildren().addAll(openBtn, spacer, extBadge);
        
        card.getChildren().addAll(topSection, new Separator(), bottomSection);
        contentContainer.getChildren().add(card);

        tab.setContent(contentContainer);
        tabFileMap.put(tab, filePath);
        tabDirtyMap.put(tab, false);
        editorTabs.getTabs().add(tab);
        editorTabs.getSelectionModel().select(tab);
    }

    private void openMarkdownPreview(Path filePath) {
        Tab tab = new Tab(filePath.getFileName().toString());
        setupTab(tab, filePath);

        String content = "";
        try {
            content = Files.readString(filePath);
        } catch (IOException e) {
            logger.error("Failed to read markdown file", e);
            content = "Error reading file";
        }

        // --- Editor (Monaco) ---
        WebView editorWebView = new WebView();
        enableWebViewCopyPaste(editorWebView);
        WebEngine editorEngine = editorWebView.getEngine();
        editorWebView.setContextMenuEnabled(false);
        
        // --- Preview (HTML) ---
        WebView previewWebView = new WebView();
        WebEngine previewEngine = previewWebView.getEngine();

        // Markdown Parser
        Parser parser = Parser.builder().extensions(Arrays.asList(TablesExtension.create())).build();
        HtmlRenderer renderer = HtmlRenderer.builder().extensions(Arrays.asList(TablesExtension.create())).build();

        // Update Preview Logic
        java.util.function.Consumer<String> updatePreview = (md) -> {
            String html = renderer.render(parser.parse(md));
            String fullHtml = "<html><head><style>body { font-family: 'Segoe UI', sans-serif; padding: 20px; color: #d4d4d4; background-color: #1e1e1e; line-height: 1.6; } a { color: #3794ff; text-decoration: none; } a:hover { text-decoration: underline; } code { background-color: #2d2d2d; padding: 2px 5px; border-radius: 4px; font-family: 'Consolas', monospace; color: #ce9178; } pre { background-color: #1e1e1e; border: 1px solid #444; padding: 15px; border-radius: 5px; overflow: auto; } pre code { background-color: transparent; padding: 0; color: #d4d4d4; } table { border-collapse: collapse; width: 100%; margin: 15px 0; } th, td { border: 1px solid #444; padding: 10px; text-align: left; } th { background-color: #252526; } tr:nth-child(even) { background-color: #252526; } blockquote { border-left: 4px solid #4caf50; padding-left: 15px; color: #858585; margin: 15px 0; } h1, h2, h3, h4, h5, h6 { color: #569cd6; margin-top: 20px; } hr { border: 0; height: 1px; background: #444; margin: 20px 0; } img { max-width: 100%; border-radius: 5px; }</style></head><body>" + html + "</body></html>";
            javafx.application.Platform.runLater(() -> previewEngine.loadContent(fullHtml));
        };

        // Load Initial Preview
        updatePreview.accept(content);

        // Load Monaco
        java.net.URL url = getClass().getResource("/monaco-editor/index.html");
        if (url != null) {
            String editorUrl = url.toExternalForm();
            JavaBridge bridge = new JavaBridge(content);
            bridge.setOnContentChangeCallback(updatePreview);
            
            editorEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                if (newState == Worker.State.SUCCEEDED) {
                    JSObject window = (JSObject) editorEngine.executeScript("window");
                    window.setMember("javaApp", bridge);
                    
                    String initScript = "setTimeout(function() { " +
                        "if(typeof initEditor === 'function') { " +
                            "var editor = initEditor(javaApp.getContent(), 'markdown'); " +
                            "editor.onDidChangeModelContent(function() { " +
                                "javaApp.onMarkdownChange(editor.getValue()); " +
                            "}); " +
                        "} " +
                    "}, 200);";
                    editorEngine.executeScript(initScript);
                }
            });
            editorEngine.load(editorUrl);
        } else {
            editorEngine.loadContent("Error: Monaco Editor not found");
        }

        // --- Layout & SplitPane ---
        SplitPane splitPane = new SplitPane(editorWebView, previewWebView);
        splitPane.setDividerPositions(0.5);

        // --- Toolbar & View Mode ---
        ComboBox<String> viewMode = new ComboBox<>();
        viewMode.getItems().addAll("Split View", "Editor Only", "Preview Only");
        viewMode.setValue("Split View");
        
        // Icons
        SVGPath splitIcon = new SVGPath();
        splitIcon.setContent("M4 4h16v16H4V4zm7 2H6v12h5V6zm7 0h-5v12h5V6z");
        splitIcon.setFill(Color.WHITE);
        
        SVGPath editorIcon = new SVGPath();
        editorIcon.setContent("M9.4 16.6L4.8 12l4.6-4.6L8 6l-6 6 6 6 1.4-1.4zm5.2 0l4.6-4.6-4.6-4.6L16 6l6 6-6 6-1.4-1.4z");
        editorIcon.setFill(Color.WHITE);
        
        SVGPath previewIcon = new SVGPath();
        previewIcon.setContent("M12 4.5C7 4.5 2.73 7.61 1 12c1.73 4.39 6 7.5 11 7.5s9.27-3.11 11-7.5c-1.73-4.39-6-7.5-11-7.5zM12 17c-2.76 0-5-2.24-5-5s2.24-5 5-5 5 2.24 5 5-2.24 5-5 5zm0-8c-1.66 0-3 1.34-3 3s1.34 3 3 3 3-1.34 3-3-1.34-3-3-3z");
        previewIcon.setFill(Color.WHITE);
        
        viewMode.setCellFactory(lv -> new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.equals("Split View")) setGraphic(createIcon(splitIcon.getContent()));
                    else if (item.equals("Editor Only")) setGraphic(createIcon(editorIcon.getContent()));
                    else if (item.equals("Preview Only")) setGraphic(createIcon(previewIcon.getContent()));
                    setStyle("-fx-text-fill: white; -fx-background-color: #333;");
                }
            }
        });
        viewMode.setButtonCell(new ListCell<String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(item);
                    if (item.equals("Split View")) setGraphic(createIcon(splitIcon.getContent()));
                    else if (item.equals("Editor Only")) setGraphic(createIcon(editorIcon.getContent()));
                    else if (item.equals("Preview Only")) setGraphic(createIcon(previewIcon.getContent()));
                    setStyle("-fx-text-fill: white; -fx-background-color: transparent;");
                }
            }
        });

        // Styles
        viewMode.setStyle("-fx-background-color: #333; -fx-text-fill: white; -fx-mark-color: white;");
        
        viewMode.setOnAction(e -> {
            switch (viewMode.getValue()) {
                case "Split View":
                    splitPane.getItems().setAll(editorWebView, previewWebView);
                    splitPane.setDividerPositions(0.5);
                    break;
                case "Editor Only":
                    splitPane.getItems().setAll(editorWebView);
                    break;
                case "Preview Only":
                    splitPane.getItems().setAll(previewWebView);
                    break;
            }
        });

        HBox toolBar = new HBox(10);
        toolBar.setPadding(new Insets(5));
        toolBar.setStyle("-fx-background-color: #252526; -fx-border-color: #3e3e42; -fx-border-width: 0 0 1 0;");
        Label modeLabel = new Label("View Mode:");
        modeLabel.setTextFill(Color.web("#cccccc"));
        toolBar.getChildren().addAll(modeLabel, viewMode);
        toolBar.setAlignment(Pos.CENTER_LEFT);

        VBox container = new VBox(toolBar, splitPane);
        VBox.setVgrow(splitPane, Priority.ALWAYS);
        
        // Store editor reference for saving (Required by handleSave)
        container.setUserData(editorWebView);

        tab.setContent(container);
        tabFileMap.put(tab, filePath);
        tabDirtyMap.put(tab, false);
        editorTabs.getTabs().add(tab);
        editorTabs.getSelectionModel().select(tab);
    }

    private SVGPath createIcon(String path) {
        SVGPath icon = new SVGPath();
        icon.setContent(path);
        icon.setFill(Color.web("#cccccc"));
        icon.setScaleX(0.7);
        icon.setScaleY(0.7);
        return icon;
    }

    private void openFileByPath(Path filePath) {
        openFileByPath(filePath, false);
    }

    private void openFileByPath(Path filePath, boolean forceCodeView) {
        // Intercept .TODO/tasks.json opening
        if (!forceCodeView && (filePath.endsWith(Paths.get(".TODO", "tasks.json")) || 
            (filePath.getFileName().toString().equals("tasks.json") && filePath.getParent() != null && filePath.getParent().getFileName().toString().equals(".TODO")))) {
            
            // Ensure sidebar is open
            if (todoView != null) {
                if (!todoView.isVisible()) {
                    toggleTodoView();
                } else {
                    // If already visible, just ensure it's updated
                    updateTodoView();
                }
            }
            return;
        }

        // Check if file is already open
        for (Tab tab : editorTabs.getTabs()) {
            if (tabFileMap.get(tab).equals(filePath)) {
                editorTabs.getSelectionModel().select(tab);
                return;
            }
        }

        String fileName = filePath.getFileName().toString().toLowerCase();

        // Check for .mcpack / .mcaddon
        if (fileName.endsWith(".mcpack") || fileName.endsWith(".mcaddon")) {
            openMcPackPreview(filePath);
            return;
        }

        // Check for Markdown
        if (fileName.endsWith(".md") || fileName.equalsIgnoreCase("readme.txt")) {
            openMarkdownPreview(filePath);
            return;
        }

        // Check for Audio
        if (fileName.endsWith(".mp3") || fileName.endsWith(".ogg") || fileName.endsWith(".wav")) {
            openAudioPreview(filePath);
            return;
        }

        // Check if it's an image file
        if (fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg")
                || fileName.endsWith(".gif")) {
            openImageInEditor(filePath);
            return;
        }

        // Check if it's a 3D model
        if (fileName.endsWith(".bbmodel") || fileName.endsWith(".geo.json")) {
            openModelInEditor(filePath);
            return;
        }

        // Open text file
        try {
            String content = Files.readString(filePath);

            Tab tab = new Tab(filePath.getFileName().toString());
            setupTab(tab, filePath);
            
            WebView webView = new WebView();
            enableWebViewCopyPaste(webView);
            WebEngine webEngine = webView.getEngine();
            
            // Disable context menu
            webView.setContextMenuEnabled(false);
            
            // Load local Monaco editor
            java.net.URL url = getClass().getResource("/monaco-editor/index.html");
            if (url == null) {
                log("✗ Error: No se encontró monaco-editor/index.html");
                // Fallback to TextArea if Monaco is missing
                TextArea editor = new TextArea(content);
                editor.setStyle("-fx-font-family: 'Consolas', 'Monaco', monospace; -fx-font-size: 13px;");
                tab.setContent(editor);
            } else {
                String editorUrl = url.toExternalForm();
                
                // Create a StackPane to hold WebView and LoadingOverlay
                StackPane contentContainer = new StackPane();
                contentContainer.getChildren().add(webView);
                
                // Add Loading Overlay
                Node loadingOverlay = LoadingSpinnerHelper.createOverlay("Cargando editor...");
                contentContainer.getChildren().add(loadingOverlay);
                
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("javaApp", new JavaBridge(content));
                        
                        String lang = getMonacoLanguage(filePath.getFileName().toString());
                        // Use initEditor for robust loading, fallback to old method if not found (though it should be)
                        webEngine.executeScript("setTimeout(function() { if(typeof initEditor === 'function') { initEditor(javaApp.getContent(), '" + lang + "'); } else { setContent(javaApp.getContent()); setLanguage('" + lang + "'); } }, 200);");
                        
                        // Fade out loading overlay
                        FadeTransition ft = new FadeTransition(Duration.millis(300), loadingOverlay);
                        ft.setFromValue(1.0);
                        ft.setToValue(0.0);
                        ft.setOnFinished(e -> contentContainer.getChildren().remove(loadingOverlay));
                        ft.play();
                    }
                });
                
                webEngine.load(editorUrl);
                tab.setContent(contentContainer);
            }

            tabFileMap.put(tab, filePath);
            tabDirtyMap.put(tab, false); // Not dirty initially
            editorTabs.getTabs().add(tab);
            editorTabs.getSelectionModel().select(tab);

            log("Archivo abierto: " + filePath.getFileName());

        } catch (IOException ex) {
            logger.error("Failed to open file", ex);
            log("✗ Error al abrir archivo: " + ex.getMessage());
        }
    }


    private void handleFormat() {
        Tab selectedTab = editorTabs.getSelectionModel().getSelectedItem();
        if (selectedTab != null && selectedTab.getContent() instanceof WebView) {
            WebView webView = (WebView) selectedTab.getContent();
            webView.getEngine().executeScript("if(typeof formatCode === 'function') { formatCode(); }");
            log("Formateando documento...");
        } else {
            log("No hay archivo compatible abierto para formatear");
        }
    }

    private void setupFileTree() {
        // Handle file selection
        fileTree.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal != null) {
                Path path = FileTreeManager.getPathFromTreeItem(newVal, Paths.get(currentProject.getRootPath()));
                if (Files.isRegularFile(path)) {
                    openFileInEditor(newVal);
                }
            }
        });

        // Setup Drag and Drop via CellFactory
        fileTree.setCellFactory(tree -> {
            TreeCell<String> cell = new TreeCell<String>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setText(null);
                        setGraphic(null);
                    } else {
                        setText(item);
                        
                        TreeItem<String> treeItem = getTreeItem();
                        Path path = FileTreeManager.getPathFromTreeItem(treeItem, Paths.get(currentProject.getRootPath()));
                        boolean isDir = Files.isDirectory(path);
                        
                        setGraphic(FileIconFactory.createIcon(item, isDir));
                    }
                }
            };

            // Context Menu
            cell.setOnContextMenuRequested(event -> {
                if (!cell.isEmpty()) {
                    fileTree.getSelectionModel().select(cell.getTreeItem());
                    showFileContextMenu(cell.getTreeItem(), event.getScreenX(), event.getScreenY());
                    event.consume();
                }
            });

            // Drag Detected
            cell.setOnDragDetected(event -> {
                if (!cell.isEmpty() && cell.getTreeItem() != fileTree.getRoot()) {
                    Dragboard db = cell.startDragAndDrop(TransferMode.MOVE);
                    ClipboardContent content = new ClipboardContent();
                    // Store the full path of the source
                    TreeItem<String> treeItem = cell.getTreeItem();
                    Path path = FileTreeManager.getPathFromTreeItem(treeItem, Paths.get(currentProject.getRootPath()));
                    content.putString(path.toAbsolutePath().toString());
                    db.setContent(content);
                    event.consume();
                }
            });

            // Drag Over
            cell.setOnDragOver(event -> {
                if (event.getGestureSource() != cell && event.getDragboard().hasString()) {
                    TreeItem<String> targetItem = cell.getTreeItem();
                    if (targetItem != null) {
                         Path targetPath = FileTreeManager.getPathFromTreeItem(targetItem, Paths.get(currentProject.getRootPath()));
                         // Allow drop if target is a folder (or root)
                         if (Files.isDirectory(targetPath)) {
                             event.acceptTransferModes(TransferMode.MOVE);
                         }
                    }
                }
                event.consume();
            });

            // Drag Dropped
            cell.setOnDragDropped(event -> {
                Dragboard db = event.getDragboard();
                boolean success = false;
                if (db.hasString()) {
                    String sourcePathStr = db.getString();
                    Path sourcePath = Paths.get(sourcePathStr);
                    
                    TreeItem<String> targetItem = cell.getTreeItem();
                    Path targetPath = FileTreeManager.getPathFromTreeItem(targetItem, Paths.get(currentProject.getRootPath()));

                    // If target is folder, move into it
                    if (Files.isDirectory(targetPath)) {
                        try {
                            Path destPath = targetPath.resolve(sourcePath.getFileName());
                            
                            // Check if moving into itself or subfolder
                            if (!destPath.equals(sourcePath) && !destPath.startsWith(sourcePath)) {
                                Files.move(sourcePath, destPath);
                                success = true;
                                refreshFileTree();
                                log("✓ Movido: " + sourcePath.getFileName());
                                
                                // Update tabs
                                for (Map.Entry<Tab, Path> entry : tabFileMap.entrySet()) {
                                    if (entry.getValue().equals(sourcePath)) {
                                        entry.setValue(destPath);
                                    }
                                }
                            }
                        } catch (IOException e) {
                            logger.error("Failed to move file", e);
                            log("✗ Error al mover: " + e.getMessage());
                        }
                    }
                }
                event.setDropCompleted(success);
                event.consume();
            });

            return cell;
        });
    }

    private void showFileContextMenu(TreeItem<String> item, double x, double y) {
        ContextMenu menu = new ContextMenu();

        MenuItem openItem = new MenuItem("📂 Abrir");
        openItem.setOnAction(e -> {
            Path path = FileTreeManager.getPathFromTreeItem(item, Paths.get(currentProject.getRootPath()));
            if (Files.isRegularFile(path)) {
                openFileByPath(path, true); // Force code view (skips TODO check)
            } else if (Files.isDirectory(path)) {
                if (!item.isExpanded()) {
                    item.setExpanded(true);
                }
            }
        });

        MenuItem renameItem = new MenuItem("✏ Renombrar");
        renameItem.setOnAction(e -> handleRenameFile(item));

        MenuItem deleteItem = new MenuItem("🗑 Eliminar");
        deleteItem.setOnAction(e -> handleDeleteFile(item));

        menu.getItems().addAll(openItem, renameItem, deleteItem);
        menu.show(fileTree, x, y);
    }

    private void handleRenameFile(TreeItem<String> item) {
        Path filePath = FileTreeManager.getPathFromTreeItem(item, Paths.get(currentProject.getRootPath()));
        
        TextInputDialog dialog = new TextInputDialog(filePath.getFileName().toString());
        dialog.setTitle("Renombrar");
        dialog.setHeaderText("Renombrar " + filePath.getFileName());
        dialog.setContentText("Nuevo nombre:");
        
        dialog.showAndWait().ifPresent(newName -> {
            if (newName.trim().isEmpty() || newName.equals(filePath.getFileName().toString())) {
                return;
            }
            
            try {
                Path targetPath = filePath.resolveSibling(newName);
                Files.move(filePath, targetPath);
                
                // Update open tabs if any
                for (Map.Entry<Tab, Path> entry : tabFileMap.entrySet()) {
                    if (entry.getValue().equals(filePath)) {
                        entry.setValue(targetPath);
                        // Re-setup tab to update header properly
                        setupTab(entry.getKey(), targetPath);
                    }
                }
                
                refreshFileTree();
                log("✓ Renombrado: " + newName);
                
            } catch (IOException e) {
                logger.error("Failed to rename file", e);
                showError("Error", "No se pudo renombrar: " + e.getMessage());
            }
        });
    }

    private void handleDeleteFile(TreeItem<String> item) {
        Path filePath = FileTreeManager.getPathFromTreeItem(item, Paths.get(currentProject.getRootPath()));

        Alert confirmDialog = new Alert(Alert.AlertType.CONFIRMATION);
        confirmDialog.setTitle("Confirmar eliminación");
        confirmDialog.setHeaderText("¿Eliminar " + filePath.getFileName() + "?");
        confirmDialog.setContentText("Esta acción no se puede deshacer.");

        confirmDialog.showAndWait().ifPresent(response -> {
            if (response == ButtonType.OK) {
                try {
                    if (Files.isDirectory(filePath)) {
                        // Delete directory and all contents
                        deleteDirectory(filePath);
                    } else {
                        // Delete file
                        Files.delete(filePath);

                        // Close tab if file is open
                        editorTabs.getTabs().removeIf(tab -> tabFileMap.get(tab).equals(filePath));
                    }

                    refreshFileTree();
                    log("✓ Eliminado: " + filePath.getFileName());

                } catch (IOException ex) {
                    logger.error("Failed to delete file", ex);
                    log("✗ Error al eliminar: " + ex.getMessage());
                    showError("Error", "No se pudo eliminar: " + ex.getMessage());
                }
            }
        });
    }

    private void deleteDirectory(Path directory) throws IOException {
        Files.walk(directory)
                .sorted((a, b) -> b.compareTo(a)) // Reverse order to delete files before directories
                .forEach(path -> {
                    try {
                        Files.delete(path);
                    } catch (IOException e) {
                        logger.error("Failed to delete: " + path, e);
                    }
                });
    }

    private void setupConsole() {
        btnClearConsole.setOnAction(e -> consoleOutput.clear());
        btnToggleConsole.setOnAction(e -> toggleConsole());
    }

    private void setupTerminal() {
        if (terminalInput != null) {
            terminalInput.setOnAction(e -> handleTerminalInput());
        }
        startTerminalProcess();
    }

    private void startTerminalProcess() {
        if (terminalProcess != null && terminalProcess.isAlive()) {
            terminalProcess.destroy();
        }
        
        Thread terminalThread = new Thread(() -> {
            try {
                // Determine OS and shell
                String os = System.getProperty("os.name").toLowerCase();
                String shell = os.contains("win") ? "powershell.exe" : "bash";
                
                ProcessBuilder pb = new ProcessBuilder(shell);
                pb.redirectErrorStream(true);
                
                // Set working directory to project root if available
                if (currentProject != null) {
                    pb.directory(new File(currentProject.getRootPath()));
                }
                
                terminalProcess = pb.start();
                
                terminalWriter = new java.io.BufferedWriter(new java.io.OutputStreamWriter(terminalProcess.getOutputStream()));
                
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(terminalProcess.getInputStream()))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        String finalLine = line;
                        javafx.application.Platform.runLater(() -> {
                            if (terminalOutput != null) {
                                terminalOutput.appendText(finalLine + "\n");
                            }
                            if (finalLine.toLowerCase().contains("error")) {
                                errorCount++;
                                lastErrorSource = "terminal";
                                updateErrorLabel();
                            }
                        });
                    }
                }
            } catch (IOException e) {
                log("Error starting terminal: " + e.getMessage());
            }
        });
        terminalThread.setDaemon(true);
        terminalThread.start();
    }

    private void handleTerminalInput() {
        String command = terminalInput.getText();
        terminalInput.clear();
        
        if (terminalWriter != null && terminalProcess != null && terminalProcess.isAlive()) {
            try {
                terminalWriter.write(command);
                terminalWriter.newLine();
                terminalWriter.flush();
            } catch (IOException e) {
                log("Error writing to terminal: " + e.getMessage());
            }
        } else {
             if (terminalOutput != null) {
                terminalOutput.appendText("Terminal is not running. Restarting...\n");
            }
            startTerminalProcess();
        }
    }

    private void setupAddElementButton() {
        btnAddElement.setOnAction(e -> showAddElementMenu());
    }

    private void setupNewFileButton() {
        btnNewFile.setOnAction(e -> showNewFileMenu(btnNewFile, true));
    }

    private void showNewFileMenu(javafx.scene.Node anchor, boolean includeFolders) {
        ContextMenu menu = new ContextMenu();

        if (includeFolders) {
            MenuItem folderItem = new MenuItem("📁 Nueva Carpeta");
            folderItem.setOnAction(e -> handleCreateFolder());
            menu.getItems().add(folderItem);
            menu.getItems().add(new SeparatorMenuItem());
        }

        MenuItem txtItem = new MenuItem("📄 Archivo .txt");
        txtItem.setOnAction(e -> handleCreateFile(".txt"));

        MenuItem jsonItem = new MenuItem("📄 Archivo .json");
        jsonItem.setOnAction(e -> handleCreateFile(".json"));
        
        MenuItem jsItem = new MenuItem("📄 Archivo .js");
        jsItem.setOnAction(e -> handleCreateFile(".js"));

        menu.getItems().addAll(txtItem, jsonItem, jsItem);
        
        if (anchor != null) {
            menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
        } else {
             // Fallback if no anchor provided (though we use btnNewFile currently)
             if (btnNewFile.getScene() != null && btnNewFile.getScene().getWindow() != null) {
                 double x = btnNewFile.getScene().getWindow().getX() + btnNewFile.getScene().getWindow().getWidth() / 2;
                 double y = btnNewFile.getScene().getWindow().getY() + btnNewFile.getScene().getWindow().getHeight() / 2;
                 menu.show(btnNewFile.getScene().getWindow(), x, y);
             }
        }
    }

    private void handleCreateFolder() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nueva Carpeta");
        dialog.setHeaderText("Crear nueva carpeta");
        dialog.setContentText("Nombre de la carpeta:");

        dialog.showAndWait().ifPresent(folderName -> {
            if (folderName.trim().isEmpty()) {
                showError("Error", "El nombre no puede estar vacío");
                return;
            }

            try {
                Path newFolder = Paths.get(currentProject.getRootPath(), folderName);
                Files.createDirectories(newFolder);
                refreshFileTree();
                log("✓ Carpeta creada: " + folderName);
            } catch (IOException e) {
                logger.error("Failed to create folder", e);
                log("✗ Error al crear carpeta: " + e.getMessage());
            }
        });
    }

    private void handleCreateFile(String extension) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Nuevo Archivo");
        dialog.setHeaderText("Crear nuevo archivo " + extension);
        dialog.setContentText("Nombre del archivo:");

        dialog.showAndWait().ifPresent(fileName -> {
            if (fileName.trim().isEmpty()) {
                showError("Error", "El nombre no puede estar vacío");
                return;
            }

            try {
                String fullName = fileName.endsWith(extension) ? fileName : fileName + extension;
                Path newFile = Paths.get(currentProject.getRootPath(), fullName);
                Files.writeString(newFile, "");
                refreshFileTree();
                log("✓ Archivo creado: " + fullName);
            } catch (IOException e) {
                logger.error("Failed to create file", e);
                log("✗ Error al crear archivo: " + e.getMessage());
            }
        });
    }

    private void showAddElementMenu() {
        ContextMenu menu = new ContextMenu();

        MenuItem entityItem = new MenuItem("📦 Entity");
        entityItem.setOnAction(e -> handleAddEntity());

        MenuItem itemItem = new MenuItem("🔨 Item");
        itemItem.setOnAction(e -> handleAddItem());

        MenuItem blockItem = new MenuItem("🧱 Block");
        blockItem.setOnAction(e -> handleAddBlock());

        menu.getItems().addAll(entityItem, itemItem, blockItem);
        menu.show(btnAddElement, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    private void refreshFileTree() {
        Path rootPath = Paths.get(currentProject.getRootPath());
        TreeItem<String> root = FileTreeManager.buildFileTree(rootPath);
        fileTree.setRoot(root);
    }

    private void setupTab(Tab tab, Path filePath) {
        // We use a custom HBox for the header to control layout precisely
        HBox header = new HBox(8);
        header.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        
        // 1. Icon
        javafx.scene.Node icon = FileIconFactory.createIcon(filePath.getFileName().toString(), false);
        
        // 2. Title Label
        Label titleLabel = new Label(filePath.getFileName().toString());
        titleLabel.setMaxWidth(150);
        titleLabel.setTextOverrun(OverrunStyle.ELLIPSIS);
        
        // 3. Close Button / Dirty Indicator
        StackPane indicatorContainer = new StackPane();
        indicatorContainer.setPrefSize(16, 16);
        
        // Close Button (X)
        Button closeBtn = new Button("✕");
        closeBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: #909090; -fx-padding: 0; -fx-font-size: 10px; -fx-cursor: hand;");
        closeBtn.setMinSize(16, 16);
        closeBtn.setMaxSize(16, 16);
        closeBtn.setOnAction(e -> {
            // Check if dirty and ask to save? For now just close or rely on save logic
            if (tabDirtyMap.getOrDefault(tab, false)) {
                 // Maybe prompt? Or just close. User asked for indicator.
                 // Ideally we should prompt. But standard behavior for now:
            }
            editorTabs.getTabs().remove(tab);
        });
        
        // Dirty Circle (●)
        Circle dirtyDot = new Circle(4);
        dirtyDot.setFill(Color.WHITE);
        dirtyDot.setVisible(false); // Hidden by default
        
        // Logic: Show X by default. If dirty, show Dot. 
        // If hovering Dot, show X? VS Code does this.
        
        indicatorContainer.getChildren().addAll(dirtyDot, closeBtn);
        
        // Logic to switch visibility
        Runnable updateState = () -> {
            boolean isDirty = tabDirtyMap.getOrDefault(tab, false);
            if (isDirty) {
                dirtyDot.setVisible(true);
                closeBtn.setVisible(false);
            } else {
                dirtyDot.setVisible(false);
                closeBtn.setVisible(true);
            }
        };
        
        // Add listener to show close button on hover even if dirty
        indicatorContainer.setOnMouseEntered(e -> {
            if (tabDirtyMap.getOrDefault(tab, false)) {
                dirtyDot.setVisible(false);
                closeBtn.setVisible(true);
            }
        });
        
        indicatorContainer.setOnMouseExited(e -> {
            if (tabDirtyMap.getOrDefault(tab, false)) {
                dirtyDot.setVisible(true);
                closeBtn.setVisible(false);
            }
        });
        
        // Store the updater in user data to access it later
        tab.setUserData(updateState);
        
        header.getChildren().addAll(icon, titleLabel, indicatorContainer);
        
        tab.setGraphic(header);
        tab.setText(""); // Clear text to avoid double name
        tab.setClosable(false); // Disable default close button

        // Context Menu
        ContextMenu contextMenu = new ContextMenu();

        MenuItem closeItem = new MenuItem("Cerrar");
        closeItem.setOnAction(e -> {
            editorTabs.getTabs().remove(tab);
        });

        MenuItem closeAllItem = new MenuItem("Cerrar Todo");
        closeAllItem.setOnAction(e -> {
            editorTabs.getTabs().clear();
        });
        
        MenuItem closeOthersItem = new MenuItem("Cerrar Otros");
        closeOthersItem.setOnAction(e -> {
            editorTabs.getTabs().removeIf(t -> t != tab);
        });

        contextMenu.getItems().addAll(closeItem, closeOthersItem, closeAllItem);
        tab.setContextMenu(contextMenu);
        
        // Ensure map is cleaned up when closed
        tab.setOnClosed(e -> {
            tabFileMap.remove(tab);
            tabDirtyMap.remove(tab);
        });
    }

    private void openFileInEditor(TreeItem<String> fileItem) {
        Path filePath = FileTreeManager.getPathFromTreeItem(fileItem, Paths.get(currentProject.getRootPath()));
        openFileByPath(filePath);
    }

    private void openImageInEditor(Path imagePath) {
        try {
            javafx.scene.image.Image image = new javafx.scene.image.Image(imagePath.toUri().toString());
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setPreserveRatio(true);
            
            // Set initial size
            double maxWidth = 800;
            if (image.getWidth() > maxWidth) {
                imageView.setFitWidth(maxWidth);
            } else {
                imageView.setFitWidth(image.getWidth());
            }

            // Zoom State
            final double[] currentZoom = {1.0};
            final double ZOOM_STEP = 0.1;
            final double MIN_ZOOM = 0.1;
            final double MAX_ZOOM = 5.0;

            // Create zoom controls
            Label zoomLabel = new Label("100%");
            zoomLabel.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 14px; -fx-padding: 0 10; -fx-font-family: 'Segoe UI', sans-serif;");
            
            // Icons
            SVGPath zoomOutIcon = new SVGPath();
            zoomOutIcon.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14zM7 9h5v1H7z");
            zoomOutIcon.setFill(Color.WHITE);
            zoomOutIcon.setScaleX(0.9); zoomOutIcon.setScaleY(0.9);

            SVGPath zoomInIcon = new SVGPath();
            zoomInIcon.setContent("M15.5 14h-.79l-.28-.27C15.41 12.59 16 11.11 16 9.5 16 5.91 13.09 3 9.5 3S3 5.91 3 9.5 5.91 16 9.5 16c1.61 0 3.09-.59 4.23-1.57l.27.28v.79l5 4.99L20.49 19l-4.99-5zm-6 0C7.01 14 5 11.99 5 9.5S7.01 5 9.5 5 14 7.01 14 9.5 11.99 14 9.5 14zM12 10h-2v2H9v-2H7V9h2V7h1v2h2v1z");
            zoomInIcon.setFill(Color.WHITE);
            zoomInIcon.setScaleX(0.9); zoomInIcon.setScaleY(0.9);

            // Buttons
            Button zoomOutBtn = new Button();
            zoomOutBtn.setGraphic(zoomOutIcon);
            zoomOutBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");
            
            Button zoomInBtn = new Button();
            zoomInBtn.setGraphic(zoomInIcon);
            zoomInBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;");

            Runnable updateZoom = () -> {
                imageView.setScaleX(currentZoom[0]);
                imageView.setScaleY(currentZoom[0]);
                zoomLabel.setText(String.format("%.0f%%", currentZoom[0] * 100));
            };

            zoomOutBtn.setOnAction(e -> {
                if (currentZoom[0] > MIN_ZOOM) {
                    currentZoom[0] = Math.max(MIN_ZOOM, currentZoom[0] - ZOOM_STEP);
                    updateZoom.run();
                }
            });

            zoomInBtn.setOnAction(e -> {
                if (currentZoom[0] < MAX_ZOOM) {
                    currentZoom[0] = Math.min(MAX_ZOOM, currentZoom[0] + ZOOM_STEP);
                    updateZoom.run();
                }
            });

            // Hover effects for buttons
            zoomOutBtn.setOnMouseEntered(e -> zoomOutBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 3;"));
            zoomOutBtn.setOnMouseExited(e -> zoomOutBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;"));
            
            zoomInBtn.setOnMouseEntered(e -> zoomInBtn.setStyle("-fx-background-color: rgba(255,255,255,0.1); -fx-cursor: hand; -fx-padding: 5; -fx-background-radius: 3;"));
            zoomInBtn.setOnMouseExited(e -> zoomInBtn.setStyle("-fx-background-color: transparent; -fx-cursor: hand; -fx-padding: 5;"));

            // Zoom with scroll wheel
            javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane(imageView);
            imageContainer.setStyle("-fx-background-color: #1e1e1e; -fx-alignment: center;");
            
            imageContainer.setOnScroll(e -> {
                if (e.isControlDown()) {
                    double delta = e.getDeltaY();
                    double zoomFactor = 1.1; // Slightly smoother
                    
                    if (delta < 0) {
                        currentZoom[0] = Math.max(MIN_ZOOM, currentZoom[0] / zoomFactor);
                    } else {
                        currentZoom[0] = Math.min(MAX_ZOOM, currentZoom[0] * zoomFactor);
                    }
                    updateZoom.run();
                    e.consume();
                }
            });

            // Wrap in ScrollPane for panning
            ScrollPane scrollPane = new ScrollPane(imageContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #1e1e1e; -fx-background: #1e1e1e;");
            
            // Zoom Controls Container
            HBox zoomControls = new HBox(5);
            zoomControls.setAlignment(javafx.geometry.Pos.CENTER);
            zoomControls.setStyle("-fx-background-color: #2b2b2b; -fx-background-radius: 20; -fx-padding: 5 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 0); -fx-border-color: #3e3e3e; -fx-border-radius: 20; -fx-border-width: 1;");
            zoomControls.setMaxWidth(Region.USE_PREF_SIZE);
            zoomControls.setMaxHeight(Region.USE_PREF_SIZE);
            zoomControls.getChildren().addAll(zoomOutBtn, zoomLabel, zoomInBtn);

            // Main Layout (StackPane to overlay controls)
            StackPane mainLayout = new StackPane();
            mainLayout.setStyle("-fx-background-color: #1e1e1e;");
            mainLayout.getChildren().add(scrollPane);
            mainLayout.getChildren().add(zoomControls);
            StackPane.setAlignment(zoomControls, javafx.geometry.Pos.BOTTOM_RIGHT);
            StackPane.setMargin(zoomControls, new javafx.geometry.Insets(20));

            Tab tab = new Tab(imagePath.getFileName().toString());
            setupTab(tab, imagePath);
            tab.setContent(mainLayout);

            tabFileMap.put(tab, imagePath);
            editorTabs.getTabs().add(tab);
            editorTabs.getSelectionModel().select(tab);

            log("Imagen abierta: " + imagePath.getFileName());

        } catch (Exception ex) {
            logger.error("Failed to open image", ex);
            log("✗ Error al abrir imagen: " + ex.getMessage());
        }
    }

    private void openAudioPreview(Path audioPath) {
        try {
            Media media = new Media(audioPath.toUri().toString());
            MediaPlayer mediaPlayer = new MediaPlayer(media);
            
            // Root Container (Full Tab Background)
            VBox rootContainer = new VBox();
            rootContainer.setAlignment(Pos.CENTER);
            rootContainer.setStyle("-fx-background-color: #1e1e1e; -fx-padding: 20;");
            
            // Card Container (The "Div" in the middle)
            VBox card = new VBox(20);
            card.setMaxWidth(600);
            card.setAlignment(Pos.CENTER);
            card.setStyle("-fx-background-color: #252526; -fx-background-radius: 10; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.5), 10, 0, 0, 5); -fx-padding: 30;");

            // --- Card Content ---
            
            // Top Section: Icon + Filename + Time
            HBox infoSection = new HBox(20);
            infoSection.setAlignment(Pos.CENTER_LEFT);
            
            // Icon
            javafx.scene.Node iconNode = FileIconFactory.createIcon(audioPath.getFileName().toString(), false);
            iconNode.setScaleX(4.0);
            iconNode.setScaleY(4.0);
            StackPane iconPane = new StackPane(iconNode);
            iconPane.setPrefSize(80, 80);
            iconPane.setAlignment(Pos.CENTER);
            
            // Text Info
            VBox textInfo = new VBox(5);
            textInfo.setAlignment(Pos.CENTER_LEFT);
            
            Label nameLabel = new Label(audioPath.getFileName().toString());
            nameLabel.setStyle("-fx-text-fill: white; -fx-font-size: 18px; -fx-font-weight: bold;");
            nameLabel.setWrapText(true);
            
            Label timeLabel = new Label("00:00 / 00:00");
            timeLabel.setStyle("-fx-text-fill: #aaaaaa; -fx-font-size: 14px; -fx-font-family: 'Consolas', monospace;");
            
            textInfo.getChildren().addAll(nameLabel, timeLabel);
            HBox.setHgrow(textInfo, Priority.ALWAYS);
            
            infoSection.getChildren().addAll(iconPane, textInfo);
            
            // Controls Section
            VBox controlsSection = new VBox(15);
            controlsSection.setAlignment(Pos.CENTER);
            
            // Progress Slider
            Slider progressSlider = new Slider();
            progressSlider.setMaxWidth(Double.MAX_VALUE);
            progressSlider.setStyle("-fx-cursor: hand;");
            
            // Buttons Row
            HBox buttonsRow = new HBox(20);
            buttonsRow.setAlignment(Pos.CENTER);
            
            Button playBtn = new Button("▶");
            playBtn.setStyle("-fx-background-color: transparent; -fx-text-fill: white; -fx-font-size: 32px; -fx-cursor: hand; -fx-padding: 0;");
            
            // Volume Control
            HBox volBox = new HBox(10);
            volBox.setAlignment(Pos.CENTER);
            Label volIcon = new Label("🔊");
            volIcon.setStyle("-fx-text-fill: #cccccc; -fx-font-size: 16px;");
            Slider volSlider = new Slider(0, 1, 1);
            volSlider.setPrefWidth(100);
            volBox.getChildren().addAll(volIcon, volSlider);
            
            buttonsRow.getChildren().addAll(playBtn, volBox);
            
            controlsSection.getChildren().addAll(progressSlider, buttonsRow);
            
            // Add all to Card
            card.getChildren().addAll(infoSection, new Separator(), controlsSection);
            
            // Add Card to Root
            rootContainer.getChildren().add(card);
            
            // Logic
            playBtn.setOnAction(e -> {
                MediaPlayer.Status status = mediaPlayer.getStatus();
                if (status == MediaPlayer.Status.UNKNOWN || status == MediaPlayer.Status.HALTED) {
                    return;
                }
                if (status == MediaPlayer.Status.PAUSED || status == MediaPlayer.Status.READY || status == MediaPlayer.Status.STOPPED) {
                    mediaPlayer.play();
                    playBtn.setText("⏸");
                } else {
                    mediaPlayer.pause();
                    playBtn.setText("▶");
                }
            });
            
            mediaPlayer.currentTimeProperty().addListener((obs, oldTime, newTime) -> {
                if (!progressSlider.isValueChanging()) {
                    progressSlider.setValue(newTime.toMillis() / media.getDuration().toMillis() * 100.0);
                }
                updateTimeLabel(timeLabel, newTime, media.getDuration());
            });
            
            progressSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                if (progressSlider.isValueChanging()) {
                    mediaPlayer.seek(media.getDuration().multiply(newVal.doubleValue() / 100.0));
                }
            });
            
            progressSlider.setOnMouseReleased(e -> {
                 mediaPlayer.seek(media.getDuration().multiply(progressSlider.getValue() / 100.0));
            });
            
            volSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                mediaPlayer.setVolume(newVal.doubleValue());
                if (newVal.doubleValue() == 0) {
                    volIcon.setText("🔇");
                } else if (newVal.doubleValue() < 0.5) {
                    volIcon.setText("🔉");
                } else {
                    volIcon.setText("🔊");
                }
            });
            
            mediaPlayer.setOnEndOfMedia(() -> {
                mediaPlayer.stop();
                playBtn.setText("▶");
                progressSlider.setValue(0);
            });
            
            mediaPlayer.setOnReady(() -> {
                 updateTimeLabel(timeLabel, mediaPlayer.getCurrentTime(), media.getDuration());
            });

            // Tab Setup
            Tab tab = new Tab(audioPath.getFileName().toString());
            setupTab(tab, audioPath);
            tab.setContent(rootContainer);
            
            // Cleanup on close
            tab.setOnClosed(e -> {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                tabFileMap.remove(tab);
                tabDirtyMap.remove(tab);
            });

            tabFileMap.put(tab, audioPath);
            editorTabs.getTabs().add(tab);
            editorTabs.getSelectionModel().select(tab);

            log("Audio abierto: " + audioPath.getFileName());

        } catch (Exception ex) {
            logger.error("Failed to open audio", ex);
            log("✗ Error al abrir audio: " + ex.getMessage());
        }
    }

    private void updateTimeLabel(Label label, Duration current, Duration total) {
        if (total == null || current == null) return;
        String currStr = formatDuration(current);
        String totalStr = formatDuration(total);
        label.setText(currStr + " / " + totalStr);
    }

    private String formatDuration(Duration duration) {
        if (duration == null) return "00:00";
        int seconds = (int) duration.toSeconds();
        int minutes = seconds / 60;
        seconds = seconds % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    private void openModelInEditor(Path modelPath) {
        try {
            // Create 3D Scene
            Group root3D = new Group();
            SubScene subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
            subScene.setFill(Color.rgb(30, 30, 30));
            
            // Add Grid/Floor
            root3D.getChildren().add(createGrid());
            
            PerspectiveCamera camera = new PerspectiveCamera(true);
            camera.setNearClip(0.1);
            camera.setFarClip(1000.0);
            camera.setTranslateZ(-50);
            subScene.setCamera(camera);

            // Attempt to load model
            boolean loaded = false;
            try {
                String content = Files.readString(modelPath);
                // Simple parser for Bedrock Geometry
                if (content.contains("minecraft:geometry")) {
                    loadBedrockGeometry(content, root3D);
                    loaded = true;
                }
            } catch (Exception e) {
                logger.error("Failed to parse model", e);
            }

            if (!loaded) {
                // Default Cube if parsing fails or unknown format
                Box box = new Box(10, 10, 10);
                box.setMaterial(new PhongMaterial(Color.LIGHTBLUE));
                root3D.getChildren().add(box);
            }

            // Mouse Control (Rotate)
            final Rotate rotateX = new Rotate(0, Rotate.X_AXIS);
            final Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
            root3D.getTransforms().addAll(rotateX, rotateY);

            // Wrapper to handle events
            StackPane container = new StackPane(subScene);
            container.setStyle("-fx-background-color: #1e1e1e;");
            
            // Resize logic
            subScene.widthProperty().bind(container.widthProperty());
            subScene.heightProperty().bind(container.heightProperty());

            // Interaction
            final double[] lastMouse = new double[2];
            container.setOnMousePressed(event -> {
                lastMouse[0] = event.getSceneX();
                lastMouse[1] = event.getSceneY();
            });
            
            container.setOnMouseDragged(event -> {
                double dx = event.getSceneX() - lastMouse[0];
                double dy = event.getSceneY() - lastMouse[1];
                
                if (event.isPrimaryButtonDown()) {
                    rotateY.setAngle(rotateY.getAngle() + dx * 0.5);
                    rotateX.setAngle(rotateX.getAngle() - dy * 0.5);
                }
                
                lastMouse[0] = event.getSceneX();
                lastMouse[1] = event.getSceneY();
            });
            
            // Zoom with scroll
            container.setOnScroll(event -> {
                double delta = event.getDeltaY();
                double z = camera.getTranslateZ();
                double newZ = z + delta * 0.1;
                camera.setTranslateZ(newZ);
            });

            // Tab Setup
            Tab tab = new Tab(modelPath.getFileName().toString());
            setupTab(tab, modelPath);
            tab.setContent(container);

            tabFileMap.put(tab, modelPath);
            editorTabs.getTabs().add(tab);
            editorTabs.getSelectionModel().select(tab);

            log("Modelo 3D abierto: " + modelPath.getFileName());

        } catch (Exception ex) {
            logger.error("Failed to open model", ex);
            log("✗ Error al abrir modelo: " + ex.getMessage());
        }
    }

    private void loadBedrockGeometry(String jsonContent, Group root) {
        try {
            JsonObject json = JsonParser.parseString(jsonContent).getAsJsonObject();
            JsonArray geometries = json.getAsJsonArray("minecraft:geometry");
            if (geometries != null) {
                for (JsonElement geo : geometries) {
                    JsonArray bones = geo.getAsJsonObject().getAsJsonArray("bones");
                    if (bones != null) {
                        for (JsonElement bone : bones) {
                            JsonObject boneObj = bone.getAsJsonObject();
                            JsonArray cubes = boneObj.getAsJsonArray("cubes");
                            if (cubes != null) {
                                for (JsonElement cube : cubes) {
                                    JsonObject cubeObj = cube.getAsJsonObject();
                                    JsonArray origin = cubeObj.getAsJsonArray("origin");
                                    JsonArray size = cubeObj.getAsJsonArray("size");
                                    
                                    if (origin != null && size != null) {
                                        double w = size.get(0).getAsDouble();
                                        double h = size.get(1).getAsDouble();
                                        double d = size.get(2).getAsDouble();
                                        
                                        double x = origin.get(0).getAsDouble();
                                        double y = origin.get(1).getAsDouble();
                                        double z = origin.get(2).getAsDouble();
                                        
                                        Box box = new Box(w, h, d);
                                        box.setMaterial(new PhongMaterial(Color.LIGHTGRAY));
                                        
                                        // Position adjustment (JavaFX center vs Bedrock corner)
                                        // Bedrock Y is up. JavaFX Y is down.
                                        
                                        box.setTranslateX(x + w/2);
                                        box.setTranslateY(-(y + h/2)); // Invert Y
                                        box.setTranslateZ(z + d/2);
                                        
                                        root.getChildren().add(box);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("Error parsing bedrock geometry", e);
        }
    }

    private Group createGrid() {
        Group grid = new Group();
        int size = 100;
        int spacing = 10;
        
        Color gridColor = Color.rgb(80, 80, 80);
        Color axisXColor = Color.RED;
        Color axisZColor = Color.BLUE;
        
        for (int i = -size; i <= size; i += spacing) {
            // Line parallel to X axis (at Z = i)
            Cylinder lineX = new Cylinder(0.05, size * 2);
            lineX.setMaterial(new PhongMaterial(i == 0 ? axisXColor : gridColor));
            lineX.setRotationAxis(Rotate.Z_AXIS);
            lineX.setRotate(90);
            lineX.setTranslateZ(i);
            
            // Line parallel to Z axis (at X = i)
            Cylinder lineZ = new Cylinder(0.05, size * 2);
            lineZ.setMaterial(new PhongMaterial(i == 0 ? axisZColor : gridColor));
            lineZ.setRotationAxis(Rotate.X_AXIS);
            lineZ.setRotate(90);
            lineZ.setTranslateX(i);
            
            grid.getChildren().addAll(lineX, lineZ);
        }
        
        return grid;
    }

    private void updateSaveButtonState() {
        Tab selectedTab = editorTabs.getSelectionModel().getSelectedItem();
        boolean shouldEnable = false;

        if (selectedTab != null) {
            Path filePath = tabFileMap.get(selectedTab);
            if (filePath != null) {
                String fileName = filePath.getFileName().toString().toLowerCase();
                boolean isImage = fileName.endsWith(".png") || fileName.endsWith(".jpg") || fileName.endsWith(".jpeg") || fileName.endsWith(".gif");
                
                // Only enable if not an image AND is dirty
                if (!isImage && tabDirtyMap.getOrDefault(selectedTab, false)) {
                    shouldEnable = true;
                }
            }
        }

        if (btnSave != null) btnSave.setDisable(!shouldEnable);
        menuSave.setDisable(!shouldEnable);
    }

    private void handleSave() {
        Tab selectedTab = editorTabs.getSelectionModel().getSelectedItem();
        if (selectedTab == null) {
            log("No hay archivo abierto para guardar");
            return;
        }

        Path filePath = tabFileMap.get(selectedTab);
        String content = "";
        javafx.scene.Node node = selectedTab.getContent();
        
        // Handle wrapped content (like Markdown split view)
        if (node instanceof Pane && node.getUserData() instanceof javafx.scene.Node) {
            node = (javafx.scene.Node) node.getUserData();
        }
        
        if (node instanceof WebView) {
            WebView webView = (WebView) node;
            Object result = webView.getEngine().executeScript("getContent()");
            content = (result != null) ? result.toString() : "";
        } else if (node instanceof TextArea) {
            content = ((TextArea) node).getText();
        } else {
            // Probably image or something else
            return;
        }

        try {
            Files.writeString(filePath, content);
            log("✓ Guardado: " + filePath.getFileName());
            
            // Mark as clean
            tabDirtyMap.put(selectedTab, false);
            if (selectedTab.getUserData() instanceof Runnable) {
                ((Runnable) selectedTab.getUserData()).run();
            }
            updateSaveButtonState();
            
        } catch (IOException e) {
            logger.error("Failed to save file", e);
            log("✗ Error al guardar: " + e.getMessage());
        }
    }

    private void handleSaveAll() {
        for (Tab tab : editorTabs.getTabs()) {
            Path filePath = tabFileMap.get(tab);
            String content = "";
            javafx.scene.Node node = tab.getContent();
            
            // Handle wrapped content (like Markdown split view)
            if (node instanceof Pane && node.getUserData() instanceof javafx.scene.Node) {
                node = (javafx.scene.Node) node.getUserData();
            }
            
            if (node instanceof WebView) {
                WebView webView = (WebView) node;
                Object result = webView.getEngine().executeScript("getContent()");
                content = (result != null) ? result.toString() : "";
            } else if (node instanceof TextArea) {
                content = ((TextArea) node).getText();
            } else {
                continue;
            }

            try {
                Files.writeString(filePath, content);
            } catch (IOException e) {
                logger.error("Failed to save file", e);
            }
        }
        log("✓ Todos los archivos guardados");
    }

    private void handleExport() {
        log("Exportando proyecto...");
        showInfo("Export", "Función de exportación en desarrollo");
    }

    private void handleTest() {
        log("Ejecutando pruebas...");
        showInfo("Test", "Función de pruebas en desarrollo");
    }

    private void handleSettings() {
        NavigationManager.getInstance().showSettingsModal();
    }

    private void handleClose() {
        logger.info("Closing project");
        NavigationManager.getInstance().showHomeScreen();
    }

    private void handleAddEntity() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Añadir Entidad");
        dialog.setHeaderText("Crear nueva entidad");
        dialog.setContentText("Nombre de la entidad:");

        dialog.showAndWait().ifPresent(entityName -> {
            if (entityName.trim().isEmpty()) {
                showError("Error", "El nombre no puede estar vacío");
                return;
            }

            try {
                ensureBaseStructure();
                ProjectGenerator.createEntityFolder(Paths.get(currentProject.getRootPath()));

                currentProject.addEntity(entityName);
                projectManager.updateProject(currentProject);

                refreshFileTree();
                log("✓ Entidad añadida: " + entityName);

            } catch (Exception e) {
                logger.error("Failed to add entity", e);
                log("✗ Error: " + e.getMessage());
            }
        });
    }

    private void handleAddItem() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Añadir Item");
        dialog.setHeaderText("Crear nuevo item");
        dialog.setContentText("Nombre del item:");

        dialog.showAndWait().ifPresent(itemName -> {
            if (itemName.trim().isEmpty()) {
                showError("Error", "El nombre no puede estar vacío");
                return;
            }

            try {
                ensureBaseStructure();
                ProjectGenerator.createItemFolder(Paths.get(currentProject.getRootPath()));

                currentProject.addItem(itemName);
                projectManager.updateProject(currentProject);

                refreshFileTree();
                log("✓ Item añadido: " + itemName);

            } catch (Exception e) {
                logger.error("Failed to add item", e);
                log("✗ Error: " + e.getMessage());
            }
        });
    }

    private void handleAddBlock() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Añadir Bloque");
        dialog.setHeaderText("Crear nuevo bloque");
        dialog.setContentText("Nombre del bloque:");

        dialog.showAndWait().ifPresent(blockName -> {
            if (blockName.trim().isEmpty()) {
                showError("Error", "El nombre no puede estar vacío");
                return;
            }

            try {
                ensureBaseStructure();
                ProjectGenerator.createBlockFolder(Paths.get(currentProject.getRootPath()));

                currentProject.addBlock(blockName);
                projectManager.updateProject(currentProject);

                refreshFileTree();
                log("✓ Bloque añadido: " + blockName);

            } catch (Exception e) {
                logger.error("Failed to add block", e);
                log("✗ Error: " + e.getMessage());
            }
        });
    }

    private void ensureBaseStructure() throws IOException {
        if (!currentProject.isManifestGenerated()) {
            log("Generando estructura base...");
            ProjectGenerator.generateBaseStructure(
                    Paths.get(currentProject.getRootPath()),
                    currentProject.getName(),
                    currentProject.getDescription());
            currentProject.setManifestGenerated(true);
            projectManager.updateProject(currentProject);
            log("✓ Estructura base creada");
        }
    }

    private void toggleConsole() {
        consoleVisible = !consoleVisible;
        // Don't set visible(false) to keep the layout, just minimize height
        if (consoleVisible) {
            consoleContainer.setPrefHeight(Region.USE_COMPUTED_SIZE);
            consoleContainer.setMinHeight(150); // Default height
            consoleContainer.setMaxHeight(Double.MAX_VALUE);
            bottomTabPane.setVisible(true);
        } else {
            consoleContainer.setPrefHeight(30); // Minimal height for header/button
            consoleContainer.setMinHeight(30);
            consoleContainer.setMaxHeight(30);
            bottomTabPane.setVisible(false);
        }
        
        // Ensure button is always visible by not hiding the container
        // consoleContainer.setVisible(true); // Always true now
        // consoleContainer.setManaged(true); // Always true now
        
        // Update menu check
        menuToggleConsole.setSelected(consoleVisible);
    }

    private void moveSidebarToLeft() {
        try {
            // Fallback for sidebarContainer if null
            if (sidebarContainer == null && projectExplorerView != null && projectExplorerView.getParent() instanceof StackPane) {
                sidebarContainer = (StackPane) projectExplorerView.getParent();
                logger.info("Recovered sidebarContainer from projectExplorerView parent");
            }
            
            // Fallback for mainLayout if null
            if (mainLayout == null && btnBack != null && btnBack.getScene() != null && btnBack.getScene().getRoot() instanceof BorderPane) {
                mainLayout = (BorderPane) btnBack.getScene().getRoot();
                logger.info("Recovered mainLayout from scene root");
            }

            // Fallback for activityBar if null
            if (activityBar == null && mainLayout != null) {
                // Try to find it in Right or Left
                if (mainLayout.getRight() instanceof VBox) {
                    activityBar = (VBox) mainLayout.getRight();
                    logger.info("Recovered activityBar from mainLayout.right");
                } else if (mainLayout.getLeft() instanceof VBox) {
                    activityBar = (VBox) mainLayout.getLeft();
                    logger.info("Recovered activityBar from mainLayout.left");
                }
            }

            if (ideSplitPane == null || sidebarContainer == null) {
                logger.error("Components not initialized: splitPane={}, sidebar={}", ideSplitPane, sidebarContainer);
                return;
            }
            
            javafx.application.Platform.runLater(() -> {
                try {
                    // Move Activity Bar to Left
                    if (mainLayout != null && activityBar != null) {
                        logger.info("Moving activityBar to LEFT");
                        mainLayout.setRight(null);
                        mainLayout.setLeft(activityBar);
                    } else {
                        logger.warn("Could not move activityBar: mainLayout={}, activityBar={}", mainLayout, activityBar);
                    }

                    // Check if already on left (index 0)
                    if (ideSplitPane.getItems().indexOf(sidebarContainer) == 0) {
                        ideSplitPane.setDividerPositions(0.2);
                        return;
                    }

                    // Safe reordering using a new list to avoid "Child already exists" issues
                    List<Node> newItems = new ArrayList<>(ideSplitPane.getItems());
                    newItems.remove(sidebarContainer);
                    newItems.add(0, sidebarContainer);
                    
                    ideSplitPane.getItems().setAll(newItems);
                    ideSplitPane.setDividerPositions(0.2);
                } catch (Exception ex) {
                    logger.error("Error setting divider position or moving components", ex);
                }
            });
        } catch (Exception e) {
            logger.error("Error moving sidebar to left", e);
        }
    }

    private void moveSidebarToRight() {
        try {
            // Fallback for sidebarContainer if null
            if (sidebarContainer == null && projectExplorerView != null && projectExplorerView.getParent() instanceof StackPane) {
                sidebarContainer = (StackPane) projectExplorerView.getParent();
                logger.info("Recovered sidebarContainer from projectExplorerView parent");
            }
            
            // Fallback for mainLayout if null
            if (mainLayout == null && btnBack != null && btnBack.getScene() != null && btnBack.getScene().getRoot() instanceof BorderPane) {
                mainLayout = (BorderPane) btnBack.getScene().getRoot();
                logger.info("Recovered mainLayout from scene root");
            }

            // Fallback for activityBar if null
            if (activityBar == null && mainLayout != null) {
                // Try to find it in Left or Right
                if (mainLayout.getLeft() instanceof VBox) {
                    activityBar = (VBox) mainLayout.getLeft();
                    logger.info("Recovered activityBar from mainLayout.left");
                } else if (mainLayout.getRight() instanceof VBox) {
                    activityBar = (VBox) mainLayout.getRight();
                    logger.info("Recovered activityBar from mainLayout.right");
                }
            }

            if (ideSplitPane == null || sidebarContainer == null) {
                logger.error("Components not initialized: splitPane={}, sidebar={}", ideSplitPane, sidebarContainer);
                return;
            }

            javafx.application.Platform.runLater(() -> {
                try {
                    // Move Activity Bar to Right
                    if (mainLayout != null && activityBar != null) {
                        logger.info("Moving activityBar to RIGHT");
                        mainLayout.setLeft(null);
                        mainLayout.setRight(activityBar);
                    } else {
                        logger.warn("Could not move activityBar: mainLayout={}, activityBar={}", mainLayout, activityBar);
                    }

                    // Check if already on right (last index)
                    if (ideSplitPane.getItems().indexOf(sidebarContainer) == ideSplitPane.getItems().size() - 1) {
                        ideSplitPane.setDividerPositions(0.8);
                        return;
                    }

                    // Safe reordering using a new list to avoid "Child already exists" issues
                    List<Node> newItems = new ArrayList<>(ideSplitPane.getItems());
                    newItems.remove(sidebarContainer);
                    newItems.add(sidebarContainer); // Adds to end
                    
                    ideSplitPane.getItems().setAll(newItems);
                    ideSplitPane.setDividerPositions(0.8);
                } catch (Exception ex) {
                    logger.error("Error setting divider position or moving components", ex);
                }
            });
        } catch (Exception e) {
            logger.error("Error moving sidebar to right", e);
        }
    }

    private void showAbout() {
        showInfo("About", "Addon Creator v1.0.0\nCreated by Agustín Benítez");
    }

    private void showDocs() {
        log("Opening documentation...");
    }

    private void showLicenses() {
        StringBuilder content = new StringBuilder();
        
        // Project License
        try (java.io.InputStream is = getClass().getResourceAsStream("/LICENSE")) {
            if (is != null) {
                content.append("=== ADDON CREATOR LICENSE ===\n\n");
                content.append(new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
                content.append("\n\n");
            } else {
                logger.warn("Project license file not found: /LICENSE");
            }
        } catch (Exception e) {
            logger.error("Failed to load project license", e);
        }

        // Monaco License
        try (java.io.InputStream is = getClass().getResourceAsStream("/monaco-editor/LICENSE.txt")) {
            if (is != null) {
                content.append("=== MONACO EDITOR LICENSE ===\n\n");
                content.append(new String(is.readAllBytes(), java.nio.charset.StandardCharsets.UTF_8));
            } else {
                logger.warn("Monaco license file not found: /monaco-editor/LICENSE.txt");
            }
        } catch (Exception e) {
            logger.error("Failed to load Monaco license", e);
        }

        if (content.length() == 0) {
            showError("Error", "No se encontraron archivos de licencia.");
            return;
        }

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Licencias");
        alert.setHeaderText("Licencias del Proyecto y Terceros");
        
        TextArea textArea = new TextArea(content.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setPrefWidth(600);
        textArea.setPrefHeight(400);
        
        alert.getDialogPane().setContent(textArea);
        alert.setResizable(true);
        alert.showAndWait();
    }

    private void setupLanguageStatus() {
        // Listener for tab selection
        editorTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateLanguageLabel(newTab);
        });
        
        // Initial update
        updateLanguageLabel(editorTabs.getSelectionModel().getSelectedItem());
        
        // Click listener for changing language
        if (languageStatusLabel != null) {
            languageStatusLabel.setOnMouseClicked(e -> showLanguageSelector(e));
        }
    }

    private void updateLanguageLabel(Tab tab) {
        if (languageStatusLabel == null) return;
        
        if (tab != null && tabFileMap.containsKey(tab)) {
            Path path = tabFileMap.get(tab);
            String lang = getMonacoLanguage(path.getFileName().toString());
            languageStatusLabel.setText(lang.toUpperCase());
        } else {
            languageStatusLabel.setText("PLAIN TEXT");
        }
    }

    private void showLanguageSelector(javafx.scene.input.MouseEvent e) {
        Tab selectedTab = editorTabs.getSelectionModel().getSelectedItem();
        if (selectedTab == null || !tabFileMap.containsKey(selectedTab)) return;

        ContextMenu menu = new ContextMenu();
        String[] languages = {"json", "javascript", "typescript", "html", "css", "java", "xml", "markdown", "plaintext"};
        
        for (String lang : languages) {
            MenuItem item = new MenuItem(lang.toUpperCase());
            item.setOnAction(event -> {
                changeEditorLanguage(selectedTab, lang);
                languageStatusLabel.setText(lang.toUpperCase());
            });
            menu.getItems().add(item);
        }
        
        menu.show(languageStatusLabel, javafx.geometry.Side.TOP, 0, 0);
    }

    private void changeEditorLanguage(Tab tab, String language) {
        if (tab.getContent() instanceof WebView) {
            WebView webView = (WebView) tab.getContent();
            javafx.scene.web.WebEngine webEngine = webView.getEngine();
            webEngine.executeScript("if(typeof setLanguage === 'function') { setLanguage('" + language + "'); }");
        }
    }

    private void setupErrorStatus() {
        updateErrorLabel();
        if (errorStatusLabel != null) {
            errorStatusLabel.setOnMouseClicked(e -> openConsole());
        }
    }

    private void updateErrorLabel() {
        if (errorStatusLabel != null) {
            javafx.application.Platform.runLater(() -> {
                errorStatusLabel.setText(errorCount + " Errors");
                if (errorCount > 0) {
                     // Keep style but maybe change color if errors > 0?
                     // The FXML defines default style.
                     // Let's just update text. The icon is red.
                }
            });
        }
    }

    private void openConsole() {
        if (!consoleVisible) {
            toggleConsole();
        }
        if (bottomTabPane != null) {
            if ("terminal".equals(lastErrorSource)) {
                bottomTabPane.getSelectionModel().select(1);
            } else {
                bottomTabPane.getSelectionModel().select(0);
            }
        }
    }

    private void log(String message) {
        consoleOutput.appendText(message + "\n");
        logger.info(message);
        
        if (message != null && (
            message.toLowerCase().contains("error") || 
            message.toLowerCase().contains("exception") || 
            message.toLowerCase().contains("fail"))) {
            errorCount++;
            lastErrorSource = "log";
            updateErrorLabel();
        }
    }

    private void showError(String title, String message) {
        errorCount++;
        lastErrorSource = "log";
        updateErrorLabel();
        
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void showInfo(String title, String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String getMonacoLanguage(String filename) {
        String lower = filename.toLowerCase();
        if (lower.endsWith(".json")) return "json";
        if (lower.endsWith(".js")) return "javascript";
        if (lower.endsWith(".ts")) return "typescript";
        if (lower.endsWith(".html")) return "html";
        if (lower.endsWith(".css")) return "css";
        if (lower.endsWith(".java")) return "java";
        if (lower.endsWith(".xml")) return "xml";
        if (lower.endsWith(".md")) return "markdown";
        return "plaintext";
    }

    private void enableWebViewCopyPaste(WebView webView) {
        // Setup Context Menu
        setupWebViewContextMenu(webView);

        // Setup Keyboard Shortcuts (Ctrl+C, Ctrl+V, Ctrl+X)
        webView.addEventFilter(javafx.scene.input.KeyEvent.KEY_PRESSED, event -> {
            if (event.isShortcutDown()) {
                if (event.getCode() == KeyCode.C) {
                    performCopy(webView);
                    event.consume();
                } else if (event.getCode() == KeyCode.V) {
                    performPaste(webView);
                    event.consume();
                } else if (event.getCode() == KeyCode.X) {
                    performCut(webView);
                    event.consume();
                }
            }
        });
    }

    private void setupWebViewContextMenu(WebView webView) {
        ContextMenu contextMenu = new ContextMenu();

        MenuItem cutItem = new MenuItem("Cortar"); // Cut
        cutItem.setOnAction(e -> performCut(webView));
        cutItem.setAccelerator(new KeyCodeCombination(KeyCode.X, KeyCombination.CONTROL_DOWN));

        MenuItem copyItem = new MenuItem("Copiar"); // Copy
        copyItem.setOnAction(e -> performCopy(webView));
        copyItem.setAccelerator(new KeyCodeCombination(KeyCode.C, KeyCombination.CONTROL_DOWN));

        MenuItem pasteItem = new MenuItem("Pegar"); // Paste
        pasteItem.setOnAction(e -> performPaste(webView));
        pasteItem.setAccelerator(new KeyCodeCombination(KeyCode.V, KeyCombination.CONTROL_DOWN));

        contextMenu.getItems().addAll(cutItem, copyItem, new SeparatorMenuItem(), pasteItem);
        
        // Attach context menu to WebView
        webView.setOnContextMenuRequested(e -> {
            contextMenu.show(webView, e.getScreenX(), e.getScreenY());
        });
    }

    private void performCopy(WebView webView) {
        webView.getEngine().executeScript(
            "if(typeof editor !== 'undefined' && editor.getModel()) { " +
            "   var selection = editor.getModel().getValueInRange(editor.getSelection()); " +
            "   if(selection) { javaApp.copyToClipboard(selection); } " +
            "}"
        );
    }

    private void performCut(WebView webView) {
        webView.getEngine().executeScript(
            "if(typeof editor !== 'undefined' && editor.getModel()) { " +
            "   var selection = editor.getModel().getValueInRange(editor.getSelection()); " +
            "   if(selection) { " +
            "       javaApp.copyToClipboard(selection); " +
            "       editor.executeEdits('source', [{range: editor.getSelection(), text: ''}]); " +
            "   } " +
            "}"
        );
    }

    private void performPaste(WebView webView) {
        Clipboard clipboard = Clipboard.getSystemClipboard();
        if (clipboard.hasString()) {
            String text = clipboard.getString();
            if (text != null) {
                // Use Gson to safely escape string for JS
                String jsonText = new Gson().toJson(text);
                webView.getEngine().executeScript(
                    "if(typeof editor !== 'undefined') { " +
                    "   var selection = editor.getSelection();" +
                    "   var op = {range: selection, text: " + jsonText + ", forceMoveMarkers: true};" +
                    "   editor.executeEdits('source', [op]);" +
                    "}"
                );
            }
        }
    }

    public class JavaBridge {
        private String content;
        private java.util.function.Consumer<String> onContentChangeCallback;

        public JavaBridge(String content) {
            this.content = content;
        }

        public void copyToClipboard(String text) {
            javafx.application.Platform.runLater(() -> {
                ClipboardContent content = new ClipboardContent();
                content.putString(text);
                Clipboard.getSystemClipboard().setContent(content);
            });
        }

        public void setOnContentChangeCallback(java.util.function.Consumer<String> callback) {
            this.onContentChangeCallback = callback;
        }

        public String getContent() {
            return content;
        }
        
        public void log(String msg) {
            EditorController.this.log(msg);
        }

        public void onMarkdownChange(String newContent) {
            if (onContentChangeCallback != null) {
                onContentChangeCallback.accept(newContent);
            }
            onContentChange();
        }

        public void onContentChange() {
            javafx.application.Platform.runLater(() -> {
                Tab selectedTab = editorTabs.getSelectionModel().getSelectedItem();
                if (selectedTab != null) {
                    if (autoSaveEnabled) {
                        handleSave();
                    } else {
                        // Mark as dirty
                        if (!tabDirtyMap.getOrDefault(selectedTab, false)) {
                            tabDirtyMap.put(selectedTab, true);
                            
                            // Update UI
                            if (selectedTab.getUserData() instanceof Runnable) {
                                ((Runnable) selectedTab.getUserData()).run();
                            }
                            updateSaveButtonState();
                        }
                    }
                }
            });
        }
    }

    private void setupUserProfile() {
        if (userProfileIcon == null) return;
        
        // Target size: 18x18 pixels (Radius 9)
        // Center: (9, 9)
        
        // Background Circle
        Circle bg = new Circle(9, 9, 9);
        bg.setFill(Color.web("#5E5E5E"));
        
        // Head: Center at (9, 7.5), Radius 3.15
        Circle head = new Circle(9, 7.5, 3.15);
        head.setFill(Color.web("#C4C4C4"));
        
        // Body: Center at (9, 17.55), Radius 6.75
        Circle body = new Circle(9, 17.55, 6.75);
        body.setFill(Color.web("#C4C4C4"));
        
        // Clip group for the body/head inside the main circle
        Group content = new Group(head, body);
        Circle clip = new Circle(9, 9, 9);
        content.setClip(clip);
        
        userProfileIcon.getChildren().clear();
        userProfileIcon.getChildren().addAll(bg, content);
        
        if (btnUserProfile != null) {
            btnUserProfile.setOnAction(e -> handleUserProfile());
            
            // Logout Context Menu
            ContextMenu contextMenu = new ContextMenu();
            MenuItem logoutItem = new MenuItem("Cerrar sesión");
            logoutItem.setOnAction(e -> handleLogout());
            contextMenu.getItems().add(logoutItem);
            
            btnUserProfile.setContextMenu(contextMenu);
        }

        // Check for saved credentials
        String savedUser = SettingsManager.getInstance().getGitUser();
        if (savedUser != null && !savedUser.isEmpty()) {
            updateUserAvatar(savedUser);
        }
    }

    private void handleUserProfile() {
        // Prevent login dialog if already logged in
        if (SettingsManager.getInstance().getGitUser() != null && !SettingsManager.getInstance().getGitUser().isEmpty()) {
            return;
        }

        LoginDialogHelper.showLoginDialog((user, token) -> {
            SettingsManager.getInstance().setGitCredentials(user, token);
            updateUserAvatar(user);
            log("Logged in as " + user);
        });
    }

    private void handleLogout() {
        SettingsManager.getInstance().clearGitCredentials();
        resetUserProfileButton();
        log("Sesión cerrada");
        
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sesión Cerrada");
        alert.setHeaderText(null);
        alert.setContentText("Has cerrado sesión correctamente.");
        alert.showAndWait();
    }

    private void resetUserProfileButton() {
        // Restore default icon
        SVGPath defaultIcon = new SVGPath();
        defaultIcon.setContent("M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 3c1.66 0 3 1.34 3 3s-1.34 3-3 3-3-1.34-3-3 1.34-3 3-3zm0 14.2c-2.5 0-4.71-1.28-6-3.22.03-1.99 4-3.08 6-3.08 1.99 0 5.97 1.09 6 3.08-1.29 1.94-3.5 3.22-6 3.22z");
        defaultIcon.setFill(Color.WHITE);
        defaultIcon.setScaleX(1.0);
        defaultIcon.setScaleY(1.0);
        
        userProfileIcon.getChildren().setAll(defaultIcon);
    }

    private void updateUserAvatar(String user) {
        try {
            String avatarUrl = "https://github.com/" + user + ".png";
            Image image = new Image(avatarUrl, true);
            
            image.progressProperty().addListener((obs, oldV, newV) -> {
                if (newV.doubleValue() == 1.0 && !image.isError()) {
                    javafx.application.Platform.runLater(() -> {
                         // Update with avatar, keeping size 18x18
                         Circle avatar = new Circle(9, 9, 9);
                         avatar.setFill(new ImagePattern(image));
                         userProfileIcon.getChildren().setAll(avatar);
                    });
                }
            });
        } catch (Exception e) {
            logger.error("Failed to load avatar", e);
        }
    }

    private static class FileIconFactory {
        // SVG Paths
        private static final String FOLDER_PATH = "M10 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V8C22 6.9 21.1 6 20 6H12L10 4Z";
        private static final String FILE_BODY = "M14 2H6C4.89 2 4 2.9 4 4V20C4 21.1 4.89 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2Z";
        private static final String FILE_CORNER = "M14 2V8H20";
        private static final String TODO_PATH = "M19 3h-4.18C14.4 1.84 13.3 1 12 1c-1.3 0-2.4.84-2.82 2H5c-1.1 0-2 .9-2 2v14c0 1.1.9 2 2 2h14c1.1 0 2-.9 2-2V5c0-1.1-.9-2-2-2zm-7 0c.55 0 1 .45 1 1s-.45 1-1 1-1-.45-1-1 .45-1 1-1zm-2 14l-4-4 1.41-1.41L10 14.17l6.59-6.59L18 9l-8 8z";
        
        // Audio Icon Paths
        private static final String AUDIO_BODY = "M14 2H6C4.89 2 4 2.9 4 4V20C4 21.1 4.89 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2Z";
        private static final String AUDIO_CORNER = "M14 2V8H20";

        // 3D Model Icon Paths (Deprecated/Replaced)
        // private static final String MODEL_PATH_1 = "M12 2L2 7l10 5 10-5-10-5z";
        // ...
        
        public static javafx.scene.Node createIcon(String filename, boolean isDir) {
            String nameLower = filename.toLowerCase();
            
            // Special check for .TODO folder
            if (isDir && nameLower.equals(".todo")) {
                SVGPath todoIcon = new SVGPath();
                todoIcon.setContent(TODO_PATH);
                todoIcon.setFill(Color.web("#FFC107")); // Keep folder color convention but with Todo shape
                return createScaledIcon(todoIcon);
            }

            if (isDir) {
                SVGPath folder = new SVGPath();
                folder.setContent(FOLDER_PATH);
                folder.setFill(Color.web("#FFC107")); // Amber
                return createScaledIcon(folder);
            }
            
            // Special check for tasks.json
            if (nameLower.equals("tasks.json")) {
                 SVGPath todoIcon = new SVGPath();
                 todoIcon.setContent(TODO_PATH);
                 todoIcon.setFill(Color.web("#90A4AE")); // Match default file color or distinct
                 return createScaledIcon(todoIcon);
            }
            
            String ext = getExtension(filename);
            
            // Audio Files
            if (nameLower.endsWith(".mp3") || nameLower.endsWith(".ogg") || nameLower.endsWith(".wav")) {
                SVGPath body = new SVGPath();
                body.setContent(AUDIO_BODY);
                body.setFill(Color.web("#4F46E5"));
                
                SVGPath corner = new SVGPath();
                corner.setContent(AUDIO_CORNER);
                corner.setFill(Color.web("#3730A3"));
                
                Text note = new Text("♫");
                note.setFill(Color.WHITE);
                note.setFont(Font.font("Arial", FontWeight.BOLD, 10));
                note.setX(6);
                note.setY(18);
                
                Group group = new Group(body, corner, note);
                return createScaledIcon(group);
            }

            // Check specific filenames first
            if (nameLower.startsWith("readme")) {
                // Info Icon
                Group g = new Group();
                
                Circle circle = new Circle(12, 12, 10);
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(Color.web("#007ACC"));
                circle.setStrokeWidth(2);
                
                Line l1 = new Line(12, 16, 12, 12);
                l1.setStroke(Color.web("#007ACC"));
                l1.setStrokeWidth(2);
                l1.setStrokeLineCap(StrokeLineCap.ROUND);
                
                Line l2 = new Line(12, 8, 12.01, 8);
                l2.setStroke(Color.web("#007ACC"));
                l2.setStrokeWidth(2);
                l2.setStrokeLineCap(StrokeLineCap.ROUND);
                
                g.getChildren().addAll(circle, l1, l2);
                return createScaledIcon(g);
            }
            
            if (nameLower.endsWith(".lang")) {
                // Globe Icon
                Group g = new Group();
                
                Circle circle = new Circle(12, 12, 10);
                circle.setFill(Color.TRANSPARENT);
                circle.setStroke(Color.web("#4CAF50"));
                circle.setStrokeWidth(2);
                
                Line equator = new Line(2, 12, 22, 12);
                equator.setStroke(Color.web("#4CAF50"));
                equator.setStrokeWidth(2);
                equator.setStrokeLineCap(StrokeLineCap.ROUND);
                
                SVGPath meridians = new SVGPath();
                meridians.setContent("M12 2a15.3 15.3 0 0 1 4 10 15.3 15.3 0 0 1-4 10 15.3 15.3 0 0 1-4-10 15.3 15.3 0 0 1 4-10z");
                meridians.setFill(Color.TRANSPARENT);
                meridians.setStroke(Color.web("#4CAF50"));
                meridians.setStrokeWidth(2);
                meridians.setStrokeLineCap(StrokeLineCap.ROUND);
                meridians.setStrokeLineJoin(StrokeLineJoin.ROUND);

                g.getChildren().addAll(circle, equator, meridians);
                return createScaledIcon(g);
            }

            if (nameLower.contains("license") || nameLower.contains("licencia")) {
                // License Icon
                Group g = new Group();
                
                SVGPath body = new SVGPath();
                body.setContent("M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z");
                body.setFill(Color.TRANSPARENT);
                body.setStroke(Color.web("#FFB300"));
                body.setStrokeWidth(2);
                body.setStrokeLineCap(StrokeLineCap.ROUND);
                body.setStrokeLineJoin(StrokeLineJoin.ROUND);
                
                Polyline corner = new Polyline(14, 2, 14, 8, 20, 8);
                corner.setStroke(Color.web("#FFB300"));
                corner.setStrokeWidth(2);
                corner.setStrokeLineCap(StrokeLineCap.ROUND);
                corner.setStrokeLineJoin(StrokeLineJoin.ROUND);
                
                Circle seal = new Circle(10, 15, 2);
                seal.setFill(Color.TRANSPARENT);
                seal.setStroke(Color.web("#FFB300"));
                seal.setStrokeWidth(2);
                
                Line ribbon = new Line(11.5, 16.5, 13, 18);
                ribbon.setStroke(Color.web("#FFB300"));
                ribbon.setStrokeWidth(2);
                ribbon.setStrokeLineCap(StrokeLineCap.ROUND);

                g.getChildren().addAll(body, corner, seal, ribbon);
                return createScaledIcon(g);
            }
            
            if (nameLower.startsWith(".git") || nameLower.equals("git")) {
                // Git Icon
                Group g = new Group();
                
                Circle c1 = new Circle(18, 6, 3);
                c1.setFill(Color.TRANSPARENT);
                c1.setStroke(Color.WHITE);
                c1.setStrokeWidth(2);

                Circle c2 = new Circle(6, 18, 3);
                c2.setFill(Color.TRANSPARENT);
                c2.setStroke(Color.WHITE);
                c2.setStrokeWidth(2);

                SVGPath path = new SVGPath();
                path.setContent("M18 9v2c0 3.314-2.686 6-6 6h-3");
                path.setFill(Color.TRANSPARENT);
                path.setStroke(Color.WHITE);
                path.setStrokeWidth(2);
                path.setStrokeLineCap(StrokeLineCap.ROUND);
                path.setStrokeLineJoin(StrokeLineJoin.ROUND);

                g.getChildren().addAll(c1, c2, path);
                return createScaledIcon(g);
            }
            
            if (nameLower.endsWith(".mcaddon") || nameLower.endsWith(".mcpack")) {
                return createMcPackIcon();
            }

            if (nameLower.endsWith(".bbmodel") || nameLower.endsWith(".geo.json")) {
                Group g = new Group();
                
                // Cara superior
                SVGPath top = new SVGPath();
                top.setContent("M12 2 L22 7 L12 12 L2 7 Z");
                top.setFill(Color.web("#3B82F6"));
                
                // Cara izquierda
                SVGPath left = new SVGPath();
                left.setContent("M2 7 L12 12 L12 22 L2 17 Z");
                left.setFill(Color.web("#2563EB"));
                
                // Cara derecha
                SVGPath right = new SVGPath();
                right.setContent("M22 7 L12 12 L12 22 L22 17 Z");
                right.setFill(Color.web("#60A5FA"));

                g.getChildren().addAll(top, left, right);
                
                return createScaledIcon(g);
            }

            Group group = new Group();
            
            // Base
            SVGPath body = new SVGPath();
            body.setContent(FILE_BODY);
            
            SVGPath corner = new SVGPath();
            corner.setContent(FILE_CORNER);
            
            // Default Colors (Text)
            Color bodyColor = Color.web("#90A4AE");
            Color cornerColor = Color.web("#78909C");
            
            Group extra = new Group();
            
            switch (ext) {
                case "js":
                    bodyColor = Color.web("#F7DF1E");
                    cornerColor = Color.web("#D1BC19");
                    
                    Text jsText = new Text(7, 18, "JS");
                    jsText.setFont(Font.font("Arial", FontWeight.BOLD, 6));
                    jsText.setFill(Color.web("#323330"));
                    extra.getChildren().add(jsText);
                    break;
                    
                case "json":
                    bodyColor = Color.web("#292D3E");
                    cornerColor = Color.web("#1A1C25");
                    
                    Text jsonText = new Text(6, 17, "{ }");
                    jsonText.setFont(Font.font("Monospaced", FontWeight.BOLD, 7));
                    jsonText.setFill(Color.web("#82AAFF"));
                    extra.getChildren().add(jsonText);
                    break;
                    
                case "png": case "jpg": case "jpeg": case "webp": case "gif":
                    bodyColor = Color.web("#4CAF50");
                    cornerColor = Color.web("#388E3C");
                    
                    Circle circle = new Circle(8.5, 11.5, 1.5);
                    circle.setFill(Color.WHITE);
                    
                    SVGPath mountains = new SVGPath();
                    mountains.setContent("M6 19L9 15L11 17L14 13L18 19H6Z");
                    mountains.setFill(Color.WHITE);
                    
                    extra.getChildren().addAll(circle, mountains);
                    break;
                    
                default: // TXT or others
                    // Lines
                    Line l1 = new Line(8, 12, 16, 12); l1.setStroke(Color.WHITE); l1.setStrokeWidth(1.5);
                    Line l2 = new Line(8, 15, 16, 15); l2.setStroke(Color.WHITE); l2.setStrokeWidth(1.5);
                    Line l3 = new Line(8, 18, 13, 18); l3.setStroke(Color.WHITE); l3.setStrokeWidth(1.5);
                    extra.getChildren().addAll(l1, l2, l3);
                    break;
            }
            
            body.setFill(bodyColor);
            corner.setFill(cornerColor);
            
            group.getChildren().addAll(body, corner, extra);
            
            return createScaledIcon(group);
        }
        
        private static javafx.scene.Node createMcPackIcon() {
            try {
                javafx.scene.image.Image img = new javafx.scene.image.Image(EditorController.class.getResourceAsStream("/images/mcfiles.png"));
                javafx.scene.image.ImageView iv = new javafx.scene.image.ImageView(img);
                iv.setFitWidth(18);
                iv.setFitHeight(18);
                iv.setPreserveRatio(true);
                
                StackPane p = new StackPane(iv);
                p.setPrefSize(18, 18);
                p.setMinSize(18, 18);
                p.setMaxSize(18, 18);
                return p;
            } catch (Exception e) {
                // Fallback if image not found
                SVGPath folder = new SVGPath();
                folder.setContent(FOLDER_PATH);
                folder.setFill(Color.web("#8B4513"));
                return createScaledIcon(folder);
            }
        }

        private static javafx.scene.Node createScaledIcon(javafx.scene.Node node) {
            Group g = new Group(node);
            g.setScaleX(0.75); 
            g.setScaleY(0.75);
            // Wrap in StackPane to enforce size
            StackPane p = new StackPane(g);
            p.setPrefSize(18, 18);
            p.setMinSize(18, 18);
            p.setMaxSize(18, 18);
            return p;
        }
        
        private static String getExtension(String filename) {
            int i = filename.lastIndexOf('.');
            if (i > 0) {
                return filename.substring(i + 1).toLowerCase();
            }
            return "";
        }
    }
}
