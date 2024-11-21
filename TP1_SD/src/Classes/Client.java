package Classes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class Client {

    private static final String SERVER_ADDRESS = "127.0.0.1";
    private static final int SERVER_PORT = 4444;

    public static void main(String[] args) {

        try (Socket socket = new Socket(SERVER_ADDRESS, SERVER_PORT);
             BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
             Scanner scanner = new Scanner(System.in)) {

            System.out.println("Connected to the server!");

            String serverMessage;

            while (true) {

                serverMessage = reader.readLine();

                if (serverMessage == null || serverMessage.contains("Disconnected!")) {
                    System.out.println("Server closed connection.");
                    break;
                }

                System.out.println(serverMessage);

                if (serverMessage.contains(":")) {
                    String userInput = scanner.nextLine();
                    writer.println(userInput);
                }
            }

        } catch (IOException e) {
            System.err.println("Error in client: " + e.getMessage());
        }
    }
}

