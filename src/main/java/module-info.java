module org.example.musikafspiller {
    requires javafx.fxml;
    requires static lombok;
    requires jaudiotagger;
    requires com.jfoenix;
    requires com.fasterxml.jackson.databind;
    requires org.controlsfx.controls;
    requires java.logging;
    requires javafx.media;

    opens org.example.musikafspiller to javafx.fxml;
    exports org.example.musikafspiller;
}