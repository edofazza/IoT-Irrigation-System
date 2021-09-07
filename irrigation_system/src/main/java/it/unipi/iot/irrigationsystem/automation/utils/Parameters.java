package it.unipi.iot.irrigationsystem.automation.utils;

import it.unipi.iot.irrigationsystem.enumerate.BoundStatus;
import it.unipi.iot.irrigationsystem.mqtt.aquifer.AquiferCollector;
import it.unipi.iot.irrigationsystem.mqtt.reservoir.ReservoirCollector;
import it.unipi.iot.irrigationsystem.registration.RegistrationServer;

public class Parameters{
    public boolean isRaining;
    public BoundStatus soilStatus;
    public BoundStatus temperatureStatus;
    public double aquiferLevel;
    public double reservoirLevel;
    public double tapIntensity;

    private RegistrationServer rs;
    private AquiferCollector ac;
    private ReservoirCollector rc;

    public Parameters(RegistrationServer rs, AquiferCollector ac, ReservoirCollector rc){
        this.rs = rs;
        this.rc = rc;
        this.ac = ac;
    }

    public void populateParameters(){
        isRaining = rs.getWeather();
        if(isRaining==true)
            return;
        soilStatus = rs.getSoilTensionBoundStatus();
        temperatureStatus = rs.getTempBoundStatus();
        aquiferLevel = ac.getLastAverageAquiferLevel();
        reservoirLevel = rc.getLastAverageReservoirLevel();
        tapIntensity = rs.getTapIntensity();
    }

}
