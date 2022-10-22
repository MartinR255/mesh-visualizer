package com.code.mesh_visualizer;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.shape.Line;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import java.util.ArrayList;
import java.util.List;


public class Main extends Application {
    private Group group;
    private Scene layout_scene;
    private Pane main_scene;
    private Stage stage;

    private BorderPane layout;
    private String css_style_file;

    private double width, height;
    private final double WIDTH_SCREEN_ADJUST_CONSTANT = 0.75, HEIGHT_SCREEN_ADJUST_CONSTANT = 0.7;
    private  Mash mash;
    double initialScaling;

    //*************************
    Mat4 objectTransformationMatrix;

    @Override
    public void start(Stage stage) throws Exception {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        width = screenBounds.getWidth() * WIDTH_SCREEN_ADJUST_CONSTANT;
        height = screenBounds.getHeight() * HEIGHT_SCREEN_ADJUST_CONSTANT;

        this.stage = stage;
        group = new Group();
        main_scene = new Pane();
        mash = new Mash();

        // set up border pane layout
        layout = new BorderPane();
        layout.setCenter(main_scene);
        createTopActionPanel();
        createRightActionPanel();


        layout_scene = new Scene(layout, width, height);
        stage.setTitle("Mash Visualizer");
        stage.setScene(layout_scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }

    public void paintScene() {
        main_scene.getChildren().clear();
        Mat4 transformationMatrix = createProjectionMatrix();

        for (Face face : mash.getFaces()) {
            List<Vec4> facePoints = face.getPoints();

            Vec4 pointAV = Transformations.multiply(transformationMatrix, facePoints.get(0));
            Vec4 pointBV = Transformations.multiply(transformationMatrix, facePoints.get(1));
            Vec4 pointCV = Transformations.multiply(transformationMatrix, facePoints.get(2));

            Double[] pointA = { pointAV.getVec4().get(0), pointAV.getVec4().get(1) };
            Double[] pointB = { pointBV.getVec4().get(0), pointBV.getVec4().get(1) };
            Double[] pointC = { pointCV.getVec4().get(0), pointCV.getVec4().get(1) };

            Line AB = new Line(pointA[0], pointA[1], pointB[0], pointB[1]);
            Line BC = new Line(pointB[0], pointB[1], pointC[0], pointC[1]);
            Line CA = new Line(pointC[0], pointC[1], pointA[0], pointA[1]);

            main_scene.getChildren().addAll(AB, BC, CA);
        }
    }

    public void clearScreen() {
        main_scene.getChildren().clear();
        mash.clearMash();
        objectTransformationMatrix = new Mat4();
        resetSlicerValuesToDefault();
    }

    private HBox topActionPanel;
    private Button fileExplorerButton, clearScreenButton;
    private FileChooser fileExplorer;
    private void createTopActionPanel() {
        topActionPanel = new HBox();
        topActionPanel.getStylesheets().add(
                this.getClass().getResource("/mesh_visualizer/css/main_style.css").toExternalForm());
        topActionPanel.getStyleClass().add("hbox");
        topActionPanel.setAlignment(Pos.CENTER_LEFT);
        topActionPanel.setSpacing(45);

        // setup buttons
        fileExplorerButton = new Button("Choose File");
        clearScreenButton = new Button("Clear");

        topActionPanel.getChildren().addAll(fileExplorerButton, clearScreenButton);

        fileExplorer = new FileChooser();
        fileExplorerButton.setOnAction(event -> {
            File file = fileExplorer.showOpenDialog(stage);
            if (file != null) {
                clearScreen();
                mash.createMash(file.getAbsolutePath());
                paintScene();
            }
        });

        clearScreenButton.setOnAction(event -> {
            clearScreen();
            paintScene();
        });

        layout.setTop(topActionPanel);
    }

    private AnchorPane rightActionPanel;
    private Slider translateSliderX, translateSliderY, translateSliderZ;
    private Slider rotateSliderX, rotateSliderY, rotateSliderZ;
    private Slider scaleSlider;
    private Slider lightDirectionSliderX, lightDirectionSliderY, lightDirectionSliderZ;
    private TextField translateSliderXText, translateSliderYText, translateSliderZText;
    private TextField rotateSliderXText, rotateSliderYText, rotateSliderZText;
    private TextField scaleSliderText;
    private TextField lightDirectionXText, lightDirectionYText, lightDirectionZText;
    private Button computeButton, resetButton;
    private double defaultValueTranslationSlider = 0, defaultValueRotationSlider = 0, defaultValueScaleSlider = 1,
                    defaultValueLightSlider = 0;


    private void createRightActionPanel() {
        rightActionPanel = new AnchorPane();
        rightActionPanel.getStylesheets().add(
                this.getClass().getResource("/mesh_visualizer/css/main_style.css").toExternalForm());
        rightActionPanel.getStyleClass().add("anchorPane");
        rightActionPanel.setMinWidth(270);

        double w = rightActionPanel.getWidth();
        double h = rightActionPanel.getHeight();

        List<Object> sliderElement;

        // TRANSLATE CONTROLS
        Label translateLabel = createLabel("Translate", 5d);
        double[] sliderValues = new double[]{-2, 2, defaultValueTranslationSlider};
        sliderElement = setupSlider(30d, sliderValues);
        translateSliderXText = (TextField) sliderElement.get(0);
        translateSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(70d, sliderValues);
        translateSliderYText = (TextField) sliderElement.get(0);
        translateSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(110d, sliderValues);
        translateSliderZText = (TextField) sliderElement.get(0);
        translateSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // ROTATE CONTROLS
        Label rotateLabel = createLabel("Rotate", 160d);
        sliderValues = new double[]{-Math.PI, Math.PI, defaultValueRotationSlider};
        sliderElement = setupSlider(185d, sliderValues);
        rotateSliderXText = (TextField) sliderElement.get(0);
        rotateSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(225d, sliderValues);
        rotateSliderYText = (TextField) sliderElement.get(0);
        rotateSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(265d, sliderValues);
        rotateSliderZText = (TextField) sliderElement.get(0);
        rotateSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // SCALE CONTROLS
        Label scaleLabel = createLabel("Scale", 305d);
        sliderValues = new double[]{0, 2, defaultValueScaleSlider};
        sliderElement = setupSlider(330d, sliderValues);
        scaleSliderText = (TextField) sliderElement.get(0);
        scaleSlider = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // LIGHT SETTINGS CONTROLS
        Label lightLabel = createLabel("Light Settings", 370d);
        sliderValues = new double[]{-2, 2, defaultValueLightSlider};
        sliderElement = setupSlider(395d, sliderValues);
        lightDirectionXText = (TextField) sliderElement.get(0);
        lightDirectionSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(435d, sliderValues);
        lightDirectionYText = (TextField) sliderElement.get(0);
        lightDirectionSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(475d, sliderValues);
        lightDirectionZText = (TextField) sliderElement.get(0);
        lightDirectionSliderZ = (Slider) sliderElement.get(1);

        computeButton = createButton("Compute", 610d);
        // ----------------------------------------------------
        resetButton = createButton("Reset", 670);

        addEventListeners();

        rightActionPanel.getChildren().addAll(
                translateSliderX, translateSliderXText, translateSliderY, translateSliderYText, translateSliderZ,
                translateSliderZText, rotateSliderX, rotateSliderXText, rotateSliderY, translateLabel,
                rotateSliderYText, rotateSliderZ, rotateSliderZText, scaleSliderText, scaleSlider, rotateLabel,
                lightDirectionSliderX, lightDirectionXText, lightDirectionSliderY, lightDirectionYText, scaleLabel,
                lightDirectionSliderZ, lightDirectionZText, computeButton, resetButton, lightLabel);

        layout.setRight(rightActionPanel);
    }

    private <T> List<T> setupSlider(double yPositionText, double[] sliderValues) {
        TextField sliderText = new TextField(String.valueOf(sliderValues[2]));
        sliderText.setPrefSize(45, 12);
        AnchorPane.setTopAnchor(sliderText, yPositionText);
        AnchorPane.setRightAnchor(sliderText, 15.0);

        Slider slider = new Slider(sliderValues[0], sliderValues[1], sliderValues[2]);
        slider.setPrefSize(185, 15);
        AnchorPane.setTopAnchor(slider, yPositionText + 5d);
        AnchorPane.setLeftAnchor(slider, 15.0);

        TextField finalSliderText = sliderText;
        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String rounded = Double.toString(Math.round(newValue.doubleValue() * 100.0) / 100.0);
            finalSliderText.setText(rounded);
        });

        sliderText.textProperty().addListener((observable, oldText, newText) -> {
            Pattern pattern = Pattern.compile("[0-9]+(.[0-9]+|)");
            Matcher matcher = pattern.matcher(newText);
            if (matcher.find()) {
                Double rounded = Double.valueOf(Math.round(Double.valueOf(newText) * 100.0) / 100.0);
                slider.setValue(rounded);
            }

        });

        return new ArrayList<>(List.of((T) sliderText, (T) slider));
    }

    private Button createButton(String buttonText, double distanceFromTop) {
        Button button = new Button(buttonText);
        AnchorPane.setTopAnchor(button, distanceFromTop);
        AnchorPane.setLeftAnchor(button, 15d);
        AnchorPane.setRightAnchor(button, 15d);
        return button;
    }

    private Label createLabel(String text, double positionFromTop) {
        Label label = new Label();
        label.setText(text);
        AnchorPane.setTopAnchor(label, positionFromTop);
        AnchorPane.setLeftAnchor(label, 15d);
        return label;
    }

    private void addEventListeners() {
        computeButton.setOnAction(event -> {
//            changeLightSource();
            objectTransformationMatrix = new Mat4();
            addRotation(rotateSliderX.getValue(), rotateSliderY.getValue(), rotateSliderZ.getValue());
            addScaling(scaleSlider.getValue());
            addTranslation(translateSliderX.getValue(), translateSliderY.getValue(), translateSliderZ.getValue());
            paintScene();
        });


        resetButton.setOnAction(event -> {
            resetObject();
            paintScene();
        });
    }

    private void resetSlicerValuesToDefault() {
        translateSliderX.setValue(defaultValueTranslationSlider);
        translateSliderY.setValue(defaultValueTranslationSlider);
        translateSliderZ.setValue(defaultValueTranslationSlider);
        rotateSliderX.setValue(defaultValueRotationSlider);
        rotateSliderY.setValue(defaultValueRotationSlider);
        rotateSliderZ.setValue(defaultValueRotationSlider);
        scaleSlider.setValue(defaultValueScaleSlider);
        lightDirectionSliderX.setValue(defaultValueLightSlider);
        lightDirectionSliderY.setValue(defaultValueLightSlider);
        lightDirectionSliderZ.setValue(defaultValueLightSlider);
    }

    private Mat4 createProjectionMatrix() {
        double w_center = main_scene.getWidth() / 2;
        double h_center = main_scene.getHeight() / 2;
        initialScaling = width / 10;

        Mat4 translationMatrix = new Mat4();
        translationMatrix.setValue(0, 3, w_center);
        translationMatrix.setValue(1, 3, h_center);

        Mat4 scaleMatrix = new Mat4();
        scaleMatrix.setValue(0, 0, initialScaling);
        scaleMatrix.setValue(1, 1, initialScaling);

        scaleMatrix = Transformations.multiply(scaleMatrix, getMirrorMatrixOverX());
        return Transformations.multiply(Transformations.multiply(translationMatrix, scaleMatrix), objectTransformationMatrix);
    }

    private Mat4 getMirrorMatrixOverX() {
        Mat4 mirrorMatrix = new Mat4();
        mirrorMatrix.setValue(1, 1, -1);
        mirrorMatrix.setValue(2, 2, -1);
        return mirrorMatrix;
    }

    private void addTranslation(double x, double y, double z) {
        Mat4 translationMatrix = new Mat4();
        translationMatrix.setValue(0, 3, x);
        translationMatrix.setValue(1, 3, y);
        translationMatrix.setValue(2, 3, z);

        objectTransformationMatrix = Transformations.multiply(translationMatrix, objectTransformationMatrix);
    }

    private void addScaling(double scaleValue) {
        Mat4 scalingMatrix = new Mat4();
        scalingMatrix.setValue(0, 0, scaleValue);
        scalingMatrix.setValue(1, 1, scaleValue);
        scalingMatrix.setValue(2, 2, scaleValue);

        objectTransformationMatrix = Transformations.multiply(scalingMatrix, objectTransformationMatrix);
    }

    private void addRotation(double x, double y, double z) {
        if (x != 0.0) {
            Mat4 rotationXMatrix = new Mat4();
            rotationXMatrix.setValue(1, 1, Math.cos(x));
            rotationXMatrix.setValue(1, 2, -Math.sin(x));
            rotationXMatrix.setValue(2, 1, Math.sin(x));
            rotationXMatrix.setValue(2, 2, Math.cos(x));

            objectTransformationMatrix = Transformations.multiply(rotationXMatrix, objectTransformationMatrix);
        }

        if (y != 0.0) {
            Mat4 rotationYMatrix = new Mat4();
            rotationYMatrix.setValue(0, 0, Math.cos(y));
            rotationYMatrix.setValue(0, 2, Math.sin(y));
            rotationYMatrix.setValue(2, 0, -Math.sin(y));
            rotationYMatrix.setValue(2, 2, Math.cos(y));

            objectTransformationMatrix = Transformations.multiply(rotationYMatrix, objectTransformationMatrix);
        }

        if (z != 0.0) {
            Mat4 rotationZMatrix = new Mat4();
            rotationZMatrix.setValue(0, 0, Math.cos(z));
            rotationZMatrix.setValue(0, 1, -Math.sin(z));
            rotationZMatrix.setValue(1, 0, Math.sin(z));
            rotationZMatrix.setValue(1, 1, Math.cos(z));

            objectTransformationMatrix = Transformations.multiply(rotationZMatrix, objectTransformationMatrix);
        }
    }

    private void changeLightSource() {
    }

    private void resetObject() {
        main_scene.getChildren().clear();
        objectTransformationMatrix = new Mat4();
        resetSlicerValuesToDefault();
    }
}
