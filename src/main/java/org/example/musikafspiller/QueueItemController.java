package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

public class QueueItemController {

   @Setter Song song;

   @Setter MediaPlayer mediaPlayer;

   @Setter QueueViewController queueViewController;


    @FXML
    private ImageView image_Cover;

    @FXML
    private Label label_SongName;

    @FXML
    private Button button_Remove;

    @FXML
    private void removeSongFromQueue() {
        // Remove the song from the media player
        mediaPlayer.removeSongFromQueue(song);

        // Call removeQueueItem from QueueViewController
        if (queueViewController != null) {
            queueViewController.removeQueueItem(button_Remove.getParent());  // Pass the parent HBox
        }
    }

    public void initCustom() {
        if (song != null && mediaPlayer != null) {
            label_SongName.setText(song.getSongTitle());
            image_Cover.setImage(song.getAlbumCover());
        }
    }
}
