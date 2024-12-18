package org.example.musikafspiller;

import org.jaudiotagger.audio.AudioFile;
import org.jaudiotagger.audio.AudioFileIO;
import org.jaudiotagger.audio.AudioHeader;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.exceptions.InvalidAudioFrameException;
import org.jaudiotagger.audio.exceptions.ReadOnlyFileException;
import org.jaudiotagger.tag.FieldKey;
import org.jaudiotagger.tag.Tag;
import org.jaudiotagger.tag.TagException;
import org.jaudiotagger.tag.images.Artwork;

import java.io.*;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SongParser {


    private static final Logger logger = Logger.getLogger(SongParser.class.getName());

    public Song parseSong(File audioFile, String cacheDataPath, UserLibrary userLibrary) {

        String audioFilePath = audioFile.getAbsolutePath();

        try {
            // Read the audio file
            AudioFile audio = AudioFileIO.read(audioFile);

            // Get the song length
            AudioHeader audioHeader = audio.getAudioHeader();
            int trackLengthInSeconds = audioHeader.getTrackLength();

            Tag tag = audio.getTag();

            // Song metadata
            String songTitle = tag != null ? tag.getFirst(FieldKey.TITLE) : "Unknown Title";
            String songArtist = tag != null ? tag.getFirst(FieldKey.ARTIST) : "Unknown Artist";
            String songAlbum = tag != null ? tag.getFirst(FieldKey.ALBUM) : "Unknown Album";
            String songYear = tag != null ? tag.getFirst(FieldKey.YEAR) : "Unknown Year";
            String songGenre = tag != null ? tag.getFirst(FieldKey.GENRE) : "Unknown Genre";

            String imagePath = null;

            // Extract album artwork and set it in the Album object
            if (tag != null && tag.getFirstArtwork() != null) {
                Artwork artwork = tag.getFirstArtwork();
                byte[] imageData = artwork.getBinaryData();

                if (imageData != null) {
                    // Save the artwork to the cache and get the file path
                    imagePath = saveArtworkToCache(imageData, songAlbum, songArtist, cacheDataPath);

                    // Find the existing album in the UserLibrary or create a new one
                    Album album = userLibrary.findAlbum(songAlbum);
                    if (album == null) {
                        // If the album doesn't exist, create a new one
                        album = new Album(songAlbum, songArtist, songYear);
                        userLibrary.addAlbum(album); // Add the new album to the library
                    }

                    // Set the album's artwork
                    album.setAlbumArtPath(imagePath);

                }
            }

            // Create song object
            Song newSong = new Song(songTitle, songArtist, songAlbum, songYear, trackLengthInSeconds, imagePath);
            newSong.setSongFile(audioFile);



            return newSong;

        } catch (CannotReadException | TagException | InvalidAudioFrameException | ReadOnlyFileException e) {
            logger.warning("Error reading the audio file: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred: " + e.getMessage());
        }
        return null;
    }

    private String saveArtworkToCache(byte[] imageData, String albumTitle, String songArtist, String cacheDirectory) {
        try {

            // Create a safe file name for the album artwork
            String sanitizedAlbumTitle = albumTitle.replaceAll("[^a-zA-Z0-9._-]", "_");
            String sanitizedArtistName = songArtist.replaceAll("[^a-zA-Z0-9._-]", "_");
            String imagePath = cacheDirectory + File.separator + sanitizedArtistName + "_" + sanitizedAlbumTitle + ".png";

            // Write image data to file
            try (FileOutputStream fos = new FileOutputStream(imagePath)) {
                fos.write(imageData);
            }

            return imagePath;

        }
        catch (Exception e) {
            logger.log(Level.SEVERE, "Failed to save album artwork: " + e.getMessage());
            throw new RuntimeException(e);
        }
    }

    public void parseSongs(UserLibrary userLibrary, ArrayList<File> songsFilesToParse, String cacheDataPath) {

        // Validate input
        if (songsFilesToParse == null || songsFilesToParse.isEmpty()) {
            logger.warning("No songs found to parse.");
            return;
        }

        if (userLibrary == null) {
            logger.severe("UserLibrary is null. Cannot parse songs.");
            return;
        }

        // Loop through the song files
        for (File file : songsFilesToParse) {
            if (file == null || !file.exists()) {
                logger.warning("Invalid file encountered: " + file);
                continue;
            }

            // Check if the song already exists, based on the file
            if (userLibrary.containsSongFile(file)) {
                logger.info("Skipping song. Already exists in UserLibrary: " + file.getName());
                continue; // Spring denne sang over
            }

            // Parse the song
            Song newSong = parseSong(file, cacheDataPath, userLibrary);
            if (newSong == null) {
                logger.warning("Failed to parse song: " + file.getName());
                continue;
            }

            // Add to library
            userLibrary.addSong(newSong);

            // Add to album
            associateSongWithAlbum(userLibrary, newSong);
        }
    }

    private void associateSongWithAlbum(UserLibrary userLibrary, Song song) {
        // Find or create album for the song
        Album album = userLibrary.findAlbum(song.getAlbumTitle());
        if (album == null) {
            album = new Album(song.getAlbumTitle(), song.getSongArtist(), song.getSongYear());
            userLibrary.addAlbum(album);
        }

        // Add the song to the album
        album.addSong(song);
    }

}
