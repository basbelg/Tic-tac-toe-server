package TicTacToe;

import GameInterfaces.Board;
import GameInterfaces.Evaluator;

public class TTT_Evaluator implements Evaluator {
    //------------------------------------------------------------------------------------------------------------------
    //                                                Evaluator Methods
    //------------------------------------------------------------------------------------------------------------------
    @Override
    public Integer quickEval(Board board) {
        Integer evaluation = null;

        if(board instanceof TTT_Board)
            evaluation = boardState((TTT_Board) board, 0);

        return evaluation;
    }

    @Override
    public Integer weightedEval(Board board, Object weight) {
        Integer evaluation = null;

        if(weight instanceof Integer && board instanceof TTT_Board) {
            int depth = (Integer) weight;
            evaluation = boardState((TTT_Board) board, depth);
        }

        return evaluation;
    }

    //------------------------------------------------------------------------------------------------------------------
    //                                      TTT_Evaluator Specific Methods
    //------------------------------------------------------------------------------------------------------------------
    private Integer boardState(TTT_Board board, int depth) {
        Integer evaluation = null;

        if (board.val(0, 0) + board.val(0,1) + board.val(0,2) == 3 ||
                board.val(1, 0) + board.val(1,1) + board.val(1,2) == 3 ||
                board.val(2, 0) + board.val(2,1) + board.val(2,2) == 3 ||
                board.val(0, 0) + board.val(1,1) + board.val(2,2) == 3 ||
                board.val(0, 2) + board.val(1,1) + board.val(2,0) == 3 ||
                board.val(0, 0) + board.val(1,0) + board.val(2,0) == 3 ||
                board.val(0, 1) + board.val(1,1) + board.val(2,1) == 3 ||
                board.val(0, 2) + board.val(1,2) + board.val(2,2) == 3)
            evaluation = Integer.MAX_VALUE - depth;
        else if (board.val(0, 0) + board.val(0,1) + board.val(0,2) == -3 ||
                board.val(1, 0) + board.val(1,1) + board.val(1,2) == -3 ||
                board.val(2, 0) + board.val(2,1) + board.val(2,2) == -3 ||
                board.val(0, 0) + board.val(1,1) + board.val(2,2) == -3 ||
                board.val(0, 2) + board.val(1,1) + board.val(2,0) == -3 ||
                board.val(0, 0) + board.val(1,0) + board.val(2,0) == -3 ||
                board.val(0, 1) + board.val(1,1) + board.val(2,1) == -3 ||
                board.val(0, 2) + board.val(1,2) + board.val(2,2) == -3)
            evaluation = Integer.MIN_VALUE + depth;
        else if (board.val(0, 0) != 0 && board.val(0, 1) != 0 && board.val(0, 2) != 0 &&
                board.val(1, 0) != 0 && board.val(1, 1) != 0 && board.val(1, 2) != 0 &&
                board.val(2, 0) != 0 && board.val(2, 1) != 0 && board.val(2, 2) != 0)
            evaluation = 0;

        return evaluation;
    }
}
