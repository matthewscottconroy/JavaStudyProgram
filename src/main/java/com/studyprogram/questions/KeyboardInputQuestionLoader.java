package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class KeyboardInputQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.KEYBOARD_INPUT; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("kbd-mc-01", Topic.KEYBOARD_INPUT, 2,
                "Which interface handles keyboard events in Swing?",
                "KeyHandler", "KeyAdapter", "KeyListener", "InputListener",
                "c",
                "java.awt.event.KeyListener has 3 methods: keyPressed (key goes down), "
                + "keyReleased (key comes up), keyTyped (a character is generated). "
                + "Like MouseListener, use KeyAdapter to override only what you need."),

            mc("kbd-mc-02", Topic.KEYBOARD_INPUT, 2,
                "What is the difference between keyPressed() and keyTyped()?",
                "keyTyped fires for any key; keyPressed only fires for printable characters",
                "keyPressed fires for any key event; keyTyped fires only when a printable character is generated",
                "They fire in the same situations — just different naming conventions",
                "keyTyped fires when the key is released",
                "b",
                "keyTyped fires when a Unicode character is produced (printable keys like A-Z, 0-9). "
                + "keyPressed fires for every physical key press, including Shift, Ctrl, Delete, arrow keys. "
                + "Use keyPressed for game controls and function keys; keyTyped for text input."),

            mc("kbd-mc-03", Topic.KEYBOARD_INPUT, 2,
                "How do you check which key was pressed in keyPressed()?",
                "e.getCharacter()", "e.getKey()", "e.getKeyCode() compared to KeyEvent.VK_ constants", "e.getKeyName()",
                "c",
                "KeyEvent provides getKeyCode() which returns an int. "
                + "Compare against KeyEvent.VK_LEFT, VK_RIGHT, VK_SPACE, VK_ENTER, etc. "
                + "For keyTyped, use getKeyChar() to get the Unicode char that was typed."),

            mc("kbd-mc-04", Topic.KEYBOARD_INPUT, 3,
                "Why might a component not receive KeyEvents even after adding a KeyListener?",
                "KeyListener requires a special constructor parameter",
                "The component must have keyboard focus — call component.requestFocusInWindow()",
                "KeyEvents are only sent to JFrame, not JPanel",
                "KeyListener only works on text fields",
                "b",
                "KeyEvents are delivered only to the focused component. "
                + "Call component.setFocusable(true) and component.requestFocusInWindow() to ensure the component "
                + "can receive keyboard focus. JPanel is focusable but often doesn't have focus by default."),

            trace("kbd-tr-01", Topic.KEYBOARD_INPUT, 2,
                "What is printed when the user presses the Enter key?",
                "component.addKeyListener(new KeyAdapter() {\n"
                + "    @Override\n"
                + "    public void keyPressed(KeyEvent e) {\n"
                + "        if (e.getKeyCode() == KeyEvent.VK_ENTER) {\n"
                + "            System.out.println(\"Enter pressed\");\n"
                + "        }\n"
                + "    }\n"
                + "});",
                "Enter pressed",
                "VK_ENTER matches the Enter key's key code. The condition is true, so 'Enter pressed' is printed."),

            debug("kbd-db-01", Topic.KEYBOARD_INPUT, 3,
                "keyTyped() never prints anything for arrow keys. Why?",
                "component.addKeyListener(new KeyAdapter() {\n"
                + "    @Override\n"
                + "    public void keyTyped(KeyEvent e) {\n"
                + "        System.out.println(\"Key: \" + e.getKeyChar());\n"
                + "    }\n"
                + "});",
                "getKeyChar() is only valid in keyPressed()",
                "Arrow keys do not produce a Unicode character, so keyTyped() is never fired for them",
                "KeyAdapter overrides block keyTyped()",
                "Arrow keys require a separate ArrowKeyListener",
                "b",
                "keyTyped() only fires when a printable Unicode character is produced. "
                + "Arrow keys, Shift, Ctrl, function keys do not produce characters, so keyTyped is never called for them. "
                + "Use keyPressed() with e.getKeyCode() == KeyEvent.VK_LEFT / VK_RIGHT / VK_UP / VK_DOWN instead."),

            codegen("kbd-cg-01", Topic.KEYBOARD_INPUT, 3,
                "Which correctly moves a game character left when the left arrow key is pressed?",
                "panel.addKeyListener(new KeyAdapter() { public void keyTyped(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_LEFT) x -= 5; } });",
                "panel.addKeyListener(new KeyAdapter() { public void keyPressed(KeyEvent e) { if (e.getKeyCode() == KeyEvent.VK_LEFT) { x -= 5; panel.repaint(); } } });",
                "panel.addKeyListener(e -> { if (e == KeyEvent.VK_LEFT) x -= 5; });",
                "panel.onKeyPress(KeyEvent.VK_LEFT, () -> x -= 5);",
                "b",
                "keyPressed() fires for arrow keys (no Unicode char generated, so keyTyped won't work). "
                + "Compare getKeyCode() to VK_LEFT. Call repaint() to refresh the display after moving. "
                + "Option A uses keyTyped, which won't fire for arrow keys. "
                + "Option C: KeyListener is not a functional interface."),

            mc("kbd-mc-05", Topic.KEYBOARD_INPUT, 2,
                "How do you check if the Shift key was held during a key event?",
                "e.getShift()", "e.isShiftDown()", "KeyEvent.VK_SHIFT == e.getKeyCode()", "e.getModifiers() == SHIFT",
                "b",
                "InputEvent (parent of KeyEvent) provides isShiftDown(), isControlDown(), isAltDown(), isMetaDown(). "
                + "These check the modifier state at the time of the event. "
                + "Useful for: Shift+click, Ctrl+Z, Alt+F4 detection."),

            mc("kbd-mc-06", Topic.KEYBOARD_INPUT, 3,
                "What is the advantage of using Key Bindings (InputMap/ActionMap) over KeyListener?",
                "Key bindings are faster than KeyListener",
                "Key bindings work regardless of which component has focus; KeyListener only works on the focused component",
                "Key bindings support more keys than KeyListener",
                "Key bindings don't require event classes",
                "b",
                "Key Bindings (component.getInputMap() and getActionMap()) can be scoped to: "
                + "WHEN_FOCUSED (default), WHEN_IN_FOCUSED_WINDOW (fires even if sibling has focus), "
                + "or WHEN_ANCESTOR_OF_FOCUSED_COMPONENT. "
                + "This solves the focus problem that often plagues KeyListeners in complex UIs."),

            mc("kbd-mc-07", Topic.KEYBOARD_INPUT, 2,
                "What does e.getKeyChar() return for the Enter key?",
                "13", "\"Enter\"", "KeyEvent.CHAR_UNDEFINED", "0",
                "c",
                "KeyEvent.CHAR_UNDEFINED is returned by getKeyChar() for keys that don't produce a printable character, "
                + "such as Enter, Backspace, arrow keys, function keys. "
                + "Use getKeyCode() for these: getKeyCode() == KeyEvent.VK_ENTER."),

            trace("kbd-tr-02", Topic.KEYBOARD_INPUT, 2,
                "What is printed when the user types the lowercase letter 'a'?",
                "component.addKeyListener(new KeyAdapter() {\n"
                + "    public void keyTyped(KeyEvent e) {\n"
                + "        System.out.println((int) e.getKeyChar());\n"
                + "    }\n"
                + "});",
                "97",
                "keyTyped fires for printable characters. 'a' has Unicode (ASCII) value 97. "
                + "Casting char to int gives 97. (int)'A' would be 65."),

            trace("kbd-tr-03", Topic.KEYBOARD_INPUT, 3,
                "What is printed when the user presses F1?",
                "component.addKeyListener(new KeyAdapter() {\n"
                + "    public void keyPressed(KeyEvent e) {\n"
                + "        System.out.println(KeyEvent.getKeyText(e.getKeyCode()));\n"
                + "    }\n"
                + "});",
                "F1",
                "KeyEvent.getKeyText(keyCode) returns a human-readable name for the key. "
                + "For F1, it returns the string 'F1'. Useful for displaying key names in help screens."),

            debug("kbd-db-02", Topic.KEYBOARD_INPUT, 2,
                "The component's KeyListener never fires. What is missing?",
                "JPanel panel = new JPanel();\n"
                + "panel.addKeyListener(new KeyAdapter() {\n"
                + "    public void keyPressed(KeyEvent e) {\n"
                + "        System.out.println(\"key: \" + e.getKeyChar());\n"
                + "    }\n"
                + "});\n"
                + "frame.add(panel);\n"
                + "frame.setVisible(true);",
                "KeyAdapter is abstract and cannot be instantiated",
                "panel must be focusable and have focus — add setFocusable(true) and requestFocusInWindow()",
                "KeyListener only works on JFrame, not JPanel",
                "addKeyListener requires a second parameter",
                "b",
                "JPanel is not focusable by default and doesn't receive keyboard focus unless explicitly configured. "
                + "Fix: panel.setFocusable(true); and then panel.requestFocusInWindow(); after the frame is shown. "
                + "Without focus, KeyEvents go to whatever component has focus (often nothing)."),

            codegen("kbd-cg-02", Topic.KEYBOARD_INPUT, 3,
                "Which uses Key Bindings to handle Ctrl+S without needing focus?",
                "frame.addKeyListener(new KeyAdapter() { public void keyPressed(KeyEvent e) { if (e.isControlDown() && e.getKeyCode() == VK_S) save(); } });",
                "panel.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_S, InputEvent.CTRL_DOWN_MASK), \"save\"); panel.getActionMap().put(\"save\", new AbstractAction() { public void actionPerformed(ActionEvent e) { save(); } });",
                "panel.addKeyListener(e -> { if (e.isControlDown()) save(); });",
                "JMenuBar.setAccelerator(KeyStroke.getKeyStroke(\"control S\"));",
                "b",
                "WHEN_IN_FOCUSED_WINDOW scope fires the binding even when a different component has focus. "
                + "InputMap maps a KeyStroke to a string key; ActionMap maps that key to an AbstractAction. "
                + "This is the recommended approach for application shortcuts in Swing.")
        );
    }
}
