package Messages;

import DataClasses.LobbyInfo;

import java.io.Serializable;
import java.util.List;

public class AllActiveGamesMessage implements Serializable {
    private int senderId;
    private List<LobbyInfo> allActiveGames;

    public AllActiveGamesMessage() {
    }

    public int getSenderId() {
        return senderId;
    }

    public void setSenderId(int senderId) {
        this.senderId = senderId;
    }

    public List<LobbyInfo> getAllActiveGames() {
        return allActiveGames;
    }

    public void setAllActiveGames(List<LobbyInfo> allActiveGames) {
        this.allActiveGames = allActiveGames;
    }
}
