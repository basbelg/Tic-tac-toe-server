package Messages;

import GameInterfaces.Move;

import java.io.Serializable;

public class LegalMoveMessage implements Serializable {
    private Move nextMove;

    public LegalMoveMessage() {
    }

    public Move getNextMove() {
        return nextMove;
    }

    public void setNextMove(Move nextMove) {
        this.nextMove = nextMove;
    }
}
