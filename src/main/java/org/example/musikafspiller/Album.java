package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.Random;
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
    @Getter @Setter
    public int albumDuration;

    @Getter @Setter @JsonProperty
    public boolean isLiked;

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

    @JsonIgnore
    public Song getRandomSong(Song songPlaying) {
        Random r = new Random();
        Song randomSong;

        // Keep selecting a new random song until it is different from the current one
        do {
            randomSong = songs.get(r.nextInt(songs.size()));
        } while (randomSong.equals(songPlaying));

        return randomSong;
    }

    @JsonIgnore
    public String getAlbumDurationAsString() {

        int hours = albumDuration / 3600;
        int minutes = (albumDuration % 3600) / 60;
        int seconds = albumDuration % 60;

        if (hours > 0) {
            return String.format("%dh, %02dm, %02ds", hours, minutes, seconds); // Hours displayed without leading 0
        } else {
            return String.format("%02dm, %02ds", minutes, seconds); // No hours
        }
    }

    @Override
    public String toString() {
        return albumName;
    }
}
