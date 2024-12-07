package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;


public class AlbumCoverController {

    @FXML
    private ImageView image_cover;

    @FXML
    private Label label_title;

    @Getter @Setter private Album album;
    @Setter private MainViewController mainViewController;

    public AlbumCoverController() {
    }

    public void setImage_cover(Image image) {
        image_cover.setImage(image);
    }

    public void setLabel_title(String title) {
        label_title.setText(title);
    }

    @FXML
    private void likeAlbum() {
        if (mainViewController != null) {
            mainViewController.handleLikeAlbum(album);
        }
    }
}
