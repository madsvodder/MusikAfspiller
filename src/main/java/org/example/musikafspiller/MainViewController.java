package org.example.musikafspiller;

import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.effect.ColorAdjust;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.util.Duration;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.TaskProgressView;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;
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
    @FXML
    private Button button_Shuffle;
    @FXML
    private ImageView imgview_Shuffle;

    // These images are the ones that we change during runtime.
    private Image playImage;
    private Image pauseImage;
    private Image musicRecordImage;


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
    @Getter
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

        // Load images
        playImage = new Image(getClass().getResourceAsStream("/images/LightImages/CircledPlay.png"));
        pauseImage = new Image(getClass().getResourceAsStream("/images/LightImages/PauseButton.png"));
        musicRecordImage = new Image(getClass().getResourceAsStream("/images/LightImages/MusicRecord.png"));


        // Load everything
        if (dataSaver.doesSaveFileExist()) {
            load();
        } else {
            save();
        }
    }

    private Task<Void> createSongParsingTask() {
        return new Task<>() {
            @Override
            protected Void call() {
                updateTitle("Parsing Songs");
                SongParser songParser = new SongParser();

                List<File> audioFiles = getAudioFilesFromDocuments();
                System.out.println("Found " + audioFiles.size() + " audio files.");

                int totalFiles = audioFiles.size();
                int progress = 0;

                if (totalFiles == 0) {
                    updateMessage("No files found to parse.");
                    return null;
                }

                for (File file : audioFiles) {
                    try {
                        updateMessage("Parsing " + file.getName());
                        songParser.parseSong(file, cacheDataPath, userLibrary);
                        System.out.println("Parsing " + file.getName());

                        progress++;
                        updateProgress(progress, totalFiles);
                    } catch (Exception ex) {
                        updateMessage("Error parsing " + file.getName() + ": " + ex.getMessage());
                        ex.printStackTrace();
                    }
                }

                updateMessage("Parsing Complete");
                return null;
            }

        };
    }

    private void setupUserDocuments() {
        String userHome = System.getProperty("user.home");
        Path documentsFolder = Paths.get(userHome, "Documents");
        Path baseDir = documentsFolder.resolve("JavaMytunesPlayer");
        Path musicDir = baseDir.resolve("Music");
        Path saveDataDir = baseDir.resolve("SaveData");
        Path cacheDataDir = baseDir.resolve("CacheData");

        try {
            // Ensure that the base directory exists
            if (!Files.exists(baseDir)) {
                Files.createDirectory(baseDir);
                logger.info("Created base directory: " + baseDir);
            }

            // Create the subdirectories
            if (!Files.exists(musicDir)) {
                Files.createDirectory(musicDir);
                logger.info("Created music directory: " + musicDir);
            }

            if (!Files.exists(saveDataDir)) {
                Files.createDirectory(saveDataDir);
                logger.info("Created save data directory: " + saveDataDir);
            }

            if (!Files.exists(cacheDataDir)) {
                Files.createDirectory(cacheDataDir);
                logger.info("Created cache data directory: " + cacheDataDir);
            }

            // Set paths for the directories
            libraryPath = musicDir.toAbsolutePath().toString();
            saveDataPath = saveDataDir.toAbsolutePath().toString();
            cacheDataPath = cacheDataDir.toAbsolutePath().toString();

            // Log the save data path
            if (saveDataPath == null || saveDataPath.isEmpty()) {
                logger.severe("saveDataPath is null or empty. Cannot initialize DataSaver.");
                return;
            }

            // Initialize DataSaver with the saveDataPath
            dataSaver = new DataSaver(saveDataPath);
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

    // Method for adding a new playlist (called by Scene Builder)
    @FXML
    private void addNewPlaylistToSidebar() {
        // null indicates a new playlist
        addItemToSidebar(userLibrary.newPlaylist(), false);
    }

    private void addItemToSidebar(Object item, boolean isAlbum) {
        try {
            // Load FXML file and add it to the side
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist-item.fxml"));
            HBox playlistItem = loader.load();
            vbox_playlists.getChildren().add(playlistItem);

            logger.info("Added item to sidebar");

            // Get the controller from the FXML playlistitem
            PlaylistItemController playlistItemController = loader.getController();

            // Set reference to MainViewController
            playlistItemController.setMainViewController(this);

            // Set the item (either playlist or album) with proper type checking
            if (isAlbum) {
                // Check if it's an Album
                if (item instanceof Album) {
                    // Casting as Album
                    playlistItemController.setAlbum((Album) item);
                } else {
                    throw new IllegalArgumentException("Expected an Album, but got: " + item.getClass());
                }
            } else {
                // Check if it's a Playlist
                if (item instanceof Playlist) {
                    // Casting as Playlist
                    playlistItemController.setPlaylist((Playlist) item);
                } else {
                    throw new IllegalArgumentException("Expected a Playlist, but got: " + item.getClass());
                }
            }

            // Set the userData of the HBox to the controller
            playlistItem.setUserData(playlistItemController);

            // Initialize playlistItem with appropriate flag
            playlistItemController.customInitialize(isAlbum);
        } catch (IOException e) {
            logger.warning("Failed to add or load item: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warning(e.getMessage()); // Log the specific type error
        }
    }

    // Removes playlist or album from sidebar and in the user library
    public void removeItemFromSidebar(Object item, PlaylistItemController playlistItemController) {
        // Get the HBox (playlist item) from the controller
        HBox playlistItemBox = playlistItemController.getPlaylistItemBox();

        if (item instanceof Playlist) {
            // Remove the playlist item from the sidebar
            vbox_playlists.getChildren().remove(playlistItemBox);

            // Remove the playlist from the user library
            userLibrary.removePlaylist((Playlist) item);
            System.out.println("Removed playlist item: " + item);

            // If the selected playlist is the one being removed, reset the selectedPlaylist
            if (selectedPlaylist == item) {
                selectedPlaylist = null;
                // Switch to the first playlist if available
                if (!userLibrary.getPlaylists().isEmpty()) {
                    switchToPlaylistView(userLibrary.getPlaylists().getFirst());
                }
            }
        } else if (item instanceof Album) {
            // Remove the album item from the sidebar
            vbox_playlists.getChildren().remove(playlistItemBox);

            // Unlike the album in the user library
            userLibrary.unlikeAlbum((Album) item);
            System.out.println("Removed album item: " + item);
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
        albumsOverviewController.setMainViewController(this);
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
    public void onPlaylistSelected(Object item) {
        if (item != null) {
            switchToPlaylistView(item); // Switch to the playlist view
        }
    }

    // Method to switch to the selected playlist view
    private void switchToPlaylistView(Object item) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist-view.fxml"));
            BorderPane newView = loader.load();

            // Retrieve the controller instance created by FXMLLoader
            PlaylistViewController controller = loader.getController();

            if (item instanceof Playlist) {
                // Pass the required data to the controller
                controller.setPlaylist((Playlist) item);
                controller.setUserLibrary(userLibrary);
                controller.setMainViewController(this);
                controller.customInit(false);

                // Set reference to selected playlist
                selectedPlaylist = (Playlist) item;

                // Log for debugging
                logger.info("Switching to playlist view with playlist: " + (Playlist) item + " and userLibrary: " + userLibrary);
            } else if (item instanceof Album) {
                controller.setAlbum((Album) item);
                controller.setUserLibrary(userLibrary);
                controller.setMainViewController(this);
                controller.customInit(true);
                logger.info("Switching to album view with album: " + (Album) item + " and userLibrary: " + userLibrary);
            }

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
                    newAlbum.setAlbumArtPath(newSong.getAlbumCoverPath());
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
        mediaPlayer.setCurrentSongIndex(playlist.getSongs().indexOf(song));
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

    @FXML
    private void nextSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.skipSong();
        }
    }

    @FXML
    private void previousSong() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.previousSong();
        }
    }

    @FXML
    private void enableShuffle() {
        if (mediaPlayer != null) {
            mediaPlayer.shuffle();
            if (mediaPlayer.isShuffle()) {
                button_Shuffle.setOpacity(1);
            } else {
                button_Shuffle.setOpacity(0.3);
            }
        }
    }

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
                label_songDuration.setText(formatTime(Duration.seconds(targetSeconds))); // Update label while dragging
            });

            // Handle seeking when the user releases the slider
            slider_songProgress.setOnMouseReleased(event -> {
                seekToSliderPosition();
            });
        }
    }

    // Helper method to format Duration into a string (MM:SS)
    private String formatTime(Duration duration) {
        int minutes = (int) duration.toMinutes();
        int seconds = (int) (duration.toSeconds() % 60);
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Method to seek to the position when the user releases the slider
    private void seekToSliderPosition() {
        double targetTimeInSeconds = slider_songProgress.getValue();
        mediaPlayer.getMediaPlayer().seek(Duration.seconds(targetTimeInSeconds));
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

    public void handleLikeAlbum(Album albumToLike) {
        // If the album isnt liked, then like it
        if (!albumToLike.isLiked()) {
            userLibrary.likeAlbum(albumToLike);
            addItemToSidebar(albumToLike, true);
        } else {
            userLibrary.unlikeAlbum(albumToLike);
        }
    }

    @FXML
    public void save() {
        dataSaver.saveUserData(userLibrary);
    }

    @FXML
    public void load() {
        // Load user data from the file
        UserLibrary newuserLibrary = dataSaver.loadUserData();

        // If the loaded library is not null, replace the existing userLibrary
        if (newuserLibrary != null) {
            userLibrary = newuserLibrary;

            //Clear sidebar
            vbox_playlists.getChildren().clear();

            // Check if userLibrary has playlists
            System.out.println("Loaded user library: " + userLibrary);

            for (Playlist playlist : userLibrary.getPlaylists()) {
                System.out.println("Loading playlist: " + playlist);
                addItemToSidebar(playlist, false);
            }

            for (Album album : userLibrary.getAlbums()) {
                if (album.isLiked()) {
                    addItemToSidebar(album, true);
                }
            }

        } else {
            System.out.println("No user data found to load.");
        }
    }
}