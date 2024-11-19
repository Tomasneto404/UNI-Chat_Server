package Classes;

import java.util.Date;

public class Message {

    private String message;
    private User userSender;
    private Date date;

    public Message(String message, User userSender, Date date) {
        this.message = message;
        this.userSender = userSender;
        this.date = date;
    }
}
