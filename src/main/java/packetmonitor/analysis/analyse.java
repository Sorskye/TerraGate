package packetmonitor.analysis;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import packetmonitor.LocalUtil.*;
import packetmonitor.core.*;

public class analyse {


    public static void AnalyseLoop(){
        
        while (true) {
            try{
                //packet.isTCP: boolean
                //packet.data: Map<String, String> (String data, int sensorid, String timestamp)
                //packet.data.get(sensorid)
                Packet packet = app.AnalyseQueue.take();
                String message = packet.data.get("data");
                String sensorid = packet.data.get("sensorid");
                InetAddress source = packet.source;
                boolean isTCP = packet.isTCP;

                String ClientTimeStampString = packet.data.get("timestamp");
                LocalDateTime ClientTimeStamp = LocalDateTime.parse(ClientTimeStampString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime ServerTimeStamp = LocalDateTime.now();
                Duration TravelTime = Duration.between(ClientTimeStamp, ServerTimeStamp);

                if (app.DisplayIncoming){
                    System.out.println("=====Incoming Packet=====");
                    System.out.println("source: "+source);
                    System.out.println("data: "+message);
                    System.out.println("TCP: "+isTCP);
                    System.out.println("traveltime(ms): "+ TravelTime.toMillis());
                    System.out.println("Sensor ID: "+sensorid);
                    System.out.println("");

                }

                
                
                
                
            }catch(Exception e){
                System.out.println(e);
            }
        }
    }
}
