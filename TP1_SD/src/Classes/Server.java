package Classes;

import java.io.*;
import java.net.*;

public class Server {

    private final String USERS_FILE = "Users.txt";

    private final String HIGH_MULTICAST_GROUP_ADDRESS = "230.0.0.0";
    private final int HIGH_MULTICAST_GROUP_PORT = 4445;

    private final String MEDIUM_MULTICAST_GROUP_ADDRESS = "231.0.0.0";
    private final int MEDIUM_MULTICAST_GROUP_PORT = 4446;

    private final String LOW_MULTICAST_GROUP_ADDRESS = "232.0.0.0";
    private final int LOW_MULTICAST_GROUP_PORT = 4447;

    private int serverPort;

    public Server(int port) {
        this.serverPort = port;
    }

    public void start() throws IOException {

        try {
            ServerSocket ss = new ServerSocket(serverPort);

            MulticastSocket highGroupSocket = new MulticastSocket(HIGH_MULTICAST_GROUP_PORT);
            highGroupSocket.joinGroup(InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS));

            System.out.println("Server connected on port: " + serverPort);

            while (true) {
                Socket clientSocket = ss.accept();
                System.out.println("Received connection from " + clientSocket.getInetAddress());

                Thread clientThread = new Thread(new ClientHandler(clientSocket, USERS_FILE));
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
