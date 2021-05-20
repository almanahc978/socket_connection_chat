package server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.UUID;

public class Server implements Runnable {

    private List<ServerClient> clients = new ArrayList<ServerClient>();
    private List<UUID> clientResponse = new ArrayList<UUID>();

    private DatagramSocket socket;
    private int port;
    private boolean running = false;
    private boolean raw = false;
    private Thread run, manage, send, receive;

    private final int MAX_ATTEMPTS = 5;

    public Server(int port) {
        this.port = port;
        try {
            socket = new DatagramSocket(port);
        } catch (SocketException e) {
            e.printStackTrace();
            return;
        }
        run = new Thread(this, "Server");
        run.start();
    }

    @Override
    public void run() {
        running = true;
        System.out.println("Server started on port " + port);
        manageClients();
        receive();
        Scanner sc = new Scanner(System.in);
        while (running) {
            String text = sc.nextLine();
            if (!text.startsWith("/")) {
                sendToAll("/m/Server: " + text + "/e/");
                continue;
            }
            text = text.substring(1);
            if (text.startsWith("raw")) {
                if(raw) System.out.println("Raw mode off");
                else System.out.println("Raw mode on");
                raw = !raw;
            } else if (text.equals("clients")) {
                System.out.println("Clients: ");
                System.out.println("==========");
                for (int i = 0; i < clients.size(); i++) {
                    ServerClient c = clients.get(i);
                    System.out.println(c.getName() + " (" + c.getID() + ")");
                }
                System.out.println("==========");
            } else if (text.startsWith("kick")) {
                String name = text.split(" ")[1];
                for (int i = 0; i < clients.size(); i++) {
                    ServerClient c = clients.get(i);
                    if (c.getName().equals(name) || c.getID().toString().equals(name)) {
                        disconnect(c.getID(), true);
                        break;
                    }
                }
            }
            else if(text.equals("quit")){
                quit();
            }
            else if(text.equals("help")){
                printHelp();
            }
            else {
                System.out.println("Couldn't resolve the command!");
                printHelp();
            }
        }
        sc.close();
    }

    private void manageClients() {
        manage = new Thread("Manage") {
            @Override
            public void run() {
                while (running) {
                    sendToAll("/i/ping");
                    sendStatus();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    for (int i = 0; i < clients.size(); i++) {
                        ServerClient c = clients.get(i);
                        if (!clientResponse.contains(clients.get(i).getID())) {
                            if (c.getAttempt() >= MAX_ATTEMPTS) {
                                disconnect(c.getID(), false);
                            } else {
                                c.attempt++;
                            }
                        } else {
                            clientResponse.remove(c.getID());
                            c.attempt = 0;
                        }
                    }

                }
            }
        };
        manage.start();
    }

    private void receive() {
        receive = new Thread("Receive") {
            @Override
            public void run() {
                while (running) {
                    byte[] data = new byte[1024];
                    DatagramPacket packet = new DatagramPacket(data, data.length);
                    try {
                        socket.receive(packet);
                    }
                    catch (SocketException e){
                        //blank
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    process(packet);
                }
            }
        };
        receive.start();
    }

    private void send(final byte[] data, InetAddress address, int port) {
        send = new Thread("Send") {
            public void run() {
                DatagramPacket packet = new DatagramPacket(data, data.length, address, port);
                try {
                    socket.send(packet);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        send.start();
    }

    private void send(String message, InetAddress address, int port) {
        message += "/e/";
        send(message.getBytes(), address, port);
    }


    private void sendToAll(String message) {
        if (message.startsWith("/m/")) {
            String text = message.substring(3);
            text = text.split("/e/")[0];
            System.out.println(text);
        }
        if (raw) {
            System.out.println(message);
        }
        for (int i = 0; i < clients.size(); i++) {
            ServerClient client = clients.get(i);
            send(message.getBytes(), client.address, client.port);
        }
    }
    private void sendStatus(){
        if(clients.size() <= 0) return;
        String users = "/u/";
        for(int i = 0; i < clients.size() - 1; i++){
            users += clients.get(i).name + "/n/";
        }
        users += clients.get(clients.size() - 1).name + "/e/";
        sendToAll(users);
    }

    private void process(DatagramPacket packet) {

        String string = new String(packet.getData());

        if (raw) {
            System.out.println(string);
        }

        if (string.startsWith("/c/")) {
            UUID id = UUID.randomUUID();
            String name = string.split("/c/|/e/")[1];
            clients.add(new ServerClient(name, packet.getAddress(), packet.getPort(), id));
            System.out.println(name + ":" + id + "/" + packet.getAddress() + ":" + packet.getPort());
            String ID = "/c/" + id;
            send(ID, packet.getAddress(), packet.getPort());

        } else if (string.startsWith("/d/")) {
            UUID ID = UUID.fromString(string.split("/d/|/e/")[1]);
            disconnect(ID, true);
        } else if (string.startsWith("/m/")) {
            sendToAll(string);
        } else if (string.startsWith("/i/")) {
            clientResponse.add(UUID.fromString(string.split("/i/|/e/")[1]));
        } else {
            System.out.println(string);
        }

    }

    private void disconnect(UUID id, boolean status) {
        ServerClient c = null;
        boolean existed = false;
        for (int i = 0; i < clients.size(); i++) {
            if (clients.get(i).getID().equals(id)) {
                c = clients.get(i);
                clients.remove(i);
                existed = true;
                break;
            }
        }
            if (!existed) return;
            String message = "";
            if (status) {
                message = "Client " + c.getName() + " (" + c.getID() + ") @" + c.address.toString() + ":" + c.port + " has disconnected.";
            } else {
                message = "Client " + c.getName() + "(" + c.getID() + ") @" + c.address.toString() + ":" + c.port + " timed out.";
            }
            System.out.println(message);
        }

        private  void quit(){
           for(int i = 0; i < clients.size(); i++){
              disconnect(clients.get(i).getID(), true);
           }
           running = false;
           socket.close();
        }
        private void printHelp(){
            System.out.println("Available commands: ");
            System.out.println("============================");
            System.out.println("/clients");
            System.out.println("/kick [username or user ID]");
            System.out.println("/raw - enables raw mode");
            System.out.println("/quit - shuts down the server");
            System.out.println("/help - list of commands");
            System.out.println("============================");
            System.out.println();
        }

    }
