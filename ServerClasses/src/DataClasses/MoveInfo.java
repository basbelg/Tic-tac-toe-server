package DataClasses;

import GameInterfaces.Move;

import java.io.Serializable;

import java.time.LocalDateTime;

public class MoveInfo implements Serializable{
    private Move nextMove;
    private LocalDateTime timeMade;

    public MoveInfo(Move nextMove, LocalDateTime timeMade) {
        this.nextMove = nextMove;
        this.timeMade = timeMade;
    }

    public Move getNextMove() {
        return nextMove;
    }

    public LocalDateTime getTimeMade() {
        return timeMade;
    }
}
