package Classes;

import Enums.Rank;

public class User {

    private int id;
    private String name;
    private Rank rank;
    private String password;

    public User(int id, String name, Rank rank, String password) {
        this.id = id;
        this.name = name;
        this.rank = rank;
        this.password = password;
    }


}
