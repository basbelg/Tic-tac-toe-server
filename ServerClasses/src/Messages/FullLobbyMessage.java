package Messages;

import java.io.Serializable;

public class FullLobbyMessage implements Serializable {
    private String lobbyGameId;

    public FullLobbyMessage() {
    }

    public String getLobbyGameId() {
        return lobbyGameId;
    }

    public void setLobbyGameId(String lobbyGameId) {
        this.lobbyGameId = lobbyGameId;
    }
}
