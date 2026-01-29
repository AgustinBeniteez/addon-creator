package com.agustinbenitez.addoncreator.core;

import com.agustinbenitez.addoncreator.models.Manifest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates folder structure for Minecraft Bedrock addons on-demand
 * 
 * @author Agustín Benítez
 */
public class ProjectGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ProjectGenerator.class);

    /**
     * Creates only the base BP and RP folders with manifests
     * Called when first element is added to a project
     * 
     * @param rootPath    Root directory where to create the addon
     * @param addonName   Name of the addon
     * @param description Description of the addon
     * @throws IOException if folder creation fails
     */
    public static void generateBaseStructure(Path rootPath, String addonName, String description) throws IOException {
        logger.info("Generating base structure at: {}", rootPath);

        // Create BP and RP directories
        Path bpPath = rootPath.resolve("BP");
        Path rpPath = rootPath.resolve("RP");

        Files.createDirectories(bpPath);
        Files.createDirectories(rpPath);

        // Generate manifests
        Manifest bpManifest = ManifestGenerator.createBehaviorPackManifest(addonName, description);
        ManifestGenerator.writeManifest(bpManifest, bpPath.resolve("manifest.json"));

        String bpUUID = bpManifest.getHeader().getUuid();
        Manifest rpManifest = ManifestGenerator.createResourcePackManifest(addonName, description, bpUUID);
        ManifestGenerator.writeManifest(rpManifest, rpPath.resolve("manifest.json"));

        // Create pack icons (placeholder)
        createPackIcon(bpPath);
        createPackIcon(rpPath);

        logger.info("Base structure generated successfully");
    }

    /**
     * Creates entity folder when first entity is added
     */
    public static void createEntityFolder(Path rootPath) throws IOException {
        Path entityPath = rootPath.resolve("BP/entities");
        if (!Files.exists(entityPath)) {
            Files.createDirectories(entityPath);
            logger.info("Created entities folder");
        }

        // Also create RP entity folders
        Files.createDirectories(rootPath.resolve("RP/textures/entity"));
        Files.createDirectories(rootPath.resolve("RP/models/entity"));
    }

    /**
     * Creates item folder when first item is added
     */
    public static void createItemFolder(Path rootPath) throws IOException {
        Path itemPath = rootPath.resolve("BP/items");
        if (!Files.exists(itemPath)) {
            Files.createDirectories(itemPath);
            logger.info("Created items folder");
        }

        // Also create RP item folders
        Files.createDirectories(rootPath.resolve("RP/textures/items"));
    }

    /**
     * Creates block folder when first block is added
     */
    public static void createBlockFolder(Path rootPath) throws IOException {
        Path blockPath = rootPath.resolve("BP/blocks");
        if (!Files.exists(blockPath)) {
            Files.createDirectories(blockPath);
            logger.info("Created blocks folder");
        }

        // Also create RP block folders
        Files.createDirectories(rootPath.resolve("RP/textures/blocks"));
    }

    /**
     * OLD METHOD - kept for backward compatibility
     * Creates the complete addon structure with BP and RP folders
     * 
     * @param rootPath    Root directory where to create the addon
     * @param addonName   Name of the addon
     * @param description Description of the addon
     * @throws IOException if folder creation fails
     */
    @Deprecated
    public static void generateProject(Path rootPath, String addonName, String description) throws IOException {
        logger.info("Generating addon project at: {}", rootPath);

        // Create BP and RP directories
        Path bpPath = rootPath.resolve("BP");
        Path rpPath = rootPath.resolve("RP");

        Files.createDirectories(bpPath);
        Files.createDirectories(rpPath);

        // Create BP structure
        createBehaviorPackStructure(bpPath);

        // Create RP structure
        createResourcePackStructure(rpPath);

        // Generate manifests
        Manifest bpManifest = ManifestGenerator.createBehaviorPackManifest(addonName, description);
        ManifestGenerator.writeManifest(bpManifest, bpPath.resolve("manifest.json"));

        String bpUUID = bpManifest.getHeader().getUuid();
        Manifest rpManifest = ManifestGenerator.createResourcePackManifest(addonName, description, bpUUID);
        ManifestGenerator.writeManifest(rpManifest, rpPath.resolve("manifest.json"));

        // Create pack icons (placeholder)
        createPackIcon(bpPath);
        createPackIcon(rpPath);

        logger.info("Addon project generated successfully");
    }

    /**
     * Creates the standard Behavior Pack folder structure
     */
    private static void createBehaviorPackStructure(Path bpPath) throws IOException {
        logger.debug("Creating BP structure");

        Files.createDirectories(bpPath.resolve("entities"));
        Files.createDirectories(bpPath.resolve("items"));
        Files.createDirectories(bpPath.resolve("blocks"));
        Files.createDirectories(bpPath.resolve("loot_tables"));
        Files.createDirectories(bpPath.resolve("recipes"));
        Files.createDirectories(bpPath.resolve("spawn_rules"));
        Files.createDirectories(bpPath.resolve("trading"));
        Files.createDirectories(bpPath.resolve("functions"));
        Files.createDirectories(bpPath.resolve("animations"));
        Files.createDirectories(bpPath.resolve("animation_controllers"));
    }

    /**
     * Creates the standard Resource Pack folder structure
     */
    private static void createResourcePackStructure(Path rpPath) throws IOException {
        logger.debug("Creating RP structure");

        Files.createDirectories(rpPath.resolve("textures/items"));
        Files.createDirectories(rpPath.resolve("textures/blocks"));
        Files.createDirectories(rpPath.resolve("textures/entity"));
        Files.createDirectories(rpPath.resolve("models/entity"));
        Files.createDirectories(rpPath.resolve("animations"));
        Files.createDirectories(rpPath.resolve("animation_controllers"));
        Files.createDirectories(rpPath.resolve("render_controllers"));
        Files.createDirectories(rpPath.resolve("sounds"));
        Files.createDirectories(rpPath.resolve("particles"));
        Files.createDirectories(rpPath.resolve("texts"));
    }

    /**
     * Creates a placeholder pack_icon.png file
     */
    private static void createPackIcon(Path packPath) throws IOException {
        // For now, just create an empty file
        // In the future, we can generate a default icon
        Path iconPath = packPath.resolve("pack_icon.png");
        if (!Files.exists(iconPath)) {
            Files.createFile(iconPath);
            logger.debug("Created placeholder pack_icon.png");
        }
    }
}
