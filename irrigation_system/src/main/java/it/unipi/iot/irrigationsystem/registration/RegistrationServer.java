package it.unipi.iot.irrigationsystem.registration;

import it.unipi.iot.irrigationsystem.coap.CoapNetworkHandler;
import it.unipi.iot.irrigationsystem.enumerate.Bound;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
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

    // General functions
    public void printDevices() {
        coapHandler.printAllDevices();
    }
}
