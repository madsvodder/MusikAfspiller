package org.example.musikafspiller;

import javafx.scene.image.Image;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.UUID;

public class Album {

    @Getter
    UUID uuid = UUID.randomUUID();
    @Getter
    private String albumName;
    private String albumArtist;
    private String albumGenre;
    private String albumYear;
    @Getter
    private Image albumCover;

    private ArrayList<Song> songs = new ArrayList<>();

    public Album(String name, String artist, String year, Image albumCover) {
        this.albumName = name;
        this.albumArtist = artist;
        this.albumYear = year;
        this.albumCover = albumCover;
    }

    public void addSongToAlbum(Song song) {
        this.songs.add(song);
    }

    @Override
    public String toString() {
        return albumName;
    }
}
