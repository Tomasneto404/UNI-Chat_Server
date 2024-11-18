package Classes;

import java.io.*;
import java.net.*;

public class ClientHandler implements Runnable {
    private Socket clientSocket;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    @Override
    public void run() {
        try {

            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            UserInterface menu = new UserInterface();

            menu.displayMainMenu(reader, writer);
            clientSocket.close();

        } catch (IOException e) {
            System.err.println("Error: Couldnt connect with the client. " + e.getMessage());
        }
    }

}
