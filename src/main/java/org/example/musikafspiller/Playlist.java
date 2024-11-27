package org.example.musikafspiller;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

public class Playlist {

    @Getter
    @Setter
    ArrayList<Song> songs = new ArrayList<>();
    private String playlistName;
    private int playlistDuration;

    public Playlist(String name, int duration) {
        this.playlistName = name;
        this.playlistDuration = duration;
    }
}
