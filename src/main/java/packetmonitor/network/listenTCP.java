package packetmonitor.network;

import java.io.*;
import java.net.*;

import java.util.Map;
import java.util.concurrent.ExecutorService;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;

import packetmonitor.core.app;
import packetmonitor.LocalUtil.*;

import java.security.KeyStore;


// gen ssl using: keytool -genkeypair -alias server -keyalg RSA -keysize 2048 -keystore server.jks -validity 365

public class listenTCP {
    public static volatile int TCPConnectionsMade = 0;
    public static volatile boolean TCPrunning = false;
    private static SSLServerSocket ServerSocket = null;

    public static boolean StopTCPServer(){
        if(TCPrunning == false){return true;}
        try{
            ServerSocket.close();
            
        }catch (Exception e){
            System.out.println("[TCP SERVER] An error occurred when closing the TCP server.. "+e);
            return false;
        }
        
        TCPrunning = false;
        return true;
    }
   
    public static void startTCPServer(int Port_TCP, String TLSver, ExecutorService TCPHandlerPool, ExecutorService AnalyzeThreadPool){
        
        try{
            KeyStore keystore = KeyStore.getInstance("JKS");
            keystore.load(new FileInputStream(app.CertifacteFilePath), "changeit".toCharArray());

            KeyManagerFactory keymanagerfactory = KeyManagerFactory.getInstance("Sunx509");
            keymanagerfactory.init(keystore, "changeit".toCharArray());

            SSLContext sslContext = SSLContext.getInstance(TLSver);
            sslContext.init(keymanagerfactory.getKeyManagers(), null, null);

            SSLServerSocketFactory ssf = sslContext.getServerSocketFactory();
            ServerSocket = (SSLServerSocket) ssf.createServerSocket(Port_TCP);
            ServerSocket.setNeedClientAuth(false);

            System.out.println("");
            System.out.println("[TCP SERVER] TCP listening on port: "+Port_TCP+" TLS version: "+TLSver);
            TCPrunning = true;
        }catch (Exception e){
             System.out.println("[TLS INIT] TLS init failed for TCP server.. "+e);
            
        }
       

        while (app.SocketStatus == true) {
            try{
                SSLSocket socket = (SSLSocket) ServerSocket.accept();
                TCPConnectionsMade++;
                TCPHandlerPool.submit(()-> handleTCPclient(socket, AnalyzeThreadPool));
            }catch (IOException e){
               // System.out.println("[TCP SERVER] An error occurred while setting up TLS socket.. "+e);
            }
            
        }
    }

    private static void StopTCPhandler(SSLSocket clientSocket){
        try{
            clientSocket.close();
        }catch (IOException e){
            System.out.println("[TCP HANDLER] Error when closing TCP socket | client: "+clientSocket.getInetAddress());
        }
    }



    private static void handleTCPclient(SSLSocket clientSocket, ExecutorService AnalyzeThreadPool){
        try{
            //String name = Thread.currentThread().getName();
            //System.out.println("Handle TCP. Thread: "+name);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String json = reader.readLine();
            InetAddress source = clientSocket.getInetAddress();

            Map<String, String> data = jsonUtil.ParseJSON(json);
            app.AnalyseQueue.put(new Packet(true, data, source));
            
            
        }catch(Exception e){
            System.out.println("[ERROR] non-fatal error while handling TCP client ("+clientSocket.getInetAddress()+"): "+e);
            
        }finally{
            StopTCPhandler(clientSocket);
        }
        
    }
}
