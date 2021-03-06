package it.unipi.iot.irrigationsystem.registration.resources;

import it.unipi.iot.irrigationsystem.coap.CoapNetworkHandler;
import org.eclipse.californium.core.CoapResource;
import org.eclipse.californium.core.coap.CoAP;
import org.eclipse.californium.core.server.resources.CoapExchange;

import java.nio.charset.StandardCharsets;

public class RegistrationResource extends CoapResource {
    private static CoapNetworkHandler coapHandler;

    public RegistrationResource(CoapNetworkHandler cnh) {
        super("registration");
        coapHandler = cnh;
    }

    @Override
    public void handlePOST(CoapExchange exchange) {
        String deviceType = exchange.getRequestText();
        String ipAddress = exchange.getSourceAddress().getHostAddress();
        boolean success = true;


        switch (deviceType) {
            case "temperature_sensor":
                coapHandler.addTemperatureSensor(ipAddress);
                break;
            case "rain_sensor":
                coapHandler.addRainSensor(ipAddress);
                break;
            case "soil_moisture_sensor":
                coapHandler.addSoilMoisture(ipAddress);
                break;
            case "tap_actuator":
                coapHandler.addTapActuator(ipAddress);
                break;
            default:
                success = false;
                break;
        }

        if (success)
            exchange.respond(CoAP.ResponseCode.CREATED, "Success".getBytes(StandardCharsets.UTF_8));
        else
            exchange.respond(CoAP.ResponseCode.NOT_ACCEPTABLE, "Unsuccessful".getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public void handleDELETE(CoapExchange exchange) {
        String[] request = exchange.getRequestText().split("-");
        String ipAddress = request[0];
        String deviceType = request[1];
        boolean success = true;


        switch (deviceType) {
            case "temperature_sensor":
                coapHandler.deleteTemperatureSensor(ipAddress);
                break;
            case "rain_sensor":
                coapHandler.deleteRainSensor(ipAddress);
                break;
            case "soilMoisture_sensor":
                coapHandler.deleteSoilMoisture(ipAddress);
                break;
            case "tap_actuator":
                coapHandler.deleteTapActuator(ipAddress);
                break;
            default:
                success = false;
                break;
        }

        if (success)
            exchange.respond(CoAP.ResponseCode.DELETED, "Cancellation Completed!".getBytes(StandardCharsets.UTF_8));
        else
            exchange.respond(CoAP.ResponseCode.BAD_REQUEST, "Cancellation not allowed!".getBytes(StandardCharsets.UTF_8));
    }
}
