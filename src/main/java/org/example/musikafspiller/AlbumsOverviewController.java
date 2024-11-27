package org.example.musikafspiller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.RowConstraints;
import javafx.scene.layout.VBox;
import lombok.Setter;

import java.io.IOException;

public class AlbumsOverviewController {

    @FXML
    private GridPane grid_Albums;

    @Setter
    private UserLibrary userLibrary;

    public void populateAlbumGrid() {
        grid_Albums.getChildren().clear();  // Clear any previous content

        // Set column constraints dynamically based on your requirements
        addColumnConstraints();

        // Variables to manage grid layout
        int column = 0;
        int row = 0;

        // Iterate through albums and add them to the grid
        for (Album album : userLibrary.getAlbums()) {
            VBox albumBox = createAlbumBox(album);
            if (albumBox != null) {
                grid_Albums.add(albumBox, column, row);  // Add the album box to the grid
                column++;

                // Move to the next row after 5 columns
                if (column == 5) {
                    column = 0;
                    row++;
                }
            }
        }

        // Adjust row constraints dynamically if needed (e.g., based on album count)
        addRowConstraints(row);
    }
    public VBox createAlbumBox(Album album) {
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

    private void addColumnConstraints() {
        grid_Albums.getColumnConstraints().clear();  // Clear existing column constraints

        // Set each column to resize dynamically
        for (int i = 0; i < 5; i++) { // Assuming 5 columns
            ColumnConstraints col = new ColumnConstraints();
            col.setHgrow(Priority.ALWAYS); // Allow columns to grow
            grid_Albums.getColumnConstraints().add(col);  // Add column constraint
        }
    }
    private void addRowConstraints(int rows) {
        grid_Albums.getRowConstraints().clear();  // Clear existing row constraints

        // Add row constraints dynamically based on the number of rows
        for (int i = 0; i <= rows; i++) {
            RowConstraints row = new RowConstraints();
            row.setVgrow(Priority.ALWAYS);  // Allow rows to grow
            grid_Albums.getRowConstraints().add(row);  // Add row constraint
        }
    }
}
