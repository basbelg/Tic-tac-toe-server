package sample;

import DataClasses.*;
import Database.DBManager;

import javax.imageio.stream.ImageOutputStream;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

class DBManagerTest {
    @org.junit.jupiter.api.Test
    void addUser() {
        boolean wasSuccess;
        User user = new User("AI Player", null, null, null);

        wasSuccess = DBManager.getInstance().insert(user);

        if(wasSuccess)
            System.out.println(user.getId());
    }

    @org.junit.jupiter.api.Test
    void updateUser() {
        User user = (User) DBManager.getInstance().get(User.class, "rbradt");

        user.setUsername("updated_rbradt");

        DBManager.getInstance().update(user);
    }

    @org.junit.jupiter.api.Test
    void printAllActiveUsers() {
        List<Object> users = DBManager.getInstance().query(User.class, "active");

        System.out.println("Active Users:");
        for(Object obj: users) {
            User user = (User) obj;
            System.out.printf("id: %d | user: %s | pass: %s | first name: %s | last name: %s\n",
                    user.getId(), user.getUsername(), user.getPassword(), user.getFirstName(), user.getLastName());
        }
    }

    @org.junit.jupiter.api.Test
    void clearUsers() {
        List<Object> users = DBManager.getInstance().list(User.class);

        for(Object obj: users) {
            User user = (User) obj;
            DBManager.getInstance().delete(user);
        }
    }

    @org.junit.jupiter.api.Test
    void addGame() {
        TTT_GameData TTTGameDataLocal;
        TTT_GameData TTTGameDataFromDB;

        User startingPlayer = (User) DBManager.getInstance().get(User.class, "updated_rbradt");
        User player2 = (User) DBManager.getInstance().get(User.class, "rbradt2");
        TTTGameDataLocal = new TTT_GameData(UUID.randomUUID().toString(), LocalDateTime.now(), startingPlayer.getId(), player2.getId(), startingPlayer.getId());

        DBManager.getInstance().insert(TTTGameDataLocal);

        TTTGameDataFromDB = (TTT_GameData) DBManager.getInstance().get(TTT_GameData.class, TTTGameDataLocal.getId());

        System.out.printf("id: %s | start: %s | end: %s | p1: %d | p2: %d | sp: %d | wp: %d ",
                TTTGameDataFromDB.getId(), TTTGameDataFromDB.getStartingTime().toString(),
                (TTTGameDataFromDB.getEndTime() == null)? "null": TTTGameDataFromDB.getEndTime().toString(),
                TTTGameDataFromDB.getPlayer1Id(), TTTGameDataFromDB.getPlayer2Id(), TTTGameDataFromDB.getStartingPlayerId(),
                TTTGameDataFromDB.getWinningPlayerId());
    }
}