package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
    @FXML
    private Label label_songDurationFinal;

    // These images are the ones that we change during runtime.
    private Image playImage;
    private Image pauseImage;

    // A reference to the selected song that is playing or paused
    @Setter @Getter
    Song selectedSong;

    // Selected playlist
    Playlist selectedPlaylist;

    // Directory of the users music
    private String libraryPath;
    private String saveDataPath;
    private String savePlaylistPath;

    // The main classes. The user library, and the media player.
    UserLibrary userLibrary = new UserLibrary();
    MediaPlayer mediaPlayer = new MediaPlayer();
    DataSaver dataSaver;

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

        // Load everything
        load();
    }


    private void setupUserDocuments() {
        String userHome = System.getProperty("user.home");
        Path documentsFolder = Paths.get(userHome, "Documents");
        Path baseDir = documentsFolder.resolve("JavaMytunesPlayer");
        Path musicDir = baseDir.resolve("Music");
        Path saveDataDir = baseDir.resolve("SaveData");
        Path savePlaylistDataDir = saveDataDir.resolve("PlaylistData");

        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(musicDir);
                Files.createDirectories(saveDataDir);
                Files.createDirectories(savePlaylistDataDir);
                logger.info("Created directories: " + baseDir);
            }

            libraryPath = musicDir.toAbsolutePath().toString();
            saveDataPath = saveDataDir.toAbsolutePath().toString();
            savePlaylistPath = savePlaylistDataDir.toAbsolutePath().toString();

            if (saveDataPath == null || saveDataPath.isEmpty()) {
                logger.severe("saveDataPath is null or empty. Cannot initialize DataSaver.");
                return;
            }

            dataSaver = new DataSaver(saveDataPath, savePlaylistPath); // Initialize here.
            logger.info("DataSaver initialized with path: " + saveDataPath);

        } catch (IOException e) {
            logger.severe("Error creating directories: " + e.getMessage());
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
    public void parseSongs() {

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

            // Create the new playlist object
            Playlist newPlaylist = userLibrary.newPlaylist();

            // Create a new playlist, and assign it to the controller
            playlistItemController.setPlaylist(newPlaylist);

            // Set the userData of the HBox to the controller
            playlistItem.setUserData(playlistItemController);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    private void loadPlaylistToSidebar(Playlist playlist) {
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
            playlistItemController.setPlaylist(playlist);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updatePlaylistNameInSidebar(Playlist playlist) {
        // Print the UUID of the playlist being passed in
        logger.info("Attempting to update playlist with UUID: " + playlist.getUuid());

        for (Node node : vbox_playlists.getChildren()) {
            if (node instanceof HBox) {
                // Get the PlaylistItemController from userData
                PlaylistItemController itemController = (PlaylistItemController) node.getUserData();

                // Check if the controller is not null
                if (itemController != null) {
                    // Debug print for the UUID of the playlist in the sidebar
                    logger.info("Sidebar playlist UUID: " + itemController.getPlaylist().getUuid());

                    // Compare UUIDs instead of playlist names
                    if (itemController.getPlaylist().getUuid().equals(playlist.getUuid())) {
                        logger.info("UUIDs match! Updating playlist name...");
                        itemController.updatePlaylistNameUI();
                        break;
                    }
                } else {
                    logger.info("Item controller is null for this node.");
                }
            }
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

            // Set reference to selected playlist
            selectedPlaylist = playlist;

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
    private void onPressedPlay() {togglePlayPause();}

    // Play a specific song. This is also used when double-clicking a song in a playlist.
    public void playSong(Song song) {
        mediaPlayer.playSong(song);
        image_PlayPause.setImage(pauseImage);
        setupSongProgressSlider();
        label_songDurationFinal.setText(song.getSongDurationFormatted());
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

            // Set the max value for the slider when the media is ready
            mediaPlayer.getMediaPlayer().setOnReady(() ->
                    slider_songProgress.setMax(mediaPlayer.getMediaPlayer().getTotalDuration().toSeconds())
            );

            // Update the slider position based on the current time (but only if the user isn't dragging the slider)
            mediaPlayer.getCurrentTimeProperty().addListener((observable, oldValue, newValue) -> {
                if (!slider_songProgress.isValueChanging()) {
                    // Parse the time from newValue and convert to total seconds
                    String[] timeParts = newValue.split(":");
                    int minutes = Integer.parseInt(timeParts[0]);
                    int seconds = Integer.parseInt(timeParts[1]);
                    double totalSeconds = minutes * 60 + seconds; // Calculate total seconds

                    // Set the slider value rounded to two decimal places
                    slider_songProgress.setValue(Math.round(totalSeconds * 100.0) / 100.0);
                }
            });

            // Handle seeking when the user drags the slider or releases it
            slider_songProgress.setOnMouseReleased(event ->
                    seekToSliderPosition()
            );

            slider_songProgress.valueChangingProperty().addListener((observable, oldValue, newValue) -> {
                if (!slider_songProgress.isValueChanging()) {
                    seekToSliderPosition();
                }
            });
        }
    }

    private void seekToSliderPosition() {
        long targetTime = Math.round(slider_songProgress.getValue()); // Round to nearest second
        mediaPlayer.getMediaPlayer().seek(Duration.seconds(targetTime));
    }

    @FXML
    public void save() {
        if (dataSaver == null) {
            logger.severe("DataSaver is not initialized. Skipping save operation.");
            return;
        }

        try {
            for (Playlist playlist : userLibrary.getPlaylists()) {
                dataSaver.savePlaylist(playlist);
            }
            logger.info("Save operation completed successfully.");
        } catch (Exception e) {
            logger.severe("Error during save operation: " + e.getMessage());
            e.printStackTrace();
        }
    }

    @FXML
    private void load() {
        if (dataSaver == null) {
            logger.severe("DataSaver is not initialized. Skipping load operation.");
            return;
        }

        try {

            // Clear playlists and make space for the new ones
            userLibrary.clearPlaylists();

            for (Playlist playlists : dataSaver.findPlaylists()) {
                userLibrary.addPlaylist(playlists);
                loadPlaylistToSidebar(playlists);
            }

        } catch (Exception e) {
            logger.severe("Error during load operation: " + e.getMessage());
            e.printStackTrace();
        }
    }
}