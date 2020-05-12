package Messages;

import java.io.Serializable;

// Server Only Message
public class AllGameInfoMessage implements Serializable {
    private String game_id;
    private GameViewersMessage GVM;
    private GameLogMessage GLM;

    public AllGameInfoMessage() {
        GVM = new GameViewersMessage();
        GLM = new GameLogMessage();
    }

    public void setId(String game_id) {this.game_id = game_id;}
    public String getId() {return game_id;}
    public GameViewersMessage getGameViewers() {return GVM;}
    public GameLogMessage getGameLog() {return GLM;}
}