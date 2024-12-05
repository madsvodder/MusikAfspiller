package org.example.musikafspiller;

import javafx.concurrent.Task;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.controlsfx.control.TaskProgressView;

public class Loader {

    private Stage popupStage;

    public Loader() {
        // Initialize the stage
        popupStage = new Stage();
        // Makes the popup modal
        popupStage.initModality(Modality.APPLICATION_MODAL);
        popupStage.setTitle("Task Progress");
        // Make the window stay on top
        popupStage.setAlwaysOnTop(true);
    }

    public void show(Task<Void> task) {
        // Create a container for the TaskProgressView
        VBox popupContent = new VBox();
        popupContent.setSpacing(10);
        popupContent.setStyle("-fx-padding: 20;");

        // Create the TaskProgressView and add the task
        TaskProgressView<Task<Void>> taskProgressView = new TaskProgressView<>();
        taskProgressView.getTasks().add(task);

        popupContent.getChildren().add(taskProgressView);

        // Create and set the scene
        Scene popupScene = new Scene(popupContent, 550, 150);
        popupStage.setScene(popupScene);

        // Show the popup
        popupStage.show();

        // Run the task in a separate thread
        new Thread(task).start();

        // Close the popup when the task is done
        task.setOnSucceeded(event -> popupStage.close());
        task.setOnFailed(event -> popupStage.close());
        task.setOnCancelled(event -> popupStage.close());
    }
}

