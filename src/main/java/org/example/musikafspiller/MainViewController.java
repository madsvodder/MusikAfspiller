package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.AnchorPane;
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


    UserLibrary userLibrary = new UserLibrary();

    private void initialize() {
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
    private void importSong() {

        // create a File chooser
        FileChooser fil_chooser = new FileChooser();

        fil_chooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Audio Files", "*.FLAC", "*.MP3", "*.WAV"));

        File selectedFile = fil_chooser.showOpenDialog(anchorCenter.getScene().getWindow());

        if (selectedFile != null) {
            SongParser songParser = new SongParser();

            userLibrary.addSong(songParser.parseSong(selectedFile));

            logger.info("Added song: " + userLibrary.songs.getLast());

        }

    }

}