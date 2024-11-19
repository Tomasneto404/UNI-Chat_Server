package Classes;

import Enums.Rank;
import Exceptions.StopReadingException;

import java.io.*;
import java.util.*;

public class UserInterface {

    private String usersFile;
    private List<User> users;

    {
        this.users = new ArrayList<>();
    }

    public void setUsersFile(String file) {
        usersFile = file;
    }

    public void displayMainMenu(BufferedReader reader, PrintWriter writer) throws IOException {

        //Load data from files to memory
        loadData();

        writer.println("\n--- Welcome to the CHAT ---");

        boolean authenticated = false;

        while (!authenticated) {
            try {
                authenticated = displayAuthMenu(reader, writer);
            } catch (StopReadingException e) {
                System.out.println(e.getMessage());
                return;
            }

        }



        while (true) {

            writer.println("\n--- Menu ---");
            writer.println("1. Channels");
            writer.println("2. Notifications");
            writer.println("4. Exit");
            writer.println("Option > ");

            String option = reader.readLine();

            if (option == null) {
                break;
            }

            switch (option) {
                case "1":
                    break;
                case "2":
                    break;
                case "4":
                    writer.println("\nDisconnected!");
                    return;
                default:
                    writer.println("\nInvalid option!.");
            }
        }
    }

    private boolean displayAuthMenu(BufferedReader reader, PrintWriter writer) throws IOException {

        while (true) {

            writer.println("\n--- Authentication ---");
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
                    registUser(reader, writer);
                case "2":
                    return loginUser(reader, writer);
                case "3":
                    writer.println("Disconnected!");
                    throw new StopReadingException("User disconnected!");

                default:
                    writer.println("Invalid option!.");
            }
        }
        return true;
    }

    private void registUser(BufferedReader reader, PrintWriter writer) throws IOException {

        writer.println("Enter Username: ");
        String username = reader.readLine();

        writer.println("Rank: " +
                "1 - HIGH | " +
                "2 - MEDIUM | " +
                "3 - LOW");
        String inputRank = reader.readLine();

        Rank rank;

        switch (inputRank) {
            case "1":
                rank = Rank.HIGH;
                break;
            case "2":
                rank = Rank.MEDIUM;
                break;
            case "3":
                rank = Rank.LOW;
                break;
            default:
                rank = Rank.NONE;
        }



        writer.println("Enter Password: ");
        String password = reader.readLine();

        writer.println("Repeat Password: ");
        String repeatPassword = reader.readLine();

        if (password.equals(repeatPassword)) {

            User tmpUser = new User(username, rank, password);

            if (!searchUser(tmpUser)) {
                tmpUser.writeToFile(usersFile);
            } else {
                writer.println("User already exists!");
            }

        } else {

            writer.println("\nPasswords do not match!");

        }

    }

    private boolean loginUser(BufferedReader reader, PrintWriter writer) throws IOException {

        loadData();

        writer.println("Username ");
        String username = reader.readLine();

        writer.println("Password ");
        String password = reader.readLine();

        User tmpUser = new User(username, password);

        if (searchUser(tmpUser)) {
            writer.println("Authenticated!");
            System.out.println("User <" + tmpUser.getName() + "> logged in successfully!");
            return true;
        }

        writer.println("User not found!");
        return false;
    }

    private void loadData(){

        //Carregar utilizadores para array
        try (BufferedReader br = new BufferedReader(new FileReader(usersFile))) {
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
            System.err.println("Error reading file: " + e.getMessage());
        }

    }

    public boolean searchUser(User userToSearch) {

        if (!users.isEmpty()){
            for (User user : users) {
                if ( user.getName().equals(userToSearch.getName()) ) {
                    return true;
                }
            }
        }

        return false;
    }

}
