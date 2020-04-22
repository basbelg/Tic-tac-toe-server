package Messages;

import java.io.Serializable;

public class LoginMessage implements Serializable {
    private String username;
    private String password;

    public LoginMessage() {
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
