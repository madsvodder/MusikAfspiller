package org.example.musikafspiller;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;

public class DataSaver {

    Logger logger = Logger.getLogger(DataSaver.class.getName());

    private ObjectMapper objectMapper = new ObjectMapper();

    @Getter @Setter
    String saveDataPath;
    @Getter @Setter
    String savePlaylistsPath;

    public DataSaver(String saveDataPath, String savePlaylistsPath) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.saveDataPath = saveDataPath;
        this.savePlaylistsPath = savePlaylistsPath;
    }

    public Playlist loadPlaylist(String playlistName) {
        try {
            // Check if the savedata folder exists
            if (savePlaylistsPath == null || savePlaylistsPath.isEmpty()) {
                throw new IllegalStateException("savePlaylistsPath is null or empty");
            }

            // Construct the file path
            Path filePath = Paths.get(savePlaylistsPath, playlistName + ".json");

            // Check if the file exists
            if (!Files.exists(filePath)) {
                logger.warning("Playlist file does not exist: " + filePath.toAbsolutePath());
                return null; // Or throw an exception, based on your requirements
            }

            // Read and deserialize the JSON file into a Playlist object
            Playlist playlist = objectMapper.readValue(filePath.toFile(), Playlist.class);

            logger.info("Playlist loaded from: " + filePath.toAbsolutePath());
            return playlist;

        } catch (IOException e) {
            logger.warning("Error loading playlist: " + e.getMessage());
            return null; // Or handle it differently, e.g., rethrow a custom exception
        } catch (IllegalStateException e) {
            logger.warning("Invalid state: " + e.getMessage());
            return null;
        }
    }

    public void savePlaylist(Playlist playlistToSave) {
        try {
            logger.info("Saving playlist: " + playlistToSave.toString());
            // Check if the savedata folder exists
            if (savePlaylistsPath == null || savePlaylistsPath.isEmpty()) {
                logger.warning("saveDataPath is null or empty");
                throw new IllegalStateException("saveDataPath is null or empty");
            }

            // Construct the file path
            Path filePath = Paths.get(savePlaylistsPath, playlistToSave.getPlaylistName() + ".json");

            // If the parent directories doesn't exist, create them - The folder in documents
            Files.createDirectories(filePath.getParent());

            // Write the playlist object to a JSON file
            objectMapper.writeValue(filePath.toFile(), playlistToSave);

            logger.info("Playlist saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.warning("Error saving playlist: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.warning("Invalid state: " + e.getMessage());}
    }

    public ArrayList<Playlist> findPlaylists() {
        ArrayList<Playlist> foundPlaylists = new ArrayList<>();
        if (savePlaylistsPath == null || savePlaylistsPath.isEmpty()) {
            logger.warning("savePlaylistsPath is null or empty");
            throw new IllegalStateException("savePlaylistsPath is null or empty");
        }

        try (DirectoryStream<Path> stream = Files.newDirectoryStream(Paths.get(savePlaylistsPath), "*.json")) {
            for (Path path : stream) {
                try {
                    // Deserialize JSON file into a Playlist object
                    Playlist playlist = objectMapper.readValue(path.toFile(), Playlist.class);
                    foundPlaylists.add(playlist);
                    logger.info("Found playlist: " + playlist.toString());
                } catch (IOException e) {
                    logger.warning("Error reading playlist: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            logger.warning("Error accessing playlist directory: " + e.getMessage());
        } return foundPlaylists;
        }
}
