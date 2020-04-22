package Messages;

import DataClasses.User;

import java.io.Serializable;

public class UpdateAccountInfoMessage implements Serializable {
    private User updatedUser;

    public UpdateAccountInfoMessage() {
    }

    public User getUpdatedUser() {
        return updatedUser;
    }

    public void setUpdatedUser(User updatedUser) {
        this.updatedUser = updatedUser;
    }
}
