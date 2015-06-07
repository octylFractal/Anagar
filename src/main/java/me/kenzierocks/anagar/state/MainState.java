package me.kenzierocks.anagar.state;

import static java.awt.event.KeyEvent.VK_ESCAPE;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.Constants;
import me.kenzierocks.anagar.Utility.GridConstraints;
import me.kenzierocks.anagar.Utility.JComp;
import me.kenzierocks.anagar.functional.Consumer;
import me.kenzierocks.anagar.state.level.LevelGUI;
import me.kenzierocks.anagar.state.level.LevelState;
import me.kenzierocks.anagar.swing.HintTextField;

public class MainState
        extends JPanelBasedGUI implements State {

    private static final long serialVersionUID = -417283140788552174L;

    public MainState() {
        setLayout(new GridBagLayout());
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout(10, 10));
        JLabel titleLabel = new JLabel("Anagar", JLabel.CENTER);
        titleLabel.setFont(Font.decode("Vani-bold-36"));
        panel.add(titleLabel, BorderLayout.CENTER);
        JPanel buttons = makeButtonsPanel();
        panel.add(buttons, BorderLayout.SOUTH);
        add(panel, new GridConstraints().setCoords(0, 0));
        // addFontSelector(titleLabel);
    }

    private JPanel makeButtonsPanel() {
        JPanel panel = new JPanel(new GridBagLayout());
        int col = 0;
        GridConstraints cons =
                new GridConstraints().setAnchor(GridBagConstraints.CENTER);
        panel.add(JComp.actionBound(new JButton("Start"),
                                    new Consumer<JButton>() {

                                        @Override
                                        public void consume(JButton obj) {
                                            changeToLevel(0);
                                        }

                                    }),
                  cons.copy().setFill(GridBagConstraints.HORIZONTAL)
                          .setWidth(GridBagConstraints.REMAINDER)
                          .setCoords(0, col++));

        panel.add(Box.createVerticalStrut(10), cons.copy().setWidth(3)
                .setCoords(0, col++));

        final JTextField text = new HintTextField("level", 3);
        final Consumer<JButton> startLevelAction = new Consumer<JButton>() {

            @Override
            public void consume(JButton obj) {
                try {
                    changeToLevel(Integer.parseInt(text.getText().replace(",",
                                                                          "")));
                } catch (NumberFormatException notANumber) {
                    // ignore
                }
            }

        };
        text.setHorizontalAlignment(JTextField.CENTER);
        text.addKeyListener(new KeyAdapter() {

            @Override
            public void keyReleased(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    // bam
                    startLevelAction.consume(null);
                }
            }

        });
        panel.add(text, cons.copy().setFill(GridBagConstraints.HORIZONTAL)
                .setWidth(GridBagConstraints.RELATIVE).setCoords(0, col));
        panel.add(JComp.actionBound(new JButton("Start Level"),
                                    startLevelAction),
                  cons.copy().setFill(GridBagConstraints.HORIZONTAL)
                          .setWidth(GridBagConstraints.REMAINDER)
                          .setCoords(2, col));
        col++;

        panel.add(Box.createVerticalStrut(10), cons.setCoords(1, col++));

        panel.add(JComp.actionBound(new JButton("Exit"),
                                    new Consumer<JButton>() {

                                        @Override
                                        public void consume(JButton obj) {
                                            AnagarMainWindow.INSTANCE.close();
                                        }

                                    }),
                  cons.copy().setFill(GridBagConstraints.HORIZONTAL)
                          .setWidth(GridBagConstraints.REMAINDER)
                          .setCoords(0, col++));
        return panel;
    }

    public void addFontSelector(final JLabel titleLabel) {
        JButton selectFont = new JButton("Font?");
        selectFont.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                String font =
                        (String) JOptionPane
                                .showInputDialog(AnagarMainWindow.INSTANCE,
                                                 "Choose a font",
                                                 "Font Chooser",
                                                 JOptionPane.QUESTION_MESSAGE,
                                                 null,
                                                 GraphicsEnvironment
                                                         .getLocalGraphicsEnvironment()
                                                         .getAvailableFontFamilyNames(),
                                                 null);
                if (font != null) {
                    titleLabel.setFont(Font.decode(font + "-bold-36"));
                }
            }

        });
        add(selectFont, BorderLayout.SOUTH);
    }

    private void changeToLevel(int n) {
        LevelState state = new LevelState(n);
        AnagarMainWindow.INSTANCE.setCurrentState(state);
        LevelGUI gui = new LevelGUI(n);
        state.setCurrentGUI(gui);
        AnagarMainWindow.INSTANCE.setCurrentGUI(gui);
    }

    @Override
    public String getTitle() {
        return Constants.GAME_TITLE;
    }

    @Override
    public StateType getType() {
        return StateType.Defaults.MAIN.getAsIs();
    }

    @Override
    public void onKeyRelease(KeyEvent event) {
        if (event.getKeyCode() == VK_ESCAPE) {
            AnagarMainWindow.INSTANCE.close();
        }
    }

    @Override
    public boolean onClose() {
        return true;
    }

}
