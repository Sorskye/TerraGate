package packetmonitor.core;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import java.util.Scanner;

import packetmonitor.network.listenTCP;
import packetmonitor.network.listenUDP;
import packetmonitor.LocalUtil.*;
import packetmonitor.analysis.*;




public class app {
    public static int Port_UDP = 5005;
    public static final int DefaultPort_UDP = 5005;
    public static int Port_TCP = 5004;
    public static final int DefaultPort_TCP = 5004;

    private static final int MaxQueueSize = 500;
    public static final BlockingQueue<Packet> HandlerQueue = new ArrayBlockingQueue<>(MaxQueueSize);
    public static final BlockingQueue<Packet> AnalyseQueue = new ArrayBlockingQueue<>(MaxQueueSize);
    public static final BlockingQueue<Packet> OutputQueue = new ArrayBlockingQueue<>(MaxQueueSize);
    public static volatile boolean SocketStatus = true;
    public static volatile boolean DisplayIncoming = false;

    private static Scanner StartupScanner = null;
    private static void shutdown(){
        StartupScanner.close();
        System.out.println("Shutdown hook is closing sockets.. ");
        listenTCP.StopTCPServer();
        listenUDP.stopUDPServer();
        SocketStatus = false;

        System.out.println("Waiting for servers to shutdown..");
        int i = 0;
        while(listenTCP.TCPrunning == true || listenUDP.UDPrunning == true){
            try{
                Thread.sleep(500);
                }catch (Exception e){System.out.println("[APP] Error: "+e);}
                if(i>10){ System.out.println("TCP and/or UDP server(s) did NOT close. Sockets need to be terminated manually..");break;}
                i++;
            }
        System.out.println("Shutdown successfull");
        
    }

    public static void main(String[] args) {
        Runtime runtime = Runtime.getRuntime();

        System.out.println("\n");
        System.out.println("TerraGate V 0.0.1");
        System.out.println("Powered by Java 25");
        System.out.println("Licencend under the GNU General Public Licence v3.0");
        System.out.println("Developed by Teun Maalderink");
        System.out.println("---");

        

        Runtime.getRuntime().addShutdownHook(new Thread(()->shutdown()));

        int avail_proc = runtime.availableProcessors();
        int RequiredThreads = 6;
        long JVMTotalMemory = runtime.totalMemory();

        StartupScanner = new Scanner(System.in);
        System.out.println("Threads to use? (min: "+(RequiredThreads)+" max:"+avail_proc+") (0 for max) >");
        int thread_input = StartupScanner.nextInt();
        if(thread_input != 0){
            avail_proc = thread_input;
        }else if(thread_input < RequiredThreads){
            thread_input = RequiredThreads;
        }

        int reservedThreads = 4;
        int LeftOverThreads = avail_proc - reservedThreads;

        // Divide value into two parts
        int parserThreads = LeftOverThreads / 2;        
        int tcpThreads = LeftOverThreads - parserThreads;   
        parserThreads = (LeftOverThreads + 1) / 2; 
        tcpThreads = LeftOverThreads / 2;

        System.out.println("Threads: (TCP:"+tcpThreads+"), (Parsers:"+parserThreads+"), (CLI,Output,Listeners: "+reservedThreads+") Total: "+(tcpThreads+parserThreads+reservedThreads));


        while(true){
            System.out.println("Use default ports? UDP-5005 & TCP-5004 (y,n)");
            String port_input = StartupScanner.next();
            if(port_input.equalsIgnoreCase("y")){
                Port_TCP = DefaultPort_TCP;
                Port_UDP = DefaultPort_UDP;
                break;
            }else if(port_input.equalsIgnoreCase("n")){
                System.out.println("UDP port?: ");
                int InputUDP = StartupScanner.nextInt();
                Port_UDP = InputUDP;
                System.out.println("TCP port?: ");
                int InputTCP = StartupScanner.nextInt();
                Port_TCP = InputTCP;
                break;
            }
        }

        
       
        System.out.println("UDP port is set to: "+Port_UDP + " | Default: "+DefaultPort_UDP);
        System.out.println("TCP port is set to: "+Port_TCP + " | Default: "+DefaultPort_TCP);
        System.out.println("JVM current Total Memory: "+JVMTotalMemory / (1024 * 1024)+" MB");
       
        ExecutorService AnalyzeThreadPool = Executors.newFixedThreadPool(parserThreads);
        ExecutorService TCPHandlerPool = Executors.newFixedThreadPool(4);

        System.out.println("Executor Initialized, "+parserThreads+" Analysing threads available");
        String TLSver = "TLSv1.3";

        new Thread(()-> listenTCP.startTCPServer(Port_TCP, TLSver, TCPHandlerPool, AnalyzeThreadPool)).start();
        

        for (int i = 0; i < parserThreads; i++){
            AnalyzeThreadPool.submit(() -> analyse.AnalyseLoop());
            System.out.println("Packet Analyser Thread: "+i+" -> running :");
        }

        System.out.println("Waiting for TCP server to start..");
        int i = 0;
        while (listenTCP.TCPrunning == false) {
            try{
                Thread.sleep(500);
                if(i>5){ System.out.println("TCP server did not start.."); break;}
                i++;
            }catch (Exception e){System.out.println("[APP] Error: "+e);}
        }

        System.out.println("[!!!] the UDP server is unencrypted, and not enabled by default. To enable, use the command interface..");
       

        // START CLI
        StartupScanner.nextLine();
        while (true) {
            System.out.print("TerraGate >");
            String cliInput = StartupScanner.nextLine();
            switch (cliInput) {
                case "exit":
                    
                    System.exit(0);
                    break;
                case "help":
                    System.out.println("");
                    System.out.println("===== available commands =====");
                    System.out.println("exit            -quits the program");
                    System.out.println("help            -show a list of available commands");
                    System.out.println("show incoming   -display incoming packets");
                    System.out.println("hide incoming   -hide incoming packets");
                    System.out.println("start UDP       -[!] start the unencrypted UDP server");
                    System.out.println("stop UDP        -stop the unencrypted UDP server");
                    System.out.println("status          -shows the status of the program");
                    System.out.println("");
                    break;
                case "status":
                    System.out.println("");
                    System.out.println("===== status =====");
                    System.out.println("Total memory in use: "+runtime.totalMemory() / (1024 * 1024)+" MB");
                    System.out.println("Packets analyzed since startup: "+listenTCP.TCPConnectionsMade);
                    System.out.println("");

                    break;
                case "show incoming":
                    DisplayIncoming = true;
                    break;
                case "hide incoming":
                    DisplayIncoming = false;
                    break;
                case "start UDP":
                    new Thread(()-> listenUDP.startUDPServer(Port_UDP)).start();
                    break;
                case "stop UDP":
                    boolean success = listenUDP.stopUDPServer();
                    if(success){System.out.println("[APP] UDP server stopped..");break;}
                    System.out.println("[APP] UDP server did not close!");
                default:
                   
                    break;
            }
        }
        
        

        // Start output thread

    }
    
}