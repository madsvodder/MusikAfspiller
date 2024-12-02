package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;

import java.util.Optional;
import java.util.logging.Logger;

public class PlaylistViewController {

    @Getter @Setter
    private Playlist playlist;

    @Setter
    private UserLibrary userLibrary;

    @Setter
    private MainViewController mainViewController;

    @FXML
    private TableColumn<Song, String> kolonne_album;

    @FXML
    private TableColumn<Song, Image> kolonne_cover;

    @FXML
    private TableColumn<Song, Integer> kolonne_duration;

    @FXML
    private TableColumn<Song, Integer> kolonne_number;

    @FXML
    private TableColumn<Song, String> kolonne_title;

    @FXML
    private TableView<Song> tableview_playlist;

    @FXML
    private TextField TF_PlaylistName;


    private static final Logger logger = Logger.getLogger(PlaylistViewController.class.getName());

    private void setupPlaylistNameListener() {
        TF_PlaylistName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (playlist != null) {
                // Set the playlist name in the playlist class
                playlist.setPlaylistName(newValue);
            }
        });
    }

    private void logInitialize() {
        logger.info("Initializing PlaylistViewController");

        if (playlist == null) {
            logger.warning("Playlist is null");
        } else {
            logger.info("Playlist initialized: " + playlist);
        }

        if (userLibrary == null) {
            logger.warning("UserLibrary is null");
        } else {
            logger.info("UserLibrary initialized: " + userLibrary);
        }
    }

    public void customInit() {
        logInitialize();
        setupTableview();
        populateTableView();
        setupPlaylistNameListener();
    }

    private void setupTableview() {
        tableview_playlist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

        kolonne_album.setCellValueFactory(new PropertyValueFactory<>("albumTitle"));

        // Custom cell factory for displaying album cover image
        kolonne_cover.setCellValueFactory(new PropertyValueFactory<>("albumCover"));


        kolonne_cover.setCellFactory(param -> new TableCell<Song, Image>() {
            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);
                if (empty || image == null) {
                    setGraphic(null); // If no image or empty, don't display anything
                } else {
                    // Create an ImageView and set the image
                    ImageView imageView = new ImageView(image);
                    imageView.setFitHeight(50); // Resize the image to fit within the cell
                    imageView.setPreserveRatio(true); // Maintain aspect ratio
                    setGraphic(imageView); // Display the image in the cell
                }
            }
        });

        kolonne_duration.setCellValueFactory(new PropertyValueFactory<>("songDurationFormatted"));

        kolonne_title.setCellValueFactory(new PropertyValueFactory<>("songTitle"));

        // This doesnt work yet
        kolonne_number.setCellValueFactory(param -> {
            int rowIndex = tableview_playlist.getItems().indexOf(param.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(rowIndex + 1).asObject();
        });

        tableview_playlist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = tableview_playlist.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    mainViewController.setSelectedSong(selectedSong);
                    mainViewController.playSong(selectedSong);
                    logger.info("Selected song: " + selectedSong);
                }
            }
        });
    }

    private void populateTableView() {
        // Clear the table view for all old songs
        tableview_playlist.getItems().clear();

        // Populate the table view with all the songs from the playlist
        for (Song song : playlist.getSongs()) {
            tableview_playlist.getItems().add(song);
        }
    }

    @FXML
    private void addSongToPlaylist() {
        // Create a ChoiceBox to let the user select a song to add to the playlist
        ChoiceBox<Song> songChoiceBox = new ChoiceBox<>();
        // Get a list of all the songs from the users library
        songChoiceBox.getItems().addAll(userLibrary.getSongs());

        // Create and show the dialog
        VBox content = new VBox(songChoiceBox);
        Optional<ButtonType> result = showDialogWithContent("Add Song To Playlist", "Add Song", content);

        // if the user presses ok and a song is selected, add it to the playlist
        result.ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
                playlist.addSong(songChoiceBox.getValue()); // Add the song to the playlist
                refreshUI(); // Refresh the UI
            }
        });
    }

    private void refreshUI() {
        populateTableView();
    }

    // Helper method to set up and show a dialog with custom content
    private Optional<ButtonType> showDialogWithContent(String title, String headerText, Node content) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.setHeaderText(headerText);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        dialog.getDialogPane().setContent(content);
        return dialog.showAndWait();
    }
}