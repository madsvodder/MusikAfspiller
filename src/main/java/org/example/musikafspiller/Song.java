package org.example.musikafspiller;

import lombok.Getter;
import lombok.Setter;

public class Song {

    @Getter
    @Setter
    private String songTitle;
    private String songArtist;
    private String albumTitle;
    private int songDuration;
    // enum
    // private String genre;

    public Song(String title, String artist, String album, int duration) {
        this.songTitle = title;
        this.songArtist = artist;
        this.albumTitle = album;
        this.songDuration = duration;
    }

    @Override
    public String toString() {
        return songTitle + " - " + songArtist + " - " + albumTitle;
    }
}
