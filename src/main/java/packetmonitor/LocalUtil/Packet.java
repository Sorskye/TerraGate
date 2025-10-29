package packetmonitor.LocalUtil;
import java.net.InetAddress;
import java.util.Map;

public class Packet {
    public final boolean isTCP;
    public final Map<String, String> data;
    public final InetAddress source;
    
    
    public Packet(boolean isTCP, Map<String, String> data, InetAddress source) {
        this.isTCP = isTCP;
        this.data = data;
        this.source = source;
    }
}

