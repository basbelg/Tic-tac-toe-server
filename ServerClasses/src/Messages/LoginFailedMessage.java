package Messages;

import java.io.Serializable;

public class LoginFailedMessage implements Serializable {
    boolean isOnline;

    public void setOnline(boolean online) {
        isOnline = online;
    }

    public boolean isOnline() {
        return isOnline;
    }

    @Override
    public String toString() {
        return (isOnline ? "This User is Already Logged In" : "Username or Password Incorrect");
    }
}
