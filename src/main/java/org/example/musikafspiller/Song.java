package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter @Setter
public class Song {

    private String songTitle;
    private String songArtist;
    private String albumTitle;
    @Getter
    private int songDuration;
    private String songYear;
    private File songFile;
    private String albumCoverPath;

    public Song(){}

    public Song(String title, String artist, String album, String songYear, int duration, String albumCoverPath) {
        this.songTitle = title;
        this.songArtist = artist;
        this.albumTitle = album;
        this.songDuration = duration;
        this.songYear = songYear;
        this.albumCoverPath = albumCoverPath;
    }

    @JsonIgnore
    public Image getAlbumCover() {
        if (albumCoverPath != null && !albumCoverPath.isEmpty()) {
            System.out.println("Returned image: " + albumCoverPath);
            return new Image("file:" + albumCoverPath);  // Ensure it's a valid file URL
        }
        System.out.println("No image to return");
        return null;
    }

    @JsonIgnore
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
