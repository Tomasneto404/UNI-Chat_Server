package Classes;

import java.io.*;
import java.net.*;

public class Server {

    private final String USERS_FILE = "Users.txt";

    private int port;

    public Server(int port) {
        this.port = port;
    }

    public void start() throws IOException {
        try {
            ServerSocket ss = new ServerSocket(port);

            System.out.println("Server connected on port: " + port);

            while (true) {
                Socket clientSocket = ss.accept();
                System.out.println("Received connection from " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }

        } catch (IOException e) {
            System.out.println("Server Error");
        }
    }


    public static void main(String[] args) {

        Server server = new Server(4444);
        try {
            server.start();
        } catch (IOException e) {
            System.out.println("Server Error");
        }

    }
}
