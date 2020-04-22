package Messages;

import DataClasses.GameInfo;

import java.io.Serializable;
import java.util.List;

public class GamesPlayedMessage implements Serializable {
    private int playerId; // Used by the server when sending the message back
    private List<GameInfo> gameInfoList;

    public GamesPlayedMessage() {
    }

    public int getPlayerId() {
        return playerId;
    }

    public void setPlayerId(int playerId) {
        this.playerId = playerId;
    }

    public List<GameInfo> getGameInfoList() {
        return gameInfoList;
    }

    public void setGameInfoList(List<GameInfo> gameInfoList) {
        this.gameInfoList = gameInfoList;
    }
}
