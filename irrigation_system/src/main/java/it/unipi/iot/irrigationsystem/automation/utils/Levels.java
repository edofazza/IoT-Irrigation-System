package it.unipi.iot.irrigationsystem.automation.utils;

public enum Levels {
    NOT_NEEDED(0), LOW(4), MEDIUM(8), HIGH(12), VERY_HIGH(14);
    private double action;

    public double getAction() {
        return this.action;
    }

    public Levels increaseAction(){
        if (action == Levels.NOT_NEEDED.getAction()) {
            return Levels.LOW;
        } else if (action == Levels.LOW.getAction()) {
            return Levels.MEDIUM;
        } else if (action == Levels.MEDIUM.getAction()) {
            return HIGH;
        } else
            return VERY_HIGH;
    }

    Levels(double action) {
        this.action = action;
    }
}
