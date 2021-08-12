package it.unipi.iot.irrigationsystem.coap;

import it.unipi.iot.irrigationsystem.coap.rain.RainSensor;
import it.unipi.iot.irrigationsystem.coap.soilmoisture.SoilMoistureNetwork;
import it.unipi.iot.irrigationsystem.coap.tap.TapActuator;
import it.unipi.iot.irrigationsystem.coap.temperature.TemperatureSensorNetwork;
import it.unipi.iot.irrigationsystem.enumerate.Bound;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
import it.unipi.iot.irrigationsystem.enumerate.WhereWater;

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

    // Add and remove functions
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

    // Temperature Actions
    public boolean changeTemperatureBounds(Bound bound, int newValue) {
        return temperatureSensorNetwork.changeBounds(bound, newValue);
    }

    public boolean changeTemperatureSwitchStatus(SwitchStatus switchStatus) {
        return temperatureSensorNetwork.turnSwitch(switchStatus);
    }

    public int getTemperature() {
        return temperatureSensorNetwork.getTemperatureDetected();
    }

    // Rain Actions
    public boolean getWeather() {
        return rainSensor.isRaining();
    }

    // Soil Moisture Actions
    public double getSoilTension() {
        return soilMoistureNetwork.getSoilTensionDetected();
    }

    public boolean changeSoilTensionBounds(Bound bound, double newValue) {
        return soilMoistureNetwork.changeBounds(bound, newValue);
    }

    public boolean changeSoilMoistureSwitch(SwitchStatus switchStatus) {
        return soilMoistureNetwork.turnSwitch(switchStatus);
    }

    // Tap Actions
    public int getTapInterval() {
        return tapActuator.getTapInterval();
    }

    public double getTapIntensity() {
        return tapActuator.getTapIntensity();
    }

    public WhereWater getTapWhereWater() {
        return tapActuator.getWhereWater();
    }

    public boolean setTapWhereWater(WhereWater whereWater) {
        return tapActuator.setWhereWater(whereWater);
    }

    public boolean setTapIntensity(double newValue) {
        return tapActuator.setTapIntensity(newValue);
    }

    public boolean setTapInterval(int newValue) {
        return tapActuator.setTapInterval(newValue);
    }

    // General functions
    public void printAllDevices() {
        temperatureSensorNetwork.printDevices();
        rainSensor.printDevice();
        soilMoistureNetwork.printDevices();
        tapActuator.printDevice();
    }
}
