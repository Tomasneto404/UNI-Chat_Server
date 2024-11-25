package Client;

import Common.User;
import Enums.Rank;
import Exceptions.StopReadingException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

public class UserInterface {

    private final String HIGH_MULTICAST_GROUP_ADDRESS = "230.0.0.0";
    private final int HIGH_MULTICAST_GROUP_PORT = 4445;

    private final String MEDIUM_MULTICAST_GROUP_ADDRESS = "231.0.0.0";
    private final int MEDIUM_MULTICAST_GROUP_PORT = 4446;

    private final String LOW_MULTICAST_GROUP_ADDRESS = "232.0.0.0";
    private final int LOW_MULTICAST_GROUP_PORT = 4447;

    private MulticastSocket multicastSocket;

    private InetAddress highGroup;

    private InetAddress mediumGroup;

    private InetAddress lowGroup;

    private User loggedUser;

    public UserInterface() {
        try {
            this.highGroup = InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS);
            this.mediumGroup = InetAddress.getByName(MEDIUM_MULTICAST_GROUP_ADDRESS);
            this.lowGroup = InetAddress.getByName(LOW_MULTICAST_GROUP_ADDRESS);

        } catch (IOException e) {
            System.err.println("Error initializing multicast: " + e.getMessage());
        }
    }


    public boolean start(Scanner scanner, BufferedReader reader, PrintWriter writer) throws IOException, StopReadingException {
        while (true) {
            System.out.println("\n--- Authentication ---");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("0. Exit");
            System.out.print("Option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    writer.println("regist");
                    handleRegistration(scanner, reader, writer);
                    break;

                case "2":
                    writer.println("login");
                    if (handleLogin(scanner, reader, writer)) {
                        displayMainMenu(scanner, reader, writer);
                    }
                    break;

                case "0":
                    writer.println("exit");
                    throw new StopReadingException("Disconnected from the server.");
                default:
                    System.out.println("Invalid option.");
            }

        }
    }

    public void displayMainMenu(Scanner scanner, BufferedReader reader, PrintWriter writer) throws IOException {
        System.out.println("--- Welcome to the CHAT <" + loggedUser.getName() + "> ---");

        while (true) {
            System.out.println("--- Menu ---");
            System.out.println("1. Channels");
            System.out.println("2. Notifications");
            System.out.println("0. Exit");
            System.out.print("Option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    handleChannels(scanner, reader);
                    break;

                case "2":
                    break;

                case "0":
                    writer.println("exit");
                    throw new StopReadingException("Disconnected from the server.");
                default:
                    System.out.println("Invalid option.");
            }
        }
    }

    private void handleChannels(Scanner scanner, BufferedReader reader) throws IOException {
        String userRank = loggedUser.getRank().toString();

        showGroupOptions(userRank);

        String option = scanner.nextLine();

        if (userRank.equals("HIGH")) {
            handleHighGroup(option, scanner);

        } else if (userRank.equals("MEDIUM")) {
            handleMediumGroup(option, scanner);

        } else if (userRank.equals("LOW")) {
            handleLowGroup(option, scanner);
        }
    }

    private void showGroupOptions(String userRank) {
        System.out.println(" --- Groups --- ");

        if (userRank.equals("HIGH")) {
            System.out.println("1. High");
            System.out.println("2. Medium");
            System.out.println("3. Low");

        } else if (userRank.equals("MEDIUM")) {
            System.out.println("1. Medium");
            System.out.println("2. Low");

        } else if (userRank.equals("LOW")) {
            System.out.println("1. Low");

        }
        System.out.println("0. Back");
        System.out.print("Option: ");
    }

    private void handleHighGroup(String option, Scanner scanner) throws IOException {
        switch (option) {
            case "1":
                joinGroup("HIGH", HIGH_MULTICAST_GROUP_PORT, highGroup, scanner);
                break;
            case "2":
                joinGroup("MEDIUM", MEDIUM_MULTICAST_GROUP_PORT, mediumGroup, scanner);
                break;
            case "3":
                joinGroup("LOW", LOW_MULTICAST_GROUP_PORT, lowGroup, scanner);
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void handleMediumGroup(String option, Scanner scanner) throws IOException {
        switch (option) {
            case "1":
                joinGroup("MEDIUM", MEDIUM_MULTICAST_GROUP_PORT, mediumGroup, scanner);
                break;
            case "2":
                joinGroup("LOW", LOW_MULTICAST_GROUP_PORT, lowGroup, scanner);
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void handleLowGroup(String option, Scanner scanner) throws IOException {
        switch (option) {
            case "1":
                joinGroup("LOW", LOW_MULTICAST_GROUP_PORT, lowGroup, scanner);
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void joinGroup(String groupName, int port, InetAddress group, Scanner scanner) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(port);
        try {
            multicastSocket.joinGroup(group);
            System.out.println("+ " + groupName.toUpperCase() + " GROUP +");

            // Thread para receber mensagens
            Thread receiveThread = new Thread(() -> receiveMessages(multicastSocket));
            receiveThread.start();

            // Enviar mensagens
            while (true) {
                String message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {
                    System.out.println("Leaving chat...");
                    break;
                }
                sendMessage(multicastSocket, message, group, port);
            }

            multicastSocket.leaveGroup(group);
        } finally {
            multicastSocket.close();
        }
    }

    private void receiveMessages(MulticastSocket multicastSocket) {
        try {
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String receivedMessage = new String(packet.getData(), 0, packet.getLength());
                System.out.println(receivedMessage);
            }
        } catch (IOException e) {
            //System.out.println("Erro ao receber mensagem: " + e.getMessage());
        }
    }

    private void sendMessage(MulticastSocket multicastSocket, String message, InetAddress group, int port) throws IOException {
        String fullMessage = "[" + loggedUser.getName() + "] (" + loggedUser.getRank().toString() + "): " + message;
        byte[] buffer = fullMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        multicastSocket.send(packet);
    }

    private boolean handleLogin(Scanner scanner, BufferedReader reader, PrintWriter writer) throws IOException, StopReadingException {
        //Username
        String askUsername = reader.readLine();
        System.out.print(askUsername);
        writer.println(scanner.nextLine());

        //Password
        String askPassword = reader.readLine();
        System.out.print(askPassword);
        writer.println(scanner.nextLine());

        //Get Message
        String serverResponse = reader.readLine();
        System.out.println(serverResponse);

        if (serverResponse.equals("Login completed successfully!")) {
            String userFromServer = reader.readLine();
            if (userFromServer != "None") {
                this.loggedUser = loadUser(userFromServer);
            } else {
                this.loggedUser = null;
            }
            return true;
        }

        return false;
    }

    private void handleRegistration(Scanner scanner, BufferedReader reader, PrintWriter writer) throws IOException {
        //Username
        String askUsername = reader.readLine();
        System.out.print(askUsername);
        writer.println(scanner.nextLine());

        //Rank
        String askRank = reader.readLine();
        System.out.print(askRank);
        writer.println(scanner.nextLine());

        //Password
        String askPassword = reader.readLine();
        System.out.print(askPassword);
        writer.println(scanner.nextLine());

        //Repeat Password
        String askRepeatPassword = reader.readLine();
        System.out.print(askRepeatPassword);
        writer.println(scanner.nextLine());

        //Get Message
        String serverResponse = reader.readLine();
        System.out.println(serverResponse);

    }

    private User loadUser(String userDataCSV) {

        String[] parts = userDataCSV.split(";");
        if (parts.length == 4) {

            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            String password = parts[2];
            Rank rank = Rank.valueOf(parts[3]);

            return new User(id, name, password, rank);
        }

        return null;
    }
}
