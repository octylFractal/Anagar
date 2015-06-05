package me.kenzierocks.anagar.state;

import java.awt.Image;
import java.awt.image.BufferedImage;

import javax.swing.JPanel;

public abstract class JPanelBasedGUI extends JPanel implements State.GUI {

    private static final long serialVersionUID = 9069870546344583492L;

    @Override
    public Image captureScreen() {
        BufferedImage image = new BufferedImage(this.getWidth(),
                this.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
        this.print(image.getGraphics());
        return image;
    }

}
