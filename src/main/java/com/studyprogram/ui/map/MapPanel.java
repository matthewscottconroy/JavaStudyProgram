package com.studyprogram.ui.map;

import com.studyprogram.model.StudentProfile;
import com.studyprogram.model.Topic;
import com.studyprogram.ui.map.MapModel.Node;
import com.studyprogram.ui.map.MapModel.NodeState;

import javax.swing.JPanel;
import javax.swing.ToolTipManager;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.CubicCurve2D;
import java.awt.geom.RoundRectangle2D;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * The overworld view: topics as nodes in level-band "worlds", prerequisite paths
 * between them, colors showing progress. Clicking an unlocked node toggles it in
 * the student's selected-topics set (the same set manual sessions use).
 *
 * This panel is itself course material: it is a custom-painted Swing component
 * using Graphics2D — exactly what the GUI units teach.
 */
public class MapPanel extends JPanel {

    private static final int COL_W = 230, ROW_H = 48, NODE_W = 200, NODE_H = 36;
    private static final int MARGIN_X = 30, MARGIN_Y = 70;

    private static final Color BG          = new Color(0x1d, 0x2b, 0x3a);
    private static final Color BAND        = new Color(0xff, 0xff, 0xff, 18);
    private static final Color EDGE        = new Color(0xff, 0xff, 0xff, 40);
    private static final Color LOCKED_FILL = new Color(0x3a, 0x46, 0x52);
    private static final Color AVAIL_FILL  = new Color(0x2f, 0x5d, 0x8a);
    private static final Color PROG_FILL   = new Color(0xb8, 0x86, 0x1c);
    private static final Color DONE_FILL   = new Color(0x2e, 0x7d, 0x32);
    private static final Color SECRET_FILL = new Color(0x2a, 0x23, 0x3d);
    private static final Color SELECTED    = new Color(0x4d, 0xd0, 0xe1);

    private static final String[] WORLDS =
            {"World 1 · Foundations", "World 2 · Elementary", "World 3 · Intermediate",
             "World 4 · Advanced", "World 5 · Expert"};

    private final StudentProfile profile;
    private final Runnable onSelectionChanged;
    private List<Node> nodes;
    private Topic hovered;

    public MapPanel(StudentProfile profile, Runnable onSelectionChanged) {
        this.profile = profile;
        this.onSelectionChanged = onSelectionChanged;
        this.nodes = MapModel.build(profile);
        setBackground(BG);
        ToolTipManager.sharedInstance().registerComponent(this);

        MouseAdapter mouse = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                nodeAt(e.getPoint()).ifPresent(MapPanel.this::toggle);
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                Topic t = nodeAt(e.getPoint()).map(Node::topic).orElse(null);
                if (t != hovered) {
                    hovered = t;
                    setCursor(Cursor.getPredefinedCursor(
                            t == null ? Cursor.DEFAULT_CURSOR : Cursor.HAND_CURSOR));
                    repaint();
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    private void toggle(Node node) {
        if (node.state() == NodeState.SECRET) return;   // secrets stay secret until unlocked
        Topic t = node.topic();
        if (!profile.getSelectedTopics().remove(t)) {
            profile.getSelectedTopics().add(t);
        }
        nodes = MapModel.build(profile);
        repaint();
        if (onSelectionChanged != null) onSelectionChanged.run();
    }

    private java.util.Optional<Node> nodeAt(Point p) {
        return nodes.stream().filter(n -> bounds(n).contains(p)).findFirst();
    }

    private Rectangle bounds(Node n) {
        return new Rectangle(MARGIN_X + n.col() * COL_W,
                             MARGIN_Y + n.row() * ROW_H, NODE_W, NODE_H);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(MARGIN_X * 2 + 5 * COL_W - (COL_W - NODE_W),
                             MARGIN_Y + MapModel.maxRows(nodes) * ROW_H + 30);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        return nodeAt(e.getPoint()).map(n -> {
            if (n.state() == NodeState.SECRET) {
                return "A secret path… master its prerequisites to reveal it.";
            }
            Topic t = n.topic();
            StringBuilder sb = new StringBuilder("<html><b>").append(t.displayName)
                    .append("</b><br>").append(t.description)
                    .append("<br>Mastery: ").append(Math.round(n.mastery() * 100)).append("%");
            if (!t.getPrerequisites().isEmpty()) {
                sb.append("<br>Requires: ");
                t.getPrerequisites().forEach(p -> sb.append(p.displayName).append("  "));
            }
            sb.append("<br><i>Click to ").append(n.selected() ? "remove from" : "add to")
              .append(" your session topics</i></html>");
            return sb.toString();
        }).orElse(null);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintWorldBands(g2);
        paintEdges(g2);
        for (Node n : nodes) paintNode(g2, n);
        g2.dispose();
    }

    private void paintWorldBands(Graphics2D g2) {
        g2.setFont(getFont().deriveFont(Font.BOLD, 13f));
        for (int col = 0; col < 5; col++) {
            int x = MARGIN_X + col * COL_W - 10;
            if (col % 2 == 1) {
                g2.setColor(BAND);
                g2.fillRect(x, 0, COL_W, getHeight());
            }
            g2.setColor(new Color(255, 255, 255, 170));
            g2.drawString(WORLDS[col], x + 12, 30);
        }
    }

    private void paintEdges(Graphics2D g2) {
        Map<Topic, Node> byTopic = new EnumMap<>(Topic.class);
        for (Node n : nodes) byTopic.put(n.topic(), n);
        for (Node n : nodes) {
            Rectangle to = bounds(n);
            for (Topic prereq : n.topic().getPrerequisites()) {
                Node from = byTopic.get(prereq);
                if (from == null) continue;
                Rectangle fb = bounds(from);
                boolean hot = hovered == n.topic() || hovered == prereq;
                g2.setColor(hot ? SELECTED : EDGE);
                g2.setStroke(new BasicStroke(hot ? 2f : 1f));
                int x1 = fb.x + fb.width, y1 = fb.y + fb.height / 2;
                int x2 = to.x,             y2 = to.y + to.height / 2;
                g2.draw(new CubicCurve2D.Float(x1, y1, x1 + 30, y1, x2 - 30, y2, x2, y2));
            }
        }
    }

    private void paintNode(Graphics2D g2, Node n) {
        Rectangle r = bounds(n);
        RoundRectangle2D shape = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 12, 12);

        g2.setColor(switch (n.state()) {
            case LOCKED -> LOCKED_FILL;
            case SECRET -> SECRET_FILL;
            case AVAILABLE -> AVAIL_FILL;
            case IN_PROGRESS -> PROG_FILL;
            case MASTERED -> DONE_FILL;
        });
        g2.fill(shape);

        // mastery bar along the bottom edge
        if (n.mastery() > 0 && n.state() != NodeState.SECRET) {
            g2.setColor(new Color(255, 255, 255, 90));
            g2.fillRoundRect(r.x + 4, r.y + r.height - 7,
                    (int) ((r.width - 8) * Math.min(1.0, n.mastery())), 4, 4, 4);
        }

        g2.setStroke(new BasicStroke(n.selected() ? 2.5f : 1f));
        g2.setColor(n.selected() ? SELECTED : new Color(255, 255, 255, 70));
        g2.draw(shape);

        g2.setFont(getFont().deriveFont(
                n.state() == NodeState.MASTERED ? Font.BOLD : Font.PLAIN, 12f));
        g2.setColor(n.state() == NodeState.LOCKED
                ? new Color(255, 255, 255, 110) : Color.WHITE);
        String label = n.label();
        FontMetrics fm = g2.getFontMetrics();
        if (fm.stringWidth(label) > r.width - 16) {
            while (fm.stringWidth(label + "…") > r.width - 16 && label.length() > 3) {
                label = label.substring(0, label.length() - 1);
            }
            label += "…";
        }
        g2.drawString(label, r.x + 8, r.y + (r.height + fm.getAscent()) / 2 - 3);

        if (n.state() == NodeState.MASTERED) {
            g2.drawString("★", r.x + r.width - 18, r.y + (r.height + fm.getAscent()) / 2 - 3);
        }
    }
}
