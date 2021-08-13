package it.unipi.iot.irrigationsystem.coap.rain;

import it.unipi.iot.irrigationsystem.coap.soilmoisture.SoilMoistureNetwork;
import it.unipi.iot.irrigationsystem.coap.tap.TapActuator;
import it.unipi.iot.irrigationsystem.coap.temperature.TemperatureSensorNetwork;
import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.concurrent.atomic.AtomicBoolean;

public class RainSensor {
    private CoapClient clientRainSensor;
    private CoapObserveRelation observeRain;
    private AtomicBoolean isRaining=new AtomicBoolean();

    private SoilMoistureNetwork moistureNetwork;
    private TapActuator tapActuator;
    private TemperatureSensorNetwork temperatureSensorNetwork;

    public void addRainSensor(String ip) {
        System.out.println("The rain sensor: [" + ip + "] + is now registered");
        clientRainSensor = new CoapClient("coap://[" + ip + "]/rain_sensor");

        observeRain = clientRainSensor.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();

                        isRaining.set(responseString.equals("raining"));

                        if (moistureNetwork != null)
                            moistureNetwork.turnSwitch(isRaining.get() ? SwitchStatus.ON : SwitchStatus.OFF);
                        if (temperatureSensorNetwork != null)
                            temperatureSensorNetwork.turnSwitch(isRaining.get() ? SwitchStatus.ON : SwitchStatus.OFF);
                        if (tapActuator != null)
                            tapActuator.turnSwitch(isRaining.get() ? SwitchStatus.ON : SwitchStatus.OFF);

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

    public void addControlledDevices(SoilMoistureNetwork moistureNetwork,
                                     TapActuator tapActuator,
                                     TemperatureSensorNetwork temperatureSensorNetwork) {
        this.moistureNetwork = moistureNetwork;
        this.tapActuator = tapActuator;
        this.temperatureSensorNetwork = temperatureSensorNetwork;
    }
}
