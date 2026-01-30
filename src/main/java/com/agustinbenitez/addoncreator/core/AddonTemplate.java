package com.agustinbenitez.addoncreator.core;

import javafx.scene.image.Image;

/**
 * Represents a project template
 */
public class AddonTemplate {
    private String id;
    private String name;
    private String description;
    private String projectType; // "Resource Pack", "Behavior Pack", "Both (Addon)"
    private String category; // "Armor", "Blocks", "Mobs", "Biomes", "Utility"
    private String imagePath; // Path to placeholder image
    private java.io.File sourceDir; // Directory containing the template files

    public AddonTemplate(String id, String name, String description, String projectType, String category, String imagePath) {
        this(id, name, description, projectType, category, imagePath, null);
    }

    public AddonTemplate(String id, String name, String description, String projectType, String category, String imagePath, java.io.File sourceDir) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.projectType = projectType;
        this.category = category;
        this.imagePath = imagePath;
        this.sourceDir = sourceDir;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getProjectType() { return projectType; }
    public String getCategory() { return category; }
    public String getImagePath() { return imagePath; }
    public java.io.File getSourceDir() { return sourceDir; }
}
