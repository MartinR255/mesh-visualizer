package com.code.mesh_visualizer;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
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
    private Mat4 objectTransformationMatrix;

    private boolean modelLoaded = false;
    private boolean skeletonVisual = false;
    private double initialScaling;

    private final Vec4 V = new Vec4(0, 0, -10.0, 0d); // view
    private Vec4 L = new Vec4(0, -1.0, 0.0, 0d); // light


    // RIGHT ACTION PANEL WIDGETS
    private Slider translateSliderX, translateSliderY, translateSliderZ;
    private Slider rotateSliderX, rotateSliderY, rotateSliderZ;
    private Slider scaleSlider;
    private Slider lightDirectionSliderX, lightDirectionSliderY, lightDirectionSliderZ;
    private Slider kdSlider, ksSlider, kaSlider;
    private Button resetButton;
    private ColorPicker cPicker;
    private final double translationSliderDefaultValueX = 0, translationSliderDefaultValueY = 0,
            translationSliderDefaultValueZ = 0, rotationSliderDefaultValueX = 0, rotationSliderDefaultValueY = 0,
            rotationSliderDefaultValueZ = 0, scaleSliderDefaultValue = 1, defaultValueLightSliderX = 0,
            defaultValueLightSliderY = -10, defaultValueLightSliderZ = 0, kdSliderDefaultValue = 0.5,
            ksSliderDefaultValue = 0.5, kaSliderDefaultValue = 0.5;

    // LIGHT SETTINGS
    private Color modelColor = Color.rgb(244, 231, 46);
    private double ka = 0.5; // ambient color
    private double kd = 0.5; // diffuse color
    private double ks = 0.5; // specular color
    private final double h = 1.0; // shininess constant
    private final double Ia = 0.5; // ambient light intensity


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
        if (!modelLoaded) return;

        mainScene.getChildren().clear();
        Mat4 transformationMatrix = createProjectionMatrix();

        if (skeletonVisual) {
            paintSkeleton(transformationMatrix);
        } else {
            paintSolid(transformationMatrix);
        }
    }

    private void paintSkeleton(Mat4 transformationMatrix) {
        for (Face face : mash.getFaces()) {
            List<Vec4> facePoints = face.getPoints();

            Vec4 pointAV = Transformations.multiply(transformationMatrix, facePoints.get(0));
            Vec4 pointBV = Transformations.multiply(transformationMatrix, facePoints.get(1));
            Vec4 pointCV = Transformations.multiply(transformationMatrix, facePoints.get(2));

            if (renderPolygon(List.of(pointAV, pointBV, pointCV))) continue;

            Double[] pointA = {pointAV.getVec4().get(0), pointAV.getVec4().get(1)};
            Double[] pointB = {pointBV.getVec4().get(0), pointBV.getVec4().get(1)};
            Double[] pointC = {pointCV.getVec4().get(0), pointCV.getVec4().get(1)};

            Line AB = new Line(pointA[0], pointA[1], pointB[0], pointB[1]);
            Line BC = new Line(pointB[0], pointB[1], pointC[0], pointC[1]);
            Line CA = new Line(pointC[0], pointC[1], pointA[0], pointA[1]);

            mainScene.getChildren().addAll(AB, BC, CA);
        }
    }


    private void paintSolid(Mat4 transformationMatrix) {
        List<Double> intensities = new ArrayList<>();
        List<Polygon> polygons = new ArrayList<>();

        L = Transformations.normalizeVector(L);
        Vec4 H = Transformations.normalizeVector(Transformations.addVec4(V, L, 0d)); // half
        for (Face face : mash.getFaces()) {
            List<Vec4> facePoints = face.getPoints();

            Vec4 pointAV = Transformations.multiply(transformationMatrix, facePoints.get(0));
            Vec4 pointBV = Transformations.multiply(transformationMatrix, facePoints.get(1));
            Vec4 pointCV = Transformations.multiply(transformationMatrix, facePoints.get(2));

            if (renderPolygon(List.of(pointAV, pointBV, pointCV))) continue;

            Double[] pointA = {pointAV.getVec4().get(0), pointAV.getVec4().get(1)};
            Double[] pointB = {pointBV.getVec4().get(0), pointBV.getVec4().get(1)};
            Double[] pointC = {pointCV.getVec4().get(0), pointCV.getVec4().get(1)};

            Double[] points = ArrayTr.concatWithStream(ArrayTr.concatWithStream(pointA, pointB), pointC);
            Polygon polygon = new Polygon();
            polygon.getPoints().addAll(points);
            polygons.add(polygon);
            intensities.add(colorIntensity(pointAV, pointBV, pointCV, H));
        }

        intensities = ArrayTr.normalizeValues(intensities);
        for (int index = 0; index < polygons.size(); index++) {
            Color polygonColor = Color.hsb(modelColor.getHue(), modelColor.getSaturation(), intensities.get(index));
            polygons.get(index).setFill(polygonColor);
        }
        mainScene.getChildren().addAll(polygons);
    }


    public static boolean renderPolygon(List<Vec4> polygonPoints) {
        Vec4 V = new Vec4(0, 0, -1, 0); //view
        Vec4 N = Transformations.normalizeVector(
                Transformations.getTriangleNormal(polygonPoints.get(0), polygonPoints.get(1), polygonPoints.get(2))); // normalized normal

        double dotProduct = Transformations.dotProduct(V, N);
        return !(dotProduct > 0);
    }

    // Blinn-Phong Reflection Model
    private double colorIntensity(Vec4 p0, Vec4 p1, Vec4 p2, Vec4 H) {
        Vec4 N = Transformations.normalizeVector(Transformations.getTriangleNormal(p0, p1, p2)); // normal

        double Id = Transformations.dotProduct(N, L);
        double Is = Math.pow(Transformations.dotProduct(H, N), h);
        return ka * Ia + kd * Id + ks * Is;
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
        modelLoaded = false;
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
        Button skeletonButton = new Button("Skeleton");
        Button solidButton = new Button("Solid");

        topActionPanel.getChildren().addAll(fileExplorerButton, clearScreenButton, skeletonButton, solidButton);

        fileExplorer = new FileChooser();
        fileExplorerButton.setOnAction(event -> {
            fileExplorer.getExtensionFilters().addAll(new FileChooser.ExtensionFilter(".obj", "*.obj"));
            File file = fileExplorer.showOpenDialog(stage);
            if (file != null) {
                clearScreen();
                modelLoaded = true;
                mash.createMash(file.getAbsolutePath());
                paintScene();
            }
        });

        skeletonButton.setOnAction(event -> {
            if (!skeletonVisual) {
                skeletonVisual = true;
                paintScene();
            }
        });

        solidButton.setOnAction(event -> {
            if (skeletonVisual) {
                skeletonVisual = false;
                paintScene();
            }
        });

        clearScreenButton.setOnAction(event -> {
            clearScreen();
            paintScene();
        });

        layout.setTop(topActionPanel);
    }


    private void createRightActionPanel() {
        AnchorPane rightActionPanel = new AnchorPane();
        rightActionPanel.getStylesheets().add(
                Objects.requireNonNull(this.getClass().getResource("/mesh_visualizer/css/main_style.css")).toExternalForm());
        rightActionPanel.getStyleClass().add("anchorPane");
        rightActionPanel.setMinWidth(270);
        rightActionPanel.setMinHeight(mainScene.getHeight());

        List<Object> sliderElement;
        // TRANSLATE CONTROLS
        Label translateLabel = createLabel("Translate", 5d);
        sliderElement = setupSlider(30d, -2, 2, translationSliderDefaultValueX);
        TextField translateSliderXText = (TextField) sliderElement.get(0);
        translateSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(70d, -2, 2, translationSliderDefaultValueY);
        TextField translateSliderYText = (TextField) sliderElement.get(0);
        translateSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(110d, -2, 2, translationSliderDefaultValueZ);
        TextField translateSliderZText = (TextField) sliderElement.get(0);
        translateSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // ROTATE CONTROLS
        Label rotateLabel = createLabel("Rotate", 160d);
        sliderElement = setupSlider(185d, -Math.PI, Math.PI, rotationSliderDefaultValueX);
        TextField rotateSliderXText = (TextField) sliderElement.get(0);
        rotateSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(225d, -Math.PI, Math.PI, rotationSliderDefaultValueY);
        TextField rotateSliderYText = (TextField) sliderElement.get(0);
        rotateSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(265d, -Math.PI, Math.PI, rotationSliderDefaultValueZ);
        TextField rotateSliderZText = (TextField) sliderElement.get(0);
        rotateSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // SCALE CONTROLS
        Label scaleLabel = createLabel("Scale", 305d);
        sliderElement = setupSlider(330d, 0, 2, scaleSliderDefaultValue);
        TextField scaleSliderText = (TextField) sliderElement.get(0);
        scaleSlider = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // LIGHT SETTINGS CONTROLS
        Label lightLabel = createLabel("Light Settings", 370d);
        sliderElement = setupSlider(395d, -100, 100, defaultValueLightSliderX);
        TextField lightDirectionXText = (TextField) sliderElement.get(0);
        lightDirectionSliderX = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(435d, -100, 100, defaultValueLightSliderY);
        TextField lightDirectionYText = (TextField) sliderElement.get(0);
        lightDirectionSliderY = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(475d, -100, 100, defaultValueLightSliderZ);
        TextField lightDirectionZText = (TextField) sliderElement.get(0);
        lightDirectionSliderZ = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        // SETUP MATERIAL PROPERTIES SETTINGS (ka, kd, ks, shininess)
        Label materialProperties = createLabel("Material Properties", 510d);
        sliderElement = setupSlider(550d, 0, 1, kaSliderDefaultValue);
        TextField kaText = (TextField) sliderElement.get(0);
        kaSlider = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(590d, -0, 1, kdSliderDefaultValue);
        TextField kdText = (TextField) sliderElement.get(0);
        kdSlider = (Slider) sliderElement.get(1);

        sliderElement = setupSlider(630d, 0, 1, ksSliderDefaultValue);
        TextField ksText = (TextField) sliderElement.get(0);
        ksSlider = (Slider) sliderElement.get(1);

        // ----------------------------------------------------

        //SETUP COLOR PICKER
        Label cPickerLabel = createLabel("Model color", 670);
        cPicker = new ColorPicker(modelColor);
        cPicker.setPrefSize(270 - 30, 35);
        AnchorPane.setTopAnchor(cPicker, 710.0);
        AnchorPane.setLeftAnchor(cPicker, 15.0);

        // ----------------------------------------------------
        resetButton = createButton("Reset", 765);

        addRightPanelEventListeners();

        rightActionPanel.getChildren().addAll(
                translateSliderX, translateSliderXText, translateSliderY, translateSliderYText, translateSliderZ,
                translateSliderZText, rotateSliderX, rotateSliderXText, rotateSliderY, translateLabel,
                rotateSliderYText, rotateSliderZ, rotateSliderZText, scaleSliderText, scaleSlider, rotateLabel,
                lightDirectionSliderX, lightDirectionXText, lightDirectionSliderY, lightDirectionYText, scaleLabel,
                lightDirectionSliderZ, lightDirectionZText, resetButton, lightLabel, cPicker, cPickerLabel,
                kaSlider, kdSlider, ksSlider, materialProperties, ksText, kdText, kaText);

        scrollingAnchorPane = new ScrollPane();
        scrollingAnchorPane.setMinViewportWidth(270d);
        scrollingAnchorPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.AS_NEEDED);
        scrollingAnchorPane.setContent(rightActionPanel);
        layout.setRight(scrollingAnchorPane);
    }


    private <T> List<T> setupSlider(double yPositionText, double sliderBottomVal, double sliderUpperVal, double sliderDefault) {
        TextField sliderText = new TextField(String.valueOf(sliderDefault));
        sliderText.setPrefSize(45, 12);
        AnchorPane.setTopAnchor(sliderText, yPositionText);
        AnchorPane.setRightAnchor(sliderText, 15.0);

        Slider slider = new Slider(sliderBottomVal, sliderUpperVal, sliderDefault);
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
                double rounded = Math.round(Double.parseDouble(newText) * 100.0) / 100.0;
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
        changeMaterialProperties(kaSlider.getValue(), kdSlider.getValue(), ksSlider.getValue());
        changeLightSource(lightDirectionSliderX.getValue(), lightDirectionSliderY.getValue(), lightDirectionSliderZ.getValue());
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

        cPicker.setOnAction(event -> {
            modelColor = Color.valueOf(cPicker.getValue().toString());
            paintScene();
        });

        layout.setOnScroll(event -> {
            if (event.getDeltaY() >= 0) {
                scaleSlider.setValue(scaleSlider.getValue() + 0.02);
            } else {
                scaleSlider.setValue(scaleSlider.getValue() - 0.02);
            }
        });
    }

    private void resetSlicerValuesToDefault() {
        translateSliderX.setValue(translationSliderDefaultValueX);
        translateSliderY.setValue(translationSliderDefaultValueY);
        translateSliderZ.setValue(translationSliderDefaultValueZ);
        rotateSliderX.setValue(rotationSliderDefaultValueX);
        rotateSliderY.setValue(rotationSliderDefaultValueY);
        rotateSliderZ.setValue(rotationSliderDefaultValueZ);
        scaleSlider.setValue(scaleSliderDefaultValue);
        lightDirectionSliderX.setValue(defaultValueLightSliderX);
        lightDirectionSliderY.setValue(defaultValueLightSliderY);
        lightDirectionSliderZ.setValue(defaultValueLightSliderZ);
        kaSlider.setValue(kaSliderDefaultValue);
        kdSlider.setValue(kdSliderDefaultValue);
        ksSlider.setValue(ksSliderDefaultValue);
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
        scaleMatrix.setValue(2, 2, initialScaling);

        scaleMatrix = Transformations.multiply(scaleMatrix, Transformations.getMirrorMatrixOverX());
        return Transformations.multiply(Transformations.multiply(translationMatrix, scaleMatrix), objectTransformationMatrix);
    }


    private void changeLightSource(double x, double y, double z) {
        L.setValue(0, x);
        L.setValue(1, y);
        L.setValue(2, z);
    }

    private void changeMaterialProperties(double ka, double kd, double ks) {
        this.ka = ka;
        this.kd = kd;
        this.ks = ks;
    }


    private void resetObject() {
        objectTransformationMatrix = new Mat4();
        resetSlicerValuesToDefault();
        L.setValue(0, defaultValueLightSliderX);
        L.setValue(1, defaultValueLightSliderY);
        L.setValue(2, defaultValueLightSliderZ);
        ka = kaSliderDefaultValue;
        kd = kdSliderDefaultValue;
        ks = ksSliderDefaultValue;
    }
}
