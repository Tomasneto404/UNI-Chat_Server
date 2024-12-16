package Tests;

import Common.User;

import java.util.List;

public class PrivateMessage {

    private User user1;
    private User user2;
    private List<String> messages;
    private String messagesFile;

    public PrivateMessage(User user1, User user2, List<String> messages) {
        this.user1 = user1;
        this.user2 = user2;
        this.messages = messages;
    }

}
