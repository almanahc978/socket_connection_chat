package client;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

public class LoginWindow extends JFrame {

    private final JPanel contentPane;
    private final JTextField txtName;
    private final JTextField txtAddress;
    private final JTextField txtPort;
    private final JButton btnLogin;

    public LoginWindow(){

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }


        setResizable(false);
        setTitle("Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(300,380);
        setLocationRelativeTo(null);
        contentPane = new JPanel();
        contentPane.setBorder(new EmptyBorder(5,5,5,5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        txtName =  new JTextField();
        txtName.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    txtAddress.requestFocusInWindow();
                }
            }
        });
        txtName.setBounds(67,50,165,28);
        contentPane.add(txtName);
        txtName.setColumns(10);

        JLabel lblName = new JLabel("Name:");
        lblName.setBounds(127,34,45,16);
        contentPane.add(lblName);

        txtAddress = new JTextField();
        txtAddress.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    txtPort.requestFocusInWindow();
                }
            }
        });
        txtAddress.setBounds(67,116,165,28);
        contentPane.add(txtAddress);
        txtAddress.setColumns(10);

        JLabel lblAddress = new JLabel("IP Address:");
        lblAddress.setBounds(111,96,77,16);
        contentPane.add(lblAddress);

        txtPort = new JTextField();
        txtPort.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if(e.getKeyCode() == KeyEvent.VK_ENTER){
                    String name = txtName.getText();
                    String address = txtAddress.getText();
                    int port = Integer.parseInt(txtPort.getText());
                    login(name,address,port);
                }
            }
        });
        txtPort.setBounds(67,191,165,28);
        contentPane.add(txtPort);
        txtPort.setColumns(10);

        JLabel lblPort = new JLabel("Port:");
        lblPort.setBounds(133,171,34,16);
        contentPane.add(lblPort);

        JLabel lbleg = new JLabel("(eg. 192.168.0.2)");
        lbleg.setBounds(94,142,112,16);
        contentPane.add(lbleg);

        JLabel lbleg_1 = new  JLabel("(eg. 8192)");
        lbleg_1.setBounds(116,218,68,16);
        contentPane.add(lbleg_1);

        btnLogin = new JButton("Login");
        btnLogin.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String name = txtName.getText();
                String address = txtAddress.getText();
                int port = Integer.parseInt(txtPort.getText());
                login(name,address,port);
            }
        });
        btnLogin.setBounds(91,270,117,29);
        contentPane.add(btnLogin);

    }

    private  void login(String name, String address, int port){

        dispose();
        //System.out.println(name+", "+address+", "+port);
        new ClientWindow(name, address, port);

    }


}
