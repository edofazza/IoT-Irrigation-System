package it.unipi.iot.irrigationsystem.mqtt.aquifer;
import it.unipi.iot.irrigationsystem.mqtt.MQTTNetworkHandler;

public class AcquiferCollector{
    private MQTTNetworkHandler handler;
    private final String intervalPubTopic = "interval";
    private final String aquiferSubTopic = "aquifer_level";

    public AcquiferCollector(MQTTNetworkHandler handler){
        this.handler = handler;
    }

    public double getLastAverageAquiferLevel(){
        int sum=num=0;
        Map<String, Double>samples = handler.getReceivedAquiferSamples();
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            sum += sample.getValue();
            num++;
        }
        return (double) sum / num;
    }

    public void changeInterval(long newInterval){
        handler.publish(intervalPubTopic, Long.toString(newInterval));
    }
}