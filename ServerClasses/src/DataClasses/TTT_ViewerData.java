package DataClasses;

public class TTT_ViewerData {
    private String game_id;
    private int viewer_id;

    public TTT_ViewerData(String game_id, int viewer_id) {
        this.game_id = game_id;
        this.viewer_id = viewer_id;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public int getViewer_id() {
        return viewer_id;
    }

    public void setViewer_id(int viewer_id) {
        this.viewer_id = viewer_id;
    }
}
