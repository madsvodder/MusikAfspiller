package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.logging.Logger;

public class MediaPlayer {

    MainViewController mainViewController;

    public MediaPlayer(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    Playlist playingPlaylist;

    Logger logger = Logger.getLogger(MediaPlayer.class.getName());

    private boolean isSongPlaying = false;

    @Getter
    private javafx.scene.media.MediaPlayer mediaPlayer;

    @Getter
    private StringProperty currentTimeProperty = new SimpleStringProperty("0:00");



    // Play the song
    public void getReadyToPlaySongInPlaylist(Song songToPlay, Playlist playlistToPlay) {
        // Check if the song file exists
        if (songToPlay.getSongFile() == null || !songToPlay.getSongFile().exists()) {
            logger.warning("Invalid song file.");
            return;
        }

        if (playlistToPlay != null) {
            playingPlaylist = playlistToPlay;
        }

        if (!isSongPlaying) {
            System.out.println("Song is not playing");
            doPlaySongInPlaylist(songToPlay);
        } else {
            System.out.println("Song is already playing.");
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            isSongPlaying = false;
            doPlaySongInPlaylist(songToPlay);
        }

    }
    private void doPlaySongInPlaylist(Song songToPlay) {

        // Create a Media object from the song's file path
        File songFile = songToPlay.getSongFile();
        Media media = new Media(songFile.toURI().toString());

        // Create a MediaPlayer to control playback
        mediaPlayer = new javafx.scene.media.MediaPlayer(media);

        // Bind currentTimeProperty to update dynamically
        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) -> {
            currentTimeProperty.set(formatDuration(newTime));
        });

        // Set up media player events
        mediaPlayer.setOnReady(() -> {
            logger.info("Song: " + songToPlay.getSongTitle() + " - " + songToPlay.getSongArtist());
            currentSongIndex = playingPlaylist.getSongs().indexOf(songToPlay);
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            logger.info("Song finished playing");
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
            isSongPlaying = false;

            if (isNextSongAvailable()) {
                doPlaySongInPlaylist(getNextSong());
            }
        });

        // Play the media
        mediaPlayer.play();
        mainViewController.updateSongUI(songToPlay);
        isSongPlaying = true;
    }

    @Getter @Setter @JsonIgnore
    public int currentSongIndex = 0;

    public boolean isNextSongAvailable() {
        if (playingPlaylist.getSongs().isEmpty()) {
            return false; // Playlist is empty
        }
        return currentSongIndex < playingPlaylist.getSongs().size() - 1; // Check if next song exists
    }

    public boolean isPreviousSongAvailable() {
        if (playingPlaylist.getSongs().isEmpty()) {
            return false;
        }
        return currentSongIndex > 0; // Check previous song
    }

    public Song getPreviousSong() {
        if (playingPlaylist.getSongs().isEmpty()) {
            return null; // Return null if the playlist is null or empty
        }

        if (currentSongIndex > 0) {
            return playingPlaylist.getSongs().get(--currentSongIndex);
        } else {
            return null;
        }
    }

    public Song getNextSong() {
        if (playingPlaylist.getSongs().isEmpty()) {
            return null; // Return null if the playlist is null or empty
        }

        if (currentSongIndex >= playingPlaylist.getSongs().size()) {
            currentSongIndex = 0;
            return playingPlaylist.getSongs().get(currentSongIndex);
        } else {
            return playingPlaylist.getSongs().get(++currentSongIndex);
        }

    }

    public Song getCurrentSong() {
        if (playingPlaylist.getSongs().isEmpty()) {
            return null; // No song in the playlist
        }
        return playingPlaylist.getSongs().get(currentSongIndex);
    }

    // Pause the song
    public void pauseSong() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
        }
    }

    // Resume the song
    public void resumeSong() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    // Stop the song
    public void stopSong() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
        }
    }

    public void skipSong() {
        if (mediaPlayer != null) {
            if (isNextSongAvailable()) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
                isSongPlaying = false;
                doPlaySongInPlaylist(getNextSong());
            } else {
                mainViewController.updateSongUI(null);
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
                isSongPlaying = false;
            }
        }
    }

    public void previousSong() {
        if (mediaPlayer != null) {
            if (isPreviousSongAvailable()) {
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
                isSongPlaying = false;
                doPlaySongInPlaylist(getPreviousSong());
            } else {
                mainViewController.updateSongUI(null);
                mediaPlayer.stop();
                mediaPlayer.dispose();
                mediaPlayer = null;
                isSongPlaying = false;
            }
        }
    }

    // Check if the song is currently playing
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.getStatus() == javafx.scene.media.MediaPlayer.Status.PLAYING;
        }
        return false;
    }

    // Helper method to format Duration into a string
    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }

    // Get the current play time of the song
    public Duration getCurrentTime() {
        if (mediaPlayer != null) {
            return mediaPlayer.getCurrentTime();
        }
        return Duration.ZERO;
    }
}
