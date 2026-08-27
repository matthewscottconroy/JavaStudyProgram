package com.studyprogram.storage;

import com.studyprogram.model.StudentProfile;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

/** Persistence layer for student profiles. */
public interface ProfileStorage {
    void save(StudentProfile profile) throws IOException;
    Optional<StudentProfile> load(String name) throws IOException;
    List<String> listProfileNames() throws IOException;
    void delete(String name) throws IOException;

    /** The directory profiles live in — sibling data (attempt logs, reports) lives nearby. */
    Path directory();
}
