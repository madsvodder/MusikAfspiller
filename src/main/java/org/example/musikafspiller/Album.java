package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

public class Album extends MusicCollection {
    @Getter @Setter
    private String albumArtist;
    @Getter @Setter
    private String albumYear;
    @Getter @Setter
    private boolean isLiked;

    public Album(String name, String artist, String year) {
        setCollectionName(name); // Use collectionName directly
        this.albumArtist = artist;
        this.albumYear = year;
    }

    public Album() {}

    @JsonIgnore
    @Override
    public String toString() {
        return getCollectionName();
    }
}
