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
    @FXML
    private ImageView image_currentAlbumPlaying;
    @FXML
    private Label label_CurrentSongName;
    @FXML
    private Label label_currentArtistName;

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
    private String cacheDataPath;

    // The main classes. The user library, and the media player.
    UserLibrary userLibrary = new UserLibrary();
    MediaPlayer mediaPlayer = new MediaPlayer(this);
    DataSaver dataSaver;

    // Initialize
    public void initialize() {
        // Set up the user documents folder
        setupUserDocuments();

        // Read all the songs in the documents folder
        SongParser songParser = new SongParser();
        songParser.parseSongs(userLibrary, getAudioFilesFromDocuments(), cacheDataPath);

        // Bind the label in the corner for the song duration
        label_songDuration.textProperty().bind(mediaPlayer.getCurrentTimeProperty());

        slider_songProgress.valueProperty().addListener((observable, oldValue, newValue) -> {
            progressBar_SongProgress.setProgress(newValue.doubleValue() / slider_songProgress.getMax());
        });

        // Load play and pause images
        playImage = new Image(getClass().getResourceAsStream("/images/CircledPlay.png"));
        pauseImage = new Image(getClass().getResourceAsStream("/images/PauseButton.png"));

        // Load everything
        //load();
    }


    private void setupUserDocuments() {
        String userHome = System.getProperty("user.home");
        Path documentsFolder = Paths.get(userHome, "Documents");
        Path baseDir = documentsFolder.resolve("JavaMytunesPlayer");
        Path musicDir = baseDir.resolve("Music");
        Path saveDataDir = baseDir.resolve("SaveData");
        Path cacheDataDir = baseDir.resolve("CacheData");

        try {
            if (!Files.exists(baseDir)) {
                Files.createDirectories(musicDir);
                Files.createDirectories(saveDataDir);
                logger.info("Created directories: " + baseDir);
            }

            libraryPath = musicDir.toAbsolutePath().toString();
            saveDataPath = saveDataDir.toAbsolutePath().toString();
            cacheDataPath = cacheDataDir.toAbsolutePath().toString();

            if (saveDataPath == null || saveDataPath.isEmpty()) {
                logger.severe("saveDataPath is null or empty. Cannot initialize DataSaver.");
                return;
            }

            dataSaver = new DataSaver(saveDataPath); // Initialize here.
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

            // Set reference to MainViewController
            playlistItemController.setMainViewController(this);

            // Create the new playlist object
            Playlist newPlaylist = userLibrary.newPlaylist();

            // Add the playlist to userLibrary
            userLibrary.addPlaylist(newPlaylist);

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
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.FLAC", "*.MP3", "*.WAV"));

        File selectedFile = fileChooser.showOpenDialog(anchorCenter.getScene().getWindow());

        if (selectedFile != null) {
            SongParser songParser = new SongParser();
            Song newSong = songParser.parseSong(selectedFile, cacheDataPath, userLibrary);

            if (newSong != null) {
                // Add song to the library
                userLibrary.addSong(newSong);

                // Check if album exists
                Album existingAlbum = userLibrary.findAlbum(newSong.getAlbumTitle());
                if (existingAlbum == null) {
                    // Create new album
                    Album newAlbum = new Album(newSong.getAlbumTitle(), newSong.getSongArtist(), newSong.getSongYear());
                    newAlbum.setAlbumArtPath(newSong.getAlbumArtPath());
                    newAlbum.addSongToAlbum(newSong);
                    userLibrary.addAlbum(newAlbum);
                } else {
                    // Add song to existing album
                    existingAlbum.addSongToAlbum(newSong);
                }
            }
        }
    }



    // Press play button for the ui
    @FXML
    private void onPressedPlay() {togglePlayPause();}

    // Play a specific song. This is also used when double-clicking a song in a playlist.
    public void playSongFromPlaylist(Song song, Playlist playlist) {
        mediaPlayer.getReadyToPlaySongInPlaylist(song, playlist);
        //image_PlayPause.setImage(pauseImage);
        //setupSongProgressSlider();
        //label_songDurationFinal.setText(song.getSongDurationFormatted());
        updateSongUI(song);
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

    public void updateSongUI(Song songToPlay) {
        label_currentArtistName.setText(songToPlay.getSongArtist());
        label_CurrentSongName.setText(songToPlay.getSongTitle());
        image_currentAlbumPlaying.setImage(songToPlay.getAlbumCover());
        setupSongProgressSlider();
        image_PlayPause.setImage(pauseImage);
        label_songDurationFinal.setText(songToPlay.getSongDurationFormatted());

    }

    @FXML
    public void save() {
    }

    @FXML
    public void load() {
    }

}