package it.unipi.iot.irrigationsystem.mqtt;

import java.util.Map;
import java.util.HashMap;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.ParseException;

public class MQTTNetworkHandler implements MqttCallback{

    private final String brokerIpAddr = "127.0.0.1";
    private final int brokerPort = 1883;
    private final String broker = "tcp://" + brokerIpAddr + brokerPort;
    private final String clientId = "JavaApp";

    private final String intervalPubTopic = "interval";

    private final String reservoirSubTopic = "reservoir_level";
    private final String reservoirPubTopic = "set_reservoir_level";

    private final String aquiferSubTopic = "aquifer_level";

    private MqttClient mqttClient = null;

    private Map<String, Double> receivedAquiferSamples = new HashMap();
    private Map<String, Double> receivedReservoirSamples = new HashMap();

    public CollectorMqttClient() throws InterruptedException {
        do {
            try {
                this.mqttClient = new MqttClient(this.broker,this.clientId);
                System.out.println("Connecting to broker: "+broker);

                this.mqttClient.setCallback( this );
                this.mqttClient.connect();

                this.mqttClient.subscribe(this.reservoirSubTopic);
                System.out.println("Subscribed to topic: "+this.reservoirSubTopic);

                this.mqttClient.subscribe(this.aquiferSubTopic);
                System.out.println("Subscribed to topic: "+this.aquiferSubTopic);
            }catch(MqttException me) {
                System.out.println("I could not connect, Retrying ...");
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
        System.out.println("Connection is broken: " + cause);
        int timeWindow = 3000;
        while (!this.mqttClient.isConnected()) {
            try {
                System.out.println("Trying to reconnect in " + timeWindow/1000 + " seconds.");
                Thread.sleep(timeWindow);
                System.out.println("Reconnecting ...");
                timeWindow *= 2;
                this.mqttClient.connect();

                this.mqttClient.subscribe(this.tempSubTopic);
                this.mqttClient.subscribe(this.clSubTopic);
                System.out.println("Connection is restored");
            }catch(MqttException me) {
                System.out.println("I could not connect");
            } catch (InterruptedException e) {
                System.out.println("I could not connect");
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

                    String unit = sensorMessage.get("unit").toString();

                    receivedReservoirSamples.put(nodeId, numericValue);
                    // TODO add to db


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
                    // TODO add to db

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
        System.out.println("Delivery Complete\n");

    }


    private double averageSampleValue(final Map<String, Double> samples) {
        int sum = 0;
        int num = 0;
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            sum += sample.getValue();
            num++;
        }
        return (double) sum / num;
    }


    public int getNumberOfAquiferSensors() {
        return receivedAquiferSamples.size();
    }

    public int getNumberOfReservoirSensors() {
        return receivedReservoirSamples.size();
    }

    public void printAquiferSensors() {
        stampSensors(receivedAquiferSamples);
    }

    public void printReservoirSensors() {
        stampSensors(receivedReservoirSamples);
    }

    private void printSensors(final Map<String, Double> samples) {
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            System.out.println("> " + sample.getKey() + "\n");
        }
    }
}

