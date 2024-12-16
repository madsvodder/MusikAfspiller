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
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Logger;

public class MediaPlayer {

    private static final Logger logger = Logger.getLogger(MediaPlayer.class.getName());

    private PlayerBarController playerBarController;

    @Getter @Setter private boolean shuffle = false;
    @Getter private StringProperty currentTimeProperty = new SimpleStringProperty("0:00");

    @Getter @Setter
    private MusicCollection currentPlayingMusicCollection;

    @Getter
    private javafx.scene.media.MediaPlayer mediaPlayer;

    @Getter
    private ArrayList<Song> songQueue = new ArrayList<>();
    private Song lastPlayedSong;
    private double mediaVolume = 0.5;

    @Getter @Setter @JsonIgnore private int currentSongIndex = 0;
    @Getter private boolean isSongPlaying = false;

    @Getter @Setter
    private boolean isPlayingFromQueue = false;

    private boolean isManualPlay = false;

    // Constructor
    public MediaPlayer(PlayerBarController playerBarController) {
        this.playerBarController = playerBarController;
    }

    /* Public Methods */

    public void playSong(Song songToPlay, MusicCollection collectionToPlay) {
        if (!isValidSong(songToPlay)) return;
        cleanupMediaPlayer();

        isPlayingFromQueue = false;
        isManualPlay = true;

        currentPlayingMusicCollection = collectionToPlay;
        doPlaySong(songToPlay);
    }

    public void removeLastSongFromQueue() {
        if (!isPlayingFromQueue || lastPlayedSong == null) {
            logger.info("No song to remove or not playing from queue.");
            return;
        }

        if (songQueue.remove(lastPlayedSong)) {
            logger.info("Song removed from queue.");
        } else {
            logger.warning("Song not found in queue.");
        }

        playerBarController.queueViewController.refreshQueue(songQueue);
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
            Song previousSong = getPreviousSong();
            doPlaySong(previousSong);
        } else {
            logger.info("No previous song available. Staying at the current song.");
            handleNoPreviousSong();
        }
    }

    private void handleNoPreviousSong() {
        logger.info("Restarting the current song.");
        doPlaySong(currentPlayingMusicCollection.getSongs().get(currentSongIndex));
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

    public void adjustVolume(double volume) {
        mediaVolume = volume;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(mediaVolume);
        }
    }

    public Duration getCurrentTime() {
        return mediaPlayer != null ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }

    public void addSongToQueue(Song songToAdd, MusicCollection collectionToPlay) {
        songQueue.add(songToAdd);
    }

    public void removeSongFromQueue(Song songToRemove) {
        if (songQueue.remove(songToRemove)) {
            logger.info("Song removed from queue.");
        } else {
            logger.warning("Song not found in queue.");
        }
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
    }

    /* Private Methods */

    private void doPlaySong(Song songToPlay) {
        if (!isManualPlay && isPlayingFromQueue && songQueue.contains(lastPlayedSong)) {
            removeLastSongFromQueue();
        }

        isManualPlay = false;
        isPlayingFromQueue = false;

        File songFile = songToPlay.getSongFile();
        try {
            Media media = new Media(songFile.toURI().toString());
            mediaPlayer = new javafx.scene.media.MediaPlayer(media);
        } catch (Exception e) {
            logger.severe("Error initializing media player: " + e.getMessage());
            return;
        }

        mediaPlayer.setVolume(mediaVolume);
        bindCurrentTimeProperty();
        setupMediaPlayerEvents(songToPlay);

        mediaPlayer.play();
        playerBarController.updateSongUI(songToPlay);
        isSongPlaying = true;
        lastPlayedSong = songToPlay;
        songToPlay.setAmountOfPlays(songToPlay.getAmountOfPlays() + 1);

        logger.info("Now playing: " + songToPlay.getSongTitle() + " by " + songToPlay.getSongArtist());

        currentSongIndex = currentPlayingMusicCollection.getSongs().indexOf(songToPlay);

        logger.info("Song has plays; now playing: " + songToPlay.getAmountOfPlays());
    }

    private void setupMediaPlayerEvents(Song songToPlay) {
        mediaPlayer.setOnEndOfMedia(this::handleMediaEnd);
    }

    private void bindCurrentTimeProperty() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) ->
                currentTimeProperty.set(formatDuration(newTime))
        );
    }

    private void handleMediaEnd() {
        logger.info("Song finished playing");

        if (!songQueue.isEmpty()) {
            isPlayingFromQueue = true;
            playNextInQueue();
        } else {
            isPlayingFromQueue = false;
            skipSong();
        }
    }

    private void playNextInQueue() {
        isPlayingFromQueue = true;
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
        logger.info("Cleaning up media player...");
        if (mediaPlayer != null) {
            mediaPlayer.stop();
            mediaPlayer.dispose();
            mediaPlayer = null;
        }
        logger.info("Media player cleaned up.");
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
