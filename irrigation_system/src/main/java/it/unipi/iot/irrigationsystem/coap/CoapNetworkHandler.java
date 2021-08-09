package it.unipi.iot.irrigationsystem.coap;

public class CoapNetworkHandler {
    private static CoapNetworkHandler instance = null; // SINGLETON

    public static CoapNetworkHandler getInstance() {
        if (instance == null)
            instance = new CoapNetworkHandler();

        return instance;
    }

    public void addTemperatureSensor(String ip) {

    }

    public void deleteTemperatureSensor(String ip) {

    }

    public void addRainSensor(String ip) {

    }

    public void deleteRainSensor(String ip) {

    }

    public void addSoilMoisture(String ip) {
    }

    public void deleteSoilMoisture(String ip) {
    }

    public void addTapActuator(String ip) {
    }

    public void deleteTapActuator(String ip) {
    }
}
