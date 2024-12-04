package Common;

import Enums.OperationStatus;
import Enums.OperationType;
import Enums.Rank;
import Exceptions.InvalidUserException;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Operation {

    private static int id = 0;
    private User locutor;
    private User interlocutor;
    private OperationType type;
    private String msg;
    private OperationStatus status;
    private String dateRequested;
    private String dateResponded;

    public Operation(OperationType type, User locutor, String msg) {
        this.id++;

        this.type = type;
        this.locutor = locutor;
        this.msg = msg;

        this.dateRequested = actualDate();

        if (validateTypeAndRank(type, locutor.getRank())) {
            status = OperationStatus.PENDING;
        } else {
            status = OperationStatus.REJECTED;
        }
    }

    public Operation(int id, User user, OperationType opType, String opMsg, OperationStatus opStatus, String dateRequested, String dateResponded) {
        this.id = id;
        this.locutor = user;
        this.interlocutor = null;
        this.type = opType;
        this.msg = opMsg;
        this.status = opStatus;
        this.dateRequested = dateRequested;
        this.dateResponded = dateResponded;
    }

    public Operation(int id, User locutor, User interlocutor, OperationType opType, String opMsg, OperationStatus opStatus, String dateRequested, String dateResponded) {
        this.id = id;
        this.locutor = locutor;
        this.interlocutor = interlocutor;
        this.type = opType;
        this.msg = opMsg;
        this.status = opStatus;
        this.dateRequested = dateRequested;
        this.dateResponded = dateResponded;
    }

    public void approve(User user, boolean approved) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (!validateTypeAndRank(type, user.getRank())) {
            throw new InvalidUserException("User without permission to approve.");
        }

        this.interlocutor = user;
        this.dateResponded = actualDate();

        if (approved) {
            status = OperationStatus.APPROVED;
        } else {
            status = OperationStatus.REJECTED;
        }
    }


    public String getMsg(){
        return msg;
    }

    private String actualDate(){
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        return myDateObj.format(myFormatObj);
    }

    public boolean validateTypeAndRank(OperationType type, Rank userRank) {
        switch (type) {
            case EVACUATION:
                return userRank == Rank.HIGH;

            case EMERGENCY_COMUNICATION:
                return userRank == Rank.HIGH || userRank == Rank.MEDIUM;

            case RESOURCES_DISTRIBUTION:
                return true;

            default:
                return false;
        }
    }


    public static int getId() {
        return id;
    }

    public static void setId(int id) {
        Operation.id = id;
    }

    public User getLocutor() {
        return locutor;
    }

    public void setLocutor(User locutor) {
        this.locutor = locutor;
    }

    public User getInterlocutor() {
        return interlocutor;
    }

    public void setInterlocutor(User interlocutor) {
        this.interlocutor = interlocutor;
    }

    public OperationType getType() {
        return type;
    }

    public void setType(OperationType type) {
        this.type = type;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public OperationStatus getStatus() {
        return status;
    }

    public void setStatus(OperationStatus status) {
        this.status = status;
    }

    public String getDateRequested() {
        return dateRequested;
    }

    public void setDateRequested(String dateRequested) {
        this.dateRequested = dateRequested;
    }

    public String getDateResponded() {
        return dateResponded;
    }

    public void setDateResponded(String dateResponded) {
        this.dateResponded = dateResponded;
    }

    public String getOperationServerMessage(){
        String str = "\n*SERVER MESSAGE* -> EXECUTE OPERATION - " + type + "\n"
                    + "Date: " + dateRequested + "\n"
                    + "Sender: " + locutor.getName() + " (" + locutor.getRank() + ")" + "\n"
                    + "Accepted By: " + interlocutor.getName() + " (" + interlocutor.getRank() + ")" + "\n"
                    + "Message: " + msg + "\n";

        return str;
    }

    public String getApprovalRequestMessage(){
        String str = "\n-OPERATION REQUEST- " + type + "\n"
                + "Date: " + dateRequested + "\n"
                + "Sender: " + locutor.getName() + " (" + locutor.getRank() + ")" + "\n"
                + "Code: " + id + "\n"
                + "Message: " + msg + "\n";

        return str;
    }

    public String toCSV(String chr) {
        String str = "";
        if (interlocutor == null) {
            str = id + chr + locutor.getName() + chr + type.toString() + chr + msg + chr + status + chr + dateRequested + chr + dateResponded + "\n";
        } else {
            str = id + chr + locutor.getName() + chr + interlocutor.getName() + chr + type.toString() + chr + msg + chr + status + chr + dateRequested + chr + dateResponded + "\n";
        }
        return str;
    }

    public void writeToFile(String file) throws IOException {

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.append(this.toCSV(";"));
        fileWriter.close();

    }
}
