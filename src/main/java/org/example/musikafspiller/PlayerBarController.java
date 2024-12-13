package org.example.musikafspiller;

import io.github.palexdev.materialfx.controls.MFXSlider;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

public class PlayerBarController {

    @Getter @Setter
    MediaPlayer mediaPlayer;

    @Setter
    private MainViewController mainViewController;

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

    QueueViewController queueViewController;

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
        queueViewController = mainViewController.showQueueSidebar();
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
            mediaPlayer.setCurrentPlayingMusicCollection(musicCollection);
        }
        updateSongUI(song);
    }

    public void handleAddSongToQueue(Song songToAdd, MusicCollection musicCollection) {
        mediaPlayer.addSongToQueue(songToAdd, musicCollection);
        if (queueViewController != null) {
            queueViewController.customInit(mediaPlayer);
        }
    }

    @FXML
    private void setUnderlineOnHover() {
        label_CurrentSongName.setUnderline(true);
    }

    @FXML
    private void setUnderlineOffHover() {
        label_CurrentSongName.setUnderline(false);
    }


    @FXML
    private void goToPlaylist() {
        if (mediaPlayer.getCurrentPlayingMusicCollection() != null) {
            mainViewController.switchToPlaylistView(mediaPlayer.getCurrentPlayingMusicCollection());
        }
    }
}
