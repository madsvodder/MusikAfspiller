package org.example.musikafspiller;

import javafx.scene.media.Media;
import javafx.scene.media.MediaPlayer;
import javafx.util.Duration;

import java.io.File;

public class SongPlayer {

    private MediaPlayer mediaPlayer;

    // Play the song
    public void playSong(Song songToPlay) {
        // Check if the song file exists
        if (songToPlay.getSongFile() == null || !songToPlay.getSongFile().exists()) {
            System.out.println("Invalid song file.");
            return;
        }

        // Create a Media object from the song's file path
        File songFile = songToPlay.getSongFile();
        Media media = new Media(songFile.toURI().toString());

        // Create a MediaPlayer to control playback
        mediaPlayer = new MediaPlayer(media);

        // Set up media player events
        mediaPlayer.setOnReady(() -> {
            System.out.println("Media is ready to play.");
            System.out.println("Song: " + songToPlay.getSongTitle() + " - " + songToPlay.getSongArtist());
        });

        mediaPlayer.setOnEndOfMedia(() -> {
            System.out.println("Song finished playing.");
            mediaPlayer.stop();
        });

        // Play the media
        mediaPlayer.play();
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

    // Check if the song is currently playing
    public boolean isPlaying() {
        if (mediaPlayer != null) {
            return mediaPlayer.getStatus() == MediaPlayer.Status.PLAYING;
        }
        return false;
    }

    // Get the current play time of the song
    public String getCurrentTime() {
        if (mediaPlayer != null) {
            Duration currentTime = mediaPlayer.getCurrentTime();
            int minutes = (int) currentTime.toMinutes();
            int seconds = (int) currentTime.toSeconds() % 60;
            return String.format("%d:%02d", minutes, seconds);
        }
        return "0:00";
    }
}
