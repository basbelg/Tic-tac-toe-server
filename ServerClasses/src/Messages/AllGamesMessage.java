package Messages;

import DataClasses.TTT_GameData;

import java.io.Serializable;
import java.util.List;

// Server Only Message
public class AllGamesMessage implements Serializable {
    private List<Object> games;

    public AllGamesMessage() {
    }

    public List<Object> getGames() {
        return games;
    }

    public void setGames(List<Object> games) {this.games = games;}
}