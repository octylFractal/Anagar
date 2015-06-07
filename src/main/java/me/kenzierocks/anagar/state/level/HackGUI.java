package me.kenzierocks.anagar.state.level;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;

import javax.swing.JOptionPane;
import javax.swing.JProgressBar;
import javax.swing.Timer;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.state.JPanelBasedGUI;
import me.kenzierocks.anagar.state.PauseScreen;
import me.kenzierocks.anagar.state.State;
import me.kenzierocks.anagar.state.StateType;

import com.google.common.base.Joiner;

public class HackGUI
        extends JPanelBasedGUI implements State {

    private static final long serialVersionUID = 3727410376874340909L;

    private static final Font DRAW_FONT = Font.decode("Courier New-14");
    private static final Joiner NEWLINE = Joiner.on('\n');
    private static final Timer TIMER = new Timer(20, null);
    static {
        TIMER.start();
    }

    private static String formatString(int value, int max) {
        return String.format("%s/%s", value, max);
    }

    private static int calculateHeight(Graphics g) {
        FontMetrics fm = g.getFontMetrics(DRAW_FONT);
        return AnagarMainWindow.INSTANCE.getHeight() / fm.getHeight();
    }

    private static int calculateWidth(Graphics g) {
        FontMetrics fm = g.getFontMetrics(DRAW_FONT);
        return AnagarMainWindow.INSTANCE.getWidth() / fm.charWidth('A');
    }

    private final LevelState returnToS;
    private final LevelGUI returnToG;
    private final HackData data;
    private final int kpRequired;
    private final JProgressBar bar;
    private MatrixData tracker;
    private int stepCounter;
    private int keypresses;

    public HackGUI(HackData data, LevelState state, LevelGUI gui) {
        setLayout(new GridBagLayout());
        this.returnToS = state;
        this.returnToG = gui;
        this.data = data;
        this.kpRequired =
                (state.getLevelNum() + data.getMoneyProvided() + data
                        .getProcessingPower()) * 2;
        this.bar = new JProgressBar(0, this.kpRequired);
        this.bar.addChangeListener(new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                HackGUI.this.bar.setString(formatString(HackGUI.this.bar
                        .getValue(), HackGUI.this.kpRequired));
            }

        });
        this.bar.setString(formatString(0, this.kpRequired));
        this.bar.setBackground(Color.YELLOW);
        this.bar.setForeground(Color.MAGENTA);
        this.bar.setValue(0);
        this.bar.setStringPainted(true);
        add(this.bar);
        this.tracker = null;
        setBackground(Color.BLACK);
        TIMER.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                repaint();
            }

        });
        AnagarMainWindow.INSTANCE.addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                HackGUI.this.tracker = null;
            }

        });
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        paintMatrixLikeBackground(g);
    }

    private void paintMatrixLikeBackground(Graphics g) {
        if (this.tracker == null) {
            this.tracker =
                    MatrixData.random(calculateWidth(g), calculateHeight(g));
        }
        HackGUI.this.stepCounter++;
        if (HackGUI.this.stepCounter % 10 == 1) {
            HackGUI.this.tracker = HackGUI.this.tracker.advance();
        }
        g.setColor(Color.GREEN);
        g.setFont(DRAW_FONT);
        String toDraw = NEWLINE.join(this.tracker.getGrid());
        int y = 0;
        for (String line : toDraw.split("\n")) {
            g.drawString(line, 0, y += g.getFontMetrics().getHeight());
        }
    }

    @Override
    public String getTitle() {
        return "Hacking...";
    }

    @Override
    public StateType getType() {
        return StateType.Defaults.HACK.getAsIs();
    }

    @Override
    public void onKeyRelease(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            AnagarMainWindow.INSTANCE.setCurrentStateGUI(new PauseScreen(this));
            return;
        }
        if (this.keypresses > this.kpRequired) {
            // !!!!
            return;
        }
        this.keypresses++;
        this.bar.setValue(this.keypresses);
        if (this.keypresses > this.kpRequired) {
            finishHacking();
        }
    }

    private void finishHacking() {
        JOptionPane.showMessageDialog(null, "Hacked!");
        Player p = Player.THE_PLAYER;
        p.captureData(this.data);
        this.returnToG.updatePlayerTracker();
        AnagarMainWindow.INSTANCE.setCurrentGUI(this.returnToG);
        AnagarMainWindow.INSTANCE.setCurrentState(this.returnToS);
    }

    @Override
    public boolean onClose() {
        return true;
    }

}
