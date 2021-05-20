package client;

import java.io.IOException;
import java.net.*;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class Client {


    private final String name;
    private final String address;
    private final int port;
    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");

    private DatagramSocket socket;
    private InetAddress ip;

    private Thread send;

    private UUID ID = null;


    public Client(String name, String address, int port){
        this.name = name;
        this.address = address;
        this.port = port;
    }

    public  String getName(){
        return name;
    }
    public String getAddress(){
        return  address;
    }
    public int getPort(){
        return port;
    }
    public void setID(UUID ID) { this.ID = ID; }
    public UUID getID(){
        return ID;
    }

    public DateTimeFormatter getDtf(){
        return dtf;
    }


    protected boolean openConnection(String address){

        try {
            socket = new DatagramSocket();
            ip = InetAddress.getByName(address);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return  false;
        } catch (SocketException e) {
            e.printStackTrace();
            return false;
        }
        return  true;
    }

    protected String receive(){

        byte[] data = new byte[1024];
        DatagramPacket packet = new DatagramPacket(data, data.length);
        try {
            socket.receive(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
        String message = new String(packet.getData());
        return  message;
    }

    protected void send(final byte[] data){

        send = new Thread("Send"){
            public void run(){
                DatagramPacket packet = new DatagramPacket(data, data.length, ip, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        send.start();
    }

    public void close(){
        new Thread("Close") {

            public void  run() {
                synchronized (socket) {
                    socket.close();
                }
            }
        }.start();

    }

}
