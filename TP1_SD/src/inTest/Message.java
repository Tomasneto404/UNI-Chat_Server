package inTest;

import Classes.User;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Message {

    private int messageId;
    private String message;
    private User userSender;
    private Date date;

    public Message(int id, String message) {
        this.messageId = id;
        this.message = message;
    }

    public Message(String message, User userSender, Date date) {
        this.message = message;
        this.userSender = userSender;
        this.date = date;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public User getUserSender() {
        return userSender;
    }

    public void setUserSender(User userSender) {
        this.userSender = userSender;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String toFileFormat() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String senderName = (userSender != null) ? userSender.getName() : "Unknown";
        return messageId + ";" + message + ";" + senderName + ";" + dateFormat.format(date);
    }

    public static Message fromFileFormat(String data) {
        try {
            String[] parts = data.split(";");
            int id = Integer.parseInt(parts[0]);
            String content = parts[1];
            String senderName = parts[2];
            Date date = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(parts[3]);

            User sender = new User(0, senderName, null);
            return new Message(content, sender, date);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public String toString() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String senderName;
        
        if (userSender != null) {
            senderName = userSender.getName();
        } else {
            senderName = "Unknown";
        }
        return "Message{id=" + messageId + ", message='" + message + "', sender=" + senderName + ", date="
                + dateFormat.format(date) + "}";
    }
}
