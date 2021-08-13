package it.unipi.iot.irrigationsystem.coap.rain;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.concurrent.atomic.AtomicBoolean;

public class RainSensor {
    private CoapClient clientRainSensor;
    private CoapObserveRelation observeRain;
    private AtomicBoolean isRaining=new AtomicBoolean();

    public void addRainSensor(String ip) {
        System.out.println("The rain sensor: [" + ip + "] + is now registered");
        clientRainSensor = new CoapClient("coap://[" + ip + "]/rain_sensor");

        observeRain = clientRainSensor.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();

                        isRaining.set(responseString.equals("raining"));

                        IrrigationSystemDbManager.insertRainStatus(isRaining.get());
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });
    }

    public void deleteRainSensor(String ip) {
        if(clientRainSensor.getURI().equals(ip)) {
            clientRainSensor = null;
            observeRain.proactiveCancel();
            observeRain = null;
        }
    }

    public boolean isRaining() {
        return isRaining.get();
    }

    public void printDevice() {
        if (clientRainSensor != null)
            System.out.println("Rain Sensor:\n\t" + clientRainSensor.getURI() + "\n");
    }
}
