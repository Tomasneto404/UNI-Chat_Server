package Server;

import Common.Operation;
import Common.User;
import Enums.OperationStatus;
import Enums.OperationType;
import Enums.Rank;
import Exceptions.InvalidUserException;
import Exceptions.RejectedOperationException;

import java.io.*;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/***
 * This class is ....
 */
public class ClientHandler implements Runnable {

    private final String highGroupMessagesFile;
    private final String mediumGroupMessagesFile;
    private final String lowGroupMessagesFile;

    private final String HIGH_MULTICAST_GROUP_ADDRESS = "230.0.0.0";
    private final int HIGH_MULTICAST_GROUP_PORT = 4445;

    private final String MEDIUM_MULTICAST_GROUP_ADDRESS = "231.0.0.0";
    private final int MEDIUM_MULTICAST_GROUP_PORT = 4446;

    private final String LOW_MULTICAST_GROUP_ADDRESS = "232.0.0.0";
    private final int LOW_MULTICAST_GROUP_PORT = 4447;

    private final Socket clientSocket;

    private String usersFile;
    private String operationsFile = "operations.csv";

    private List<User> users;
    private List<Operation> operations;

    private User loggedUser;


    public ClientHandler(Socket clientSocket, String usersFile, String highGroupFile, String mediumGroupFile, String lowGroupFile) {
        this.clientSocket = clientSocket;
        this.users = new ArrayList<>();
        this.operations = new ArrayList<>();
        this.usersFile = usersFile;
        this.highGroupMessagesFile = highGroupFile;
        this.mediumGroupMessagesFile = mediumGroupFile;
        this.lowGroupMessagesFile = lowGroupFile;
    }

    @Override
    public synchronized void run() {

        try {

            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " connected!");

            while (true) {
                loadUsersFromFile();
                loadOperationsFromFile();
                String input = reader.readLine();

                if (input == null) {
                    System.out.println("Client disconnected.");
                    break;
                }

                switch (input) {

                    case "regist":
                        loadUsersFromFile();
                        System.out.println("Regist user process started...");

                        User newUser = registUser(reader, writer);

                        if (newUser != null) {
                            writer.println("Register completed successfully!");
                            System.out.println("User <" + newUser.getName() + "> has been registered!");
                            loadUsersFromFile();

                        } else {
                            System.out.println("User failed to register.");
                        }
                        break;

                    case "login":
                        loadUsersFromFile();
                        System.out.println("Login user process started...");
                        loggedUser = loginUser(reader, writer);

                        if (loggedUser != null) {

                            if (loggedUser.isOnline()) {
                                writer.println("This user is already logged in.");
                                loggedUser = null;
                                break;
                            }

                            updateUserStatus(loggedUser, true);

                            writer.println("Login completed successfully!");
                            writer.println(loggedUser.toCSV(";")); //Envia dados do User para o client
                            System.out.println("User <" + loggedUser.getName() + "> has been logged in!");

                        } else {
                            writer.println("Login failed. Returning ...");
                            writer.println("None"); //Envia os dados do User como None
                            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " failed to login!");
                        }
                        break;

                    case "exit":
                        loadUsersFromFile();
                        if (loggedUser != null) {
                            updateUserStatus(loggedUser, false);
                            System.out.println("User <" + loggedUser.getName() + "> disconnected!");
                        } else {
                            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " disconnected!");
                        }
                        writer.println("Disconnected!");
                        clientSocket.close();
                        return;

                    case "messagesFromGroup:HIGH":
                        loadUsersFromFile();
                        sendGroupMessages(highGroupMessagesFile, writer);
                        cleanBuffer(reader);
                        break;

                    case "messagesFromGroup:MEDIUM":
                        loadUsersFromFile();
                        sendGroupMessages(mediumGroupMessagesFile, writer);
                        cleanBuffer(reader);
                        break;

                    case "messagesFromGroup:LOW":
                        loadUsersFromFile();
                        sendGroupMessages(lowGroupMessagesFile, writer);
                        cleanBuffer(reader);
                        break;

                    case "opEvac":
                        loadOperationsFromFile();
                        handleEvacOperation(reader);
                        cleanBuffer(reader);
                        break;

                    case "opEmergency":
                        loadOperationsFromFile();
                        handleEmergencyOperation(reader);
                        cleanBuffer(reader);
                        break;

                    case "opResources":
                        loadOperationsFromFile();
                        handleResourceDistributionOperation(reader);
                        cleanBuffer(reader);
                        break;

                    case "approve":
                        loadOperationsFromFile();
                        handleApproveRequest(reader, writer);
                        cleanBuffer(reader);
                        break;

                    default:
                        loadUsersFromFile();
                        writer.println("Invalid command!");
                        cleanBuffer(reader);
                        break;
                }
            }

        } catch (IOException e) {
            System.err.println("Error: Couldn't connect with the client. " + e.getMessage());
        }
    }

    private User loginUser(BufferedReader reader, PrintWriter writer) throws IOException {

        writer.println("Enter Username: ");
        String username = reader.readLine();

        writer.println("Enter Password: ");
        String password = reader.readLine();

        User tmpUser = new User(username, password);

        return searchUser(tmpUser);
    }

    private User registUser(BufferedReader reader, PrintWriter writer) throws IOException {

        writer.println("Enter Username: ");
        String username = reader.readLine();

        writer.println("Rank (1 - HIGH | 2 - MEDIUM | 3 - LOW): ");
        String inputRank = reader.readLine();
        Rank rank;
        switch (inputRank) {
            case "1":
                rank = Rank.HIGH;
                break;
            case "2":
                rank = Rank.MEDIUM;
                break;
            default:
                rank = Rank.LOW;
                break;
        }

        writer.println("Enter Password: ");
        String password = reader.readLine();

        writer.println("Repeat Password: ");
        String repeatPassword = reader.readLine();

        if (!password.equals(repeatPassword)) {
            writer.println("Passwords do not match! Registration failed.");
            return null;
        }

        User tmpUser = new User(username, rank, password);

        if (searchUser(tmpUser) == null) {
            tmpUser.writeToFile(usersFile);
            return tmpUser;
        } else {
            writer.println("User already exists! Registration failed.");
            return null;
        }

    }

    public User searchUser(User userToSearch) {
        if (!users.isEmpty()) {
            for (User user : users) {
                if (user.getName().equals(userToSearch.getName()) && user.getPassword().equals(userToSearch.getPassword())) {
                    return user;
                }
            }
        }
        return null;
    }

    private void loadUsersFromFile() {
        try {

            BufferedReader br = new BufferedReader(new FileReader(usersFile));

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 5) {

                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String password = parts[2];

                    Rank rank = Rank.valueOf(parts[3]);

                    Boolean status = Boolean.parseBoolean(parts[4]);

                    users.add(new User(id, name, password, rank, status));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    private void sendGroupMessages(String filePath, PrintWriter writer) {
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
            String line;
            while ((line = reader.readLine()) != null) {
                writer.println(line);
            }

            writer.println("END_OF_MESSAGES");

            writer.flush();
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }
    }

    private void updateUserStatus(User user, Boolean status) throws IOException {
        List<String> updatedLines = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(usersFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] fields = line.split(";");

                if (fields.length > 1 && fields[1].equals(user.getName())) {
                    fields[fields.length - 1] = status.toString();
                }
                updatedLines.add(String.join(";", fields));
            }
        }

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(usersFile))) {
            for (String updatedLine : updatedLines) {
                writer.write(updatedLine);
                writer.newLine();
            }
        }
    }

    private void handleEvacOperation(BufferedReader reader) {
        try {
            InetAddress highGroupAddress = InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS);

            String opMsg = reader.readLine();

            Operation op = new Operation(OperationType.EVACUATION, loggedUser, opMsg);
            op.writeToFile(operationsFile);

            sendApproveOperationRequest(op, highGroupAddress, HIGH_MULTICAST_GROUP_PORT);

        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
    }

    private void handleEmergencyOperation(BufferedReader reader) {
        try {
            InetAddress highGroupAddress = InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS);
            InetAddress mediumGroupAddress = InetAddress.getByName(MEDIUM_MULTICAST_GROUP_ADDRESS);

            String opMsg = reader.readLine();

            Operation op = new Operation(OperationType.EMERGENCY_COMUNICATION, loggedUser, opMsg);
            op.writeToFile(operationsFile);

            sendApproveOperationRequest(op, highGroupAddress, HIGH_MULTICAST_GROUP_PORT);
            sendApproveOperationRequest(op, mediumGroupAddress, MEDIUM_MULTICAST_GROUP_PORT);

        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
    }

    private void handleResourceDistributionOperation(BufferedReader reader) {
        try {
            InetAddress highGroupAddress = InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS);
            InetAddress mediumGroupAddress = InetAddress.getByName(MEDIUM_MULTICAST_GROUP_ADDRESS);
            InetAddress lowGroupAddress = InetAddress.getByName(LOW_MULTICAST_GROUP_ADDRESS);

            String opMsg = reader.readLine();

            Operation op = new Operation(OperationType.RESOURCES_DISTRIBUTION, loggedUser, opMsg);
            op.writeToFile(operationsFile);

            sendApproveOperationRequest(op, highGroupAddress, HIGH_MULTICAST_GROUP_PORT);
            sendApproveOperationRequest(op, mediumGroupAddress, MEDIUM_MULTICAST_GROUP_PORT);
            sendApproveOperationRequest(op, lowGroupAddress, LOW_MULTICAST_GROUP_PORT);

        } catch (IOException e) {
            System.err.println("Error reading message: " + e.getMessage());
        }
    }

    private void sendApproveOperationRequest(Operation operation, InetAddress group, int port) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(group);

        byte[] buffer = operation.getApprovalRequestMessage().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        multicastSocket.send(packet);
    }

    /**
     * Aprovar Operações
     *
     * @param reader
     * @param writer
     */
    private void handleApproveRequest(BufferedReader reader, PrintWriter writer) {
        try {
            String opCode = reader.readLine();

            int operationId;
            try {
                operationId = Integer.parseInt(opCode);
            } catch (NumberFormatException e) {
                writer.println("Invalid operation code. Please provide a valid number.");
                return;
            }

            Operation op = searchOperationById(operationId);

            if (op == null) {
                writer.println("Operation not found.");
                return;
            }

            if (op.getStatus().equals(OperationStatus.APPROVED)) {
                writer.println("Operation already approved.");
                return;
            }

            if (op.getLocutor().getName().equals(loggedUser.getName())) {
                writer.println("You can't approve your own operation!");
                return;
            }

            try {
                op.approve(loggedUser, true);
                InitOperation(op);
                updateOperationsInFile(op);
                writer.println("Operation approved and started!");
            } catch (InvalidUserException e) {
                writer.println("User without permission to approve.");
            } catch (IllegalArgumentException e) {
                writer.println("User can't be null.");
            }

        } catch (IOException e) {
            System.err.println("Error reading code: " + e.getMessage());
        }
    }

    private void updateOperationsInFile(Operation operation) {
        List<String> fileContent = new ArrayList<>();
        boolean updated = false;

        try (BufferedReader reader = new BufferedReader(new FileReader(operationsFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                int currentOperationId = Integer.parseInt(parts[0]);

                if (currentOperationId == operation.getId()) {
                    line = operation.toCSV(";");
                    updated = true;
                }

                fileContent.add(line);
            }
        } catch (IOException e) {
            System.err.println("Error reading file: " + e.getMessage());
        }

        if (updated) {
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(operationsFile))) {
                for (String line : fileContent) {
                    writer.write(line);
                }
            } catch (IOException e) {
                System.err.println("Error writing in file: " + e.getMessage());
            }
        }

    }

    private void InitOperation(Operation op) {
        try {
            InetAddress highGroupAddress = InetAddress.getByName(HIGH_MULTICAST_GROUP_ADDRESS);
            InetAddress mediumGroupAddress = InetAddress.getByName(MEDIUM_MULTICAST_GROUP_ADDRESS);
            InetAddress lowGroupAddress = InetAddress.getByName(LOW_MULTICAST_GROUP_ADDRESS);

            if (op.getStatus() == OperationStatus.APPROVED) {

                sendOperationMsg(op, highGroupAddress, HIGH_MULTICAST_GROUP_PORT);
                sendOperationMsg(op, mediumGroupAddress, MEDIUM_MULTICAST_GROUP_PORT);
                sendOperationMsg(op, lowGroupAddress, LOW_MULTICAST_GROUP_PORT);

            } else if (op.getStatus() == OperationStatus.REJECTED) {

                throw new RejectedOperationException("Operation was rejected.");

            }

        } catch (IOException e) {
            System.err.println("Error accessing Multicast Channels: " + e.getMessage());
        }
    }

    private void sendOperationMsg(Operation operation, InetAddress group, int port) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(group);

        byte[] buffer = operation.getOperationServerMessage().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        multicastSocket.send(packet);
    }

    private Operation searchOperationById(int id) {
        for (Operation operation : operations) {
            if (operation.getId() == id) {
                return operation;
            }
        }
        return null;
    }

    private void loadOperationsFromFile() {
        try {
            BufferedReader br = new BufferedReader(new FileReader(operationsFile));

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");

                if (parts.length == 7) {

                    int id = Integer.parseInt(parts[0]);
                    String sender = parts[1];
                    OperationType opType = OperationType.valueOf(parts[2]);
                    String opMsg = parts[3];
                    OperationStatus opStatus = OperationStatus.valueOf(parts[4]);
                    String dateRequested = parts[5];
                    String dateResponded = parts[6];
                    User user = findUser(new User(sender));

                    operations.add(new Operation(id, user, opType, opMsg, opStatus, dateRequested, dateResponded));


                } else if (parts.length == 8) {

                    int id = Integer.parseInt(parts[0]);
                    String sender = parts[1];
                    String approver = parts[2];
                    OperationType opType = OperationType.valueOf(parts[3]);
                    String opMsg = parts[4];
                    OperationStatus opStatus = OperationStatus.valueOf(parts[5]);
                    String dateRequested = parts[6];
                    String dateResponded = parts[7];

                    User locutor = findUser(new User(sender));
                    User interlocutor = findUser(new User(approver));

                    operations.add(new Operation(id, locutor, interlocutor, opType, opMsg, opStatus, dateRequested, dateResponded));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    public User findUser(User userToFind) {
        if (!users.isEmpty()) {
            for (User user : users) {
                if (user.getName().equals(userToFind.getName())) {
                    return user;
                }
            }
        }
        return null;
    }

    private void cleanBuffer(BufferedReader reader) throws IOException {
        while (reader.ready()) {
            reader.readLine();
        }
    }

}