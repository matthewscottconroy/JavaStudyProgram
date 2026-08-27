package com.studyprogram.ui.map;

import com.studyprogram.model.StudentProfile;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;

/** Window wrapper for the concept map. */
public final class OverworldFrame {

    private OverworldFrame() {}

    /**
     * Opens the map window (non-blocking). Returns false in a headless environment.
     * {@code onSelectionChanged} runs on the Swing event thread whenever the student
     * toggles a topic — pass a profile-save callback.
     */
    public static boolean open(StudentProfile profile, Runnable onSelectionChanged) {
        if (GraphicsEnvironment.isHeadless()) return false;
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Concept Map — " + profile.getName());
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.add(new JScrollPane(new MapPanel(profile, onSelectionChanged)));
            frame.setSize(1240, 720);
            frame.setLocationByPlatform(true);
            frame.setVisible(true);
        });
        return true;
    }
}
