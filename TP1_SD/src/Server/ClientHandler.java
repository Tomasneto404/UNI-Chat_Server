package Server;

import Common.User;
import Enums.Rank;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/***
 * This class is ....
 */
public class ClientHandler implements Runnable {

    private final String USERS_FILE = "Users.csv";

    private final Socket clientSocket;

    private List<User> users;

    private User loggedUser;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
        this.users = new ArrayList<>();
    }

    @Override
    public synchronized void run() {
        try {
            loadUsersFromFile();
            PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " connected!");

            while (true) {
                String input = reader.readLine();

                if (input == null) {
                    System.out.println("Client disconnected.");
                    break;
                }

                switch (input) {

                    case "regist":
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
                        System.out.println("Login user process started...");
                        loggedUser = loginUser(reader, writer);

                        if (loggedUser != null) {
                            writer.println("Login completed successfully!");
                            writer.println(loggedUser.toCSV(";")); //Envia dados do User para o client
                            System.out.println("User <" + loggedUser.getName() + "> has been logged in!");

                        } else {
                            writer.println("Login failed. Returning ...");
                            writer.println("None");
                            System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " failed to login!");
                        }
                        break;

                    case "exit":
                        System.out.println("Client from " + clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort() + " disconnected!");
                        writer.println("Disconnected!");
                        clientSocket.close();
                        return;

                    default:
                        writer.println("Invalid command!");
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
            tmpUser.writeToFile(USERS_FILE);
            return tmpUser;
        } else {
            writer.println("User already exists! Registration failed.");
            return null;
        }

    }

    public User searchUser(User userToSearch) {

        if (!users.isEmpty()){
            for (User user : users) {
                if ( user.getName().equals(userToSearch.getName()) ) {
                    return user;
                }
            }
        }

        return null;
    }

    private void loadUsersFromFile() {
        try {

            BufferedReader br = new BufferedReader(new FileReader(USERS_FILE));

            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 4) {

                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    String password = parts[2];

                    Rank rank = Rank.valueOf(parts[3]);

                    users.add(new User(id, name, password, rank));
                }
            }
        } catch (IOException e) {
            System.err.println("Error loading users: " + e.getMessage());
        }
    }



}
