package com.agustinbenitez.addoncreator.utils;

import java.util.UUID;

/**
 * Utility class for generating UUIDs for Minecraft Bedrock addons
 * 
 * @author Agustín Benítez
 */
public class UUIDGenerator {

    /**
     * Generates a random UUID in the format required by Minecraft Bedrock
     * 
     * @return A UUID string in lowercase with hyphens
     */
    public static String generate() {
        return UUID.randomUUID().toString().toLowerCase();
    }

    /**
     * Generates multiple UUIDs
     * 
     * @param count Number of UUIDs to generate
     * @return Array of UUID strings
     */
    public static String[] generate(int count) {
        String[] uuids = new String[count];
        for (int i = 0; i < count; i++) {
            uuids[i] = generate();
        }
        return uuids;
    }

    /**
     * Validates if a string is a valid UUID
     * 
     * @param uuid String to validate
     * @return true if valid UUID, false otherwise
     */
    public static boolean isValid(String uuid) {
        if (uuid == null || uuid.isEmpty()) {
            return false;
        }

        try {
            UUID.fromString(uuid);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
