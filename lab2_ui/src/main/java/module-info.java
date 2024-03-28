module com.ksan.lab2.ui.lab2_ui {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;

    opens com.ksan.lab2.ui.lab2_ui to javafx.fxml;
    exports com.ksan.lab2.ui.lab2_ui;
}