package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;

import java.io.File;
import java.io.IOException;
import java.util.logging.Logger;

public class MainViewController {

    @FXML
    private AnchorPane anchorCenter;

    @FXML
    private VBox vbox_playlists;

    // Logger
    private static final Logger logger = Logger.getLogger(MainViewController.class.getName());

    Image testImage = new Image("file:src/test/resources/OasisDefinitelyMaybealbumcover.jpg");


    UserLibrary userLibrary = new UserLibrary();

    private void initialize() {
    }

    public MainViewController() {
    }

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

            // Set reference to user library in the playlist item controller
            playlistItemController.setUserLibrary(userLibrary);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void switchToAlbumsView() throws IOException {
        // Load the new FXML file (albums-overview.fxml)
        FXMLLoader loader = new FXMLLoader(getClass().getResource("albums-overview.fxml"));
        BorderPane newView = loader.load();

        // Get the controller of the new FXML view
        AlbumsOverviewController albumsOverviewController = loader.getController();
        albumsOverviewController.setUserLibrary(userLibrary);
        albumsOverviewController.test();

        // Add the loaded view to the center of anchorCenter
        anchorCenter.getChildren().add(newView);


        // Ensure the new view fills the AnchorPane completely by setting anchors
        AnchorPane.setTopAnchor(newView, 0.0);
        AnchorPane.setBottomAnchor(newView, 0.0);
        AnchorPane.setLeftAnchor(newView, 0.0);
        AnchorPane.setRightAnchor(newView, 0.0);
    }


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
            logger.info("Added song: " + userLibrary.songs.getLast());

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

}