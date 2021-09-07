package it.unipi.iot.irrigationsystem.automation;

import it.unipi.iot.irrigationsystem.automation.utils.Levels;
import it.unipi.iot.irrigationsystem.automation.utils.Parameters;
import it.unipi.iot.irrigationsystem.enumerate.BoundStatus;
import it.unipi.iot.irrigationsystem.enumerate.WhereWater;
import it.unipi.iot.irrigationsystem.logging.Logger;
import it.unipi.iot.irrigationsystem.mqtt.aquifer.AquiferCollector;
import it.unipi.iot.irrigationsystem.mqtt.reservoir.ReservoirCollector;
import it.unipi.iot.irrigationsystem.registration.RegistrationServer;

import java.util.concurrent.atomic.AtomicLong;


public class AutomaticIrrigationSystem implements Runnable{
    private RegistrationServer rs;
    private AquiferCollector ac;
    private ReservoirCollector rc;
    private AtomicLong interval= new AtomicLong(10);

    public AutomaticIrrigationSystem(RegistrationServer rs, AquiferCollector ac, ReservoirCollector rc){
        this.ac = ac;
        this.rc = rc;
        this.rs = rs;
    }

    public AutomaticIrrigationSystem(RegistrationServer rs, AquiferCollector ac, ReservoirCollector rc, long interval){
        this.ac = ac;
        this.rc = rc;
        this.rs = rs;
        this.interval.set(interval);
    }

    public void setNewInterval(long interval){
        this.interval.set(interval);
    }

    @Override
    public void run() {
        Logger.log("[Irrigation System]: Automatic Irrigation System started");
        WhereWater waterSource;
        double quantity;
        double need;
        Parameters p;
        while(!Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(interval.get() * 1000);
            }catch (Exception e){
                break;
            }
            p = new Parameters(rs, ac, rc);
            p.populateParameters();
            if(p.isRaining){
                Logger.log("[Irrigation System]: It's Raining, no irrigation is needed");
                continue;
            }
            need = computeNeed(p);
            quantity = need*p.tapIntensity;
            waterSource = determineWaterSource(quantity, p);
            Logger.log("[Irrigation System]: Output of  "+quantity+ " cm^3 of water from the tap, source is " +waterSource);
            actuate(quantity, waterSource, p);
        }
        Logger.log("[Irrigation System]: Exiting Automatic Irrigation System");
    }

    private double computeNeed(Parameters p){
        Levels level;
        switch(p.soilStatus){
            case TOO_LOW:
                level = Levels.LOW;  //low soil moisture means wet soil, so little water is needed
                break;
            case NORMAL:
                level = Levels.MEDIUM;
                break;
            default:
                level = Levels.HIGH;
        }
        if (p.temperatureStatus == BoundStatus.TOO_HIGH)
            level.increaseAction();
        return level.getAction();
    }

    private WhereWater determineWaterSource(double need, Parameters p){
        if (need>p.aquiferLevel)
            return WhereWater.RESERVOIR;
        return WhereWater.AQUIFER;
    }

    private void actuate(double quantity, WhereWater source, Parameters p){
        if (source == WhereWater.RESERVOIR){
            rc.changeReservoirLevel(0-quantity);
            Logger.log("\tFetched "+quantity + " from the RESERVOIR");
        }
        else{
            rc.changeReservoirLevel(p.aquiferLevel-quantity);
            Logger.log("\tFetched "+p.aquiferLevel+"cm^3 from the aquifer");
            Logger.log("\t" + quantity + "cm^3 of them are output of the tap,");
            Logger.log("\t" + (p.aquiferLevel-quantity) + "cm^3 of them are stored in the reservoir");
        }
        if (rs.getTapWhereWater() != source)
            rs.setTapWhereWater(source);
    }

}
