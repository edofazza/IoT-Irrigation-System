package it.unipi.iot.irrigationsystem.coap.soilmoisture;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.ArrayList;
import java.util.List;

public class SoilMoistureNetwork {
    private List<CoapClient> clientSoilMoistureSensorList = new ArrayList<>();
    private List<CoapObserveRelation> observeSoilTensionList = new ArrayList<>();
    private List<CoapClient> clientSoilMoistureSwitchList = new ArrayList<>();
    private double soilTensionDetected = 0;

    public void addSoilMoisture(String ip) {
        System.out.println("The soil moisture sensor: [" + ip + "] + is now registered");

        // Add the soil moisture switch resource
        CoapClient newSoilMoistureSwitch = new CoapClient("coap://[" + ip + "]/soil_moisture_switch");
        clientSoilMoistureSwitchList.add(newSoilMoistureSwitch);

        // Add the temperature_sensor resource
        CoapClient newSoilMoistureSensor = new CoapClient("coap://[" + ip + "]/soil_moisture_sensor");

        CoapObserveRelation newObserveSoilTension = newSoilMoistureSensor.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();

                        if (responseString.startsWith("WARN")) {
                            String[] tokens = responseString.split(" ");

                            switch (tokens[1]) {
                                case "hot":
                                    System.out.println("Tension too low!!!");
                                    break;
                                case "cold":
                                    System.out.println("Tension too high!!!");
                                    break;
                            }
                            soilTensionDetected = Double.parseDouble(tokens[2]);
                        } else {
                            soilTensionDetected = Double.parseDouble(responseString);
                        }
                        IrrigationSystemDbManager.insertSoilMoistureValue(soilTensionDetected);
                    }

                    public void onError() {
                        System.err.println("OBSERVING FAILED");
                    }
                });

        clientSoilMoistureSensorList.add(newSoilMoistureSensor);
        observeSoilTensionList.add(newObserveSoilTension);
    }

    public void deleteSoilMoisture(String ip) {
        for (int i = 0; i < clientSoilMoistureSensorList.size(); i++) {
            if (clientSoilMoistureSensorList.get(i).getURI().equals(ip)) {
                clientSoilMoistureSensorList.remove(i);
                observeSoilTensionList.get(i).proactiveCancel();
                observeSoilTensionList.remove(i);
            }
        }
    }

    public void printDevices() {
        for(CoapClient cc: clientSoilMoistureSensorList) {
            System.out.println("Soil moisture sensors:");
            System.out.println("\t" + cc.getURI() + "\n");
        }
    }
}
