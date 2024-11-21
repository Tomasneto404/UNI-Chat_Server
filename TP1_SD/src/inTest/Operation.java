package inTest;

import Classes.User;
import Enums.OperationType;
import Enums.Rank;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;

public class Operation {

    private int operationId;
    private OperationType type;
    private String description;
    private User requester; // The user who requested the operation
    private User approver; // The user who approved/rejected the operation
    private Rank requiredRank;
    private boolean finalized; // True if the operation is approved/rejected
    private String status; // "PENDING", "APPROVED", "REJECTED"
    private LocalDateTime createdAt;
    private LocalDateTime finalizedAt;

    public Operation(int operationId, OperationType type, String description, User requester, Rank requiredRank) {
        this.operationId = operationId;
        this.type = type;
        this.description = description;
        this.requester = requester;
        this.requiredRank = requiredRank;
        this.status = "PENDING";
        this.finalized = false;
        this.createdAt = LocalDateTime.now();
    }

    public String getStatus() {
        return this.status;
    }

    public boolean isFinalized() {
        return this.finalized;
    }

    public void approve(User approver) throws Exception {
        validateApproval(approver);
        this.approver = approver;
        this.status = "APPROVED";
        this.finalized = true;
        this.finalizedAt = LocalDateTime.now();
        notifyOperation("APPROVED by " + approver.getName());
    }

    public void reject(User approver) throws Exception {
        validateApproval(approver);
        this.approver = approver;
        this.status = "REJECTED";
        this.finalized = true;
        this.finalizedAt = LocalDateTime.now();
        notifyOperation("REJECTED by " + approver.getName());
    }

    private void validateApproval(User approver) throws Exception {
        if (this.finalized) {
            throw new Exception("Operation is already finalized. No further actions are allowed.");
        }
        if (approver == null) {
            throw new Exception("Approver is not defined.");
        }
        if (approver.getRank().ordinal() < this.requiredRank.ordinal()) {
            throw new Exception("Approver does not have the required rank for this operation.");
        }
        if (approver.getName().equals(this.requester.getName())) {
            throw new Exception("Requester cannot approve or reject their own operation.");
        }
    }

    private void notifyOperation(String message) {
        System.out.println("Operation Notification: " + message);

        // Log the notification to a file
        /*
         * try {
         * logToFile("notifications.log", message);
         * } catch (IOException e) {
         * System.err.println("Error logging notification: " + e.getMessage());
         * }
         */

    }

    public void writeToFile(String file) throws IOException {
        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.append(this.toCSV(";"));
        fileWriter.close();
    }

    private String toCSV(String chr) {
        return operationId + chr + type + chr + description + chr + requester.getName() + chr +
                (approver != null ? approver.getName() : "NONE") + chr +
                status + chr + createdAt + chr + (finalizedAt != null ? finalizedAt : "N/A") + "\n";
    }

    @Override
    public String toString() {
        return "Operation{" +
                "operationId=" + operationId +
                ", type=" + type +
                ", description='" + description + '\'' +
                ", requester=" + requester.getName() +
                ", approver=" + (approver != null ? approver.getName() : "NONE") +
                ", requiredRank=" + requiredRank +
                ", status='" + status + '\'' +
                ", finalized=" + finalized +
                ", createdAt=" + createdAt +
                ", finalizedAt=" + finalizedAt +
                '}';
    }
}
