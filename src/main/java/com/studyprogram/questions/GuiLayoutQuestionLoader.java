package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class GuiLayoutQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.GUI_LAYOUT; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("glay-mc-01", Topic.GUI_LAYOUT, 2,
                "What does FlowLayout do?",
                "Stacks components vertically, one per row",
                "Places components in a row left-to-right, wrapping to the next row when full",
                "Divides the container into a fixed grid",
                "Places components at absolute pixel coordinates",
                "b",
                "FlowLayout (the default for JPanel) arranges components in a row, left to right. "
                + "When the row is full it wraps to the next line. "
                + "Components keep their preferred size. Use it for toolbars or simple rows of buttons."),

            mc("glay-mc-02", Topic.GUI_LAYOUT, 2,
                "Which layout divides a container into five named regions: NORTH, SOUTH, EAST, WEST, CENTER?",
                "GridLayout", "FlowLayout", "BorderLayout", "BoxLayout",
                "c",
                "BorderLayout (the default for JFrame's content pane) has five regions. "
                + "CENTER grows to fill remaining space. NORTH/SOUTH take full width at fixed height. "
                + "EAST/WEST take full height at fixed width. "
                + "Add with: frame.add(component, BorderLayout.NORTH);"),

            mc("glay-mc-03", Topic.GUI_LAYOUT, 2,
                "What does GridLayout(3, 2) create?",
                "A 3-pixel by 2-pixel grid",
                "A grid with 3 rows and 2 columns where all cells are the same size",
                "A layout that allows 3 nested panels with 2 columns each",
                "A layout with 3 borders and 2 flow regions",
                "b",
                "GridLayout(rows, cols) divides the container into a uniform grid. "
                + "All cells have equal width and height. Components fill cells in order (left-to-right, top-to-bottom). "
                + "Good for calculator keypads or equally-sized button grids."),

            mc("glay-mc-04", Topic.GUI_LAYOUT, 3,
                "What does setLayout(null) on a container do?",
                "Removes all components from the container",
                "Enables absolute positioning — you must set each component's bounds manually",
                "Applies the system default layout manager",
                "Makes the container invisible",
                "b",
                "setLayout(null) disables the layout manager. You must then call setBounds(x, y, width, height) "
                + "on each component. This gives pixel-perfect control but breaks resizability and accessibility. "
                + "Avoid null layouts in real applications; use a proper layout manager instead."),

            trace("glay-tr-01", Topic.GUI_LAYOUT, 2,
                "In which region does 'Save' appear?",
                "JFrame frame = new JFrame();\n"
                + "frame.setLayout(new BorderLayout());\n"
                + "frame.add(new JButton(\"File\"), BorderLayout.NORTH);\n"
                + "frame.add(new JButton(\"Save\"), BorderLayout.SOUTH);\n"
                + "frame.add(new JTextArea(), BorderLayout.CENTER);",
                "SOUTH (bottom)",
                "The Save button is added to BorderLayout.SOUTH, which appears at the bottom of the frame. "
                + "File is at the top (NORTH), the text area fills the center."),

            debug("glay-db-01", Topic.GUI_LAYOUT, 3,
                "Only the last button is visible. Why?",
                "JFrame frame = new JFrame();\n"
                + "// Default layout is BorderLayout\n"
                + "frame.add(new JButton(\"A\"));\n"
                + "frame.add(new JButton(\"B\"));\n"
                + "frame.add(new JButton(\"C\"));\n"
                + "frame.setVisible(true);",
                "BorderLayout can only show one component and defaults to CENTER",
                "add() requires a position argument in BorderLayout — without it all go to CENTER, each replacing the last",
                "JFrame cannot hold JButtons directly",
                "Three buttons exceed BorderLayout's component limit",
                "b",
                "BorderLayout.add(component) without a constraint defaults to CENTER. "
                + "Each subsequent add() to the same region replaces the previous one. "
                + "Only button 'C' is visible. Fix: use a JPanel with FlowLayout for multiple buttons, "
                + "or specify distinct regions for each."),

            codegen("glay-cg-01", Topic.GUI_LAYOUT, 2,
                "Which sets up a JPanel with GridLayout and adds 4 equal-sized buttons?",
                "JPanel p = new JPanel(); p.setLayout(new GridLayout()); p.add(b1); p.add(b2); p.add(b3); p.add(b4);",
                "JPanel p = new JPanel(new GridLayout(2, 2)); p.add(b1); p.add(b2); p.add(b3); p.add(b4);",
                "JPanel p = new JPanel(new BorderLayout(2, 2)); p.add(b1); p.add(b2);",
                "JPanel p = new JPanel(); p.setBounds(0,0,200,200); p.add(b1);",
                "b",
                "GridLayout(2, 2) creates a 2x2 grid. Adding 4 buttons fills all 4 cells with equal sizes. "
                + "Option A: GridLayout() with no args defaults to 1 row — not a 2x2 grid. "
                + "Option C uses BorderLayout which only allows 5 fixed regions."),

            mc("glay-mc-05", Topic.GUI_LAYOUT, 2,
                "Which layout manager arranges components in a single row or column using Box-model?",
                "FlowLayout", "BorderLayout", "BoxLayout", "GridBagLayout",
                "c",
                "BoxLayout arranges components either horizontally (BoxLayout.X_AXIS) or vertically (BoxLayout.Y_AXIS). "
                + "Unlike FlowLayout, it doesn't wrap. "
                + "Box.createHorizontalStrut(n) and Box.createVerticalStrut(n) add fixed spacing between components."),

            mc("glay-mc-06", Topic.GUI_LAYOUT, 3,
                "What is GridBagLayout used for?",
                "A simplified alternative to GridLayout for beginners",
                "The most flexible layout manager — lets each component span multiple rows/columns with custom weights",
                "A layout that automatically adjusts the grid to fit the screen",
                "A layout that always uses a 10x10 grid",
                "b",
                "GridBagLayout is the most powerful (and complex) layout manager. "
                + "Each component gets a GridBagConstraints object specifying: grid position, span (gridwidth/gridheight), "
                + "fill, weights, insets. It's verbose but handles complex forms and dialogs."),

            mc("glay-mc-07", Topic.GUI_LAYOUT, 2,
                "How do you add horizontal space between two components in a BoxLayout panel?",
                "panel.addSpacer(10)",
                "panel.add(Box.createHorizontalStrut(10))",
                "panel.setGap(10)",
                "panel.add(new JSeparator(10))",
                "b",
                "Box.createHorizontalStrut(pixels) creates an invisible rigid spacer of fixed width. "
                + "For flexible space that grows: Box.createHorizontalGlue(). "
                + "Vertical equivalents: Box.createVerticalStrut(pixels) and Box.createVerticalGlue()."),

            trace("glay-tr-02", Topic.GUI_LAYOUT, 2,
                "How many columns will this GridLayout have?",
                "JPanel panel = new JPanel(new GridLayout(0, 3));\n"
                + "for (int i = 0; i < 9; i++) panel.add(new JButton(String.valueOf(i)));",
                "3",
                "GridLayout(0, 3) means: any number of rows, exactly 3 columns. "
                + "Adding 9 buttons creates 3 rows of 3. The 0 for rows means rows are added automatically."),

            trace("glay-tr-03", Topic.GUI_LAYOUT, 2,
                "Where does the text area appear in the final window?",
                "JFrame frame = new JFrame();\n"
                + "JButton top = new JButton(\"Toolbar\");\n"
                + "JTextArea center = new JTextArea();\n"
                + "frame.add(top, BorderLayout.NORTH);\n"
                + "frame.add(new JScrollPane(center), BorderLayout.CENTER);",
                "CENTER (the large middle area)",
                "center is wrapped in JScrollPane and added to BorderLayout.CENTER — it fills the main area. "
                + "top appears as a narrow strip at the top (NORTH)."),

            debug("glay-db-02", Topic.GUI_LAYOUT, 2,
                "After resizing the window, the buttons look stretched and distorted. Why?",
                "JPanel panel = new JPanel(new GridLayout(1, 3));\n"
                + "panel.add(new JButton(\"A\"));\n"
                + "panel.add(new JButton(\"B\"));\n"
                + "panel.add(new JButton(\"C\"));\n"
                + "frame.add(panel);",
                "GridLayout has a bug when resizing",
                "GridLayout makes all cells equal-sized — as the window grows, all buttons stretch equally",
                "JButtons cannot resize",
                "There must be exactly 3 rows and 3 columns for GridLayout to work",
                "b",
                "GridLayout divides the container space equally among cells, so all buttons grow/shrink as the window resizes. "
                + "If you want buttons at their preferred size in a row, use FlowLayout or BoxLayout instead. "
                + "GridLayout is appropriate for calculator-style UIs where equal sizing is desired."),

            codegen("glay-cg-02", Topic.GUI_LAYOUT, 2,
                "Which creates a JFrame with a toolbar at the top and a main content area in the center?",
                "JFrame f = new JFrame(); f.add(toolbar); f.add(content);",
                "JFrame f = new JFrame(); f.setLayout(new BorderLayout()); f.add(toolbar, BorderLayout.NORTH); f.add(new JScrollPane(content), BorderLayout.CENTER);",
                "JFrame f = new JFrame(); f.setLayout(new GridLayout(2, 1)); f.add(toolbar); f.add(content);",
                "JFrame f = new JFrame(); f.add(toolbar, 0); f.add(content, 1);",
                "b",
                "BorderLayout is the right choice: NORTH for fixed-height toolbars, CENTER for the main resizable content. "
                + "JFrame's default layout is BorderLayout, so the setLayout call is optional here. "
                + "Option C uses GridLayout which makes toolbar and content equal height.")
        );
    }
}
