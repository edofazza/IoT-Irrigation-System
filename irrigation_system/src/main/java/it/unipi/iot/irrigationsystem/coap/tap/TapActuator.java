package it.unipi.iot.irrigationsystem.coap.tap;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
import it.unipi.iot.irrigationsystem.enumerate.WhereWater;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.concurrent.atomic.AtomicReference;

public class TapActuator {
    private CoapClient clientTapActuator; // intensity
    private CoapClient clientTapSwitch;
    private CoapClient clientTapWhereWater;
    private CoapClient clientTapInterval;
    private CoapObserveRelation observeTapIntensity;

    private int tapInterval = 2; // Default value
    private AtomicReference<Double> tapIntensity = new AtomicReference<>(1.0); // Default value
    private WhereWater whereWater = WhereWater.AQUIFER; // Default value


    public void addTapActuator(String ip) {
        System.out.println("The tap actuator: [" + ip + "] + is now registered");
        clientTapActuator = new CoapClient("coap://[" + ip + "]/tap_intensity");
        clientTapSwitch = new CoapClient("coap://[" + ip + "]/tap_switch");
        clientTapInterval = new CoapClient("coap://[" + ip + "]/tap_interval");
        clientTapWhereWater = new CoapClient("coap://[" + ip + "]/tap_where_water");

        observeTapIntensity = clientTapActuator.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();
                        String[] tokens = responseString.split(" ");

                        tapIntensity.set(Double.parseDouble(tokens[0]));

                        // TODO: do something with the information about the water
                        // align the where water with the device
                        whereWater = tokens[1].equals("A") ? WhereWater.AQUIFER : WhereWater.RESERVOIR;

                        // TODO IrrigationSystemDbManager.insertTapValues(tapIntensity.get(), tapInterval);
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

    public double getTapIntensity() {
        return tapIntensity.get();
    }

    public int getTapInterval() {
        tapInterval = Integer.parseInt(clientTapInterval.get().getResponseText());

        return tapInterval;
    }

    public WhereWater getWhereWater() {
        whereWater = clientTapWhereWater.get().getResponseText().equals("aquifer") ? WhereWater.AQUIFER : WhereWater.RESERVOIR;

        return whereWater;
    }

    public boolean setWhereWater(WhereWater whereWater) {
        if (clientTapWhereWater == null)
            return false;

        String msg;
        switch (whereWater) {
            case AQUIFER:
                msg = "aquifer";
                break;
            case RESERVOIR:
                msg = "reservoir";
                break;
            default:
                return false;
        }

        clientTapWhereWater.put(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess())
                        System.out.println("Something went wrong with temperature sensor");
                }
            }

            public void onError() {
                System.err.println("[ERROR: TemperatureSensor " + clientTapWhereWater.getURI() + "] ");
            }

        }, msg, MediaTypeRegistry.TEXT_PLAIN);

        this.whereWater = whereWater;
        return true;
    }

    public boolean setTapIntensity(double newValue) {
        if (clientTapActuator == null)
            return false;

        String msg = Double.toString(newValue);

        clientTapActuator.put(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess())
                        System.out.println("Something went wrong with temperature sensor");
                    }
                }

                public void onError() {
                    System.err.println("[ERROR: TemperatureSensor " + clientTapActuator.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);

        tapIntensity.set(newValue);
        return true;
    }

    public boolean setTapInterval(int newValue) {
        if (clientTapInterval == null)
            return false;

        String msg = Integer.toString(newValue);

        clientTapInterval.put(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess())
                        System.out.println("Something went wrong with temperature sensor");
                }
            }

            public void onError() {
                System.err.println("[ERROR: TemperatureSensor " + clientTapInterval.getURI() + "] ");
            }

        }, msg, MediaTypeRegistry.TEXT_PLAIN);

        tapInterval = newValue;
        return true;
    }

    public boolean turnSwitch(SwitchStatus switchStatus) {
        if (clientTapSwitch == null)
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


        clientTapSwitch.put(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess())
                        System.out.println("Something went wrong with temperature switch");
                }
            }

            public void onError() {
                    System.err.println("[ERROR: TemperatureSwitch " + clientTapSwitch.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);

        return true;
    }

    public void printDevice() {
        if (clientTapActuator != null)
            System.out.println("Tap Actuator:\n\t" + clientTapActuator.getURI() + "\n");
    }
}
