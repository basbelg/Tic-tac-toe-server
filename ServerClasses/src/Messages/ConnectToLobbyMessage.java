package Messages;


import java.io.Serializable;
import java.time.LocalDateTime;

public class ConnectToLobbyMessage implements Serializable {
    private String lobbyGameId;
    private String player1Username;
    private String player2Username;
    private LocalDateTime startTime;

    public ConnectToLobbyMessage() {
    }

    public String getLobbyGameId() {
        return lobbyGameId;
    }

    public String getPlayer2() {
        return player2Username;
    }

    public String getPlayer1() { return player1Username; }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setLobbyGameId(String lobbyGameId) {
        this.lobbyGameId = lobbyGameId;
    }

    public void setPlayer2(String player2Username) {
        this.player2Username = player2Username;
    }

    public void setPlayer1(String player1Username) { this.player1Username = player1Username; }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }
}
