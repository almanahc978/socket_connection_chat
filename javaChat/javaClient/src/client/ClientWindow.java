package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.*;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

public class ClientWindow extends JFrame implements Runnable{

    private JPanel contentPane;
    private JTextField txtMessage;
    private JTextArea txtrHistory;
    private  JMenuBar menuBar;
    private JMenu mnFile;
    private JMenuItem mntmOnlineUsers;
    private JMenuItem mntmExit;

    private OnlineUsers users;

    private Client client;

    private Thread run, listen;

    private boolean running;


    public ClientWindow(String name, String address, int port){

        client = new Client(name, address, port);

        boolean connect = client.openConnection(address);

        if(!connect){
            System.err.println("Connection failed!");
            console("Connection failed!");
        }

        createWindow();

        String connection = "/c/" + name + "/e/";
        client.send(connection.getBytes());

        LocalDateTime loginTime = LocalDateTime.now();
        console("Attempting a connection to " + address + ":" + port + ", user: " + name + " at  " + client.getDtf().format(loginTime));
        users = new OnlineUsers();
        running = true;
        run = new Thread(this, "Running");
        run.start();
    }


    private void createWindow(){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                String disconnect = "/d/" + client.getID() + "/e/";
                send(disconnect,false);
                client.close();
                running = false;
            }
        });

        setSize(880,550);
        setLocationRelativeTo(null);
        setTitle("Client");
        setVisible(true);

        menuBar = new JMenuBar();
        setJMenuBar(menuBar);

        mnFile = new JMenu("File");
        menuBar.add(mnFile);

        mntmOnlineUsers  = new JMenuItem("Online Users");
        mnFile.add(mntmOnlineUsers);
        mntmOnlineUsers.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                users.setVisible(true);
            }
        });

        mntmExit = new JMenuItem("Exit");
        mnFile.add(mntmExit);
        mnFile.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String disconnect = "/d/" + client.getID() + "/e/";
                send(disconnect,false);
                client.close();
                running = false;
            }
        });

        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5,5,5,5));
        setContentPane(contentPane);

        GridBagLayout gbl_contentPane = new GridBagLayout();
        gbl_contentPane.columnWidths = new int[]{28,815,30,7}; //SUM = 880
        gbl_contentPane.rowHeights = new int[]{25,485,40}; //SUM = 550
        contentPane.setLayout(gbl_contentPane);

        txtrHistory = new JTextArea();
        txtrHistory.setEditable(false);
        JScrollPane scroll = new JScrollPane(txtrHistory);
        GridBagConstraints scrollConstraints = new GridBagConstraints();
        scrollConstraints.fill = GridBagConstraints.BOTH;
        scrollConstraints.gridx = 0;
        scrollConstraints.gridy = 0;
        scrollConstraints.gridwidth = 3;
        scrollConstraints.gridheight = 2;
        scrollConstraints.weightx = 1;
        scrollConstraints.weighty = 1;
        scrollConstraints.insets = new Insets(20,20,20,20);
        contentPane.add(scroll, scrollConstraints);

        JButton btnSend = new JButton("Send");
        btnSend.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                send(txtMessage.getText(),true);
            }
        });
        GridBagConstraints gbc_btnSend = new GridBagConstraints();
        gbc_btnSend.insets = new Insets(0,0,0,5);
        gbc_btnSend.gridx = 2;
        gbc_btnSend.gridy = 2;
        gbc_btnSend.weightx = 0;
        gbc_btnSend.weighty = 0;
        contentPane.add(btnSend, gbc_btnSend);

        txtMessage = new JTextField();
        txtMessage.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() ==  KeyEvent.VK_ENTER){

                    send(txtMessage.getText(),true);
                }
            }
        });
        GridBagConstraints gbc_txtMessage = new GridBagConstraints();
        gbc_txtMessage.insets = new Insets(0,0,0,5);
        gbc_txtMessage.fill = GridBagConstraints.HORIZONTAL;
        gbc_txtMessage.gridx = 0;
        gbc_txtMessage.gridy = 2;
        gbc_txtMessage.gridwidth = 2;
        gbc_txtMessage.weightx = 1;
        gbc_txtMessage.weighty = 0;
        gbc_txtMessage.insets = new Insets(5,20,5,0);
        contentPane.add(txtMessage, gbc_txtMessage);
        txtMessage.setColumns(10);
        txtMessage.requestFocusInWindow();


    }

    public void run(){
        listen();
    }

    private  void send(String message, boolean text){

        if(message.equals("")) return;
        if(text) {
            message= "/m/" + client.getName() + ":" + message + "/e/";
            txtMessage.setText(null);
        }
        client.send(message.getBytes());

    }

    public void listen(){
        listen = new Thread("Listen"){
            public void run() {
                while (running) {
                    String message = client.receive();
                    if(message.startsWith("/c/")){
                        client.setID(UUID.fromString(message.split("/c/|/e/")[1]));
                        console("Successfully connected to the server! ID: " + client.getID());
                    }
                    else if(message.startsWith("/m/")){
                        String text = message.substring(3).split("/e/")[0];
                        console(text);
                    }
                    else if(message.startsWith("/i/")){
                        String text = "/i/" + client.getID() + "/e/";
                        send(text, false);
                    }
                    else if(message.startsWith("/u/")) {
                        String[] u = message.split("/u/|/n/|/e/");
                        users.update(Arrays.copyOfRange(u, 1, u.length - 1));
                    }
                }
            }
        };
        listen.start();
    }


    public void console(String message){

        txtrHistory.append(message+"\n");
    }
}
