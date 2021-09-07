package it.unipi.iot.irrigationsystem.automation.utils;

public enum Levels {
    NOT_NEEDED(0), LOW(4), MEDIUM(8), HIGH(12), VERY_HIGH(14);
    private double action;

    public double getAction() {
        return this.action;
    }

    public void increaseAction(){
        if (action == Levels.NOT_NEEDED.getAction()) {
            action = LOW.getAction();
        } else if (action == Levels.LOW.getAction()) {
            action = MEDIUM.getAction();
        } else if (action == Levels.MEDIUM.getAction()) {
            action = HIGH.getAction();
        } else
            action = VERY_HIGH.getAction();
    }

    Levels(double action) {
        this.action = action;
    }
}
