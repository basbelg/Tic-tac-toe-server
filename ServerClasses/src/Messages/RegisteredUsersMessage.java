package Messages;

import DataClasses.User;

import java.io.Serializable;
import java.util.List;

// Server Only Message
public class RegisteredUsersMessage implements Serializable {
    private List<Object> users;

    public RegisteredUsersMessage() {
    }

    public List<Object> getUsers() {
        return users;
    }

    public void setUsers(List<Object> users) {this.users = users;}
}