package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.ToString;

import java.util.ArrayList;
import java.util.UUID;

public class Album {

    @Getter @JsonProperty
    public UUID uuid = UUID.randomUUID();
    @Getter
    public String albumName;
    public String albumArtist;
    public String albumGenre;
    public String albumYear;
    @Getter
    public Image albumCover;

    public ArrayList<Song> songs = new ArrayList<>();

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
