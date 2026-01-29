package com.agustinbenitez.addoncreator.ui;

import com.agustinbenitez.addoncreator.core.FileTreeManager;
import com.agustinbenitez.addoncreator.core.ProjectGenerator;
import com.agustinbenitez.addoncreator.core.ProjectManager;
import com.agustinbenitez.addoncreator.models.Project;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.Dragboard;
import javafx.scene.input.TransferMode;
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
import java.util.Map;
import java.util.Optional;

import javafx.animation.PauseTransition;
import javafx.util.Duration;

import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.io.UncheckedIOException;

/**
 * Controller for the IDE-style editor screen
 * 
 * @author Agustín Benítez
 */
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
    private CheckMenuItem menuToggleFileTree;
    @FXML
    private MenuItem menuAbout;
    @FXML
    private MenuItem menuDocs;
    @FXML
    private MenuItem menuLicenses;

    // Toolbar
    @FXML
    private Button btnBack;
    @FXML
    private Button btnExplorer;
    @FXML
    private Button btnSearch;
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
    private VBox projectExplorerView;
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

    private Project currentProject;
    private ProjectManager projectManager;
    private Map<Tab, Path> tabFileMap; // Map tabs to their file paths
    private Map<Tab, Boolean> tabDirtyMap; // Map tabs to their dirty state
    private boolean consoleVisible = true;
    private boolean autoSaveEnabled = false;
    private PauseTransition searchDebounce;

    @FXML
    public void initialize() {
        logger.info("Initializing IDE EditorController");

        projectManager = new ProjectManager();
        tabFileMap = new HashMap<>();
        tabDirtyMap = new HashMap<>();

        setupMenuActions();
        setupToolbarActions();
        setupFileTree();
        setupConsole();
        setupAddElementButton();
        setupNewFileButton();
        setupSearch();
        
        // Tab selection listener to update save button state
        editorTabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            updateSaveButtonState();
        });
        
        // Initial state
        updateSaveButtonState();

        log("IDE initialized successfully");
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
        projectNameToolbar.setText(project.getName());

        // Build file tree
        refreshFileTree();

        log("Proyecto cargado: " + project.getName());
        log("Ubicación: " + project.getRootPath());

        if (!project.hasContent()) {
            log("Proyecto vacío - Usa el botón '+' para añadir elementos");
        }
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
        menuToggleFileTree.setOnAction(e -> toggleFileTree());

        menuAbout.setOnAction(e -> showAbout());
        menuDocs.setOnAction(e -> showDocs());
        menuLicenses.setOnAction(e -> showLicenses());
    }

    private void setupToolbarActions() {
        btnBack.setOnAction(e -> handleClose());
        btnExplorer.setOnAction(e -> handleExplorer());
        btnSearch.setOnAction(e -> handleSearch());
        btnSave.setOnAction(e -> handleSave());
        btnFormat.setOnAction(e -> handleFormat());
        btnExport.setOnAction(e -> handleExport());
        btnTest.setOnAction(e -> handleTest());
        btnSettings.setOnAction(e -> handleSettings());
        
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
        searchView.setVisible(false);
    }

    private void handleSearch() {
        projectExplorerView.setVisible(false);
        searchView.setVisible(true);
        searchField.requestFocus();
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
    
    private void openFileByPath(Path filePath) {
        // Check if file is already open
        for (Tab tab : editorTabs.getTabs()) {
            if (tabFileMap.get(tab).equals(filePath)) {
                editorTabs.getSelectionModel().select(tab);
                return;
            }
        }

        // Check if it's an image file
        String fileName = filePath.getFileName().toString().toLowerCase();
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
                
                webEngine.getLoadWorker().stateProperty().addListener((obs, oldState, newState) -> {
                    if (newState == Worker.State.SUCCEEDED) {
                        JSObject window = (JSObject) webEngine.executeScript("window");
                        window.setMember("javaApp", new JavaBridge(content));
                        
                        String lang = getMonacoLanguage(filePath.getFileName().toString());
                        webEngine.executeScript("setTimeout(function() { if(typeof setContent === 'function') { setContent(javaApp.getContent()); setLanguage('" + lang + "'); } }, 200);");
                    }
                });
                
                webEngine.load(editorUrl);
                tab.setContent(webView);
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

        MenuItem renameItem = new MenuItem("✏ Renombrar");
        renameItem.setOnAction(e -> handleRenameFile(item));

        MenuItem deleteItem = new MenuItem("🗑 Eliminar");
        deleteItem.setOnAction(e -> handleDeleteFile(item));

        menu.getItems().addAll(renameItem, deleteItem);
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

            // Create zoom controls
            Label zoomLabel = new Label("100%");
            zoomLabel.setStyle("-fx-text-fill: white; -fx-background-color: rgba(0,0,0,0.5); -fx-padding: 5; -fx-background-radius: 5;");
            
            Slider zoomSlider = new Slider(0.1, 5.0, 1.0);
            zoomSlider.setMaxWidth(200);
            zoomSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
                double zoom = newVal.doubleValue();
                imageView.setScaleX(zoom);
                imageView.setScaleY(zoom);
                zoomLabel.setText(String.format("%.0f%%", zoom * 100));
            });
            
            // Zoom with scroll wheel
            javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane(imageView);
            imageContainer.setStyle("-fx-background-color: #1e1e1e; -fx-alignment: center;");
            
            imageContainer.setOnScroll(e -> {
                if (e.isControlDown()) {
                    double delta = e.getDeltaY();
                    double zoomFactor = 1.05;
                    double currentZoom = zoomSlider.getValue();
                    
                    if (delta < 0) {
                        zoomSlider.setValue(currentZoom / zoomFactor);
                    } else {
                        zoomSlider.setValue(currentZoom * zoomFactor);
                    }
                    e.consume();
                }
            });

            // Wrap in ScrollPane for panning
            ScrollPane scrollPane = new ScrollPane(imageContainer);
            scrollPane.setFitToWidth(true);
            scrollPane.setFitToHeight(true);
            scrollPane.setStyle("-fx-background-color: #1e1e1e; -fx-background: #1e1e1e;");
            
            // Layout with controls
            VBox mainLayout = new VBox();
            mainLayout.setStyle("-fx-background-color: #1e1e1e;");
            
            HBox controls = new HBox(10);
            controls.setAlignment(javafx.geometry.Pos.CENTER);
            controls.setPadding(new javafx.geometry.Insets(10));
            controls.setStyle("-fx-background-color: #2d2d2d;");
            controls.getChildren().addAll(new Label("Zoom:"), zoomSlider, zoomLabel);
            
            mainLayout.getChildren().addAll(controls, scrollPane);
            VBox.setVgrow(scrollPane, javafx.scene.layout.Priority.ALWAYS);

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

    private void openModelInEditor(Path modelPath) {
        try {
            // Create 3D Scene
            Group root3D = new Group();
            SubScene subScene = new SubScene(root3D, 800, 600, true, SceneAntialiasing.BALANCED);
            subScene.setFill(Color.rgb(30, 30, 30));
            
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

        btnSave.setDisable(!shouldEnable);
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
        consoleContainer.setVisible(consoleVisible);
        consoleContainer.setManaged(consoleVisible);
        btnToggleConsole.setText(consoleVisible ? "▼" : "▲");
        menuToggleConsole.setSelected(consoleVisible);
    }

    private void toggleFileTree() {
        VBox sidebar = (VBox) fileTree.getParent();
        if (sidebar != null) {
            sidebar.setVisible(!sidebar.isVisible());
            sidebar.setManaged(sidebar.isVisible());
            log("Toggle file tree: " + (sidebar.isVisible() ? "visible" : "hidden"));
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
        try {
            java.net.URL projectUrl = getClass().getResource("/LICENSE");
            if (projectUrl != null) {
                content.append("=== ADDON CREATOR LICENSE ===\n\n");
                content.append(Files.readString(Paths.get(projectUrl.toURI())));
                content.append("\n\n");
            }
        } catch (Exception e) {
            logger.error("Failed to load project license", e);
        }

        // Monaco License
        try {
            java.net.URL monacoUrl = getClass().getResource("/monaco-editor/LICENSE.txt");
            if (monacoUrl != null) {
                content.append("=== MONACO EDITOR LICENSE ===\n\n");
                content.append(Files.readString(Paths.get(monacoUrl.toURI())));
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

    private void log(String message) {
        consoleOutput.appendText(message + "\n");
        logger.info(message);
    }

    private void showError(String title, String message) {
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

    public class JavaBridge {
        private String content;

        public JavaBridge(String content) {
            this.content = content;
        }

        public String getContent() {
            return content;
        }
        
        public void log(String msg) {
            EditorController.this.log(msg);
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

    private static class FileIconFactory {
        // SVG Paths
        private static final String FOLDER_PATH = "M10 4H4C2.9 4 2 4.9 2 6V18C2 19.1 2.9 20 4 20H20C21.1 20 22 19.1 22 18V8C22 6.9 21.1 6 20 6H12L10 4Z";
        private static final String FILE_BODY = "M14 2H6C4.89 2 4 2.9 4 4V20C4 21.1 4.89 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2Z";
        private static final String FILE_CORNER = "M14 2V8H20";
        
        // 3D Model Icon Paths (Deprecated/Replaced)
        // private static final String MODEL_PATH_1 = "M12 2L2 7l10 5 10-5-10-5z";
        // ...
        
        public static javafx.scene.Node createIcon(String filename, boolean isDir) {
            if (isDir) {
                SVGPath folder = new SVGPath();
                folder.setContent(FOLDER_PATH);
                folder.setFill(Color.web("#FFC107")); // Amber
                return createScaledIcon(folder);
            }
            
            String nameLower = filename.toLowerCase();
            String ext = getExtension(filename);
            
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
