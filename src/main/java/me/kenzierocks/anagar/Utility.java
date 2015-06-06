package me.kenzierocks.anagar;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Random;

import javax.swing.JButton;
import javax.swing.JPanel;

import me.kenzierocks.anagar.functional.Consumer;
import me.kenzierocks.anagar.state.State;

import com.google.auto.value.AutoValue;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

public final class Utility {

    public static final class Dim {

        public static Dimension requireSize(Dimension def, Dimension d) {
            return new Dimension(Math.max(def.width, d.width),
                    Math.max(def.height, d.height));
        }

    }

    public static final class GridConstraints
            extends GridBagConstraints {

        private static final long serialVersionUID = -6851309631667603288L;

        private static void copyImpl(GridBagConstraints from,
                GridBagConstraints to) {
            to.anchor = from.anchor;
            to.fill = from.fill;
            to.gridheight = from.gridheight;
            to.gridwidth = from.gridwidth;
            to.gridx = from.gridx;
            to.gridy = from.gridy;
            to.insets = from.insets;
            to.ipadx = from.ipadx;
            to.ipady = from.ipady;
            to.weightx = from.weightx;
            to.weighty = from.weighty;
        }

        public GridConstraints() {
        }

        public GridConstraints(GridBagConstraints gbc) {
            copyImpl(gbc, this);
        }

        /**
         * Sets the GridBagConstraints coords.
         * 
         * @param x
         *            - The x coord
         * @param y
         *            - The y coord
         */
        public GridConstraints setCoords(int x, int y) {
            this.gridx = x;
            this.gridy = y;
            return this;
        }

        /**
         * Sets the GridBagConstraints width.
         * 
         * @param width
         *            - The width
         */
        public GridConstraints setWidth(int width) {
            this.gridwidth = width;
            return this;
        }

        /**
         * Sets the GridBagConstraints height.
         * 
         * @param height
         *            - The height
         */
        public GridConstraints setHeight(int height) {
            this.gridheight = height;
            return this;
        }

        /**
         * Sets the GridBagConstraints fill.
         * 
         * @param fill
         *            - The fill
         */
        public GridConstraints setFill(int fill) {
            this.fill = fill;
            return this;
        }

        /**
         * Sets the GridBagConstraints anchor, and adjusts weights accordingly.
         * 
         * @param a
         *            - The anchor value
         * @see GridBagConstraints#anchor
         */
        public GridConstraints setAnchor(int a) {
            this.weightx = this.weighty = 1.0;
            this.anchor = a;
            return this;
        }

        public GridConstraints copy() {
            return new GridConstraints(this);
        }

        public void copyTo(GridBagConstraints awt) {
            copyImpl(this, awt);
        }

    }

    public static final class JComp {

        public static void printTree(Container c) {
            printTree(c, 0);
        }

        public static void printTree(Container c, int depth) {
            StringBuilder b = new StringBuilder(depth * 2 + 3);
            if (depth == 0) {
                b.append("==>");
                b.append(c);
                System.err.println(b);
            }
            for (Component x : c.getComponents()) {
                b.delete(0, b.length());
                for (int i = 0; i <= depth; i++) {
                    b.append("  ");
                }
                b.append("==>");
                b.append(x);
                System.err.println(b);
                if (x instanceof Container) {
                    printTree((Container) x, depth + 1);
                }
            }
        }

        public static void pack(Component c) {
            c.setSize(c.getPreferredSize());
        }

        public static JButton actionBound(final JButton button,
                final Consumer<JButton> onPress) {
            button.addActionListener(new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    onPress.consume(button);
                }
            });
            return button;
        }

        public static JPanel panelOf(final State.GUI gui) {
            JPanel guiPanel = null;
            if (gui instanceof JPanel) {
                guiPanel = (JPanel) gui;
            } else {
                guiPanel = new JPanel() {

                    private static final long serialVersionUID =
                            -6087196171804412001L;

                    @Override
                    public void paint(Graphics g) {
                        gui.paint(g);
                    }
                };
            }
            return guiPanel;
        }

        public static final Supplier<LayoutManager> BORDER_LAYOUT_SUPPLIER =
                new Supplier<LayoutManager>() {

                    @Override
                    public LayoutManager get() {
                        return new BorderLayout();
                    }

                };
        public static final Color TRANSPARENT_COLOR = new Color(0, 0, 0, 0);

        public static Color transparentify(Color base, int percent) {
            return new Color((base.getRGB() & (~(255 << 24)))
                    | ((((255 * percent) / 100) << 24)), true);
        }

        public static Component getComponentAtBottomOfTreeAt(Point loc) {
            Container target = AnagarMainWindow.INSTANCE.internalPanel;
            Component findComponentAt = target.findComponentAt(loc);
            return findComponentAt;
        }

    }

    public static final class ImageIO {

        public static enum Copy {
            FORCE, LAZY;
        }

        private static final Path base = Paths.get("src/main/resources");

        public static Image load(String name) {
            try {
                return javax.imageio.ImageIO.read(base.resolve(name + ".png")
                        .toFile());
            } catch (IOException e) {
                throw Throwables.propagate(e);
            }
        }

        public static BufferedImage
                asBufferedImage(Image src, Copy copySetting) {
            if (copySetting == Copy.LAZY && src instanceof BufferedImage) {
                return (BufferedImage) src;
            }
            BufferedImage target =
                    new BufferedImage(src.getWidth(null), src.getHeight(null),
                            BufferedImage.TYPE_INT_ARGB);
            Graphics2D g = target.createGraphics();
            g.drawImage(src, 0, 0, null);
            g.dispose();
            return target;
        }

    }

    public static final class Numbers {

        @AutoValue
        public static abstract class ExtendedRandom {

            public static final ExtendedRandom wrap(Random instance) {
                return new AutoValue_Utility_Numbers_ExtendedRandom(instance);
            }

            ExtendedRandom() {
            }

            public abstract Random getInstance();

            public <T> T getRandomItem(List<? extends T> coll) {
                return coll.get(getInstance().nextInt(coll.size()));
            }

            public <T> T getRandomItem(T[] arr) {
                return arr[getInstance().nextInt(arr.length)];
            }

            public int getRangedInt(int range, int offset) {
                return getInstance().nextInt(range) + offset;
            }

            /**
             * Returns {@code true} if the next random int from {@code 0->100}
             * is less than {@code percent}.
             * 
             * @param percent
             *            - The limit for returning true
             * @return {@code true} if the next random int from {@code 0->100}
             *         is less than {@code percent}.
             */
            public boolean randomPercent(int percent) {
                return getInstance().nextInt(101) < percent;
            }

        }

        public static String prettify(int n) {
            String nToStr = Integer.toString(n);
            StringBuilder inProgress = new StringBuilder(nToStr.length());
            int cnt = 0;
            // @formatter:off
            for (int i = nToStr.length(); i --> 0;) {
                // @formatter:on
                if (cnt % 3 == 0 && cnt != 0) {
                    inProgress.insert(0, ',');
                }
                inProgress.insert(0, nToStr.charAt(i));
                cnt++;
            }
            return inProgress.toString();
        }

    }

    public static final class RectangleRecycler {

        private static final Collection<Rectangle> storage = new HashSet<>();

        public static Rectangle fetch() {
            if (storage.isEmpty()) {
                return new Rectangle();
            }
            Rectangle first = storage.iterator().next();
            storage.remove(first);
            return first;
        }

        public static void recycle(Rectangle r) {
            storage.add(r);
        }

    }

    public static final class Strings {

        public static String multiplyStr(String str, int times) {
            StringBuilder b = new StringBuilder();
            // @formatter:off
            for (; times --> 0;) {
                // @formatter:on
                b.append(str);
            }
            return b.toString();
        }

    }

}
