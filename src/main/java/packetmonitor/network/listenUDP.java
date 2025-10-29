package packetmonitor.network;

import java.io.*;
import java.net.*;

import java.util.Map;

import packetmonitor.LocalUtil.*;
import packetmonitor.core.app;


public class listenUDP {

    public static volatile boolean UDPrunning = false;
    private static DatagramSocket UDPSocket = null;
    
    public static boolean stopUDPServer(){
        try{
            UDPSocket.close();
            UDPrunning = false;
        }catch (Exception e){
            System.out.println("[UDP SERVER] An error occurred while stopping UDP server.. "+e);
        }
        return true;
    }

    public static void startUDPServer(int Port_UDP){
        try{
            UDPSocket = new DatagramSocket(Port_UDP);
            System.out.println("[UDP SERVER] UDP (NON ENCRYPTED) listening on port "+Port_UDP+"..");
            UDPrunning = true;
            byte[] buffer = new byte[1024];
            while(app.SocketStatus == true){
               
                DatagramPacket UDP_Packet = new DatagramPacket(buffer, buffer.length);
                UDPSocket.receive(UDP_Packet);
                InetAddress source = UDPSocket.getInetAddress();
                
                Map<String, String> data = null;
                try{
                    app.AnalyseQueue.put(new Packet(false, data, source)); //Packet struct?
                }catch(InterruptedException e){
                    Thread.currentThread().interrupt();
                    System.err.println("[UDP Server] An error occurred while putting UDP packet inside analyze queue: "+e);
                }finally{
                    UDPSocket.close();
                }
            }
        }catch (IOException e){
            System.out.println("UDP socket setup failed. Is the port already in use?");
        }
    }
}
