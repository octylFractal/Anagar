package me.kenzierocks.anagar.swing;

import java.awt.Color;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import javax.swing.JTextField;

public class HintTextField extends JTextField implements FocusListener {

    private static final long serialVersionUID = -1121314197826942251L;

    private final String hint;

    public Color regularColor = Color.BLACK;
    public Color hintColor = Color.GRAY;
    private boolean hintIsVisible = false;

    public HintTextField(String hint, int cols) {
        super(cols);
        this.hint = hint;
        setHintVisible(true);
        addFocusListener(this);
    }

    private void setHintVisible(boolean b) {
        if (b ^ this.hintIsVisible) { // different values -> change
            if (b) {
                setText(this.hint);
                setForeground(this.hintColor);
            } else {
                setText("");
                setForeground(this.regularColor);
            }
            this.hintIsVisible = b;
        }
    }

    @Override
    public void focusGained(FocusEvent e) {
        setHintVisible(false);
    }

    @Override
    public void focusLost(FocusEvent e) {
        setHintVisible(getText().isEmpty());
    }

}