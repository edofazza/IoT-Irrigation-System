package it.unipi.iot.irrigationsystem.enumerate;

public enum WhereWater {
    AQUIFER, RESERVOIR;

    public String toString(WhereWater w){
        if (w.equals(AQUIFER))
            return "AQUIFER";
        return "RESERVOIR";
    }
}
