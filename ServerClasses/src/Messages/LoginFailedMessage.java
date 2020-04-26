package Messages;

import java.io.Serializable;

public class LoginFailedMessage implements Serializable {
    @Override
    public String toString() {
        return "Username or Password Incorrect";
    }
}
