package com.agustinbenitez.addoncreator.core;

import java.util.Properties;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Manages application settings
 */
public class SettingsManager {

    private static SettingsManager instance;
    private Properties properties;
    private static final String SETTINGS_FILE = "settings.properties";

    // Default settings
    public static final String KEY_LANGUAGE = "language";
    public static final String KEY_THEME = "theme";
    public static final String KEY_GIT_USER = "git_user";
    public static final String KEY_GIT_TOKEN = "git_token";

    private SettingsManager() {
        properties = new Properties();
        loadSettings();
    }

    public static SettingsManager getInstance() {
        if (instance == null) {
            instance = new SettingsManager();
            // Default values
            if (!instance.properties.containsKey(KEY_LANGUAGE)) {
                instance.setLanguage("English");
            }
        }
        return instance;
    }

    private void loadSettings() {
        File file = new File(SETTINGS_FILE);
        if (file.exists()) {
            try (FileInputStream in = new FileInputStream(file)) {
                properties.load(in);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void saveSettings() {
        try (FileOutputStream out = new FileOutputStream(SETTINGS_FILE)) {
            properties.store(out, "Addon Creator Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getLanguage() {
        return properties.getProperty(KEY_LANGUAGE, "English");
    }

    public void setLanguage(String language) {
        properties.setProperty(KEY_LANGUAGE, language);
        saveSettings();
    }

    public String getGitUser() {
        return properties.getProperty(KEY_GIT_USER, null);
    }

    public String getGitToken() {
        return properties.getProperty(KEY_GIT_TOKEN, null);
    }

    public void setGitCredentials(String user, String token) {
        if (user != null) properties.setProperty(KEY_GIT_USER, user);
        if (token != null) properties.setProperty(KEY_GIT_TOKEN, token);
        saveSettings();
    }

    public void clearGitCredentials() {
        properties.remove(KEY_GIT_USER);
        properties.remove(KEY_GIT_TOKEN);
        saveSettings();
    }
}
