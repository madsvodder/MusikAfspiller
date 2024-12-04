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
}
