package me.kenzierocks.anagar.state.level;

import java.awt.event.KeyEvent;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.state.PauseScreen;
import me.kenzierocks.anagar.state.State;
import me.kenzierocks.anagar.state.StateType;

public class LevelState implements State {

    private final int levelNum;
    private final StateType type;
    private LevelGUI currentGUI;

    public LevelState(int levelNum) {
        this.levelNum = levelNum;
        this.type =
                StateType.Defaults.LEVEL.createStateType(Integer
                        .toString(levelNum, 36));
    }

    public int getLevelNum() {
        return this.levelNum;
    }

    public LevelGUI getCurrentGUI() {
        return this.currentGUI;
    }

    public void setCurrentGUI(LevelGUI currentGUI) {
        this.currentGUI = currentGUI;
    }

    @Override
    public StateType getType() {
        return this.type;
    }

    @Override
    public void onKeyRelease(KeyEvent event) {
        if (event.getKeyCode() == KeyEvent.VK_ESCAPE) {
            AnagarMainWindow.INSTANCE.setCurrentStateGUI(new PauseScreen(this,
                    this.currentGUI));
        }
    }

    @Override
    public boolean onClose() {
        return true;
    }

}
