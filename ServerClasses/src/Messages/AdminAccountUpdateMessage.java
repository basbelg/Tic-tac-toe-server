package Messages;

import java.io.Serializable;

public class AdminAccountUpdateMessage implements Serializable {
    private UpdateAccountInfoMessage UPA;
    private int id;

    public AdminAccountUpdateMessage(int id, UpdateAccountInfoMessage UPA) {
        this.id = id;
        this.UPA = UPA;
    }

    public UpdateAccountInfoMessage getUPA() {return UPA;}
    public int getId() {return id;}
}
