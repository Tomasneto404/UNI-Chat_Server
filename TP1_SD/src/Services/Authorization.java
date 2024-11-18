package Services;

import Classes.User;
import Enums.OperationType;
import Enums.Rank;

public class Authorization {
<<<<<<< HEAD
    public boolean requestAproval(User user, OperationType operation) {
        if (operation.equals(OperationType.EVACUATION)) {
=======
    public boolean requestAproval (User user, OperationType operation){

        if( operation.equals(OperationType.EVACUATION)){
>>>>>>> 7a552ed (UserInterface)
            return user.getRank().equals(Rank.HIGH);
        }

        else if (operation.equals(OperationType.EMERGENCY_COMUNICATION)) {
            return user.getRank().equals(Rank.MEDIUM);

        }

<<<<<<< HEAD
        else if (operation.equals(OperationType.RESOURCES_DISTRIBUTION)) {
=======
        else if(operation.equals(OperationType.RESOURCES_DISTRIBUTION)){
>>>>>>> 7a552ed (UserInterface)
            return user.getRank().equals(Rank.LOW);
        }

        return false;
    }
}
