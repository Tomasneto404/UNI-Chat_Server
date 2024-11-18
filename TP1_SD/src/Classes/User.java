package Classes;

import Enums.Rank;

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
        this.name = name;
        this.password = password;
    }

    public Rank getRank(){
        return this.rank;
    }

    @Override
    public String toString(){
        return this.name + " " + this.password;
    }

}
