package Server;

import javax.imageio.stream.FileImageInputStream;
import java.io.IOException;
import java.io.*;
import java.net.Socket;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private Socket socket;
    private DataInputStream in;
    private DataOutputStream out;
    private String username;
    private static File[] files;
    private boolean isinthechat  = true;


    public ClientHandler(Socket socket ) {
        this.socket = socket;

    }

    @Override
    public void run() {
        try {

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
    

            while (true) {

                String request = in.readUTF();

                if (request.startsWith("login,")) {

                    String[] parts = request.split(",");

                    if (parts.length >= 3) {
                        username = parts[1];
                        String pass = parts[2];
                        handleLogin(username, pass);
                    }
                }
                else if (request.startsWith("MESSAGE,")){


                    String message = request.replace("MESSAGE,","");
                    System.out.println(message);
                    broadcast(message);
                }
//                UPLOAD,FILENAME,FILELENGHT
               else if (request.startsWith("UPLOAD,")) {
                    System.out.println(request.replace("UPLOAD,",""));
                    String[] parts = request.split(",");
                    String filename = parts[1];
                    long length = Long.parseLong(parts[2]);
                    receiveFile(filename,length);
                }
                else if(request.startsWith("DOWNLOAD")){
                    sendFileList();

                }
                else if (request.startsWith("AskFile,")) {
                    String file = request.replace("AskFile,","");
                    sendFile(file);
                }
                else if (request.startsWith("exit")) {
                    isinthechat = false;
                }

            }
        }
        catch (Exception e) {

            e.printStackTrace();
        }
    }



    private void sendMessage(String msg) throws IOException {

        out.writeUTF("MESSAGE;"+ msg);

    }
    private void broadcast(String msg) throws IOException {

        System.out.println("["+username+"]:"  + msg);
        if(!isinthechat) {
            isinthechat = true;
        }

        for (ClientHandler client : Server.clients) {
            if (!client.username.equals(username) && client.isinthechat) {
                client.sendMessage(msg);
            }
        }
    }

    private void sendFileList() throws IOException {
         String file = "src/main/resources/Server/Files";
         File folder = new File(file);
         files = folder.listFiles();
        StringBuilder filename = new StringBuilder("SERVERFILE|");
        for (int i = 0; i < files.length; i++) {
            filename.append(files[i].getName());
            if (i != files.length - 1) filename.append(",");
        }
        out.writeUTF(filename.toString());

        // TODO: List all files in the server directory
        // TODO: Send a message containing file names as a comma-separated string
    }
    private void sendFile(String fileName) throws IOException {

        File selectedfile = null;
        for (int i = 0; i < files.length; i++) {
            if (files[i].getName().equals(fileName)) {

                out.writeLong( files[i].length());

                selectedfile = files[i];

            }
        }

        FileInputStream file = new  FileInputStream(selectedfile);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = file.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
        out.flush();


        // TODO: Send file name and size to client
        // TODO: Send file content as raw bytes
    }
    private void receiveFile(String filename, long fileLength) throws IOException {

        byte[] buffer = new byte[4096];
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        int bytesRead = 0;
        int totalRead = 0;

        while (totalRead < fileLength &&
                (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, fileLength - totalRead))) != -1) {
            baos.write(buffer, 0, bytesRead);
            totalRead += bytesRead;
        }
        byte[] fileData = baos.toByteArray();
        baos.close();
        saveUploadedFile(filename, fileData);

    }
    private void saveUploadedFile(String filename, byte[] data) throws IOException {
        File file = new  File ("src/main/resources/Server/Files/"+filename);
        FileOutputStream filee = new FileOutputStream(file);
        filee.write(data);
        filee.close();
    }

    private void handleLogin(String username, String password) throws IOException, ClassNotFoundException {

        boolean login = Server.authenticate(username, password);
        if(login){
            out.writeUTF("login:true");
        }
        else if(!login){
            out.writeUTF("login:false");
        }
    }

}
