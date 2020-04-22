package Messages;

import java.io.Serializable;
import java.time.LocalDateTime;

public class CreateAIGameMessage implements Serializable {
    private int player1Id;
    private String gameLobbyId; // Player2 of this game will always be an AI player (whose ID is predefined to be 1)
    private LocalDateTime startTime;

    public CreateAIGameMessage() {
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public String getGameLobbyId() {
        return gameLobbyId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    public void setGameLobbyId(String gameLobbyId) {
        this.gameLobbyId = gameLobbyId;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
