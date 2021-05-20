package client;

import java.awt.*;

public class Chat {

    //launch the application

    public  static  void main(String[] args){

        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                try{
                    LoginWindow frame = new LoginWindow();
                    frame.setVisible(true);
                }
                catch (Exception e){
                    e.printStackTrace();
                }
            }
        });

    }

}
