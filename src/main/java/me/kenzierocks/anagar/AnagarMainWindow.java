package me.kenzierocks.anagar;

import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.AWTEventMulticaster;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JOptionPane;
import javax.swing.JPanel;

import me.kenzierocks.anagar.state.MainState;
import me.kenzierocks.anagar.state.State;
import me.kenzierocks.anagar.swing.MotionTracker;

import com.google.common.base.Throwables;

public class AnagarMainWindow extends SimpleWin {

    public static AnagarMainWindow INSTANCE;
    private static final long serialVersionUID = 6427675907542379554L;
    private static final Dimension DEFAULT_SIZE = new Dimension(900, 700);

    public static void refreshAll() {
        INSTANCE.validate();
        INSTANCE.repaint();
        INSTANCE.requestFocus();
        INSTANCE.requestFocusInWindow();
    }

    private final AtomicReference<KeyListener> currentKeyCapture = new AtomicReference<>();
    private final AtomicReference<MouseListener> currentMouseCapture = new AtomicReference<>();
    private final AtomicReference<State> currentState = new AtomicReference<>();
    private final MotionTracker internalMotionTracker = new MotionTracker();

    public AnagarMainWindow() {
        super(Constants.GAME_TITLE, new StateManagingPanel());
        INSTANCE = this;
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        this.internalPanel.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        addObjects();
        addBindings();
        setCurrentStateGUI(new MainState());
       // setupGlassPane();
        pack();
        Dimension packedSize = getSize();
        setSize(Utility.Dim.requireSize(DEFAULT_SIZE, packedSize));
        drop();
    }

    private void setupGlassPane() {
        Component c = getGlassPane();
        c.addKeyListener(new KeyListener() {

            @Override
            public void keyReleased(KeyEvent e) {
                KeyListener keyListener = AnagarMainWindow.this.currentKeyCapture
                        .get();
                if (keyListener != null) {
                    // consume it always?
                    keyListener.keyReleased(e);
                    e.consume();
                }
            }

            @Override
            public void keyTyped(KeyEvent e) {
                KeyListener keyListener = AnagarMainWindow.this.currentKeyCapture
                        .get();
                if (keyListener != null) {
                    // consume it always?
                    keyListener.keyTyped(e);
                    e.consume();
                }
            }

            @Override
            public void keyPressed(KeyEvent e) {
                KeyListener keyListener = AnagarMainWindow.this.currentKeyCapture
                        .get();
                if (keyListener != null) {
                    // consume it always?
                    keyListener.keyPressed(e);
                    e.consume();
                }
            }

        });
        c.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                MouseListener mouseListener = AnagarMainWindow.this.currentMouseCapture
                        .get();
                if (mouseListener != null) {
                    mouseListener.mouseReleased(e);
                    e.consume();
                }
            }

            @Override
            public void mousePressed(MouseEvent e) {
                MouseListener mouseListener = AnagarMainWindow.this.currentMouseCapture
                        .get();
                if (mouseListener != null) {
                    mouseListener.mousePressed(e);
                    e.consume();
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                MouseListener mouseListener = AnagarMainWindow.this.currentMouseCapture
                        .get();
                if (mouseListener != null) {
                    mouseListener.mouseExited(e);
                    e.consume();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
                MouseListener mouseListener = AnagarMainWindow.this.currentMouseCapture
                        .get();
                if (mouseListener != null) {
                    mouseListener.mouseEntered(e);
                    e.consume();
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                MouseListener mouseListener = AnagarMainWindow.this.currentMouseCapture
                        .get();
                if (mouseListener != null) {
                    mouseListener.mouseClicked(e);
                    e.consume();
                }
            }

        });
        c.addMouseMotionListener(this.internalMotionTracker);
    }

    @Override
    public void addObjects() {
        add(this.internalPanel, BorderLayout.CENTER);
    }

    @Override
    public void addBindings() {
        addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                getCurrentState().onKeyRelease(e);
            }

        });
        setAutoRequestFocus(true);
    }

    @Override
    public void onClose() {
        try {
            if (!getCurrentState().onClose()) {
                return;
            }
        } catch (Throwable t) {
            // we must dispose or the application never exits
            dispose();
            throw Throwables.propagate(t);
        }
        int res = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to quit " + Constants.GAME_TITLE + "?",
                "Quit", JOptionPane.YES_NO_OPTION);
        if (res == JOptionPane.NO_OPTION) {
            // do nothing
        } else {
            // dispose
            dispose();
        }
    }

    @Override
    public void actionPerformed(Object o) {

    }

    @Override
    public void onAction(int actionID) {
    }

    public void addTotalKeyController(KeyListener k) {
        // bah, this isn't totally concurrent
        this.currentKeyCapture.set(AWTEventMulticaster.add(
                this.currentKeyCapture.get(), k));
    }

    public void removeTotalKeyController(KeyListener k) {
        this.currentKeyCapture.set(AWTEventMulticaster.remove(
                this.currentKeyCapture.get(), k));
    }

    public void clearTotalKeyControllers() {
        this.currentKeyCapture.set(null);
    }

    public void addTotalMouseController(MouseListener k) {
        // bah, this isn't totally concurrent
        this.currentMouseCapture.set(AWTEventMulticaster.add(
                this.currentMouseCapture.get(), k));
    }

    public void removeTotalMouseController(MouseListener k) {
        this.currentMouseCapture.set(AWTEventMulticaster.remove(
                this.currentMouseCapture.get(), k));
    }

    public void clearTotalMouseControllers() {
        this.currentMouseCapture.set(null);
    }

    public <X extends KeyListener & MouseListener> void addTotalController(X k) {
        addTotalKeyController(k);
        addTotalMouseController(k);
    }

    public <X extends KeyListener & MouseListener> void removeTotalController(
            X k) {
        removeTotalKeyController(k);
        removeTotalMouseController(k);
    }

    public State getCurrentState() {
        return this.currentState.get();
    }

    public void setCurrentState(State state) {
        checkNotNull(state);
        State current = this.currentState.get();
        if (current != null) {
            current.onClose();
        }
        this.currentState.set(state);
    }

    public void setCurrentGUI(State.GUI gui) {
        JPanel guiPanel = Utility.JComp.panelOf(gui);
        this.internalPanel.removeAll();
        this.internalPanel.add(guiPanel, BorderLayout.CENTER);
        this.internalPanel.requestFocus();
        validate();
        repaint();
        requestFocus();
        requestFocusInWindow();
    }

    public <T extends State.GUI & State> void setCurrentStateGUI(T guiState) {
        checkNotNull(guiState);
        setCurrentState(guiState);
        setCurrentGUI(guiState);
    }

    public MotionTracker getMotionTracker() {
        return this.internalMotionTracker;
    }

}
