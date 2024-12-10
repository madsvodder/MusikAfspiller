package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;
import java.util.ArrayList;

public class QueueViewController {


    MediaPlayer mediaPlayer;

    @FXML
    private VBox vbox_queueItems;

    public void customInit(MediaPlayer mediaPlayer) {

        this.mediaPlayer = mediaPlayer;

        vbox_queueItems.getChildren().clear();

        // Iterate over the songQueue and create a queue item for each song
        for (Song song : mediaPlayer.getSongQueue()) {
            try {
                vbox_queueItems.getChildren().add(createQueueItem(song));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to create a queue item for a given song
    private Node createQueueItem(Song song) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("queue-item.fxml"));

        // Load the FXML for the queue item
        HBox queueItem = loader.load();

        // Get the controller of the queue item FXML
        QueueItemController queueItemController = loader.getController();

        if (mediaPlayer == null) {
            System.out.println("NULNULNULNUL");
        }

        // Set the song data in the queue item
        queueItemController.setQueueViewController(this);
        queueItemController.setSong(song);
        queueItemController.setMediaPlayer(mediaPlayer);
        queueItemController.initCustom();

        // Return the loaded HBox (the queue item)
        return queueItem;
    }

    // Method to remove a queue item (HBox) from the VBox
    public void removeQueueItem(Node node) {
        // Remove the node (HBox) from the VBox
        vbox_queueItems.getChildren().remove(node);
    }

}
