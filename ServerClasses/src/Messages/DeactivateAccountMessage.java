package Messages;

import java.io.Serializable;

public class DeactivateAccountMessage implements Serializable {
    private int userId;

    public DeactivateAccountMessage() {
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }
}
