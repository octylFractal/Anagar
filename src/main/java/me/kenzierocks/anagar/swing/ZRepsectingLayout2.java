package me.kenzierocks.anagar.swing;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkNotNull;

import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.LayoutManager2;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.JLayeredPane;
import javax.swing.JPanel;

import com.google.auto.value.AutoValue;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;

public class ZRepsectingLayout2 implements LayoutManager2 {

    @AutoValue
    public static abstract class Constraint {

        public static Constraint merge(Object delegate, int level) {
            return new AutoValue_ZRepsectingLayout2_Constraint(delegate, level);
        }

        Constraint() {
        }

        public abstract Object getDelegate();

        public abstract int getLevel();

    }

    private final Function<Container, Dimension> maxLayoutSize;
    private final Function<Container, Dimension> preferredLayoutSize;
    private final Function<Container, Dimension> minLayoutSize;
    private final Function<Container, Object> invalidate;
    private final Function<Container, Object> layout;
    private final LayoutManager2 delegate;

    public ZRepsectingLayout2(LayoutManager2 delegate) {
        this.delegate = checkNotNull(delegate);
        this.maxLayoutSize = new Function<Container, Dimension>() {

            @Override
            public Dimension apply(Container input) {
                return ZRepsectingLayout2.this.delegate
                        .maximumLayoutSize(input);
            }

        };
        this.preferredLayoutSize = new Function<Container, Dimension>() {

            @Override
            public Dimension apply(Container input) {
                return ZRepsectingLayout2.this.delegate
                        .preferredLayoutSize(input);
            }

        };
        this.minLayoutSize = new Function<Container, Dimension>() {

            @Override
            public Dimension apply(Container input) {
                return ZRepsectingLayout2.this.delegate
                        .minimumLayoutSize(input);
            }

        };
        this.invalidate = new Function<Container, Object>() {

            @Override
            public Dimension apply(Container input) {
                ZRepsectingLayout2.this.delegate.invalidateLayout(input);
                return null;
            }

        };
        this.layout = new Function<Container, Object>() {

            @Override
            public Dimension apply(Container input) {
                ZRepsectingLayout2.this.delegate.layoutContainer(input);
                return null;
            }

        };
    }

    private <T> List<T> applyOverAll(Container target,
            Function<Container, T> transformation) {
        checkArgument(target instanceof JLayeredPane,
                "%s must be a JLayeredPane", target);
        List<T> list = new ArrayList<>();
        JLayeredPane pane = (JLayeredPane) target;
        Set<Integer> layers = new HashSet<>();
        for (Component c : target.getComponents()) {
            Integer layer = pane.getLayer(c);
            if (!layers.contains(layer)) {
                layers.add(layer);
            }
        }
        for (Integer layer : layers) {
            Component[] layerComps = pane
                    .getComponentsInLayer(layer.intValue());
            if (layerComps.length > 1) {
                // TODO handle non-one-component
                throw new UnsupportedOperationException(
                        "must use a single component per layer");
            } else {
                Component a = layerComps[0];
                Container c = new JPanel();
                c.add(a);
                c.setLayout(this.delegate);
                T res = transformation.apply(c);
                // print(c, 0);
                if (res != null) {
                    list.add(res);
                }
            }
        }
        return ImmutableList.copyOf(list);
    }

    @Override
    public void addLayoutComponent(Component comp, Object constraints) {
        if (constraints instanceof Constraint) {
            Constraint cons = (Constraint) constraints;
            ((JLayeredPane) comp.getParent()).setLayer(comp, cons.getLevel());
            constraints = cons.getDelegate();
        }
        this.delegate.addLayoutComponent(comp, constraints);
    }

    @Override
    public Dimension maximumLayoutSize(Container target) {
        List<Dimension> dimensions = applyOverAll(target, this.maxLayoutSize);
        Dimension max = new Dimension(0, 0);
        for (Dimension dimension : dimensions) {
            max.width = Math.max(max.width, dimension.width);
            max.height = Math.max(max.height, dimension.height);
        }
        return max;
    }

    @Override
    public float getLayoutAlignmentX(Container target) {
        return this.delegate.getLayoutAlignmentX(target);
    }

    @Override
    public float getLayoutAlignmentY(Container target) {
        return this.delegate.getLayoutAlignmentY(target);
    }

    @Override
    public void invalidateLayout(Container target) {
        applyOverAll(target, this.invalidate);
    }

    @Override
    public void addLayoutComponent(String name, Component comp) {
        this.delegate.addLayoutComponent(name, comp);
    }

    @Override
    public void removeLayoutComponent(Component comp) {
        this.delegate.removeLayoutComponent(comp);
    }

    @Override
    public Dimension preferredLayoutSize(Container target) {
        List<Dimension> dimensions = applyOverAll(target,
                this.preferredLayoutSize);
        Dimension max = new Dimension(0, 0);
        for (Dimension dimension : dimensions) {
            max.width = Math.max(max.width, dimension.width);
            max.height = Math.max(max.height, dimension.height);
        }
        return max;
    }

    @Override
    public Dimension minimumLayoutSize(Container target) {
        List<Dimension> dimensions = applyOverAll(target, this.minLayoutSize);
        Dimension min = new Dimension(0, 0);
        for (Dimension dimension : dimensions) {
            min.width = Math.min(min.width, dimension.width);
            min.height = Math.min(min.height, dimension.height);
        }
        return min;
    }

    @Override
    public void layoutContainer(Container target) {
        applyOverAll(target, this.layout);
    }

}
