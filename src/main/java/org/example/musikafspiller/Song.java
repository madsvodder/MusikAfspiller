package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.ByteArrayInputStream;
import java.io.File;

public class Song {

    @Getter
    @Setter
    public String songTitle;
    @Getter
    @Setter
    public String songArtist;
    @Getter
    @Setter
    public String albumTitle;
    @Getter
    @Setter
    public int songDuration;
    @Getter
    @Setter
    public String songYear;

    // Plz dont delete this, then it breaks.
    @Getter
    @Setter
    @JsonIgnore
    public transient Image albumCover;

    @Getter
    @Setter
    public String songDurationFormatted;
    @Getter
    @Setter
    public File songFile;

    @Getter @Setter
    public byte[] albumCoverBytes; // Store album cover as bytes, to make serialization easy

    public Song(){}

    public Song(String title, String artist, String album, String songYear, int duration, byte[] albumCoverBytes) {
        this.songTitle = title;
        this.songArtist = artist;
        this.albumTitle = album;
        this.songDuration = duration;
        this.songYear = songYear;
        this.albumCoverBytes = albumCoverBytes;
        songDurationFormatted = getSongDurationFormatted();
    }


    public Image getAlbumCover() {
        return new Image(new ByteArrayInputStream(albumCoverBytes));
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
