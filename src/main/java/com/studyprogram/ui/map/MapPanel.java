package com.studyprogram.ui.map;

import com.studyprogram.core.BossChallenge;
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
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * The overworld view: topics as nodes in level-band "worlds", prerequisite paths
 * between them, colors showing progress. Clicking an unlocked node toggles it in
 * the student's selected-topics set (the same set manual sessions use).
 *
 * A Swing Timer drives the animations: a staggered entrance when the map opens,
 * a pulsing glow on bosses that are ready to fight, a soft hover halo, and gold
 * "NEW!" badges on topics that have unlocked since the map was last opened
 * (tracked on the profile, so each reveal is celebrated exactly once).
 *
 * This panel is itself course material: it is a custom-painted Swing component
 * using Graphics2D, Timers, and events — exactly what the GUI units teach.
 */
public class MapPanel extends JPanel {

    private static final int COL_W = 230, ROW_H = 48, NODE_W = 200, NODE_H = 36;
    private static final int MARGIN_X = 30, MARGIN_Y = 70;

    private static final int  FRAME_MS = 33;          // ~30 fps
    private static final long ENTRANCE_MS = 900;

    private static final Color BG          = new Color(0x1d, 0x2b, 0x3a);
    private static final Color BAND        = new Color(0xff, 0xff, 0xff, 18);
    private static final Color EDGE        = new Color(0xff, 0xff, 0xff, 40);
    private static final Color LOCKED_FILL = new Color(0x3a, 0x46, 0x52);
    private static final Color AVAIL_FILL  = new Color(0x2f, 0x5d, 0x8a);
    private static final Color PROG_FILL   = new Color(0xb8, 0x86, 0x1c);
    private static final Color DONE_FILL   = new Color(0x2e, 0x7d, 0x32);
    private static final Color SECRET_FILL = new Color(0x2a, 0x23, 0x3d);
    private static final Color SELECTED    = new Color(0x4d, 0xd0, 0xe1);
    private static final Color GOLD        = new Color(0xff, 0xd5, 0x4f);

    private static final String[] WORLDS =
            {"World 1 · Foundations", "World 2 · Elementary", "World 3 · Intermediate",
             "World 4 · Advanced", "World 5 · Expert"};

    private final StudentProfile profile;
    private final Runnable onSelectionChanged;
    private List<Node> nodes;
    private Topic hovered;

    private final javax.swing.Timer animTimer;
    private long openedAt;
    private float hoverAlpha;                       // eases toward 1 while hovering
    private final Set<Topic> newlyRevealed = EnumSet.noneOf(Topic.class);

    public MapPanel(StudentProfile profile, Runnable onSelectionChanged) {
        this.profile = profile;
        this.onSelectionChanged = onSelectionChanged;
        this.nodes = MapModel.build(profile);
        setBackground(BG);
        ToolTipManager.sharedInstance().registerComponent(this);

        // Topics that unlocked since the last map visit get a one-time NEW! badge
        for (Node n : nodes) {
            if (n.state() != NodeState.LOCKED && n.state() != NodeState.SECRET
                    && !profile.getRevealedOnMap().contains(n.topic())) {
                newlyRevealed.add(n.topic());
                profile.getRevealedOnMap().add(n.topic());
            }
        }
        if (!newlyRevealed.isEmpty() && onSelectionChanged != null) {
            onSelectionChanged.run();   // persist the reveal so next open is calm
        }

        animTimer = new javax.swing.Timer(FRAME_MS, e -> onTick());

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
                }
            }
        };
        addMouseListener(mouse);
        addMouseMotionListener(mouse);
    }

    // ── Animation plumbing ───────────────────────────────────────────────────

    @Override
    public void addNotify() {
        super.addNotify();
        openedAt = System.currentTimeMillis();
        animTimer.start();
    }

    @Override
    public void removeNotify() {
        animTimer.stop();
        super.removeNotify();
    }

    private void onTick() {
        float target = hovered == null ? 0f : 1f;
        hoverAlpha += (target - hoverAlpha) * 0.25f;
        repaint();
    }

    private float entrance() {
        return Math.min(1f, (System.currentTimeMillis() - openedAt) / (float) ENTRANCE_MS);
    }

    private static float easeOut(float t) {
        float u = 1 - t;
        return 1 - u * u * u;
    }

    /** Per-column entrance progress: columns pop in left to right. */
    private float columnEase(int col) {
        float t = Math.min(1f, Math.max(0f, entrance() * 1.4f - col * 0.08f));
        return easeOut(t);
    }

    private float pulse(double periodMs) {
        return (float) (0.5 + 0.5 * Math.sin(System.currentTimeMillis() * 2 * Math.PI / periodMs));
    }

    // ── Interaction ──────────────────────────────────────────────────────────

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

    private int rowsInColumn(int col) {
        int rows = 0;
        for (Node n : nodes) if (n.col() == col) rows = Math.max(rows, n.row() + 1);
        return rows;
    }

    /** The boss "castle" node at the foot of each world column. */
    private Rectangle bossBounds(int col) {
        return new Rectangle(MARGIN_X + col * COL_W + (NODE_W - 140) / 2,
                             MARGIN_Y + rowsInColumn(col) * ROW_H + 10, 140, 32);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(MARGIN_X * 2 + 5 * COL_W - (COL_W - NODE_W),
                             MARGIN_Y + MapModel.maxRows(nodes) * ROW_H + ROW_H + 50);
    }

    @Override
    public String getToolTipText(MouseEvent e) {
        for (int col = 0; col < 5; col++) {
            if (bossBounds(col).contains(e.getPoint())) {
                int level = col + 1;
                if (profile.getBossesCleared().contains(level)) {
                    return "World " + level + "'s boss is defeated — the star is yours!";
                }
                if (BossChallenge.unlocked(profile, level)) {
                    return "The World " + level + " boss awaits! Choose Boss Challenge "
                            + "from the main menu to fight it.";
                }
                return "Reach " + (int) (BossChallenge.UNLOCK_AVG_MASTERY * 100)
                        + "% average mastery across World " + level + " to summon its boss.";
            }
        }
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

    // ── Painting ─────────────────────────────────────────────────────────────

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        Graphics2D g2 = (Graphics2D) g.create();
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        paintWorldBands(g2);
        paintEdges(g2);
        for (Node n : nodes) paintNode(g2, n);
        paintBosses(g2);
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
            boolean bossDown = profile.getBossesCleared().contains(col + 1);
            g2.setColor(new Color(255, 255, 255,
                    (int) (170 * columnEase(col))));
            g2.drawString(WORLDS[col], x + 12, 30);
            if (bossDown) {
                g2.setColor(GOLD);
                g2.drawString("★ boss cleared", x + 12, 48);
            }
        }
    }

    private void paintEdges(Graphics2D g2) {
        float edgeAlpha = easeOut(Math.max(0f, entrance() - 0.25f) / 0.75f);
        if (edgeAlpha <= 0.01f) return;
        Map<Topic, Node> byTopic = new EnumMap<>(Topic.class);
        for (Node n : nodes) byTopic.put(n.topic(), n);
        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, edgeAlpha));
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
        g2.setComposite(old);
    }

    private void paintNode(Graphics2D g2, Node n) {
        float ease = columnEase(n.col());
        if (ease <= 0.01f) return;
        int rise = (int) ((1 - ease) * 14);           // nodes drift up as they appear
        Rectangle r = bounds(n);
        r.translate(0, rise);
        RoundRectangle2D shape = new RoundRectangle2D.Float(r.x, r.y, r.width, r.height, 12, 12);

        Composite old = g2.getComposite();
        g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ease));

        // hover halo
        if (hovered == n.topic() && hoverAlpha > 0.03f) {
            g2.setColor(new Color(SELECTED.getRed(), SELECTED.getGreen(), SELECTED.getBlue(),
                    (int) (90 * hoverAlpha)));
            g2.setStroke(new BasicStroke(6f));
            g2.draw(new RoundRectangle2D.Float(r.x - 3, r.y - 3, r.width + 6, r.height + 6, 16, 16));
        }

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
                    (int) ((r.width - 8) * Math.min(1.0, n.mastery()) * ease), 4, 4, 4);
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

        // one-time reveal celebration: pulsing gold NEW! pill
        if (newlyRevealed.contains(n.topic())) {
            float p = 0.55f + 0.45f * pulse(1100);
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ease * p));
            g2.setFont(getFont().deriveFont(Font.BOLD, 10f));
            FontMetrics bfm = g2.getFontMetrics();
            String badge = "NEW!";
            int bw = bfm.stringWidth(badge) + 10;
            int bx = r.x + r.width - bw + 6, by = r.y - 8;
            g2.setColor(GOLD);
            g2.fillRoundRect(bx, by, bw, 15, 8, 8);
            g2.setColor(new Color(0x33, 0x27, 0x00));
            g2.drawString(badge, bx + 5, by + 11);
        }

        g2.setComposite(old);
    }

    private void paintBosses(Graphics2D g2) {
        for (int col = 0; col < 5; col++) {
            int level = col + 1;
            float ease = columnEase(col);
            if (ease <= 0.01f) continue;
            Rectangle r = bossBounds(col);
            boolean cleared = profile.getBossesCleared().contains(level);
            boolean ready = !cleared && BossChallenge.unlocked(profile, level);

            Composite old = g2.getComposite();
            g2.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, ease));

            // pulsing summon glow while the boss is ready
            if (ready) {
                float p = pulse(1400);
                g2.setColor(new Color(GOLD.getRed(), GOLD.getGreen(), GOLD.getBlue(),
                        (int) (70 * p)));
                g2.setStroke(new BasicStroke(7f));
                g2.drawRoundRect(r.x - 3, r.y - 3, r.width + 6, r.height + 6, 18, 18);
            }

            Color fill = cleared ? DONE_FILL
                       : ready   ? PROG_FILL
                                 : new Color(0x2c, 0x33, 0x3b);
            g2.setColor(fill);
            g2.fillRoundRect(r.x, r.y, r.width, r.height, 14, 14);
            g2.setStroke(new BasicStroke(ready ? 2f : 1f));
            g2.setColor(ready ? GOLD : new Color(255, 255, 255, 70));
            g2.drawRoundRect(r.x, r.y, r.width, r.height, 14, 14);

            g2.setFont(getFont().deriveFont(Font.BOLD, 12f));
            g2.setColor(cleared || ready ? Color.WHITE : new Color(255, 255, 255, 110));
            String label = cleared ? "★ BOSS DOWN" : ready ? "⚔ BOSS READY" : "BOSS";
            FontMetrics fm = g2.getFontMetrics();
            g2.drawString(label, r.x + (r.width - fm.stringWidth(label)) / 2,
                          r.y + (r.height + fm.getAscent()) / 2 - 3);
            g2.setComposite(old);
        }
    }
}
