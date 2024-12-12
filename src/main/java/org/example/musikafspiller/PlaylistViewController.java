package org.example.musikafspiller;

import io.github.palexdev.materialfx.controls.MFXSlider;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import lombok.Getter;
import lombok.Setter;
import org.controlsfx.control.SearchableComboBox;

import java.util.Optional;
import java.util.logging.Logger;

public class PlaylistViewController {

    @Getter @Setter
    private MusicCollection musicCollection;

    @Setter
    private UserLibrary userLibrary;

    @Setter
    private MainViewController mainViewController;

    @Setter
    private PlayerBarController playerBarController;

    @FXML
    private TableColumn<Song, String> kolonne_album;

    @FXML
    private TableColumn<Song, Image> kolonne_cover;

    @FXML
    private TableColumn<Song, String> kolonne_duration;

    @FXML
    private TableColumn<Song, Integer> kolonne_number;

    @FXML
    private TableColumn<Song, String> kolonne_title;

    @FXML
    private TableView<Song> tableview_playlist;

    @FXML
    private TextField TF_PlaylistName;

    @FXML
    private Label label_Artist;

    @FXML
    private Label label_amountOfSongs;

    @FXML
    private Button button_addSong;

    @FXML
    private ImageView image_cover;

    @Setter @Getter
    private boolean userMadePlaylist = true;

    @Getter @Setter
    private boolean isAlbum;

    private static final Logger logger = Logger.getLogger(PlaylistViewController.class.getName());

    private ObservableList<Song> songObservableList = FXCollections.observableArrayList();

    private void setupPlaylistNameListener() {
        TF_PlaylistName.textProperty().addListener((observable, oldValue, newValue) -> {
            if (musicCollection != null) {
                // Set the playlist name in the playlist class
                musicCollection.setCollectionName(newValue);
                System.out.println("HELLOOO");
                mainViewController.updatePlaylistNameInSidebar((Playlist) musicCollection);
            }
        });
    }

    private void logInitialize() {
        logger.info("Initializing PlaylistViewController");

        if (musicCollection == null) {
            logger.warning("Playlist is null");
        } else {
            logger.info("Playlist initialized: " + musicCollection);
        }

        if (userLibrary == null) {
            logger.warning("UserLibrary is null");
        } else {
            logger.info("UserLibrary initialized: " + userLibrary);
        }
    }

    public void customInit(boolean album) {

        isAlbum = album;

        if (album) {
            initializeAsAlbum();
        } else {
            initializePlaylist();
        }
    }

    private void initializePlaylist() {
        // Initialize labels and setup listeners
        setupLabels();
        logInitialize();
        setupTableview();

        // Initial update of playlist duration property
        updatePlaylistDuration();

        //populateTableView();
        setupPlaylistNameListener();

        syncObservableList();

        // Bind TableView to songObservableList
        tableview_playlist.setItems(songObservableList);

        button_addSong.setVisible(true);
        button_addSong.setDisable(false);
    }

    private void initializeAsAlbum() {
        // Initialize labels and setup listeners
        setupLabels();
        logInitialize();
        setupTableview();

        // Initial update of playlist duration property
        updatePlaylistDuration();

        //populateTableView();
        setupPlaylistNameListener();

        syncObservableList();

        // Bind TableView to songObservableList
        tableview_playlist.setItems(songObservableList);

        button_addSong.setVisible(false);
        button_addSong.setDisable(true);

        image_cover.setImage(musicCollection.getAlbumArt());
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
        //kolonne_album.setCellValueFactory(new PropertyValueFactory<>("albumTitle"));
        //kolonne_duration.setCellValueFactory(new PropertyValueFactory<>("songDurationFormatted"));
        //kolonne_title.setCellValueFactory(new PropertyValueFactory<>("songTitle"));


        kolonne_album.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getAlbumTitle()));
        kolonne_duration.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getSongDurationFormatted()));
        kolonne_cover.setCellValueFactory(param -> new SimpleObjectProperty<>(param.getValue().getAlbumCover()));
        kolonne_title.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().getSongTitle()));

        // Set up other columns
        kolonne_number.setCellValueFactory(param -> {
            int rowIndex = tableview_playlist.getItems().indexOf(param.getValue());
            return new javafx.beans.property.SimpleIntegerProperty(rowIndex + 1).asObject();
        });

        // Use a custom cell factory to display the album cover image
        kolonne_cover.setCellFactory(param -> new TableCell<Song, Image>() {
            @Override
            protected void updateItem(Image item, boolean empty) {
                super.updateItem(item, empty);

                if (empty || item == null) {
                    // Clear the graphic if the "image" is empty
                    setGraphic(null);
                } else {
                    // If the item is an image, display it in the cell
                    ImageView imageView = new ImageView(item);
                    // Resize image to fit the cell
                    imageView.setFitHeight(50);
                    // Force maintain aspect ratio
                    imageView.setPreserveRatio(true);
                    // Set the ImageView as the graphic of the cell
                    setGraphic(imageView);
                }
            }
        });
    }

    private void setupMouseClicks(){
        tableview_playlist.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2) {
                Song selectedSong = tableview_playlist.getSelectionModel().getSelectedItem();
                if (selectedSong != null) {
                    mainViewController.setSelectedSong(selectedSong);
                    playerBarController.playSongFromPlaylist(selectedSong, musicCollection);
                    logger.info("Selected song: " + selectedSong);
                }
            }
        });
    }

    private void setupContextMenu(){
        // Setup context menu
        ContextMenu contextMenu = new ContextMenu();
        MenuItem removeItem = new MenuItem("Remove");
        MenuItem queueSong = new MenuItem("Add To Queue");

        // Add the remove item to the context menu
        contextMenu.getItems().add(removeItem);
        contextMenu.getItems().add(queueSong);

        // Add action to remove the selected song
        removeItem.setOnAction(event -> {
            Song selectedSong = tableview_playlist.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                musicCollection.removeSong(selectedSong); // Remove from Playlist
                songObservableList.remove(selectedSong); // Remove from ObservableList (UI refreshes automatically)//

                // Update the playlist duration after the change
                updatePlaylistDuration();

                System.out.println(musicCollection.getDurationAsString());

                logger.info("Removed song: " + selectedSong);
            }
        });

        queueSong.setOnAction(event -> {
            Song selectedSong = tableview_playlist.getSelectionModel().getSelectedItem();
            if (selectedSong != null) {
                playerBarController.handleAddSongToQueue(selectedSong);
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

    private void syncObservableList() {
        songObservableList.setAll(musicCollection.getSongs());
    }

    private void setupLabels() {

        if (isAlbum) {
            TF_PlaylistName.setText(musicCollection.getCollectionName());
            TF_PlaylistName.setEditable(false);
        } else {
            TF_PlaylistName.setText(musicCollection.getCollectionName());
        }

        // Set the initial playlist duration
        updatePlaylistDuration();

        if (userMadePlaylist) {
            label_Artist.setText("User Playlist");
        }
    }

    // Helper method to update the playlist duration label
    private void updatePlaylistDuration() {
        label_amountOfSongs.setText(String.valueOf(musicCollection.getSongs().size()) + " songs, " + musicCollection.getDurationAsString());
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
            if (buttonType.equals(ButtonType.OK) && searchableComboBox.getSelectionModel().getSelectedItem() != null) {
                Song newSong = searchableComboBox.getValue();
                musicCollection.addSong(newSong); // Add to Playlist
                songObservableList.add(newSong); // Add to ObservableList (UI refreshes automatically)

                // Update the playlist duration after the change
                updatePlaylistDuration();
            }
        });
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

    /*
    private void populateTableView() {
        // Clear the table view for all old songs
        tableview_playlist.getItems().clear();

        // Populate the table view with all the songs from the playlist
        for (Song song : playlist.getSongs()) {
            // Reload the album for each song
            //song.reloadAlbum();

            // Debug: Check if the album is set properly before adding to the table
            System.out.println("Album for song '" + song.getSongTitle() + "': " + song.getAlbumCover());

            // Add song to the table
            tableview_playlist.getItems().add(song);
        }

        // Refresh the table to ensure it renders all the updated content
        tableview_playlist.refresh();
    }
    */
}