package org.example.musikafspiller;

import java.util.ArrayList;

public class UserLibrary {

    ArrayList<Playlist> playlists = new ArrayList<>();
    ArrayList<Song> songs = new ArrayList<>();

    public void addSong(Song song) {
        songs.add(song);
    }
}
