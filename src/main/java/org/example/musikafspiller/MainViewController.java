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

    private List<PlaylistItemController> sidebarItems = new ArrayList<>();

    private PlaylistViewController currentPlaylistViewController;

    @Getter @Setter private Stage primaryStage;

    // A reference to the selected song that is playing or paused
    @Setter @Getter
    Song selectedSong;

    // Selected playlist
    MusicCollection selectedMusicCollection;

    // Use this to check if the user is looking at albums. Used for refreshing
    AlbumsOverviewController albumsOverviewController;

    // Directory of the users music
    private String libraryPath;
    private String saveDataPath;
    private String cacheDataPath;

    // The main classes. The user library, and the media player.
    @Getter
    UserLibrary userLibrary = new UserLibrary();
    DataSaver dataSaver;
    @Setter PlayerBarController playerBarController;
    private VBox vboxQueueSidebar;

    // Initialize
    public void initialize() {

        // Set the reference to the user library, so it can scan all the files on start
        userLibrary.setMainViewController(this);

        // Set up the user documents folder
        setupUserDocuments();

        // Hvis brugerdatafil eksisterer, skal vi indlæse gemte data
        if (dataSaver.doesSaveFileExist()) {
            UserLibrary loadedLibrary = dataSaver.loadUserData();

            // Kombiner gemte data med den eksisterende struktur
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

        // Parse kun nye sange fra disk
        SongParser songParser = new SongParser();
        List<File> audioFiles = getAudioFilesFromDocuments(); // Få alle filer fra disk

        // Tilføj sange fra disk, der ikke findes i biblioteket
        List<File> newFiles = audioFiles.stream()
                .filter(file -> !userLibrary.containsSongFile(file))
                .toList();
        songParser.parseSongs(userLibrary, new ArrayList<>(newFiles), cacheDataPath);

        // Initialiser UI-elementer
        setupPlayerBar();
        setupQueueSidebar();

        // Tilføj gemte playlister og albums til UI
        reloadSidebar();

        // Gem opdateret brugerdata
        save();
    }

    private void reloadSidebar() {
        // Clear sidebar før opdatering
        vbox_playlists.getChildren().clear();

        // Tilføj playlister til sidebar
        for (Playlist playlist : userLibrary.getPlaylists()) {
            System.out.println("Loading playlist into sidebar: " + playlist.getCollectionName());
            addItemToSidebar(playlist);
        }

        // Tilføj albums til sidebar, hvis de er liket
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

    private void setupPlayerBar() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("playerbar-view.fxml"));
            HBox playerBar = loader.load(); // Load the FXML file into an HBox (or whatever root node is defined).
            bp_mainBorderPane.setBottom(playerBar); // Set the loaded node as the bottom of the BorderPane.
            // Add 10px margin on all sides
            BorderPane.setMargin(playerBar, new Insets(10, 10, 10, 10));
            this.playerBarController = loader.getController();
            playerBarController.setMainViewController(this);
            playerBarController.setUserLibrary(userLibrary);
            playerBarController.customInit();
            System.out.println("Player Bar Initialized: " + playerBarController);
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

    private boolean isQueueVisible = true; // Track visibility state
    public void toggleQueueSidebar() {
        if (isQueueVisible) {
            bp_mainBorderPane.setRight(null); // Hide the queue
        } else {
            bp_mainBorderPane.setRight(vboxQueueSidebar); // Show the queue
        }
        isQueueVisible = !isQueueVisible; // Toggle state
    }

    // Method for adding a new playlist (called by Scene Builder)
    @FXML
    private void addNewPlaylistToSidebar() {
        // null indicates a new playlist
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

            // Set the userData of the HBox to the controller
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

    // Removes playlist or album from sidebar and in the user library
    public void removeItemFromSidebar(Object item, PlaylistItemController playlistItemController) {
        // Get the HBox (playlist item) from the controller
        HBox playlistItemBox = playlistItemController.getPlaylistItemBox();

        if (item instanceof Playlist) {
            // Remove the playlist item from the sidebar
            vbox_playlists.getChildren().remove(playlistItemBox);
            userLibrary.removePlaylist((Playlist) item);
            System.out.println("Removed playlist item: " + item);
        } else if (item instanceof Album) {
            // Remove the album item from the sidebar
            vbox_playlists.getChildren().remove(playlistItemBox);
            userLibrary.unlikeAlbum((Album) item);
            System.out.println("Removed album item: " + item);
        }

        // Remove the controller from the sidebarItems list
        sidebarItems.remove(playlistItemController);

        if (userLibrary.getPlaylists().isEmpty()) {
            switchToAlbumsView();
        }
    }


    public void updatePlaylistNameInSidebar(Playlist playlist) {
        // Log the UUID of the playlist being updated
        logger.info("Attempting to update playlist with UUID: " + playlist.getUuid());

        // Iterate through children in the VBox
        for (Node node : vbox_playlists.getChildren()) {
            // Check if the node is an HBox
            if (node instanceof HBox) {
                // Retrieve the PlaylistItemController from userData
                Object userData = node.getUserData();
                if (userData instanceof PlaylistItemController) {
                    PlaylistItemController itemController = (PlaylistItemController) userData;

                    // Compare UUIDs to identify the correct playlist
                    if (itemController.getPlaylist().getUuid().equals(playlist.getUuid())) {
                        logger.info("UUIDs match! Updating playlist name in UI.");
                        itemController.updatePlaylistNameUI();
                        return; // Exit once the playlist is found and updated
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

            // Add the loaded view to the center of anchorCenter
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
    public void onPlaylistSelected(MusicCollection item) {
        if (item != null) {
            switchToPlaylistView(item); // Switch to the playlist view
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

    // Method for importing a song using the file chooser

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
                break; // Exit the loop once the item is found and removed
            }
        }
    }


    public void showFileNotFoundPrompt(Song song) {
        // Dette kan være GUI-kode afhængigt af produktet, Fx Alert i JavaFX
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Fil ikke fundet");
        alert.setHeaderText("Sangfilen findes ikke længere: " + song.getSongTitle());
        alert.setContentText("Vil du fjerne sangen fra dit bibliotek?");

        ButtonType yesButton = new ButtonType("Ja");
        ButtonType noButton = new ButtonType("Nej", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(yesButton, noButton);

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == yesButton) {
            // Fjern sangen fra biblioteket
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

        if (currentPlaylistViewController != null) {
            currentPlaylistViewController.customInit(selectedMusicCollection);
            logger.info("Refreshed Playlist View: " + currentPlaylistViewController.getMusicCollection().getCollectionName());
        }

        if (albumsOverviewController != null) {
            albumsOverviewController.populateAlbumGrid();
            logger.info("Refreshed Albums Overview");
        }



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
    
            // Provide feedback to the user
            Alert alert = new Alert(Alert.AlertType.INFORMATION, newFiles.size() + " new files detected and added to the library.");
            alert.showAndWait();
    
            // Reload the sidebar to reflect updated playlists and albums
            reloadSidebar();
        } else {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "No new files detected in the Documents folder.");
            alert.showAndWait();
        }
    }
}