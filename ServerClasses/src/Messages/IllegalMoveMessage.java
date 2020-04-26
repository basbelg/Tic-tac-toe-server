package Messages;

import java.io.Serializable;

public class IllegalMoveMessage implements Serializable {
    @Override
    public String toString() {
        return "Invalid Move!";
    }
}
