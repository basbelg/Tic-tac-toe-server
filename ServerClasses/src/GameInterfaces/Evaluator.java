package GameInterfaces;

public interface Evaluator {
    Integer quickEval(Board board);
    Integer weightedEval(Board board, Object weight);
}
