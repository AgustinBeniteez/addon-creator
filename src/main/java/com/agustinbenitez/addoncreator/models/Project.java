package com.agustinbenitez.addoncreator.models;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Model class representing an addon project
 * 
 * @author Agustín Benítez
 */
public class Project {

    private String id;
    private String name;
    private String description;
    private String rootPath;
    private LocalDateTime createdDate;
    private LocalDateTime lastModified;

    // Lists to track added elements
    private List<String> entities;
    private List<String> items;
    private List<String> blocks;

    // Manifest info
    private boolean manifestGenerated;

    public Project() {
        this.id = UUID.randomUUID().toString();
        this.createdDate = LocalDateTime.now();
        this.lastModified = LocalDateTime.now();
        this.entities = new ArrayList<>();
        this.items = new ArrayList<>();
        this.blocks = new ArrayList<>();
        this.manifestGenerated = false;
    }

    public Project(String name, String description, String rootPath) {
        this();
        this.name = name;
        this.description = description;
        this.rootPath = rootPath;
    }

    // Getters and Setters

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
        updateLastModified();
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
        updateLastModified();
    }

    public String getRootPath() {
        return rootPath;
    }

    public void setRootPath(String rootPath) {
        this.rootPath = rootPath;
    }

    public LocalDateTime getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(LocalDateTime createdDate) {
        this.createdDate = createdDate;
    }

    public LocalDateTime getLastModified() {
        return lastModified;
    }

    public void setLastModified(LocalDateTime lastModified) {
        this.lastModified = lastModified;
    }

    public List<String> getEntities() {
        return entities;
    }

    public void setEntities(List<String> entities) {
        this.entities = entities;
    }

    public List<String> getItems() {
        return items;
    }

    public void setItems(List<String> items) {
        this.items = items;
    }

    public List<String> getBlocks() {
        return blocks;
    }

    public void setBlocks(List<String> blocks) {
        this.blocks = blocks;
    }

    public boolean isManifestGenerated() {
        return manifestGenerated;
    }

    public void setManifestGenerated(boolean manifestGenerated) {
        this.manifestGenerated = manifestGenerated;
    }

    // Helper methods

    public void addEntity(String entityName) {
        if (!entities.contains(entityName)) {
            entities.add(entityName);
            updateLastModified();
        }
    }

    public void addItem(String itemName) {
        if (!items.contains(itemName)) {
            items.add(itemName);
            updateLastModified();
        }
    }

    public void addBlock(String blockName) {
        if (!blocks.contains(blockName)) {
            blocks.add(blockName);
            updateLastModified();
        }
    }

    public boolean hasContent() {
        return !entities.isEmpty() || !items.isEmpty() || !blocks.isEmpty();
    }

    private void updateLastModified() {
        this.lastModified = LocalDateTime.now();
    }

    @Override
    public String toString() {
        return "Project{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", rootPath='" + rootPath + '\'' +
                '}';
    }
}
