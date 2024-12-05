package org.example.musikafspiller;

import atlantafx.base.theme.PrimerDark;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        //Application.setUserAgentStylesheet(new PrimerDark().getUserAgentStylesheet());
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MainViewController controller = fxmlLoader.getController();
        stage.setTitle("Main");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            controller.save();
        });
    }

    public static void main(String[] args) {
        launch();
    }
}