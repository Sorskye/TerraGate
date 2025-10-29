package packetmonitor.LocalUtil;

import java.util.HashMap;
import java.util.Map;

public class jsonUtil {
    public static Map<String, String> ParseJSON(String json){
        Map<String, String> map = new  HashMap<>();
        json = json.trim().substring(1,json.length()-1);

        String[] pairs = json.split(",");
        for(String pair : pairs){
            String[] kv = pair.split(":",2);
            String key = kv[0].trim().replace("\"", "");
            String value = kv[1].trim().replace("\"", "");
            map.put(key, value);
        }
        return map;
    } 

    public static void Test(){
        
    }
}
