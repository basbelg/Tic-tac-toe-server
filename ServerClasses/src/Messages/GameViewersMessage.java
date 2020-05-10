package Messages;

import DataClasses.Spectator;

import java.io.Serializable;
import java.util.List;

public class GameViewersMessage implements Serializable {
    private int userId;
    private String gameId;
    private List<Spectator> spectators;
    private boolean isGameActive;

    public GameViewersMessage() {}

    public int getUserId() {
        return userId;
    }

    public String getGameId() {
        return gameId;
    }

    public List<Spectator> getSpectators() {
        return spectators;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setSpectators(List<Spectator> spectators) {
        this.spectators = spectators;
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public void setGameActive(boolean gameActive) {
        isGameActive = gameActive;
    }
}
