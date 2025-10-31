package packetmonitor.analysis;
import java.net.InetAddress;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

import packetmonitor.LocalUtil.*;
import packetmonitor.core.*;

public class analyse {

    
    private static boolean isWhitelisted(String id, InetAddress source){
        for (Map<String, String> entry : app.ConnectionEntries) {

            int entry_id =  Integer.parseInt(entry.get("id"));
            int claimed_id = Integer.parseInt(id);

            
            if (entry_id == claimed_id){
                InetAddress whitelist_address;
                try{whitelist_address = InetAddress.getByName(entry.get("source"));}catch (Exception e){
                    System.out.println("[ANALYSER] could not resolve host address from whitelist");
                    return false;
                }

               
                if(Arrays.equals(whitelist_address.getAddress(), source.getAddress())){
                    return true;
                }
            }
        }
        return false;
    }

    public static void AnalyseLoop(){
        
        while (true) {
            try{
                //packet.isTCP: boolean
                //packet.data: Map<String, String> (String data, int sensorid, String timestamp)
                //packet.data.get(sensorid)
                Packet packet = app.AnalyseQueue.take();
                String message = packet.data.get("data");
                String id = packet.data.get("id");
                InetAddress source = packet.source;
                boolean isTCP = packet.isTCP;

                String ClientTimeStampString = packet.data.get("timestamp");
                LocalDateTime ClientTimeStamp = LocalDateTime.parse(ClientTimeStampString, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                LocalDateTime ServerTimeStamp = LocalDateTime.now();
                Duration TravelTime = Duration.between(ClientTimeStamp, ServerTimeStamp);

                boolean IsAllowed = isWhitelisted(id, source);

                if (app.DisplayIncoming){
                    System.out.println("=====Incoming Packet=====");
                    System.out.println("source: "+source);
                    System.out.println("data: "+message);
                    System.out.println("TCP: "+isTCP);
                    System.out.println("traveltime(ms): "+ TravelTime.toMillis());
                    System.out.println("Sensor ID: "+id);
                    System.out.println("allowed: "+IsAllowed);
                }


                if(IsAllowed){
                    // Redirect packet to main server
                    // server address is at: 'app.DestinationServerAddress' (String)
                }

                
            }catch(Exception e){
                System.out.println(e);
            }
        }
    }
}
