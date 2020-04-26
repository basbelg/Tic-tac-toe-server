package GameInterfaces;

import java.io.Serializable;

public interface Board extends Cloneable, Serializable
{
    int getRowCount();
    int getColumnCount();
    void clearBoard();
    boolean hasMovesLeft();
    int getWinningPlayer() throws Exception;
    int getPlayerAt(int row, int col) throws Exception;
    void setPosition(int player, int row, int col) throws Exception;
    void unsetPosition(int row, int col) throws Exception;
    Object clone() throws CloneNotSupportedException;
}