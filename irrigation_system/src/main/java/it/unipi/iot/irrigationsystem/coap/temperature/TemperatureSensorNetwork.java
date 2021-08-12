package it.unipi.iot.irrigationsystem.coap.temperature;

import it.unipi.iot.irrigationsystem.enumerate.Bound;
import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import it.unipi.iot.irrigationsystem.enumerate.BoundStatus;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.ArrayList;
import java.util.List;

public class TemperatureSensorNetwork {
    private List<CoapClient> clientTemperatureSensorList = new ArrayList<>();
    private List<CoapObserveRelation> observeTemperatureList = new ArrayList<>();
    private List<CoapClient> clientTemperatureSwitchList = new ArrayList<>();
    private int temperatureDetected = 0;
    private BoundStatus boundStatus = BoundStatus.NORMAL;

    public void addTemperatureSensor(String ip) {
        System.out.println("The presence sensor: [" + ip + "] is now registered");

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
                                    boundStatus = BoundStatus.TOO_HIGH;
                                    break;
                                case "cold":
                                    System.out.println("Temperature too cold!!!");
                                    boundStatus = BoundStatus.TOO_LOW;
                                    break;
                            }
                            temperatureDetected = Integer.parseInt(tokens[2]);
                        } else {
                            temperatureDetected = Integer.parseInt(responseString);
                        }
                        //IrrigationSystemDbManager.insertTemperature(temperatureDetected);
                        boundStatus = BoundStatus.NORMAL;
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

    public boolean changeBounds(Bound bound, int newValue) {
        if (clientTemperatureSensorList.isEmpty())
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

        for (CoapClient coapClient: clientTemperatureSensorList) {
            coapClient.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Something went wrong with temperature sensor");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: TemperatureSensor " + coapClient.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }

        return true;
    }

    public boolean turnSwitch(SwitchStatus switchStatus) {
        if (clientTemperatureSwitchList.isEmpty())
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

        for (CoapClient coapClient: clientTemperatureSensorList) {
            coapClient.put(new CoapHandler() {

                public void onLoad(CoapResponse response) {
                    if (response != null) {
                        if(!response.isSuccess())
                            System.out.println("Something went wrong with temperature switch");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: TemperatureSwitch " + coapClient.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);
        }
        return true;
    }

    public void printDevices() {
        System.out.println("Temperature sensors:");
        for(CoapClient cc: clientTemperatureSensorList)
            System.out.println("\t" + cc.getURI());
    }

    public int getTemperatureDetected() {
        return temperatureDetected;
    }

    public BoundStatus getBoundStatus() {
        return boundStatus;
    }
}
