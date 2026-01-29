package com.agustinbenitez.addoncreator.models;

import com.google.gson.annotations.SerializedName;
import java.util.Arrays;
import java.util.List;

/**
 * Represents the header section of a manifest.json file
 * 
 * @author Agustín Benítez
 */
public class Header {
    
    private String name;
    private String description;
    private String uuid;
    private List<Integer> version;
    
    @SerializedName("min_engine_version")
    private List<Integer> minEngineVersion;
    
    public Header() {
        this.version = Arrays.asList(1, 0, 0);
        this.minEngineVersion = Arrays.asList(1, 20, 0);
    }
    
    public Header(String name, String description, String uuid) {
        this();
        this.name = name;
        this.description = description;
        this.uuid = uuid;
    }
    
    // Getters and Setters
    
    public String getName() {
        return name;
    }
    
    public void setName(String name) {
        this.name = name;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description;
    }
    
    public String getUuid() {
        return uuid;
    }
    
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    
    public List<Integer> getVersion() {
        return version;
    }
    
    public void setVersion(List<Integer> version) {
        this.version = version;
    }
    
    public void setVersion(int major, int minor, int patch) {
        this.version = Arrays.asList(major, minor, patch);
    }
    
    public List<Integer> getMinEngineVersion() {
        return minEngineVersion;
    }
    
    public void setMinEngineVersion(List<Integer> minEngineVersion) {
        this.minEngineVersion = minEngineVersion;
    }
    
    public void setMinEngineVersion(int major, int minor, int patch) {
        this.minEngineVersion = Arrays.asList(major, minor, patch);
    }
    
    @Override
    public String toString() {
        return "Header{" +
                "name='" + name + '\'' +
                ", uuid='" + uuid + '\'' +
                ", version=" + version +
                '}';
    }
}
