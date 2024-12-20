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
import java.util.Collections;
import java.util.logging.Logger;

public class CustomMediaPlayer {

    @Setter
    MainViewController mainViewController;
    private PlayerBarController playerBarController;
    private static final Logger logger = Logger.getLogger(CustomMediaPlayer.class.getName());

    @Getter @Setter private boolean shuffle = false;
    @Getter private StringProperty currentTimeProperty = new SimpleStringProperty("0:00");

    @Getter @Setter private MusicCollection currentPlayingMusicCollection;

    @Getter private javafx.scene.media.MediaPlayer mediaPlayer;

    @Getter private ArrayList<Song> songQueue = new ArrayList<>();
    private Song lastPlayedSong;

    private double lastMediaVolume;
    @Getter private Double mediaVolume = 1.0;
    @Getter private boolean muted = false;

    @Setter
    UserLibrary userLibrary;

    private ArrayList<Song> playedSongs = new ArrayList<>();

    @Getter @Setter @JsonIgnore private int currentSongIndex = 0;

    @Getter
    public boolean isSongPlaying = false;

    public void mute() {
        if (!muted) {
            muted = true;
            lastMediaVolume = mediaVolume;
            adjustVolume(0);
        } else {
            muted = false;
            adjustVolume(lastMediaVolume);
        }

        System.out.println("Muted: " + muted);
    }

    public CustomMediaPlayer(PlayerBarController playerBarController) {
        this.playerBarController = playerBarController;
    }


    // Get ready to play a song. Executed when a user double-clicks a song
    public void playSong(Song songToPlay, MusicCollection collectionToPlay) {

        if (songToPlay.isSongFileValid()) {
            cleanupMediaPlayer();
            currentPlayingMusicCollection = collectionToPlay;

            startPlayingSong(songToPlay, true);
        } else {
            logger.warning("Song file does not exist or is not a file.");
            mainViewController.showFileNotFoundPrompt(songToPlay);
        }
    }

    public void skipSong() {

        if (mediaPlayer != null) {
            cleanupMediaPlayer();
            isSongPlaying = false;

            if (!songQueue.isEmpty()) {
                System.out.println("Play Next In Queue");
                playNextInQueue();
            } else if (shuffle) {
                playRandomSong();
            } else if (isNextSongAvailable()) {
                playNextSong();
            } else {
                handleNoSongsAvailable();
            }

            playerBarController.queueViewController.refreshQueue(getSongQueue());
        }
    }

    public void previousSong() {
        cleanupMediaPlayer();
        if (isPreviousSongAvailable()) {
            startPlayingSong(getPreviousSong(), false); // Automatisk
        } else {
            handleNoSongsAvailable();
        }

        playerBarController.queueViewController.refreshQueue(getSongQueue());
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

    public void shuffleQueue() {
        Collections.shuffle(songQueue);
        logger.info("Queue shuffled");
    }

    public void adjustVolume(double volume) {
        mediaVolume = volume;
        if (mediaPlayer != null) {
            mediaPlayer.setVolume(mediaVolume);

            if (volume == 0.0) {
                muted = true;
            } else {
                muted = false;
            }
        }
    }

    public Duration getCurrentTime() {
        return (mediaPlayer != null) ? mediaPlayer.getCurrentTime() : Duration.ZERO;
    }

    public void addSongToQueue(Song songToAdd, MusicCollection collectionToPlay) {
        if (!isValidSong(songToAdd)) {
            logger.warning("Cannot add invalid song to queue.");
            return;
        }
        songQueue.add(songToAdd);
        logger.info("Song added to queue: " + songToAdd.getSongTitle());
    }

    public void removeSongFromQueue(Song songToRemove) {
        songQueue.remove(songToRemove);
        logger.info("Song Removed From Queue");
    }

    public void toggleShuffle() {
        shuffle = !shuffle;
        //shuffleQueue();
    }

    /* Private Methods */

    private void startPlayingSong(Song songToPlay, boolean isManualPlay) {

        try {
            if (songToPlay.getSongFile() == null || !songToPlay.getSongFile().exists()) {
                logger.warning("Song file does not exist or is not a file.");
                mainViewController.showFileNotFoundPrompt(songToPlay);

                // Fixes bug where queue stops if a song is not valid.
                if (!isManualPlay && lastPlayedSong != null) {
                    System.out.println("Trying next song!");
                    playNextSong();
                }

                return;
            }

            if (!isManualPlay && lastPlayedSong != null) {
                //removeLastSongFromQueue();

                // Add the last played song. I dont even remember what this is used for.
                // Prorably for fixing the annoying queue bugs
                playedSongs.add(lastPlayedSong);
            }

            File songFile = songToPlay.getSongFile();
            Media media = new Media(songFile.toURI().toString());

            mediaPlayer = new javafx.scene.media.MediaPlayer(media);
            mediaPlayer.setVolume(mediaVolume);

            bindCurrentTimeProperty();
            setupMediaPlayerEvents(songToPlay);

            mediaPlayer.play();
            playerBarController.updateSongUI(songToPlay);
            isSongPlaying = true;
            lastPlayedSong = songToPlay;

            Song userLibrarySong = userLibrary.findSong(songToPlay.getSongTitle());

            userLibrarySong.increasePlays();

            System.out.println("Song: " + userLibrarySong + " played " + userLibrarySong.getAmountOfPlays() + " times." );

            logger.info("Now playing: " + songToPlay.getSongTitle() + " by " + songToPlay.getSongArtist());
            currentSongIndex = currentPlayingMusicCollection.getSongs().indexOf(songToPlay);
        } catch (Exception e) {
            logger.severe("Error while playing song: " + e.getMessage());
            handleNoSongsAvailable();
        }
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
        skipSong();
    }

    private void playNextInQueue() {
        // Remove and get the first song in the queue
        Song nextSong = songQueue.remove(0);
        startPlayingSong(nextSong, false);
    }

    private void playRandomSong() {
        startPlayingSong(currentPlayingMusicCollection.getRandomSong(lastPlayedSong), false); // Automatisk
    }

    private void playNextSong() {
        startPlayingSong(getNextSong(), false);
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
            try {
                mediaPlayer.stop();
                mediaPlayer.dispose();
            } catch (Exception e) {
                logger.severe("Error while cleaning up media player: " + e.getMessage());
            } finally {
                mediaPlayer = null;
            }
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
