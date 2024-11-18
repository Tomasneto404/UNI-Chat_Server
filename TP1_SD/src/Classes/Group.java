package Classes;

import java.util.*;
import Enums.Rank;

public class Group {
    private static int CAPACITY = 20;
    private int groupId;
    private String groupName;
    private Rank type;
    private User[] members;
    private int counter;

    public Group() {
        this.groupId = 0;
        this.groupName = null;
        this.type = null;
        this.members = new User[CAPACITY];
        this.counter = 0;
    }

    public Group(int groupId, String groupName, Rank type) {
        this.groupId = groupId;
        this.groupName = groupName;
        this.type = type;
        this.members = new User[CAPACITY];
        this.counter = 0;
    }

    private void expandCapacity() {
        User[] tmp = new User[CAPACITY * 2];
        for (int i = 0; i < this.counter; i++) {
            tmp[i] = members[i];
        }

        this.members = tmp;
    }

    public boolean contains(User user) {
        for (int i = 0; i < this.counter; i++) {
            if (members[i].equals(user)) {
                return true;
            }
        }
        return false;
    }

    public int posMember(User user) {
        for (int i = 0; i < this.counter; i++) {
            if (members[i].equals(user)) {
                return i;
            }
        }
        return -1;
    }

    public void addMember(User user) {

        if (this.counter == CAPACITY) {
            expandCapacity();
        }

        if (!contains(user)) {
            if (user.getRank().equals(this.type)) {
                this.members[this.counter++] = user;
            }
        }
    }

    public void removeMember(User user) {
        if (contains(user)) {
            int pos = posMember(user);

            for(int i = pos; i<this.counter -1; i++){
                this.members[i]=this.members[i+1];
            }

            this.members[this.counter - 1] = null; 
            this.counter--;
        }
    }

    public int getGroupId() {
        return groupId;
    }

    public String getGroupName() {
        return groupName;
    }

    public Rank getType() {
        return type;
    }

    //adicionar a mensagem
}
