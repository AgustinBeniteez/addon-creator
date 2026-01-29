package com.agustinbenitez.addoncreator.models;

import java.util.Arrays;
import java.util.List;

/**
 * Represents a dependency in the manifest.json file
 * 
 * @author Agustín Benítez
 */
public class Dependency {

    private String uuid;
    private List<Integer> version;

    public Dependency() {
        this.version = Arrays.asList(1, 0, 0);
    }

    public Dependency(String uuid, List<Integer> version) {
        this.uuid = uuid;
        this.version = version;
    }

    public Dependency(String uuid, int major, int minor, int patch) {
        this.uuid = uuid;
        this.version = Arrays.asList(major, minor, patch);
    }

    // Getters and Setters

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
        return "Dependency{" +
                "uuid='" + uuid + '\'' +
                ", version=" + version +
                '}';
    }
}
