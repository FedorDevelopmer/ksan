module com.ksan.lab3 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires jdk.httpserver;

    opens com.ksan.lab3 to javafx.fxml;
    exports com.ksan.lab3;
}