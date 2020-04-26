package Messages;

import DataClasses.MoveInfo;

import java.io.Serializable;

public class MoveMessage implements Serializable {
    private MoveInfo moveInfo;
    private String gameId;
    private int movingPlayerId;

    public MoveMessage() {
    }

    public MoveInfo getMoveInfo() {
        return moveInfo;
    }

    public String getGameId() {
        return gameId;
    }

    public int getMovingPlayerId() {
        return movingPlayerId;
    }

    public void setMoveInfo(MoveInfo moveInfo) {
        this.moveInfo = moveInfo;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setMovingPlayerId(int movingPlayerId) {
        this.movingPlayerId = movingPlayerId;
    }
}
