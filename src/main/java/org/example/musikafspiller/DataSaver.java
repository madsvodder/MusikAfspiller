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
import javafx.scene.image.Image;
import lombok.Getter;
import lombok.Setter;

public class DataSaver {

    Logger logger = Logger.getLogger(DataSaver.class.getName());

    private ObjectMapper objectMapper = new ObjectMapper();

    @Getter @Setter
    String saveDataPath;

    public DataSaver(String saveDataPath) {
        this.saveDataPath = saveDataPath;
    }

    public void saveUserData(UserLibrary userLibrary) {
        try {
            // Create the JSON file
            File file = new File(saveDataPath + "user_data.json");

            // Ensure the parent directory exists
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs(); // Create directories if not present
            }

            /*
            // Clear the file by creating a new empty one (it will overwrite existing content)
            if (file.exists()) {
                file.delete(); // Delete the existing file to prevent appending
            }*/

            for (Playlist playlist : userLibrary.getPlaylists()) {
                System.out.println("Playlist " + playlist.getPlaylistName());
            }

            // Write the userLibrary object to the file as JSON
            objectMapper.writeValue(file, userLibrary);
            logger.info("User data saved to: " + file.getAbsolutePath());

        } catch (IOException e) {
            logger.severe("Error saving user data: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public UserLibrary loadUserData() {
        UserLibrary userLibrary = null;
        try {
            // Create the JSON file
            File file = new File(saveDataPath + "user_data.json");

            // Ensure the file exists
            if (file.exists()) {
                // Read the userLibrary object from the file
                userLibrary = objectMapper.readValue(file, UserLibrary.class);
                logger.info("User data loaded from: " + file.getAbsolutePath());
            } else {
                logger.warning("User data file not found. Returning empty UserLibrary.");
                userLibrary = new UserLibrary(); // Return an empty UserLibrary if no file is found
            }

        } catch (IOException e) {
            logger.severe("Error loading user data: " + e.getMessage());
            e.printStackTrace();
        }
        return userLibrary;
    }
}