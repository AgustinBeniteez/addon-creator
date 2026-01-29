package com.agustinbenitez.addoncreator.models;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a module in the manifest.json file
 * 
 * @author Agustín Benítez
 */
public class Module {
    
    private String type;
    private String uuid;
    private List<Integer> version;
    
    public Module() {
        this.version = Arrays.asList(1, 0, 0);
    }
    
    public Module(String type, String uuid) {
        this();
        this.type = type;
        this.uuid = uuid;
    }
    
    public Module(String type, String uuid, List<Integer> version) {
        this.type = type;
        this.uuid = uuid;
        this.version = version;
    }
    
    // Getters and Setters
    
    public String getType() {
        return type;
    }
    
    public void setType(String type) {
        this.type = type;
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
    
    @Override
    public String toString() {
        return "Module{" +
                "type='" + type + '\'' +
                ", uuid='" + uuid + '\'' +
                ", version=" + version +
                '}';
    }
    
    /**
     * Module type constants
     */
    public static class Type {
        public static final String DATA = "data";
        public static final String RESOURCES = "resources";
        public static final String SCRIPT = "script";
        public static final String SKIN_PACK = "skin_pack";
        public static final String WORLD_TEMPLATE = "world_template";
    }
}
