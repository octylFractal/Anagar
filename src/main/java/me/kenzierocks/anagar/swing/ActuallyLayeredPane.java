package me.kenzierocks.anagar.swing;

import static com.google.common.base.Preconditions.checkArgument;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.LayoutManager;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseListener;

import javax.swing.JComponent;
import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import me.kenzierocks.anagar.AnagarMainWindow;

import com.google.common.base.Supplier;

public class ActuallyLayeredPane
        extends JComponent {

    private static final long serialVersionUID = -764793746028936642L;

    private static final Color TRANSPARENCY = new Color(0, 0, 0, 0);

    private static final MouseListener NULL_MOUSE_LISTENER =
            new MouseAdapter() {
            };

    private final JLayeredPane internalPane = new JLayeredPane();
    private final Supplier<LayoutManager> managerSupplier;

    public ActuallyLayeredPane(Supplier<LayoutManager> managerSupplier) {
        this.managerSupplier = managerSupplier;
        setLayout(new BorderLayout());
        setOpaque(true);
        this.internalPane.setOpaque(true);
        this.internalPane.setBackground(Color.BLACK);
        this.internalPane.setForeground(Color.BLACK);
        super.addImpl(this.internalPane, null, -1);
        addComponentListener(new ComponentAdapter() {

            @Override
            public void componentResized(ComponentEvent e) {
                JLayeredPane internalPane2 =
                        ActuallyLayeredPane.this.internalPane;
                internalPane2.setPreferredSize(getSize());
                internalPane2.setSize(internalPane2.getPreferredSize());
                for (Component c : internalPane2.getComponents()) {
                    c.setPreferredSize(internalPane2.getSize());
                    c.setSize(c.getPreferredSize());
                }
                AnagarMainWindow.refreshAll();
            }

        });
    }

    @Override
    public Component add(Component comp) {
        throw new UnsupportedOperationException(
                "There is no default layer option.");
    }

    @Override
    protected void addImpl(Component comp, Object constraints, int index) {
        checkArgument(index >= 0, "no layer specified");
        getLayer(index).add(comp, constraints);
    }

    public JPanel getLayer(int layer) {
        ensureLayerExists(layer);
        return (JPanel) this.internalPane.getComponentsInLayer(layer)[0];
    }

    public boolean isLayerThere(int layer) {
        return this.internalPane.getComponentsInLayer(layer).length > 0;
    }

    public void removeLayer(int layer) {
        Integer boxedLayer = layer;
        for (Component c : this.internalPane.getComponents()) {
            if (((JPanel) c).getClientProperty(getClass()).equals(boxedLayer)) {
                this.internalPane.remove(c);
                return;
            }
        }
    }

    private void ensureLayerExists(int layer) {
        if (this.internalPane.getComponentsInLayer(layer).length == 0) {
            JPanel jpanel = new JPanel(this.managerSupplier.get());
            jpanel.putClientProperty(getClass(), layer);
            jpanel.setBackground(TRANSPARENCY);
            jpanel.setOpaque(false);
            jpanel.addMouseListener(NULL_MOUSE_LISTENER);
            this.internalPane.add(jpanel, new Integer(layer));
        }
    }

}