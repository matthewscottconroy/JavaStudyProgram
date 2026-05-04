package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class MouseInputQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.MOUSE_INPUT; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("mse-mc-01", Topic.MOUSE_INPUT, 2,
                "Which interface do you implement to handle mouse clicks?",
                "MouseHandler", "MouseAdapter", "MouseListener", "ClickListener",
                "c",
                "java.awt.event.MouseListener has 5 methods: mouseClicked, mousePressed, mouseReleased, "
                + "mouseEntered, mouseExited. All 5 must be implemented. "
                + "Use MouseAdapter instead to override only the methods you need (it provides empty default implementations)."),

            mc("mse-mc-02", Topic.MOUSE_INPUT, 2,
                "What is the difference between mousePressed() and mouseClicked()?",
                "mouseClicked fires when the button is held; mousePressed fires when released",
                "mousePressed fires when the button goes down; mouseClicked fires after both press and release on the same component",
                "They are identical — you only need to implement one",
                "mouseClicked fires for left button; mousePressed fires for right button",
                "b",
                "mousePressed fires as soon as the mouse button is depressed. "
                + "mouseClicked fires only if the button is pressed AND released on the same component without moving. "
                + "For drag operations, mouseClicked may never fire — prefer mousePressed for most interactive use."),

            mc("mse-mc-03", Topic.MOUSE_INPUT, 2,
                "How do you get the x and y coordinates of a mouse event?",
                "event.getLocation()", "event.x and event.y", "event.getX() and event.getY()", "MouseInfo.getPosition()",
                "c",
                "MouseEvent provides getX() and getY() for the position relative to the source component, "
                + "and getXOnScreen() / getYOnScreen() for absolute screen coordinates. "
                + "getPoint() returns a Point object with both."),

            mc("mse-mc-04", Topic.MOUSE_INPUT, 3,
                "Which interface handles mouse movement and dragging?",
                "MouseListener", "MouseMotionListener", "MouseDragListener", "MouseMoveHandler",
                "b",
                "MouseMotionListener has two methods: mouseMoved (no button held) and mouseDragged (button held while moving). "
                + "Add it with component.addMouseMotionListener(). "
                + "MouseAdapter implements both MouseListener and MouseMotionListener, so you can extend it "
                + "and override only the methods you need."),

            trace("mse-tr-01", Topic.MOUSE_INPUT, 2,
                "When the user clicks at coordinates (100, 200), what is printed?",
                "panel.addMouseListener(new MouseAdapter() {\n"
                + "    @Override\n"
                + "    public void mouseClicked(MouseEvent e) {\n"
                + "        System.out.println(e.getX() + \",\" + e.getY());\n"
                + "    }\n"
                + "});",
                "100,200",
                "e.getX() returns 100 and e.getY() returns 200 (the click coordinates relative to the panel). "
                + "String concatenation: 100 + \",\" + 200 = \"100,200\"."),

            debug("mse-db-01", Topic.MOUSE_INPUT, 3,
                "This code throws a compile error. Why?",
                "panel.addMouseListener(new MouseListener() {\n"
                + "    @Override\n"
                + "    public void mouseClicked(MouseEvent e) {\n"
                + "        System.out.println(\"clicked\");\n"
                + "    }\n"
                + "});",
                "MouseListener cannot be instantiated as an anonymous class",
                "Anonymous class doesn't implement all 5 methods of MouseListener",
                "panel.addMouseListener() does not exist",
                "MouseEvent must be imported separately",
                "b",
                "MouseListener is an interface with 5 abstract methods. "
                + "An anonymous class must implement all of them: mouseClicked, mousePressed, mouseReleased, "
                + "mouseEntered, mouseExited. "
                + "Fix: extend MouseAdapter instead, which provides empty implementations of all 5."),

            codegen("mse-cg-01", Topic.MOUSE_INPUT, 3,
                "Which correctly adds a mouse listener that prints coordinates on right-click only?",
                "panel.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { System.out.println(e.getX()); } });",
                "panel.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { if (e.getButton() == MouseEvent.BUTTON3) System.out.println(e.getX() + \",\" + e.getY()); } });",
                "panel.addRightClickListener(e -> System.out.println(e.getX()));",
                "panel.addMouseListener(e -> System.out.println(e.getX()));",
                "b",
                "MouseEvent.BUTTON3 is the right mouse button. Checking e.getButton() == BUTTON3 filters right-clicks only. "
                + "Option A fires on any click. Option C — addRightClickListener doesn't exist. "
                + "Option D — MouseListener is not a functional interface so a lambda doesn't work directly."),

            mc("mse-mc-05", Topic.MOUSE_INPUT, 2,
                "What method returns the number of times the mouse button was clicked (for detecting double-clicks)?",
                "e.getClickCount()", "e.getDoubleClick()", "e.getRepeatCount()", "MouseInfo.getClickCount()",
                "a",
                "MouseEvent.getClickCount() returns 1 for a single click, 2 for a double-click, etc. "
                + "Check: if (e.getClickCount() == 2) to handle double-clicks in mouseClicked()."),

            mc("mse-mc-06", Topic.MOUSE_INPUT, 3,
                "What is the purpose of MouseWheelListener?",
                "To detect when the mouse enters or leaves a component",
                "To respond to mouse wheel scroll events",
                "To detect mouse button clicks",
                "To track mouse drag distance",
                "b",
                "MouseWheelListener has one method: mouseWheelMoved(MouseWheelEvent e). "
                + "e.getWheelRotation() returns positive (scroll down) or negative (scroll up) values. "
                + "e.getUnitsToScroll() gives the amount to scroll in 'units'. "
                + "Add with: component.addMouseWheelListener(listener)."),

            mc("mse-mc-07", Topic.MOUSE_INPUT, 3,
                "What is the difference between mouseEntered() and mouseMoved()?",
                "mouseEntered fires when the mouse moves inside the component; mouseMoved fires when the mouse enters",
                "mouseEntered fires once when the cursor enters the component boundary; mouseMoved fires continuously as the cursor moves inside",
                "They fire simultaneously",
                "mouseEntered is from MouseMotionListener; mouseMoved is from MouseListener",
                "b",
                "mouseEntered (MouseListener) fires once when the cursor crosses into the component boundary. "
                + "mouseMoved (MouseMotionListener) fires repeatedly as the cursor moves within the component. "
                + "Use mouseEntered for hover effects and mouseMoved for drag-to-draw or cursor tracking."),

            trace("mse-tr-02", Topic.MOUSE_INPUT, 2,
                "The user presses the mouse button, moves it, then releases. Which methods fire in order?",
                "panel.addMouseListener(mouseListener);\n"
                + "panel.addMouseMotionListener(motionListener);\n"
                + "// user action: press → move → release",
                "mousePressed, mouseDragged (repeatedly), mouseReleased",
                "mousePressed fires on button down, mouseDragged fires for each movement while button is held, "
                + "mouseReleased fires on button up. mouseClicked fires only if press and release were at same position."),

            trace("mse-tr-03", Topic.MOUSE_INPUT, 2,
                "What is printed for a triple-click on the panel?",
                "panel.addMouseListener(new MouseAdapter() {\n"
                + "    public void mouseClicked(MouseEvent e) {\n"
                + "        System.out.println(e.getClickCount());\n"
                + "    }\n"
                + "});",
                "1\n2\n3",
                "Each click triggers mouseClicked. getClickCount() returns the running click count for rapid clicks: 1, 2, 3. "
                + "Your code should check for == 2 (double-click) or == 3 (triple-click) as needed."),

            debug("mse-db-02", Topic.MOUSE_INPUT, 3,
                "The mouse coordinates are always (0,0) when the user clicks anywhere. Why?",
                "JPanel panel = new JPanel();\n"
                + "JFrame frame = new JFrame();\n"
                + "frame.add(panel);\n"
                + "frame.addMouseListener(new MouseAdapter() {\n"
                + "    public void mouseClicked(MouseEvent e) {\n"
                + "        System.out.println(e.getX() + \",\" + e.getY());\n"
                + "    }\n"
                + "});",
                "MouseAdapter does not support getX() and getY()",
                "The listener is on the frame, not the panel — e.getX()/getY() are relative to the frame's top-left, but the content pane offsets may make it appear off",
                "getX() always returns 0 in debug mode",
                "The listener is on the frame; clicks inside the panel are consumed by the panel, not propagated to the frame's listener",
                "d",
                "Mouse events are delivered to the component where the click occurred. "
                + "The panel absorbs clicks; the frame's listener only fires if the click lands on the frame decorations (title bar, border). "
                + "Fix: add the listener to the panel, not the frame."),

            codegen("mse-cg-02", Topic.MOUSE_INPUT, 3,
                "Which implements drag-to-move: tracking press position and updating on drag?",
                "panel.addMouseMotionListener(new MouseAdapter() { public void mouseDragged(MouseEvent e) { x = e.getX(); y = e.getY(); repaint(); } });",
                "panel.addMouseListener(new MouseAdapter() { public void mouseClicked(MouseEvent e) { x = e.getX(); y = e.getY(); repaint(); } });",
                "panel.addMouseListener(new MouseAdapter() { public void mouseDragged(MouseEvent e) { x = e.getX(); repaint(); } });",
                "panel.addMouseMotionListener(e -> { x = e.getX(); y = e.getY(); });",
                "a",
                "mouseDragged (from MouseMotionListener) fires continuously while the button is held and the mouse moves. "
                + "Updating x,y and calling repaint() moves the drawn object. "
                + "Option B uses mouseClicked (only fires on click, not drag). "
                + "Option C adds mouseDragged to a MouseListener, which doesn't have that method.")
        );
    }
}
