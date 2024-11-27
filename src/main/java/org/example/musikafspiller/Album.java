package org.example.musikafspiller;

import javafx.scene.image.Image;
import lombok.Getter;

import java.util.ArrayList;
import java.util.UUID;

public class Album {


    UUID uuid = UUID.randomUUID();
    @Getter
    private String albumName;
    private String albumArtist;
    private String albumGenre;
    private int albumYear;
    @Getter
    private Image albumCover;

    private ArrayList<Song> songs = new ArrayList<>();

    public Album(String name, String artist, int year, Image albumCover) {
        this.albumName = name;
        this.albumArtist = artist;
        this.albumYear = year;
        this.albumCover = albumCover;
    }

    public void addSongToAlbum(Song song) {
        this.songs.add(song);
    }
}
