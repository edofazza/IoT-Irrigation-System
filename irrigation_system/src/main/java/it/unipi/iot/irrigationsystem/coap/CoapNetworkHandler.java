package it.unipi.iot.irrigationsystem.coap;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;

import java.util.ArrayList;
import java.util.List;

public class CoapNetworkHandler {
    private List<CoapClient> clientTemperatureSensorList = new ArrayList<>();
    private List<CoapObserveRelation> observeTemperatureList = new ArrayList<>();
    private List<CoapClient> clientTemperatureSwitchList = new ArrayList<>();
    private int temperatureDetected = 0;

    private CoapClient clientRainSensor;
    private CoapObserveRelation observeRain;
    private boolean isRaining;

    private List<CoapClient> clientSoilMoistureSensorList = new ArrayList<>();
    private List<CoapObserveRelation> observeSoilTensionList = new ArrayList<>();
    private List<CoapClient> clientSoilMoistureSwitchList = new ArrayList<>();
    private double soilTensionDetected = 0;

    private CoapClient clientTapActuator;
    private CoapObserveRelation observeTapIntensity;
    private int tapInterval;
    private double tapIntensity = 1; // Default value

    private static CoapNetworkHandler instance = null; // SINGLETON



    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();

        return instance;
    }

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

    public void addRainSensor(String ip) {
        System.out.println("The rain sensor: [" + ip + "] + is now registered");
        clientRainSensor = new CoapClient("coap://[" + ip + "]/rain_sensor");

        observeRain = clientRainSensor.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();

                        isRaining = responseString.equals("raining");

                        IrrigationSystemDbManager.insertRainStatus(isRaining);
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
        for (int i = 0; i < clientTemperatureSensorList.size(); i++) {
            if (clientTemperatureSensorList.get(i).getURI().equals(ip)) {
                clientTemperatureSensorList.remove(i);
                observeTemperatureList.get(i).proactiveCancel();
                observeTemperatureList.remove(i);
            }
        }
    }

    public void addTapActuator(String ip) {
        System.out.println("The tap actuator: [" + ip + "] + is now registered");
        clientTapActuator = new CoapClient("coap://[" + ip + "]/tap_intensity");

        observeTapIntensity = clientRainSensor.observe(
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
