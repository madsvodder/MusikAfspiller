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

    public PlaylistItemController() {
    }

    public void customInitialize() {
        label_PlaylistName.setText(playlist.getPlaylistName());
        label_Type.setText("Playlist");
        setupContextMenu();
    }

    public void initializeAsAlbum() {
        label_PlaylistName.setText(album.getAlbumName());
        label_Type.setText("Album");
        imageCover.setImage(album.getAlbumArt());
    }

    @FXML
    private void onPlaylistPressed() {
        if (playlist != null && mainViewController != null) {
            mainViewController.onPlaylistSelected(playlist);
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
            mainViewController.removePlaylistFromSidebar(this);
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
