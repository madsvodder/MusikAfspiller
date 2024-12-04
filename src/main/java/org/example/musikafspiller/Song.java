package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.UUID;

public class Song {

    @Getter @Setter
    public String songTitle;
    @Getter @Setter
    public String songArtist;
    @Getter @Setter
    public String albumTitle;
    @Getter @Setter
    public int songDuration;
    @Getter @Setter
    public String songYear;
    @Setter @Getter
    public String songDurationFormatted;
    @Setter @Getter
    public File songFile;
    @Getter @Setter
    private String albumArtPath;
    @JsonIgnore // Don't serialize the Image object directly
    private Image albumCover;


    @JsonIgnore // Prevent cyclic references if serialized
    @Getter @Setter
    private Album album; // Association with an album

    public Song(){}

    public Song(String title, String artist, String album, String songYear, int duration) {
        this.songTitle = title;
        this.songArtist = artist;
        this.albumTitle = album;
        this.songDuration = duration;
        this.songYear = songYear;
        this.songDurationFormatted = getSongDurationFormatted();
        albumArtPath = getAlbumArtPath();
    }

    // Getter for album art via the album
    @JsonIgnore
    public Image getAlbumCover() {
        if (album != null) {
            // Ensure that the album art path is valid and the image can be loaded
            Image albumImage = album.getAlbumArt();
            if (albumImage != null) {
                System.out.println("Album art retrieved successfully.");
                return albumImage;
            } else {
                System.out.println("Album art is null in getAlbumCover.");
            }
        } else {
            System.out.println("Album is null in getAlbumCover.");
        }
        return null;
    }


    public String getAlbumArtPath() {
        if (album != null) {
            return album.getAlbumArtPath();
        }
        return null;
    }


    public String getSongDurationFormatted() {
        int minutes = songDuration / 60; // Get full minutes
        int seconds = songDuration % 60; // Get remaining seconds

        // Format seconds to always be 2 digits
        return String.format("%d:%02d", minutes, seconds);
    }

    @Override
    public String toString() {
        return songTitle + " - " + songArtist + " - " + albumTitle;
    }
}
