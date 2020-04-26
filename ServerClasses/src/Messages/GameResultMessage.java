package Messages;

import java.io.Serializable;

public class GameResultMessage implements Serializable {
    String winner;

    public GameResultMessage() {
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }
	
	public String getWinner() {
		return winner;
	}

    @Override
    public String toString() {
        return (winner != null ? (winner + " has won the game") : "It's a tie!");
    }
}
