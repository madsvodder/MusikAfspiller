module org.example.musikafspiller {
    requires static lombok;
    requires jaudiotagger;
    requires com.jfoenix;
    requires com.fasterxml.jackson.databind;
    requires org.controlsfx.controls;
    requires javafx.media;
    requires org.slf4j;
    requires atlantafx.base;
    requires jdk.security.jgss;
    requires java.logging;
    requires MaterialFX;

    opens org.example.musikafspiller to javafx.fxml;
    exports org.example.musikafspiller;
}