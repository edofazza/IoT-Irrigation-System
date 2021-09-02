package it.unipi.iot.irrigationsystem.mqtt.reservoir;
import it.unipi.iot.irrigationsystem.mqtt.MQTTNetworkHandler;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Map;

public class ReservoirCollector {
    private MQTTNetworkHandler handler;
    private final String pubTopic = "reservoir";
    private double level = 50;

    public ReservoirCollector(MQTTNetworkHandler handler){
        this.handler = handler;
    }

    public double getLastAverageReservoirLevel(){
        int sum = 0;
        int num = 0;

        Map<String, Double> samples = handler.getReceivedReservoirSamples();
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            sum += sample.getValue();
            num++;
        }
        level = (double) sum / num;
        return level;

    }

    public void changeInterval(long newInterval){
        try {
            String message ="i" + newInterval;
            handler.publish(pubTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void changeReservoirLevel(double quantity){
        try {
            String message = "l"+ (int)quantity;
            handler.publish(pubTopic, message);
        } catch (MqttException e) {
            e.printStackTrace();
        }
        level +=quantity;
    }
}