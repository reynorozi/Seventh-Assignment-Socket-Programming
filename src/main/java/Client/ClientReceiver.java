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

                if(messageserver.startsWith("MESSAGE;")){

                    String b = messageserver.replace("MESSAGE;","");
                    String[] parts = b.split(",");
                    String username = parts[0];
                    String message = parts[1];
                    System.out.println("[" +username+"]:"  + message);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
