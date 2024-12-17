package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Playlist extends MusicCollection {
    public Playlist(String name) {
        setCollectionName(name);
    }

    public Playlist() {}

    @JsonIgnore
    @Override
    public String toString() {
        return getCollectionName();
    }
}
