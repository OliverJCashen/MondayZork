package org.example;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

// For saving Endings; Seperate from Game State
public class ProgressHandler {
    private final ObjectMapper mapper;
    private final Path progressPath;

    public ProgressHandler() {
        this.mapper = new ObjectMapper();
        this.mapper.enable(SerializationFeature.INDENT_OUTPUT);
        this.progressPath = Paths.get("progress.json");
        ensureFile();
    }

    private void ensureFile() {
        try {
            if (!Files.exists(progressPath)) {
                Files.createFile(progressPath);
                Files.writeString(progressPath, "{}", StandardCharsets.UTF_8);
            }
        } catch (IOException ignored) { }
    }

    public Optional<ProgressData> load() {
        try {
            byte[] bytes = Files.readAllBytes(progressPath);
            String content = new String(bytes, StandardCharsets.UTF_8).trim();
            if (content.isEmpty()) {
                return Optional.of(new ProgressData());
            }
            ProgressData data = mapper.readValue(bytes, ProgressData.class);
            if (data == null) data = new ProgressData();
            return Optional.of(data);
        } catch (IOException e) {
            return Optional.of(new ProgressData());
        }
    }

    public void save(ProgressData data) {
        try {
            byte[] bytes = mapper.writeValueAsBytes(data);
            Files.write(progressPath, bytes);
        } catch (IOException ignored) { }
    }

    public void addEndingIfNew(String endingName) {
        if (endingName == null || endingName.isEmpty()) return;
        ProgressData data = load().orElseGet(ProgressData::new);
        List<String> list = data.getEndingsAchieved();
        Set<String> set = new HashSet<>(list);
        if (set.add(endingName)) {
            list.clear();
            list.addAll(set);
            save(data);
        }
    }

    public List<String> getEndings() {
        return load().map(ProgressData::getEndingsAchieved).orElseGet(() -> new ProgressData().getEndingsAchieved());
    }
}
