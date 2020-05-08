package TicTacToe;

import GameInterfaces.Board;
import GameInterfaces.Evaluator;

public class TTT_Board implements Board {
    private int[][] currentBoard;
    private Evaluator evaluator = new TTT_Evaluator();

    //------------------------------------------------------------------------------------------------------------------
    //                                                Constructors
    //------------------------------------------------------------------------------------------------------------------
    public TTT_Board() {currentBoard = new int[][]{{0,0,0},{0,0,0},{0,0,0}};}

    public TTT_Board(TTT_Board board) {
        for(int i = 0; i < 9; i++)
            this.currentBoard[i/3][i%3] = board.currentBoard[i/3][i%3];
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                                Board Methods
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public int getRowCount() {return 3;}

    @Override
    public int getColumnCount() {return 3;}

    @Override
    public void clearBoard() {
        for(int i = 0; i < 9; i++)
            this.currentBoard[i/3][i%3] = 0;
    }

    @Override
    public boolean hasMovesLeft() {return evaluator.quickEval(this) == null;}

    @Override
    public int getWinningPlayer() throws Exception{
        int player = 0;
        Integer eval = evaluator.quickEval(this);

        if(eval == null)
            throw new Exception("Game has not ended yet!");
        else if(eval != 0)
            player = (eval > 0)? 1: 2;

        return player;
    }

    @Override
    public int getPlayerAt(int row, int col) throws Exception {
        int player = 0;

        if(currentBoard[row][col] == 1)
            player = 1;
        else if(currentBoard[row][col] == -1)
            player = 2;
        else
            throw new Exception("No player at the specified position!");

        return player;
    }

    public int[][] getCurrentBoard() {
        return currentBoard.clone();
    }

    @Override
    public void setPosition(int player, int row, int col) throws Exception {
        if(player == 1 && currentBoard[row][col] == 0)
            currentBoard[row][col] = 1;
        else if(player == 2 && currentBoard[row][col] == 0)
            currentBoard[row][col] = -1;
        else
            throw new Exception("Illegal Set Exception: Can not set position at row: " + row + " column: " + col + "!");
    }

    @Override
    public void unsetPosition(int row, int col) throws Exception {
        if(currentBoard[row][col] == 0)
            throw new Exception("Illegal Set Exception: Can not unset position at row: " + row + " column: " + col + "!");

        currentBoard[row][col] = 0;
    }

    @Override
    public Object clone() throws CloneNotSupportedException {return new TTT_Board(this);}

    //------------------------------------------------------------------------------------------------------------------
    //                                       TTT_Board Specific Methods
    //------------------------------------------------------------------------------------------------------------------
    // returns raw value at row and column
    public int val(int row, int col) {return currentBoard[row][col];}
}
