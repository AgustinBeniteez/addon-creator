package com.agustinbenitez.addoncreator.core;

import com.agustinbenitez.addoncreator.models.Manifest;
import com.agustinbenitez.addoncreator.models.Header;
import com.agustinbenitez.addoncreator.models.Module;
import com.agustinbenitez.addoncreator.models.Dependency;
import com.agustinbenitez.addoncreator.models.Metadata;
import com.agustinbenitez.addoncreator.utils.UUIDGenerator;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates manifest.json files for Minecraft Bedrock addons
 * 
 * @author Agustín Benítez
 */
public class ManifestGenerator {

    private static final Logger logger = LoggerFactory.getLogger(ManifestGenerator.class);
    private static final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    /**
     * Creates a Behavior Pack manifest
     * 
     * @param name        Addon name
     * @param description Addon description
     * @param authors     List of authors
     * @param license     License text
     * @param productType Product type
     * @return Manifest object for BP
     */
    public static Manifest createBehaviorPackManifest(String name, String description, List<String> authors, String license, String productType) {
        logger.info("Creating Behavior Pack manifest for: {}", name);

        Manifest manifest = new Manifest();
        manifest.setFormatVersion(2);

        // Create header
        Header header = new Header(name, description, UUIDGenerator.generate());
        manifest.setHeader(header);

        // Add data module
        Module dataModule = new Module(Module.Type.DATA, UUIDGenerator.generate());
        manifest.addModule(dataModule);
        
        // Add metadata
        if (authors != null || license != null || productType != null) {
            Metadata metadata = new Metadata(authors, license, productType);
            manifest.setMetadata(metadata);
        }

        return manifest;
    }

    /**
     * Creates a Resource Pack manifest
     * 
     * @param name             Addon name
     * @param description      Addon description
     * @param behaviorPackUUID UUID of the corresponding BP (for dependency)
     * @param authors          List of authors
     * @param license          License text
     * @param productType      Product type
     * @return Manifest object for RP
     */
    public static Manifest createResourcePackManifest(String name, String description, String behaviorPackUUID, List<String> authors, String license, String productType) {
        logger.info("Creating Resource Pack manifest for: {}", name);

        Manifest manifest = new Manifest();
        manifest.setFormatVersion(2);

        // Create header
        Header header = new Header(name, description, UUIDGenerator.generate());
        manifest.setHeader(header);

        // Add resources module
        Module resourcesModule = new Module(Module.Type.RESOURCES, UUIDGenerator.generate());
        manifest.addModule(resourcesModule);

        // Add dependency to BP
        if (behaviorPackUUID != null && !behaviorPackUUID.isEmpty()) {
            Dependency dependency = new Dependency(behaviorPackUUID, 1, 0, 0);
            manifest.addDependency(dependency);
        }
        
        // Add metadata
        if (authors != null || license != null || productType != null) {
            Metadata metadata = new Metadata(authors, license, productType);
            manifest.setMetadata(metadata);
        }

        return manifest;
    }

    /**
     * Writes a manifest to a file
     * 
     * @param manifest   Manifest object to write
     * @param outputPath Path where to write the manifest.json
     * @throws IOException if writing fails
     */
    public static void writeManifest(Manifest manifest, Path outputPath) throws IOException {
        logger.info("Writing manifest to: {}", outputPath);

        // Ensure parent directory exists
        Files.createDirectories(outputPath.getParent());

        // Write JSON
        try (FileWriter writer = new FileWriter(outputPath.toFile())) {
            gson.toJson(manifest, writer);
            logger.info("Manifest written successfully");
        }
    }

    /**
     * Reads a manifest from a file
     * 
     * @param manifestPath Path to the manifest.json file
     * @return Manifest object
     * @throws IOException if reading fails
     */
    public static Manifest readManifest(Path manifestPath) throws IOException {
        logger.info("Reading manifest from: {}", manifestPath);

        String json = Files.readString(manifestPath);
        return gson.fromJson(json, Manifest.class);
    }
}
