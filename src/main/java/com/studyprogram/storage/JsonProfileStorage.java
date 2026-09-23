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

/**
 * Saves student profiles as JSON files in a local directory.
 *
 * <p>Writes are atomic and reads are forgiving, because this file is the one place a student's
 * work can be destroyed. The program saves after every answered question, so a plain
 * write-in-place would leave a truncated file — and with it a lost semester — any time a laptop
 * lid closed at the wrong moment. Instead each save goes to a temporary file that is then moved
 * over the old one in a single step: either the whole new profile is there, or the whole old one
 * still is, never half of each.
 *
 * <p>A profile that is already damaged is quarantined rather than thrown, so one bad file cannot
 * make a student's other profiles unreachable. What was in it is usually recoverable from the
 * append-only attempt log — see {@link ProfileRecovery}.
 */
public class JsonProfileStorage implements ProfileStorage {

    private final Path directory;
    private final ObjectMapper mapper;
    private final List<String> warnings = new java.util.ArrayList<>();

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
        Path temp = Files.createTempFile(directory, file.getFileName().toString(), ".tmp");
        try {
            mapper.writeValue(temp.toFile(), profile);
            try {
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING,
                           StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                // Some filesystems (and some network shares) cannot promise atomicity. A plain
                // replace is still better than writing into the live file.
                Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temp);
        }
    }

    /**
     * Loads a profile, or empty when there is none.
     *
     * <p>A file that cannot be parsed is moved aside with a {@code .corrupt-<timestamp>} suffix
     * and reported through {@link #getWarnings()}. Throwing instead would take the whole profile
     * menu down with it, which is how a student with one damaged file loses access to the three
     * good ones sitting next to it.
     */
    @Override
    public Optional<StudentProfile> load(String name) throws IOException {
        Path file = profilePath(name);
        if (!Files.exists(file)) return Optional.empty();
        try {
            return Optional.of(mapper.readValue(file.toFile(), StudentProfile.class));
        } catch (com.fasterxml.jackson.core.JacksonException e) {
            Path aside = quarantine(file);
            warnings.add("Profile '" + name + "' could not be read and was moved to "
                    + aside.getFileName() + " — " + e.getOriginalMessage());
            return Optional.empty();
        }
    }

    /** Moves an unreadable profile out of the way, keeping it for a human to look at. */
    private Path quarantine(Path file) throws IOException {
        String stamp = java.time.LocalDateTime.now()
                .format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        Path aside = file.resolveSibling(file.getFileName() + ".corrupt-" + stamp);
        Files.move(file, aside, StandardCopyOption.REPLACE_EXISTING);
        return aside;
    }

    /** Problems met while loading, for the UI to show. Cleared when read. */
    public List<String> getWarnings() {
        List<String> copy = List.copyOf(warnings);
        warnings.clear();
        return copy;
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

    @Override
    public Path directory() {
        return directory;
    }

    private Path profilePath(String name) {
        String safe = name.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return directory.resolve(safe + ".json");
    }
}
