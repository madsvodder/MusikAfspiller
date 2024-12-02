package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import lombok.Getter;
import lombok.Setter;

import java.awt.event.ActionEvent;

public class PlaylistItemController {

    @Setter
    MainViewController mainViewController;
    @Setter
    UserLibrary userLibrary;
    @Setter @Getter
    Playlist playlist;

    @FXML
    private Label label_PlaylistName;

    public PlaylistItemController() {
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
}
