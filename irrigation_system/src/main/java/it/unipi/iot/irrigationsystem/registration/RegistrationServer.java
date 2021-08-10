package it.unipi.iot.irrigationsystem.registration;

import it.unipi.iot.irrigationsystem.coap.CoapNetworkHandler;
import it.unipi.iot.irrigationsystem.registration.resources.RegistrationResource;
import org.eclipse.californium.core.CoapServer;

import java.net.SocketException;

public class RegistrationServer extends CoapServer {
    private final static CoapNetworkHandler coapHandler = CoapNetworkHandler.getInstance();


    public RegistrationServer() throws SocketException {
        add(new RegistrationResource(coapHandler));
    }

    public void printDevices() {
        coapHandler.printAllDevices();
    }
}
