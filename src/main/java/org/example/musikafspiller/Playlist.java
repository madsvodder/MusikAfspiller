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
        setPlaylistDuration();
    }

    public void removeSong(Song song) {
        songs.remove(song);
        setPlaylistDuration();
    }

    public boolean containsSong(Song song) {
        return songs.contains(song);
    }

    private void setPlaylistDuration() {
        // Clear the current duration to be safe
        playlistDuration = 0;
        for (Song song : songs) {
            playlistDuration += song.getSongDuration();
        }
    }

    @JsonIgnore
    public String getPlaylistDurationAsString() {

        int hours = playlistDuration / 3600;
        int minutes = (playlistDuration % 3600) / 60;
        int seconds = playlistDuration % 60;

        if (hours > 0) {
            return String.format("%d:%02d:%02d", hours, minutes, seconds); // Hours displayed without leading 0
        } else {
            return String.format("%02d:%02d", minutes, seconds); // No hours
        }
    }

}
