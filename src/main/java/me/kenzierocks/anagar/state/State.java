package me.kenzierocks.anagar.state;

import java.awt.Graphics;
import java.awt.Image;
import java.awt.event.KeyEvent;

/**
 * A State class provides information about itself, and may contain more
 * information based on the type.
 */
public interface State {

    StateType getType();

    void onKeyRelease(KeyEvent event);

    boolean onClose();

    interface GUI {

        String getTitle();

        Image captureScreen();

        void paint(Graphics graphics);

    }

}
