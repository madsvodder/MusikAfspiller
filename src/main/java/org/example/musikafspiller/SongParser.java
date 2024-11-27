package org.example.musikafspiller;

import javafx.scene.image.Image;
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

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;

public class SongParser {

    private static final Logger logger = Logger.getLogger(SongParser.class.getName());

    public Song parseSong(File audioFile) {

        try {
            // Read the audio file
            AudioFile audio = AudioFileIO.read(audioFile);

            // Get the song length
            AudioHeader audioHeader = audio.getAudioHeader();
            int trackLengthInSeconds = audioHeader.getTrackLength();

            Tag tag = audio.getTag();

            // Song metadata
            String songTitle = tag.getFirst(FieldKey.TITLE);
            String songArtist = tag.getFirst(FieldKey.ARTIST);
            String songAlbum = tag.getFirst(FieldKey.ALBUM);
            int songYear = Integer.parseInt(tag.getFirst(FieldKey.YEAR));
            String songGenre = tag.getFirst(FieldKey.GENRE);

            Image albumCover = null;
            try {
                Artwork artwork = tag.getFirstArtwork();
                if (artwork != null) {
                    byte[] imageData = artwork.getBinaryData();
                    try (InputStream inputStream = new ByteArrayInputStream(imageData)) {
                        albumCover = new Image(inputStream);
                    }
                } else {
                    // Placeholder image
                    albumCover = new Image("src/main/resources/images/MusicRecord.png");
                }
            } catch (Exception e) {
                logger.warning("Failed to load album art: " + e.getMessage());
            }

            return new Song(songTitle, songArtist, songAlbum, songYear, trackLengthInSeconds, albumCover);

        } catch (CannotReadException | TagException | InvalidAudioFrameException | ReadOnlyFileException e) {
            logger.warning("Error reading the audio file: " + e.getMessage());
        } catch (Exception e) {
            logger.log(Level.SEVERE, "An unexpected error occurred: " + e.getMessage());
        }
        return null;
    }
}
