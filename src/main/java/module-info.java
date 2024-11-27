module org.example.musikafspiller {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.musikafspiller to javafx.fxml;
    exports org.example.musikafspiller;
}