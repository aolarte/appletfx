/*
 * Based on the JavaFX tutorial: 
 * https://docs.oracle.com/javase/8/javafx/graphics-tutorial/javafx-3d-graphics.htm
 */
package com.andresolarte.javafx.applet;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.logging.Level;
import java.util.logging.Logger;
import javafx.animation.AnimationTimer;
import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.embed.swing.JFXPanel;
import javafx.scene.*;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Box;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.Sphere;
import javafx.scene.text.Font;
import javafx.scene.text.FontSmoothingType;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.transform.Rotate;
import javax.swing.JApplet;

/**
 *
 * @author aolarte
 */
public class ConstructionApplet extends JApplet {

    private static final int JFXPANEL_WIDTH_INT = 300;
    private static final int JFXPANEL_HEIGHT_INT = 250;
    private static final double ROTATION_INTERVAL = 30000000; //Around 30fps
    private static final double TEXT_UPDATE_INTERVAL = 5 * 1000000000L; //Every 5 sec
    private static final double CAMERA_INITIAL_DISTANCE = -450;
    private static final double CAMERA_INITIAL_X_ANGLE = 70.0;
    private static final double CAMERA_INITIAL_Y_ANGLE = 320.0;
    private static final double CAMERA_NEAR_CLIP = 1;
    private static final double CAMERA_FAR_CLIP = 10000.0;
    private static final double AXIS_LENGTH = 300.0;
    private static final double MOUSE_SPEED = 0.1;
    private static final double ROTATION_SPEED = 2.0;
    private static final double AUTO_X_ROTATION = 0.4;
    private static final double AUTO_Y_ROTATION = 0.2;
    private static JFXPanel fxContainer;
    private final Group root = new Group();
    private final Xform axisGroup = new Xform();
    private final Xform world = new Xform();
    private final PerspectiveCamera camera = new PerspectiveCamera(true);
    private final Xform cameraXform = new Xform();
    private final Xform cameraXform2 = new Xform();
    private final Xform cameraXform3 = new Xform();
    private final String[] texts=new String[]{"Page\nunder\nConstruction","Página\nen\nConstrucción","Page\nen\nConstruction","Seite\n\nim    Aufbau"};
    private boolean buttonPressed = false;
    private double mousePosX;
    private double mousePosY;
    private double mouseOldX;
    private double mouseOldY;
    private double mouseDeltaX;
    private double mouseDeltaY;
    private Text text;
    private int textIndex=0;

    @Override
    public void init() {
        fxContainer = new JFXPanel();
        fxContainer.setPreferredSize(new Dimension(JFXPANEL_WIDTH_INT, JFXPANEL_HEIGHT_INT));
        add(fxContainer, BorderLayout.CENTER);
        // create JavaFX scene
        Platform.runLater(this::createScene);
    }

    private void createScene() {

        root.getChildren().add(world);
        root.setDepthTest(DepthTest.ENABLE);

        buildCamera();
        buildAxes();
        buildText();

        Scene scene = new Scene(root, 1024, 768, true);
        scene.setFill(Color.GREY);
        addKeyboardHandlers(scene);
        addMouseHandlers(scene);

        scene.setCamera(camera);

        Platform.setImplicitExit(false);
        fxContainer.setScene(scene);

        new AnimationTimer() {
            long lastRotate =0;

            long lastTextUpdate=0;

            @Override
            public void handle(long now) {
                if (lastRotate==0) {
                    lastRotate=now;
                    lastTextUpdate=now;
                }
                long diff = now - lastRotate;
                if (diff > ROTATION_INTERVAL) {
                    if (!buttonPressed) {
                        double factor = diff / ROTATION_INTERVAL;
                        lastRotate = now;
                        double newX = (cameraXform.ry.getAngle() + (AUTO_X_ROTATION * factor)) % 360;
                        double newY = (cameraXform.rx.getAngle() + (AUTO_Y_ROTATION * factor)) % 360;
                        cameraXform.ry.setAngle(newX);
                        cameraXform.rx.setAngle(newY);
                    }
                    lastRotate = now;
                }

                long textDiff = now - lastTextUpdate;
                if (textDiff > TEXT_UPDATE_INTERVAL ) {
                    rotateText();
                    lastTextUpdate=now;
                }

            }
        }.start();
    }

    private void buildCamera() {
        root.getChildren().add(cameraXform);
        cameraXform.getChildren().add(cameraXform2);
        cameraXform2.getChildren().add(cameraXform3);
        cameraXform3.getChildren().add(camera);
        cameraXform3.setRotateZ(180.0);

        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    private void buildText() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKORANGE);
        redMaterial.setSpecularColor(Color.ORANGE);

        text = new Text();
        text.setText(texts[textIndex]);
        text.setTextAlignment(TextAlignment.CENTER);

        Font font = new Font(18);
        text.setFill(Color.YELLOW);
        text.setFont(font);

        Xform textXform = new Xform();
        textXform.getChildren().add(text);
        textXform.setTx(-50);
        textXform.setTy(-25);

        Box box = new Box(200, 200, 1);
        box.setMaterial(redMaterial);

        PhongMaterial black = new PhongMaterial();
        black.setDiffuseColor(Color.BLACK);
        black.setSpecularColor(Color.SILVER);

        Box border1 = new Box(190, 10, 2);
        Xform border1Xform = new Xform();
        border1Xform.getChildren().add(border1);
        border1Xform.setTy(90);

        Box border2 = new Box(190, 10, 2);
        Xform border2Xform = new Xform();
        border2Xform.getChildren().add(border2);
        border2Xform.setTy(-90);

        Box border3 = new Box(10, 190, 2);
        Xform border3Xform = new Xform();
        border3Xform.getChildren().add(border3);
        border3Xform.setTx(90);

        Box border4 = new Box(10, 190, 2);
        Xform border4Xform = new Xform();
        border4Xform.getChildren().add(border4);
        border4Xform.setTx(-90);

        Xform boxXform = new Xform();
        boxXform.getChildren().add(box);
        boxXform.getChildren().add(border1Xform);
        boxXform.getChildren().add(border2Xform);
        boxXform.getChildren().add(border3Xform);
        boxXform.getChildren().add(border4Xform);
        boxXform.setRz(45);
        boxXform.setTz(2);

        Xform textGroup = new Xform();

        textGroup.getChildren().add(textXform);
        textGroup.getChildren().add(boxXform);

        world.getChildren().addAll(textGroup);

    }

    private void buildAxes() {
        final PhongMaterial redMaterial = new PhongMaterial();
        redMaterial.setDiffuseColor(Color.DARKRED);
        redMaterial.setSpecularColor(Color.RED);

        final PhongMaterial greenMaterial = new PhongMaterial();
        greenMaterial.setDiffuseColor(Color.DARKGREEN);
        greenMaterial.setSpecularColor(Color.GREEN);

        final PhongMaterial blueMaterial = new PhongMaterial();
        blueMaterial.setDiffuseColor(Color.DARKBLUE);
        blueMaterial.setSpecularColor(Color.BLUE);

        final Box xAxis = new Box(AXIS_LENGTH, 1, 1);
        final Box yAxis = new Box(1, AXIS_LENGTH, 1);
        final Box zAxis = new Box(1, 1, AXIS_LENGTH);

        xAxis.setMaterial(redMaterial);
        yAxis.setMaterial(greenMaterial);
        zAxis.setMaterial(blueMaterial);

        axisGroup.getChildren().addAll(xAxis, yAxis, zAxis);
        axisGroup.setVisible(false);
        world.getChildren().addAll(axisGroup);
    }

    private void addMouseHandlers(Scene scene) {
        scene.setOnMousePressed((MouseEvent me) -> {
            buttonPressed = true;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });
        scene.setOnMouseReleased(me -> buttonPressed = false);

        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseOldX = mousePosX;
            mouseOldY = mousePosY;
            mousePosX = me.getSceneX();
            mousePosY = me.getSceneY();
            mouseDeltaX = (mousePosX - mouseOldX);
            mouseDeltaY = (mousePosY - mouseOldY);

            double modifier = 1.0;

            if (me.isPrimaryButtonDown()) {
                cameraXform.ry.setAngle(cameraXform.ry.getAngle() - mouseDeltaX * MOUSE_SPEED * modifier * ROTATION_SPEED);
                cameraXform.rx.setAngle(cameraXform.rx.getAngle() + mouseDeltaY * MOUSE_SPEED * modifier * ROTATION_SPEED);
            }

        });
    }

    private void resetCamera() {
        cameraXform2.t.setX(0.0);
        cameraXform2.t.setY(0.0);
        camera.setTranslateZ(CAMERA_INITIAL_DISTANCE);
        cameraXform.ry.setAngle(CAMERA_INITIAL_Y_ANGLE);
        cameraXform.rx.setAngle(CAMERA_INITIAL_X_ANGLE);
    }

    private void addKeyboardHandlers(Scene scene) {
        scene.setOnKeyPressed((KeyEvent event) -> {
            switch (event.getCode()) {
                case Z:
                    resetCamera();
                    break;
                case X:
                    axisGroup.setVisible(!axisGroup.isVisible());
                    break;
                case L:
                    rotateText();
                    break;
            }
        });
    }



    private void rotateText() {
        textIndex=(textIndex+1)%texts.length;
        text.setText(texts[textIndex]);
    }
}
