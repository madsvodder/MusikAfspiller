package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.ArrayList;
import java.util.UUID;

public class Album {

    @Getter @Setter @JsonProperty
    private UUID uuid = UUID.randomUUID();
    @Getter @Setter
    private String albumName;
    @Getter @Setter
    private String albumArtist;
    @Getter @Setter
    private String albumYear;
    @Getter @Setter
    private String albumArtPath;
    @Getter @Setter
    private ArrayList<Song> songs = new ArrayList<>();

    public Album(){}

    public Album(String name, String artist, String year) {
        this.albumName = name;
        this.albumArtist = artist;
        this.albumYear = year;
    }

    @JsonIgnore
    public Image getAlbumArt() {
        if (albumArtPath == null) return null;
        return new Image(new File(albumArtPath).toURI().toString());
    }

    public void addSongToAlbum(Song song) {
        this.songs.add(song);
    }

    @Override
    public String toString() {
        return albumName;
    }
}
