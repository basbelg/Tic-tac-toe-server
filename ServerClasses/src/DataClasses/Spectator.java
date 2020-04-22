package DataClasses;

import java.io.Serializable;

public class Spectator implements Serializable {
    private String username;

    public Spectator(String username) {
        this.username = username;
    }

    public String getUsername() {
        return username;
    }
}
