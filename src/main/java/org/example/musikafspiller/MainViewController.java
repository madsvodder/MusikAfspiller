package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.logging.Logger;

public class MainViewController {

    // Logger
    Logger logger = Logger.getLogger(MainViewController.class.getName());

    // JavaFX
    @FXML
    private AnchorPane anchorCenter;
    @FXML
    private VBox vbox_playlists;
    @FXML
    private Label label_songDuration;
    @FXML
    private ImageView image_PlayPause;
    @FXML
    private ProgressBar progressBar_SongProgress;
    @FXML
    private Slider slider_songProgress;

    // These images are the ones that we change during runtime.
    private Image playImage;
    private Image pauseImage;

    // A reference to the selected song that is playing or paused
    @Setter @Getter
    Song selectedSong;

    // Directory of the users music
    private String libraryPath;

    // The main classes. The user library, and the media player.
    UserLibrary userLibrary = new UserLibrary();
    MediaPlayer mediaPlayer = new MediaPlayer();

    // Initialize
    public void initialize() {
        // Set up the user documents folder
        setupUserDocuments();

        // Read all the songs in the documents folder
        parseSongs();

        // Bind the label in the corner for the song duration
        label_songDuration.textProperty().bind(mediaPlayer.getCurrentTimeProperty());

        slider_songProgress.valueProperty().addListener((observable, oldValue, newValue) -> {
            progressBar_SongProgress.setProgress(newValue.doubleValue() / slider_songProgress.getMax());
        });

        // Load play and pause images
        playImage = new Image(getClass().getResourceAsStream("/images/CircledPlay.png"));
        pauseImage = new Image(getClass().getResourceAsStream("/images/PauseButton.png"));
    }
    private void setupUserDocuments() {
        // Get the users home directory
        String userHome = System.getProperty("user.home");

        // Define the path to the documents
        Path documentsFolder = Paths.get(userHome, "Documents");

        // Define the name of the directory to create
        Path newDir = documentsFolder.resolve("JavaMytunesPlayer");

        // Define the name of the next folder, which is inside the first one (nested folder)
        Path nestedDir = newDir.resolve("Music");

        // Try to create the directory
        try {
            // Check if the directory already exists
            if (!Files.exists(newDir)) {
                // Create directory
                Files.createDirectories(nestedDir);
                libraryPath = nestedDir.toAbsolutePath().toString();
                logger.info("Created new directory: " + newDir.toString());
            } else {
                logger.info("Directory already exists: " + newDir.toString());
                libraryPath = nestedDir.toAbsolutePath().toString();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private ArrayList<File> getAudioFilesFromDocuments () {

            ArrayList<File> audioFiles = new ArrayList<>();

            if (libraryPath != null) {
                Path dir = Paths.get(libraryPath);
                try {
                    if (Files.exists(dir)) {
                        Files.walk(dir)
                                .filter(path -> Files.isRegularFile(path) && (
                                        path.toString().endsWith(".mp3") ||
                                                path.toString().endsWith(".flac") ||
                                                path.toString().endsWith(".wav")))
                                .forEach(path -> {
                                    File file = path.toFile();
                                    audioFiles.add(file);
                                    logger.info("Found audio file: " + file.getAbsolutePath());
                                });
                    } else {
                        logger.info("No audio files found in " + libraryPath);
                    }
                } catch (IOException e) {
                    logger.severe("Error reading files from the directory: " + libraryPath);
                    e.printStackTrace();
                }
            } else {
                logger.info("No audio files found in " + libraryPath);
            }
            return audioFiles;
        }
    private void parseSongs() {
        ArrayList<File> songsToParse = new ArrayList<>();

        SongParser songParser = new SongParser();

        songsToParse = getAudioFilesFromDocuments();

        for (File file : songsToParse) {

            Song newSong = songParser.parseSong(file);

            // Add the songs to the user library as "standalone"
            userLibrary.addSong(newSong);

            // If this album doesn't exist in the users library, create it here
            if (!userLibrary.doesAlbumExist(newSong.getAlbumTitle())) {
                // If the album doesn't exist, create a new one
                userLibrary.createNewAlbumFromSong(newSong);

            } else if (userLibrary.doesAlbumExist(newSong.getAlbumTitle())) {
                // If the imported song comes from the same album, then add it to the album
                Album album = userLibrary.findAlbum(newSong.getAlbumTitle());
                album.addSongToAlbum(newSong);
            }
        }

    }

    // Adds a new playlist to the ui, and creates a new one in the user library.
    @FXML
    private void addPlaylistToSidebar() {
        try {
            // Load FXML file and add it to the side
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist-item.fxml"));
            HBox playlistItem = loader.load();
            vbox_playlists.getChildren().add(playlistItem);

            logger.info("Added playlist item");

            // Get the controller from the FXML playlistitem
            PlaylistItemController playlistItemController = loader.getController();

            // Set reference to MenuController
            playlistItemController.setMainViewController(this);

            // Create a new playlist, and assign it to the controller
            playlistItemController.setPlaylist(userLibrary.newPlaylist());

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Window for viewing all the albums that's imported
    @FXML
    private void switchToAlbumsView() throws IOException {
        // Load the new FXML file (albums-overview.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("albums-overview.fxml"));
        BorderPane newView = loader.load();

        // Get the controller of the new FXML view
        AlbumsOverviewController albumsOverviewController = loader.getController();
        albumsOverviewController.setUserLibrary(userLibrary);
        albumsOverviewController.populateAlbumGrid();

        // Add the loaded view to the center of anchorCenter
        anchorCenter.getChildren().clear();
        anchorCenter.getChildren().add(newView);


        // Ensure the new view fills the AnchorPane completely by setting anchors
        AnchorPane.setTopAnchor(newView, 0.0);
        AnchorPane.setBottomAnchor(newView, 0.0);
        AnchorPane.setLeftAnchor(newView, 0.0);
        AnchorPane.setRightAnchor(newView, 0.0);
    }

    // This method runs when you select a playlist in the sidebar
    public void onPlaylistSelected(Playlist playlist) {
        if (playlist != null) {
            switchToPlaylistView(playlist); // Switch to the playlist view
        }
    }

    // Method to switch to the selected playlist view
    private void switchToPlaylistView(Playlist playlist) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist-view.fxml"));
            BorderPane newView = loader.load();

            // Retrieve the controller instance created by FXMLLoader
            PlaylistViewController controller = loader.getController();

            // Pass the required data to the controller
            controller.setPlaylist(playlist);
            controller.setUserLibrary(userLibrary);
            controller.setMainViewController(this);
            controller.customInit();

            // Log for debugging
            logger.info("Switching to playlist view with playlist: " + playlist + " and userLibrary: " + userLibrary);

            // Update the UI with the new view
            anchorCenter.getChildren().clear();
            anchorCenter.getChildren().add(newView);

            // Ensure the view fits the parent
            AnchorPane.setTopAnchor(newView, 0.0);
            AnchorPane.setBottomAnchor(newView, 0.0);
            AnchorPane.setLeftAnchor(newView, 0.0);
            AnchorPane.setRightAnchor(newView, 0.0);

        } catch (IOException e) {
            logger.severe("Error loading playlist-view.fxml");
            e.printStackTrace();
        }
    }

    // Method for importing a song using the file chooser
    @FXML
    private void importSong() {

        // create a File chooser
        FileChooser fil_chooser = new FileChooser();

        // Make a filter of which file types the user can import
        fil_chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.FLAC", "*.MP3", "*.WAV"));

        // Show to file chooser dialog window
        File selectedFile = fil_chooser.showOpenDialog(anchorCenter.getScene().getWindow());

        // Did the user select a file?
        if (selectedFile != null) {
            // Create a new songParser object
            SongParser songParser = new SongParser();

            // Grab all the metadata from the song file
            Song newSong = songParser.parseSong(selectedFile);

            // Add the song to the users library
            userLibrary.addSong(newSong);

            // Log
            logger.info("Added song: " + userLibrary.getSongs().getLast());

            // If this album doesn't exist in the users library, create it here
            if (!userLibrary.doesAlbumExist(newSong.getAlbumTitle())) {
                // If the album doesn't exist, create a new one
                userLibrary.createNewAlbumFromSong(newSong);

            } else if (userLibrary.doesAlbumExist(newSong.getAlbumTitle())) {
                // If the imported song comes from the same album, then add it to the album
                Album album = userLibrary.findAlbum(newSong.getAlbumTitle());
                album.addSongToAlbum(newSong);
            }
        }
    }

    // Press play button for the ui
    @FXML
    private void onPressedPlay() {
        togglePlayPause();
    }

    // Play a specific song. This is also used when double-clicking a song in a playlist.
    public void playSong(Song song) {
        mediaPlayer.playSong(song);
        image_PlayPause.setImage(pauseImage);
        setupSongProgressSlider();
    }

    // Toggle between Play and Pause
    private void togglePlayPause() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pauseSong();
            image_PlayPause.setImage(playImage);
        } else {
            mediaPlayer.resumeSong();
            image_PlayPause.setImage(pauseImage);
        }
    }

    private void setupSongProgressSlider() {
        if (mediaPlayer != null) {

            // Update slider max to match the song's duration once the media is ready
            mediaPlayer.getMediaPlayer().setOnReady(() -> {
                slider_songProgress.setMax(mediaPlayer.getMediaPlayer().getTotalDuration().toSeconds());
            });

            // Sync slider position with current time from custom MediaPlayer
            mediaPlayer.getCurrentTimeProperty().addListener((observable, oldValue, newValue) -> {
                String[] timeParts = newValue.split(":");
                int minutes = Integer.parseInt(timeParts[0]);
                int seconds = Integer.parseInt(timeParts[1]);
                double totalSeconds = minutes * 60 + seconds;

                slider_songProgress.setValue(totalSeconds);
            });

            // Allow user to seek to a new position by dragging the slider
            slider_songProgress.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
                if (!slider_songProgress.isValueChanging()) {
                    mediaPlayer.getMediaPlayer().seek(Duration.seconds(slider_songProgress.getValue()));
                }
            });

            // Allow user to click on the slider to jump to a position
            slider_songProgress.setOnMouseReleased(event -> {
                // Get the current value from the slider
                double targetTime = slider_songProgress.getValue();

                // Round to the nearest second (no fractional seconds)
                long roundedTime = Math.round(targetTime); // Round to the nearest whole second

                // Seek to the rounded time in seconds
                mediaPlayer.getMediaPlayer().seek(Duration.seconds(roundedTime));
            });
        }
    }
}