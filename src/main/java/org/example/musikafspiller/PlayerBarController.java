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

import java.util.Objects;
import java.util.concurrent.atomic.AtomicBoolean;

public class PlayerBarController {

    @Getter @Setter
    CustomMediaPlayer mediaPlayer;

    @Setter
    private MainViewController mainViewController;

    @Setter
    UserLibrary userLibrary;

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
    private MFXSlider MFX_SongProgressSlider;

    @FXML
    private ImageView imgview_volume;


    // These images are the ones that we change during runtime.
    private Image playImage;
    private Image pauseImage;
    private Image musicRecordImage;

    private Image mutedImage;

    private Image fullVolumeImage;
    private Image halfVolumeImage;
    private Image lowVolumeImage;


    @Setter QueueViewController queueViewController;

    public void customInit() {


        mediaPlayer = new CustomMediaPlayer(this);
        mediaPlayer.setUserLibrary(userLibrary);
        mediaPlayer.setMainViewController(mainViewController);

        // Load images
        playImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/CircledPlay.png")));
        pauseImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/PauseButton.png")));
        musicRecordImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/MusicRecord.png")));
        mutedImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/Mute.png")));
        fullVolumeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/Volume_Full.png")));
        halfVolumeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/Volume_Half.png")));
        lowVolumeImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/Volume_Low.png")));


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

                updateSpeakerImage(volume);
            });
        }
    }

    private void updateSpeakerImage(double value) {
        if (value == 0) {
            imgview_volume.setImage(mutedImage);
        } else {
            if (value <= 0.33) {
                imgview_volume.setImage(lowVolumeImage);
            } else if (value <= 0.66) {
                imgview_volume.setImage(halfVolumeImage);
            } else {
                imgview_volume.setImage(fullVolumeImage);
            }
        }
    }

    private void setupSongProgressSlider() {
        if (mediaPlayer != null) {
            // Initialize the slider's max value when the media is ready
            mediaPlayer.getMediaPlayer().setOnReady(() ->
                    MFX_SongProgressSlider.setMax(mediaPlayer.getMediaPlayer().getTotalDuration().toSeconds())
            );

            // Track whether the user is interacting with the slider (dragging or clicking)
            AtomicBoolean isDragging = new AtomicBoolean(false);

            // Update the slider as the song progresses, only if the user is not dragging or mouse pressed
            mediaPlayer.getMediaPlayer().currentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!isDragging.get() && !MFX_SongProgressSlider.isPressed()) {
                    // Update the slider only if the user is not dragging
                    MFX_SongProgressSlider.setValue(newValue.toSeconds());
                }
            });

            // Handle mouse press to start dragging
            MFX_SongProgressSlider.setOnMousePressed(event -> {
                isDragging.set(true); // Indicate dragging has started
                // Optionally, update media time immediately on press (if desired)
                updateMediaTimeBasedOnSlider();
            });

            // Handle dragging behavior
            MFX_SongProgressSlider.setOnMouseDragged(event -> {
                // Do not update the song's time during dragging
                // We only update the slider value visually as the user drags, without affecting the song's time
            });

            // Handle mouse release to finalize dragging
            MFX_SongProgressSlider.setOnMouseReleased(event -> {
                isDragging.set(false); // Indicate dragging has ended
                updateMediaTimeBasedOnSlider(); // Seek the media to the final position when released
            });

            // Handle clicking somewhere on the slider without dragging
            MFX_SongProgressSlider.setOnMouseClicked(event -> {
                isDragging.set(false); // Ensure click is treated as final
                updateMediaTimeBasedOnSlider(); // Seek the media immediately to the clicked position
            });
        }
    }

    // Method to seek the media player to the slider's position
    private void updateMediaTimeBasedOnSlider() {
        if (mediaPlayer != null) {
            double seekTime = MFX_SongProgressSlider.getValue();
            mediaPlayer.getMediaPlayer().seek(Duration.seconds(seekTime));
        }
    }


    /* OLD METHOD FOR SONG PROGRESS SLIDER
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

                // Update label while dragging
                label_songDuration.setText(formatTime(Duration.seconds(targetSeconds)));
            });

            // Handle seeking when the user releases the slider
            slider_songProgress.setOnMouseReleased(event -> {
                seekToSliderPosition();
            });
        }
    }


    // Method to seek to the position when the user releases the slider
    private void seekToSliderPosition() {
        double targetTimeInSeconds = slider_songProgress.getValue();
        mediaPlayer.getMediaPlayer().seek(Duration.seconds(targetTimeInSeconds));
    }
    */

    @FXML
    private void onButtonVolume() {

        // Stop if any of these are null
        if (mediaPlayer == null) {
            return;
        }

        mediaPlayer.mute();

        if (mediaPlayer.isMuted()) {
            imgview_volume.setImage(mutedImage);
            slider_Volume.setValue(mediaPlayer.getMediaVolume());
        } else {
            slider_Volume.setValue(mediaPlayer.getMediaVolume()*100);
        }

        updateSpeakerImage(mediaPlayer.getMediaVolume());
    }


    // Helper method to format Duration into a string (MM:SS)
    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
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
        mainViewController.toggleQueueSidebar();
    }


    @FXML
    public void toggleShuffle() {
        if (mediaPlayer != null) {
            mediaPlayer.toggleShuffle();
            button_Shuffle.setOpacity(mediaPlayer.isShuffle() ? 1 : 0.3);
            System.out.println("Shuffle: " + mediaPlayer.isShuffle());
        }
    }

    public void handleShuffleTopButton() {
        if (mediaPlayer != null) {
            if (!mediaPlayer.isShuffle()) {
                toggleShuffle();
            }
            button_Shuffle.setOpacity(mediaPlayer.isShuffle() ? 1 : 0.3);
        }

        // I don't know what this is, but intellij told me to use it
        assert mediaPlayer != null;
        System.out.println("Shuffle: " + mediaPlayer.isShuffle());
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
            if (mediaPlayer.getCurrentTime().toSeconds() < 2 && mediaPlayer.getCurrentSongIndex() > 0) {
                mediaPlayer.previousSong();
            } else {
                mediaPlayer.getMediaPlayer().seek(Duration.ZERO);
            }
        }
    }

    public void playSongFromPlaylist(Song song, MusicCollection musicCollection) {
        if (song != null && musicCollection != null) {
            if (song.isSongFileValid()) {
                mediaPlayer.playSong(song, musicCollection);
                mediaPlayer.setCurrentSongIndex(musicCollection.getSongs().indexOf(song));
                mediaPlayer.setCurrentPlayingMusicCollection(musicCollection);
                updateSongUI(song);
            } else {
                mainViewController.showFileNotFoundPrompt(song);
            }
        }
    }

    public void handleAddSongToQueue(Song songToAdd, MusicCollection musicCollection) {
        if (songToAdd != null && musicCollection != null && queueViewController != null) {

            if (songToAdd.isSongFileValid()) {
                mediaPlayer.addSongToQueue(songToAdd, musicCollection);
                queueViewController.customInit(mediaPlayer);
            } else {
                mainViewController.showFileNotFoundPrompt(songToAdd);
            }
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
