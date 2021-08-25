package it.unipi.iot.irrigationsystem.mqtt.aquifer;
import it.unipi.iot.irrigationsystem.mqtt.MQTTNetworkHandler;
import org.eclipse.paho.client.mqttv3.MqttException;

import java.util.Map;

public class AquiferCollector {
    private MQTTNetworkHandler handler;
    private final String intervalPubTopic = "interval";

    public AquiferCollector(MQTTNetworkHandler handler){
        this.handler = handler;
    }

    public double getLastAverageAquiferLevel(){
        return 6.0;
        /*int sum = 0;
        int num = 0;
        Map<String, Double> samples = handler.getReceivedAquiferSamples();
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            sum += sample.getValue();
            num++;
        }
        return (double) sum / num;*/
    }

    public void changeInterval(long newInterval){
        /*
        try {
            handler.publish(intervalPubTopic, Long.toString(newInterval));
        } catch (MqttException e) {
            e.printStackTrace();
        }
        */
    }
}