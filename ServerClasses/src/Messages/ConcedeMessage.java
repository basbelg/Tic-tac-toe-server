package Messages;

import java.io.Serializable;

public class ConcedeMessage implements Serializable {
    private String gameId;

    public ConcedeMessage() {
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}
