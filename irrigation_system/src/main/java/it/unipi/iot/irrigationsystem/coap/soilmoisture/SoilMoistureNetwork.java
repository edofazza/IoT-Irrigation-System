package it.unipi.iot.irrigationsystem.coap.soilmoisture;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import it.unipi.iot.irrigationsystem.enumerate.Bound;
import it.unipi.iot.irrigationsystem.enumerate.BoundStatus;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

public class SoilMoistureNetwork {
    private List<CoapClient> clientSoilMoistureSensorList = new ArrayList<>();
    private List<CoapObserveRelation> observeSoilTensionList = new ArrayList<>();
    private List<CoapClient> clientSoilMoistureSwitchList = new ArrayList<>();
    private double soilTensionDetected = 0;
    private AtomicReference<BoundStatus> boundStatus = new AtomicReference<>(BoundStatus.NORMAL);

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
                                    boundStatus.set(BoundStatus.TOO_LOW);
                                    break;
                                case "cold":
                                    System.out.println("Tension too high!!!");
                                    boundStatus.set(BoundStatus.TOO_HIGH);
                                    break;
                            }
                            soilTensionDetected = Double.parseDouble(tokens[2]);
                        } else {
                            soilTensionDetected = Double.parseDouble(responseString);
                            boundStatus.set(BoundStatus.NORMAL);
                        }
                        // TODO IrrigationSystemDbManager.insertSoilMoistureValue(soilTensionDetected);
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

    public double getSoilTensionDetected() {
        return soilTensionDetected;
    }

    public boolean changeBounds(Bound bound, double newValue) {
        if (clientSoilMoistureSensorList.isEmpty())
            return false;

        String msg;
        switch (bound) {
            case LOWER:
                msg = "l " + newValue;
                break;
            case UPPER:
                msg = "u " + newValue;
                break;
            default:
                return false;
        }

        for (CoapClient coapClient: clientSoilMoistureSensorList) {
            coapClient.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Something went wrong with soil moisture sensor");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: SoilMoistureSensor " + coapClient.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }

        return true;
    }

    public boolean turnSwitch(SwitchStatus switchStatus) {
        if (clientSoilMoistureSwitchList.isEmpty())
            return false;

        String msg;
        switch (switchStatus) {
            case ON:
                msg = "ON";
                break;
            case OFF:
                msg = "OFF";
                break;
            default:
                return false;
        }

        for (CoapClient coapClient: clientSoilMoistureSwitchList) {
            coapClient.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Something went wrong with soil moisture switch");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: SoilMoistureSwitch " + coapClient.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }
        return true;
    }

    public void printDevices() {
        System.out.println("Soil moisture sensors:");
        for(CoapClient cc: clientSoilMoistureSensorList) {
            System.out.println("\t" + cc.getURI() + "\n");
        }
    }

    public BoundStatus getBoundStatus() {
        return boundStatus.get();
    }
}
