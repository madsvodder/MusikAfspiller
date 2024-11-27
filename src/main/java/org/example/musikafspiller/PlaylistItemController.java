package org.example.musikafspiller;

import javafx.fxml.FXML;
import lombok.Setter;

import java.awt.event.ActionEvent;

public class PlaylistItemController {

    @Setter
    MainViewController mainViewController;
    @Setter
    UserLibrary userLibrary;
    @Setter
    Playlist playlist;

    public PlaylistItemController() {
    }

    @FXML
    private void onPlaylistPressed() {
        if (playlist != null && mainViewController != null) {
            mainViewController.onPlaylistSelected(playlist);
        }
    }
}
