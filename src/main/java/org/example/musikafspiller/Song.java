package org.example.musikafspiller;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

public class Song {

    @Getter
    @Setter
    private String songTitle;
    @Getter
    @Setter
    private String songArtist;
    @Getter
    @Setter
    private String albumTitle;
    @Getter
    @Setter
    private int songDuration;
    @Getter
    @Setter
    private String songYear;
    @Getter
    @Setter
    private Image albumCover;
    @Getter
    @Setter
    private String songDurationFormatted;
    // enum
    // private String genre;

    public Song(String title, String artist, String album, String songYear, int duration, Image albumCover) {
        this.songTitle = title;
        this.songArtist = artist;
        this.albumTitle = album;
        this.songDuration = duration;
        this.songYear = songYear;
        this.albumCover = albumCover;
        songDurationFormatted = getSongDurationFormatted();
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
