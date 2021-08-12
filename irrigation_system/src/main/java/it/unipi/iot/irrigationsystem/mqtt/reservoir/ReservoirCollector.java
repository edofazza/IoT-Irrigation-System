package it.unipi.iot.irrigationsystem.mqtt.reservoir;
import it.unipi.iot.irrigationsystem.mqtt.MQTTNetworkHandler;

public class AcquiferCollector{
    private MQTTNetworkHandler handler;
    private final String intervalPubTopic = "interval";
    private final String reservoirSubTopic = "reservoir_level";
    private final String reservoirPubTopic = "set_reservoir_level";

    public AcquiferCollector(MQTTNetworkHandler handler){
        this.handler = handler;
    }

    public double getLastAverageReservoirLevel(){
        int sum=num=0;
        Map<String, Double>samples = handler.getReceivedReservoirSamples();
        for(Map.Entry<String, Double> sample: samples.entrySet()) {
            sum += sample.getValue();
            num++;
        }
        return (double) sum / num;
    }

    public void changeInterval(long newInterval){
        handler.publish(intervalPubTopic, Long.toString(newInterval));
    }

    public void changeReservoirLevel(double quantity){
        handler.publish(reservoirPubTopic, Double.toString(quantity));
    }
}