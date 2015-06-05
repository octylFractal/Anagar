package me.kenzierocks.anagar.state;

import static com.google.common.base.Preconditions.checkState;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.Utility;
import me.kenzierocks.anagar.Utility.GridConstraints;
import me.kenzierocks.anagar.Utility.JComp;
import me.kenzierocks.anagar.functional.Consumer;
import me.kenzierocks.anagar.swing.ActuallyLayeredPane;

public class PauseScreen extends JPanelBasedGUI implements State {

    private static final long serialVersionUID = -2367798684546188656L;
    public static final Color NICE_SHADE_OF_GRAY = JComp.transparentify(
            Color.LIGHT_GRAY, 55);
    private static final Integer YES_OPTION_INTEGER = Integer
            .valueOf(JOptionPane.YES_OPTION);

    private static final <T extends Component> T autoSize(
            final Container parent, final T child) {
        child.setSize(parent.getSize());
        final AtomicReference<Dimension> oldParentSize = new AtomicReference<>(
                new Dimension(-1, -1));
        ComponentAdapter resizeListener = new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                if (oldParentSize.compareAndSet(oldParentSize.get(),
                        parent.getSize())) {
                    return;
                }
                checkState(e.getComponent() == parent);
                checkState(parent.getComponents().length == 1);
                child.setPreferredSize(e.getComponent().getSize());
                child.setSize(child.getPreferredSize());
                parent.doLayout();
                Utility.JComp.pack(parent);
                AnagarMainWindow.refreshAll();
            }

        };
        resizeListener.componentResized(new ComponentEvent(parent, 0));
        parent.addComponentListener(resizeListener);
        return child;
    }

    private final State original;
    private final State.GUI originalGUI;
    private final StateType type;

    public <T extends State & State.GUI> PauseScreen(T returnTo) {
        this(returnTo, returnTo);
    }

    public PauseScreen(State returnTo, State.GUI returnToGUI) {
        this.original = returnTo;
        this.originalGUI = returnToGUI;
        ActuallyLayeredPane pane = new ActuallyLayeredPane(
                JComp.BORDER_LAYOUT_SUPPLIER);
        pane.setOpaque(false);
        JPanel background = Utility.JComp.panelOf(this.originalGUI);
        pane.add(background, null, 0);
        JPanel shading = autoSize(pane, new JPanel() {

            private static final long serialVersionUID = 2381965887758222899L;

            {
                setForeground(JComp.TRANSPARENT_COLOR);
                setBackground(NICE_SHADE_OF_GRAY);
            }

            @Override
            public void paint(Graphics g) {
                g.setColor(getBackground());
                g.fillRect(0, 0, getWidth(), getHeight());
            }

        });
        shading.setOpaque(false);
        pane.add(shading, null, 1);

        JPanel actualPanel = createPanel();
        actualPanel.setForeground(JComp.TRANSPARENT_COLOR);
        actualPanel.setBackground(JComp.TRANSPARENT_COLOR);
        actualPanel.setOpaque(false);
        pane.add(actualPanel, null, 2);

        setLayout(new BorderLayout());
        add(pane, BorderLayout.CENTER);
        this.type = StateType.Defaults.PAUSE.createStateType(this.original
                .getType().toString());
    }

    private JPanel createPanel() {
        GridConstraints cons = new GridConstraints();
        JPanel panel = new JPanel(new GridBagLayout());
        panel.add(JComp.actionBound(new JButton("Return To Game"),
                new Consumer<JButton>() {

                    @Override
                    public void consume(JButton obj) {
                        unpause();
                    }

                }), cons.copy().setWidth(3).setCoords(0, 0));
        panel.add(Box.createVerticalStrut(10), cons.copy().setWidth(3)
                .setCoords(0, 1));
        panel.add(
                JComp.actionBound(new JButton("Exit"), new Consumer<JButton>() {

                    @Override
                    public void consume(JButton obj) {
                        returnToMenu();
                    }

                }), cons.copy().setWidth(3).setCoords(0, 2));
        panel.add(Box.createVerticalStrut(10), cons.copy().setWidth(3)
                .setCoords(0, 3));
        panel.add(JComp.actionBound(new JButton("Crash To Desktop"),
                new Consumer<JButton>() {

                    @Override
                    public void consume(JButton obj) {
                        crashToDesktop();
                    }

                }), cons.copy().setWidth(3).setCoords(0, 5));
        return panel;
    }

    @Override
    public String getTitle() {
        return "Paused: " + this.originalGUI.getTitle();
    }

    @Override
    public StateType getType() {
        return this.type;
    }

    @Override
    public void onKeyRelease(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            unpause();
        }
    }

    private void unpause() {
        AnagarMainWindow.INSTANCE.setCurrentState(this.original);
        AnagarMainWindow.INSTANCE.setCurrentGUI(this.originalGUI);
    }

    private void returnToMenu() {
        AnagarMainWindow.INSTANCE.setCurrentStateGUI(new MainState());
    }

    private void crashToDesktop() {
        Thread t = new Thread() {

            @Override
            public void run() {
                final AtomicBoolean yesContinue = new AtomicBoolean(true);
                final Random r = new Random();
                DisplayMode m = GraphicsEnvironment
                        .getLocalGraphicsEnvironment().getDefaultScreenDevice()
                        .getDisplayMode();
                final int w = m.getWidth();
                final int h = m.getHeight();
                final List<JFrame> createdFrames = new ArrayList<>();
                while (yesContinue.get()) {
                    try {
                        Thread.sleep(1);
                    } catch (InterruptedException ignored) {
                    }
                    try {
                        SwingUtilities.invokeAndWait(new Runnable() {

                            @Override
                            public void run() {
                                final JOptionPane pane = new JOptionPane(
                                        "Crash to desktop?",
                                        JOptionPane.QUESTION_MESSAGE,
                                        JOptionPane.YES_NO_OPTION);
                                final JFrame frame = new JFrame();
                                createdFrames.add(frame);
                                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                frame.add(pane);
                                frame.pack();
                                int x = r.nextInt(w);
                                int y = r.nextInt(h);
                                frame.setLocation(x, y);

                                final PropertyChangeListener listener = new PropertyChangeListener() {

                                    private Object YES_OPTION = YES_OPTION_INTEGER;

                                    @Override
                                    public void propertyChange(
                                            PropertyChangeEvent event) {
                                        // Let the defaultCloseOperation handle
                                        // the closing
                                        // if the user closed the window without
                                        // selecting a button
                                        // (newValue = null in that case).
                                        // Otherwise, close the dialog.
                                        if (frame.isVisible()
                                                && event.getSource() == pane
                                                && (event.getPropertyName()
                                                        .equals(JOptionPane.VALUE_PROPERTY))
                                                && event.getNewValue() != null
                                                && event.getNewValue() != JOptionPane.UNINITIALIZED_VALUE) {
                                            frame.setVisible(false);
                                        }
                                        // in all cases, do check
                                        yesContinue.set(pane.getValue() == this.YES_OPTION);
                                    }
                                };

                                WindowAdapter adapter = new WindowAdapter() {

                                    private boolean gotFocus = false;

                                    @Override
                                    public void windowClosing(WindowEvent we) {
                                        pane.setValue(null);
                                    }

                                    @Override
                                    public void windowClosed(WindowEvent e) {
                                        removePropertyChangeListener(listener);
                                        frame.getContentPane().removeAll();
                                    }

                                    @Override
                                    public void windowGainedFocus(WindowEvent we) {
                                        // Once window gets focus, set initial
                                        // focus
                                        if (!this.gotFocus) {
                                            pane.selectInitialValue();
                                            this.gotFocus = true;
                                        }
                                    }
                                };
                                frame.addWindowListener(adapter);
                                frame.addWindowFocusListener(adapter);
                                frame.addComponentListener(new ComponentAdapter() {

                                    @Override
                                    public void componentShown(ComponentEvent ce) {
                                        // reset value to ensure closing works
                                        // properly
                                        pane.setValue(JOptionPane.UNINITIALIZED_VALUE);
                                    }
                                });

                                pane.addPropertyChangeListener(listener);
                                frame.setVisible(true);
                                frame.addWindowListener(new WindowAdapter() {

                                    @Override
                                    public void windowClosed(WindowEvent e) {
                                        super.windowClosed(e);
                                    }

                                });

                            }

                        });
                    } catch (InvocationTargetException e) {
                    } catch (InterruptedException e) {
                    }
                }
                AnagarMainWindow.INSTANCE.close();
                for (JFrame x : createdFrames) {
                    x.setVisible(false);
                    x.dispose();
                }
            }

        };
        t.start();
    }

    @Override
    public boolean onClose() {
        return true;
    }

}
