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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;


public class Main extends Application {
    private Scene layoutScene;
    private Pane mainScene;
    private ScrollPane scrollingAnchorPane;
    private Stage stage;

    private BorderPane layout;


    private double width, height;
    private final double WIDTH_SCREEN_ADJUST_CONSTANT = 0.75, HEIGHT_SCREEN_ADJUST_CONSTANT = 0.7;
    private  Mash mash;
    double initialScaling;
    Mat4 objectTransformationMatrix;

    @Override
    public void start(Stage stage) {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        width = screenBounds.getWidth() * WIDTH_SCREEN_ADJUST_CONSTANT;
        height = screenBounds.getHeight() * HEIGHT_SCREEN_ADJUST_CONSTANT;

        this.stage = stage;
        mainScene = new Pane();
        mash = new Mash();

        // set up border pane layout
        layout = new BorderPane();
        layout.setCenter(mainScene);
        createTopActionPanel();
        createRightActionPanel();

        objectTransformationMatrix = new Mat4();
        setWindowEventListeners();
        layoutScene = new Scene(layout, width, height);
        stage.setMinWidth(525);
        stage.setMinHeight(285);
        stage.setTitle("Mesh Visualizer");
        stage.setScene(layoutScene);
        stage.show();
    }


    public static void main(String[] args) {
        launch();
    }

    private void paintScene() {
        mainScene.getChildren().clear();
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

            mainScene.getChildren().addAll(AB, BC, CA);
        }
    }

    private void setWindowEventListeners() {
        stage.widthProperty().addListener((observable, oldValue, newValue) -> {
            width = (double) newValue;
            createTopActionPanel();
            paintScene();
        });

        stage.heightProperty().addListener((observable, oldValue, newValue) -> {
            height = (double) newValue;
            createRightActionPanel();
            paintScene();
        });
    }

    private void clearScreen() {
        mainScene.getChildren().clear();
        mash.clearMash();
        objectTransformationMatrix = new Mat4();
        resetSlicerValuesToDefault();
    }

    private FileChooser fileExplorer;
    private void createTopActionPanel() {
        HBox topActionPanel = new HBox();
        topActionPanel.getStylesheets().add(
                Objects.requireNonNull(this.getClass().getResource("/mesh_visualizer/css/main_style.css")).toExternalForm());
        topActionPanel.getStyleClass().add("hbox");
        topActionPanel.setAlignment(Pos.CENTER_LEFT);
        topActionPanel.setSpacing(45);

        // setup buttons
        Button fileExplorerButton = new Button("Choose File");
        Button clearScreenButton = new Button("Clear");

        topActionPanel.getChildren().addAll(fileExplorerButton, clearScreenButton);

        fileExplorer = new FileChooser();
        fileExplorerButton.setOnAction(event -> {
            fileExplorer.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".obj", "*.obj"));
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

    private Slider translateSliderX, translateSliderY, translateSliderZ;
    private Slider rotateSliderX, rotateSliderY, rotateSliderZ;
    private Slider scaleSlider;
    private Slider lightDirectionSliderX, lightDirectionSliderY, lightDirectionSliderZ;
    private Button resetButton;
    private double defaultValueTranslationSlider = 0, defaultValueRotationSlider = 0, defaultValueScaleSlider = 1,
                    defaultValueLightSlider = 0;


    private void createRightActionPanel() {
        AnchorPane rightActionPanel = new AnchorPane();
        rightActionPanel.getStylesheets().add(
                this.getClass().getResource("/mesh_visualizer/css/main_style.css").toExternalForm());
        rightActionPanel.getStyleClass().add("anchorPane");
        rightActionPanel.setMinWidth(270);
        rightActionPanel.setMinHeight(mainScene.getHeight());

        List<Object> sliderElement;
        // TRANSLATE CONTROLS
        Label translateLabel = createLabel("Translate", 5d);
        double[] sliderValues = new double[]{-2, 2, defaultValueTranslationSlider};
        sliderElement = setupSlider(30d, sliderValues);
        TextField translateSliderXText = (TextField) sliderElement.get(0);
        translateSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(70d, sliderValues);
        TextField translateSliderYText = (TextField) sliderElement.get(0);
        translateSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(110d, sliderValues);
        TextField translateSliderZText = (TextField) sliderElement.get(0);
        translateSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // ROTATE CONTROLS
        Label rotateLabel = createLabel("Rotate", 160d);
        sliderValues = new double[]{-Math.PI, Math.PI, defaultValueRotationSlider};
        sliderElement = setupSlider(185d, sliderValues);
        TextField rotateSliderXText = (TextField) sliderElement.get(0);
        rotateSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(225d, sliderValues);
        TextField rotateSliderYText = (TextField) sliderElement.get(0);
        rotateSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(265d, sliderValues);
        TextField rotateSliderZText = (TextField) sliderElement.get(0);
        rotateSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // SCALE CONTROLS
        Label scaleLabel = createLabel("Scale", 305d);
        sliderValues = new double[]{0, 2, defaultValueScaleSlider};
        sliderElement = setupSlider(330d, sliderValues);
        TextField scaleSliderText = (TextField) sliderElement.get(0);
        scaleSlider = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // LIGHT SETTINGS CONTROLS
        Label lightLabel = createLabel("Light Settings", 370d);
        sliderValues = new double[]{-2, 2, defaultValueLightSlider};
        sliderElement = setupSlider(395d, sliderValues);
        TextField lightDirectionXText = (TextField) sliderElement.get(0);
        lightDirectionSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(435d, sliderValues);
        TextField lightDirectionYText = (TextField) sliderElement.get(0);
        lightDirectionSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(475d, sliderValues);
        TextField lightDirectionZText = (TextField) sliderElement.get(0);
        lightDirectionSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------
        resetButton = createButton("Reset", 515);

        addRightPanelEventListeners();

        rightActionPanel.getChildren().addAll(
                translateSliderX, translateSliderXText, translateSliderY, translateSliderYText, translateSliderZ,
                translateSliderZText, rotateSliderX, rotateSliderXText, rotateSliderY, translateLabel,
                rotateSliderYText, rotateSliderZ, rotateSliderZText, scaleSliderText, scaleSlider, rotateLabel,
                lightDirectionSliderX, lightDirectionXText, lightDirectionSliderY, lightDirectionYText, scaleLabel,
                lightDirectionSliderZ, lightDirectionZText, resetButton, lightLabel);

        scrollingAnchorPane = new ScrollPane();
        scrollingAnchorPane.setMinViewportWidth(270d);
        scrollingAnchorPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollingAnchorPane.setContent(rightActionPanel);
        layout.setRight(scrollingAnchorPane);
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

        slider.valueProperty().addListener((observable, oldValue, newValue) -> {
            String rounded = Double.toString(Math.round(newValue.doubleValue() * 100.0) / 100.0);
            sliderText.setText(rounded);
            transformObject();
        });

        sliderText.textProperty().addListener((observable, oldText, newText) -> {
            if (Validators.isDouble(newText)) {
                Double rounded = Double.valueOf(Math.round(Double.valueOf(newText) * 100.0) / 100.0);
                slider.setValue(rounded);
                transformObject();
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

    private void transformObject() {
        // changeLightSource();
        objectTransformationMatrix = new Mat4();
        objectTransformationMatrix = Transformations.addRotation(
                rotateSliderX.getValue(), rotateSliderY.getValue(), rotateSliderZ.getValue(),
                objectTransformationMatrix);
        objectTransformationMatrix = Transformations.addScaling(scaleSlider.getValue(), objectTransformationMatrix);
        objectTransformationMatrix = Transformations.addTranslation(
                translateSliderX.getValue(), translateSliderY.getValue(), translateSliderZ.getValue(),
                objectTransformationMatrix);
        paintScene();
    }

    private void addRightPanelEventListeners() {
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
        double w_center = mainScene.getWidth() / 2;
        double h_center = mainScene.getHeight() / 2;
        initialScaling = width / 10;

        Mat4 translationMatrix = new Mat4();
        translationMatrix.setValue(0, 3, w_center);
        translationMatrix.setValue(1, 3, h_center);

        Mat4 scaleMatrix = new Mat4();
        scaleMatrix.setValue(0, 0, initialScaling);
        scaleMatrix.setValue(1, 1, initialScaling);

        scaleMatrix = Transformations.multiply(scaleMatrix, Transformations.getMirrorMatrixOverX());
        return Transformations.multiply(Transformations.multiply(translationMatrix, scaleMatrix), objectTransformationMatrix);
    }


    private void changeLightSource() {
    }

    private void resetObject() {
        mainScene.getChildren().clear();
        objectTransformationMatrix = new Mat4();
        resetSlicerValuesToDefault();
    }
}
