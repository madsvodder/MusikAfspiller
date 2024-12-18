package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.Pane;
import lombok.Getter;
import lombok.Setter;

import java.util.Objects;


public class AlbumCoverController {

    @FXML
    private ImageView image_cover;

    @FXML
    private Label label_title;

    @FXML @Setter @Getter
    private ImageView imgview_isLiked;

    // These images are the ones that we change during runtime.

    @Getter private Image likedImage;

    @Getter private Image unlikedImage;

    @Getter @Setter private Album album;
    @Setter private MainViewController mainViewController;
    @Setter @Getter private PlaylistItemController playlistItemController;

    public AlbumCoverController() {
        likedImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/Love.png")));
        unlikedImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/LightImages/Heart.png")));
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
            if (album.isLiked()) {
                mainViewController.handleUnlikedAlbum(album); // Album is liked
                imgview_isLiked.setImage(unlikedImage);
            } else {
                mainViewController.handleLikeAlbum(album, this); // Album is unliked
                imgview_isLiked.setImage(likedImage);
            }
        }
    }

    @FXML
    private void viewAlbum() {
        if (mainViewController != null) {
            mainViewController.switchToPlaylistView(album);
        }
    }
}
