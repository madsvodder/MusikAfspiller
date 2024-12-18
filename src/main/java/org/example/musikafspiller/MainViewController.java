package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
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
    private BorderPane bp_mainBorderPane;
    @FXML
    private VBox vbox_queue;

    private VBox vboxQueueSidebar;

    @Getter @Setter
    private Stage primaryStage;

    // List of all sidebar items. Playlists and albums
    private List<PlaylistItemController> sidebarItems = new ArrayList<>();

    // Reference to the current viewed playlist / album controller
    private PlaylistViewController currentPlaylistViewController;

    // Reference to the albums overview controller. Used for refreshing
    AlbumsOverviewController albumsOverviewController;

    // A reference to the selected song that is playing or paused
    @Setter @Getter
    Song selectedSong;

    // Reference to the current viewed playlist / album
    MusicCollection selectedMusicCollection;

    // Directory of the users music
    private String libraryPath;
    private String saveDataPath;
    private String cacheDataPath;

    // The main classes. The user library, and the media player.
    @Getter
    UserLibrary userLibrary = new UserLibrary();

    DataSaver dataSaver;

    @Setter
    PlayerBarController playerBarController;

    // Track queue sidebar visibility
    private boolean isQueueVisible = true;

    // Initialize
    public void initialize() {

        // Set the reference to the user library so it can scan all the files on start
        userLibrary.setMainViewController(this);

        // Set up the user documents folder
        setupUserDocuments();

        // If the user data file exists, load saved data
        if (dataSaver.doesSaveFileExist()) {
            UserLibrary loadedLibrary = dataSaver.loadUserData();

            // Combine saved data with the existing structure
            if (loadedLibrary != null) {
                if (loadedLibrary.getPlaylists() != null) {
                    userLibrary.getPlaylists().addAll(loadedLibrary.getPlaylists());
                }
                if (loadedLibrary.getAlbums() != null) {
                    userLibrary.getAlbums().addAll(loadedLibrary.getAlbums());
                }
                if (loadedLibrary.getSongs() != null) {
                    userLibrary.getSongs().addAll(loadedLibrary.getSongs());
                }
                System.out.println("Loaded user library: " + userLibrary);
            } else {
                System.out.println("No user data found to load. Starting with an empty library.");
            }
        }

        // Validate and remove all invalid songs
        userLibrary.validateLibraryFiles();

        // Parse only new songs from disk
        SongParser songParser = new SongParser();
        List<File> audioFiles = getAudioFilesFromDocuments(); // Get all files from disk

        // Add songs from disk that do not exist in the library
        List<File> newFiles = audioFiles.stream()
                .filter(file -> !userLibrary.containsSongFile(file))
                .toList();
        songParser.parseSongs(userLibrary, new ArrayList<>(newFiles), cacheDataPath);

        // Initialize UI elements
        setupPlayerBar();
        setupQueueSidebar();

        // Add saved playlists and albums to the UI
        reloadSidebar();

        // Save updated user data
        save();
    }


    private void reloadSidebar() {

        // Clear sidebar
        vbox_playlists.getChildren().clear();

        // Add playlists to sidebar
        for (Playlist playlist : userLibrary.getPlaylists()) {
            System.out.println("Loading playlist into sidebar: " + playlist.getCollectionName());
            addItemToSidebar(playlist);
        }

        // Add albums to sidebar, if they are liked
        for (Album album : userLibrary.getAlbums()) {
            if (album.isLiked()) {
                System.out.println("Loading liked album into sidebar: " + album.getCollectionName());
                addItemToSidebar(album);
            }
        }
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

    private ArrayList<File> getAudioFilesFromDocuments() {

        // Initialize a list to store audio files
        ArrayList<File> audioFiles = new ArrayList<>();

        // Check if the library path is defined
        if (libraryPath != null) {
            Path dir = Paths.get(libraryPath);
            try {
                // Check if the directory exists
                if (Files.exists(dir)) {
                    // Walk the directory and find audio files with supported extensions
                    Files.walk(dir)
                            .filter(path -> Files.isRegularFile(path) && (
                                    path.toString().endsWith(".mp3") ||  // Check for MP3 files
                                            path.toString().endsWith(".flac") || // Check for FLAC files
                                            path.toString().endsWith(".wav")))  // Check for WAV files
                            .forEach(path -> {
                                File file = path.toFile();

                                // Add the file to the list
                                audioFiles.add(file);

                                // Log the file path
                                logger.info("Found audio file: " + file.getAbsolutePath());
                            });
                } else {
                    // Log if the directory does not contain audio files
                    logger.info("No audio files found in " + libraryPath);
                }
            } catch (IOException e) {
                // Handle any errors encountered while reading files from the directory
                logger.severe("Error reading files from the directory: " + libraryPath);
                e.printStackTrace();
            }
        } else {
            // Log if the library path is not defined
            logger.info("No audio files found in " + libraryPath);
        }

        // Return the list of audio files
        return audioFiles;
    }


    private void setupPlayerBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playerbar-view.fxml"));

            // Load the FXML file into an HBox
            HBox playerBar = loader.load();

            // Set the loaded node as the bottom of the BorderPane.
            bp_mainBorderPane.setBottom(playerBar);

            // Add 10px margin on all sides
            BorderPane.setMargin(playerBar, new Insets(10, 10, 10, 10));

            // Setup controller stuff and init the playerbar
            this.playerBarController = loader.getController();
            playerBarController.setMainViewController(this);
            playerBarController.setUserLibrary(userLibrary);
            playerBarController.customInit();

            logger.info("PlayerBar initialized" + playerBarController);
        } catch (IOException e) {
            throw new RuntimeException("Failed to load player bar view", e);
        }
    }

    public void setupQueueSidebar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("queue-view.fxml"));
            VBox vboxQueue = loader.load();
            vboxQueueSidebar = vboxQueue;
            QueueViewController queueViewController = loader.getController();

            // Dont show the queue on startup
            //bp_mainBorderPane.setRight(vboxQueue);

            // Add 10 margin
            BorderPane.setMargin(vboxQueue, new Insets(10, 10, 10, 0));

            // Initialize the queue
            queueViewController.customInit(playerBarController.getMediaPlayer());

            playerBarController.setQueueViewController(queueViewController);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public void toggleQueueSidebar() {
        if (isQueueVisible) {

            // Hide the queue if visible
            bp_mainBorderPane.setRight(null);
        } else {
            // Show the queue
            bp_mainBorderPane.setRight(vboxQueueSidebar);
        }
        // Toggle boolean
        isQueueVisible = !isQueueVisible;
    }

    // Method for adding a new playlist (called by Scene Builder)
    @FXML
    private void addNewPlaylistToSidebar() {
        addItemToSidebar(userLibrary.newPlaylist());
    }

    private void addItemToSidebar(Object item) {
        try {

            // Load FXML file and add it to the side
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist-item.fxml"));
            HBox playlistItem = loader.load();
            vbox_playlists.getChildren().add(playlistItem);

            logger.info("Added item to sidebar");

            // Get the controller from the FXML
            PlaylistItemController playlistItemController = getPlaylistItemController(item, loader);

            // Set the userData of the HBox to the controller.
            // I have no idea why, but it breaks if i don't do it
            playlistItem.setUserData(playlistItemController);

            // Add controller to the sidebarItems list
            sidebarItems.add(playlistItemController);
            logger.info("Added item to sidebarItems list");

        } catch (IOException e) {
            logger.warning("Failed to add or load item: " + e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.warning(e.getMessage()); // Log the specific type error
        }
    }

    private PlaylistItemController getPlaylistItemController(Object item, FXMLLoader loader) {
        PlaylistItemController playlistItemController = loader.getController();

        // Set reference to MainViewController
        playlistItemController.setMainViewController(this);

        // Determine the type of item and initialize appropriately
        if (item instanceof Album) {
            playlistItemController.setAlbum((Album) item);
            playlistItemController.initializeAsAlbum();
        } else if (item instanceof Playlist) {
            playlistItemController.setPlaylist((Playlist) item);
            playlistItemController.initializeAsPlaylist();
        } else {
            throw new IllegalArgumentException("Unsupported item type: " + item.getClass());
        }
        return playlistItemController;
    }

    // Removes playlist or album from sidebar and in the user library
    public void removeItemFromSidebar(MusicCollection musicCollection, PlaylistItemController playlistItemController) {
        // Get the HBox (playlist item) from the controller
        HBox playlistItemBox = playlistItemController.getPlaylistItemBox();

        if (musicCollection instanceof Playlist) {
            // Remove the playlist item from the sidebar
            vbox_playlists.getChildren().remove(playlistItemBox);
            userLibrary.removePlaylist((Playlist) musicCollection);
            System.out.println("Removed playlist item: " + musicCollection);
        } else if (musicCollection instanceof Album) {
            // Remove the album item from the sidebar
            vbox_playlists.getChildren().remove(playlistItemBox);
            userLibrary.unlikeAlbum((Album) musicCollection);
            System.out.println("Removed album item: " + musicCollection);
        }

        // Remove the controller from the sidebarItems list
        sidebarItems.remove(playlistItemController);

        if (userLibrary.getPlaylists().isEmpty()) {
            switchToAlbumsView();
        }
    }


    // This is a mess
    public void updatePlaylistNameInSidebar(Playlist playlist) {

        // Log the UUID of the playlist being updated for debugging
        logger.info("Attempting to update playlist with UUID: " + playlist.getUuid());

        // Iterate through children in the VBox, to find the correct playlist
        for (Node node : vbox_playlists.getChildren()) {

            // Check if the node is a HBox
            if (node instanceof HBox) {

                // Retrieve the PlaylistItemController from userData
                Object userData = node.getUserData();
                if (userData instanceof PlaylistItemController itemController) {

                    // Compare UUIDs to identify the correct playlist
                    if (itemController.getPlaylist().getUuid().equals(playlist.getUuid())) {
                        logger.info("UUIDs match! Updating playlist name in UI.");
                        itemController.updatePlaylistNameUI();

                        // Exit once the playlist is found and updated
                        return;
                    } else {
                        logger.fine("UUID mismatch for playlist: " + itemController.getPlaylist().getUuid());
                    }
                } else {
                    logger.warning("User data is not a PlaylistItemController for node: " + node);
                }
            } else {
                logger.fine("Node is not an HBox: " + node);
            }
        }

        // Log if no match was found
        logger.warning("No matching playlist found in the sidebar for UUID: " + playlist.getUuid());
    }


    // Window for viewing all the albums that's imported
    @FXML
    private void switchToAlbumsView() {
        try {
            // Load the new FXML file (albums-overview.fxml)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("albums-overview.fxml"));
            BorderPane newView = loader.load();

            // Get the controller of the new FXML view
            AlbumsOverviewController albumsOverviewController = loader.getController();
            albumsOverviewController.setMainViewController(this);
            albumsOverviewController.setUserLibrary(userLibrary);
            albumsOverviewController.populateAlbumGrid();

            this.albumsOverviewController = albumsOverviewController;

            // Add the loaded view to anchorCenter
            anchorCenter.getChildren().clear();
            anchorCenter.getChildren().add(newView);


            // Ensure the new view fills the AnchorPane completely by setting anchors
            AnchorPane.setTopAnchor(newView, 0.0);
            AnchorPane.setBottomAnchor(newView, 0.0);
            AnchorPane.setLeftAnchor(newView, 0.0);
            AnchorPane.setRightAnchor(newView, 0.0);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @FXML
    private void switchToMostPlayedSongsView() {
        if (!userLibrary.doesPlaylistExist("Most Played Songs")) {
            Album album = new Album("Most Played Songs", "MyTunes", "2024");

            userLibrary.addAlbum(album);

            logger.info("Creating new Most Played Songs album: " + album);
        }

        userLibrary.refreshMostPlayedSongs();

        switchToPlaylistView(userLibrary.findAlbum("Most Played Songs"));
    }

    // This method runs when you select a playlist in the sidebar
    public void onPlaylistSelected(MusicCollection musicCollection) {
        if (musicCollection != null) {
            switchToPlaylistView(musicCollection);
        }
    }

    // Method to switch to the selected playlist view
    public void switchToPlaylistView(MusicCollection musicCollection) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playlist-view.fxml"));
            BorderPane newView = loader.load();

            // Retrieve the controller instance created by FXMLLoader
            PlaylistViewController controller = loader.getController();

            if (musicCollection instanceof Playlist) {

                // Pass the required data to the controller
                controller.setMusicCollection((Playlist) musicCollection);
                controller.setUserLibrary(userLibrary);
                controller.setMainViewController(this);
                controller.setPlayerBarController(playerBarController);
                controller.customInit(musicCollection);

                // Set reference to selected playlist
                System.out.println("Setting up playlist: " + musicCollection);
                selectedMusicCollection = (Playlist) musicCollection;

                // Log for debugging
                logger.info("Switching to playlist view with playlist: " + (Playlist) musicCollection + " and userLibrary: " + userLibrary);
            } else if (musicCollection instanceof Album) {
                controller.setMusicCollection((Album) musicCollection);
                controller.setUserLibrary(userLibrary);
                controller.setMainViewController(this);
                controller.setPlayerBarController(playerBarController);
                controller.customInit(musicCollection);

                // Set reference to selected album
                selectedMusicCollection = (Album) musicCollection;

                logger.info("Switching to album view with album: " + (Album) musicCollection + " and userLibrary: " + userLibrary);
            }

            // Update the reference in this class. Used for refreshing the ui, when a song is removed from the users files.
            currentPlaylistViewController = controller;

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

    // Method for importing a song using the file chooser. Not really used anymore
    @FXML
    private void importSong() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.MP3", "*.WAV"));

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
                    newAlbum.addSong(newSong);
                    userLibrary.addAlbum(newAlbum);
                } else {
                    // Add song to existing album
                    existingAlbum.addSong(newSong);
                }
            }
        }

        // Validate and remove invalid songs
        userLibrary.validateLibraryFiles();
    }

    public void handleLikeAlbum(Album albumToLike, AlbumCoverController albumCoverController) {
        userLibrary.likeAlbum(albumToLike);
        addItemToSidebar(albumToLike);
    }


    public void handleUnlikedAlbum(Album albumToUnlike) {
        for (PlaylistItemController controller : sidebarItems) {
            if (Objects.equals(controller.getAlbum(), albumToUnlike)) {
                removeItemFromSidebar(albumToUnlike, controller);
                userLibrary.unlikeAlbum(albumToUnlike);

                // Exit the loop once the item is found and removed
                break;
            }
        }
    }


    public void showFileNotFoundPrompt(Song song) {

        // Make a new alert
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fil ikke fundet");
        alert.setHeaderText("Sangfilen findes ikke længere: " + song.getSongTitle());
        alert.setContentText("Vil du fjerne sangen fra dit bibliotek?");

        // Setup buttons
        ButtonType yesButton = new ButtonType("Ja");
        ButtonType noButton = new ButtonType("Nej", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // Remove the song using the removeSong method. It removes the song from everything
            userLibrary.removeSong(song);
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

            // Validate library files
            userLibrary.validateLibraryFiles();

            //Clear sidebar
            vbox_playlists.getChildren().clear();

            // Check if userLibrary has playlists
            System.out.println("Loaded user library: " + userLibrary);


            for (Playlist playlist : userLibrary.getPlaylists()) {
                System.out.println("Loading playlist: " + playlist);
                addItemToSidebar(playlist);
            }

            for (Album album : userLibrary.getAlbums()) {
                if (album.isLiked()) {
                    addItemToSidebar(album);
                }
            }

        } else {
            System.out.println("No user data found to load.");
        }
    }

    @FXML
    private void handleValidateLibrary() {

        userLibrary.validateLibraryFiles();

        // If the user is looking at a musiccollection, then refresh it
        if (currentPlaylistViewController != null) {
            currentPlaylistViewController.customInit(selectedMusicCollection);
            logger.info("Refreshed Playlist View: " + currentPlaylistViewController.getMusicCollection().getCollectionName());
        }

        // If the user is looking at the albums overview, refresh that,
        if (albumsOverviewController != null) {
            albumsOverviewController.populateAlbumGrid();
            logger.info("Refreshed Albums Overview");
        }

        // Setup alert with the type information
        Alert alert = new Alert(Alert.AlertType.INFORMATION, "Library validation completed. Invalid songs removed.");
        alert.showAndWait();
    }
    
    @FXML
    private void scanForNewFiles() {
        List<File> audioFiles = getAudioFilesFromDocuments();
    
        // Filter for new files that are not already in the library
        List<File> newFiles = audioFiles.stream()
                .filter(file -> !userLibrary.containsSongFile(file))
                .toList();
    
        // Parse and add the new files
        if (!newFiles.isEmpty()) {
            SongParser songParser = new SongParser();
            songParser.parseSongs(userLibrary, new ArrayList<>(newFiles), cacheDataPath);
    
            // Validate and remove invalid library files after adding new ones
            userLibrary.validateLibraryFiles();

            // Refresh albums view
            if (albumsOverviewController != null) {
                albumsOverviewController.populateAlbumGrid();
                logger.info("Refreshed Albums Overview");
            }

            // Refresh playlist view
            if (currentPlaylistViewController != null) {
                currentPlaylistViewController.customInit(selectedMusicCollection);
                logger.info("Refreshed Playlist View: " + currentPlaylistViewController.getMusicCollection().getCollectionName());
            }
    
            // Setup alert with type information
            Alert alert = new Alert(Alert.AlertType.INFORMATION, newFiles.size() + " new files detected and added to the library.");
            alert.showAndWait();
    
            // Reload the sidebar to update playlists and albums
            reloadSidebar();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No new files detected in the Documents folder.");
            alert.showAndWait();
        }
    }
}