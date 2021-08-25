package it.unipi.iot.irrigationsystem.mqtt.reservoir;
import it.unipi.iot.irrigationsystem.mqtt.MQTTNetworkHandler;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Map;

public class ReservoirCollector {
    private MQTTNetworkHandler handler;
    private final String intervalPubTopic = "interval";
    private final String reservoirPubTopic = "set_reservoir_level";
    private double level = 0;

    public ReservoirCollector(MQTTNetworkHandler handler){
        this.handler = handler;
    }

    public double getLastAverageReservoirLevel(){
        return level;
        /*int sum = 0;
        int num = 0;

        Map<String, Double> samples = handler.getReceivedReservoirSamples();
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            sum += sample.getValue();
            num++;
        }
        return (double) sum / num;
        */
    }

    public void changeInterval(long newInterval){
        try {
            handler.publish(intervalPubTopic, Long.toString(newInterval));
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void changeReservoirLevel(double quantity){
        /*
        try {
            handler.publish(reservoirPubTopic, Double.toString(quantity));
        } catch (MqttException e) {
            e.printStackTrace();
        }*/
        level +=quantity;
    }
}