package me.kenzierocks.anagar;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * This closes a SimpleWin when attached.
 * 
 * @author Kenzie Togami
 */
class WindowCloseHandler extends WindowAdapter {

    @Override
    public void windowClosing(WindowEvent e) {
        ((SimpleWin) e.getWindow()).onClose();
    }

}
