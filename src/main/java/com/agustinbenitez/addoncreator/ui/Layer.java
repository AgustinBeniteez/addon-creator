package com.agustinbenitez.addoncreator.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.image.WritableImage;

public class Layer {
    private final StringProperty name = new SimpleStringProperty();
    private final BooleanProperty visible = new SimpleBooleanProperty(true);
    private WritableImage image;

    public Layer(String name, int width, int height) {
        this.name.set(name);
        this.image = new WritableImage(width, height);
    }

    public String getName() {
        return name.get();
    }

    public StringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public boolean isVisible() {
        return visible.get();
    }

    public BooleanProperty visibleProperty() {
        return visible;
    }

    public void setVisible(boolean visible) {
        this.visible.set(visible);
    }

    public WritableImage getImage() {
        return image;
    }

    public void setImage(WritableImage image) {
        this.image = image;
    }
    
    @Override
    public String toString() {
        return getName();
    }
}
