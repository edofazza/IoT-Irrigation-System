package it.unipi.iot.irrigationsystem.coap.temperature;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.ArrayList;
import java.util.List;

public class TemperatureSensorNetwork {
    private List<CoapClient> clientTemperatureSensorList = new ArrayList<>();
    private List<CoapObserveRelation> observeTemperatureList = new ArrayList<>();
    private List<CoapClient> clientTemperatureSwitchList = new ArrayList<>();
    private int temperatureDetected = 0;

    public void addTemperatureSensor(String ip) {
        System.out.println("The presence sensor: [" + ip + "] + is now registered");

        // Add the temperature_switch resource
        CoapClient newTemperatureSwitch = new CoapClient("coap://[" + ip + "]/temperature_switch");
        clientTemperatureSwitchList.add(newTemperatureSwitch);

        // Add the temperature_sensor resource
        CoapClient newTemperatureSensor = new CoapClient("coap://[" + ip + "]/temperature_sensor");

        CoapObserveRelation newObserveTemperature = newTemperatureSensor.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();

                        if (responseString.startsWith("WARN")) {
                            String[] tokens = responseString.split(" ");

                            switch (tokens[1]) {
                                case "hot":
                                    System.out.println("Temperature too hot!!!");
                                    break;
                                case "cold":
                                    System.out.println("Temperature too cold!!!");
                                    break;
                            }
                            temperatureDetected = Integer.parseInt(tokens[2]);
                        } else {
                            temperatureDetected = Integer.parseInt(responseString);
                        }
                        IrrigationSystemDbManager.insertTemperature(temperatureDetected);
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });

        clientTemperatureSensorList.add(newTemperatureSensor);
        observeTemperatureList.add(newObserveTemperature);
    }

    public void deleteTemperatureSensor(String ip) {
        for (int i = 0; i < clientTemperatureSensorList.size(); i++) {
            if (clientTemperatureSensorList.get(i).getURI().equals(ip)) {
                clientTemperatureSensorList.remove(i);
                observeTemperatureList.get(i).proactiveCancel();
                observeTemperatureList.remove(i);
            }
        }
    }

    public void printDevices() {
        for(CoapClient cc: clientTemperatureSensorList) {
            System.out.println("Temperature sensors:");
            System.out.println("\t" + cc.getURI() + "\n");
        }
    }
}
