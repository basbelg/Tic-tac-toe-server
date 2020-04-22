package Messages;

import DataClasses.TTT_GameData;

import java.io.Serializable;

public class SaveGameMessage implements Serializable {
	private TTT_GameData game;
	private boolean insert;
	
	public SaveGameMessage(TTT_GameData game){
		this.game = game;
	}
	
	public void setInsert() {insert = true;}
	public void setUpdate() {insert = false;}
	public boolean isInsert() {return insert;}
	public boolean isUpdate() {return !insert;}
	public TTT_GameData getGame() {return game;}
}