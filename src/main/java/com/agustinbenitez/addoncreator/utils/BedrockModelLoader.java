package com.agustinbenitez.addoncreator.utils;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.shape.VertexFormat;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.io.File;
import java.io.FileReader;

public class BedrockModelLoader {

    public static Group loadModel(File jsonFile, Image texture) {
        return loadModel(jsonFile, null, texture);
    }

    public static Group loadModel(File jsonFile, String geometryId, Image texture) {
        Group root = new Group();
        try {
            JsonObject jsonRoot = JsonParser.parseReader(new FileReader(jsonFile)).getAsJsonObject();
            JsonObject geometry = null;

            // Handle "minecraft:geometry" array
            if (jsonRoot.has("minecraft:geometry")) {
                JsonElement geo = jsonRoot.get("minecraft:geometry");
                if (geo.isJsonArray()) {
                    JsonArray geoArray = geo.getAsJsonArray();
                    if (geometryId != null) {
                        for (JsonElement e : geoArray) {
                            if (e.isJsonObject()) {
                                JsonObject obj = e.getAsJsonObject();
                                if (obj.has("description") && obj.getAsJsonObject("description").has("identifier")) {
                                    if (obj.getAsJsonObject("description").get("identifier").getAsString().equals(geometryId)) {
                                        geometry = obj;
                                        break;
                                    }
                                }
                            }
                        }
                        // Fallback to first if not found? Or return empty?
                        // If user asked for specific ID and we didn't find it, probably shouldn't load anything or first.
                        // Let's fallback to first if not found but log/warn?
                        // For now, if not found, geometry remains null.
                    }
                    
                    if (geometry == null && geoArray.size() > 0) {
                        geometry = geoArray.get(0).getAsJsonObject();
                    }
                }
            }

            if (geometry == null) return root;

            // Texture dimensions
            int texWidth = 64;
            int texHeight = 64;
            if (geometry.has("description")) {
                JsonObject desc = geometry.getAsJsonObject("description");
                if (desc.has("texture_width")) texWidth = desc.get("texture_width").getAsInt();
                if (desc.has("texture_height")) texHeight = desc.get("texture_height").getAsInt();
            }

            PhongMaterial material = new PhongMaterial();
            if (texture != null) {
                material.setDiffuseMap(texture);
            }

            if (geometry.has("bones")) {
                JsonArray bones = geometry.getAsJsonArray("bones");
                for (JsonElement boneElem : bones) {
                    JsonObject bone = boneElem.getAsJsonObject();
                    Group boneGroup = new Group();
                    
                    // Pivot
                    double pivotX = 0, pivotY = 0, pivotZ = 0;
                    if (bone.has("pivot")) {
                        JsonArray p = bone.getAsJsonArray("pivot");
                        pivotX = p.get(0).getAsDouble();
                        pivotY = p.get(1).getAsDouble();
                        pivotZ = p.get(2).getAsDouble();
                    }

                    // Rotation
                    if (bone.has("rotation")) {
                        JsonArray r = bone.getAsJsonArray("rotation");
                        double rx = -r.get(0).getAsDouble(); // Invert X rotation for JavaFX
                        double ry = -r.get(1).getAsDouble(); // Invert Y rotation
                        double rz = -r.get(2).getAsDouble(); // Invert Z rotation

                        Rotate rotX = new Rotate(rx, pivotX, -pivotY, pivotZ, Rotate.X_AXIS);
                        Rotate rotY = new Rotate(ry, pivotX, -pivotY, pivotZ, Rotate.Y_AXIS);
                        Rotate rotZ = new Rotate(rz, pivotX, -pivotY, pivotZ, Rotate.Z_AXIS);
                        
                        // Order YXZ is common in MC? Or ZXY? 
                        // Let's just add them.
                        boneGroup.getTransforms().addAll(rotZ, rotY, rotX);
                    }

                    if (bone.has("cubes")) {
                        JsonArray cubes = bone.getAsJsonArray("cubes");
                        for (JsonElement cubeElem : cubes) {
                            JsonObject cube = cubeElem.getAsJsonObject();
                            MeshView meshView = createCubeMesh(cube, texWidth, texHeight);
                            meshView.setMaterial(material);
                            meshView.setCullFace(javafx.scene.shape.CullFace.NONE); // Ensure visibility from all angles
                            boneGroup.getChildren().add(meshView);
                        }
                    }
                    
                    root.getChildren().add(boneGroup);
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return root;
    }

    private static MeshView createCubeMesh(JsonObject cube, int texWidth, int texHeight) {
        JsonArray origin = cube.getAsJsonArray("origin");
        double x = origin.get(0).getAsDouble();
        double y = origin.get(1).getAsDouble();
        double z = origin.get(2).getAsDouble();

        JsonArray size = cube.getAsJsonArray("size");
        double w = size.get(0).getAsDouble();
        double h = size.get(1).getAsDouble();
        double d = size.get(2).getAsDouble();
        
        float x0 = (float) x;
        float y0 = (float) -y; // Invert Y
        float z0 = (float) z;
        
        float w0 = (float) w;
        float h0 = (float) h;
        float d0 = (float) d;

        float x1 = x0 + w0;
        float y1 = y0 - h0;
        float z1 = z0 + d0;

        float[] points = {
            x1, y1, z0, // 0: Top-Right-North
            x0, y1, z0, // 1: Top-Left-North
            x0, y0, z0, // 2: Bottom-Left-North
            x1, y0, z0, // 3: Bottom-Right-North
            x1, y1, z1, // 4: Top-Right-South
            x0, y1, z1, // 5: Top-Left-South
            x0, y0, z1, // 6: Bottom-Left-South
            x1, y0, z1  // 7: Bottom-Right-South
        };

        float[] texCoords = new float[48]; // 6 faces * 4 points * 2 coords
        float tw = (float) texWidth;
        float th = (float) texHeight;

        if (cube.has("uv")) {
            JsonElement uvEl = cube.get("uv");
            if (uvEl.isJsonObject()) {
                // Per-face UV
                JsonObject uvObj = uvEl.getAsJsonObject();
                fillFaceUV(texCoords, 0, uvObj.getAsJsonObject("up"), tw, th);
                fillFaceUV(texCoords, 8, uvObj.getAsJsonObject("down"), tw, th);
                fillFaceUV(texCoords, 16, uvObj.getAsJsonObject("north"), tw, th);
                fillFaceUV(texCoords, 24, uvObj.getAsJsonObject("south"), tw, th);
                fillFaceUV(texCoords, 32, uvObj.getAsJsonObject("east"), tw, th);
                fillFaceUV(texCoords, 40, uvObj.getAsJsonObject("west"), tw, th);
            } else if (uvEl.isJsonArray()) {
                JsonArray uv = uvEl.getAsJsonArray();
                float u = uv.get(0).getAsFloat();
                float v = uv.get(1).getAsFloat();
                
                // Box UV logic
                // Standard mapping:
                // Top:    u+d, v          Size: w x d
                // Bottom: u+d+w, v        Size: w x d
                // Front:  u+d, v+d        Size: w x h
                // Back:   u+d+w+d, v+d    Size: w x h
                // Right:  u, v+d          Size: d x h
                // Left:   u+d+w, v+d      Size: d x h
                
                // Helper to fill array
                // Top (Up)
                fillBoxUV(texCoords, 0, u + d0, v, w0, d0, tw, th);
                // Bottom (Down)
                fillBoxUV(texCoords, 8, u + d0 + w0, v, w0, d0, tw, th);
                // Front (North)
                fillBoxUV(texCoords, 16, u + d0, v + d0, w0, h0, tw, th);
                // Back (South)
                fillBoxUV(texCoords, 24, u + d0 + w0 + d0, v + d0, w0, h0, tw, th);
                // Right (East)
                fillBoxUV(texCoords, 32, u, v + d0, d0, h0, tw, th);
                // Left (West)
                fillBoxUV(texCoords, 40, u + d0 + w0, v + d0, d0, h0, tw, th);
            }
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);

        int[] faces = {
            // Top
            1, 1,  0, 0,  5, 2,
            5, 2,  0, 0,  4, 3,
            
            // Bottom
            2, 5,  6, 6,  3, 4,
            3, 4,  6, 6,  7, 7,
            
            // Front
            1, 9,  2, 10,  0, 8,
            0, 8,  2, 10,  3, 11,
            
            // Back
            4, 12, 7, 15, 5, 13,
            5, 13, 7, 15, 6, 14,
            
            // Right
            0, 17, 3, 19, 4, 16,
            4, 16, 3, 19, 7, 18,
            
            // Left
            5, 20, 6, 23, 1, 21,
            1, 21, 6, 23, 2, 22
        };
        
        mesh.getFaces().addAll(faces);
        
        return new MeshView(mesh);
    }

    private static void fillFaceUV(float[] texCoords, int offset, JsonObject face, float tw, float th) {
        if (face == null || !face.has("uv") || !face.has("uv_size")) return;
        JsonArray uv = face.getAsJsonArray("uv");
        JsonArray size = face.getAsJsonArray("uv_size");
        
        float u = uv.get(0).getAsFloat();
        float v = uv.get(1).getAsFloat();
        float w = size.get(0).getAsFloat();
        float h = size.get(1).getAsFloat();
        
        // TR, TL, BL, BR
        texCoords[offset]     = (u + w) / tw; texCoords[offset+1] = v / th;
        texCoords[offset+2]   = u / tw;       texCoords[offset+3] = v / th;
        texCoords[offset+4]   = u / tw;       texCoords[offset+5] = (v + h) / th;
        texCoords[offset+6]   = (u + w) / tw; texCoords[offset+7] = (v + h) / th;
    }

    private static void fillBoxUV(float[] texCoords, int offset, float u, float v, float w, float h, float tw, float th) {
        // TR, TL, BL, BR
        texCoords[offset]     = (u + w) / tw; texCoords[offset+1] = v / th;
        texCoords[offset+2]   = u / tw;       texCoords[offset+3] = v / th;
        texCoords[offset+4]   = u / tw;       texCoords[offset+5] = (v + h) / th;
        texCoords[offset+6]   = (u + w) / tw; texCoords[offset+7] = (v + h) / th;
    }

    public static File scanForGeometry(File root, String geometryId) {
        if (root == null || !root.exists()) return null;
        File modelsDir = new File(root, "RP/models");
        if (!modelsDir.exists() || !modelsDir.isDirectory()) return null;
        return scanRecursively(modelsDir, geometryId);
    }

    private static File scanRecursively(File dir, String geometryId) {
        File[] files = dir.listFiles();
        if (files == null) return null;

        for (File f : files) {
            if (f.isDirectory()) {
                File found = scanRecursively(f, geometryId);
                if (found != null) return found;
            } else if (f.getName().toLowerCase().endsWith(".json")) {
                try {
                    JsonObject json = JsonParser.parseReader(new FileReader(f)).getAsJsonObject();
                    if (json.has("minecraft:geometry")) {
                        JsonElement geo = json.get("minecraft:geometry");
                        if (geo.isJsonArray()) {
                            for (JsonElement e : geo.getAsJsonArray()) {
                                if (e.isJsonObject() && e.getAsJsonObject().has("description")) {
                                    JsonObject desc = e.getAsJsonObject().getAsJsonObject("description");
                                    if (desc.has("identifier") && desc.get("identifier").getAsString().equals(geometryId)) {
                                        return f;
                                    }
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    // Ignore invalid files
                }
            }
        }
        return null;
    }
}
