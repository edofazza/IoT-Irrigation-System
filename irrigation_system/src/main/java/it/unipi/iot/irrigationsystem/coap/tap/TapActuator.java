package it.unipi.iot.irrigationsystem.coap.tap;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import it.unipi.iot.irrigationsystem.enumerate.SwitchStatus;
import it.unipi.iot.irrigationsystem.enumerate.WhereWater;
import it.unipi.iot.irrigationsystem.logging.Logger;
import org.eclipse.californium.core.CoapClient;
import org.eclipse.californium.core.CoapHandler;
import org.eclipse.californium.core.CoapObserveRelation;
import org.eclipse.californium.core.CoapResponse;
import org.eclipse.californium.core.coap.MediaTypeRegistry;

import java.util.concurrent.atomic.AtomicReference;

public class TapActuator {
    private CoapClient clientTapActuator; // intensity + switch
    private CoapClient clientTapInterval;
    private CoapObserveRelation observeTapIntensity;

    private int tapInterval = 10; // Default value
    private AtomicReference<Double> tapIntensity = new AtomicReference<>(5.0); // Default value
    private WhereWater whereWater = WhereWater.AQUIFER; // Default value


    public void addTapActuator(String ip) {
        Logger.log("The tap actuator: [" + ip + "] + is now registered");
        clientTapActuator = new CoapClient("coap://[" + ip + "]/tap_intensity");
        clientTapInterval = new CoapClient("coap://[" + ip + "]/tap_interval");

        observeTapIntensity = clientTapActuator.observe(
                new CoapHandler() {

                    public void onLoad(CoapResponse response) {
                        String responseString = response.getResponseText();

                        try {
                            tapIntensity.set(Double.parseDouble(responseString));
                        }catch (Exception e){}
                        IrrigationSystemDbManager.insertTapValues(tapIntensity.get(), tapInterval);
                    }

                    public void onError() {
                        Logger.error("Tap actuator OBSERVING FAILED");
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
        return whereWater;
    }

    public boolean setWhereWater(WhereWater whereWater) {
        this.whereWater = whereWater;
        return setTapIntensity(this.tapIntensity.get());
    }

    public boolean setTapIntensity(double newValue) {
        if (clientTapActuator == null)
            return false;

        String msg = "";
        switch (whereWater) {
            case AQUIFER:
                msg = "A ";
                break;
            case RESERVOIR:
                msg = "R ";
                break;
        }
        msg += Double.toString(newValue);

        clientTapActuator.put(new CoapHandler() {
            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess())
                        Logger.error("Something went wrong with tap actuator");
                    }
                }

                public void onError() {
                    Logger.error("[ERROR: TapActuator " + clientTapActuator.getURI() + "] ");
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
                        Logger.error("Something went wrong with tap actuator");
                }
            }

            public void onError() {
                Logger.error("[ERROR: Tap actuator " + clientTapInterval.getURI() + "] ");
            }

        }, msg, MediaTypeRegistry.TEXT_PLAIN);

        tapIntensity.set(tapIntensity.get()* ((double)newValue)/((double)tapInterval));
        tapInterval = newValue;
        return true;
    }

    public boolean turnSwitch(SwitchStatus switchStatus) {
        if (clientTapActuator == null)
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


        clientTapActuator.post(new CoapHandler() {

            public void onLoad(CoapResponse response) {
                if (response != null) {
                    if(!response.isSuccess())
                        Logger.error("Something went wrong with Tap switch");
                }
            }

            public void onError() {
                    Logger.error("[ERROR: TapSwitch " + clientTapActuator.getURI() + "] ");
                }

            }, msg, MediaTypeRegistry.TEXT_PLAIN);

        return true;
    }

    public void printDevice() {
        if (clientTapActuator != null)
            System.out.println("Tap Actuator:\n\t" + clientTapActuator.getURI() + "\n");
    }
}
