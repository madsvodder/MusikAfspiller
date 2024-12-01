package org.example.musikafspiller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.logging.Logger;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import lombok.Getter;
import lombok.Setter;

public class DataSaver {

    @Getter @Setter
    private ArrayList<String> savedPlaylists = new ArrayList<>();

    Logger logger = Logger.getLogger(DataSaver.class.getName());

    private ObjectMapper objectMapper = new ObjectMapper();
    String saveDataPath;

    public DataSaver(String saveDataPath) {
        objectMapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.saveDataPath = saveDataPath;
    }

    public Playlist loadPlaylist(String playlistName) {
        try {
            // Check if the savedata folder exists
            if (saveDataPath == null || saveDataPath.isEmpty()) {
                throw new IllegalStateException("saveDataPath is null or empty");
            }

            // Construct the file path
            Path filePath = Paths.get(saveDataPath, playlistName + ".json");

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
            if (saveDataPath == null || saveDataPath.isEmpty()) {
                logger.warning("saveDataPath is null or empty");
                throw new IllegalStateException("saveDataPath is null or empty");
            }

            // Construct the file path
            Path filePath = Paths.get(saveDataPath, playlistToSave.getPlaylistName() + ".json");

            // If the parent directores dont exist, create them - The folder in documents
            Files.createDirectories(filePath.getParent());

            // Write the playlist object to a JSON file
            objectMapper.writeValue(filePath.toFile(), playlistToSave);

            // Save it to our array of saved playlists, so we can always load it again
            savedPlaylists.add(playlistToSave.getPlaylistName());

            logger.info("Playlist saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.warning("Error saving playlist: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.warning("Invalid state: " + e.getMessage());}
    }

    public void saveDataSaver() {
        try {
            // Check if the savedata folder exists
            if (saveDataPath == null || saveDataPath.isEmpty()) {
                throw new IllegalStateException("saveDataPath is null or empty");
            }

            // Construct the file path
            Path filePath = Paths.get(saveDataPath, "dataSaver" + ".json");

            // If the parent directores dont exist, create them - The folder in documents
            Files.createDirectories(filePath.getParent());

            // Write the playlist object to a JSON file
            objectMapper.writeValue(filePath.toFile(), this);

            logger.info("dataSaver saved to: " + filePath.toAbsolutePath());
        } catch (IOException e) {
            logger.warning("Error saving dataSaver: " + e.getMessage());
        } catch (IllegalStateException e) {
            logger.warning("Invalid state: " + e.getMessage());}
    }

    public DataSaver loadDataSaver() {
        try {
            // Check if the savedata folder exists
            if (saveDataPath == null || saveDataPath.isEmpty()) {
                throw new IllegalStateException("saveDataPath is null or empty");
            }

            // Construct the file path
            Path filePath = Paths.get(saveDataPath, "dataSaver" + ".json");

            // Check if the file exists
            if (!Files.exists(filePath)) {
                logger.warning("DataSaver file does not exist: " + filePath.toAbsolutePath());
                return null;
            }

            // Read and deserialize the JSON file into a DataSaver object
            DataSaver dataSaver = objectMapper.readValue(filePath.toFile(), DataSaver.class);

            logger.info("DataSaver loaded from: " + filePath.toAbsolutePath());
            return dataSaver;

        } catch (IOException e) {
            logger.warning("Error loading datasaver: " + e.getMessage());
            return null; // Or handle it differently, e.g., rethrow a custom exception
        } catch (IllegalStateException e) {
            logger.warning("Invalid state: " + e.getMessage());
            return null;
        }
    }
}
