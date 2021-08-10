package it.unipi.iot.irrigationsystem.coap.tap;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class TapActuator {
    private CoapClient clientTapActuator;
    private CoapObserveRelation observeTapIntensity;
    private int tapInterval;
    private double tapIntensity = 1; // Default value

    public void addTapActuator(String ip) {
        System.out.println("The tap actuator: [" + ip + "] + is now registered");
        clientTapActuator = new CoapClient("coap://[" + ip + "]/tap_intensity");

        observeTapIntensity = clientTapActuator.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();
                        String[] tokens = responseString.split(" ");

                        tapIntensity = Double.parseDouble(tokens[0]);

                        // TODO: the other value tells where the water is taken, make use in the simulation

                        IrrigationSystemDbManager.insertTapValues(tapIntensity, tapInterval);
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });
    }

    public void deleteTapActuator(String ip) {
        if(clientTapActuator.getURI().equals(ip)) {
            clientTapActuator = null;
            observeTapIntensity.proactiveCancel();
            observeTapIntensity = null;
        }
    }
}
