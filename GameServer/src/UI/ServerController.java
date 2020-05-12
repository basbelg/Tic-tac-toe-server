package UI;

import DataClasses.TTT_GameData;
import DataClasses.User;
import Database.DBManager;
import MainServer.Client;
import MainServer.*;
import Messages.*;
import ServerInterfaces.ServerListener;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.ResourceBundle;

public class ServerController implements Initializable, ServerListener
{
    public ListView registeredPlayersList;
    public ListView onlinePlayerList;
    public Button modifyPlayerButton;
    public ListView activeGamesList;
    public Button activeGameDetailsButton;
    public ListView inactiveGamesList;
    public Button inactiveGameDetailsButton;
    private List<User> onlinePlayers = new ArrayList<>();
    private List<Object> allPlayers = new ArrayList<>();


    public void onModifyPlayerClicked()
    {
        try
        {
            // retrieve selected user
            int usernameIndex = registeredPlayersList.getSelectionModel().getSelectedIndex();
            User selectedPlayer = (User) allPlayers.get(usernameIndex);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ModifyPlayer.fxml"));
            Parent root = loader.load();
            ModifyPlayerController mpc = loader.getController();
            MainServer.getInstance().removeObserver(this);
            MainServer.getInstance().addObserver(mpc);
            mpc.passInfo(selectedPlayer);
            Stage stage = (Stage) modifyPlayerButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Modify Player");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException | NullPointerException e)
        {
            e.printStackTrace();
        }
    }

    public void onActiveGameDetailsClicked()
    {
        try
        {
            // retrieve selected game id
            String selected = ((Label)activeGamesList.getSelectionModel().getSelectedItem()).getText();
            String id = selected.substring(selected.lastIndexOf('(') + 1, selected.lastIndexOf(')'));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameDetails.fxml"));
            Parent root = loader.load();
            GameDetailsController gdc = loader.getController();
            MainServer.getInstance().removeObserver(this);
            MainServer.getInstance().addObserver(gdc);
            gdc.passInfo(id);
            Stage stage = (Stage) activeGameDetailsButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Active Game Details");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void onInactiveGameDetailsClicked()
    {
        try
        {
            // retrieve selected game id
            String selected = ((Label)inactiveGamesList.getSelectionModel().getSelectedItem()).getText();
            String id = selected.substring(selected.lastIndexOf('(') + 1, selected.lastIndexOf(')'));

            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameDetails.fxml"));
            Parent root = loader.load();
            GameDetailsController gdc = loader.getController();
            MainServer.getInstance().removeObserver(this);
            MainServer.getInstance().addObserver(gdc);
            gdc.passInfo(id);
            Stage stage = (Stage) inactiveGameDetailsButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Completed Game Details");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException e){e.printStackTrace();}
    }

    @Override
    public void update(Serializable msg, Object data) {
        Platform.runLater(() -> {
            switch (msg.getClass().getSimpleName()) {
                case "EncapsulatedMessage": //KEEP ENCAPSULATEDMESSAGE AND ACCOUNTSUCCESSFULMESSAGE CASES SEPARATE
                    SQLServiceConnection.getInstance().sendPacket(new Packet("RUS-MSG", new RegisteredUsersMessage()));
                    SQLServiceConnection.getInstance().sendPacket(new Packet("AGS-MSG", new AllGamesMessage()));
                    break;
                case "AccountSuccessfulMessage":
                    SQLServiceConnection.getInstance().sendPacket(new Packet("RUS-MSG", new RegisteredUsersMessage()));
                    break;

                case "AllGamesMessage":
                    List<Object> all_games = ((AllGamesMessage) msg).getGames();
                    inactiveGamesList.getItems().clear();
                    for(Object obj: all_games) {
                        TTT_GameData game = (TTT_GameData) obj;
                        if (game.getWinningPlayerId() != -1) {
                            User user = getUserFromList(game.getPlayer1Id());
                            String p1 = ((user == null) ? "<Deleted Account> " : user.getUsername()) + " (ID: " + game.getPlayer1Id() + ")";
                            String p2 = (game.getPlayer2Id() == 1) ? "AI Player (ID: 1)" : getUserFromList(game.getPlayer2Id()).getUsername() + " (ID: " + game.getPlayer2Id() + ")";
                            inactiveGamesList.getItems().add(new Label(p1 + " vs " + p2 + " (" + game.getId() + ")"));
                        }
                    }
                    break;

                case "RegisteredUsersMessage":
                    // update registered player list
                    registeredPlayersList.getItems().clear();
                    allPlayers = ((RegisteredUsersMessage)msg).getUsers();
                    for(Object obj: allPlayers) {
                        User user = (User) obj;
                        registeredPlayersList.getItems().add(new Label(user.getUsername() + " (ID: " + user.getId() + ")"));
                    }
                    break;

                case "UpdateAccountInfoMessage":
                    SQLServiceConnection.getInstance().sendPacket(new Packet("RUS-MSG", new RegisteredUsersMessage()));
                case "LoginSuccessfulMessage":
                case "DeactivateAccountMessage":
                    onlinePlayerList.getItems().clear();
                    Client client = null;
                    synchronized (MainServer.getInstance().getClients()) {
                        Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                        while (iterator.hasNext()) {
                            client = iterator.next();
                            if (client.getUser() != null && client.getUser().getId() != 0) {
                                onlinePlayerList.getItems().add(new Label(client.getUser().getUsername() + " (ID: " + client.getUser().getId() + ")"));
                            }
                        }
                    }
                    break;

                case "GameResultMessage":
                    // update completed games list
                    String game_id = (String) data;
                    TTT_GameData game = MainServer.getInstance().getGame_by_id().get(game_id);
                    String p1 = getUserFromList(game.getPlayer1Id()).getUsername() + " (ID: " + game.getPlayer1Id() + ")";
                    String p2 = (game.getPlayer2Id() == 1) ? "AI Player (ID: 1)" : getUserFromList(game.getPlayer1Id()).getUsername() + " (ID: " + game.getPlayer2Id() + ")";
                    inactiveGamesList.getItems().add(new Label(p1 + " vs " + p2 + " (" + game.getId() + ")"));
                case "ConnectToLobbyMessage":
                case "CreateAIGameMessage":
                    // update active games
                    activeGamesList.getItems().clear();
                    synchronized (MainServer.getInstance().getActiveGames()) {
                        Iterator<TTT_GameData> iterator = MainServer.getInstance().getActiveGames().iterator();
                        while (iterator.hasNext()) {
                            game = iterator.next();
                            if (game.getPlayer2Id() != 0) {
                                p1 = getUserFromList(game.getPlayer1Id()).getUsername() + " (ID: " + game.getPlayer1Id() + ")";
                                p2 = (game.getPlayer2Id() == 1) ? "AI Player (ID: 1)" : getUserFromList(game.getPlayer1Id()).getUsername() + " (ID: " + game.getPlayer2Id() + ")";
                                activeGamesList.getItems().add(new Label(p1 + " vs " + p2 + " (" + game.getId() + ")"));
                            }
                        }
                    }
                    break;
            }
        });
    }

    private User getUserFromList(int id)
    {
        for(Object user : allPlayers) {
            if(((User) user).getId() == id) {
                return (User) user;
            }
        }
        return null;
    }

    private boolean isUserInOnlineList(int id)
    {
        for(User user : onlinePlayers) {
            if(user.getId() == id) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        MainServer.getInstance().addObserver(this);
        MainServer.getInstance().notifyObservers(new EncapsulatedMessage(null, null, null), null);
    }
}
