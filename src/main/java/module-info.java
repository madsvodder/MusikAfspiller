module org.example.musikafspiller {
    requires javafx.controls;
    requires javafx.fxml;
    requires static lombok;
    requires java.logging;
    requires jaudiotagger;
    requires java.desktop;
    requires javafx.media;



    opens org.example.musikafspiller to javafx.fxml;
    exports org.example.musikafspiller;
}