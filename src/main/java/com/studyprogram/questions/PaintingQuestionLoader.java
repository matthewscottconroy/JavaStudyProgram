package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class PaintingQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.PAINTING; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("pnt-mc-01", Topic.PAINTING, 2,
                "Which method do you override in a JPanel subclass to do custom drawing?",
                "draw()", "paint()", "paintComponent(Graphics g)", "render()",
                "c",
                "Override paintComponent(Graphics g) in a JPanel subclass. "
                + "Always call super.paintComponent(g) first to clear the background and perform Swing's bookkeeping. "
                + "Use the Graphics parameter to draw shapes, text, and images."),

            mc("pnt-mc-02", Topic.PAINTING, 2,
                "Why must you call super.paintComponent(g) at the start of a custom paintComponent()?",
                "To load the graphics driver",
                "To clear the panel and allow Swing to perform background painting before your custom drawing",
                "To set the coordinate origin to (0,0)",
                "To initialise the graphics context's color to black",
                "b",
                "super.paintComponent(g) clears the background (fills with the background color) "
                + "and lets Swing handle opaque/transparent background logic. "
                + "Skipping it causes artifacts — previous frames accumulate on screen."),

            mc("pnt-mc-03", Topic.PAINTING, 2,
                "How do you trigger a repaint of a component in response to a state change?",
                "component.paint()", "component.draw()", "component.repaint()", "component.update()",
                "c",
                "repaint() schedules a call to paintComponent() on the Event Dispatch Thread. "
                + "Never call paintComponent() directly from your code. "
                + "repaint() is thread-safe and batches multiple repaint requests efficiently."),

            mc("pnt-mc-04", Topic.PAINTING, 3,
                "What coordinate system does Swing use for painting?",
                "Origin at the bottom-left, y increases upward",
                "Origin at the center, axes extend outward",
                "Origin at the top-left corner, x increases right, y increases downward",
                "Origin at the top-right corner, x increases left",
                "c",
                "Swing uses a top-left origin where (0,0) is the top-left corner of the component. "
                + "X increases to the right; Y increases downward. "
                + "A component with width=200, height=100 has its bottom-right at (199, 99)."),

            trace("pnt-tr-01", Topic.PAINTING, 2,
                "Where is the rectangle drawn?",
                "@Override\n"
                + "protected void paintComponent(Graphics g) {\n"
                + "    super.paintComponent(g);\n"
                + "    g.fillRect(10, 20, 50, 30);\n"
                + "}",
                "Top-left at (10,20), width 50, height 30",
                "g.fillRect(x, y, width, height): top-left corner at (10, 20), extending 50 pixels right and 30 pixels down."),

            debug("pnt-db-01", Topic.PAINTING, 3,
                "The animation flickers badly. What is the most likely cause?",
                "@Override\n"
                + "protected void paintComponent(Graphics g) {\n"
                + "    // super.paintComponent(g) is NOT called\n"
                + "    g.setColor(Color.WHITE);\n"
                + "    g.fillRect(0, 0, getWidth(), getHeight());\n"
                + "    g.setColor(Color.RED);\n"
                + "    g.fillOval(x, y, 20, 20);\n"
                + "}",
                "The oval is too small to animate smoothly",
                "Manually clearing with fillRect instead of super.paintComponent() bypasses double-buffering",
                "setColor must be called before super.paintComponent()",
                "repaint() cannot animate ovals",
                "b",
                "Swing's paintComponent provides double-buffering when super.paintComponent(g) is called. "
                + "Bypassing it by manually filling with white breaks this optimization, causing visible flicker. "
                + "Always call super.paintComponent(g) first — let Swing clear the background."),

            codegen("pnt-cg-01", Topic.PAINTING, 3,
                "Which correctly defines a JPanel that draws a blue filled circle at (50,50) with diameter 40?",
                "class CirclePanel extends JPanel { void draw() { graphics.fillOval(50,50,40,40); } }",
                "class CirclePanel extends JPanel { @Override protected void paintComponent(Graphics g) { super.paintComponent(g); g.setColor(Color.BLUE); g.fillOval(50,50,40,40); } }",
                "class CirclePanel extends JPanel { @Override public void paint(Graphics g) { g.fillOval(50,50,40,40); } }",
                "class CirclePanel implements Paintable { void paintComponent(Graphics g) { g.fillOval(50,50,40,40); } }",
                "b",
                "Override paintComponent (not paint), call super first, then set color and draw. "
                + "Option A doesn't override paintComponent — draw() is never called by Swing. "
                + "Option C overrides paint() which skips double-buffering benefits. "
                + "Option D: Paintable does not exist in Swing."),

            mc("pnt-mc-05", Topic.PAINTING, 2,
                "What does g.drawString(\"Hello\", 50, 100) do?",
                "Draws the text 'Hello' with its top-left corner at (50,100)",
                "Draws the text 'Hello' with its baseline at y=100 and leftmost character at x=50",
                "Draws a rectangle containing the text 'Hello' at (50,100)",
                "Draws the text 'Hello' centered at the point (50,100)",
                "b",
                "drawString uses the font baseline (not top-left) for the y coordinate. "
                + "The baseline is the line letters 'sit on' — descenders like g and y go below it. "
                + "To position by top-left, use FontMetrics to measure ascent: y = topY + fm.getAscent()."),

            mc("pnt-mc-06", Topic.PAINTING, 2,
                "How do you change the font for drawing text in paintComponent()?",
                "g.setTextFont(new Font(\"Arial\", Font.BOLD, 20))",
                "g.setFont(new Font(\"Arial\", Font.BOLD, 20))",
                "Font.setDefault(new Font(\"Arial\", Font.BOLD, 20))",
                "g.drawFont(\"Arial\", Font.BOLD, 20)",
                "b",
                "g.setFont(font) sets the font for subsequent drawString calls. "
                + "Font constructors: new Font(name, style, size). Styles: Font.PLAIN, Font.BOLD, Font.ITALIC, or combined. "
                + "Use g.getFontMetrics() after setting the font to measure text dimensions."),

            mc("pnt-mc-07", Topic.PAINTING, 3,
                "What is the purpose of a Swing Timer in animation?",
                "It measures how long paintComponent takes to run",
                "It fires ActionEvents at a fixed interval, allowing state updates and repaint() calls for smooth animation",
                "It delays the initial window display",
                "It prevents more than one repaint per second",
                "b",
                "javax.swing.Timer fires on the Event Dispatch Thread at fixed intervals. "
                + "new Timer(16, e -> { updateState(); repaint(); }).start() creates ~60 fps animation. "
                + "Using a Thread.sleep loop instead is dangerous — it may not run on the EDT."),

            trace("pnt-tr-02", Topic.PAINTING, 2,
                "What color is used for the circle?",
                "@Override\n"
                + "protected void paintComponent(Graphics g) {\n"
                + "    super.paintComponent(g);\n"
                + "    g.setColor(Color.RED);\n"
                + "    g.fillRect(0, 0, 50, 50);\n"
                + "    g.setColor(Color.BLUE);\n"
                + "    g.fillOval(60, 60, 40, 40);\n"
                + "}",
                "Blue",
                "setColor changes the current color for all subsequent draw calls. "
                + "The rectangle is red, then the color is changed to blue before drawing the oval. "
                + "The circle (oval) is drawn in blue."),

            trace("pnt-tr-03", Topic.PAINTING, 2,
                "What is the width and height of the drawing area in paintComponent()?",
                "@Override\n"
                + "protected void paintComponent(Graphics g) {\n"
                + "    super.paintComponent(g);\n"
                + "    System.out.println(getWidth() + \"x\" + getHeight());\n"
                + "}",
                "The current width and height of the JPanel component",
                "getWidth() and getHeight() return the panel's current pixel dimensions, which change as the window is resized. "
                + "Always use these (not hardcoded values) to draw backgrounds and centered content."),

            debug("pnt-db-02", Topic.PAINTING, 3,
                "The timer starts but nothing moves on screen. What is missing?",
                "int x = 0;\n"
                + "Timer timer = new Timer(16, e -> {\n"
                + "    x += 2;\n"
                + "    // missing: repaint()\n"
                + "});\n"
                + "timer.start();",
                "Timer fires too slowly at 16ms",
                "x++ should be used instead of x += 2",
                "repaint() is not called — state changes but the component is never redrawn",
                "Timer must be declared as a field, not a local variable",
                "c",
                "Updating state (x += 2) without calling repaint() means paintComponent() is never invoked again. "
                + "The screen shows the initial state forever. Add repaint(); inside the timer's action listener to request a redraw."),

            codegen("pnt-cg-02", Topic.PAINTING, 3,
                "Which creates a simple animation: a red ball moving right across the panel?",
                "class BallPanel extends JPanel { int x = 0; public BallPanel() { new Timer(16, e -> { x += 3; }).start(); } @Override protected void paintComponent(Graphics g) { super.paintComponent(g); g.setColor(Color.RED); g.fillOval(x,100,30,30); } }",
                "class BallPanel extends JPanel { int x = 0; public BallPanel() { new Timer(16, e -> { x += 3; repaint(); }).start(); } @Override protected void paintComponent(Graphics g) { super.paintComponent(g); g.setColor(Color.RED); g.fillOval(x,100,30,30); } }",
                "class BallPanel extends JPanel { public void animate() { for(;;) { repaint(); } } }",
                "class BallPanel extends JPanel { @Override protected void paintComponent(Graphics g) { g.fillOval(x++,100,30,30); } }",
                "b",
                "Option B correctly: updates x in a Timer callback on the EDT, calls repaint() to trigger redraw, "
                + "and draws the ball at the current x in paintComponent with super first. "
                + "Option A misses repaint(). Option C blocks the EDT. Option D increments x inside paintComponent "
                + "(side effects in paint methods cause unpredictable behavior).")
        );
    }
}
