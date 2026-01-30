package com.agustinbenitez.addoncreator.models;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Minecraft Bedrock addon manifest.json file
 * 
 * @author Agustín Benítez
 */
public class Manifest {
    
    @SerializedName("format_version")
    private int formatVersion;
    
    private Header header;
    
    private List<Module> modules;
    
    private List<Dependency> dependencies;

    private Metadata metadata;
    
    public Manifest() {
        this.formatVersion = 2;
        this.modules = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }
    
    public Manifest(int formatVersion, Header header) {
        this.formatVersion = formatVersion;
        this.header = header;
        this.modules = new ArrayList<>();
        this.dependencies = new ArrayList<>();
    }
    
    // Getters and Setters
    
    public int getFormatVersion() {
        return formatVersion;
    }
    
    public void setFormatVersion(int formatVersion) {
        this.formatVersion = formatVersion;
    }
    
    public Header getHeader() {
        return header;
    }
    
    public void setHeader(Header header) {
        this.header = header;
    }
    
    public List<Module> getModules() {
        return modules;
    }
    
    public void setModules(List<Module> modules) {
        this.modules = modules;
    }
    
    public void addModule(Module module) {
        this.modules.add(module);
    }
    
    public List<Dependency> getDependencies() {
        return dependencies;
    }
    
    public void setDependencies(List<Dependency> dependencies) {
        this.dependencies = dependencies;
    }
    
    public void addDependency(Dependency dependency) {
        this.dependencies.add(dependency);
    }

    public Metadata getMetadata() {
        return metadata;
    }

    public void setMetadata(Metadata metadata) {
        this.metadata = metadata;
    }
    
    @Override
    public String toString() {
        return "Manifest{" +
                "formatVersion=" + formatVersion +
                ", header=" + header +
                ", modules=" + modules.size() +
                ", dependencies=" + dependencies.size() +
                '}';
    }
}
