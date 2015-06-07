package me.kenzierocks.anagar.state.level;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Preconditions.checkState;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JComponent;

import me.kenzierocks.anagar.Utility.JComp;

public class JLevelComponent
        extends JComponent implements LevelGUIComponent, LevelComponent {

    private static final long serialVersionUID = 8337487158796244819L;

    public static final String HACKED_KEY = "HACKED?!?!?! ZOMG";

    private static final Color ZOMG_HACKED_COLOR = JComp
            .transparentify(Color.CYAN, 20);

    private final Image originalIcon;
    private final HackData metaData;

    private Image icon;

    /**
     * Currently only for tracking purposes.
     */
    private Image old;

    public JLevelComponent(Image icon, HackData metaData) {
        this.icon = this.originalIcon = checkNotNull(icon);
        this.metaData = checkNotNull(metaData);
        setPreferredSize(new Dimension(icon.getWidth(null),
                icon.getHeight(null)));
        setSize(getPreferredSize());
        setHacked(false);
    }

    public boolean isHacked() {
        return Boolean.TRUE.equals(getClientProperty(HACKED_KEY));
    }

    public void setHacked(boolean state) {
        putClientProperty(HACKED_KEY, Boolean.valueOf(state));
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
        BufferedImage target =
                new BufferedImage(this.icon.getWidth(null),
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
        if (isHacked()) {
            BufferedImage imag = getIconAsBufferedImage();
            g.setColor(ZOMG_HACKED_COLOR);
            JComp.drawWhereItIs(imag, (Graphics2D) g);
        }
    }

    @Override
    public HackData getMetaData() {
        return this.metaData;
    }

}
