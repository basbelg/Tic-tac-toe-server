package Messages;

import java.io.Serializable;

// Server Only Message
public class AllGameInfoMessage implements Serializable {
    private String game_id;
    private GameViewersMessage GVM;
    private GameLogMessage GLM;

    public AllGameInfoMessage() {}

    public void setId(String game_id) {this.game_id = game_id;}
    public void setGameViewers(GameViewersMessage GVM){this.GVM = GVM;}
    public void setGameLog(GameLogMessage GLM){this.GLM = GLM;}
    public String getId() {return game_id;}
    public GameViewersMessage getGameViewers() {return GVM;}
    public GameLogMessage getGameLog() {return GLM;}
}