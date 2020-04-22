package Messages;

import GameInterfaces.Move;

import java.io.Serializable;
import java.util.List;

public class ViewGameHistoryMessage implements Serializable {
    private int userId;
    List<List<Move>> gameHistories;

    public ViewGameHistoryMessage(int userId) {
        this.userId = userId;
    }

    public int getUserId() {
        return userId;
    }

    public List<List<Move>> getGameHistories() {
        return gameHistories;
    }

    public void setGameHistories(List<List<Move>> gameHistories) {
        this.gameHistories = gameHistories;
    }
}
