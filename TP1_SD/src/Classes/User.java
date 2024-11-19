package Classes;

import Enums.Rank;

import java.io.*;

public class User {

    private int id;
    private String name;
    private Rank rank;
    private String password;

    public User( String name, Rank rank, String password) {
        this.name = name;
        this.rank = rank;
        this.password = password;
    }

    public User (String name, String password) {
        this.id = 0000;
        this.name = name;
        this.password = password;
        this.rank = Rank.NONE;
    }

    public Rank getRank(){
        return this.rank;
    }

    @Override
    public String toString(){
        return this.id + ";" + this.name + ";" + this.password + ";" + this.rank + "\n";
    }

    public void writeToFile(String file) throws IOException {

        FileWriter fileWriter = new FileWriter(file, true);
        fileWriter.append(this.toString());
        fileWriter.close();

    }

}
