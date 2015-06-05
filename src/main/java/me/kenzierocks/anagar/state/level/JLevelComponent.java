package me.kenzierocks.anagar.state.level;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

public class JLevelComponent extends JComponent implements LevelGUIComponent,
        LevelComponent {

    private static final long serialVersionUID = 8337487158796244819L;

    private final Image originalIcon;
    private final HackGUIMetaData metaData;

    private Image icon;

    /**
     * Currently only for tracking purposes.
     */
    private Image old;

    public JLevelComponent(Image icon, HackGUIMetaData metaData) {
        this.icon = this.originalIcon = checkNotNull(icon);
        this.metaData = checkNotNull(metaData);
        setPreferredSize(new Dimension(icon.getWidth(null),
                icon.getHeight(null)));
        setSize(getPreferredSize());
    }

    public boolean isPushed() {
        return this.old != null;
    }

    public void pushIcon(Image i) {
        checkState(!isPushed(), "Cannot override old push");
        checkNotNull(i);
        this.old = this.icon;
        this.icon = i;
    }

    public void popIcon() {
        checkState(isPushed(), "Cannot pop nothing");
        this.icon = this.originalIcon;
        this.old = null;
    }

    public Image getIcon() {
        return this.originalIcon;
    }

    public BufferedImage getIconAsBufferedImage() {
        return getIconAsBufferedImage(false);
    }

    public BufferedImage getIconAsBufferedImage(boolean forceCopy) {
        if (!forceCopy && this.icon instanceof BufferedImage) {
            return (BufferedImage) this.icon;
        }
        BufferedImage target = new BufferedImage(this.icon.getWidth(null),
                this.icon.getHeight(null), BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = target.createGraphics();
        g.drawImage(this.icon, 0, 0, null);
        g.dispose();
        return target;
    }

    @Override
    public LevelGUIComponent getDisplayableBit() {
        return this;
    }

    @Override
    public void paint(Graphics g) {
        g.drawImage(this.icon, 0, 0, null);
    }

    @Override
    public HackGUIMetaData getMetaData() {
        return this.metaData;
    }

}
