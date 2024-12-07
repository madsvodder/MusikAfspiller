package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import lombok.Getter;
import lombok.Setter;

public class PlaylistItemController {

    @Setter
    MainViewController mainViewController;
    @Setter @Getter
    Playlist playlist;
    @Setter @Getter
    Album album;

    @FXML @ Getter
    private HBox hbox_playlist;

    @FXML
    private ImageView imageCover;

    @FXML
    private Label label_PlaylistName;

    @FXML
    private Label label_Type;

    private boolean isAlbum;

    public PlaylistItemController() {
    }

    public void customInitialize(Boolean isAlbum) {
        if (isAlbum) {
            initializeAsAlbum();
        } else {
            initializeAsPlaylist();
        }
    }


    public void initializeAsPlaylist() {
        label_PlaylistName.setText(playlist.getPlaylistName());
        label_Type.setText("Playlist");
        setupContextMenu();
        isAlbum = false;
    }

    public void initializeAsAlbum() {
        label_PlaylistName.setText(album.getAlbumName());
        label_Type.setText("Album");
        imageCover.setImage(album.getAlbumArt());
        setupContextMenu();
        isAlbum = true;
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
        label_PlaylistName.setText(playlist.getPlaylistName());
    }

    public HBox getPlaylistItemBox() {
        return hbox_playlist;
    }

    private void setupContextMenu() {
        // Create the context menu
        ContextMenu contextMenu = new ContextMenu();

        // Create the menu items
        MenuItem deleteItem = new MenuItem("Delete");
        deleteItem.setOnAction(event -> {
            // Handle delete playlist from sidebar here
            if (isAlbum) {
                if (mainViewController != null && album != null) {
                    mainViewController.removeItemFromSidebar(album, this);
                    mainViewController.getUserLibrary().unlikeAlbum(album);
                }
            } else {
                if (mainViewController != null && playlist != null) {
                    mainViewController.removeItemFromSidebar(playlist, this);
                }
            }
        });

        MenuItem renameItem = new MenuItem("Rename");
        renameItem.setOnAction(event -> {
            // Handle rename here
        });

        contextMenu.getItems().addAll(deleteItem, renameItem);

        // Add right click context menu event
        hbox_playlist.setOnContextMenuRequested(event -> {
            contextMenu.show(hbox_playlist, event.getScreenX(), event.getScreenY());
        });
    }
}
