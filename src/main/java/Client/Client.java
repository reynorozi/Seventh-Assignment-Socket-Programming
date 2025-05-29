package Client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private Socket socket;
    static DataInputStream in;
    static DataOutputStream out;
    private static String username;

    public static void main(String[] args) throws Exception {

        try (Socket socket = new Socket("localhost", 12345)) {



            Scanner scanner = new Scanner(System.in);

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            // --- LOGIN PHASE ---
            System.out.println("===== Welcome to CS Music Room =====");


            boolean loggedIn = false;
            while (!loggedIn) {
                System.out.print("Username: ");
                username = scanner.nextLine();
                System.out.print("Password: ");
                String password = scanner.nextLine();


                sendLoginRequest(username, password);


                String message = in.readUTF();
                System.out.println(message);

                if (message.startsWith("login:")) {

                    String succes = message.split(":")[1];
                    if (succes.equals("true")) {

                        loggedIn = true;

                    } else if (succes.equals("false")) {

                        loggedIn = false;
                    }
                }
            }
            // --- ACTION MENU LOOP ---
            while (true) {
                printMenu();
                System.out.print("Enter choice: ");
                String choice = scanner.nextLine();

                switch (choice) {
                    case "1" -> enterChat(scanner);
                    case "2" -> uploadFile(scanner);
                    case "3" -> requestDownload(scanner);
                    case "0" -> {
                        System.out.println("Exiting...");
                        return;
                    }
                    default -> System.out.println("Invalid choice.");
                }
            }

        } catch (IOException e) {
            System.out.println("Connection error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private static void printMenu() {
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. Enter chat box");
        System.out.println("2. Upload a file");
        System.out.println("3. Download a file");
        System.out.println("0. Exit");
    }

    private static void sendLoginRequest(String username, String password) throws IOException {


        out.writeUTF("login,"+ username + "," + password);
        out.flush();

    }
    private static void enterChat(Scanner scanner) throws IOException {
        System.out.print("You have entered the chat \n");

        ClientReceiver CR = new ClientReceiver(in);
        Thread t = new Thread(CR);
        t.start();



        String message_string = "";
        while (!message_string.equalsIgnoreCase("/exit")){
            message_string = scanner.nextLine();

            if (!message_string.equalsIgnoreCase("/exit")){

                sendChatMessage(message_string);
            }
            else if (message_string.equalsIgnoreCase("/exit")){

                out.writeUTF("exit");
            }
        }
    }

    private static void sendChatMessage(String message_to_send) throws IOException {

        out.writeUTF("MESSAGE," + username +"," + message_to_send);
    }

    private static void uploadFile(Scanner scanner) throws IOException {

        String filepath = "src/main/resources/Client/" + username;
        File folder = new File(filepath);
        if(!folder.exists()){
            folder.mkdir();
        }
        File[] files = folder.listFiles();
        if (files == null || files.length == 0) {
            System.out.println("No files to upload.");
            return;
        }

        // Show available files
        System.out.println("Select a file to upload:");
        for (int i = 0; i < files.length; i++) {
            System.out.println((i + 1) + ". " + files[i].getName());
        }

        System.out.print("Enter file number: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= files.length) {
            System.out.println("Invalid choice.");
            return;
        }


        out.writeUTF("UPLOAD," + files[choice].getName() + "," + files[choice].length());
        out.flush();

        FileInputStream fis = new FileInputStream(files[choice]);
        byte[] buffer = new byte[4096];
        int bytesRead;
        while ((bytesRead = fis.read(buffer)) != -1) {
            out.write(buffer, 0, bytesRead);
        }
            out.flush();


    }

    private static void requestDownload(Scanner scanner) throws IOException {

        out.writeUTF("DOWNLOAD");
        String fileL = in.readUTF();

        String filename = fileL.replace("SERVERFILE|","");
        String[] filelist = filename.split(",");


        if (filelist == null || filelist.length == 0) {
            System.out.println("No files to download.");
            return;
        }


        System.out.println("Select a file to download:");

        for(int i = 0; i < filelist.length; i++){

            System.out.println( (i+1) + "." +  filelist[i]);
        }

        System.out.print("Enter file number: ");
        int choice;
        try {
            choice = Integer.parseInt(scanner.nextLine()) - 1;
        } catch (NumberFormatException e) {
            System.out.println("Invalid input.");
            return;
        }

        if (choice < 0 || choice >= filelist.length) {
            System.out.println("Invalid choice.");
           return;
        }


        out.writeUTF("AskFile," + filelist[choice]);



        long fileSize = in.readLong();

        File outFile = new File("src/main/resources/Client/" + username + "/" + filelist[choice]);
        FileOutputStream fileOutputStream = new FileOutputStream(outFile);

        byte[] buffer = new byte[4096];
        int bytesRead;
        long totalRead = 0;

        while (totalRead < fileSize && (bytesRead = in.read(buffer, 0, (int)Math.min(buffer.length, fileSize - totalRead))) != -1) {
            fileOutputStream.write(buffer, 0, bytesRead);
            totalRead += bytesRead;
        }

        fileOutputStream.close();
        System.out.println("File received.");

    }
}
