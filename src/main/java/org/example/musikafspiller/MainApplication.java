package org.example.musikafspiller;

import atlantafx.base.theme.PrimerDark;
import io.github.palexdev.materialfx.theming.MaterialFXStylesheets;
import javafx.application.Application;
import javafx.concurrent.Task;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.effect.GaussianBlur;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

public class MainApplication extends Application {

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(MainApplication.class.getResource("main-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load());
        MainViewController controller = fxmlLoader.getController();
        controller.setPrimaryStage(stage);
        stage.setTitle("Main");
        stage.setScene(scene);
        stage.show();
        stage.setOnCloseRequest(event -> {
            controller.save();
        });
    }

    public static void main(String[] args) {
        // Set the property for macOS system menu bar integration
        System.setProperty("apple.laf.useScreenMenuBar", "true");
        launch();
    }
}