package Messages;

import DataClasses.User;

import java.io.Serializable;

public class LoginSuccessfulMessage implements Serializable {
    private User user;

    public LoginSuccessfulMessage() {
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
