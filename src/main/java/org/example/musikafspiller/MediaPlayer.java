package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.media.Media;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.logging.Logger;

public class MediaPlayer {

    private PlayerBarController playerBarController;
    Logger logger = Logger.getLogger(MediaPlayer.class.getName());


    @Getter @Setter private boolean shuffle = false;
    @Getter private StringProperty currentTimeProperty = new SimpleStringProperty("0:00");

    @Getter @Setter
    private MusicCollection currentPlayingMusicCollection;

    @Getter private javafx.scene.media.MediaPlayer mediaPlayer;

    @Getter
    private ArrayList<Song> songQueue = new ArrayList<>();
    private Song lastPlayedSong;
    private Double mediaVolume = 0.5;
    @Getter @Setter @JsonIgnore private int currentSongIndex = 0;
    @Getter boolean isSongPlaying = false;

    // Constructor
    public MediaPlayer(PlayerBarController playerBarController) {
        this.playerBarController = playerBarController;
    }

    /* Public Methods */

    public void playSong(Song songToPlay, MusicCollection collectionToPlay) {
        if (!isValidSong(songToPlay)) return;
        cleanupMediaPlayer();
        currentPlayingMusicCollection = collectionToPlay;

        doPlaySong(songToPlay);
    }

    public void removeLastSongFromQueue() {
        System.out.println("Removing last song from queue");
        if (lastPlayedSong == null) {
            System.out.println("No last played song to remove.");
            return;
        }

        if (songQueue.contains(lastPlayedSong)) {
            songQueue.remove(lastPlayedSong);
            System.out.println("Removed last played song from queue: " + lastPlayedSong);
        } else {
            System.out.println("Last played song not found in queue: " + lastPlayedSong);
        }

        // Update the queue view to reflect the changes
        playerBarController.queueViewController.refreshQueue(getSongQueue());

        System.out.println("Current queue size after removal: " + songQueue.size());
    }


    public void skipSong() {
        cleanupMediaPlayer();
        isSongPlaying = false;

        if (!songQueue.isEmpty()) {
            playNextInQueue();
        } else if (shuffle) {
            playRandomSong();
        } else if (isNextSongAvailable()) {
            playNextSong();
        } else {
            handleNoSongsAvailable();
        }
    }

    public void previousSong() {
        cleanupMediaPlayer();

        if (isPreviousSongAvailable()) {
            doPlaySong(getPreviousSong());
        } else {
            handleNoSongsAvailable();
        }
    }

    public void pauseSong() {
        if (mediaPlayer != null) {
            mediaPlayer.pause();
            isSongPlaying = false;
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
            isSongPlaying = true;
        }
    }

    public void stopSong() {
        cleanupMediaPlayer();
        playerBarController.updateSongUI(null);
        isSongPlaying = false;
    }

    public boolean isSongPlaying() {
        return mediaPlayer != null && mediaPlayer.getStatus() == javafx.scene.media.MediaPlayer.Status.PLAYING;
    }

    public void adjustVolume(double volume) {
        mediaVolume = volume;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(mediaVolume);
        }
    }

    public Duration getCurrentTime() {
        return (mediaPlayer != null) ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }

    public void addSongToQueue(Song songToAdd, MusicCollection collectionToPlay) {
        songQueue.add(songToAdd);
    }

    public void removeSongFromQueue(Song songToRemove) {
        songQueue.remove(songToRemove);
        System.out.println("Song Removed From Queue");
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
    }

    /* Private Methods */

    private void doPlaySong(Song songToPlay) {
        // Remove the last played song from the queue
        removeLastSongFromQueue();

        // Prepare to play the song
        File songFile = songToPlay.getSongFile();
        Media media = new Media(songFile.toURI().toString());

        mediaPlayer = new javafx.scene.media.MediaPlayer(media);
        mediaPlayer.setVolume(mediaVolume);

        bindCurrentTimeProperty();
        setupMediaPlayerEvents(songToPlay);

        // Play the song
        mediaPlayer.play();
        playerBarController.updateSongUI(songToPlay);
        isSongPlaying = true;
        lastPlayedSong = songToPlay;
        songToPlay.setAmountOfPlays(songToPlay.getAmountOfPlays() + 1);

        // Log the song details
        logger.info("Now playing: " + songToPlay.getSongTitle() + " by " + songToPlay.getSongArtist());

        // Update the current song index based on the current collection
        currentSongIndex = currentPlayingMusicCollection.getSongs().indexOf(songToPlay);
        System.out.println("Song has plays; now playing: " + songToPlay.getAmountOfPlays());
    }


    private void setupMediaPlayerEvents(Song songToPlay) {
        mediaPlayer.setOnEndOfMedia(() -> handleMediaEnd());
    }

    private void bindCurrentTimeProperty() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) ->
                currentTimeProperty.set(formatDuration(newTime))
        );
    }

    private void handleMediaEnd() {
        logger.info("Song finished playing");
        skipSong();
    }

    private void playNextInQueue() {
        doPlaySong(songQueue.remove(0));
    }

    private void playRandomSong() {
        doPlaySong(currentPlayingMusicCollection.getRandomSong(lastPlayedSong));
    }

    private void playNextSong() {
        doPlaySong(getNextSong());
    }

    private void handleNoSongsAvailable() {
        playerBarController.updateSongUI(null);
        logger.info("No songs available to play.");
    }

    private boolean isValidSong(Song song) {
        if (song == null || song.getSongFile() == null || !song.getSongFile().exists()) {
            logger.warning("Invalid song.");
            return false;
        }
        return true;
    }

    private void cleanupMediaPlayer() {
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
    }

    /* Helper Methods */

    private Song getPreviousSong() {
        if (!isPreviousSongAvailable()) return null;
        return currentPlayingMusicCollection.getSongs().get(--currentSongIndex);
    }

    private Song getNextSong() {
        if (!isNextSongAvailable()) return null;
        return currentPlayingMusicCollection.getSongs().get(++currentSongIndex);
    }

    private boolean isNextSongAvailable() {
        return currentPlayingMusicCollection != null && currentSongIndex < currentPlayingMusicCollection.getSongs().size() - 1;
    }

    private boolean isPreviousSongAvailable() {
        return currentPlayingMusicCollection != null && currentSongIndex > 0;
    }

    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
