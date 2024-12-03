package org.example.musikafspiller;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.SearchableComboBox;

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
                mainViewController.updatePlaylistNameInSidebar(playlist);
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
        // Set up the selection mode to only select one song
        tableview_playlist.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
        // Different setup methods
        setupCells();
        setupMouseClicks();
        setupContextMenu();
    }

    private void setupCells() {
        kolonne_album.setCellValueFactory(new PropertyValueFactory<>("albumTitle"));

        kolonne_cover.setCellValueFactory(new PropertyValueFactory<>("albumCover"));

        kolonne_cover.setCellFactory(param -> new TableCell<Song, Image>() {
            @Override
            protected void updateItem(Image image, boolean empty) {
                super.updateItem(image, empty);

                if (empty || image == null) {
                    // If no image or empty, don't display anything
                    setGraphic(null);
                } else {
                    // Log the image to ensure it's not null and is being processed
                    System.out.println("Displaying image: " + image);

                    // Create an ImageView and set the image
                    ImageView imageView = new ImageView(image);
                    // Resize the image to fit within the cell
                    imageView.setFitHeight(50);
                    // Maintain aspect ratio
                    imageView.setPreserveRatio(true);
                    // Display the image in the cell
                    setGraphic(imageView);
                }
            }
        });





        kolonne_duration.setCellValueFactory(new PropertyValueFactory<>("songDurationFormatted"));

        kolonne_title.setCellValueFactory(new PropertyValueFactory<>("songTitle"));

        kolonne_number.setCellValueFactory(param -> {
            int rowIndex = tableview_playlist.getItems().indexOf(param.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(rowIndex + 1).asObject();
        });
    }



    private void setupMouseClicks(){
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

    private void setupContextMenu(){
        // Setup context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove");

        // Add the remove item to the context menu
        contextMenu.getItems().add(removeItem);

        // Add action to remove the selected song
        removeItem.setOnAction(event -> {
            Song selectedSong = tableview_playlist.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                tableview_playlist.getItems().remove(selectedSong); // Remove from TableView
                playlist.getSongs().remove(selectedSong); // Remove from Playlist
                logger.info("Removed song: " + selectedSong);
            }
        });

        // Set row factory for context menu
        tableview_playlist.setRowFactory(tableView -> {
            TableRow<Song> row = new TableRow<>();

            // Set up context menu for right-click on a row (ContextMenuRequestedEvent)
            row.setOnContextMenuRequested(event -> {
                // Only trigger if the row is not empty to prevent selection of empty rows
                if (!row.isEmpty()) {
                    // Select the clicked row
                    tableview_playlist.getSelectionModel().select(row.getIndex());
                    // Show the context menu at the mouse position
                    contextMenu.show(row, event.getScreenX(), event.getScreenY());
                }
            });
            return row;
        });
    }

    private void populateTableView() {
        // Clear the table view for all old songs
        tableview_playlist.getItems().clear();

        // Populate the table view with all the songs from the playlist
        for (Song song : playlist.getSongs()) {
            if (song.getAlbumCover() == null) {
                System.out.println("Song without cover: " + song);
            } else {
                System.out.println("Song with cover: " + song.getAlbumCover());
            }
            tableview_playlist.getItems().add(song);
        }

        // Refresh the table to ensure it renders all the updated content
        tableview_playlist.refresh();
    }

    @FXML
    private void addSongToPlaylist() {
        // Create a ChoiceBox to let the user select a song to add to the playlist
        SearchableComboBox<Song> searchableComboBox = new SearchableComboBox<>();
        // Get a list of all the songs from the users library
        searchableComboBox.getItems().addAll(userLibrary.getSongs());
        searchableComboBox.setEditable(true);

        // Create and show the dialog
        VBox content = new VBox(searchableComboBox);
        Optional<ButtonType> result = showDialogWithContent("Add Song To Playlist", "Add Song", content);

        // if the user presses ok and a song is selected, add it to the playlist
        result.ifPresent(buttonType -> {
            if (buttonType.equals(ButtonType.OK)) {
                playlist.addSong(searchableComboBox.getValue()); // Add the song to the playlist
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