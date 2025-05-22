package Client;


import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class ClientReceiver implements Runnable {

    DataInputStream in;

    public ClientReceiver(DataInputStream in) throws IOException {
        this.in = in;
    }

    @Override
    public void run() {
        try {
            while (true) {

            String messageserver = in.readUTF();

                if(messageserver.startsWith("M;")){

                    String b = messageserver.replace("M;","");
                    String[] parts = b.split(",");
                    String username = parts[0];
                    String message = parts[1];
                    System.out.println("[" +username+"]:"  + message);
                }



                //TODO: Listen for new messages from server
                //TODO: print the  new message in CLI
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
