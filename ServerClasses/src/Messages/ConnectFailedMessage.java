package Messages;

import java.io.Serializable;

public class ConnectFailedMessage implements Serializable {
    private boolean isGameActive;

    public ConnectFailedMessage() {
    }

    public boolean isGameActive() {
        return isGameActive;
    }

    public void setGameActive(boolean gameActive) {
        isGameActive = gameActive;
    }

    @Override
    public String toString() {
        return (isGameActive ? "The Lobby is Full! Would You Like to Join as a Spectator?" : "This Game Lobby is Empty!");
    }
}
