package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

import java.util.logging.Logger;

public class PlaylistItemController {

    // Logger
    Logger logger = Logger.getLogger(PlaylistItemController.class.getName());

    @Setter
    MainViewController mainViewController;

    @Setter @Getter
    Playlist playlist;

    @Setter @Getter
    MusicCollection musicCollection;

    @Setter @Getter
    Album album;

    @FXML @Getter
    private HBox hbox_playlist;

    @FXML
    private ImageView imageCover;

    @FXML
    private Label label_PlaylistName;

    @FXML
    private Label label_Type;

    private boolean isAlbum;

    public void initializeAsPlaylist() {
        label_PlaylistName.setText(playlist.getCollectionName());
        label_Type.setText("Playlist");
        isAlbum = false;
        setupContextMenu();
    }

    public void initializeAsAlbum() {
        label_PlaylistName.setText(album.getCollectionName());
        label_Type.setText("Album");
        imageCover.setImage(album.getAlbumArt());
        isAlbum = true;
        setupContextMenu();
    }

    @FXML
    private void onPlaylistPressed() {
        if (!isAlbum) {
            mainViewController.onPlaylistSelected(playlist);
        } else {
            mainViewController.onPlaylistSelected(album);
        }
    }

    public void updatePlaylistNameUI() {
        label_PlaylistName.setText(playlist.getCollectionName());
    }

    public HBox getPlaylistItemBox() {
        return hbox_playlist;
    }

    private void setupContextMenu() {
        // Create the context menu
        ContextMenu contextMenu = new ContextMenu();

        // Delete menu item
        MenuItem deleteItem = new MenuItem(isAlbum ? "Unlike Album" : "Delete Playlist");
        deleteItem.setOnAction(event -> {handleDeleteAction();});

        // Configure the context menu
        contextMenu.getItems().add(deleteItem);

        // Make it appear with right click
        hbox_playlist.setOnContextMenuRequested(event -> {
            contextMenu.show(hbox_playlist, event.getScreenX(), event.getScreenY());
        });
    }

    private void handleDeleteAction() {
        if (mainViewController == null) {
            logger.warning("MainViewController is null. Cannot perform delete action.");
            return;
        }

        if (isAlbum && album != null) {
            mainViewController.removeItemFromSidebar(album, this);
            mainViewController.getUserLibrary().unlikeAlbum(album);
            logger.info("Album removed from sidebar and unliked.");
        } else if (!isAlbum && playlist != null) {
            mainViewController.removeItemFromSidebar(playlist, this);
            logger.info("Playlist removed from sidebar.");
        } else {
            logger.warning("No valid item to delete.");
        }
    }
}
