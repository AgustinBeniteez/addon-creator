package com.agustinbenitez.addoncreator.ui;

import javafx.animation.*;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Group;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Polygon;
import javafx.scene.shape.Polyline;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.SVGPath;
import javafx.scene.shape.StrokeLineCap;
import javafx.scene.shape.StrokeLineJoin;
import javafx.util.Duration;

/**
 * Helper class to create a loading spinner that matches the specific SVG requested.
 */
public class LoadingSpinnerHelper {

    public static Node createSpinner() {
        // Create the circle to match SVG attributes
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);
        
        StackPane container = new StackPane(circle);
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);
        
        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();
        
        return container;
    }

    public static Node createTextureLoadingSpinner() {
        // Container
        StackPane container = new StackPane();
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);

        // 1. Rotating Circle (Same as standard spinner)
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        // 2. Center Icon Group (File with animation)
        javafx.scene.Group iconGroup = new javafx.scene.Group();
        
        // Path 1: File Body
        SVGPath fileBody = new SVGPath();
        fileBody.setContent("M14 2H6C4.89 2 4 2.9 4 4V20C4 21.1 4.89 22 6 22H18C19.1 22 20 21.1 20 20V8L14 2Z");
        fileBody.setFill(Color.web("#3498db"));
        
        // Path 2: Folded Corner
        SVGPath fileFold = new SVGPath();
        fileFold.setContent("M14 2V8H20");
        fileFold.setFill(Color.web("#2980b9"));
        
        // Circle: White Dot
        Circle whiteDot = new Circle(8.5, 11.5, 1.5);
        whiteDot.setFill(Color.WHITE);
        
        // Path 3: Checkmark/Arrow
        SVGPath checkMark = new SVGPath();
        checkMark.setContent("M6 19L9 15L11 17L14 13L18 19H6Z");
        checkMark.setFill(Color.WHITE);
        
        iconGroup.getChildren().addAll(fileBody, fileFold, whiteDot, checkMark);
        
        iconGroup.setScaleX(1.5);
        iconGroup.setScaleY(1.5);
        
        // Opacity Animation
        Timeline opacityTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(iconGroup.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(4), new KeyValue(iconGroup.opacityProperty(), 1.0))
        );
        opacityTimeline.setCycleCount(Animation.INDEFINITE);
        opacityTimeline.play();
        
        container.getChildren().addAll(circle, iconGroup);
        
        return container;
    }

    public static Node createSoundLoadingSpinner() {
        // Container
        StackPane container = new StackPane();
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);

        // 1. Rotating Circle
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        // 2. Center Icon Group (Musical Note)
        javafx.scene.Group iconGroup = new javafx.scene.Group();
        
        SVGPath note = new SVGPath();
        note.setContent("M12 3v10.55A4 4 0 1 0 14 17V7h4V3h-6z");
        note.setFill(Color.web("#3498db"));
        
        iconGroup.getChildren().add(note);
        
        // Transform: translate(30,28) scale(1.5)
        // Since we are in a StackPane centered at 50,50, we need to adjust.
        // The SVG viewport is 0-100.
        // If we put this group in the stackpane, it centers based on bounds.
        // The user's SVG uses translate(30,28) to position it within the 100x100 box.
        // To mimic this in JavaFX Group inside StackPane:
        // We can't rely on auto-centering if we want exact position relative to 100x100.
        // However, StackPane centers. 
        // Let's wrap in a Pane of 100x100? No, that's complex.
        // Let's use the same approach as texture spinner - just rely on scale.
        // But the texture spinner code relies on StackPane centering the group.
        // Let's verify the texture spinner alignment.
        // If I use the same logic:
        iconGroup.setScaleX(1.5);
        iconGroup.setScaleY(1.5);
        // Translate? The SVG has translate(30,28).
        // The note path is roughly 12,3 to 18,17 (width 6, height 14).
        // 30+12*1.5 = 48. 28+3*1.5 = 32.5.
        // Center is roughly 50, 40? 
        // To match SVG exactly, let's wrap in a Group that doesn't center?
        // Actually, let's just use Translate.
        // But StackPane ignores translateX/Y for layout, it centers.
        // So we can use setTranslateX/Y on the node itself.
        // But relative to what? Center.
        // SVG coords 0,0 is top-left.
        // StackPane 0,0 is center.
        // 50,50 in SVG is 0,0 in StackPane.
        // Target pos in SVG: 30,28 (top-left of group).
        // Center of group (approx): 15,10 (width 6, height 14 -> center 3, 7).
        // 30+3=33, 28+7=35.
        // 33, 35 relative to 50,50 is -17, -15.
        // So maybe setTranslateX(-5) or something?
        // Let's stick to simple scaling and maybe slight translation if it looks off.
        // The provided SVG code: <g transform="translate(30,28) scale(1.5)">
        // This puts the path at specific coordinates.
        // Let's assume standard centering is fine for now, or just apply the scale.
        
        // Opacity Animation
        Timeline opacityTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(iconGroup.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(4), new KeyValue(iconGroup.opacityProperty(), 1.0))
        );
        opacityTimeline.setCycleCount(Animation.INDEFINITE);
        opacityTimeline.play();
        
        container.getChildren().addAll(circle, iconGroup);
        
        return container;
    }

    public static Node createRecipeLoadingSpinner() {
        // Container
        StackPane container = new StackPane();
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);

        // 1. Rotating Circle
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        // 2. Center Icon Group (3x3 Grid)
        javafx.scene.Group iconGroup = new javafx.scene.Group();
        
        // 9 Rects
        for (int row = 0; row < 3; row++) {
            for (int col = 0; col < 3; col++) {
                javafx.scene.shape.Rectangle rect = new javafx.scene.shape.Rectangle(col * 10, row * 10, 8, 8);
                rect.setFill(Color.TRANSPARENT);
                rect.setStroke(Color.web("#3498db"));
                rect.setStrokeWidth(1);
                iconGroup.getChildren().add(rect);
            }
        }
        
        // Transform: translate(38,38) scale(0.8)
        iconGroup.setScaleX(0.8);
        iconGroup.setScaleY(0.8);
        
        // Opacity Animation
        Timeline opacityTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(iconGroup.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(4), new KeyValue(iconGroup.opacityProperty(), 1.0))
        );
        opacityTimeline.setCycleCount(Animation.INDEFINITE);
        opacityTimeline.play();
        
        container.getChildren().addAll(circle, iconGroup);
        
        return container;
    }

    public static Node createCodeLoadingSpinner() {
        // Container
        StackPane container = new StackPane();
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);

        // 1. Rotating Circle
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        // 2. Center Icon Group
        javafx.scene.Group iconGroup = new javafx.scene.Group();
        
        // Lines
        // Line 1: x1="0" y1="0" x2="24" y2="0"
        javafx.scene.shape.Line line1 = new javafx.scene.shape.Line(0, 0, 24, 0);
        line1.setStroke(Color.web("#3498db"));
        line1.setStrokeWidth(2);
        line1.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Line 2: x1="0" y1="6" x2="20" y2="6"
        javafx.scene.shape.Line line2 = new javafx.scene.shape.Line(0, 6, 20, 6);
        line2.setStroke(Color.web("#3498db"));
        line2.setStrokeWidth(2);
        line2.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Line 3: x1="0" y1="12" x2="16" y2="12"
        javafx.scene.shape.Line line3 = new javafx.scene.shape.Line(0, 12, 16, 12);
        line3.setStroke(Color.web("#3498db"));
        line3.setStrokeWidth(2);
        line3.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Line 4: x1="0" y1="18" x2="24" y2="18"
        javafx.scene.shape.Line line4 = new javafx.scene.shape.Line(0, 18, 24, 18);
        line4.setStroke(Color.web("#3498db"));
        line4.setStrokeWidth(2);
        line4.setStrokeLineCap(StrokeLineCap.ROUND);
        
        // Text {/}
        javafx.scene.text.Text text = new javafx.scene.text.Text(26, 18, "{/}");
        text.setFont(javafx.scene.text.Font.font("Arial", 10));
        text.setFill(Color.web("#3498db"));

        iconGroup.getChildren().addAll(line1, line2, line3, line4, text);
        
        // Transform: translate(30,35) scale(1.2)
        // Adjusting Y to match visual placement relative to center
        iconGroup.setScaleX(1.2);
        iconGroup.setScaleY(1.2);
        iconGroup.setTranslateY(-6); 
        
        // Opacity Animation
        Timeline opacityTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(iconGroup.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(4), new KeyValue(iconGroup.opacityProperty(), 1.0))
        );
        opacityTimeline.setCycleCount(Animation.INDEFINITE);
        opacityTimeline.play();
        
        container.getChildren().addAll(circle, iconGroup);
        
        return container;
    }
    
    public static Node createEntityLoadingSpinner() {
        // Container
        StackPane container = new StackPane();
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);

        // 1. Rotating Circle
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        // 2. Person Icon
        Group iconGroup = new Group();

        // Head
        Rectangle head = new Rectangle(44, 28, 12, 12);
        head.setArcWidth(2);
        head.setArcHeight(2);
        head.setFill(Color.web("#3498db"));

        // Body
        Rectangle body = new Rectangle(42, 42, 16, 22);
        body.setArcWidth(3);
        body.setArcHeight(3);
        body.setFill(Color.web("#3498db"));

        // Left Arm
        Rectangle leftArm = new Rectangle(36, 44, 4, 16);
        leftArm.setArcWidth(2);
        leftArm.setArcHeight(2);
        leftArm.setFill(Color.web("#3498db"));

        // Right Arm
        Rectangle rightArm = new Rectangle(60, 44, 4, 16);
        rightArm.setArcWidth(2);
        rightArm.setArcHeight(2);
        rightArm.setFill(Color.web("#3498db"));

        // Left Leg
        Rectangle leftLeg = new Rectangle(44, 66, 5, 12);
        leftLeg.setArcWidth(2);
        leftLeg.setArcHeight(2);
        leftLeg.setFill(Color.web("#3498db"));

        // Right Leg
        Rectangle rightLeg = new Rectangle(51, 66, 5, 12);
        rightLeg.setArcWidth(2);
        rightLeg.setArcHeight(2);
        rightLeg.setFill(Color.web("#3498db"));

        iconGroup.getChildren().addAll(head, body, leftArm, rightArm, leftLeg, rightLeg);

        // Opacity Animation
        Timeline opacityTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(iconGroup.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(4), new KeyValue(iconGroup.opacityProperty(), 1.0))
        );
        opacityTimeline.setCycleCount(Animation.INDEFINITE);
        opacityTimeline.play();

        container.getChildren().addAll(circle, iconGroup);

        return container;
    }

    public static Node create3DModelLoadingSpinner() {
        // Container
        StackPane container = new StackPane();
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);

        // 1. Rotating Circle
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        // 2. 3D Cube Icon
        Group iconGroup = new Group();

        // Top Face
        Polygon topFace = new Polygon(
            0.0, -14.0,
            14.0, -7.0,
            0.0, 0.0,
            -14.0, -7.0
        );
        topFace.setFill(Color.web("#3498db"));
        topFace.setOpacity(0.9);

        // Left Face
        Polygon leftFace = new Polygon(
            -14.0, -7.0,
            0.0, 0.0,
            0.0, 18.0,
            -14.0, 11.0
        );
        leftFace.setFill(Color.web("#3498db"));
        leftFace.setOpacity(0.7);

        // Right Face
        Polygon rightFace = new Polygon(
            14.0, -7.0,
            0.0, 0.0,
            0.0, 18.0,
            14.0, 11.0
        );
        rightFace.setFill(Color.web("#3498db"));
        rightFace.setOpacity(0.5);

        iconGroup.getChildren().addAll(topFace, leftFace, rightFace);

        // Opacity Animation
        Timeline opacityTimeline = new Timeline(
            new KeyFrame(Duration.ZERO, new KeyValue(iconGroup.opacityProperty(), 1.0)),
            new KeyFrame(Duration.seconds(1), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(3), new KeyValue(iconGroup.opacityProperty(), 0.0)),
            new KeyFrame(Duration.seconds(4), new KeyValue(iconGroup.opacityProperty(), 1.0))
        );
        opacityTimeline.setCycleCount(Animation.INDEFINITE);
        opacityTimeline.play();

        container.getChildren().addAll(circle, iconGroup);

        return container;
    }

    public static Node createLoadingOverlay(String message, String type) {
        Node spinner;
        switch (type.toLowerCase()) {
            case "sounds":
            case "sonidos":
                spinner = createSoundLoadingSpinner();
                break;
            case "recipes":
            case "recetas":
            case "crafteos":
                spinner = createRecipeLoadingSpinner();
                break;
            case "code":
            case "codigo":
            case "script":
            case "scripts":
                spinner = createCodeLoadingSpinner();
                break;
            case "entities":
            case "entidades":
            case "entity":
            case "entidad":
                spinner = createEntityLoadingSpinner();
                break;
            case "models3d":
            case "modelos3d":
            case "3dmodels":
            case "3dmodel":
                spinner = create3DModelLoadingSpinner();
                break;
            case "textures":
            case "texturas":
            case "models":
            case "modelos":
            default:
                spinner = createTextureLoadingSpinner();
                break;
        }
        return createOverlayInternal(message, spinner);
    }

    public static Node createDownloadSpinner() {
        // Container
        StackPane container = new StackPane();
        container.setMinSize(100, 100);
        container.setPrefSize(100, 100);
        container.setMaxSize(100, 100);

        // 1. Rotating Circle (Same as standard spinner)
        Circle circle = new Circle(40);
        circle.setFill(Color.TRANSPARENT);
        circle.setStroke(Color.web("#3498db"));
        circle.setStrokeWidth(8);
        circle.setStrokeLineCap(StrokeLineCap.ROUND);
        circle.getStrokeDashArray().addAll(62.8, 188.4);

        RotateTransition rotate = new RotateTransition(Duration.seconds(1), circle);
        rotate.setByAngle(360);
        rotate.setCycleCount(Animation.INDEFINITE);
        rotate.setInterpolator(Interpolator.LINEAR);
        rotate.play();

        // 2. Bouncing Arrow (Polyline)
        Polyline arrow = new Polyline(
            50.0, 30.0,
            50.0, 60.0,
            43.0, 53.0,
            50.0, 60.0,
            57.0, 53.0
        );
        arrow.setFill(Color.TRANSPARENT);
        arrow.setStroke(Color.web("#3498db"));
        arrow.setStrokeWidth(6);
        arrow.setStrokeLineCap(StrokeLineCap.ROUND);
        arrow.setStrokeLineJoin(StrokeLineJoin.ROUND);

        // Translate Animation
        TranslateTransition bounce = new TranslateTransition(Duration.seconds(1), arrow);
        bounce.setFromY(0);
        bounce.setToY(5);
        bounce.setAutoReverse(true);
        bounce.setCycleCount(Animation.INDEFINITE);
        bounce.play();
        
        Group svgGroup = new Group(circle, arrow);
        circle.setCenterX(50);
        circle.setCenterY(50);
        
        container.getChildren().add(svgGroup);
        
        return container;
    }

    public static Node createOverlay(String message) {
        return createOverlayInternal(message, createSpinner());
    }

    public static Node createDownloadOverlay(String message) {
        return createOverlayInternal(message, createDownloadSpinner());
    }

    public static Node createFileLoadingOverlay(String message) {
        return createOverlayInternal(message, createTextureLoadingSpinner());
    }

    private static Node createOverlayInternal(String message, Node spinner) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(30, 30, 30, 0.8);");
        
        VBox content = new VBox(20);
        content.setAlignment(Pos.CENTER);
        
        Label label = new Label(message);
        label.setStyle("-fx-text-fill: white; -fx-font-size: 14px; -fx-font-weight: bold;");
        
        content.getChildren().addAll(spinner, label);
        overlay.getChildren().add(content);
        
        return overlay;
    }
}
