package it.unipi.iot.irrigationsystem.coap.tap;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import it.unipi.iot.irrigationsystem.enumerate.WhereWater;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

public class TapActuator {
    private CoapClient clientTapActuator;
    private CoapClient clientTapSwitch;
    private CoapClient clientTapWhereWater;
    private CoapClient clientTapInterval;
    private CoapObserveRelation observeTapIntensity;

    private int tapInterval = 2; // Default value
    private double tapIntensity = 1; // Default value
    private WhereWater whereWater;


    public void addTapActuator(String ip) {
        System.out.println("The tap actuator: [" + ip + "] + is now registered");
        clientTapActuator = new CoapClient("coap://[" + ip + "]/tap_intensity");
        clientTapSwitch = new CoapClient("coap://[" + ip + "]/tap_switch");
        clientTapInterval = new CoapClient("coap://[" + ip + "]/tap_interval");
        clientTapWhereWater = new CoapClient("coap://[" + ip + "]/tap_where_water");

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

    public double getTapIntensity() {
        return tapIntensity;
    }

    public int getTapInterval() {
        return tapInterval;
    }

    public WhereWater getWhereWater() {
        return whereWater;
    }

    public void printDevice() {
        if (clientTapActuator != null)
            System.out.println("Tap Actuator:\n\t" + clientTapActuator.getURI() + "\n");
    }
}
