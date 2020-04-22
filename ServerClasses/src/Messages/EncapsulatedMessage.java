package Messages;

import java.io.Serializable;

public class EncapsulatedMessage implements Serializable {
	private String type;
	private Serializable identifier;
	private Serializable message;
	
	public EncapsulatedMessage(String type, Serializable identifier, Serializable msg){
		this.type = type;
		this.identifier = identifier;
		message = msg;
	}
	
	public String getType() {return type;}
	public Serializable getidentifier() {return identifier;}
	public Serializable getMsg() {return message;}
}