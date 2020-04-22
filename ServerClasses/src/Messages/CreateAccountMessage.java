package Messages;

import DataClasses.User;

import java.io.Serializable;

public class CreateAccountMessage implements Serializable {
    private User newUser;

    public CreateAccountMessage() {
    }

    public User getNewUser() {
        return newUser;
    }

    public void setNewUser(User newUser) {
        this.newUser = newUser;
    }
}
