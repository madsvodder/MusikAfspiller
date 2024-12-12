package org.example.musikafspiller;

import io.github.palexdev.materialfx.controls.MFXSlider;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.io.IOException;

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

    // These images are the ones that we change during runtime.
    private Image playImage;
    private Image pauseImage;
    private Image musicRecordImage;

    public void customInit() {

        mediaPlayer = new MediaPlayer(this);

        // Load images
        playImage = new Image(getClass().getResourceAsStream("/images/LightImages/CircledPlay.png"));
        pauseImage = new Image(getClass().getResourceAsStream("/images/LightImages/PauseButton.png"));
        musicRecordImage = new Image(getClass().getResourceAsStream("/images/LightImages/MusicRecord.png"));

        // Bind the label in the corner for the song duration
        label_songDuration.textProperty().bind(mediaPlayer.getCurrentTimeProperty());

        slider_songProgress.valueProperty().addListener((observable, oldValue, newValue) -> {
            progressBar_SongProgress.setProgress(newValue.doubleValue() / slider_songProgress.getMax());
        });

        setupVolumeSlider();

        //setupSongProgressSlider();
    }

    private void setupVolumeSlider() {
        if (mediaPlayer != null) {
            slider_Volume.valueProperty().addListener((observable, oldValue, newValue) -> {
                double volume = newValue.doubleValue() / 100; // Convert to range [0.0, 1.0]
                mediaPlayer.adjustVolume(volume);
            });
        }
    }

    private void setupSongProgressSlider() {
        if (mediaPlayer != null) {
            // Set the max value for the slider when the media is ready
            mediaPlayer.getMediaPlayer().setOnReady(() -> {
                slider_songProgress.setMax(mediaPlayer.getMediaPlayer().getTotalDuration().toSeconds());
            });

            // Update the slider position based on the current time (only when not dragging the slider)
            mediaPlayer.getMediaPlayer().currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!slider_songProgress.isValueChanging()) {
                    // Get the current time from your custom MediaPlayer method
                    Duration currentTime = mediaPlayer.getCurrentTime();
                    double currentTimeInSeconds = currentTime.toSeconds();
                    // Update the slider
                    slider_songProgress.setValue(currentTimeInSeconds);

                    // If label is bound, unbind it before setting new value
                    if (label_songDuration.textProperty().isBound()) {
                        label_songDuration.textProperty().unbind();
                    }

                    // Update the label with the formatted time
                    label_songDuration.setText(formatTime(currentTime));
                }
            });

            // Drag event for the slider (when the user drags the slider to seek)
            slider_songProgress.setOnMouseDragged(event -> {
                double targetSeconds = slider_songProgress.getValue();
                // If label is bound, unbind it before setting new value
                if (label_songDuration.textProperty().isBound()) {
                    label_songDuration.textProperty().unbind();
                }
                label_songDuration.setText(formatTime(Duration.seconds(targetSeconds))); // Update label while dragging
            });

            // Handle seeking when the user releases the slider
            slider_songProgress.setOnMouseReleased(event -> {
                seekToSliderPosition();
            });
        }
    }

    // Helper method to format Duration into a string (MM:SS)
    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Method to seek to the position when the user releases the slider
    private void seekToSliderPosition() {
        double targetTimeInSeconds = slider_songProgress.getValue();
        mediaPlayer.getMediaPlayer().seek(Duration.seconds(targetTimeInSeconds));
    }

    public void updateSongUI(Song songToPlay) {
        if (songToPlay != null) {
            label_currentArtistName.setText(songToPlay.getSongArtist());
            label_CurrentSongName.setText(songToPlay.getSongTitle());
            image_currentAlbumPlaying.setImage(songToPlay.getAlbumCover());
            setupSongProgressSlider();
            image_PlayPause.setImage(pauseImage);
            label_songDurationFinal.setText(songToPlay.getSongDurationFormatted());
        } else {
            System.out.println("No song");
            label_currentArtistName.setText("");
            label_CurrentSongName.setText("");
            image_currentAlbumPlaying.setImage(musicRecordImage);
            image_PlayPause.setImage(playImage);
            setupSongProgressSlider();
            image_PlayPause.setImage(playImage);
            label_songDurationFinal.setText("0:00");
        }

    }

    @FXML
    public void showQueueView() {
        /*
        // Load the FXML for the queue view
        FXMLLoader loader = new FXMLLoader(getClass().getResource("queue-view.fxml"));

        try {
            // Load the queue view FXML and get the root node (Parent)
            Parent queueRoot = loader.load();

            // Retrieve the controller after loading the FXML
            QueueViewController controller = loader.getController();

            // Create a Popup for the queue view
            Popup queuePopup = new Popup();

            // Add the loaded root node (queue view) to the Popup
            queuePopup.getContent().add(queueRoot);

            // Get the main stage (the primary window) position and size
            Stage mainStage = primaryStage; // Access the main stage

            // Get the main stage's position (x and y)
            double mainStageX = mainStage.getX();
            double mainStageY = mainStage.getY();

            // Get the main stage's width and height
            double mainStageWidth = mainStage.getWidth();
            double mainStageHeight = mainStage.getHeight();

            // Set the width of the Popup (Adjust this as needed)
            double popupWidth = 400;
            double popupHeight = 300;  // Adjust height if needed

            // Position the Popup in the right bottom corner of the main window and a little up
            queuePopup.setX(mainStageX + mainStageWidth - popupWidth); // Right side
            queuePopup.setY(mainStageY + mainStageHeight - popupHeight - 100); // Bottom side with offset up

            // Show the Popup using the main stage
            queuePopup.show(mainStage); // This will make it appear on top of the main stage

            // Initialize the queue view controller with the song queue
            controller.customInit(mediaPlayer);

            System.out.println("Showing Queue In Right Bottom Corner Relative to Main Window");

        } catch (IOException e) {
            e.printStackTrace();
        }*/
    }

    @FXML
    private void toggleShuffle() {
        if (mediaPlayer != null) {
            mediaPlayer.toggleShuffle();
            button_Shuffle.setOpacity(mediaPlayer.isShuffle() ? 1 : 0.3);
        }
    }

    // Toggle between Play and Pause
    private void togglePlayPause() {
        if (mediaPlayer.isSongPlaying) {
            mediaPlayer.pauseSong();
            image_PlayPause.setImage(playImage);
        } else {
            mediaPlayer.resumeSong();
            image_PlayPause.setImage(pauseImage);
        }
    }

    // Press play button for the ui
    @FXML
    private void onPressedPlay() {togglePlayPause();}

    @FXML
    private void nextSong() {
        if (mediaPlayer.isSongPlaying) {
            mediaPlayer.skipSong();
        }
    }

    @FXML
    private void previousSong() {
        if (mediaPlayer.isSongPlaying) {
            mediaPlayer.previousSong();
        }
    }

    public void playSongFromPlaylist(Song song, MusicCollection musicCollection) {
        if (song != null && musicCollection != null) {
            mediaPlayer.playSong(song, musicCollection);
            mediaPlayer.setCurrentSongIndex(musicCollection.getSongs().indexOf(song));
        }
        updateSongUI(song);
    }

    public void handleAddSongToQueue(Song songToAdd) {
        mediaPlayer.addSongToQueue(songToAdd);
    }
}
