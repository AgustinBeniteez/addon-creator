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
import javafx.scene.layout.VBox;
import javafx.scene.web.WebView;
import javafx.scene.web.WebEngine;
import javafx.concurrent.Worker;
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
    private boolean consoleVisible = true;

    @FXML
    public void initialize() {
        logger.info("Initializing IDE EditorController");

        projectManager = new ProjectManager();
        tabFileMap = new HashMap<>();

        setupMenuActions();
        setupToolbarActions();
        setupFileTree();
        setupConsole();
        setupAddElementButton();
        setupNewFileButton();

        log("IDE initialized successfully");
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
        btnSave.setOnAction(e -> handleSave());
        btnFormat.setOnAction(e -> handleFormat());
        btnExport.setOnAction(e -> handleExport());
        btnTest.setOnAction(e -> handleTest());
        btnSettings.setOnAction(e -> handleSettings());
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
            if (newVal != null && FileTreeManager.isFile(newVal)) {
                openFileInEditor(newVal);
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
                    // Allow drop if target is a folder (or root)
                    if (targetItem != null && !FileTreeManager.isFile(targetItem)) {
                        event.acceptTransferModes(TransferMode.MOVE);
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
                        entry.getKey().setText(newName);
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

    private void openFileInEditor(TreeItem<String> fileItem) {
        Path filePath = FileTreeManager.getPathFromTreeItem(fileItem, Paths.get(currentProject.getRootPath()));

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

        // Open text file
        try {
            String content = Files.readString(filePath);

            Tab tab = new Tab(filePath.getFileName().toString());
            
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
            editorTabs.getTabs().add(tab);
            editorTabs.getSelectionModel().select(tab);

            log("Archivo abierto: " + filePath.getFileName());

        } catch (IOException ex) {
            logger.error("Failed to open file", ex);
            log("✗ Error al abrir archivo: " + ex.getMessage());
        }
    }

    private void openImageInEditor(Path imagePath) {
        try {
            javafx.scene.image.Image image = new javafx.scene.image.Image(imagePath.toUri().toString());
            javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView(image);
            imageView.setPreserveRatio(true);
            imageView.setFitWidth(800);

            javafx.scene.layout.StackPane imageContainer = new javafx.scene.layout.StackPane(imageView);
            imageContainer.setStyle("-fx-background-color: #1e1e1e; -fx-alignment: center;");

            Tab tab = new Tab(imagePath.getFileName().toString());
            tab.setContent(imageContainer);

            tabFileMap.put(tab, imagePath);
            editorTabs.getTabs().add(tab);
            editorTabs.getSelectionModel().select(tab);

            log("Imagen abierta: " + imagePath.getFileName());

        } catch (Exception ex) {
            logger.error("Failed to open image", ex);
            log("✗ Error al abrir imagen: " + ex.getMessage());
        }
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
        NavigationManager.getInstance().showSettings(() -> NavigationManager.getInstance().showEditor(currentProject));
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
    }
}
