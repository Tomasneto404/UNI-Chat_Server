package Server;

import java.io.*;
import java.net.*;
import java.time.*;
import java.time.format.DateTimeFormatter;

public class Server {

    private final String HIGH_GROUP_FILENAME = "high_group.txt";
    private final String MEDIUM_GROUP_FILENAME = "medium_group.txt";
    private final String LOW_GROUP_FILENAME = "low_group.txt";

    private final String HIGH_MULTICAST_GROUP_ADDRESS = "230.0.0.0";
    private final int HIGH_MULTICAST_GROUP_PORT = 4445;

    private final String MEDIUM_MULTICAST_GROUP_ADDRESS = "231.0.0.0";
    private final int MEDIUM_MULTICAST_GROUP_PORT = 4446;

    private final String LOW_MULTICAST_GROUP_ADDRESS = "232.0.0.0";
    private final int LOW_MULTICAST_GROUP_PORT = 4447;

    private int serverPort;

    public Server(int port) throws IOException {
        this.serverPort = port;
    }

    public void start() throws IOException {

        InetAddress highGroupAddress = InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS);
        InetAddress mediumGroupAddress = InetAddress.getByName(MEDIUM_MULTICAST_GROUP_ADDRESS);
        InetAddress lowGroupAddress = InetAddress.getByName(LOW_MULTICAST_GROUP_ADDRESS);

        try {
            ServerSocket ss = new ServerSocket(serverPort);

            System.out.println("Server connected on port: " + serverPort);

            joinGroup(HIGH_MULTICAST_GROUP_PORT, highGroupAddress, HIGH_GROUP_FILENAME);
            joinGroup(MEDIUM_MULTICAST_GROUP_PORT, mediumGroupAddress, MEDIUM_GROUP_FILENAME);
            joinGroup(LOW_MULTICAST_GROUP_PORT, lowGroupAddress, LOW_GROUP_FILENAME);

            while (true) {
                Socket clientSocket = ss.accept();
                Thread clientThread = new Thread(new ClientHandler(clientSocket));
                clientThread.start();
            }

        } catch (IOException e) {
            System.out.println("Server Error");
        }
    }

    private void joinGroup(int port, InetAddress group, String fileName) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(port);
        try {
            multicastSocket.joinGroup(group);

            Thread receiveThread = new Thread(() -> {
                try {
                    receiveAndSaveMessages(multicastSocket, fileName);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            });
            receiveThread.start();

        } catch (IOException e) {
            multicastSocket.close();
        }
    }

    private void receiveAndSaveMessages(MulticastSocket multicastSocket, String fileName) throws IOException {
        try {
            while (true) {
                byte[] buffer = new byte[256];
                DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                multicastSocket.receive(packet);

                String receivedMessage = new String(packet.getData(), 0, packet.getLength());

                LocalDateTime myDateObj = LocalDateTime.now();
                DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");
                String formattedDate = myDateObj.format(myFormatObj);

                String fullMessage = "<" + formattedDate + ">" + receivedMessage + "\n";
                saveMessageToFile(fullMessage, fileName);
            }
        } catch (IOException e) {
            //System.out.println("Erro ao receber mensagem: " + e.getMessage());
        }
    }

    private void saveMessageToFile(String message, String fileName) throws IOException {
        FileWriter fileWriter = new FileWriter(fileName, true);
        fileWriter.append(message);
        fileWriter.close();
    }


    public static void main(String[] args) {

        try {
            Server server = new Server(4444);
            server.start();
        } catch (IOException e) {
            System.out.println("Server Error");
        }

    }
}
