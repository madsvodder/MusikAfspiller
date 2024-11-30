module org.example.musikafspiller {
    requires javafx.fxml;
    requires static lombok;
    requires jaudiotagger;
    requires java.desktop;
    requires javafx.media;
    requires com.jfoenix;
    requires javafx.controls;
    requires java.logging;


    opens org.example.musikafspiller to javafx.fxml;
    exports org.example.musikafspiller;
}