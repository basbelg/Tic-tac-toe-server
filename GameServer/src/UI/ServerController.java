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
    public Label errorModifyLabel;
    public Label errorActiveLabel;
    public Label errorCompletedLabel;
    private List<Client> onlinePlayers = new ArrayList<>();
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
            mpc.passInfo(selectedPlayer, allPlayers);
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
        catch(IndexOutOfBoundsException e)
        {
            if(registeredPlayersList.getItems().isEmpty())
            {
                errorModifyLabel.setText("There Are No Registered Players");
                errorModifyLabel.setVisible(true);
            }
            else if(registeredPlayersList.getSelectionModel().getSelectedIndex() < 0)
            {
                errorModifyLabel.setText("Please Select a Player To Modify");
                errorModifyLabel.setVisible(true);
            }
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
            gdc.passInfo(id, allPlayers);
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
        catch(IndexOutOfBoundsException | NullPointerException e)
        {
            if(activeGamesList.getItems().isEmpty())
            {
                errorActiveLabel.setText("There Are No Active Games");
                errorActiveLabel.setVisible(true);
            }
            else if(activeGamesList.getSelectionModel().getSelectedIndex() < 0)
            {
                errorActiveLabel.setText("Please Select a Game To View");
                errorActiveLabel.setVisible(true);
            }
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
            gdc.passInfo(id, allPlayers);
            Stage stage = (Stage) inactiveGameDetailsButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Completed Game Details");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException e){e.printStackTrace();}
        catch(IndexOutOfBoundsException | NullPointerException e)
        {
            if(inactiveGamesList.getItems().isEmpty())
            {
                errorCompletedLabel.setText("There Are No Completed Games");
                errorCompletedLabel.setVisible(true);
            }
            else if(inactiveGamesList.getSelectionModel().getSelectedIndex() < 0)
            {
                errorCompletedLabel.setText("Please Select a Game To View");
                errorCompletedLabel.setVisible(true);
            }
        }
    }

    @Override
    public void update(Serializable msg, Object data) {
        Platform.runLater(() -> {
            switch (msg.getClass().getSimpleName()) {
                case "EncapsulatedMessage": //KEEP ENCAPSULATEDMESSAGE AND ACCOUNTSUCCESSFULMESSAGE CASES SEPARATE
                case "DeactivateAccountMessage":
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
                            user = getUserFromList(game.getPlayer2Id());
                            String p2 = (user != null)? user.getUsername() + " (ID: " + game.getPlayer2Id() + ")" : game.getPlayer2Id() == 1 ? "AI Player (ID: 1)" : "<Deleted Account> ";
                            inactiveGamesList.getItems().add(new Label(p1 + " vs " + p2 + " \n(" + game.getId() + ")"));
                        }
                    }
                    errorCompletedLabel.setVisible(false);
                    break;

                case "RegisteredUsersMessage":
                    // update registered player list
                    errorModifyLabel.setVisible(false);
                    registeredPlayersList.getItems().clear();
                    allPlayers = ((RegisteredUsersMessage)msg).getUsers();
                    for(Object obj: allPlayers) {
                        User user = (User) obj;
                        registeredPlayersList.getItems().add(new Label(user.getUsername() + " (ID: " + user.getId() + ")"));
                    }
                    break;

                case "UpdateAccountInfoMessage":
                    SQLServiceConnection.getInstance().sendPacket(new Packet("RUS-MSG", new RegisteredUsersMessage()));
                    onlinePlayerList.getItems().clear();
                    Client client = null;
                    synchronized (onlinePlayers) {
                        Iterator<Client> iterator = onlinePlayers.iterator();
                        while (iterator.hasNext()) {
                            client = iterator.next();
                            if (client.getUser() != null && client.getUser().getId() != 0) {
                                onlinePlayerList.getItems().add(new Label(client.getUser().getUsername() + " (ID: " + client.getUser().getId() + ")" + "\n" + client.getCurrentGameId()));
                            }
                        }
                    }

                case "GameResultMessage":
                    // update completed games list
                    String game_id = (String) data;

                    TTT_GameData game = MainServer.getInstance().getGame_by_id().get(game_id);
                    String p1 = getUserFromList(game.getPlayer1Id()).getUsername() + " (ID: " + game.getPlayer1Id() + ")";
                    String p2 = (game.getPlayer2Id() == 1) ? "AI Player (ID: 1)" : getUserFromList(game.getPlayer2Id()).getUsername() + " (ID: " + game.getPlayer2Id() + ")";
                    inactiveGamesList.getItems().add(new Label(p1 + " vs " + p2 + " \n(" + game.getId() + ")"));
                    errorCompletedLabel.setVisible(false);
                case "ConnectToLobbyMessage":
                case "CreateLobbyMessage":
                case "CreateAIGameMessage":
                    // update active games
                    activeGamesList.getItems().clear();
                    synchronized (MainServer.getInstance().getActiveGames()) {
                        Iterator<TTT_GameData> iterator = MainServer.getInstance().getActiveGames().iterator();
                        while (iterator.hasNext()) {
                            game = iterator.next();
                            if (game.getPlayer2Id() != 0) {
                                p1 = getUserFromList(game.getPlayer1Id()).getUsername() + " (ID: " + game.getPlayer1Id() + ")";
                                p2 = (game.getPlayer2Id() == 1) ? "AI Player (ID: 1)" : getUserFromList(game.getPlayer2Id()).getUsername() + " (ID: " + game.getPlayer2Id() + ")";
                                activeGamesList.getItems().add(new Label(p1 + " vs " + p2 + " \n(" + game.getId() + ")"));
                            }
                        }
                    }
                    errorActiveLabel.setVisible(false);

                case "StopSpectatingMessage":
                case "SpectateMessage":
                case "LoginSuccessfulMessage":
                case "DisconnectMessage":
                case "InactiveGameMessage":
                    onlinePlayerList.getItems().clear();
                    client = null;
                    synchronized (onlinePlayers) {
                        Iterator<Client> iterator = onlinePlayers.iterator();
                        while (iterator.hasNext()) {
                            client = iterator.next();
                            if (client.getUser() != null && client.getUser().getId() != 0) {
                                onlinePlayerList.getItems().add(new Label(client.getUser().getUsername() + " (ID: " + client.getUser().getId() + ")" + "\n" + client.getCurrentGameId()));
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

    public void onPlayerListClicked()
    {
        errorModifyLabel.setVisible(false);
    }

    public void onActiveGamesListClicked()
    {
        errorActiveLabel.setVisible(false);
    }

    public void onCompletedGamesListClicked()
    {
        errorCompletedLabel.setVisible(false);
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        errorActiveLabel.setVisible(false);
        errorCompletedLabel.setVisible(false);
        errorModifyLabel.setVisible(false);
        onlinePlayers = MainServer.getInstance().getClients();
        MainServer.getInstance().addObserver(this);
        MainServer.getInstance().notifyObservers(new EncapsulatedMessage(null, null, null), null);
    }

    public void passInfo(List<Object> allPlayers)
    {
        this.allPlayers = allPlayers;
        MainServer.getInstance().notifyObservers((LoginSuccessfulMessage) MessageFactory.getMessage("LOS-MSG"), null);
        MainServer.getInstance().notifyObservers((CreateAIGameMessage) MessageFactory.getMessage("CAI-MSG"), null);
    }
}
