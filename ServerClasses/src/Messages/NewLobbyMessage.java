package Messages;

import java.io.Serializable;

public class NewLobbyMessage implements Serializable {
    private String creatorUsername;
    private String gameLobbyId;

    public NewLobbyMessage() {
    }

    public String getCreatorUsername() {
        return creatorUsername;
    }

    public String getGameLobbyId() {
        return gameLobbyId;
    }

    public void setCreatorUsername(String creatorUsername) {
        this.creatorUsername = creatorUsername;
    }

    public void setGameLobbyId(String gameLobbyId) {
        this.gameLobbyId = gameLobbyId;
    }
}
