package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Optional;

public class SaveHandler { // AI GENERATED CLASS
    private final ObjectMapper mapper;
    private final Path savePath;

    public SaveHandler() {
        mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);
        savePath = Paths.get("save.json");
        ensureFile();
    }

    private void ensureFile() {
        try {
            if (!Files.exists(savePath)) {
                Files.createFile(savePath);
            }
        } catch (IOException ignored) { }
    }

    public boolean isBlank() {
        try {
            if (!Files.exists(savePath)) return true;
            String content = Files.readString(savePath, StandardCharsets.UTF_8).trim();
            return content.isEmpty();
        } catch (IOException e) {
            return true;
        }
    }

    public Optional<SaveData> load() {
        try {
            if (isBlank()) return Optional.empty();
            byte[] bytes = Files.readAllBytes(savePath);
            SaveData data = mapper.readValue(bytes, SaveData.class);
            return Optional.ofNullable(data);
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public void save(SaveData data) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(data);
            Files.write(savePath, bytes);
        } catch (IOException ignored) { }
    }

    public void clear() {
        try {
            Files.writeString(savePath, "", StandardCharsets.UTF_8);
        } catch (IOException ignored) { }
    }
}
