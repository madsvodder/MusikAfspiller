package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

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
    @Getter @Setter
    private String albumCoverPath;

    public Song(){}

    public Song(String title, String artist, String album, String songYear, int duration, String albumCoverPath) {
        this.songTitle = title;
        this.songArtist = artist;
        this.albumTitle = album;
        this.songDuration = duration;
        this.songYear = songYear;
        this.songDurationFormatted = getSongDurationFormatted();
        this.albumCoverPath = albumCoverPath;
        System.out.println("Album cover path is: " + albumCoverPath);
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
