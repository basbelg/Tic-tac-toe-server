package Messages;

import java.io.Serializable;

public class NewAILobbyMessage implements Serializable {
    private String creatorUsername;
    private String gameLobbyId;

    public NewAILobbyMessage() {
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
