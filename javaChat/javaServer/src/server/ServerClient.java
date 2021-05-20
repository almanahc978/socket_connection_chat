package server;

import java.net.InetAddress;
import java.util.UUID;

public class ServerClient {

    public String name;
    public InetAddress address;
    public int port;
    private final UUID ID;
    public int attempt = 0;

    public UUID getID(){
        return ID;
    }
    public String getName(){
        return  name;
    }
    public int getAttempt(){
        return attempt;
    }

    public  ServerClient(String name, InetAddress address, int port, final UUID ID){
        this.name = name;
        this.address = address;
        this.port = port;
        this.ID = ID;

    }

}
