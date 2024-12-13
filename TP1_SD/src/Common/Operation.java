package Common;

import Enums.OperationStatus;
import Enums.OperationType;
import Enums.Rank;
import Exceptions.InvalidUserException;

import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import java.util.Random;

/**
 * Classe Operation.
 */
public class Operation {

    private int id;
    private User locutor;
    private User interlocutor;
    private OperationType type;
    private String msg;
    private OperationStatus status;
    private String dateRequested;
    private String dateResponded;

    /**
     * Construtor da classe Operation.
     * Inicializa uma operação com o tipo, utilizador locutor e mensagem.
     * 
     * @param type    Tipo de operação.
     * @param locutor Utilizador que solicita a operação.
     * @param msg     Mensagem associada à operação.
     */
    public Operation(OperationType type, User locutor, String msg) {
        Random random = new Random();
        this.id = 100 + random.nextInt(900);

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

    /**
     * Construtor completo da classe Operation sem interlocutor.
     * 
     * @param id            Identificador da operação.
     * @param user          Utilizador locutor.
     * @param opType        Tipo de operação.
     * @param opMsg         Mensagem associada à operação.
     * @param opStatus      Estado da operação.
     * @param dateRequested Data do pedido.
     * @param dateResponded Data da resposta.
     */
    public Operation(int id, User user, OperationType opType, String opMsg, OperationStatus opStatus,
            String dateRequested, String dateResponded) {
        this.id = id;
        this.locutor = user;
        this.interlocutor = null;
        this.type = opType;
        this.msg = opMsg;
        this.status = opStatus;
        this.dateRequested = dateRequested;
        this.dateResponded = dateResponded;
    }

    /**
     * Construtor completo da classe Operation com interlocutor.
     * 
     * @param id            Identificador da operação.
     * @param locutor       Utilizador que solicitou a operação.
     * @param interlocutor  Utilizador que aprovou/rejeitou a operação.
     * @param opType        Tipo de operação.
     * @param opMsg         Mensagem associada à operação.
     * @param opStatus      Estado da operação.
     * @param dateRequested Data do pedido.
     * @param dateResponded Data da resposta.
     */
    public Operation(int id, User locutor, User interlocutor, OperationType opType, String opMsg,
            OperationStatus opStatus, String dateRequested, String dateResponded) {
        this.id = id;
        this.locutor = locutor;
        this.interlocutor = interlocutor;
        this.type = opType;
        this.msg = opMsg;
        this.status = opStatus;
        this.dateRequested = dateRequested;
        this.dateResponded = dateResponded;
    }

    /**
     * Aprova ou rejeita uma operação.
     * 
     * @param user     Utilizador que responde à operação.
     * @param approved Booleano indicando se a operação foi aprovada.
     */
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

    /**
     * Obtém a mensagem associada à operação.
     * 
     * @return Mensagem da operação.
     */
    public String getMsg() {
        return msg;
    }

    /**
     * Obtém a data e hora atual no formato "dd-MM-yyyy HH:mm:ss".
     * 
     * @return String com a data e hora formatada.
     */
    private String actualDate() {
        LocalDateTime myDateObj = LocalDateTime.now();
        DateTimeFormatter myFormatObj = DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss");

        return myDateObj.format(myFormatObj);
    }

    /**
     * Valida o tipo de operação com base no rank do utilizador.
     * 
     * @param type     Tipo de operação.
     * @param userRank Rank do utilizador.
     * @return true se o utilizador tiver permissão, false caso contrário.
     */
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

    // Métodos getter e setter para os atributos.
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
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

    /**
     * Obtém uma mensagem detalhada do servidor sobre a operação.
     * 
     * @return String com a mensagem do servidor.
     */
    public String getOperationServerMessage() {
        String str = "\n*SERVER MESSAGE* -> EXECUTE OPERATION - " + type + "\n"
                + "Date: " + dateRequested + "\n"
                + "Sender: " + locutor.getName() + " (" + locutor.getRank() + ")" + "\n"
                + "Accepted By: " + interlocutor.getName() + " (" + interlocutor.getRank() + ")" + "\n"
                + "Message: " + msg + "\n";

        return str;
    }

    /**
     * Obtém uma mensagem de pedido de aprovação da operação.
     * 
     * @return String com o pedido de aprovação.
     */
    public String getApprovalRequestMessage() {
        String str = "\n-OPERATION REQUEST- " + type + "\n"
                + "Date: " + dateRequested + "\n"
                + "Sender: " + locutor.getName() + " (" + locutor.getRank() + ")" + "\n"
                + "Code: " + id + "\n"
                + "Message: " + msg + "\n";

        return str;
    }

    /**
     * Converte os atributos da operação para uma string no formato CSV.
     * 
     * @param chr Delimitador para o CSV.
     * @return String no formato CSV.
     */
    public String toCSV(String chr) {
        String str = "";
        if (interlocutor == null) {
            str = id + chr + locutor.getName() + chr + type.toString() + chr + msg + chr + status + chr + dateRequested
                    + chr + dateResponded;
        } else {
            str = id + chr + locutor.getName() + chr + interlocutor.getName() + chr + type.toString() + chr + msg + chr
                    + status + chr + dateRequested + chr + dateResponded;
        }

        str += "\n";

        return str;
    }

    /**
     * Escreve os dados da operação num ficheiro.
     * 
     * @param file Caminho do ficheiro onde os dados serão escritos.
     * @throws IOException Lança uma exceção se ocorrer um erro de I/O.
     */
    public void writeToFile(String file) throws IOException {

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.append(this.toCSV(";"));
        fileWriter.close();

    }
}
