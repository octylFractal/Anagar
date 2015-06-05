package me.kenzierocks.anagar.state.level;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.JPanel;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.Utility;
import me.kenzierocks.anagar.Utility.GridConstraints;
import me.kenzierocks.anagar.Utility.ImageIO;
import me.kenzierocks.anagar.Utility.ImageIO.Copy;
import me.kenzierocks.anagar.Utility.JComp;
import me.kenzierocks.anagar.Utility.Numbers.ExtendedRandom;
import me.kenzierocks.anagar.Utility.RectangleRecycler;
import me.kenzierocks.anagar.state.JPanelBasedGUI;
import me.kenzierocks.anagar.swing.ActuallyLayeredPane;
import autovalue.shaded.com.google.common.common.collect.ImmutableList;

public class LevelGUI extends JPanelBasedGUI {

    private static final long serialVersionUID = -850649718212152509L;

    private static final Color NICE_SHADE_OF_BLUE = Color.CYAN;
    public static final int IMAGE_SIZE = 64;
    private static final int PADDING = 10;
    private static final int OFFSET = (IMAGE_SIZE + 1 / 2) + PADDING;
    private static final int LIKELY_FILLED = 100;
    private static final int NICE_LINE_LOOK_REQUIRED_MAX = 3;
    private static final int ASSUMED_GRID_SIZE = 10;
    public static final int LINE_LAYER = 99;
    public static final int TARGETS_LAYER = LINE_LAYER + 1;
    public static final int PANEL_LAYER = TARGETS_LAYER + 1;

    private static int calculateWidth() {
        Insets i = AnagarMainWindow.INSTANCE.getInsets();
        return AnagarMainWindow.INSTANCE.getWidth() - i.right - i.left - OFFSET
                * 2;
    }

    private static int calculateHeight() {
        Insets i = AnagarMainWindow.INSTANCE.getInsets();
        return AnagarMainWindow.INSTANCE.getHeight() - i.top - i.bottom
                - OFFSET * 2;
    }

    private static int calculateLevelEnemyCount(int levelNum) {
        return (int) (levelNum * 1.5d) + 2;
    }

    private final int levelNum;
    private final ExtendedRandom random;
    public final ActuallyLayeredPane pane = new ActuallyLayeredPane(
            Utility.JComp.BORDER_LAYOUT_SUPPLIER);

    private final MouseListener openHackGUI = new OpenHackGUIListener(this);

    public LevelGUI(int levelNum) {
        this.levelNum = levelNum;
        this.random = ExtendedRandom.wrap(new Random(serialVersionUID
                / (this.levelNum + 1)));
        setLayout(new BorderLayout());
        add(this.pane);
        generateLevel();
    }

    private LevelHackObject getRandomData() {
        return this.random.getRandomItem(LevelHackObject.DEFAULTS);
    }

    private void generateLevel() {
        int count = calculateLevelEnemyCount(this.levelNum);
        int attempts = 0;
        JPanel panel = this.pane.getLayer(TARGETS_LAYER);
        panel.setLayout(null);
        mainloop: for (int i = 0; i < count; i++) {
            LevelHackObject hackObject = getRandomData();
            BufferedImage image = ImageIO.asBufferedImage(
                    hackObject.getImage(), Copy.LAZY);
            int x = this.random.getRangedInt(calculateWidth(), OFFSET);
            int y = this.random.getRangedInt(calculateHeight(), OFFSET);
            Rectangle bounds = new Rectangle(x, y, image.getHeight(),
                    image.getWidth());
            for (Component z : panel.getComponents()) {
                if (z.getBounds().intersects(bounds)) {
                    i--;
                    attempts++;
                    if (attempts > LIKELY_FILLED) {
                        clearAndGridLayout(count, panel);
                        break mainloop;
                    }
                    continue mainloop;
                }
            }
            attempts = 0;
            Component comp = new JLevelComponent(image, hackObject
                    .getMetaData().get());
            comp.addMouseListener(this.openHackGUI);
            comp.setLocation(x, y);
            panel.add(comp);
        }
        ComponentAdapter addLinesListener = new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                addLines();
                AnagarMainWindow.refreshAll();
            }

        };
        panel.addComponentListener(addLinesListener);
        addLinesListener.componentResized(null);
    }

    private void clearAndGridLayout(int count, JPanel panel) {
        panel.removeAll();
        panel.setLayout(new GridBagLayout());
        GridConstraints cons = new GridConstraints();
        cons.ipadx = cons.ipady = 64 + 10;
        int x = 0;
        int y = 0;
        for (int i = 0; i < count; i++) {
            LevelHackObject hackObject = getRandomData();
            BufferedImage image = ImageIO.asBufferedImage(
                    hackObject.getImage(), Copy.LAZY);
            Component comp = new JLevelComponent(image, hackObject
                    .getMetaData().get());
            panel.add(comp, cons.setCoords(x, y));
            x++;
            if (x > ASSUMED_GRID_SIZE) {
                x = 0;
                y++;
                if (y > ASSUMED_GRID_SIZE) {
                    y = 0;
                }
            }
        }
    }

    private void addLines() {
        JPanel panel = this.pane.getLayer(LINE_LAYER);
        panel.removeAll();
        Rectangle fromRect = RectangleRecycler.fetch();
        Rectangle toRect = RectangleRecycler.fetch();
        List<Component> toConnect = ImmutableList.copyOf(this.pane.getLayer(
                TARGETS_LAYER).getComponents());
        int compCount = toConnect.size();
        final List<int[]> lines = new ArrayList<>(compCount * compCount);
        for (int i = 0; i < compCount; i++) {
            Component from = toConnect.get(i);
            from.getBounds(fromRect);
            double xFrom = fromRect.getCenterX();
            double yFrom = fromRect.getCenterY();
            for (int j = i + 1; j < compCount; j++) {
                Component to = toConnect.get(j);
                to.getBounds(toRect);
                double xTo = toRect.getCenterX();
                double yTo = toRect.getCenterY();
                if (lines.size() <= NICE_LINE_LOOK_REQUIRED_MAX
                        || this.random.getInstance().nextInt(100) > 70) {
                    lines.add(new int[] { (int) xFrom, (int) yFrom, (int) xTo,
                            (int) yTo });
                }
            }
        }
        class LinePanel extends JPanel {

            private static final long serialVersionUID = 4753074418055771306L;

            {
                setForeground(JComp.TRANSPARENT_COLOR);
                setBackground(JComp.TRANSPARENT_COLOR);
                setOpaque(false);
            }

            @Override
            public void paint(Graphics g) {
                for (int[] line : lines) {
                    g.setColor(Color.WHITE);
                    g.drawLine(line[0] + 2, line[1] + 2, line[2] + 2,
                            line[3] + 2);
                    g.setColor(NICE_SHADE_OF_BLUE);
                    g.drawLine(line[0] + 1, line[1] + 1, line[2] + 1,
                            line[3] + 1);
                    g.drawLine(line[0], line[1], line[2], line[3]);
                    g.drawLine(line[0] - 1, line[1] - 1, line[2] - 1,
                            line[3] - 1);
                    g.setColor(Color.WHITE);
                    g.drawLine(line[0] - 2, line[1] - 2, line[2] - 2,
                            line[3] - 2);
                }
            }

        }
        JPanel linePanel = new LinePanel();
        panel.add(linePanel);
    }

    @Override
    public String getTitle() {
        return "Level " + Utility.Numbers.prettify(this.levelNum);
    }

}
