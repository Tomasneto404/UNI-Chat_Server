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

/**
 * A classe ClientHandler é responsável por gerir a comunicação entre o servidor
 * e os clientes.
 * Ela processa solicitações dos clientes, autentica usuários, realiza
 * operações, envia mensagens em grupos multicast
 * e aprova solicitações de operações. A comunicação é feita principalmente
 * através de sockets TCP e multicast.
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

    /**
     * Construtor da classe ClientHandler.
     * 
     * @param clientSocket    Socket utilizado para comunicação com o cliente.
     * @param usersFile       Caminho do ficheiro que armazena os usuários.
     * @param highGroupFile   Caminho do ficheiro de mensagens do grupo HIGH.
     * @param mediumGroupFile Caminho do ficheiro de mensagens do grupo MEDIUM.
     * @param lowGroupFile    Caminho do ficheiro de mensagens do grupo LOW.
     */
    public ClientHandler(Socket clientSocket, String usersFile, String highGroupFile, String mediumGroupFile,
            String lowGroupFile) {
        this.clientSocket = clientSocket;
        this.users = new ArrayList<>();
        this.operations = new ArrayList<>();
        this.usersFile = usersFile;
        this.highGroupMessagesFile = highGroupFile;
        this.mediumGroupMessagesFile = mediumGroupFile;
        this.lowGroupMessagesFile = lowGroupFile;
    }

    @Override
    /**
     * Método principal da thread que executa a comunicação entre cliente e
     * servidor.
     */
    public synchronized void run() {

        try {

            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":"
                    + clientSocket.getPort() + " connected!");

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
                            writer.println(loggedUser.toCSV(";")); // Envia dados do User para o client
                            System.out.println("User <" + loggedUser.getName() + "> has been logged in!");

                        } else {
                            writer.println("Login failed. Returning ...");
                            writer.println("None"); // Envia os dados do User como None
                            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":"
                                    + clientSocket.getPort() + " failed to login!");
                        }
                        break;

                    case "exit":
                        loadUsersFromFile();
                        if (loggedUser != null) {
                            updateUserStatus(loggedUser, false);
                            System.out.println("User <" + loggedUser.getName() + "> disconnected!");
                        } else {
                            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":"
                                    + clientSocket.getPort() + " disconnected!");
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

    /**
     * Realiza o login do usuário.
     *
     * @param reader BufferedReader para leitura das solicitações do cliente.
     * @param writer PrintWriter para envio das respostas ao cliente.
     * @return O usuário autenticado.
     */
    private User loginUser(BufferedReader reader, PrintWriter writer) throws IOException {

        writer.println("Enter Username: ");
        String username = reader.readLine();

        writer.println("Enter Password: ");
        String password = reader.readLine();

        User tmpUser = new User(username, password);

        return searchUser(tmpUser);
    }

    /**
     * Registra um novo usuário no sistema.
     *
     * @param reader BufferedReader para leitura das solicitações do cliente.
     * @param writer PrintWriter para envio das respostas ao cliente.
     */
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

    /**
     * Envia mensagens dos grupos multicast ao cliente.
     *
     * @param filePath Caminho do ficheiro contendo as mensagens do grupo.
     * @param writer   PrintWriter usado para comunicação.
     */
    public User searchUser(User userToSearch) {
        if (!users.isEmpty()) {
            for (User user : users) {
                if (user.getName().equals(userToSearch.getName())
                        && user.getPassword().equals(userToSearch.getPassword())) {
                    return user;
                }
            }
        }
        return null;
    }

    /**
     * Carrega os utilizadores a partir do ficheiro.
     */
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

    /**
     * Envia as mensagens armazenadas num ficheiro para um destino através do objeto
     * PrintWriter.
     * 
     * Este método lê o conteúdo do ficheiro especificado linha a linha e envia cada
     * linha através
     * do PrintWriter. No final, envia um marcador especial "END_OF_MESSAGES" para
     * sinalizar que
     * todas as mensagens foram enviadas. Garante que todos os recursos são fechados
     * corretamente,
     * mesmo em caso de erros.
     * 
     * @param filePath O caminho do ficheiro contendo as mensagens a serem enviadas.
     * @param writer   O objeto PrintWriter usado para enviar as mensagens ao
     *                 destino (socket, console, etc.).
     */
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

    /**
     * Atualiza o estado do utilizador no ficheiro.
     *
     * @param user   O utilizador a atualizar.
     * @param status true se estiver ativo, false caso contrário.
     */
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

    /**
     * Trata a operação de evacuação.
     * Este método lê a mensagem do BufferedReader, cria um objeto `Operation` do
     * tipo EVACUATION,
     * grava a operação num ficheiro e envia a solicitação de aprovação ao grupo
     * multicast de alta prioridade.
     * 
     * @param reader O objeto `BufferedReader` usado para ler a mensagem do cliente.
     */
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

    /**
     * Trata a operação de comunicação de emergência.
     * Este método lê a mensagem do BufferedReader, cria um objeto `Operation` do
     * tipo EMERGENCY_COMUNICATION,
     * grava a operação num ficheiro e envia a solicitação de aprovação aos grupos
     * multicast de alta e média prioridade.
     * 
     * @param reader O objeto `BufferedReader` usado para ler a mensagem.
     */
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

    /**
     * Trata a operação de distribuição de recursos.
     * Este método lê a mensagem do BufferedReader, cria um objeto `Operation` do
     * tipo RESOURCES_DISTRIBUTION,
     * grava a operação num ficheiro e envia a solicitação de aprovação aos grupos
     * multicast de alta, média e baixa prioridade.
     * 
     * @param reader O objeto `BufferedReader` usado para ler a mensagem.
     */
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

    /**
     * Envia uma solicitação de aprovação de operação a um grupo multicast
     * específico.
     *
     * @param operation Objeto da operação a ser aprovada.
     * @param group     Endereço do grupo multicast.
     * @param port      Porta do grupo multicast.
     * @throws IOException Caso haja um erro ao enviar a mensagem.
     */
    private void sendApproveOperationRequest(Operation operation, InetAddress group, int port) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(group);

        byte[] buffer = operation.getApprovalRequestMessage().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        multicastSocket.send(packet);
    }

    /**
     * Aprova as solicitações das operações em grupo multicast.
     *
     * @param reader BufferedReader para leitura das solicitações do cliente.
     * @param writer PrintWriter para envio das respostas.
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

    /**
     * Atualiza as operações no ficheiro.
     *
     * @param operation A operação a ser aprovada ou atualizada.
     */
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

    /**
     * Inicializa a operação e a guarda na lista de operações.
     *
     * @param operation A operação que será iniciada.
     */
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

    /**
     * Envia a mensagem das operações ao grupo multicast especificado.
     *
     * @param operation Objeto da operação a ser enviada.
     * @param group     Endereço multicast do grupo.
     * @param port      Porta utilizada para enviar a mensagem.
     * @throws IOException Caso ocorra um erro ao enviar a mensagem.
     */
    private void sendOperationMsg(Operation operation, InetAddress group, int port) throws IOException {
        MulticastSocket multicastSocket = new MulticastSocket(port);
        multicastSocket.joinGroup(group);

        byte[] buffer = operation.getOperationServerMessage().getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, group, port);
        multicastSocket.send(packet);
    }

    /**
     * Pesquisa e retorna uma operação específica pela sua ID.
     * 
     * Este método procura na lista `operations` por uma instância de `Operation`
     * cujo identificador coincide
     * com o fornecido. Caso encontre a operação, retorna o objeto; caso contrário,
     * retorna `null`.
     * 
     * @param id O identificador único da operação a ser pesquisada.
     * @return A instância da operação correspondente ao ID, ou `null` se não
     *         existir.
     */
    private Operation searchOperationById(int id) {
        for (Operation operation : operations) {
            if (operation.getId() == id) {
                return operation;
            }
        }
        return null;
    }

    /**
     * Carrega as operações do ficheiro de operações no sistema.
     * 
     * Este método lê o ficheiro especificado (`operationsFile`), parseia cada linha
     * e cria instâncias de
     * objetos `Operation`. Adiciona essas operações à lista `operations`. Suporta
     * dois formatos de linhas:
     * 
     * - Um ficheiro com **7 partes**, representando a operação sem aprovador.
     * - Um ficheiro com **8 partes**, representando a operação com aprovador.
     * 
     * Caso ocorra algum erro ao ler ou parsear o ficheiro, a mensagem do erro é
     * exibida no console.
     */
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

                    operations.add(new Operation(id, locutor, interlocutor, opType, opMsg, opStatus, dateRequested,
                            dateResponded));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }

    /**
     * Procura um utilizador na lista de utilizadores.
     *
     * @param userToFind Objeto do utilizador que se deseja encontrar.
     * @return O utilizador encontrado ou null caso não exista.
     */
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

    /**
     * Limpa o conteúdo restante no buffer do BufferedReader.
     * 
     * Este método lê e descarta todas as linhas restantes no buffer do
     * `BufferedReader` até que não existam mais linhas.
     * Isto é útil para garantir que qualquer dado residual no buffer seja ignorado
     * antes de realizar operações subsequentes.
     *
     * @param reader O objeto `BufferedReader` que deve ser limpo.
     * @throws IOException Caso ocorra um erro ao ler o ficheiro ou o fluxo de
     *                     entrada.
     */
    private void cleanBuffer(BufferedReader reader) throws IOException {
        while (reader.ready()) {
            reader.readLine();
        }
    }

}