package Messages;

import DataClasses.User;

import java.io.Serializable;

public class AccountSuccessfulMessage implements Serializable {

    public AccountSuccessfulMessage() {
    }

    @Override
    public String toString() {
        return "Account Created Successfully";
    }
}
