package Client;

import Exceptions.StopReadingException;

import java.io.*;
import java.net.*;
import java.util.Scanner;

import static java.lang.System.exit;


public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private int serverPort = 4444;

    public Client(int serverPort) throws IOException {
        this.serverPort = serverPort;


    }

    public void start() throws IOException {

        try {
            Socket socket = new Socket(SERVER_ADDRESS, serverPort);
            BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            Scanner scanner = new Scanner(System.in);
            UserInterface menu = new UserInterface();

            System.out.println("Connected to the server!");

            while (true) {
                try {
                    menu.start(scanner, reader, writer);
                } catch (StopReadingException e) {
                    exit(0);
                }
            }

        } catch (IOException e) {
            System.err.println("Error in client: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        try {
            Client client = new Client(4444);
            client.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}


