package GameInterfaces;

import java.io.Serializable;
import java.util.List;

public interface Game extends Serializable {
    void startGame() throws Exception;
    void endGame() throws Exception;
    boolean isFinished();
    void saveGame() throws Exception;

    String getGameId();

    void performMove(Move nextMove) throws Exception;
    void undoMove() throws Exception;
    void restart() throws Exception;

    int getNextPlayer();
    int getWinner() throws Exception;

    <T extends Move> List<T> getMoveHistory();

    Board getBoard() throws Exception;

    void addGameListener(GameListener listener);
    void removeGameListener(GameListener listener);
}
