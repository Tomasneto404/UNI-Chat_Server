package Classes;

import java.util.Date;

public class Message {

    private int messageId;
    private String message;
    private User userSender;
    private Date date;

    public Message(int id,String message) {
        this.messageId=id;
        this.message = message;
    }

    public Message(String message, User userSender, Date date) {
        this.message = message;
        this.userSender = userSender;
        this.date = date;
    }
}
