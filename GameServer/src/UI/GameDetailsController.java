package UI;

import DataClasses.TTT_GameData;
import GameInterfaces.Game;
import TicTacToe.TTT_Game;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;
import java.util.ResourceBundle;

public class GameDetailsController implements Initializable
{
    public GridPane board;
    public Button previousButton;
    public Button nextButton;
    public Button cancelButton;
    public Label moveNumLabel;
    public Label playerTurnLabel;
    public Label player1Label;
    public Label player2Label;
    public Label startTimeLabel;
    public Label endTimeLabel;
    public Label winnerLabel;
    public Label moveTimeLabel;
    public ListView spectatorsList;
    private TTT_GameData gameData;
    private TTT_Game game;

    public void onCancelClicked()
    {
        try
        {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("Server.fxml"));
            Parent root = loader.load();
            ServerController sc = loader.getController();
            Stage stage = (Stage) cancelButton.getScene().getWindow();
            stage.close();
            stage.setTitle("Server");
            stage.setScene(new Scene(root));
            stage.show();
        }
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void passInfo(TTT_Game game, TTT_GameData gameData)
    {
        this.gameData = gameData;
        this.game = game;

        Platform.runLater(() -> {
            startTimeLabel.setText(gameData.getStartingTime().toString());
            endTimeLabel.setText(gameData.getEndTime().toString());

            int x = game.getMoveHistory().get(0).getRow();
            int y = game.getMoveHistory().get(0).getColumn();
            board.add(new Label("X"), y, x);

            //set other fxml elements
            previousButton.setDisable(true);
        });
    }


    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}
}
