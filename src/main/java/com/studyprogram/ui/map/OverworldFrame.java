package com.studyprogram.ui.map;

import com.studyprogram.model.StudentProfile;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;

/** Window wrapper for the concept map. */
public final class OverworldFrame {

    private OverworldFrame() {}

    /**
     * Opens the map and blocks until the student closes it, so the terminal and the window never
     * compete for input. Returns whatever the student asked for on the way out — study a topic,
     * fight a boss, or nothing.
     *
     * @param onSelectionChanged runs whenever topics are toggled (pass a profile-save callback)
     * @return the requested action, or empty in a headless environment
     */
    public static Optional<MapAction> showModal(StudentProfile profile, Runnable onSelectionChanged) {
        if (GraphicsEnvironment.isHeadless()) return Optional.empty();

        AtomicReference<MapAction> chosen = new AtomicReference<>(MapAction.none());
        try {
            SwingUtilities.invokeAndWait(() -> {
                JDialog dialog = new JDialog((Frame) null,
                        "Concept Map — " + profile.getName(), true);
                MapPanel panel = new MapPanel(profile, onSelectionChanged, action -> {
                    chosen.set(action);
                    dialog.dispose();
                });

                dialog.setLayout(new BorderLayout());
                dialog.add(new JScrollPane(panel), BorderLayout.CENTER);
                dialog.add(legend(), BorderLayout.SOUTH);
                dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

                // Escape always closes, and the map takes focus so keyboard control works at once
                dialog.getRootPane().registerKeyboardAction(e -> dialog.dispose(),
                        KeyStroke.getKeyStroke("ESCAPE"), JComponent.WHEN_IN_FOCUSED_WINDOW);

                dialog.setSize(1240, 760);
                dialog.setLocationByPlatform(true);
                SwingUtilities.invokeLater(panel::requestFocusInWindow);
                dialog.setVisible(true);      // blocks until disposed
            });
        } catch (Exception e) {
            return Optional.empty();
        }
        MapAction action = chosen.get();
        return action.isNone() ? Optional.empty() : Optional.of(action);
    }

    /** Legend and key help — the map's states must be readable without relying on colour. */
    private static JComponent legend() {
        JLabel label = new JLabel("<html><body style='padding:6px'>"
                + "&#9654; available &nbsp;&nbsp; &#9680; in progress &nbsp;&nbsp; &#9733; mastered "
                + "&nbsp;&nbsp; &#10006; locked &nbsp;&nbsp; ? secret"
                + "<br>Arrows move &nbsp;&nbsp; Enter selects &nbsp;&nbsp; S studies the focused topic "
                + "&nbsp;&nbsp; B fights its world boss &nbsp;&nbsp; double-click studies "
                + "&nbsp;&nbsp; Esc closes</body></html>");
        label.setOpaque(true);
        label.setBackground(new Color(0x14, 0x1f, 0x2b));
        label.setForeground(new Color(0xdd, 0xdd, 0xdd));
        label.getAccessibleContext().setAccessibleName("Map legend and keyboard help");
        return label;
    }
}
