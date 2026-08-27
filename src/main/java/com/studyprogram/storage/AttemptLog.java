package com.studyprogram.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studyprogram.model.AttemptRecord;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;

/**
 * Append-only JSONL log of question attempts, one file per profile.
 * Appending a line per attempt is crash-safe: a partial final line at worst
 * loses one record and is skipped on read.
 */
public class AttemptLog {

    private final Path file;
    private final ObjectMapper mapper;

    public AttemptLog(Path file) {
        this.file = file;
        this.mapper = new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    /** The log file for a profile, alongside its JSON profile in the same directory. */
    public static AttemptLog forProfile(Path profileDir, String profileName) {
        String safe = profileName.replaceAll("[^a-zA-Z0-9_\\-]", "_");
        return new AttemptLog(profileDir.resolve(safe + ".attempts.jsonl"));
    }

    public Path getFile() { return file; }

    /** Appends one record. Failures are reported but never crash a study session. */
    public void append(AttemptRecord record) {
        try {
            Files.createDirectories(file.getParent());
            Files.writeString(file, mapper.writeValueAsString(record) + System.lineSeparator(),
                    StandardCharsets.UTF_8, StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            System.err.println("Warning: could not write attempt log — " + e.getMessage());
        }
    }

    /** All records in chronological (append) order; malformed lines are skipped. */
    public List<AttemptRecord> readAll() {
        List<AttemptRecord> records = new ArrayList<>();
        if (!Files.exists(file)) return records;
        try {
            for (String line : Files.readAllLines(file, StandardCharsets.UTF_8)) {
                if (line.isBlank()) continue;
                try {
                    records.add(mapper.readValue(line, AttemptRecord.class));
                } catch (IOException ignored) {
                    // partial or corrupt line — skip
                }
            }
        } catch (IOException e) {
            System.err.println("Warning: could not read attempt log — " + e.getMessage());
        }
        return records;
    }
}
