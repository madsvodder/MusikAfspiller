package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.File;

@Getter @Setter
public class Song {

    @Getter @Setter
    private int amountOfPlays;

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
            return new Image("file:" + albumCoverPath);
        }
        System.out.println("No image to return");
        return null;
    }

    public void increasePlays() {
        amountOfPlays++;
    }

    @JsonIgnore
    public boolean isSongFileValid() {

        String filePath = songFile.getAbsolutePath();

        if (filePath.isEmpty()) {
            return false;
        }
        File file = new File(filePath);
        return file.exists() && file.isFile();
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
