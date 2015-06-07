package me.kenzierocks.anagar.state.level;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics2D;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerAdapter;
import java.awt.event.ContainerEvent;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.Utility.GridConstraints;
import me.kenzierocks.anagar.Utility.JComp;
import me.kenzierocks.anagar.functional.Consumer;

public class OpenHackGUIListener implements MouseListener {

    private static final String STATE_KEY = OpenHackGUIListener.class.getName()
            + ".state";

    private static enum SelectionState {
        OFF, HOVER_ON, SELECTED_ON;
    }

    private static final Color HOVER_COLOR = JComp
            .transparentify(Color.RED, 55);
    private static final Color SELECT_COLOR = JComp.transparentify(Color.BLUE,
                                                                   55);

    private final Consumer<Component> SETUP_COMPONENT =
            new Consumer<Component>() {

                @Override
                public void consume(Component c) {
                    for (MouseListener ml : c.getMouseListeners()) {
                        if (ml == OpenHackGUIListener.this) {
                            return;
                        }
                    }
                    AnagarMainWindow.INSTANCE.getMotionTracker()
                            .forComponent(c)
                            .addMouseListener(OpenHackGUIListener.this);
                    ((JComponent) c).putClientProperty(STATE_KEY,
                                                       SelectionState.OFF);
                }
            };

    private final LevelGUI gui;

    public OpenHackGUIListener(LevelGUI levelGUI) {
        this.gui = levelGUI;
        JPanel layer = levelGUI.pane.getLayer(Layer.TARGETS.ordinal());
        for (Component c : layer.getComponents()) {
            this.SETUP_COMPONENT.consume(c);
        }
        layer.addContainerListener(new ContainerAdapter() {

            @Override
            public void componentAdded(ContainerEvent e) {
                OpenHackGUIListener.this.SETUP_COMPONENT.consume(e.getChild());
            }

        });
        AnagarMainWindow.INSTANCE.addTotalMouseController(this);
    }

    private void select(Point loc, JLevelComponent c, SelectionState state) {
        try {
            if (!c.contains(loc)) {
                c.putClientProperty(STATE_KEY, SelectionState.OFF);
                if (c.isPushed()) {
                    c.popIcon();
                }
                return;
            }
            SelectionState old =
                    (SelectionState) c.getClientProperty(STATE_KEY);
            c.putClientProperty(STATE_KEY, state);
            if (old != SelectionState.OFF) {
                Component hoverTarget =
                        JComp.getComponentAtBottomOfTreeAt(SwingUtilities
                                .convertPoint(c,
                                              loc,
                                              AnagarMainWindow.INSTANCE.internalPanel));
                if (hoverTarget != c) {
                    // ugh
                    if (c.isPushed()) {
                        c.popIcon();
                    }
                    return;
                }
                if (c.isPushed()) {
                    c.popIcon();
                }
            }
            if (state != SelectionState.OFF) {
                BufferedImage imag = c.getIconAsBufferedImage(true);
                Graphics2D g = imag.createGraphics();
                if (state == SelectionState.HOVER_ON) {
                    g.setColor(HOVER_COLOR);
                } else if (state == SelectionState.SELECTED_ON) {
                    g.setColor(SELECT_COLOR);
                }
                JComp.drawWhereItIs(imag, g);
                g.dispose();
                c.pushIcon(imag);
            }
        } finally {
            // just need to repaint c
            c.repaint();
        }
    }

    private void openHackGUI(JLevelComponent source) {
        JPanel layer = this.gui.pane.getLayer(Layer.PANEL.ordinal());
        if (layer.getComponents().length > 0) {
            return;
        }
        HackData data = source.getMetaData();
        JPanel panelLayer = new JPanel(new GridBagLayout());
        JPanel inbetween = new JPanel(new GridBagLayout());
        inbetween.add(panelLayer);
        layer.add(inbetween);
        inbetween.setOpaque(false);
        setupLayer(panelLayer, data);
        this.gui.pane.dispatchEvent(new ComponentEvent(this.gui.pane,
                ComponentEvent.COMPONENT_RESIZED));
        AnagarMainWindow.refreshAll();
    }

    private void setupLayer(JPanel panelLayer, final HackData data) {
        JLabel money = new JLabel("Money: $" + data.getMoneyProvided() + "/hr");
        JLabel power =
                new JLabel("Processing Power: " + data.getProcessingPower()
                        + " CPUs @ 3.40 GHz");
        JLabel stability =
                new JLabel("Stability: " + (100 - data.getStability())
                        + "% chance of being removed every 60 seconds");
        JButton hackButton =
                JComp.actionBound(new JButton("Hack it!"),
                                  new Consumer<JButton>() {

                                      @Override
                                      public void consume(JButton obj) {
                                          beginHack(data);
                                      }

                                  });
        JButton closeButton =
                JComp.actionBound(new JButton("Close"),
                                  new Consumer<JButton>() {

                                      @Override
                                      public void consume(JButton obj) {
                                          closeHackWindow();
                                      }

                                  });
        GridConstraints cons =
                new GridConstraints().setFill(GridBagConstraints.BOTH);
        cons.insets = new Insets(10, 10, 10, 10);
        panelLayer.setBorder(BorderFactory.createCompoundBorder(BorderFactory
                .createTitledBorder(data.getTitle()), BorderFactory
                .createEmptyBorder(5, 5, 5, 5)));
        panelLayer.add(money, cons.setCoords(0, 0).copy().setWidth(2));
        panelLayer.add(power, cons.setCoords(0, 1).copy().setWidth(2));
        panelLayer.add(stability, cons.setCoords(0, 2).copy().setWidth(2));
        panelLayer.add(hackButton, cons.setCoords(0, 3));
        panelLayer.add(closeButton, cons.setCoords(2, 3));
        panelLayer.setBackground(JComp.transparentify(Color.GREEN, 75));
    }

    private void beginHack(HackData data) {
        closeHackWindow();
        HackGUI gui =
                new HackGUI(data,
                        (LevelState) AnagarMainWindow.INSTANCE
                                .getCurrentState(), this.gui);
        AnagarMainWindow.INSTANCE.setCurrentStateGUI(gui);
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        openHackGUI((JLevelComponent) e.getComponent());
    }

    @Override
    public void mousePressed(MouseEvent e) {
        select(e.getPoint(),
               (JLevelComponent) e.getComponent(),
               SelectionState.SELECTED_ON);
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        select(e.getPoint(), (JLevelComponent) e.getComponent(), e
                .getComponent().contains(e.getPoint())
                                                      ? SelectionState.HOVER_ON
                                                      : SelectionState.OFF);
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        select(e.getPoint(),
               (JLevelComponent) e.getComponent(),
               SelectionState.HOVER_ON);
    }

    @Override
    public void mouseExited(MouseEvent e) {
        select(e.getPoint(),
               (JLevelComponent) e.getComponent(),
               SelectionState.OFF);
    }

    private void closeHackWindow() {
        OpenHackGUIListener.this.gui.pane.removeLayer(Layer.PANEL.ordinal());
        AnagarMainWindow.refreshAll();
    }

}
