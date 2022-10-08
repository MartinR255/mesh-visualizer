module com.code.mesh_visualizer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;

    opens com.code.mesh_visualizer to javafx.fxml;
    exports com.code.mesh_visualizer;
}