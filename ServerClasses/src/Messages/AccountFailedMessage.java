package Messages;

import java.io.Serializable;

public class AccountFailedMessage implements Serializable {
    @Override
    public String toString() {
        return "Username Taken";
    }
}
