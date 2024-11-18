package Classes;

import Enums.OperationType;
import Enums.Rank;

public class Authorization {

    public boolean requestAproval (User user, OperationType operation){

        if( operation.equals(OperationType.EVACUATION)){
            return user.getRank().equals(Rank.HIGH);
        }

        else if (operation.equals(OperationType.EMERGENCY_COMUNICATION)) {
            return user.getRank().equals(Rank.MEDIUM);

        } else if(operation.equals(OperationType.RESOURCES_DISTRIBUTION)){
            return user.getRank().equals(Rank.LOW);
        }

        return false;
    }
}
