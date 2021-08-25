package it.unipi.iot.irrigationsystem.automation;

import it.unipi.iot.irrigationsystem.automation.utils.Levels;
import it.unipi.iot.irrigationsystem.automation.utils.Parameters;
import it.unipi.iot.irrigationsystem.enumerate.BoundStatus;
import it.unipi.iot.irrigationsystem.enumerate.WhereWater;
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
        WhereWater waterSource;
        double quantity;
        while(!Thread.currentThread().isInterrupted()){
            try {
                Thread.sleep(interval.get() * 1000);
            }catch (Exception e){
                break;
            }
            Parameters p = new Parameters();
            populateParameters(p);
            if(p.isRaining){
                System.out.println("Is Raining, no irrigation is needed");
                continue;
            }
            double need = computeNeed(p);
            quantity = need*p.tapIntensity;
            waterSource = determineWaterSource(quantity, p);
            System.out.println("Output of  "+quantity+ " cm^3 of water from the tap, source is " +waterSource);
            //TODO log of quantity and source
            actuate(quantity, waterSource, p);
        }
        System.out.println("Exiting automatic system");
    }

    private void populateParameters(Parameters p){
        p.isRaining = rs.getWeather();
        if(p.isRaining==true)
            return;
        p.soilStatus = rs.getSoilTensionBoundStatus();
        p.temperatureStatus = rs.getTempBoundStatus();
        p.aquiferLevel = ac.getLastAverageAquiferLevel();
        p.reservoirLevel = rc.getLastAverageReservoirLevel();
        p.tapIntensity = rs.getTapIntensity();
    }

    private double computeNeed(Parameters p){
        Levels level;
        switch(p.soilStatus){
            case TOO_LOW:
                level = Levels.HIGH;
                break;
            case NORMAL:
                level = Levels.MEDIUM;
                break;
            default:
                level = Levels.LOW;
        }
        if (p.temperatureStatus == BoundStatus.TOO_HIGH)
            level = level.increaseLevel(level);
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
            System.out.println("\tFetched "+quantity + " from the RESERVOIR");
        }
        else{
            rc.changeReservoirLevel(p.aquiferLevel-quantity);
            System.out.println("\tFetched "+p.aquiferLevel+"cm^3 from the aquifer");
            System.out.println("\t" + quantity + "cm^3 of them are output of the tap,");
            System.out.println("\t" + (p.aquiferLevel-quantity) + "cm^3 of them are stored in the reservoir");
        }
        rs.setTapWhereWater(source);
    }

}
