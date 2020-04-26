package DataClasses;

import java.time.LocalDateTime;
import java.io.Serializable;

public class GameInfo implements Serializable
{
    private String player2Username;
    private LocalDateTime startTime;
    private String gameId;

    public GameInfo(String player2Username, LocalDateTime startTime, String gameId) {
        this.player2Username = player2Username;
        this.startTime = startTime;
        this.gameId = gameId;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public String getGameId() {
        return gameId;
    }
}
