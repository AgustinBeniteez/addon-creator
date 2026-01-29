package com.agustinbenitez.addoncreator.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.StrokeLineCap;
import javafx.util.Duration;

/**
 * Helper class to create a loading spinner that matches the specific SVG requested.
 * SVG Reference:
 * <svg width="100" height="100" viewBox="0 0 100 100" xmlns="http://www.w3.org/2000/svg">
 *   <circle cx="50" cy="50" r="40" stroke="#3498db" stroke-width="8" fill="none" 
 *           stroke-linecap="round" stroke-dasharray="62.8 188.4">
 *     <animateTransform ... type="rotate" ... />
 *   </circle>
 * </svg>
 */
public class LoadingSpinnerHelper {

    public static Node createSpinner() {
        // Create the circle to match SVG attributes
        // SVG: cx=50, cy=50, r=40 -> Diameter 80 + stroke 8 = 88-100 size.
        // We can scale it down if needed, but let's stick to these proportions.
        
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // stroke-dasharray="62.8 188.4"
        // This creates a gap. 62.8 is length of dash, 188.4 is length of gap.
        circle.getStrokeDashArray().addAll(62.8, 188.4);
        
        // Center the circle in a container that matches the SVG viewBox 100x100
        StackPane container = new StackPane(circle);
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);
        
        // Animation: rotate 360 degrees indefinitely
        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
        
        return container;
    }

    public static Node createOverlay(String message) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(30, 30, 30, 0.8);"); // Dark semi-transparent background
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        Node spinner = createSpinner();
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        content.getChildren().addAll(spinner, label);
        overlay.getChildren().add(content);
        
        return overlay;
    }
}
