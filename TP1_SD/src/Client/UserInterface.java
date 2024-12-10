package Client;

import Common.User;
import Enums.OperationType;
import Enums.Rank;
import Exceptions.InvalidOperationFormatException;
import Exceptions.StopReadingException;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.util.Scanner;

/**
 * Classe responsável por gerir a interface do utilizador e comunicar com o
 * servidor e grupos multicast.
 * Fornece métodos para autenticação, navegação no menu principal e interação
 * com grupos de chat.
 */
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

    /**
     * Construtor da classe. Inicializa os grupos multicast.
     */
    public UserInterface() {
        try {
            this.highGroup = InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS);
            this.mediumGroup = InetAddress.getByName(MEDIUM_MULTICAST_GROUP_ADDRESS);
            this.lowGroup = InetAddress.getByName(LOW_MULTICAST_GROUP_ADDRESS);

        } catch (IOException e) {
            System.err.println("Error initializing multicast: " + e.getMessage());
        }
    }

    /**
     * Inicia o processo de autenticação e navegação na interface.
     * 
     * @param scanner Entrada do utilizador.
     * @param reader  Leitura de mensagens do servidor.
     * @param writer  Escrita de mensagens para o servidor.
     * @return Verdadeiro se o processo iniciar corretamente.
     * @throws IOException          Em caso de erros de comunicação.
     * @throws StopReadingException Para encerrar a aplicação.
     */
    public boolean start(Scanner scanner, BufferedReader reader, PrintWriter writer)
            throws IOException, StopReadingException {
        while (true) {
            cleanBuffer(reader);
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

    /**
     * Mostra o menu principal após o login bem sucedido.
     * 
     * @param scanner Entrada do utilizador.
     * @param reader  Leitura de mensagens do servidor.
     * @param writer  Escrita de mensagens para o servidor.
     * @throws IOException Em caso de erros de comunicação.
     */
    public void displayMainMenu(Scanner scanner, BufferedReader reader, PrintWriter writer) throws IOException {
        System.out.println("--- Welcome to the CHAT <" + loggedUser.getName() + "> ---");

        while (true) {
            cleanBuffer(reader);
            System.out.println("--- Menu ---");
            System.out.println("1. Channels");
            System.out.println("2. Private Messages");
            System.out.println("3. Notifications");
            System.out.println("0. Exit");
            System.out.print("Option: ");

            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    handleChannels(scanner, reader, writer);
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

    /**
     * Gere a navegação nos canais de chat baseados no nível do utilizador.
     */

    private void handleChannels(Scanner scanner, BufferedReader reader, PrintWriter writer) throws IOException {
        String userRank = loggedUser.getRank().toString();

        showGroupOptions(userRank);

        String option = scanner.nextLine();

        if (userRank.equals("HIGH")) {
            handleHighGroup(option, scanner, writer, reader);

        } else if (userRank.equals("MEDIUM")) {
            handleMediumGroup(option, scanner, writer, reader);

        } else if (userRank.equals("LOW")) {
            handleLowGroup(option, scanner, writer, reader);
        }
    }

    /**
     * Mostra opções de grupos disponíveis para o nível do utilizador.
     */
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

    private void handleHighGroup(String option, Scanner scanner, PrintWriter writer, BufferedReader reader)
            throws IOException {
        switch (option) {
            case "1":
                joinGroup("HIGH", HIGH_MULTICAST_GROUP_PORT, highGroup, scanner, writer, reader);
                break;
            case "2":
                joinGroup("MEDIUM", MEDIUM_MULTICAST_GROUP_PORT, mediumGroup, scanner, writer, reader);
                break;
            case "3":
                joinGroup("LOW", LOW_MULTICAST_GROUP_PORT, lowGroup, scanner, writer, reader);
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void handleMediumGroup(String option, Scanner scanner, PrintWriter writer, BufferedReader reader)
            throws IOException {
        switch (option) {
            case "1":
                joinGroup("MEDIUM", MEDIUM_MULTICAST_GROUP_PORT, mediumGroup, scanner, writer, reader);
                break;
            case "2":
                joinGroup("LOW", LOW_MULTICAST_GROUP_PORT, lowGroup, scanner, writer, reader);
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    private void handleLowGroup(String option, Scanner scanner, PrintWriter writer, BufferedReader reader)
            throws IOException {
        switch (option) {
            case "1":
                joinGroup("LOW", LOW_MULTICAST_GROUP_PORT, lowGroup, scanner, writer, reader);
                break;
            case "0":
                break;
            default:
                System.out.println("Invalid option.");
        }
    }

    /**
     * Entra em um grupo multicast, permitindo que o utilizador envie e receba
     * mensagens.
     * 
     * @param groupName Nome do grupo.
     * @param port      Porta multicast.
     * @param group     Endereço do grupo.
     * @param scanner   Entrada do utilizador.
     * @param writer    Escrita de mensagens para o servidor.
     * @param reader    Leitura de mensagens do servidor.
     * @throws IOException Em caso de erros de comunicação.
     */
    private void joinGroup(String groupName, int port, InetAddress group, Scanner scanner, PrintWriter writer,
            BufferedReader reader) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(port);

        try {
            multicastSocket.joinGroup(group);
            System.out.println("+ " + groupName.toUpperCase() + " GROUP +");

            // Receber historico de mensagens
            getMessagesFromServer(groupName, writer, reader);
            cleanBuffer(reader);

            // Thread para receber mensagens
            Thread receiveThread = new Thread(new Runnable() {
                @Override
                public void run() {
                    receiveMessages(multicastSocket);
                }
            });

            receiveThread.start();

            // Enviar mensagens
            while (true) {
                String message = scanner.nextLine();
                if ("exit".equalsIgnoreCase(message)) {

                    System.out.println("Leaving chat...");
                    break;

                } else if (message.startsWith("/op_evac:")) {

                    if (loggedUser.getRank().equals(Rank.HIGH)) {

                        System.out.println("\n<Operation request started>\n");
                        String newMessage = messageExtractor("/op_evac:", message);
                        startOperation(writer, newMessage, OperationType.EVACUATION);

                    } else {
                        System.out.println("\n<No permissions for this operation>\n");
                    }

                } else if (message.startsWith("/op_ec:")) {

                    if (loggedUser.getRank().equals(Rank.HIGH) || loggedUser.getRank().equals(Rank.MEDIUM)) {
                        System.out.println("\n<Operation request started>\n");
                        String newMessage = messageExtractor("/op_ec:", message);
                        startOperation(writer, newMessage, OperationType.EMERGENCY_COMUNICATION);
                    } else {
                        System.out.println("\n<No permissions for this operation>\n");
                    }

                } else if (message.startsWith("/op_rd:")) {

                    System.out.println("\n<Operation request started>\n");
                    String newMessage = messageExtractor("/op_rd:", message);
                    startOperation(writer, newMessage, OperationType.RESOURCES_DISTRIBUTION);

                } else if (message.startsWith("/approve:")) {

                    int code = Integer.valueOf(messageExtractor("/approve:", message));
                    approveOperation(writer, code);
                    System.out.println(reader.readLine()); // Notificação do servidor

                } else {
                    sendMessage(multicastSocket, message, group, port);
                }
            }

            multicastSocket.leaveGroup(group);
        } finally {
            multicastSocket.close();
        }
    }

    /**
     * Aprova uma operação pendente no servidor com base em um código fornecido.
     *
     * @param writer Escrita de mensagens para o servidor.
     * @param code   Código da operação a ser aprovada.
     */
    private void approveOperation(PrintWriter writer, int code) {
        writer.println("approve");
        writer.println(code);
    }

    /**
     * Inicia uma operação especial no servidor.
     *
     * @param writer Escrita de mensagens para o servidor.
     * @param msg    Mensagem associada à operação.
     * @param opType Tipo de operação a ser iniciada.
     */
    private void startOperation(PrintWriter writer, String msg, OperationType opType) {

        String code="";

        switch(opType){case OperationType.EVACUATION->code="opEvac";case OperationType.EMERGENCY_COMUNICATION->code="opEmergency";case OperationType.RESOURCES_DISTRIBUTION->code="opResources";}

        writer.println(code);writer.println(msg);
    }

    /**
     * Extrai o conteúdo de uma mensagem baseado em um prefixo específico.
     *
     * @param prefix  Prefixo indicando o tipo de mensagem.
     * @param message Mensagem completa recebida.
     * @return Conteúdo da mensagem após o prefixo.
     */
    private String messageExtractor(String prefix, String message) {
        if (message.contains(prefix)) {
            int startIndex = message.indexOf(prefix) + prefix.length();
            return message.substring(startIndex).trim();
        }
        throw new InvalidOperationFormatException("Invalid format: " + prefix);
    }

    /**
     * Recebe mensagens de um grupo multicast em tempo real.
     *
     * @param multicastSocket Socket multicast para o grupo.
     */
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
            // System.out.println("Erro ao receber mensagem: " + e.getMessage());
        }
    }

    /**
     * Envia uma mensagem para um grupo multicast.
     */
    private void sendMessage(MulticastSocket multicastSocket, String message, InetAddress group, int port)
            throws IOException {
        String fullMessage = "[" + loggedUser.getName() + "] (" + loggedUser.getRank().toString() + "): " + message;
        byte[] buffer = fullMessage.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        multicastSocket.send(packet);
    }

    /**
     * Realiza o login do utilizador no servidor.
     *
     * @param scanner Entrada do utilizador.
     * @param reader  Leitura de mensagens do servidor.
     * @param writer  Escrita de mensagens para o servidor.
     * @return True se o login for bem-sucedido, False caso contrário.
     * @throws IOException          Em caso de erros de comunicação.
     * @throws StopReadingException Para encerrar a aplicação em caso de falha.
     */
    private boolean handleLogin(Scanner scanner, BufferedReader reader, PrintWriter writer)
            throws IOException, StopReadingException {
        // Username
        String askUsername = reader.readLine();
        System.out.print(askUsername);
        writer.println(scanner.nextLine());

        // Password
        String askPassword = reader.readLine();
        System.out.print(askPassword);
        writer.println(scanner.nextLine());

        // Get Message
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

    /**
     * Realiza o registro de um novo utilizador no servidor.
     *
     * @param scanner Entrada do utilizador.
     * @param reader  Leitura de mensagens do servidor.
     * @param writer  Escrita de mensagens para o servidor.
     * @throws IOException Em caso de erros de comunicação.
     */
    private void handleRegistration(Scanner scanner, BufferedReader reader, PrintWriter writer) throws IOException {
        // Username
        String askUsername = reader.readLine();
        System.out.print(askUsername);
        writer.println(scanner.nextLine());

        // Rank
        String askRank = reader.readLine();
        System.out.print(askRank);
        writer.println(scanner.nextLine());

        // Password
        String askPassword = reader.readLine();
        System.out.print(askPassword);
        writer.println(scanner.nextLine());

        // Repeat Password
        String askRepeatPassword = reader.readLine();
        System.out.print(askRepeatPassword);
        writer.println(scanner.nextLine());

        // Get Message
        String serverResponse = reader.readLine();
        System.out.println(serverResponse);
    }

    /**
     * Carrega as informações do utilizador a partir de uma string CSV.
     *
     * @param userDataCSV Dados do utilizador recebidos no formato CSV.
     * @return Objeto User preenchido ou null em caso de erro.
     */
    private User loadUser(String userDataCSV) {

        String[] parts = userDataCSV.split(";");
        if (parts.length == 5) {

            int id = Integer.parseInt(parts[0]);
            String name = parts[1];
            String password = parts[2];
            Rank rank = Rank.valueOf(parts[3]);
            Boolean status = Boolean.parseBoolean(parts[4]);

            return new User(id, name, password, rank, status);
        }

        return null;
    }

    /**
     * Limpa o buffer de leitura, garantindo que todas as mensagens pendentes sejam
     * descartadas.
     *
     * @param reader Leitor de mensagens do servidor.
     * @throws IOException Em caso de erros de comunicação.
     */
    private void cleanBuffer(BufferedReader reader) throws IOException {
        while (reader.ready()) {
            reader.readLine();
        }
    }

    /**
     * Obtém mensagens anteriores do servidor para um grupo específico.
     *
     * @param groupName Nome do grupo.
     * @param writer    Escrita de mensagens para o servidor.
     * @param reader    Leitura de mensagens do servidor.
     */
    private void getMessagesFromServer(String groupName, PrintWriter writer, BufferedReader reader) {
        writer.println("messagesFromGroup:" + groupName);
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                if ("END_OF_MESSAGES".equals(line)) {
                    break;
                }
                System.out.println(line);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
