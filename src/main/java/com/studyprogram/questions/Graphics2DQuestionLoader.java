package com.studyprogram.questions;

import com.studyprogram.model.Question;
import com.studyprogram.model.Topic;

import java.util.List;

public class Graphics2DQuestionLoader extends BaseQuestionLoader {

    @Override public Topic getTopic() { return Topic.GRAPHICS_2D; }

    @Override
    public List<Question> load() {
        return List.of(

            mc("g2d-mc-01", Topic.GRAPHICS_2D, 3,
                "How do you get a Graphics2D object inside paintComponent()?",
                "Graphics2D g2d = new Graphics2D();",
                "Graphics2D g2d = (Graphics2D) g;",
                "Graphics2D g2d = Graphics2D.create();",
                "Graphics2D g2d = getGraphics2D();",
                "b",
                "The Graphics parameter passed to paintComponent is actually a Graphics2D instance at runtime. "
                + "Cast it: Graphics2D g2d = (Graphics2D) g; "
                + "Graphics2D adds anti-aliasing, strokes, transforms, and gradient fills not available in Graphics."),

            mc("g2d-mc-02", Topic.GRAPHICS_2D, 3,
                "Which rendering hint enables anti-aliasing for smoother shapes?",
                "g2d.setSmooth(true)",
                "g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)",
                "g2d.setStroke(new BasicStroke(2, BasicStroke.SMOOTH, BasicStroke.ROUND))",
                "g2d.setQuality(HIGH)",
                "b",
                "RenderingHints.KEY_ANTIALIASING with VALUE_ANTIALIAS_ON enables anti-aliased rendering "
                + "for shapes and text, producing smoother edges. "
                + "This is especially visible for diagonal lines and curves at low resolutions."),

            mc("g2d-mc-03", Topic.GRAPHICS_2D, 3,
                "What does g2d.setStroke(new BasicStroke(3f)) do?",
                "Sets the font size to 3",
                "Sets the line drawing width to 3 pixels",
                "Sets the opacity of drawn shapes to 30%",
                "Sets the color to a shade of gray",
                "b",
                "BasicStroke controls the width and style of lines drawn by draw() calls (drawLine, drawRect, etc.). "
                + "new BasicStroke(3f) makes lines 3 pixels wide. "
                + "You can also control end caps and line joins: new BasicStroke(3f, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND)."),

            mc("g2d-mc-04", Topic.GRAPHICS_2D, 3,
                "What is the difference between fill*() and draw*() methods in Graphics2D?",
                "fill() uses the current color; draw() always uses black",
                "fill() fills the interior of a shape; draw() draws only the outline",
                "draw() is faster than fill()",
                "fill() requires a Brush object; draw() does not",
                "b",
                "drawRect/drawOval/drawLine draw outlines (edges only). "
                + "fillRect/fillOval fill the interior with the current color. "
                + "To draw a filled shape with an outline: call fillRect first, set a different color, then drawRect."),

            trace("g2d-tr-01", Topic.GRAPHICS_2D, 3,
                "What shape is drawn and where?",
                "Graphics2D g2d = (Graphics2D) g;\n"
                + "g2d.setColor(Color.GREEN);\n"
                + "g2d.fillOval(30, 40, 60, 60);",
                "A filled green circle, top-left at (30,40), 60x60 pixels",
                "fillOval(x, y, width, height) draws an ellipse. With equal width and height it is a circle. "
                + "Color is green. The bounding box starts at (30,40)."),

            debug("g2d-db-01", Topic.GRAPHICS_2D, 3,
                "After calling setColor(Color.RED), drawn shapes still appear black. Why?",
                "@Override\n"
                + "protected void paintComponent(Graphics g) {\n"
                + "    super.paintComponent(g);\n"
                + "    Graphics2D g2d = (Graphics2D) g;\n"
                + "    g2d.setColor(Color.RED);\n"
                + "    g.fillRect(10, 10, 50, 50);  // using 'g', not 'g2d'\n"
                + "}",
                "setColor does not affect fillRect",
                "The color is set on g2d but fillRect is called on the original g reference — they share state, so this actually works fine",
                "g2d.setColor only applies to draw() calls, not fill() calls",
                "The super.paintComponent() resets the color after setColor",
                "b",
                "g and g2d are the same underlying object (g2d is just a cast of g). "
                + "Setting color on g2d and drawing with g actually works — this is not the bug. "
                + "The real subtle issue: super.paintComponent(g) may reset the color to the component's foreground. "
                + "Always set color AFTER super.paintComponent(). In this code the order is correct, so shapes should be red."),

            codegen("g2d-cg-01", Topic.GRAPHICS_2D, 3,
                "Which draws an anti-aliased blue rectangle outline with a 2-pixel border?",
                "g.setColor(Color.BLUE); g.drawRect(10,10,80,50);",
                "Graphics2D g2d = (Graphics2D) g; g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON); g2d.setStroke(new BasicStroke(2f)); g2d.setColor(Color.BLUE); g2d.drawRect(10,10,80,50);",
                "g.setStroke(2); g.drawRect(10,10,80,50);",
                "Graphics2D g2d = (Graphics2D) g; g2d.setColor(Color.BLUE); g2d.fillRect(10,10,80,50);",
                "b",
                "Enable anti-aliasing via RenderingHints, set stroke width with BasicStroke(2f), set color, then drawRect for the outline. "
                + "Option A lacks stroke width control. Option C: Graphics has no setStroke method. "
                + "Option D draws a filled rectangle, not an outline."),

            mc("g2d-mc-05", Topic.GRAPHICS_2D, 3,
                "What does g2d.rotate(Math.PI / 4) do?",
                "Rotates the entire JPanel 45 degrees",
                "Rotates the Graphics2D coordinate system 45 degrees clockwise around the origin",
                "Rotates an image 45 degrees without affecting other drawing",
                "Rotates text only",
                "b",
                "Graphics2D transforms affect the coordinate system, not just a single shape. "
                + "After rotate(Math.PI/4), all subsequent draw calls are rotated. "
                + "Save/restore the transform: AffineTransform old = g2d.getTransform(); ... g2d.setTransform(old); "
                + "to limit rotation to specific shapes."),

            mc("g2d-mc-06", Topic.GRAPHICS_2D, 3,
                "What does a GradientPaint do in Graphics2D?",
                "Renders shapes with a transparent (see-through) fill",
                "Fills a shape with a color that gradually transitions between two colors",
                "Adds a drop shadow to drawn shapes",
                "Paints only the outline of shapes in a gradient",
                "b",
                "GradientPaint(x1,y1,color1, x2,y2,color2) creates a linear gradient from color1 to color2. "
                + "g2d.setPaint(new GradientPaint(0,0,Color.RED, 100,0,Color.BLUE)); g2d.fillRect(0,0,100,50); "
                + "draws a rectangle that fades from red (left) to blue (right)."),

            mc("g2d-mc-07", Topic.GRAPHICS_2D, 3,
                "What is the Shape interface in Java2D?",
                "An interface that all Swing components implement for custom bounds",
                "An interface representing a geometric shape that can be drawn, filled, or used for hit testing",
                "An interface that Graphics2D uses instead of Graphics",
                "An interface for 3D shapes in JavaFX",
                "b",
                "Shape is implemented by Rectangle2D, Ellipse2D, Path2D, Arc2D, etc. "
                + "g2d.draw(shape) draws the outline; g2d.fill(shape) fills it. "
                + "shape.contains(x, y) checks if a point is inside — useful for click hit testing."),

            trace("g2d-tr-02", Topic.GRAPHICS_2D, 3,
                "What is drawn?",
                "Graphics2D g2d = (Graphics2D) g;\n"
                + "g2d.setColor(Color.ORANGE);\n"
                + "g2d.fillRoundRect(20, 20, 100, 60, 15, 15);",
                "An orange rounded rectangle at (20,20), 100x60 pixels, with 15-pixel arc corners",
                "fillRoundRect(x, y, width, height, arcWidth, arcHeight) draws a rectangle with rounded corners. "
                + "arcWidth and arcHeight control the curve radius of the corners."),

            trace("g2d-tr-03", Topic.GRAPHICS_2D, 3,
                "After g2d.scale(2.0, 2.0), where does g2d.drawRect(10, 10, 50, 30) actually appear?",
                "Graphics2D g2d = (Graphics2D) g;\n"
                + "g2d.scale(2.0, 2.0);\n"
                + "g2d.drawRect(10, 10, 50, 30);",
                "At (20,20) with size 100x60 in screen pixels",
                "scale(2.0, 2.0) doubles all coordinates. The logical (10,10,50,30) maps to screen (20,20,100,60). "
                + "All subsequent drawing operations are scaled by the same factor."),

            debug("g2d-db-02", Topic.GRAPHICS_2D, 3,
                "After rotating and drawing, all subsequent shapes are also rotated unexpectedly. Why?",
                "Graphics2D g2d = (Graphics2D) g;\n"
                + "g2d.rotate(Math.PI / 6);  // 30 degrees\n"
                + "g2d.fillRect(50, 50, 80, 40);\n"
                + "// later: draw unrotated background — still rotated!",
                "rotate() permanently changes the canvas orientation",
                "The transform was not saved and restored — rotate() modifies the Graphics2D state permanently for this paint cycle",
                "fillRect ignores transforms",
                "rotate() applies to shapes, not the coordinate system",
                "b",
                "Graphics2D transforms accumulate. To rotate only one shape: "
                + "AffineTransform saved = g2d.getTransform(); g2d.rotate(angle); g2d.fillRect(...); g2d.setTransform(saved); "
                + "Or use: g2d.rotate(angle, cx, cy) to rotate around a specific center point."),

            codegen("g2d-cg-02", Topic.GRAPHICS_2D, 3,
                "Which draws a red-to-blue horizontal gradient across a 200x100 rectangle?",
                "g.setColor(Color.RED); g.fillRect(0,0,200,100);",
                "Graphics2D g2d = (Graphics2D) g; g2d.setPaint(new GradientPaint(0,0,Color.RED,200,0,Color.BLUE)); g2d.fillRect(0,0,200,100);",
                "Graphics2D g2d = (Graphics2D) g; g2d.setGradient(Color.RED, Color.BLUE); g2d.fillRect(0,0,200,100);",
                "g.drawGradient(0,0,200,100,Color.RED,Color.BLUE);",
                "b",
                "GradientPaint(x1,y1,startColor, x2,y2,endColor) defines a linear gradient from left to right. "
                + "setPaint sets it as the current paint; fillRect uses it. "
                + "Options C and D use methods that don't exist in the Graphics/Graphics2D API.")
        );
    }
}
