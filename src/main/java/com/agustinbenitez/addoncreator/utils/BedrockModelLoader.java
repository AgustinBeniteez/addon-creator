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
        Group root = new Group();
        try {
            JsonObject jsonRoot = JsonParser.parseReader(new FileReader(jsonFile)).getAsJsonObject();
            JsonObject geometry = null;

            // Handle "minecraft:geometry" array
            if (jsonRoot.has("minecraft:geometry")) {
                JsonElement geo = jsonRoot.get("minecraft:geometry");
                if (geo.isJsonArray()) {
                    geometry = geo.getAsJsonArray().get(0).getAsJsonObject();
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
        
        // JavaFX Y is down, MC Y is up.
        // We render relative to 0,0,0 and let the scene transform handle it?
        // Or we transform here.
        // Let's transform coordinates to match JavaFX 3D space where Y is down.
        // MC: (x, y, z) -> JFX: (x, -y, z)
        // Also the cube "origin" in MC is the corner with lowest coordinates.
        // In JFX, we build the mesh.

        float x0 = (float) x;
        float y0 = (float) -y; // Invert Y
        float z0 = (float) z;
        
        float w0 = (float) w;
        float h0 = (float) h; // Height goes UP in MC, so in JFX it goes UP (negative Y)
        float d0 = (float) d;

        // Vertices
        // MC Y is Up. Origin is bottom-left-north.
        // So top-right-south is (x+w, y+h, z+d)
        
        // JFX Y is Down.
        // y_mc = y -> y_jfx = -y
        // y_mc_top = y + h -> y_jfx_top = -(y + h) = -y - h
        
        // So JFX Y range is [-y, -y-h]
        // y0 = -y (Bottom in MC, "Top" in JFX value wise but lower visually)
        // y1 = -y - h (Top in MC, "Higher" in JFX value wise)
        
        float x1 = x0 + w0;
        float y1 = y0 - h0; // h is positive, so we subtract to go "Up" visually (negative Y)
        float z1 = z0 + d0;

        //   5-------4    y1 (Top in MC)
        //  /|      /|
        // 1-------0 |    y1
        // | 6-----|-7    y0 (Bottom in MC)
        // |/      |/
        // 2-------3      y0
        
        // Let's define 8 vertices
        float[] points = {
            x1, y1, z0, // 0: Top-Right-North (MC: x+w, y+h, z)
            x0, y1, z0, // 1: Top-Left-North  (MC: x,   y+h, z)
            x0, y0, z0, // 2: Bottom-Left-North (MC: x, y, z)
            x1, y0, z0, // 3: Bottom-Right-North (MC: x+w, y, z)
            x1, y1, z1, // 4: Top-Right-South
            x0, y1, z1, // 5: Top-Left-South
            x0, y0, z1, // 6: Bottom-Left-South
            x1, y0, z1  // 7: Bottom-Right-South
        };

        // Texture Coordinates
        // Bedrock uses Box UV: [u, v] (top-left of the texture region)
        // Layout:
        // Top:    (d, w) at (u+d, v)
        // Bottom: (d, w) at (u+d+w, v) -> Wait, standard MC layout is different.
        
        // Standard Box UV Layout (Reference: Blockbench/Wiki)
        // x, y = origin of UV block
        // Top:    x + d,      y
        // Bottom: x + d + w,  y
        // Right:  x,          y + d
        // Front:  x + d,      y + d
        // Left:   x + d + w,  y + d
        // Back:   x + 2d + 2w, y + d (or something like that)
        
        // Let's check the array "uv"
        float[] texCoords = new float[0];
        
        if (cube.has("uv")) {
            JsonElement uvEl = cube.get("uv");
            if (uvEl.isJsonArray()) {
                JsonArray uv = uvEl.getAsJsonArray();
                float u = uv.get(0).getAsFloat();
                float v = uv.get(1).getAsFloat();
                
                // Box UV logic
                // North (Front), East (Right), South (Back), West (Left), Up (Top), Down (Bottom)
                // Sizes:
                // Front: w x h
                // Right: d x h
                // Back:  w x h
                // Left:  d x h
                // Top:   w x d
                // Bottom: w x d
                
                // Standard mapping:
                // Right  (d x h): u,                     v + d
                // Front  (w x h): u + d,                 v + d
                // Left   (d x h): u + d + w,             v + d
                // Back   (w x h): u + d + w + d,         v + d
                // Top    (w x d): u + d,                 v
                // Bottom (w x d): u + d + w,             v
                
                // We need to normalize by texture size
                float tw = (float) texWidth;
                float th = (float) texHeight;
                
                // Define 4 corners for each face
                // North (Front) - Face 3-2-1-0? No, 2-3-0-1 (CounterClockwise)
                // Vertices: 2(BL), 3(BR), 0(TR), 1(TL) -> Z- face?
                // Wait, Z0 is North?
                // MC: +Z is South. So Z0 is North. Z1 is South.
                // Face North is at Z0. Vertices: 1, 0, 3, 2
                
                // Let's helper function to create UV coords
                // We need 4 UV points per face to allow independent mapping
                
                // But TriangleMesh uses a list of texCoords and faces reference them by index.
                // Since faces share vertices but NOT necessarily UVs (seams), we often duplicate vertices or just use independent UV indices.
                // JavaFX TriangleMesh supports separate indices for points and texCoords.
                
                // Let's generate specific UVs for each face
                
                // Top (Up) - w x d
                // u_start = u + d, v_start = v
                // TL=(0,0), TR=(w,0), BL=(0,d), BR=(w,d)
                float u_top = u + d0;
                float v_top = v;
                
                // Bottom (Down) - w x d
                // u_start = u + d + w0, v_start = v
                float u_bot = u + d0 + w0;
                float v_bot = v;
                
                // Right - d x h
                // u_start = u, v_start = v + d0
                float u_right = u;
                float v_right = v + d0;
                
                // Front - w x h
                // u_start = u + d0, v_start = v + d0
                float u_front = u + d0;
                float v_front = v + d0;
                
                // Left - d x h
                // u_start = u + d0 + w0, v_start = v + d0
                float u_left = u + d0 + w0;
                float v_left = v + d0;
                
                // Back - w x h
                // u_start = u + d0 + w0 + d0, v_start = v + d0
                float u_back = u + d0 + w0 + d0;
                float v_back = v + d0;

                texCoords = new float[] {
                    // Top (Up)
                    (u_top + w0)/tw, v_top/th,      // TR
                    u_top/tw, v_top/th,             // TL
                    u_top/tw, (v_top + d0)/th,      // BL
                    (u_top + w0)/tw, (v_top + d0)/th, // BR

                    // Bottom (Down)
                    (u_bot + w0)/tw, v_bot/th,      // TR
                    u_bot/tw, v_bot/th,             // TL
                    u_bot/tw, (v_bot + d0)/th,      // BL
                    (u_bot + w0)/tw, (v_bot + d0)/th, // BR

                    // Front (North, Z0)
                    (u_front + w0)/tw, v_front/th,      // TR
                    u_front/tw, v_front/th,             // TL
                    u_front/tw, (v_front + h0)/th,      // BL
                    (u_front + w0)/tw, (v_front + h0)/th, // BR

                    // Back (South, Z1)
                    (u_back + w0)/tw, v_back/th,      // TR
                    u_back/tw, v_back/th,             // TL
                    u_back/tw, (v_back + h0)/th,      // BL
                    (u_back + w0)/tw, (v_back + h0)/th, // BR

                    // Right (East, X1)
                    (u_right + d0)/tw, v_right/th,      // TR
                    u_right/tw, v_right/th,             // TL
                    u_right/tw, (v_right + h0)/th,      // BL
                    (u_right + d0)/tw, (v_right + h0)/th, // BR

                    // Left (West, X0)
                    (u_left + d0)/tw, v_left/th,      // TR
                    u_left/tw, v_left/th,             // TL
                    u_left/tw, (v_left + h0)/th,      // BL
                    (u_left + d0)/tw, (v_left + h0)/th  // BR
                };

            } else {
                 // Per-face UV
                 // Simplified fallback or implementation
                 texCoords = new float[48]; // 6 faces * 4 points * 2 coords
            }
        }

        TriangleMesh mesh = new TriangleMesh();
        mesh.setVertexFormat(VertexFormat.POINT_TEXCOORD);
        mesh.getPoints().addAll(points);
        mesh.getTexCoords().addAll(texCoords);

        // Faces
        // Format: p0, t0, p1, t1, p2, t2 ...
        // We defined 8 points (0-7)
        // We defined 24 texCoords (0-23) (4 per face * 6 faces)
        
        // Indices for texCoords:
        // Top: 0, 1, 2, 3
        // Bottom: 4, 5, 6, 7
        // Front: 8, 9, 10, 11
        // Back: 12, 13, 14, 15
        // Right: 16, 17, 18, 19
        // Left: 20, 21, 22, 23
        
        // Face definition (CCW winding usually)
        
        // Top Face (y1): 1, 0, 4, 5
        // Normals pointing up?
        // 1(TLN), 0(TRN), 4(TRS), 5(TLS)
        // Texture: 1(TL), 0(TR), 3(BR), 2(BL)? No, mapped to corners.
        // Tex: 1(TL), 0(TR), 3(BR), 2(BL) - wait my tex order was TR, TL, BL, BR
        // 0: TR, 1: TL, 2: BL, 3: BR
        
        // Quad 1-0-4-5
        // Tri 1: 1, 0, 5
        // Tri 2: 5, 0, 4
        // Tex 1: 1(TL), 0(TR), 2(BL) ? No
        // Let's map corners to texture corners
        // Point 1 (TLN) -> Tex 1 (TL)
        // Point 0 (TRN) -> Tex 0 (TR)
        // Point 5 (TLS) -> Tex 2 (BL)
        // Point 4 (TRS) -> Tex 3 (BR)
        
        // Top Face indices:
        // 1, 1,  0, 0,  5, 2
        // 5, 2,  0, 0,  4, 3
        
        // Bottom Face (y0): 2, 3, 7, 6 (Looking from bottom)
        // 2(BLN), 3(BRN), 7(BRS), 6(BLS)
        // Tex: 5(TL), 4(TR), 6(BL), 7(BR)
        // Point 2 (BLN) -> Tex 5 (TL)
        // Point 3 (BRN) -> Tex 4 (TR)
        // Point 6 (BLS) -> Tex 6 (BL)
        // Point 7 (BRS) -> Tex 7 (BR)
        // Order: 2-6-3, 3-6-7
        // 2, 5,  6, 6,  3, 4
        // 3, 4,  6, 6,  7, 7
        
        // Front Face (Z0): 1, 0, 3, 2
        // 1(TLN), 0(TRN), 3(BRN), 2(BLN)
        // Tex: 9(TL), 8(TR), 10(BL), 11(BR)
        // 1, 9,  2, 10,  0, 8
        // 0, 8,  2, 10,  3, 11
        
        // Back Face (Z1): 4, 5, 6, 7
        // 4(TRS), 5(TLS), 6(BLS), 7(BRS)
        // Tex: 13(TL), 12(TR), 14(BL), 15(BR)
        // 4, 12,  7, 15,  5, 13
        // 5, 13,  7, 15,  6, 14
        
        // Right Face (X1): 0, 4, 7, 3
        // 0(TRN), 4(TRS), 7(BRS), 3(BRN)
        // Tex: 17(TL), 16(TR), 18(BL), 19(BR)
        // 0, 17,  3, 19,  4, 16
        // 4, 16,  3, 19,  7, 18
        
        // Left Face (X0): 5, 1, 2, 6
        // 5(TLS), 1(TLN), 2(BLN), 6(BLS)
        // Tex: 21(TL), 20(TR), 22(BL), 23(BR)
        // 5, 20,  6, 23,  1, 21
        // 1, 21,  6, 23,  2, 22
        
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
}
