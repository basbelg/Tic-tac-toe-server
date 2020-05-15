package UI;

import MainServer.*;
import DataClasses.User;
import Messages.*;
import ServerInterfaces.ServerListener;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class ModifyPlayerController implements Initializable, ServerListener
{
    public Button confirmButton;
    public Button cancelButton;
    public TextField enterUsername;
    public TextField enterFirstName;
    public TextField enterLastName;
    public TextField enterPassword;
    public TextField enterConfirmPassword;
    public Label errorLabel;
    private User player;
    private List<Object> allPlayers;

    public void onConfirmClicked()
    {
        if(!enterFirstName.getText().equals("") && !enterLastName.getText().equals("") && !enterUsername.getText().equals("") &&
                !enterPassword.getText().equals("") && !enterConfirmPassword.getText().equals("") &&
                enterPassword.getText().equals(enterConfirmPassword.getText()))
        {
            player.setUsername(enterUsername.getText());
            player.setFirstName(enterFirstName.getText());
            player.setLastName(enterLastName.getText());
            player.setPassword(enterPassword.getText());

            UpdateAccountInfoMessage UPA = (UpdateAccountInfoMessage) MessageFactory.getMessage("UPA-MSG");
            UPA.setUpdatedUser(player);

            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));
                Parent root = loader.load();
                ServerController sc = loader.getController();
                MainServer.getInstance().removeObserver(this);
                MainServer.getInstance().addObserver(sc);
                Stage stage = (Stage) confirmButton.getScene().getWindow();
                stage.close();
                stage.setTitle("Server");
                stage.setScene(new Scene(root));
                stage.show();
            }
            catch(IOException e) {e.printStackTrace();}

            SQLServiceConnection.getInstance().sendPacket(new Packet("AAU-MSG",  new AdminAccountUpdateMessage(UPA.getUpdatedUser().getId(), UPA)));
        }
        else
        {
            Platform.runLater(() -> {
                StringBuffer error = new StringBuffer();
                if (enterFirstName.getText().equals(""))
                    error.append("Please enter a first name!\n");
                if (enterLastName.getText().equals(""))
                    error.append("Please enter a last name!\n");
                if (enterUsername.getText().equals(""))
                    error.append("Please enter a valid username!\n");
                if (enterPassword.getText().equals(""))
                    error.append("Please enter a valid password!\n");
                if (!enterPassword.getText().equals(enterConfirmPassword.getText()) && !enterPassword.getText().equals(""))
                    error.append("Passwords do NOT match!\n");

                errorLabel.setText(error.toString());
            });
        }

    }

    public void onCancelClicked() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));
            Parent root = loader.load();
            ServerController sc = loader.getController();
            MainServer.getInstance().removeObserver(this);
            sc.passInfo(allPlayers);
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Server");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException e) {e.printStackTrace();}
    }

    public void passInfo(User player, List<Object> allPlayers) {
        this.player = player;
        this.allPlayers = allPlayers;

        enterUsername.setText(player.getUsername());
        enterFirstName.setText(player.getFirstName());
        enterLastName.setText(player.getLastName());
        enterPassword.setText(player.getPassword());
        enterConfirmPassword.setText(player.getPassword());
    }

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    @Override
    public void update(Serializable msg, Object data) {}
}
