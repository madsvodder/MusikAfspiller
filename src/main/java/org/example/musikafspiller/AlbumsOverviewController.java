package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.io.IOException;

public class AlbumsOverviewController {

    @FXML
    private GridPane grid_Albums;

    @Setter
    UserLibrary userLibrary;

    public AlbumsOverviewController() {
    }

    public void test() {
        int column = 0;
        int row = 0;

        grid_Albums.getChildren().clear(); // Clear any previous content

        for (Album album : userLibrary.getAlbums()) {
            VBox albumBox = showcaseAlbum(album);
            if (albumBox != null) {
                grid_Albums.add(albumBox, column, row);

                column++;
                if (column == 3) { // Assuming 3 albums per row
                    column = 0;
                    row++;
                }
            }
        }
    }

    public VBox showcaseAlbum(Album album) {
        try {
            FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("album-cover.fxml"));
            VBox albumBox = fxmlLoader.load();

            AlbumCoverController controller = fxmlLoader.getController();

            controller.setImage_cover(album.getAlbumCover());
            controller.setLabel_title(album.getAlbumName());

            return albumBox;

        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
