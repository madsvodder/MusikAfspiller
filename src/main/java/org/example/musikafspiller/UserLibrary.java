package org.example.musikafspiller;

import javafx.scene.image.Image;
import lombok.Getter;

import java.util.ArrayList;
import java.util.UUID;

public class UserLibrary {

    ArrayList<Playlist> playlists = new ArrayList<>();
    @Getter
    ArrayList<Album> albums = new ArrayList<>();
    ArrayList<Song> songs = new ArrayList<>();

    public void addSong(Song song) {
        songs.add(song);
    }

    public void createNewAlbumFromSong(Song song) {
        String albumName = song.getAlbumTitle();
        String albumArtist = song.getSongArtist();
        String albumYear = song.getSongYear();
        Image albumCover = song.getAlbumCover();

        Album newAlbum = new Album(albumName, albumArtist, albumYear, albumCover);

        addAlbum(newAlbum);
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public boolean doesAlbumExist(String title) {
        for (Album album : albums) {
            if (album.getAlbumName().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public Album findAlbum(String albumName) {
        for (Album album : albums) {
            if (album.getAlbumName().equals(albumName)) {
                return album;
            }
        }
        return null;
    }

}
