module com.course.filetransfer {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires commons.net;
    requires com.ibm.icu;
    requires json.simple;

    opens com.course.filetransfer to javafx.fxml;
    exports com.course.filetransfer;
}