package Messages;

import java.io.Serializable;

public class SpectateMessage implements Serializable {
    private int spectatorId;
    private String gameId;
    private String player1Username;
    private String player2Username;
    private int[][] gameBoard;

    public SpectateMessage() {
    }

    public int getSpectatorId() {
        return spectatorId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public int[][] getGameBoard() {
        return gameBoard;
    }

    public void setSpectatorId(int spectatorId) {
        this.spectatorId = spectatorId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public void setGameBoard(int[][] gameBoard) {
        this.gameBoard = gameBoard;
    }
}
