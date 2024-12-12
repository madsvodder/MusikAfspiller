package org.example.musikafspiller;

import io.github.palexdev.materialfx.controls.MFXSlider;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.ImageView;
import lombok.Getter;
import lombok.Setter;

public class PlayerBarController {

    @Getter @Setter
    MediaPlayer mediaPlayer;

    @FXML
    private Button button_Shuffle;

    @FXML
    private Button button_Volume;

    @FXML
    private ImageView image_PlayPause;

    @FXML
    private ImageView image_currentAlbumPlaying;

    @FXML
    private ImageView imgview_Shuffle;

    @FXML
    private ImageView imgview_Shuffle1;

    @FXML
    private Label label_CurrentSongName;

    @FXML
    private Label label_currentArtistName;

    @FXML
    private Label label_songDuration;

    @FXML
    private Label label_songDurationFinal;

    @FXML
    private ProgressBar progressBar_SongProgress;

    @FXML
    private MFXSlider slider_Volume;

    @FXML
    private Slider slider_songProgress;

    @FXML
    void nextSong(ActionEvent event) {

    }

    @FXML
    void onPressedPlay(ActionEvent event) {

    }

    @FXML
    void previousSong(ActionEvent event) {

    }

    @FXML
    void showQueueView(ActionEvent event) {

    }

    @FXML
    void toggleShuffle(ActionEvent event) {

    }

}
