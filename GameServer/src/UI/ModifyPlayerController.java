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
    public Label errorFNameLabel;
    public Label errorLNameLabel;
    public Label errorUsernameLabel;
    public Label errorPasswordLabel;
    public Label errorConfirmPasswordLabel;
    public Label errorLabel;
    private User player;
    private List<Object> allPlayers;

    public void onConfirmClicked()
    {

        errorFNameLabel.setText("");
        errorLNameLabel.setText("");
        errorUsernameLabel.setText("");
        errorPasswordLabel.setText("");
        errorConfirmPasswordLabel.setText("");
        errorLabel.setText("");

        if(!enterFirstName.getText().equals("") && !enterLastName.getText().equals("") && !enterUsername.getText().equals("") &&
                !enterPassword.getText().equals("") && !enterConfirmPassword.getText().equals("") &&
                enterPassword.getText().equals(enterConfirmPassword.getText()))
        {
            player.setUsername(enterUsername.getText());
            player.setFirstName(enterFirstName.getText());
            player.setLastName(enterLastName.getText());
            player.setPassword(enterPassword.getText());

            confirmButton.setDisable(true);
            cancelButton.setDisable(true);
            UpdateAccountInfoMessage UPA = (UpdateAccountInfoMessage) MessageFactory.getMessage("UPA-MSG");
            UPA.setUpdatedUser(player);
            SQLServiceConnection.getInstance().sendPacket(new Packet("AAU-MSG",  new AdminAccountUpdateMessage(UPA.getUpdatedUser().getId(), UPA)));
        }
        else
        {
            Platform.runLater(() -> {
                if (enterFirstName.getText().equals("")) {
                    errorFNameLabel.setText("Please enter your first name!\n");
                }
                if (enterLastName.getText().equals("")) {
                    errorLNameLabel.setText("Please enter your last name!\n");
                }
                if (enterUsername.getText().equals("")) {
                    errorUsernameLabel.setText("Please enter a valid username!\n");
                }
                if (enterPassword.getText().equals("")) {
                    errorPasswordLabel.setText("Please enter a valid password!\n");
                }
                if (!enterPassword.getText().equals(enterConfirmPassword.getText()) && !enterPassword.getText().equals("")) {
                    errorConfirmPasswordLabel.setText("Passwords do NOT match!\n");
                }
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
    public void update(Serializable msg, Object data)
    {
        Platform.runLater(() -> {
            switch(msg.getClass().getSimpleName())
            {
                case "AdminAccountFailedMessage":
                    errorLabel.setText(msg.toString());
                    confirmButton.setDisable(false);
                    cancelButton.setDisable(false);
                    break;
                case "AdminAccountSuccessfulMessage":
                    try {
                        FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));
                        Parent root = loader.load();
                        ServerController sc = loader.getController();
                        MainServer.getInstance().removeObserver(this);
                        sc.passInfo(allPlayers);
                        Stage stage = (Stage) confirmButton.getScene().getWindow();
                        stage.close();
                        stage.setTitle("Server");
                        stage.setScene(new Scene(root));
                        stage.show();
                    }
                    catch(IOException e) {e.printStackTrace();}
                    break;
            }
        });

    }
}
