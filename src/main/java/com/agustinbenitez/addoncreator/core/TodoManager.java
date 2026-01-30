package com.agustinbenitez.addoncreator.core;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class TodoManager {
    private final Path projectRoot;
    private final Path todoDir;
    private final Path tasksFile;
    private final Gson gson;
    private List<Task> tasks;

    public TodoManager(Path projectRoot) {
        this.projectRoot = projectRoot;
        this.todoDir = projectRoot.resolve(".TODO");
        this.tasksFile = todoDir.resolve("tasks.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.tasks = new ArrayList<>();
    }

    public boolean isInitialized() {
        return Files.exists(todoDir) && Files.exists(tasksFile);
    }

    public void initialize() throws IOException {
        if (!Files.exists(todoDir)) {
            Files.createDirectories(todoDir);
        }
        if (!Files.exists(tasksFile)) {
            tasks = new ArrayList<>();
            saveTasks();
        } else {
            loadTasks();
        }
    }

    public void loadTasks() throws IOException {
        if (!isInitialized()) {
            tasks = new ArrayList<>();
            return;
        }
        try (Reader reader = Files.newBufferedReader(tasksFile)) {
            tasks = gson.fromJson(reader, new TypeToken<List<Task>>(){}.getType());
            if (tasks == null) tasks = new ArrayList<>();
        }
    }

    public void saveTasks() throws IOException {
        try (Writer writer = Files.newBufferedWriter(tasksFile)) {
            gson.toJson(tasks, writer);
        }
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void addTask(String description) throws IOException {
        tasks.add(new Task(description, false));
        saveTasks();
    }
    
    public void updateTask(Task task) throws IOException {
        // Since we are modifying the object directly in the list, just save.
        saveTasks();
    }
    
    public void removeTask(Task task) throws IOException {
        tasks.remove(task);
        saveTasks();
    }

    public static class Task {
        private String description;
        private boolean completed;

        public Task(String description, boolean completed) {
            this.description = description;
            this.completed = completed;
        }

        public String getDescription() { return description; }
        public void setDescription(String description) { this.description = description; }

        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
