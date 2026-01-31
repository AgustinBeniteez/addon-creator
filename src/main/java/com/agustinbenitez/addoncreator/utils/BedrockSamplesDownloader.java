package com.agustinbenitez.addoncreator.utils;

import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

/**
 * Utility to download textures from the official Bedrock Samples repository.
 */
public class BedrockSamplesDownloader {

    private static final Logger logger = LoggerFactory.getLogger(BedrockSamplesDownloader.class);
    private static final String REPO_ZIP_URL = "https://github.com/Mojang/bedrock-samples/archive/refs/heads/main.zip";
    private static final String API_TREE_URL = "https://api.github.com/repos/Mojang/bedrock-samples/git/trees/main?recursive=1";
    private static final String RAW_CONTENT_URL = "https://raw.githubusercontent.com/Mojang/bedrock-samples/main/";
    
    // Paths inside the ZIP (it usually starts with bedrock-samples-main/)
    private static final String ZIP_ROOT_PREFIX = "bedrock-samples-main/";
    private static final String RP_PREFIX = "resource_pack/";
    private static final String BP_PREFIX = "behavior_pack/";

    public enum TextureCategory {
        ITEMS,
        BLOCKS,
        ENTITIES,
        HUD
    }

    /**
     * Fetches a list of available texture files from the repository using GitHub API.
     * Returns paths relative to the repository root (e.g., resource_pack/textures/items/apple.png).
     */
    public static List<String> fetchTextureList() throws IOException {
        return fetchFileList("resource_pack/textures/", ".png", ".tga", ".jpg");
    }

    public static List<String> fetchModelList() throws IOException {
        return fetchFileList("resource_pack/models/", ".json", ".geo.json");
    }

    public static List<String> fetchEntityList() throws IOException {
        return fetchFileList("behavior_pack/entities/", ".json");
    }

    public static List<String> fetchSoundList() throws IOException {
        return fetchFileList("resource_pack/sounds/", ".ogg", ".wav", ".fsb");
    }

    private static List<String> fetchFileList(String prefix, String... extensions) throws IOException {
        logger.info("Fetching file list from GitHub API for prefix: " + prefix);
        List<String> files = new ArrayList<>();
        
        URL url = new URL(API_TREE_URL);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/vnd.github.v3+json");
        conn.setRequestProperty("User-Agent", "AddonCreator"); // GitHub requires User-Agent
        
        if (conn.getResponseCode() != 200) {
            throw new IOException("Failed to fetch file list. HTTP Code: " + conn.getResponseCode());
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            Gson gson = new Gson();
            JsonObject json = gson.fromJson(reader, JsonObject.class);
            JsonArray tree = json.getAsJsonArray("tree");
            
            for (JsonElement element : tree) {
                JsonObject entry = element.getAsJsonObject();
                String path = entry.get("path").getAsString();
                
                // Filter
                if (path.startsWith(prefix)) {
                    for (String ext : extensions) {
                        if (path.endsWith(ext)) {
                            files.add(path);
                            break;
                        }
                    }
                }
            }
        }
        
        return files;
    }

    /**
     * Downloads specific files from the repository.
     * 
     * @param relativePaths List of paths relative to repo root (e.g. resource_pack/textures/items/apple.png)
     * @param projectRoot Project root path
     * @param onProgress Callback run after each file download
     */
    public static void downloadSpecificFiles(List<String> relativePaths, Path projectRoot, Runnable onProgress) {
        Path rpRoot = projectRoot.resolve("RP");
        Path bpRoot = projectRoot.resolve("BP");
        
        for (String relativePath : relativePaths) {
            try {
                Path targetRoot = rpRoot;
                String pathInsidePack = relativePath;
                
                // Determine target pack (RP or BP)
                if (relativePath.startsWith("resource_pack/")) {
                    pathInsidePack = relativePath.substring("resource_pack/".length());
                    targetRoot = rpRoot;
                } else if (relativePath.startsWith("behavior_pack/")) {
                    pathInsidePack = relativePath.substring("behavior_pack/".length());
                    targetRoot = bpRoot;
                }
                
                Path targetPath = targetRoot.resolve(pathInsidePack);
                Files.createDirectories(targetPath.getParent());
                
                // Download file
                URL url = new URL(RAW_CONTENT_URL + relativePath);
                try (InputStream in = url.openStream()) {
                    Files.copy(in, targetPath, StandardCopyOption.REPLACE_EXISTING);
                }
                
                if (onProgress != null) {
                    Platform.runLater(onProgress);
                }
                
            } catch (Exception e) {
                logger.error("Failed to download file: " + relativePath, e);
            }
        }
    }

    /**
     * Downloads and extracts selected texture categories to the project's Resource Pack folder.
     * This method is blocking and should be run on a background thread.
     *
     * @param projectRoot The root folder of the project.
     * @param categories The set of categories to download.
     * @param onProgress Callback for progress (optional).
     */
    public static void downloadTextures(Path projectRoot, Set<TextureCategory> categories, Runnable onProgress) throws IOException {
        logger.info("Starting download of textures for categories: {}", categories);
        
        Path rpRoot = projectRoot.resolve("RP");
        if (!Files.exists(rpRoot)) {
            Files.createDirectories(rpRoot);
        }

        URL url = new URL(REPO_ZIP_URL);
        
        try (InputStream in = url.openStream();
             BufferedInputStream buf = new BufferedInputStream(in);
             ZipInputStream zipIn = new ZipInputStream(buf)) {
            
            ZipEntry entry;
            while ((entry = zipIn.getNextEntry()) != null) {
                if (entry.isDirectory()) {
                    continue;
                }

                String name = entry.getName();
                
                // Strip the root folder name (e.g. bedrock-samples-main/)
                String relativeName = name;
                if (name.startsWith(ZIP_ROOT_PREFIX)) {
                    relativeName = name.substring(ZIP_ROOT_PREFIX.length());
                } else {
                    // Just in case the branch name is different or structure changed
                    int slashIndex = name.indexOf('/');
                    if (slashIndex != -1) {
                        relativeName = name.substring(slashIndex + 1);
                    }
                }

                // We only care about resource_pack/
                if (!relativeName.startsWith(RP_PREFIX)) {
                    continue;
                }

                String pathInsideRp = relativeName.substring(RP_PREFIX.length());
                
                if (shouldDownload(pathInsideRp, categories)) {
                    Path targetPath = rpRoot.resolve(pathInsideRp);
                    
                    // Create parent directories
                    Files.createDirectories(targetPath.getParent());
                    
                    // Copy file
                    Files.copy(zipIn, targetPath, StandardCopyOption.REPLACE_EXISTING);
                    
                    if (onProgress != null) {
                        // Simple progress tick
                        Platform.runLater(onProgress);
                    }
                }
            }
        }
        
        logger.info("Texture download completed.");
    }

    private static boolean shouldDownload(String path, Set<TextureCategory> categories) {
        // path is relative to resource_pack/ e.g. "textures/items/apple.png"
        
        if (categories.contains(TextureCategory.ITEMS)) {
            if (path.startsWith("textures/items/")) return true;
        }
        
        if (categories.contains(TextureCategory.BLOCKS)) {
            if (path.startsWith("textures/blocks/")) return true;
        }
        
        if (categories.contains(TextureCategory.ENTITIES)) {
            if (path.startsWith("textures/entity/")) return true;
        }
        
        if (categories.contains(TextureCategory.HUD)) {
            if (path.startsWith("textures/ui/")) return true;
        }
        
        return false;
    }
}
