package DataClasses;

import java.util.Date;
import java.util.UUID;

public class Game {
    private String id;
    private Date startingTime;
    private Date endTime;
    private int player1Id;
    private int player2Id;
    private int startingPlayerId;
    private int winningPlayerId;

    public Game(Date startingTime, int player1Id, int player2Id, int startingPlayerId) {
        id = UUID.randomUUID().toString();
        this.startingTime = startingTime;
        this.endTime = null;
        this.player1Id = player1Id;
        this.player2Id = player2Id;
        this.startingPlayerId = startingPlayerId;
        this.winningPlayerId = -1;
    }

    public String getId() {
        return id;
    }

    public Date getStartingTime() {
        return startingTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public int getPlayer1Id() {
        return player1Id;
    }

    public int getPlayer2Id() {
        return player2Id;
    }

    public int getStartingPlayerId() {
        return startingPlayerId;
    }

    public int getWinningPlayerId() {
        return winningPlayerId;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public void setWinningPlayerId(int winningPlayerId) {
        this.winningPlayerId = winningPlayerId;
    }
}