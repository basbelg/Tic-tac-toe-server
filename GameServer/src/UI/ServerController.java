package UI;

import DataClasses.User;
import GameInterfaces.Game;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ServerController implements Initializable
{
    public ListView registeredPlayersList;
    public Button modifyPlayerButton;
    public ListView activeGamesList;
    public Button activeGameDetailsButton;
    public ListView inactiveGamesList;
    public Button inactiveGameDetailsButton;
    private List<User> playerList;
    private List<Game> activeGameList;
    private List<Game> inactiveGameList;

    public void onModifyPlayerClicked()
    {
        try
        {
            int usernameIndex = registeredPlayersList.getSelectionModel().getSelectedIndex();
            User selectedPlayer = playerList.get(usernameIndex);

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
            //gdc.passInfo();
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



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    {
        //query for listview data and update UI
    }
}
