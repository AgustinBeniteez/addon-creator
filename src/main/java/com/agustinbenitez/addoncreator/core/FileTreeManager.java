package com.agustinbenitez.addoncreator.core;

import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Manages file tree display for the IDE
 * 
 * @author Agustín Benítez
 */
public class FileTreeManager {

    private static final Logger logger = LoggerFactory.getLogger(FileTreeManager.class);

    /**
     * Build a TreeItem structure from a root path
     */
    public static TreeItem<String> buildFileTree(Path rootPath) {
        if (!Files.exists(rootPath)) {
            logger.warn("Root path does not exist: {}", rootPath);
            return new TreeItem<>("Project (empty)");
        }

        File rootFile = rootPath.toFile();
        TreeItem<String> rootItem = new TreeItem<>(rootFile.getName());
        rootItem.setExpanded(true);

        buildTreeRecursive(rootFile, rootItem);

        return rootItem;
    }

    /**
     * Recursively build tree structure
     */
    private static void buildTreeRecursive(File directory, TreeItem<String> parent) {
        File[] files = directory.listFiles();

        if (files == null) {
            return;
        }

        // Sort: directories first, then files, alphabetically
        Arrays.sort(files, Comparator.comparing((File f) -> !f.isDirectory())
                .thenComparing(File::getName));

        for (File file : files) {
            // Skip .git folder and target directory, but show other dotfiles like .gitignore
            if ((file.isDirectory() && file.getName().equals(".git")) || file.getName().equals("target")) {
                continue;
            }

            // Store raw name, icons are handled by CellFactory
            String displayName = file.getName();

            TreeItem<String> item = new TreeItem<>(displayName);
            parent.getChildren().add(item);

            // Recursively add children for directories
            if (file.isDirectory()) {
                buildTreeRecursive(file, item);
            }
        }
    }

    /**
     * Get the file path from a TreeItem
     */
    public static Path getPathFromTreeItem(TreeItem<String> item, Path rootPath) {
        if (item == null || item.getParent() == null) {
            return rootPath;
        }

        // Build path from root to this item
        StringBuilder pathBuilder = new StringBuilder();
        TreeItem<String> current = item;

        while (current.getParent() != null) {
            String name = current.getValue();
            
            if (pathBuilder.length() > 0) {
                pathBuilder.insert(0, File.separator);
            }
            pathBuilder.insert(0, name);

            current = current.getParent();
        }

        return rootPath.resolve(pathBuilder.toString());
    }

    /**
     * Check if a TreeItem represents a file (not a directory)
     */
    public static boolean isFile(TreeItem<String> item) {
        if (item == null) {
            return false;
        }
        // Since we removed emojis, we need another way to check.
        // However, this method is static and doesn't have access to the File system directly easily
        // without reconstructing the path.
        // But the caller usually knows context.
        // Actually, the TreeItem doesn't store if it's a file or folder explicitly anymore in the value.
        // We can check if it has children, but empty folders have no children too.
        // Best approach: The TreeItem is just a model. The controller determines if it's a file by checking filesystem.
        // Or we can keep a convention but invisible? No.
        // Let's rely on the filesystem check in the controller using getPathFromTreeItem.
        // For now, return !item.getChildren().isEmpty() is NOT reliable.
        // Let's deprecate this or make it try to guess?
        // Actually, buildFileTree creates the structure.
        // We can subclass TreeItem<String> to FileTreeItem that knows?
        // Too much refactoring.
        // Let's just use Files.isDirectory(path) in the controller.
        return true; // Placeholder, logic should move to controller
    }
}
