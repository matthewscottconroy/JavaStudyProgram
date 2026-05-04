package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class GuiComponentsQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.GUI_COMPONENTS; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("gcmp-mc-01", Topic.GUI_COMPONENTS, 2,
                "Which Swing component displays a single line of editable text?",
                "JLabel", "JTextField", "JTextArea", "JButton",
                "b",
                "JTextField is a single-line text input component. "
                + "JTextArea is multi-line. JLabel displays read-only text. JButton triggers an action. "
                + "To retrieve the entered text: String text = textField.getText();"),

            mc("gcmp-mc-02", Topic.GUI_COMPONENTS, 2,
                "What is the difference between JCheckBox and JRadioButton?",
                "JCheckBox is for numbers; JRadioButton is for text",
                "JCheckBox allows multiple selections; JRadioButton in a ButtonGroup allows only one",
                "JRadioButton can be clicked; JCheckBox cannot",
                "They are identical — just different visual styles",
                "b",
                "JCheckBoxes are independent — any combination can be selected. "
                + "JRadioButtons are grouped using ButtonGroup so that selecting one deselects the others. "
                + "Use JCheckBox for on/off flags (e.g., 'Subscribe to newsletter') "
                + "and JRadioButton for mutually exclusive options (e.g., 'Small / Medium / Large')."),

            mc("gcmp-mc-03", Topic.GUI_COMPONENTS, 2,
                "Which component shows a dropdown list of choices?",
                "JList", "JComboBox", "JMenu", "JSpinner",
                "b",
                "JComboBox<T> shows a dropdown of options. Add items with comboBox.addItem(). "
                + "Get the selected item: comboBox.getSelectedItem(). "
                + "JList shows a scrollable list where multiple items can be selected simultaneously. "
                + "JSpinner lets the user increment/decrement a value."),

            mc("gcmp-mc-04", Topic.GUI_COMPONENTS, 3,
                "What does JScrollPane do?",
                "Adds scrollbars to the JFrame automatically",
                "Wraps a component and provides scroll bars when content exceeds the visible area",
                "Creates a scrolling animation effect",
                "Manages multiple panels that slide horizontally",
                "b",
                "JScrollPane wraps any component (usually JTextArea or JList) and adds "
                + "vertical and/or horizontal scroll bars as needed. "
                + "Example: new JScrollPane(textArea) — place the JScrollPane in the layout, not the textArea directly."),

            trace("gcmp-tr-01", Topic.GUI_COMPONENTS, 2,
                "What text appears in the window after this code runs?",
                "JFrame frame = new JFrame();\n"
                + "JLabel label = new JLabel(\"Hello, GUI!\");\n"
                + "frame.add(label);\n"
                + "frame.pack();\n"
                + "frame.setVisible(true);",
                "Hello, GUI!",
                "JLabel is created with 'Hello, GUI!' text and added to the frame. "
                + "pack() sizes the frame to fit its contents. setVisible(true) shows it. "
                + "The label text 'Hello, GUI!' is displayed in the window."),

            debug("gcmp-db-01", Topic.GUI_COMPONENTS, 3,
                "The JTextArea in this code is not scrollable. Why?",
                "JFrame frame = new JFrame();\n"
                + "JTextArea area = new JTextArea(10, 30);\n"
                + "frame.add(area);  // added directly, not wrapped\n"
                + "frame.pack();\n"
                + "frame.setVisible(true);",
                "JTextArea does not support scrolling",
                "JTextArea must be wrapped in a JScrollPane to gain scroll bars",
                "frame.add() only accepts JPanel",
                "The rows and columns constructor is not valid",
                "b",
                "JTextArea itself has no scroll bars — you must wrap it: "
                + "frame.add(new JScrollPane(area)). "
                + "JScrollPane adds scroll bars when the text content exceeds the visible rows/columns."),

            codegen("gcmp-cg-01", Topic.GUI_COMPONENTS, 3,
                "Which creates a JButton labeled 'Submit' and adds an action listener?",
                "JButton btn = new JButton(); btn.setLabel(\"Submit\"); btn.onClick(() -> save());",
                "JButton btn = new JButton(\"Submit\"); btn.addActionListener(e -> save());",
                "Button btn = new Button(\"Submit\"); btn.addActionListener(e -> save());",
                "JButton btn = new JButton(\"Submit\"); btn.setAction(() -> save());",
                "b",
                "JButton(String) sets the label. addActionListener(ActionListener) wires up the event handler. "
                + "A lambda (e -> save()) works because ActionListener is a functional interface. "
                + "Option C uses AWT Button, not Swing. Option D — setAction takes an Action object, not a lambda directly."),

            mc("gcmp-mc-05", Topic.GUI_COMPONENTS, 2,
                "Which Swing component displays multi-line text that the user can edit?",
                "JTextField", "JTextPane", "JTextArea", "JLabel",
                "c",
                "JTextArea is a multi-line plain-text editor. "
                + "Set size with rows/cols constructor: new JTextArea(10, 40). "
                + "Wrap in JScrollPane for scroll bars. "
                + "JTextPane supports styled text (fonts, colors) but is more complex."),

            mc("gcmp-mc-06", Topic.GUI_COMPONENTS, 2,
                "How do you disable a JButton so the user cannot click it?",
                "btn.setClickable(false)",
                "btn.setEnabled(false)",
                "btn.setActive(false)",
                "btn.hide()",
                "b",
                "setEnabled(false) on any JComponent grays it out and makes it non-interactive. "
                + "setEnabled(true) re-enables it. "
                + "Useful for form validation: disable Submit until all required fields are filled."),

            mc("gcmp-mc-07", Topic.GUI_COMPONENTS, 3,
                "What is a JPanel used for in Swing?",
                "It is the only container that can hold components",
                "It is a general-purpose container used to group and lay out components within a frame",
                "It provides scroll functionality",
                "It is the top-level window that appears in the taskbar",
                "b",
                "JPanel is a lightweight container you can nest inside JFrame or other JPanels. "
                + "It has its own layout manager (default: FlowLayout). "
                + "Use multiple JPanels to create complex UIs by composing panels with different layout managers."),

            trace("gcmp-tr-02", Topic.GUI_COMPONENTS, 2,
                "What is the selected item after this code?",
                "JComboBox<String> box = new JComboBox<>();\n"
                + "box.addItem(\"Red\");\n"
                + "box.addItem(\"Green\");\n"
                + "box.addItem(\"Blue\");\n"
                + "box.setSelectedIndex(2);\n"
                + "System.out.println(box.getSelectedItem());",
                "Blue",
                "Items are indexed from 0. Index 2 is the third item: 'Blue'. "
                + "getSelectedItem() returns it as Object — println calls toString() on it."),

            trace("gcmp-tr-03", Topic.GUI_COMPONENTS, 2,
                "Is the checkbox checked after this code?",
                "JCheckBox cb = new JCheckBox(\"Accept terms\");\n"
                + "cb.setSelected(true);\n"
                + "System.out.println(cb.isSelected());",
                "true",
                "setSelected(true) checks the checkbox. isSelected() returns true. "
                + "JCheckBox.setSelected() can be used to initialize the UI state programmatically."),

            debug("gcmp-db-02", Topic.GUI_COMPONENTS, 2,
                "The JLabel shows 'null' instead of the user's name. Why?",
                "String name = null;\n"
                + "JLabel label = new JLabel(name);\n"
                + "frame.add(label);",
                "JLabel cannot display variables, only string literals",
                "name is null — JLabel displays 'null' when given a null String",
                "JLabel requires HTML format for text",
                "frame.add() only accepts JPanel",
                "b",
                "JLabel(String) calls String.valueOf(null) which is the string \"null\". "
                + "Fix: check if name is null before creating the label, or provide a default: "
                + "new JLabel(name != null ? name : \"Unknown\")."),

            codegen("gcmp-cg-02", Topic.GUI_COMPONENTS, 3,
                "Which creates a JTextField that shows placeholder-style prompt and retrieves the typed text?",
                "JTextField field = new JTextField(\"Enter name here\"); String text = field.getValue();",
                "JTextField field = new JTextField(20); field.setToolTipText(\"Enter name\"); String text = field.getText();",
                "JTextField field = new JTextField(); field.setPlaceholder(\"Enter name\"); String text = field.getText();",
                "JTextField field = new JTextField(\"Enter name\", 20); String text = field.getText();",
                "d",
                "JTextField(String text, int columns) sets initial text and visible width. "
                + "getText() retrieves the current content. "
                + "Option B uses a tooltip (not the same as initial text). "
                + "Option C — setPlaceholder() doesn't exist in standard Swing. "
                + "For real placeholder text, use a custom DocumentListener or FocusListener approach.")
        );
    }
}
