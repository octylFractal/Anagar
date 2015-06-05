package me.kenzierocks.anagar;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

/**
 * Action that is bound to a key.
 * 
 * @author Kenzie Togami
 */
@SuppressWarnings("serial")
public class IDBasedAction extends AbstractAction {

    private final int keyCode;
    private final SimpleWin boundWIndow;

    /**
     * Creates a new KeyAction.
     * 
     * @param key
     *            - The key code
     * @param target
     *            - The window to bind to
     */
    public IDBasedAction(int key, SimpleWin target) {
        this.keyCode = key;
        this.boundWIndow = target;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        this.boundWIndow.onAction(this.keyCode);
    }

}
