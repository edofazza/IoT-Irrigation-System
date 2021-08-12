package it.unipi.iot.irrigationsystem;

import it.unipi.iot.irrigationsystem.enumerate.WhereWater;
import it.unipi.iot.irrigationsystem.mqtt.aquifer.AquiferCollector;
import it.unipi.iot.irrigationsystem.mqtt.reservoir.ReservoirCollector;
import it.unipi.iot.irrigationsystem.registration.RegistrationServer;

import java.util.concurrent.atomic.AtomicLong;

class Parameters{
    public boolean isRaining;
    public double soilTension;
    public int temperature;
    public double aquiferLevel;
    public double reservoirLevel;
}

public class AutomaticIrrigationSystem implements Runnable{
    private RegistrationServer rs;
    private AquiferCollector ac;
    private ReservoirCollector rc;
    private AtomicLong interval= new AtomicLong(1);

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
        while(true){
            try {
                Thread.sleep(interval.get() * 1000);
            }catch (Exception e){
                e.printStackTrace();
            }
            Parameters p = new Parameters();
            WhereWater w = WhereWater.AQUIFER;
            double quantity = 0;
            populateParameters(p);
            if(p.isRaining){
                System.out.println("Is Raining, no irrigation is needed");
                continue;
            }

        }
    }

    private void populateParameters(Parameters p){
        p.isRaining = rs.getWeather();
        if(p.isRaining==true)
            return;
        p.soilTension = rs.getSoilTension();
        p.temperature = rs.getTemperature();
        p.aquiferLevel = ac.getLastAverageAquiferLevel();
        p.reservoirLevel = rc.getLastAverageReservoirLevel();
    }
}
