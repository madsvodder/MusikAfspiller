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

public abstract class MusicCollection {
    @Getter @Setter
    private UUID uuid = UUID.randomUUID();
    @Getter @Setter
    private ArrayList<Song> songs = new ArrayList<>();
    @Getter @Setter
    private int duration;
    @Getter @Setter
    private String albumArtPath;

    @Getter @Setter
    @JsonProperty("name") // Ensure Jackson maps this as the "name" property
    private String collectionName;

    @JsonIgnore
    public Image getAlbumArt() {
        if (albumArtPath == null) return null;
        return new Image(new File(albumArtPath).toURI().toString());
    }

    public void addSong(Song song) {
        songs.add(song);
        updateDuration();
    }

    public void removeSong(Song song) {
        songs.remove(song);
        updateDuration();
    }

    public boolean containsSong(Song song) {
        return songs.contains(song);
    }

    private void updateDuration() {
        duration = 0;
        for (Song song : songs) {
            duration += song.getSongDuration();
        }
    }

    @JsonIgnore
    public Song getRandomSong(Song songPlaying) {
        Random random = new Random();
        Song randomSong;

        do {
            randomSong = songs.get(random.nextInt(songs.size()));
        } while (randomSong.equals(songPlaying));

        return randomSong;
    }

    @JsonIgnore
    public String getDurationAsString() {
        int hours = duration / 3600;
        int minutes = (duration % 3600) / 60;
        int seconds = duration % 60;

        if (hours > 0) {
            return String.format("%dh, %02dm, %02ds", hours, minutes, seconds);
        } else {
            return String.format("%02dm, %02ds", minutes, seconds);
        }
    }
}
