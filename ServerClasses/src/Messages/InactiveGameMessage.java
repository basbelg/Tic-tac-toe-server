package Messages;

import java.io.Serializable;

public class InactiveGameMessage implements Serializable {
    String finishedGameId;

    public InactiveGameMessage() {
    }

    public String getFinishedGameId() {
        return finishedGameId;
    }

    public void setFinishedGameId(String finishedGameId) {
        this.finishedGameId = finishedGameId;
    }
}
