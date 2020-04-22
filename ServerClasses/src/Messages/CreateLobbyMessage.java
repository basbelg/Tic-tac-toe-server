package Messages;

import java.io.Serializable;

public class CreateLobbyMessage implements Serializable {
    private int player1Id;
    private String gameLobbyId;

    public CreateLobbyMessage() {
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public String getGameLobbyId() {
        return gameLobbyId;
    }

    public void setPlayer1Id(int player1Id) {
        this.player1Id = player1Id;
    }

    public void setGameLobbyId(String gameLobbyId) {
        this.gameLobbyId = gameLobbyId;
    }
}
