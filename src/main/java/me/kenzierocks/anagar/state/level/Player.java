package me.kenzierocks.anagar.state.level;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import javax.swing.Timer;

import me.kenzierocks.anagar.AnagarMainWindow;
import me.kenzierocks.anagar.Utility.Numbers.ExtendedRandom;

public final class Player implements ActionListener {

    private static final Timer moneyGiver = new Timer(1000, null);
    private static final List<Player> activePlayers = Collections
            .synchronizedList(new ArrayList<Player>());
    private static final ExtendedRandom RANDOM = ExtendedRandom
            .wrap(new Random());
    static {
        moneyGiver.start();
    }
    public static final Player THE_PLAYER = new Player();
    private final List<HackData> capturedData = new ArrayList<>();
    private int recaptureCounter = 0;
    private int moneyPerSecond;
    private int processingPower;
    private int money;

    public Player() {
        activePlayers.add(this);
        moneyGiver.addActionListener(this);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        increaseMoney();
        if (this.recaptureCounter % 60 == 0) {
            attemptRecapture();
        }
        this.recaptureCounter++;
    }

    private void attemptRecapture() {
        if (this.capturedData.isEmpty()) {
            return;
        }
        HackData recapture = RANDOM.getRandomItem(this.capturedData);
        if (!RANDOM.randomPercent(recapture.getStability())) {
            Component c =
                    AnagarMainWindow.INSTANCE.internalPanel.getComponent(0);
            if (c instanceof LevelGUI) {
                ((LevelGUI) c).getComponentWithData(recapture).setHacked(false);
            }
            releaseData(recapture);
        }
    }

    public void increaseMoney() {
        this.money += this.moneyPerSecond;
        Component gui = AnagarMainWindow.INSTANCE.internalPanel.getComponent(0);
        if (gui instanceof LevelGUI) {
            ((LevelGUI) gui).updatePlayerTracker();
        }
    }

    public int getMoneyPerSecond() {
        return this.moneyPerSecond;
    }

    public int getMoney() {
        return this.money;
    }

    public int getProcessingPower() {
        return this.processingPower;
    }

    public void setMoney(int money) {
        this.money = money;
    }

    public void setMoneyPerSecond(int moneyPerSecond) {
        this.moneyPerSecond = moneyPerSecond;
    }

    public void setProcessingPower(int processingPower) {
        this.processingPower = processingPower;
    }

    public void increaseMoneyPerSecond(int inc) {
        this.moneyPerSecond += inc;
    }

    public void increaseProcessingPower(int inc) {
        this.processingPower += inc;
    }

    public void captureData(HackData data) {
        increaseMoneyPerSecond(data.getMoneyProvided());
        increaseProcessingPower(data.getProcessingPower());
        this.capturedData.add(data);
        Component c = AnagarMainWindow.INSTANCE.internalPanel.getComponent(0);
        if (c instanceof LevelGUI) {
            ((LevelGUI) c).getComponentWithData(data).setHacked(true);
        }
    }

    public void releaseData(HackData data) {
        if (!this.capturedData.remove(data)) {
            return;
        }
        increaseMoneyPerSecond(-data.getMoneyProvided());
        increaseProcessingPower(-data.getProcessingPower());
    }
    
    public void reset() {
        this.capturedData.clear();
        this.recaptureCounter = 0;
        this.moneyPerSecond = 0;
        this.processingPower = 0;
        this.money = 0;
    }

}
