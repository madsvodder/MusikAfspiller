package org.example.musikafspiller;

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

    @Getter @JsonProperty
    public UUID uuid = UUID.randomUUID();
    @Getter
    public String albumName;
    public String albumArtist;
    public String albumYear;
    @Getter @Setter
    private String albumArtPath;

    public ArrayList<Song> songs = new ArrayList<>();


    public Album(){}

    public Album(String name, String artist, String year) {
        this.albumName = name;
        this.albumArtist = artist;
        this.albumYear = year;
    }

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
