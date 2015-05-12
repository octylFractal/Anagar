package me.kenzierocks.anagar;

import java.awt.Dimension;
import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.Document;

/**
 * A simple window base for Swing.
 * 
 * @author Kenzie Togami
 */
@SuppressWarnings("serial")
public abstract class SimpleWin
        extends JFrame implements ActionListener, DocumentListener {
    protected final List<JButton> buttonList = new ArrayList<JButton>();
    protected final List<JTextField> textFieldList =
            new ArrayList<JTextField>();
    protected final List<JLabel> labelList = new ArrayList<JLabel>();
    protected final Map<Integer, Action> actionMap =
            new HashMap<Integer, Action>();
    /**
     * The GridBagConstraints used by the window.
     */
    public final GridBagConstraints gbc;
    protected final JPanel internalPanel;

    /**
     * This constructor must only be called by a subclass, never call this any
     * other way.
     * 
     * <p>
     * The title is the title of the window, the JPanel is the JPanel you wish
     * to work with. Note that the JPanel will have its layout set by default to
     * a GridBag but you may change it after this constructor is called. You
     * also need to set the size and position the window manually, but you may
     * use the drop() and pack() methods to help with that.
     * </p>
     * 
     * @param title
     *            - The title of the window.
     * @param jp
     *            - The working JPanel instance
     * 
     * @see #drop()
     * @see #pack()
     */
    public SimpleWin(String title, JPanel jp) {
        super(title);
        jp.setLayout(new GridBagLayout());
        this.internalPanel = jp;
        this.gbc = new GridBagConstraints();
        this.setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        addWindowListener(new WindowCloseHandler());
    }

    /**
     * Add objects to the JPanel.
     * 
     * <p>
     * Please note this method is not called automatically!
     * </p>
     */
    public abstract void addObjects();

    /**
     * Add bindings to the JPanel.
     * 
     * <p>
     * Please note this method is not called automatically!
     * </p>
     */
    public abstract void addBindings();

    /**
     * Add an action to the action map.
     * 
     * @param actionID
     *            - The ID to map the action to
     * @return The generated action
     */
    public Action addAction(int actionID) {
        Integer idBoxed = actionID;
        if (!this.actionMap.containsKey(idBoxed)) {
            this.actionMap.put(idBoxed, new IDBasedAction(actionID, this));
        }
        return this.actionMap.get(idBoxed);
    }

    /**
     * Add a binding to this window's JPanel.
     * 
     * @param keystroke
     *            - The string of key strokes to bind to, see
     *            {@link KeyStroke#getKeyStroke(String)} for details
     * @param actionID
     *            - The ID for the underlying action
     * @return The bound action
     */
    public Action addBind(String keystroke, int actionID) {
        return addBind(this.internalPanel, keystroke, actionID);
    }

    /**
     * Add a binding to a specific component.
     * 
     * @param c
     *            - The component to add the binding to
     * @param keystroke
     *            - The string of key strokes to bind to, see
     *            {@link KeyStroke#getKeyStroke(String)} for details
     * @param actionID
     *            - The ID for the underlying action
     * @return The bound action
     */
    public Action addBind(JComponent c, String keystroke, int actionID) {
        c.getInputMap().put(KeyStroke.getKeyStroke(keystroke), "" + actionID);
        c.getActionMap().put("" + actionID, addAction(actionID));
        return this.actionMap.get(actionID);
    }

    /**
     * Used for cleanup. You should use this to close all of your resources and
     * do a save-data-on-close in this method.
     */
    public abstract void onClose();

    /**
     * Trigger for event that resolves the correct Object from the event.
     * 
     * @param o
     *            - The source object of the triggering event
     */
    public abstract void actionPerformed(Object o);

    @Override
    public void actionPerformed(ActionEvent e) {
        actionPerformed(e.getSource());
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        actionPerformed(e.getDocument());
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        actionPerformed(e.getDocument());
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        actionPerformed(e.getDocument());
    }

    /**
     * Closes this window.
     */
    public void close() {
        WindowEvent wev = new WindowEvent(this, WindowEvent.WINDOW_CLOSING);
        Toolkit.getDefaultToolkit().getSystemEventQueue().postEvent(wev);
    }

    /**
     * Clears all lists and removes all components from the JPanel.
     */
    public void clear() {
        this.buttonList.clear();
        this.labelList.clear();
        this.textFieldList.clear();
        this.internalPanel.removeAll();
    }

    /**
     * Active full screen mode for this window.
     */
    public void activateFullScreen() {
        GraphicsDevice de =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice();
        if (isDisplayable()) {
            dispose();
        }
        setUndecorated(true);
        de.setFullScreenWindow(this);
    }

    /**
     * Deactivate full screen mode for this window.
     */
    public void deactivateFullScreen() {
        GraphicsDevice de =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice();
        de.setFullScreenWindow(null);
        dispose();
        setUndecorated(false);
    }

    /**
     * Set this window's size so it fills the screen.
     */
    public void maximizeSCreen() {
        DisplayMode displayMode =
                GraphicsEnvironment.getLocalGraphicsEnvironment()
                        .getDefaultScreenDevice().getDisplayMode();
        setSize(displayMode.getWidth(), displayMode.getHeight());
        setVisible(true);
    }

    /**
     * Adds a button to the internal panel using {@link #gbc} and also adds it
     * to the button list and adds this as the action listener.
     * 
     * @param button
     *            - The button to add
     * @return The index of the button in the list
     */
    public int addButton(JButton button) {
        this.internalPanel.add(button, this.gbc);
        this.buttonList.add(button);
        button.addActionListener(this);
        return this.buttonList.lastIndexOf(button);
    }

    /**
     * Adds a text field to the internal panel using {@link #gbc} and also adds
     * it to the text field list and adds this as the action listener to the
     * field and its document.
     * 
     * @param textField
     *            - The text field to add
     * @return The index of the text field in the list
     */
    public int addTextField(JTextField textField) {
        this.internalPanel.add(textField, this.gbc);
        this.textFieldList.add(textField);
        textField.addActionListener(this);
        textField.getDocument().addDocumentListener(this);
        return this.textFieldList.lastIndexOf(textField);
    }

    /**
     * Adds a label to the internal panel using {@link #gbc} and also adds it to
     * the label list,
     * 
     * @param label
     *            - The label to add
     * @return The index of the label in the list
     */
    public int addLabel(JLabel label) {
        this.internalPanel.add(label, this.gbc);
        this.labelList.add(label);
        return this.labelList.lastIndexOf(label);
    }

    /**
     * Adds a horizontal glue to the internal panel using {@link #gbc}.
     */
    public void addHorizonatalGlue() {
        this.internalPanel.add(Box.createHorizontalGlue(), this.gbc);
    }

    /**
     * Adds a vertical glue to the internal panel using {@link #gbc}.
     */
    public void addVerticalGlue() {
        this.internalPanel.add(Box.createVerticalGlue(), this.gbc);
    }

    /**
     * Adds a rigid area to the internal panel using {@link #gbc}.
     * 
     * @param width
     *            - The width of the rigid area
     * @param height
     *            - The height of the rigid area
     */
    public void addRigidArea(int width, int height) {
        this.internalPanel.add(Box
                .createRigidArea(new Dimension(width, height)), this.gbc);
    }

    /**
     * Matches a JTextField to a Document.
     * 
     * @param d
     *            - the document to match a JTextField to
     * @return the matched text field, or null if none was found
     * @see javax.swing.JTextField
     * @see javax.swing.text.Document
     */
    public JTextField match(Document d) {
        for (JTextField f : this.textFieldList) {
            if (f.getDocument() == d)
                return f;
        }
        return null;
    }

    /**
     * Positions the window in the middle of the screen
     */
    public void drop() {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        this.setLocation((screen.width / 2) - this.getWidth() / 2,
                         (screen.height / 2) - this.getHeight() / 2);
    }

    /**
     * Sets the coords for the next object.
     * 
     * @param x
     *            - The x coord
     * @param y
     *            - The y coord
     */
    public void setCoords(int x, int y) {
        this.gbc.gridx = x;
        this.gbc.gridy = y;
    }

    /**
     * Sets the GridBagConstraints anchor, and adjusts weights accordingly.
     * 
     * @param a
     *            - The anchor value
     * @see GridBagConstraints#anchor
     */
    public void setAnchor(int a) {
        this.gbc.weightx = this.gbc.weighty = 1.0;
        this.gbc.anchor = a;
    }

    /**
     * Called upon action.
     * 
     * @param actionID
     *            - The action ID that was fired
     */
    public abstract void onAction(int actionID);
}
