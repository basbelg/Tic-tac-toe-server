package DataClasses;

import java.io.Serializable;

public class User implements Serializable {
    private int id;
    private String username;
    private String firstName;
    private String lastName;
    private String password;
    private boolean isActive;

    public User(String username, String firstName, String lastName, String password) {
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        isActive = true;
    }

    public User(int id, String username, String password, String firstName, String lastName, boolean isActive) {
        this.id = id;
        this.username = username;
        this.firstName = firstName;
        this.lastName = lastName;
        this.password = password;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public String getFirstName() {
        return firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public String getFullName() {
        return lastName + ", " + firstName;
    }

    public String getPassword() {
        return password;
    }

    public boolean isActive() { return isActive; }

    public void setId(int id) {
        this.id = id;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public void setFullName(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public void setPassword(String password) {
        this.password = password;
    }
	
	public void setIsActive(boolean active) {
        this.isActive = active;
    }
}
