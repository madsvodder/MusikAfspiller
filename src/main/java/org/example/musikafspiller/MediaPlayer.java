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

    private MainViewController mainViewController;
    Logger logger = Logger.getLogger(MediaPlayer.class.getName());


    @Getter @Setter private boolean shuffle = false;
    @Getter private StringProperty currentTimeProperty = new SimpleStringProperty("0:00");

    private MusicCollection musicCollection;
    @Getter private javafx.scene.media.MediaPlayer mediaPlayer;

    private ArrayList<Song> songQueue = new ArrayList<>();
    private Song lastPlayedSong;
    private Double mediaVolume = 0.5;
    @Getter @Setter @JsonIgnore private int currentSongIndex = 0;
    @Getter boolean isSongPlaying = false;

    // Constructor
    public MediaPlayer(MainViewController mainViewController) {
        this.mainViewController = mainViewController;
    }

    /* Public Methods */

    public void playSong(Song songToPlay, MusicCollection collectionToPlayer) {
        if (!isValidSong(songToPlay)) return;

        cleanupMediaPlayer();
        musicCollection = collectionToPlayer;
        doPlaySong(songToPlay);
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
        }
    }

    public void resumeSong() {
        if (mediaPlayer != null) {
            mediaPlayer.play();
        }
    }

    public void stopSong() {
        cleanupMediaPlayer();
        mainViewController.updateSongUI(null);
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

    public void addSongToQueue(Song songToAdd) {
        songQueue.add(songToAdd);
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
    }

    /* Private Methods */

    private void doPlaySong(Song songToPlay) {
        File songFile = songToPlay.getSongFile();
        Media media = new Media(songFile.toURI().toString());

        mediaPlayer = new javafx.scene.media.MediaPlayer(media);
        mediaPlayer.setVolume(mediaVolume);

        bindCurrentTimeProperty();
        setupMediaPlayerEvents(songToPlay);

        mediaPlayer.play();
        mainViewController.updateSongUI(songToPlay);
        isSongPlaying = true;
    }

    private void setupMediaPlayerEvents(Song songToPlay) {
        mediaPlayer.setOnReady(() -> handleMediaReady(songToPlay));
        mediaPlayer.setOnEndOfMedia(() -> handleMediaEnd());
    }

    private void bindCurrentTimeProperty() {
        mediaPlayer.currentTimeProperty().addListener((observable, oldTime, newTime) ->
                currentTimeProperty.set(formatDuration(newTime))
        );
    }

    private void handleMediaReady(Song songToPlay) {
        logger.info("Now playing: " + songToPlay.getSongTitle() + " by " + songToPlay.getSongArtist());
        currentSongIndex = musicCollection.getSongs().indexOf(songToPlay);
        lastPlayedSong = songToPlay;
    }

    private void handleMediaEnd() {
        logger.info("Song finished playing");
        skipSong();
    }

    private void playNextInQueue() {
        doPlaySong(songQueue.remove(0));
    }

    private void playRandomSong() {
        doPlaySong(musicCollection.getRandomSong(lastPlayedSong));
    }

    private void playNextSong() {
        doPlaySong(getNextSong());
    }

    private void handleNoSongsAvailable() {
        mainViewController.updateSongUI(null);
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
        return musicCollection.getSongs().get(--currentSongIndex);
    }

    private Song getNextSong() {
        if (!isNextSongAvailable()) return null;
        return musicCollection.getSongs().get(++currentSongIndex);
    }

    private boolean isNextSongAvailable() {
        return musicCollection != null && currentSongIndex < musicCollection.getSongs().size() - 1;
    }

    private boolean isPreviousSongAvailable() {
        return musicCollection != null && currentSongIndex > 0;
    }

    private String formatDuration(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) duration.toSeconds() % 60;
        return String.format("%d:%02d", minutes, seconds);
    }
}
