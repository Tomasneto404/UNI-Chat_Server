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

            displayMenu(reader, writer);
            clientSocket.close();

        } catch (IOException e) {
            System.err.println("Error: Couldnt connect with the client. " + e.getMessage());
        }
    }

    private void displayMenu(BufferedReader reader, PrintWriter writer) throws IOException {
        writer.println("\n--- Welcome to the CHAT ---");

        while (true) {

            writer.println("\n--- Menu ---");
            writer.println("1. Send Message");
            writer.println("2. Channels");
            writer.println("3. Messages");
            writer.println("4. Exit");
            writer.println("Option > ");

            String option = reader.readLine();

            if (option == null) {
                break;
            }

            switch (option) {
                case "1":
                    sendMessage(reader, writer);
                    break;
                case "2":
                    joinChannel(reader, writer);
                    break;
                case "3":
                    viewMessages(writer);
                    break;
                case "4":
                    writer.println("Disconnected!");
                    return;
                default:
                    writer.println("Invalid option!.");
            }
        }
    }

    private boolean displayAuthMenu(BufferedReader reader, PrintWriter writer) throws IOException {
        writer.println("\n--- Authentication ---");

        while (true) {

            writer.println("\n--- Menu ---");
            writer.println("1. Register");
            writer.println("2. Login");
            writer.println("3. Exit");
            writer.println("Option > ");

            String option = reader.readLine();

            if (option == null) {
                break;
            }

            switch (option) {
                case "1":
                    login(reader, writer);
                    break;
                case "2":
                    joinChannel(reader, writer);
                    break;
                case "3":
                    writer.println("Disconnected!");
                    return false;

                default:
                    writer.println("Invalid option!.");
            }
        }
    }

    private void sendMessage(BufferedReader reader, PrintWriter writer) throws IOException {
        writer.println("Digite o ID do destinatário: ");
        String recipientId = reader.readLine();

        writer.println("Digite a mensagem: ");
        String message = reader.readLine();

        // Simula o envio da mensagem (a lógica real pode salvar ou enviar para outro cliente)
        writer.println("Mensagem enviada para " + recipientId + ": " + message);
    }

    private void joinChannel(BufferedReader reader, PrintWriter writer) throws IOException {
        writer.println("Digite o nome do canal: ");
        String channelName = reader.readLine();

        // Simula a entrada no canal
        writer.println("Você entrou no canal: " + channelName);
    }

    private void viewMessages(PrintWriter writer) {
        // Simula a exibição de mensagens (a lógica real puxaria mensagens de um banco de dados ou memória)
        writer.println("\n--- Suas Mensagens ---");
        writer.println("1. Mensagem de exemplo 1");
        writer.println("2. Mensagem de exemplo 2");
        writer.println("END_OF_MESSAGES");
    }

    private void login(BufferedReader reader, PrintWriter writer) throws IOException {
        writer.println("Username ");
        String username = reader.readLine();
        writer.println("Password ");
        String password = reader.readLine();

    }
}
