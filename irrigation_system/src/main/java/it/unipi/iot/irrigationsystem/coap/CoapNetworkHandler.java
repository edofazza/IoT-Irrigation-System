package it.unipi.iot.irrigationsystem.coap;

import it.unipi.iot.irrigationsystem.coap.rain.RainSensor;
import it.unipi.iot.irrigationsystem.coap.soilmoisture.SoilMoistureNetwork;
import it.unipi.iot.irrigationsystem.coap.tap.TapActuator;
import it.unipi.iot.irrigationsystem.coap.temperature.TemperatureSensorNetwork;

public class CoapNetworkHandler {
    private TemperatureSensorNetwork temperatureSensorNetwork = new TemperatureSensorNetwork();
    private RainSensor rainSensor = new RainSensor();
    private SoilMoistureNetwork soilMoistureNetwork = new SoilMoistureNetwork();
    private TapActuator tapActuator = new TapActuator();

    private static CoapNetworkHandler instance = null; // SINGLETON

    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();

        return instance;
    }

    public void addTemperatureSensor(String ip) {
        temperatureSensorNetwork.addTemperatureSensor(ip);
    }

    public void deleteTemperatureSensor(String ip) {
        temperatureSensorNetwork.deleteTemperatureSensor(ip);
    }

    public void addRainSensor(String ip) {
        rainSensor.addRainSensor(ip);
    }

    public void deleteRainSensor(String ip) {
        rainSensor.deleteRainSensor(ip);
    }

    public void addSoilMoisture(String ip) {
        soilMoistureNetwork.addSoilMoisture(ip);
    }

    public void deleteSoilMoisture(String ip) {
        soilMoistureNetwork.deleteSoilMoisture(ip);
    }

    public void addTapActuator(String ip) {
        tapActuator.addTapActuator(ip);
    }

    public void deleteTapActuator(String ip) {
        tapActuator.deleteTapActuator(ip);
    }

    public void printAllDevices() {
        temperatureSensorNetwork.printDevices();
        rainSensor.printDevice();
        soilMoistureNetwork.printDevices();
        tapActuator.printDevice();
    }
}
