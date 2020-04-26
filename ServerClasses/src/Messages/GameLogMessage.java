package Messages;

import DataClasses.MoveInfo;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

public class GameLogMessage implements Serializable {
    private int userId;
    private String gameId;
    private String winner;
    private String player1Username;
    private String player2Username;
    private LocalDateTime gameStarted;
    private LocalDateTime gameEnded;
    private List<MoveInfo> moveHistory;

    public GameLogMessage() {
    }

    public int getUserId() {
        return userId;
    }

    public String getGameId() {
        return gameId;
    }

    public String getWinner() {
        return winner;
    }

    public String getPlayer1Username() {
        return player1Username;
    }

    public String getPlayer2Username() {
        return player2Username;
    }

    public LocalDateTime getGameStarted() {
        return gameStarted;
    }

    public LocalDateTime getGameEnded() {
        return gameEnded;
    }

    public List<MoveInfo> getMoveHistory() {
        return moveHistory;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public void setGameId(String gameId) {
        this.gameId = gameId;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public void setPlayer1Username(String player1Username) {
        this.player1Username = player1Username;
    }

    public void setPlayer2Username(String player2Username) {
        this.player2Username = player2Username;
    }

    public void setGameStarted(LocalDateTime gameStarted) {
        this.gameStarted = gameStarted;
    }

    public void setGameEnded(LocalDateTime gameEnded) {
        this.gameEnded = gameEnded;
    }

    public void setMoveHistory(List<MoveInfo> moveHistory) {
        this.moveHistory = moveHistory;
    }
}
