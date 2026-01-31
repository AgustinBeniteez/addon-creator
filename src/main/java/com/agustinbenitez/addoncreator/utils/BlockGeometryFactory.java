package com.agustinbenitez.addoncreator.utils;

import javafx.scene.Group;
import javafx.scene.image.Image;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;

public class BlockGeometryFactory {

    public static Group createCubeAll(double size, Image texture) {
        Group group = new Group();
        Box box = new Box(size, size, size);
        PhongMaterial material = new PhongMaterial();
        if (texture != null) {
            material.setDiffuseMap(texture);
        } else {
            material.setDiffuseColor(Color.GRAY);
        }
        box.setMaterial(material);
        group.getChildren().add(box);
        return group;
    }

    public static Group createCubeSided(double size, Image top, Image bottom, Image side) {
        return createCubeSix(size, top, bottom, side, side, side, side);
    }

    public static Group createCubeSix(double size, Image up, Image down, Image north, Image south, Image east, Image west) {
        Group group = new Group();
        double half = size / 2;

        // Up (Top)
        Box boxUp = new Box(size, 0.1, size);
        boxUp.setTranslateY(-half);
        boxUp.setMaterial(createMaterial(up));

        // Down (Bottom)
        Box boxDown = new Box(size, 0.1, size);
        boxDown.setTranslateY(half);
        boxDown.setMaterial(createMaterial(down));

        // North
        Box boxNorth = new Box(size, size, 0.1);
        boxNorth.setTranslateZ(half);
        boxNorth.setMaterial(createMaterial(north));

        // South
        Box boxSouth = new Box(size, size, 0.1);
        boxSouth.setTranslateZ(-half);
        boxSouth.setMaterial(createMaterial(south));

        // East
        Box boxEast = new Box(0.1, size, size);
        boxEast.setTranslateX(half);
        boxEast.setMaterial(createMaterial(east));

        // West
        Box boxWest = new Box(0.1, size, size);
        boxWest.setTranslateX(-half);
        boxWest.setMaterial(createMaterial(west));

        group.getChildren().addAll(boxUp, boxDown, boxNorth, boxSouth, boxEast, boxWest);
        return group;
    }

    private static PhongMaterial createMaterial(Image texture) {
        PhongMaterial mat = new PhongMaterial(Color.GRAY);
        if (texture != null) {
            mat.setDiffuseMap(texture);
        }
        return mat;
    }
}
