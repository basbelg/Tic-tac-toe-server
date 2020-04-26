package DataClasses;

import java.time.LocalDateTime;

public class TTT_MoveData {
    private String game_id;
    private int player_id;
    private LocalDateTime time;
    private int row;
    private int column;
    private int turn;

    public TTT_MoveData(String game_id, int player_id, LocalDateTime time, int row, int column, int turn) {
        this.game_id = game_id;
        this.player_id = player_id;
        this.time = time;
        this.row = row;
        this.column = column;
        this.turn = turn;
    }

    public String getGame_id() {
        return game_id;
    }

    public void setGame_id(String game_id) {
        this.game_id = game_id;
    }

    public int getPlayer_id() {
        return player_id;
    }

    public void setPlayer_id(int player_id) {
        this.player_id = player_id;
    }

    public LocalDateTime getTime() {
        return time;
    }

    public void setTime(LocalDateTime time) {
        this.time = time;
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public int getColumn() {
        return column;
    }

    public void setColumn(int column) {
        this.column = column;
    }

    public int getTurn() {
        return turn;
    }

    public void setTurn(int turn) {
        this.turn = turn;
    }
}