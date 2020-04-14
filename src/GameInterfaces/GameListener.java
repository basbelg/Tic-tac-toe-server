package GameInterfaces;

public interface GameListener {
    void onEventReceived(String name, Board b, Move m, Object addInfo);
}
