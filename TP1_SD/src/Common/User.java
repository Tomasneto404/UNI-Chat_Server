package Common;

import Enums.Rank;

import Enums.OperationType;

import java.io.*;

public class User {

    private int id;
    private String name;
    private Rank rank;
    private String password;
    private OperationType op;

    // preciso disto tomás, não apagues
    public User(int id, String name, Rank rank) {
        this.id = id;
        this.name = name;
        this.rank = rank;
    }

    public User(String name, Rank rank, String password) {
        this.name = name;
        this.rank = rank;
        this.password = password;
    }

    // É preciso alterar
    public User(String name, String password) {
        this.id = 0;
        this.name = name;
        this.password = password;
        this.rank = Rank.NONE;
    }

    public User(int id, String name, String password, Rank rank) {
        this.id = id;
        this.name = name;
        this.password = password;
        this.rank = rank;
    }

    public void setOperation(OperationType op) {
        this.op = op;
    }

    public OperationType getOperation() {
        return this.op;
    }

    public String getName() {
        return name;
    }

    protected String getPassword() {
        return password;
    }

    public Rank getRank() {
        return this.rank;
    }

    public String toCSV(String chr) {
        return this.id + chr + this.name + chr + this.password + chr + this.rank + "\n";
    }

    public void writeToFile(String file) throws IOException {

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.append(this.toCSV(";"));
        fileWriter.close();

    }

}
