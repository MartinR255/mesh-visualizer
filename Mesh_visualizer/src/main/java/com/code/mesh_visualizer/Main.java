package com.code.mesh_visualizer;

import javafx.application.Application;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Line;
import javafx.scene.shape.Polygon;
import javafx.stage.FileChooser;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.io.File;
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



    @Override
    public void start(Stage stage) throws Exception {
        Rectangle2D screenBounds = Screen.getPrimary().getBounds();
        width = screenBounds.getWidth() * WIDTH_SCREEN_ADJUST_CONSTANT;
        height = screenBounds.getHeight() * HEIGHT_SCREEN_ADJUST_CONSTANT;

        this.stage = stage;
        group = new Group();
        main_scene = new Pane();
        mash = new Mash();

        // load color palette
        File css_file = new File("style/main_style.css");
        css_style_file = "file:///" + css_file.getAbsolutePath().replace("\\", "/");

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
        double w_center = main_scene.getWidth() / 2;
        double h_center = main_scene.getHeight() / 2;
        initialScaling = width / 10;

        for (Face face : mash.getFaces()) {
            List<Point> facePoints = face.getPoints();

            Double[] pointA = translateCoordinates(facePoints.get(0).getX(), facePoints.get(0).getY());
            Double[] pointB = translateCoordinates(facePoints.get(1).getX(), facePoints.get(1).getY());
            Double[] pointC = translateCoordinates(facePoints.get(2).getX(), facePoints.get(2).getY());

            Line AB = new Line(w_center + pointA[0], h_center + pointA[1], w_center + pointB[0], h_center + pointB[1]);
            Line BC = new Line(w_center + pointB[0], h_center + pointB[1], w_center + pointC[0], h_center + pointC[1]);
            Line CA = new Line(w_center + pointC[0], h_center + pointC[1], w_center + pointA[0], h_center + pointA[1]);

            main_scene.getChildren().addAll(AB, BC, CA);
        }

        System.out.println("painting");
    }


    public void clearScreen() {
        main_scene.getChildren().clear();
        mash.clearMash();
        paintScene();
    }


    private HBox topActionPanel;
    private Button fileExplorerButton, clearScreenButton;
    private FileChooser fileExplorer;
    private void createTopActionPanel() {
        topActionPanel = new HBox();
        topActionPanel.getStylesheets().add(css_style_file);
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
        });

        layout.setTop(topActionPanel);
    }

    private AnchorPane rightActionPanel;
    private Button translateButton, scaleButton, rotateButton;
    private Spinner translateSpinnerLeft, translateSpinnerMid, translateSpinnerRight;
    private Slider translateSliderX;
    private TextField translateSliderXText;

    private void createRightActionPanel() {
        rightActionPanel = new AnchorPane();
        rightActionPanel.getStylesheets().add(css_style_file);
        rightActionPanel.getStyleClass().add("anchorPane");
        rightActionPanel.setMinWidth(270);


        double w = rightActionPanel.getWidth();
        double h = rightActionPanel.getHeight();


//        translateSliderXText = new TextField("0");
//        translateSliderXText.setPrefSize(45, 12);
//        AnchorPane.setTopAnchor(translateSliderXText, 40.0);
//        AnchorPane.setRightAnchor(translateSliderXText, 15.0);
//
//
//        translateSliderX = new Slider(-1, 1, 0.01);
//        translateSliderX.setPrefSize(185, 15);
//        AnchorPane.setTopAnchor(translateSliderX, 45.0);
//        AnchorPane.setLeftAnchor(translateSliderX, 15.0);
//
//        translateSliderX.valueProperty().addListener((observable, oldValue, newValue) -> {
//            String rounded = Double.toString(Math.round(newValue.doubleValue() * 100.0) / 100.0);
//            translateSliderXText.setText(rounded);
//        });
//
//
//        translateButton = new Button("Translate");
//        AnchorPane.setTopAnchor(translateButton, 125.0);
//        AnchorPane.setLeftAnchor(translateButton, 15.0);
//        AnchorPane.setRightAnchor(translateButton, 15.0);
////        AnchorPane.setBottomAnchor(translateButton, 600.0);

        rightActionPanel.getChildren().addAll();

//        scaleButton = new Button("Scale");
//        rotateButton = new Button("Rotate");

        //rightActionPanel.getChildren().addAll(scaleButton, rotateButton);

        layout.setRight(rightActionPanel);
    }


    public Double[] translateCoordinates(double x, double y) {
        double refactored_x = x * initialScaling;
        double refactored_y = -1 * y * initialScaling;
        return new Double[] {refactored_x, refactored_y};
    }
}
