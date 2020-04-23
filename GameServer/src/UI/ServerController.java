package UI;

import DataClasses.User;
import Database.DBManager;
import MainServer.Client;
import MainServer.MainServer;
import MainServer.SQLServiceConnection;
import Messages.AccountSuccessfulMessage;
import Messages.DeactivateAccountMessage;
import Messages.LoginSuccessfulMessage;
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

public class ServerController implements Initializable
{
    public ListView registeredPlayersList;
    public ListView onlinePlayerList;
    public Button modifyPlayerButton;
    public ListView activeGamesList;
    public Button activeGameDetailsButton;
    public ListView inactiveGamesList;
    public Button inactiveGameDetailsButton;
    private List<User> allPlayers = new ArrayList<>();


    public void onModifyPlayerClicked()
    {
        try
        {
            int usernameIndex = registeredPlayersList.getSelectionModel().getSelectedIndex();
            User selectedPlayer = allPlayers.get(usernameIndex);

            FXMLLoader loader = new FXMLLoader(getClass().getResource("ModifyPlayer.fxml"));
            Parent root = loader.load();
            ModifyPlayerController mpc = loader.getController();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameDetails.fxml"));
            Parent root = loader.load();
            GameDetailsController gdc = loader.getController();
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
            FXMLLoader loader = new FXMLLoader(getClass().getResource("GameDetails.fxml"));
            Parent root = loader.load();
            GameDetailsController gdc = loader.getController();
            //gdc.passInfo();
            Stage stage = (Stage) inactiveGameDetailsButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Inactive Game Details");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void update(Serializable msg) {
        Platform.runLater(() -> {
            if (msg instanceof LoginSuccessfulMessage || msg instanceof DeactivateAccountMessage) {
                onlinePlayerList.getItems().clear();
                Client client = null;
                synchronized (MainServer.getInstance().getClients()) {
                    Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                    while (iterator.hasNext()) {
                        client = iterator.next();
                        if (client.getUser() != null) {
                            if (client.getUser().getId() != 0) {
                                allPlayers.add(client.getUser());
                                onlinePlayerList.getItems().add(new Label(client.getUser().getUsername() + " (" + client.getUser().getFullName() + ")"));
                            }
                        }
                    }
                }
            }/* else if (msg instanceof LoginSuccessfulMessage) {
                for (Client c : MainServer.getInstance().getClients()) {
                    if (!c.getUser().equals(null)) {
                        onlinePlayerList.getItems().add(new Label(c.getUser().getUsername() + " (" + c.getUser().getFullName() + ")"));
                    }
                }
            }*/
        });
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
       SQLServiceConnection.getInstance().setListener(this);

       /*  //ALL Players
        List<Object> players = DBManager.getInstance().list(User.class);
        for(Object user : players)
        {
            if(((User) user).getId() != 0)
            {
                allPlayers.add((User) user);
                registeredPlayersList.getItems().add(new Label(((User) user).getUsername() + " (" + ((User) user).getFullName() + ")"));
            }
        }

        //ONLINE Players
        for(Client c : MainServer.getInstance().getClients())
        {
            if(!c.getUser().equals(null))
            {
                onlinePlayerList.getItems().add(new Label(c.getUser().getUsername() + " (" + c.getUser().getFullName() + ")"));
            }
        }*/
    }
}
