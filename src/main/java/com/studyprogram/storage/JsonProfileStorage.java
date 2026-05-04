package com.studyprogram.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studyprogram.model.StudentProfile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/** Saves student profiles as JSON files in a local directory. */
public class JsonProfileStorage implements ProfileStorage {

    private final Path directory;
    private final ObjectMapper mapper;

    public JsonProfileStorage(Path directory) throws IOException {
        this.directory = directory;
        Files.createDirectories(directory);
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .enable(SerializationFeature.INDENT_OUTPUT)
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Override
    public void save(StudentProfile profile) throws IOException {
        Path file = profilePath(profile.getName());
        mapper.writeValue(file.toFile(), profile);
    }

    @Override
    public Optional<StudentProfile> load(String name) throws IOException {
        Path file = profilePath(name);
        if (!Files.exists(file)) return Optional.empty();
        return Optional.of(mapper.readValue(file.toFile(), StudentProfile.class));
    }

    @Override
    public List<String> listProfileNames() throws IOException {
        try (var stream = Files.list(directory)) {
            return stream.map(Path::getFileName)
                         .map(Path::toString)
                         .filter(n -> n.endsWith(".json"))
                         .map(n -> n.substring(0, n.length() - 5))
                         .sorted()
                         .collect(Collectors.toList());
        }
    }

    @Override
    public void delete(String name) throws IOException {
        Files.deleteIfExists(profilePath(name));
    }

    private Path profilePath(String name) {
        String safe = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return directory.resolve(safe + ".json");
    }
}
