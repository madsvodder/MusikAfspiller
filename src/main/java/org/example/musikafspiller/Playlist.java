package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.UUID;

public class Playlist {

    @Getter @Setter
    public UUID uuid = UUID.randomUUID();

    @Getter @Setter
    ArrayList<Song> songs = new ArrayList<>();
    @Getter @Setter
    public String playlistName;
    @Getter @Setter
    public int playlistDuration;

    public Playlist(String name, int duration) {
        this.playlistName = name;
        this.playlistDuration = duration;
    }

    public Playlist() {
    }

    public void addSong(Song song) {
        songs.add(song);
    }

    public boolean containsSong(Song song) {
        return songs.contains(song);
    }
}
