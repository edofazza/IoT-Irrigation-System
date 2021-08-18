package it.unipi.iot.irrigationsystem.automation.utils;

public enum Levels {
    NOT_NEEDED(0), LOW(0.4), MEDIUM(0.8), HIGH(1.2), VERY_HIGH(1.4);
    private double action;

    public double getAction() {
        return this.action;
    }

    public Levels increaseLevel(Levels level){
        if (level == Levels.NOT_NEEDED) {
            return LOW;
        } else if (level == LOW) {
            return MEDIUM;
        } else if (level == MEDIUM) {
            return HIGH;
        }
        return VERY_HIGH;
    }

    Levels(double action) {
        this.action = action;
    }
}
