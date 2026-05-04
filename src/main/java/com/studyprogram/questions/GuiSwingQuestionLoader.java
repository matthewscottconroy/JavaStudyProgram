package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class GuiSwingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.GUI_SWING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("gui-mc-01", Topic.GUI_SWING, 2,
                "What is JFrame in Swing?",
                "A dialog box for user input",
                "The top-level window that holds the application's GUI",
                "A layout manager",
                "A thread for GUI operations",
                "b",
                "JFrame is the main application window. It has a title bar, borders, "
                + "and contains a content pane where you add panels and components."),

            mc("gui-mc-02", Topic.GUI_SWING, 2,
                "How do you make a JFrame visible?",
                "frame.open()", "frame.display(true)", "frame.setVisible(true)", "frame.show()",
                "c",
                "setVisible(true) makes the frame appear on screen. "
                + "show() is deprecated. open() and display() are not Swing methods."),

            mc("gui-mc-03", Topic.GUI_SWING, 2,
                "What does frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE) do?",
                "Hides the window but keeps the JVM running",
                "Exits the JVM when the window's close button is clicked",
                "Asks the user to confirm before closing",
                "Disposes of the window but continues running",
                "b",
                "EXIT_ON_CLOSE calls System.exit(0) when the close button is clicked, stopping the JVM. "
                + "DISPOSE_ON_CLOSE just disposes the window. HIDE_ON_CLOSE hides it. "
                + "If you omit this, clicking close only hides the window and the JVM keeps running."),

            mc("gui-mc-04", Topic.GUI_SWING, 3,
                "Why should Swing components be created on the Event Dispatch Thread (EDT)?",
                "Swing is thread-safe and requires multiple threads",
                "Swing is NOT thread-safe — updating components from other threads can cause corruption",
                "The EDT is faster than regular threads for drawing",
                "Only the EDT can make network calls in Swing apps",
                "b",
                "Swing components are not thread-safe. All GUI updates must happen on the EDT. "
                + "Use SwingUtilities.invokeLater(() -> { ... }) to schedule work on the EDT "
                + "from a background thread."),

            trace("gui-tr-01", Topic.GUI_SWING, 2,
                "Which method should wrap Swing startup code for thread safety?",
                "// Swing startup snippet:\n"
                + "public static void main(String[] args) {\n"
                + "    // ??? — place the JFrame creation here safely\n"
                + "    JFrame f = new JFrame(\"Hello\");\n"
                + "    f.setSize(300, 200);\n"
                + "    f.setVisible(true);\n"
                + "}",
                "SwingUtilities.invokeLater(() -> { JFrame f = ...; })",
                "The startup code should be wrapped in SwingUtilities.invokeLater() "
                + "to ensure it runs on the Event Dispatch Thread."),

            debug("gui-db-01", Topic.GUI_SWING, 3,
                "The button click handler performs a 5-second network call. What is the problem?",
                "button.addActionListener(e -> {\n"
                + "    String data = fetchFromServer(); // takes 5 seconds\n"
                + "    label.setText(data);\n"
                + "});",
                "ActionListener cannot be a lambda",
                "label.setText() requires the text to be a number",
                "The 5-second call blocks the EDT, freezing the entire GUI",
                "fetchFromServer() must be declared static",
                "c",
                "ActionListeners run on the EDT. A long-running call blocks all GUI events, "
                + "making the window unresponsive. "
                + "Fix: run the network call in a background thread (SwingWorker or Thread), "
                + "then update the label on the EDT with SwingUtilities.invokeLater()."),

            codegen("gui-cg-01", Topic.GUI_SWING, 2,
                "Which code correctly creates and displays a minimal JFrame?",
                "JFrame f = new JFrame(); f.show();",
                "JFrame f = new JFrame(\"App\"); f.setSize(400,300); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setVisible(true);",
                "new JFrame(\"App\").open(400, 300);",
                "JFrame.create(\"App\", 400, 300).setVisible(true);",
                "b",
                "A minimal working JFrame needs: a title, a size (or pack()), a close operation, "
                + "and setVisible(true). Option A uses deprecated show(). Options C and D use non-existent methods."),

            mc("gui-mc-05", Topic.GUI_SWING, 2,
                "What does frame.pack() do?",
                "Compresses the frame into the system tray",
                "Resizes the frame to fit the preferred sizes of its components",
                "Packs all components into a single row",
                "Saves the frame's layout to a file",
                "b",
                "pack() calculates the minimum size needed to display all components at their preferred sizes "
                + "and resizes the frame accordingly. Call it after adding all components and before setVisible(true). "
                + "It's better than setSize() because it adapts to different screen DPIs."),

            mc("gui-mc-06", Topic.GUI_SWING, 2,
                "How do you center a JFrame on the screen?",
                "frame.setPosition(CENTER)",
                "frame.setLocationRelativeTo(null)",
                "frame.setLocation(0, 0)",
                "frame.center()",
                "b",
                "setLocationRelativeTo(null) centers the frame on the screen. "
                + "Pass another component to center relative to it. "
                + "Call this after setSize() or pack() so the frame has a known size."),

            mc("gui-mc-07", Topic.GUI_SWING, 3,
                "What is the content pane in a JFrame?",
                "The title bar area at the top of the window",
                "The area below the menu bar where you add panels and components",
                "A panel for dialog buttons",
                "The glass pane used for overlays",
                "b",
                "frame.getContentPane() returns the container where you add components. "
                + "Since Java 5, you can call frame.add(component) directly — it delegates to the content pane. "
                + "The content pane has BorderLayout by default."),

            trace("gui-tr-02", Topic.GUI_SWING, 2,
                "What is the title of the window after this code?",
                "JFrame frame = new JFrame(\"My App\");\n"
                + "frame.setTitle(\"Updated Title\");\n"
                + "System.out.println(frame.getTitle());",
                "Updated Title",
                "setTitle() changes the window title. getTitle() returns the current title. "
                + "The second call overwrites the constructor-set title."),

            trace("gui-tr-03", Topic.GUI_SWING, 2,
                "After this code, what is the close operation?",
                "JFrame frame = new JFrame();\n"
                + "frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);\n"
                + "System.out.println(frame.getDefaultCloseOperation() == JFrame.DISPOSE_ON_CLOSE);",
                "true",
                "getDefaultCloseOperation() returns the integer constant set. "
                + "DISPOSE_ON_CLOSE = 2 in JFrame constants. The comparison to DISPOSE_ON_CLOSE is true."),

            debug("gui-db-02", Topic.GUI_SWING, 3,
                "A student creates a JFrame but nothing appears on screen. What is missing?",
                "JFrame frame = new JFrame(\"Test\");\n"
                + "frame.setSize(400, 300);\n"
                + "frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);\n"
                + "// missing: frame.setVisible(true);",
                "The frame needs components before it can be shown",
                "setSize must be called after setDefaultCloseOperation",
                "frame.setVisible(true) is missing — the frame exists but is hidden",
                "JFrame requires pack() before being visible",
                "c",
                "A JFrame starts invisible. setVisible(true) must be called to display it on screen. "
                + "Without it, the program runs but no window appears. "
                + "setVisible should be called last, after all components and settings are configured."),

            codegen("gui-cg-02", Topic.GUI_SWING, 2,
                "Which correctly creates a JFrame on the EDT with proper close behavior and centered placement?",
                "new JFrame(\"App\").setVisible(true);",
                "SwingUtilities.invokeLater(() -> { JFrame f = new JFrame(\"App\"); f.setSize(400,300); f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE); f.setLocationRelativeTo(null); f.setVisible(true); });",
                "JFrame f = new JFrame(); f.setVisible(true); f.setSize(400,300);",
                "Thread.EDT.run(() -> new JFrame(\"App\").show());",
                "b",
                "invokeLater ensures the JFrame is created on the EDT. "
                + "The sequence: create frame, set size, set close operation, center, make visible. "
                + "Option C sets size after visible (may not render correctly). "
                + "Option D: Thread.EDT is not real Java API.")
        );
    }
}
