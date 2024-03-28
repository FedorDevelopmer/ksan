module com.ksan.lab4 {
    requires javafx.controls;
    requires javafx.fxml;
    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires jdk.httpserver;
    requires json.simple;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    opens com.ksan.lab4 to javafx.fxml;
    exports com.ksan.lab4;
}