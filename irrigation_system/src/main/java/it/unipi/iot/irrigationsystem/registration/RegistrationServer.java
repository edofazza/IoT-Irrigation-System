package it.unipi.iot.irrigationsystem.registration;

import it.unipi.iot.irrigationsystem.coap.CoapNetworkHandler;
import it.unipi.iot.irrigationsystem.enumerate.Bound;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
import it.unipi.iot.irrigationsystem.enumerate.WhereWater;
import it.unipi.iot.irrigationsystem.registration.resources.RegistrationResource;
import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;

public class RegistrationServer extends CoapServer {
    private final static CoapNetworkHandler coapHandler = CoapNetworkHandler.getInstance();


    public RegistrationServer() throws SocketException {
        add(new RegistrationResource(coapHandler));
    }

    // Temperature utility functions
    public boolean changeTemperatureBounds(Bound bound, int newValue) {
        return coapHandler.changeTemperatureBounds(bound, newValue);
    }

    public boolean changeTemperatureSwitchStatus(SwitchStatus switchStatus) {
        return coapHandler.changeTemperatureSwitchStatus(switchStatus);
    }

    public int getTemperature() {
        return coapHandler.getTemperature();
    }

    // Rain utility functions
    public boolean getWeather() {
        return coapHandler.getWeather();
    }

    // Soil Moisture utility function
    public double getSoilTension() {
        return coapHandler.getSoilTension();
    }

    public boolean changeSoilTensionBound(Bound bound, double newTension) {
        return coapHandler.changeSoilTensionBounds(bound, newTension);
    }

    public boolean changeSoilMoistureSwitchStatus(SwitchStatus switchStatus) {
        return coapHandler.changeSoilMoistureSwitch(switchStatus);
    }

    // Tap utility functions
    public int getTapInterval() {
        return coapHandler.getTapInterval();
    }

    public double getTapIntensity() {
        return coapHandler.getTapIntensity();
    }

    public WhereWater getTapWhereWater() {
        return coapHandler.getTapWhereWater();
    }

    public boolean setTapWhereWater(WhereWater whereWater) {
        return coapHandler.setTapWhereWater(whereWater);
    }

    public boolean setTapIntensity(double newValue) {
        return coapHandler.setTapIntensity(newValue);
    }

    public boolean setTapInterval(int newValue) {
        return  coapHandler.setTapInterval(newValue);
    }

    public boolean changeTapSwitchStatus(SwitchStatus switchStatus) {
        return coapHandler.changeTapSwitchStatus(switchStatus);
    }

    // General functions
    public void printDevices() {
        coapHandler.printAllDevices();
    }

}
