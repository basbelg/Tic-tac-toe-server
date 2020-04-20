package DataClasses;

public class Spectator
{
    private String gameID;
    private String playerID;

    public Spectator(String gameID, String playerID)
    {
        this.gameID = gameID;
        this.playerID = playerID;
    }

    public String getGameID() {
        return gameID;
    }

    public String getPlayerID() {
        return playerID;
    }
}
