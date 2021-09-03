package it.unipi.iot.irrigationsystem.mqtt;

import it.unipi.iot.irrigationsystem.database.IrrigationSystemDbManager;
import it.unipi.iot.irrigationsystem.logging.Logger;
import org.eclipse.paho.client.mqttv3.*;
import org.json.simple.*;
import org.json.simple.parser.ParseException;

import java.util.HashMap;
import java.util.Map;

public class MQTTNetworkHandler implements MqttCallback{

    private final String brokerIpAddr = "127.0.0.1";
    private final int brokerPort = 1883;
    private final String broker = "tcp://" + brokerIpAddr + ":" + brokerPort;
    private final String clientId = "JavaApp";

    private final String reservoirSubTopic = "reservoir_level";

    private final String aquiferSubTopic = "aquifer_level";

    private MqttClient mqttClient = null;

    private Map<String, Double> receivedAquiferSamples = new HashMap();
    private Map<String, Double> receivedReservoirSamples = new HashMap();

    public MQTTNetworkHandler() throws InterruptedException {

        do {
            try {
                this.mqttClient = new MqttClient(this.broker,this.clientId);
                Logger.log("[MQTT Java Client]: Connecting to broker "+broker);

                this.mqttClient.setCallback( this );
                this.mqttClient.connect();

                this.mqttClient.subscribe(this.reservoirSubTopic);
                Logger.log("[MQTT Java Client]:Subscribed to topic "+this.reservoirSubTopic);

                this.mqttClient.subscribe(this.aquiferSubTopic);
                Logger.log("[MQTT Java Client]: Subscribed to topic "+this.aquiferSubTopic);
            }catch(MqttException me) {
                Logger.error("[MQTT Java Client]: I could not connect, Retrying ...");
            }
        }while(!this.mqttClient.isConnected());

    }

    public void publish(final String topic, final String content) throws MqttException{
        try {
            MqttMessage message = new MqttMessage(content.getBytes());
            this.mqttClient.publish(topic, message);
        } catch(MqttException me) {
            me.printStackTrace();
        }
    }

    public void connectionLost(Throwable cause) {
        Logger.warning("[MQTT Java Client]: Connection is broken: " + cause);
        int timeWindow = 3000;
        while (!this.mqttClient.isConnected()) {
            try {
                Logger.log("[MQTT Java Client]: Trying to reconnect in " + timeWindow/1000 + " seconds.");
                Thread.sleep(timeWindow);
                Logger.log("[MQTT Java Client]: Reconnecting ...");
                timeWindow *= 2;
                this.mqttClient.connect();

                this.mqttClient.subscribe(this.aquiferSubTopic);
                this.mqttClient.subscribe(this.reservoirSubTopic);
                Logger.log("[MQTT Java Client]: Connection is restored");
            }catch(MqttException me) {
                Logger.error("[MQTT Java Client]: I could not connect");
            } catch (InterruptedException e) {
                Logger.error("[MQTT Java Client]: I could not connect");
            }
        }
    }

    public void messageArrived(String topic, MqttMessage message) throws Exception {
        byte[] payload = message.getPayload();

        try {
            JSONObject sensorMessage = (JSONObject) JSONValue.parseWithException(new String(payload));

            if(topic.equals(this.reservoirSubTopic))
            {
                if (sensorMessage.containsKey("node")
                        && sensorMessage.containsKey("reservoir_availability")
                        && sensorMessage.containsKey("unit")
                ) {
                    Double numericValue = Double.parseDouble(sensorMessage.get("reservoir_availability").toString());
                    String nodeId = sensorMessage.get("node").toString();

                    receivedReservoirSamples.put(nodeId, numericValue);
                    IrrigationSystemDbManager.insertWaterLevReservoir(nodeId, numericValue);


                } else {
                    System.out.println("Garbage data from sensor");
                }
            } else if (topic.equals(this.aquiferSubTopic)) {
                if (sensorMessage.containsKey("node")
                        && sensorMessage.containsKey("aquifer_availability")
                        && sensorMessage.containsKey("unit")
                ) {
                    Double numericValue = Double.parseDouble(sensorMessage.get("aquifer_availability").toString());
                    String nodeId = sensorMessage.get("node").toString();

                    receivedAquiferSamples.put(nodeId, numericValue);
                    IrrigationSystemDbManager.insertWaterLevAquifer(nodeId, numericValue);

                } else {
                    System.out.println("Garbage data from sensor");
                }
            } else {
                System.out.println(String.format("Unknown topic: [%s] %s", topic, new String(payload)));
            }
        } catch (ParseException e) {
            System.out.println(String.format("Received badly formatted message: [%s] %s", topic, new String(payload)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void deliveryComplete(IMqttDeliveryToken token) {
        //System.out.println("Delivery Complete\n");
    }

    public int getNumberOfAquiferSensors() {
        return receivedAquiferSamples.size();
    }

    public int getNumberOfReservoirSensors() {
        return receivedReservoirSamples.size();
    }

    public void printAquiferSensors() {
        printSensors(receivedAquiferSamples);
    }

    public void printReservoirSensors() {
        printSensors(receivedReservoirSamples);
    }

    private void printSensors(final Map<String, Double> samples) {
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            System.out.println("\t" + sample.getKey() + "\n");
        }
    }

    public Map<String, Double> getReceivedAquiferSamples() {
        return receivedAquiferSamples;
    }

    public Map<String, Double> getReceivedReservoirSamples() {
        return receivedReservoirSamples;
    }
}

