package me.kenzierocks.anagar.swing;

import java.awt.Component;
import java.awt.Container;
import java.awt.Point;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.util.HashSet;
import java.util.Set;

import javax.swing.SwingUtilities;

public class MotionTracker implements MouseMotionListener {

    private final Set<Component> interestingComponents = new HashSet<>();
    private Component lastEnteredComponent = null;

    @Override
    public void mouseDragged(MouseEvent e) {
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        Container c = (Container) e.getComponent();
        Point hit = e.getPoint();
        if (this.lastEnteredComponent != null) {
            Point adapted = SwingUtilities.convertPoint(c, e.getPoint(),
                    this.lastEnteredComponent);
            MouseEvent exitEvent = new MouseEvent(this.lastEnteredComponent,
                    MouseEvent.MOUSE_EXITED, e.getWhen(), e.getModifiers(),
                    adapted.x, adapted.y, e.getXOnScreen(), e.getYOnScreen(),
                    e.getClickCount(), e.isPopupTrigger(), e.getButton());
            this.lastEnteredComponent.dispatchEvent(exitEvent);
        }
        Component target = c;
        while (!this.interestingComponents.contains(target)) {
            if (target instanceof Container) {
                Component newTarget = ((Container) target).getComponentAt(hit);
                System.err.println(target + " -> " + newTarget);
                hit = SwingUtilities.convertPoint(target, hit, newTarget);
                target = newTarget; 
            } else {
                // no component to target
                return;
            }
        }
        this.lastEnteredComponent = target;
        target.dispatchEvent(SwingUtilities.convertMouseEvent(c, e, target));
    }

    public Component forComponent(Component c) {
        this.interestingComponents.add(c);
        return c;
    }

}
