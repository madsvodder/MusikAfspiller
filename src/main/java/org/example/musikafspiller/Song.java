package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.File;

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

    @JsonIgnore // Prevent cyclic references if serialized
    private Album album; // Association with an album

    public Song(){}

    public Song(String title, String artist, String album, String songYear, int duration) {
        this.songTitle = title;
        this.songArtist = artist;
        this.albumTitle = album;
        this.songDuration = duration;
        this.songYear = songYear;
        this.songDurationFormatted = getSongDurationFormatted();
    }


    // Associate the song with an album
    public void setAlbum(Album album) {
        this.album = album;
    }

    // Getter for album art via the album
    public Image getAlbumCover() {
        if (album != null) {
            Image albumImage = album.getAlbumArt();
            if (albumImage != null) {
                System.out.println("Album art retrieved successfully.");
            } else {
                System.out.println("Album art is null.");
            }
            return albumImage;
        } else {
            System.out.println("Album is null in getAlbumArt.");
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
