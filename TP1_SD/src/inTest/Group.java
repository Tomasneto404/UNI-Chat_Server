package inTest;

import java.io.*;
import java.util.*;

import Classes.User;
import Enums.Rank;

public class Group {

    private static final int INITIAL_CAPACITY = 20;
    private int groupId;
    private String groupName;
    private Rank type;
    private List<User> members;
    private List<Message> messages;

    public Group() {
        this.groupId = 0;
        this.groupName = null;
        this.type = null;
        this.members = new ArrayList<>(INITIAL_CAPACITY);
        this.messages = new ArrayList<>();
    }

    public Group(int groupId, String groupName, Rank type) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.type = type;
        this.members = new ArrayList<>(INITIAL_CAPACITY);
        this.messages = new ArrayList<>();
    }

    public boolean contains(User user) {
        return members.contains(user);
    }

    public void addMember(User user) {
        if (!contains(user)) {
            if (user.getRank().equals(this.type)) {
                members.add(user);
            }
        }
    }

    public void removeMember(User user) {
        members.remove(user);
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public Rank getType() {
        return type;
    }

    public void addMessage(Message message) {
        messages.add(message);
    }

    public List<User> getMembers() {
        return new ArrayList<>(members);
    }

    public List<Message> getMessages() {
        return new ArrayList<>(messages);
    }

    public void saveToFile(String fp) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fp))) {
            writer.write(groupId + ";" + groupName + ";" + type + "\n");

            writer.write("Members: \n");
            for (User member : members) {
                writer.write(member.toString() + "\n");
            }

            writer.write("Messages: \n");
            for (Message message : messages) {
                writer.write(message.toString() + "\n");
            }
        }

    }

    public void loadFromFile(String fp) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(fp))) {
            String line = reader.readLine();

            String[] groupInfo = line.split(";");
            this.groupId = Integer.parseInt(groupInfo[0]);
            this.groupName = groupInfo[1];
            this.type = Rank.valueOf(groupInfo[2]);

            members.clear();
            messages.clear();

            while ((line = reader.readLine()) != null && !line.equals("MESSAGES:")) {
                if (!line.equals("MEMBERS:")) {
                    String[] parts = line.split(";");
                    int id = Integer.parseInt(parts[0]);
                    String name = parts[1];
                    Rank rank = Rank.valueOf(parts[2]);
                    members.add(new User(id, name, rank));
                }
            }

            while (line != null && (line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                int messageId = Integer.parseInt(parts[0]);
                String content = parts[1];
                messages.add(new Message(messageId, content));
            }
        }

    }
}
