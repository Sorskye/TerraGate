package packetmonitor.LocalUtil;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class jsonUtil {
    private static final String CONNECTIONS_FILE_PATH = "whitelist.json";
    private static final String CONFIG_FILE_PATH = "config.json";

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

    public static List<Map<String, String>> load_whitelist() {
        List<Map<String, String>> entries = new ArrayList<>();
        File file = new File(CONNECTIONS_FILE_PATH);

        if(!file.exists()) return entries;

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null){
                if(!line.trim().isEmpty()){
                    Map<String, String> map = jsonUtil.ParseJSON(line);
                    entries.add(map);
                }
            }
        }catch (IOException e){
            System.err.println("[JSON util] Error while loading JSON data for: "+CONNECTIONS_FILE_PATH+": "+e);
        }

        return entries;
    }

    public static List<Map<String, String>> load_config() {
        List<Map<String, String>> entries = new ArrayList<>();
        File file = new File(CONFIG_FILE_PATH);

        if(!file.exists()) return entries;

        try(BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null){
                if(!line.trim().isEmpty()){
                    Map<String, String> map = jsonUtil.ParseJSON(line);
                    entries.add(map);
                }
            }
        }catch (IOException e){
            System.err.println("[JSON util] Error while loading JSON data for: "+CONNECTIONS_FILE_PATH+": "+e);
        }

        return entries;
    }

    public static void save_whitelist(List<Map<String, String>> entries){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(CONNECTIONS_FILE_PATH))){
            for (Map<String, String> entry: entries){
                String json = String.format(
                    "{\"id\":\"%s\",\"source\":\"%s\"}",
                    entry.get("id"),
                    entry.get("source")
                );
                writer.write(json);
                writer.newLine();
            }
        }catch (IOException e){
            System.out.println("[JSON util] Error while saving JSON data for: "+CONNECTIONS_FILE_PATH+": "+e);
        }
    }

    public static void save_config(List<Map<String, String>> entries){
        try(BufferedWriter writer = new BufferedWriter(new FileWriter(CONFIG_FILE_PATH))){
            for (Map<String, String> entry: entries){
                String json = String.format(
                    "{\"destination-server\":\"%s\",\"MaxQueue\":\"%s\",\"Cert-FilePath\":\"%s\"}",
                    entry.get("destination"),
                    entry.get("queuesize"),
                    entry.get("CertPath")
                );
                writer.write(json);
                writer.newLine();
            }
        }catch (IOException e){
            System.out.println("[JSON util] Error while saving JSON data for: "+CONNECTIONS_FILE_PATH+": "+e);
        }
    }

}
