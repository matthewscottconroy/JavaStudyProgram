package com.studyprogram.storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.studyprogram.model.AttemptRecord;
import com.studyprogram.model.StudentProfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Moves a student's whole record between machines as one file: the profile plus its attempt log.
 *
 * <p>This is what makes the classroom features usable when students work on their own laptops —
 * they export a bundle, hand it in, and the instructor imports a folder of bundles and runs the
 * class report against it.
 */
public final class ProfileTransfer {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .enable(SerializationFeature.INDENT_OUTPUT)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    private ProfileTransfer() {}

    /** Writes {profile, attempts} for one student to a single file. */
    public static Path export(ProfileStorage storage, String profileName, Path outFile)
            throws IOException {
        StudentProfile profile = storage.load(profileName)
                .orElseThrow(() -> new IOException("No profile named '" + profileName + "'"));
        List<AttemptRecord> attempts = AttemptLog
                .forProfile(storage.directory(), profileName).readAll();

        ObjectNode bundle = MAPPER.createObjectNode();
        bundle.put("format", "javastudy-profile-v1");
        bundle.set("profile", MAPPER.valueToTree(profile));
        bundle.set("attempts", MAPPER.valueToTree(attempts));

        Path parent = outFile.toAbsolutePath().getParent();
        if (parent != null) Files.createDirectories(parent);
        MAPPER.writeValue(outFile.toFile(), bundle);
        return outFile;
    }

    /**
     * Reads a bundle into a profile directory. An existing profile of the same name is renamed
     * with a numeric suffix rather than overwritten, so importing a class set never destroys data.
     *
     * @return the profile name as imported
     */
    public static String importBundle(ProfileStorage storage, Path bundleFile) throws IOException {
        var root = MAPPER.readTree(bundleFile.toFile());
        if (!root.has("profile")) throw new IOException("Not a profile bundle: " + bundleFile);

        StudentProfile profile = MAPPER.treeToValue(root.get("profile"), StudentProfile.class);
        String name = profile.getName() == null || profile.getName().isBlank()
                ? "Imported" : profile.getName();
        if (storage.load(name).isPresent()) {
            int suffix = 2;
            while (storage.load(name + " (" + suffix + ")").isPresent()) suffix++;
            name = name + " (" + suffix + ")";
            profile.setName(name);
        }
        storage.save(profile);

        if (root.has("attempts")) {
            AttemptLog log = AttemptLog.forProfile(storage.directory(), name);
            for (var node : root.get("attempts")) {
                log.append(MAPPER.treeToValue(node, AttemptRecord.class));
            }
        }
        return name;
    }

    /** Imports every {@code *.json} bundle in a directory; returns the names imported. */
    public static List<String> importAll(ProfileStorage storage, Path dir) throws IOException {
        List<String> imported = new java.util.ArrayList<>();
        if (!Files.isDirectory(dir)) throw new IOException("Not a directory: " + dir);
        try (var files = Files.list(dir)) {
            for (Path p : files.filter(f -> f.toString().endsWith(".json")).sorted().toList()) {
                try {
                    imported.add(importBundle(storage, p));
                } catch (IOException e) {
                    System.err.println("Skipping " + p.getFileName() + " — " + e.getMessage());
                }
            }
        }
        return imported;
    }
}
