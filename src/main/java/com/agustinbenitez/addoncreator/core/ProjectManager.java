package com.agustinbenitez.addoncreator.core;

import com.agustinbenitez.addoncreator.models.Project;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Manages project persistence and CRUD operations
 * 
 * @author Agustín Benítez
 */
public class ProjectManager {

    private static final Logger logger = LoggerFactory.getLogger(ProjectManager.class);
    private static final String APP_DATA_DIR = System.getenv("APPDATA") + File.separator + "AddonCreator";
    private static final String PROJECTS_FILE = "projects.json";

    private final Gson gson;
    private final Path projectsFilePath;

    public ProjectManager() {
        this.gson = new GsonBuilder()
                .setPrettyPrinting()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeAdapter())
                .create();

        this.projectsFilePath = Paths.get(APP_DATA_DIR, PROJECTS_FILE);

        // Ensure app data directory exists
        try {
            Files.createDirectories(Paths.get(APP_DATA_DIR));
            logger.info("App data directory: {}", APP_DATA_DIR);
        } catch (IOException e) {
            logger.error("Failed to create app data directory", e);
        }
    }

    /**
     * Load all projects from storage
     */
    public List<Project> loadProjects() {
        if (!Files.exists(projectsFilePath)) {
            logger.info("No projects file found, returning empty list");
            return new ArrayList<>();
        }

        try (FileReader reader = new FileReader(projectsFilePath.toFile())) {
            Type listType = new TypeToken<ArrayList<Project>>() {
            }.getType();
            List<Project> projects = gson.fromJson(reader, listType);
            logger.info("Loaded {} projects", projects != null ? projects.size() : 0);
            return projects != null ? projects : new ArrayList<>();
        } catch (IOException e) {
            logger.error("Failed to load projects", e);
            return new ArrayList<>();
        }
    }

    /**
     * Save all projects to storage
     */
    public void saveProjects(List<Project> projects) {
        try (FileWriter writer = new FileWriter(projectsFilePath.toFile())) {
            gson.toJson(projects, writer);
            logger.info("Saved {} projects", projects.size());
        } catch (IOException e) {
            logger.error("Failed to save projects", e);
        }
    }

    /**
     * Add a new project
     */
    public void addProject(Project project) {
        List<Project> projects = loadProjects();
        projects.add(project);
        saveProjects(projects);
        logger.info("Added project: {}", project.getName());
    }

    /**
     * Update an existing project
     */
    public void updateProject(Project updatedProject) {
        List<Project> projects = loadProjects();

        for (int i = 0; i < projects.size(); i++) {
            if (projects.get(i).getId().equals(updatedProject.getId())) {
                projects.set(i, updatedProject);
                saveProjects(projects);
                logger.info("Updated project: {}", updatedProject.getName());
                return;
            }
        }

        logger.warn("Project not found for update: {}", updatedProject.getId());
    }

    /**
     * Delete a project
     */
    public void deleteProject(String projectId) {
        List<Project> projects = loadProjects();
        projects.removeIf(p -> p.getId().equals(projectId));
        saveProjects(projects);
        logger.info("Deleted project: {}", projectId);
    }

    /**
     * Remove a project (convenience method)
     */
    public void removeProject(Project project) {
        deleteProject(project.getId());
    }

    /**
     * Get a project by ID
     */
    public Optional<Project> getProjectById(String projectId) {
        List<Project> projects = loadProjects();
        return projects.stream()
                .filter(p -> p.getId().equals(projectId))
                .findFirst();
    }

    /**
     * Get projects file path for debugging
     */
    public String getProjectsFilePath() {
        return projectsFilePath.toString();
    }
}
