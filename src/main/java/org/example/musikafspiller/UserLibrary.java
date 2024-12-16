package org.example.musikafspiller;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class UserLibrary {

    Logger logger = Logger.getLogger(UserLibrary.class.getName());

    @Getter @Setter @JsonProperty
    public ArrayList<Playlist> playlists = new ArrayList<>();
    @Getter @JsonProperty
    public ArrayList<Album> albums = new ArrayList<>();
    @Getter @JsonProperty
    public ArrayList<Song> songs = new ArrayList<>();

    public void addSong(Song song) {
        songs.add(song);
    }

    public void createNewAlbumFromSong(Song song) {
        String albumName = song.getAlbumTitle();
        String albumArtist = song.getSongArtist();
        String albumYear = song.getSongYear();
        Image albumCover = song.getAlbumCover();

        Album newAlbum = new Album(albumName, albumArtist, albumYear);

        newAlbum.addSong(song);

        addAlbum(newAlbum);
    }

    public void addAlbum(Album album) {
        albums.add(album);
    }

    public boolean doesAlbumExist(String title) {
        for (Album album : albums) {
            if (album.getCollectionName().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public boolean doesPlaylistExist(String title) {
        for (Playlist playlist : playlists) {
            if (playlist.getCollectionName().equals(title)) {
                return true;
            }
        }
        return false;
    }

    public void unlikeAlbum(Album album) {
        album.setLiked(false);
        System.out.println("Unliked album: " + album.getCollectionName());
    }

    public void likeAlbum(Album album) {
        album.setLiked(true);
        System.out.println("Liked album: " + album.getCollectionName());
    }

    public Album findAlbum(String albumName) {
        if (albumName == null || albumName.isEmpty()) {
            //System.out.println("Provided album name is null or empty.");
            return null;
        }
        for (Album album : albums) {
            if (album.getCollectionName().equals(albumName)) {
                //System.out.println("Found album: " + album.getAlbumName());
                return album;
            }
        }
        return null;
    }

    public Playlist newPlaylist() {
        Playlist newPlaylist = new Playlist("New Playlist " + playlists.size());
        playlists.add(newPlaylist);
        logger.info("Created new playlist in User Library");
        return newPlaylist;
    }

    public void removePlaylist(Playlist playlist) {
        playlists.remove(playlist);
        logger.info("Removed playlist from User Library");
    }

    public void clearPlaylists() {
        playlists.clear();
    }

    public void addPlaylist(Playlist playlist) {
        playlists.add(playlist);
    }

    // Tjekker, om en sang-fil allerede findes i brugerens bibliotek
    public boolean containsSongFile(File file) {
        return getSongs().stream()
                .anyMatch(song -> song.getSongFile().equals(file));
    }
    public List<Song> getMostPlayedSongs() {
        System.out.println("Total songs: " + songs.size());

        // Filtrer, sorter og indsamle de mest spillede sange
        List<Song> mostPlayedSongs = songs.stream()
                .filter(song -> song.getAmountOfPlays() >= 1) // Filtrer sange med plays >= 1
                .sorted((s1, s2) -> Integer.compare(s2.getAmountOfPlays(), s1.getAmountOfPlays())) // Sorter i faldende rækkefølge
                .collect(Collectors.toList()); // Indsaml som en liste

        if (!mostPlayedSongs.isEmpty()) {
            System.out.println("Most played songs:");
            for (Song song : mostPlayedSongs) {
                System.out.println("Song: " + song.getSongTitle() + ", Plays: " + song.getAmountOfPlays());
            }
        } else {
            System.out.println("No songs with plays found.");
        }

        return mostPlayedSongs;
    }


    public Song findSong(String title) {
        for (Song song : songs) {
            if (song.getSongTitle().equals(title)) {
                return song;
            }
        }
        return null;
    }
    public Playlist findPlaylist(String title) {
        for (Playlist playlist : playlists) {
            if (playlist.getCollectionName().equals(title)) {
                return playlist;
            }
        }
        return null;
    }

    public void refreshMostPlayedSongs() {

        System.out.println("Refreshing most played songs...");

        Album mostPlayedAlbum = findAlbum("Most Played Songs");
        mostPlayedAlbum.clearSongs();

        for (Song song : getMostPlayedSongs()) {
            mostPlayedAlbum.addSong(song);
        }
    }
}
