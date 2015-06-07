package me.kenzierocks.anagar.state.level;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.Timer;
import javax.swing.border.Border;

import com.google.common.base.Predicate;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.Utility;
import me.kenzierocks.anagar.Utility.GridConstraints;
import me.kenzierocks.anagar.Utility.ImageIO;
import me.kenzierocks.anagar.Utility.ImageIO.Copy;
import me.kenzierocks.anagar.Utility.JComp;
import me.kenzierocks.anagar.Utility.Numbers;
import me.kenzierocks.anagar.Utility.Numbers.ExtendedRandom;
import me.kenzierocks.anagar.Utility.RectangleRecycler;
import me.kenzierocks.anagar.state.JPanelBasedGUI;
import me.kenzierocks.anagar.state.State;
import me.kenzierocks.anagar.swing.ActuallyLayeredPane;
import autovalue.shaded.com.google.common.common.collect.ImmutableList;

public class LevelGUI
        extends JPanelBasedGUI {

    private static final long serialVersionUID = -850649718212152509L;

    private static final Color NICE_SHADE_OF_BLUE = Color.CYAN;
    public static final int IMAGE_SIZE = 64;
    private static final int PADDING = 10;
    private static final int OFFSET = (IMAGE_SIZE + 1 / 2) + PADDING;
    private static final int LIKELY_FILLED = 100;
    private static final int NICE_LINE_LOOK_REQUIRED_MAX = 3;
    private static final int ASSUMED_GRID_SIZE = 10;

    private static final Font LEVEL_TITLE_FONT = Font.decode("Courier New-16");

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

    private static int calculateMoneyRequired(int levelNum) {
        int pls = levelNum + 1;
        int eCount = calculateLevelEnemyCount(levelNum);
        eCount += levelNum;
        return (int) (50 * Math.sqrt(eCount * Math.exp(pls)));
    }

    private final int levelNum;
    private final ExtendedRandom random;
    public final ActuallyLayeredPane pane = new ActuallyLayeredPane(
            Utility.JComp.BORDER_LAYOUT_SUPPLIER);
    private final MouseListener openHackGUI = new OpenHackGUIListener(this);
    private final int moneyRequired;
    private final String prettyRequiredMoney;

    private JLabel ppLabel;

    private JLabel mpsLabel;

    private JLabel moneyLabel;

    public LevelGUI(int levelNum) {
        this.levelNum = levelNum;
        this.random =
                ExtendedRandom.wrap(new Random(serialVersionUID
                        / (this.levelNum + 1)));
        this.moneyRequired = calculateMoneyRequired(levelNum);
        this.prettyRequiredMoney = Numbers.prettify(this.moneyRequired);
        setLayout(new BorderLayout());
        add(this.pane);
        generateLevel();
        addPlayerTracker();
        updatePlayerTracker();
    }

    public JLevelComponent getComponentWithData(HackData data) {
        for (Component c : this.pane.getLayer(Layer.TARGETS.ordinal())
                .getComponents()) {
            if (c instanceof JLevelComponent) {
                JLevelComponent jlc = (JLevelComponent) c;
                if (jlc.getMetaData() == data) {
                    return jlc;
                }
            }
        }
        return null;
    }

    private String getMoneyFormatted(Player p) {
        return "Money: $" + Numbers.prettify(p.getMoney()) + "/$"
                + this.prettyRequiredMoney;
    }

    private String getMoneyPerSecondFormatted(Player p) {
        return "MPS: $" + Numbers.prettify(p.getMoneyPerSecond()) + "/s";
    }

    private String getPPFormated(Player p) {
        return "Processing Power: " + Numbers.prettify(p.getProcessingPower());
    }

    private void addPlayerTracker() {
        this.pane.removeLayer(Layer.TRACKER.ordinal());
        JPanel gui = new JPanel();
        JPanel layer = this.pane.getLayer(Layer.TRACKER.ordinal());
        layer.setLayout(new FlowLayout(FlowLayout.LEFT));
        layer.add(gui);
        gui.setLayout(new GridBagLayout());
        gui.setBackground(Color.GREEN);
        Border cyanLine = BorderFactory.createLineBorder(Color.CYAN, 3, true);
        Border whiteLine = BorderFactory.createLineBorder(Color.WHITE);
        Border merge1 = BorderFactory.createCompoundBorder(cyanLine, whiteLine);
        Border merge2 = BorderFactory.createCompoundBorder(whiteLine, merge1);
        gui.setBorder(merge2);
        Player p = Player.THE_PLAYER;
        GridConstraints cons =
                new GridConstraints().setFill(GridBagConstraints.BOTH);
        cons.insets = new Insets(0, 5, 0, 5);
        JLabel titleLabel =
                new JLabel("Level " + Numbers.prettify(this.levelNum),
                        JLabel.CENTER);
        titleLabel.setFont(LEVEL_TITLE_FONT);
        gui.add(Box.createVerticalStrut(5), cons.setCoords(0, 0));
        gui.add(titleLabel, cons.setCoords(0, 1));
        gui.add(new JSeparator(), cons.setCoords(0, 2));
        this.moneyLabel = new JLabel(getMoneyFormatted(p));
        this.mpsLabel = new JLabel(getMoneyPerSecondFormatted(p));
        this.ppLabel = new JLabel(getPPFormated(p));
        gui.add(this.moneyLabel, cons.setCoords(0, 3));
        gui.add(this.mpsLabel, cons.setCoords(0, 4));
        gui.add(this.ppLabel, cons.setCoords(0, 5));
        gui.add(Box.createVerticalStrut(5), cons.setCoords(0, 6));
    }

    public void updatePlayerTracker() {
        Player p = Player.THE_PLAYER;
        this.moneyLabel.setText(getMoneyFormatted(p));
        this.mpsLabel.setText(getMoneyPerSecondFormatted(p));
        this.ppLabel.setText(getPPFormated(p));
        if (p.getMoney() >= this.moneyRequired) {
            waitForUsToBeGUI();
            JOptionPane
                    .showMessageDialog(null,
                                       "Level "
                                               + Numbers
                                                       .prettify(this.levelNum)
                                               + " Complete!",
                                       "Level Complete",
                                       JOptionPane.INFORMATION_MESSAGE);
            p.reset();
            LevelGUI next = new LevelGUI(this.levelNum + 1);
            LevelState nextS = new LevelState(next.levelNum);
            nextS.setCurrentGUI(next);
            AnagarMainWindow.INSTANCE.setCurrentGUI(next);
            AnagarMainWindow.INSTANCE.setCurrentState(nextS);
        }
        AnagarMainWindow.refreshAll();
    }

    private void waitForUsToBeGUI() {
        final Predicate<Object> condition = new Predicate<Object>() {

            @Override
            public boolean apply(Object input) {
                State s = AnagarMainWindow.INSTANCE.getCurrentState();
                if (s instanceof LevelState) {
                    LevelState ls = (LevelState) s;
                    if (ls.getCurrentGUI() == LevelGUI.this) {
                        return true;
                    }
                }
                return false;
            }
        };
        final Object await = new Object();
        final Timer temp = new Timer(10, null);
        temp.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                if (condition.apply(null)) {
                    temp.stop();
                    synchronized (await) {
                        await.notify();
                    }
                }
            }

        });
        temp.start();
        while (!condition.apply(null)) {
            try {
                synchronized (await) {
                    await.wait();
                }
            } catch (InterruptedException e1) {
            }
        }
    }

    private LevelHackObject getRandomData() {
        return this.random.getRandomItem(LevelHackObject.DEFAULTS);
    }

    private void generateLevel() {
        int count = calculateLevelEnemyCount(this.levelNum);
        int attempts = 0;
        JPanel panel = this.pane.getLayer(Layer.TARGETS.ordinal());
        panel.setLayout(null);
        mainloop: for (int i = 0; i < count; i++) {
            LevelHackObject hackObject = getRandomData();
            BufferedImage image =
                    ImageIO.asBufferedImage(hackObject.getImage(), Copy.LAZY);
            int x = this.random.getRangedInt(calculateWidth(), OFFSET);
            int y = this.random.getRangedInt(calculateHeight(), OFFSET);
            Rectangle bounds =
                    new Rectangle(x, y, image.getHeight(), image.getWidth());
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
            Component comp =
                    new JLevelComponent(image, hackObject.getMetaData().get());
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
            BufferedImage image =
                    ImageIO.asBufferedImage(hackObject.getImage(), Copy.LAZY);
            Component comp =
                    new JLevelComponent(image, hackObject.getMetaData().get());
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
        JPanel panel = this.pane.getLayer(Layer.LINE.ordinal());
        panel.removeAll();
        Rectangle fromRect = RectangleRecycler.fetch();
        Rectangle toRect = RectangleRecycler.fetch();
        List<Component> toConnect =
                ImmutableList.copyOf(this.pane
                        .getLayer(Layer.TARGETS.ordinal()).getComponents());
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
                        || this.random.randomPercent(100)) {
                    lines.add(new int[] { (int) xFrom, (int) yFrom, (int) xTo,
                            (int) yTo });
                }
            }
        }
        class LinePanel
                extends JPanel {

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
                    g.drawLine(line[0] + 2,
                               line[1] + 2,
                               line[2] + 2,
                               line[3] + 2);
                    g.setColor(NICE_SHADE_OF_BLUE);
                    g.drawLine(line[0] + 1,
                               line[1] + 1,
                               line[2] + 1,
                               line[3] + 1);
                    g.drawLine(line[0], line[1], line[2], line[3]);
                    g.drawLine(line[0] - 1,
                               line[1] - 1,
                               line[2] - 1,
                               line[3] - 1);
                    g.setColor(Color.WHITE);
                    g.drawLine(line[0] - 2,
                               line[1] - 2,
                               line[2] - 2,
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
