package Messages;

import java.io.Serializable;

public class StopSpectatingMessage implements Serializable {
    private int playerId;
    private String gameId;

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public String getGameId() {
        return gameId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }
}

