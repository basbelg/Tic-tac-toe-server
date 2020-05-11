package UI;

import DataClasses.Spectator;
import DataClasses.TTT_ViewerData;
import MainServer.*;
import Messages.AllGameInfoMessage;
import Messages.Packet;
import ServerInterfaces.ServerListener;
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
import java.io.Serializable;
import java.net.URL;
import java.util.ResourceBundle;

public class GameDetailsController implements Initializable, ServerListener
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
    private AllGameInfoMessage gameData;

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

    public void passInfo(String game_id) {
        gameData = new AllGameInfoMessage();
        gameData.setId(game_id);
        SQLServiceConnection.getInstance().sendPacket(new Packet("AGI-MSG", gameData));
    }

    public void onNextClicked()
    {

    }

    public void onPreviousClicked()
    {

    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {}

    @Override
    public void update(Serializable msg, Object data) {
        if(data instanceof String && gameData.getId().equals(data)) {
            Platform.runLater(() -> {
                switch (msg.getClass().getSimpleName()) {
                    case "MoveMessage": // If a new move comes in while observing active game

                        break;

                    case "GameResultMessage": // If active game concludes
                        break;

                    case "AllGameInfoMessage": // Pull game information from db
                        this.gameData = (AllGameInfoMessage) msg;

                        startTimeLabel.setText(gameData.getGameLog().getGameStarted().toString());
                        endTimeLabel.setText(gameData.getGameLog().getGameEnded().toString());

                        int x = gameData.getGameLog().getMoveHistory().get(0).getNextMove().getRow();
                        int y = gameData.getGameLog().getMoveHistory().get(0).getNextMove().getColumn();
                        board.add(new Label("X"), y, x);

                        for(Spectator viewer: gameData.getGameViewers().getSpectators())
                            spectatorsList.getItems().add(new Label(viewer.getUsername()));

                        //FIND WAY TO DIFFERENTIATE BETWEEN SPECTATORS AND USERS (INSTANCEOF OR IDS) \\ SINGLETON MAINSERVER GET PUBLISHER
                        /* POPULATE SPECTATOR LIST
                        for(GameListener gl: game.getObservers())
                        {
                            spectatorsList.getItems().add(new Label(gl.getObserverUsername()));
                        }*/

                        //set other fxml elements
                        previousButton.setDisable(true);
                        break;
                }
            });
        }
    }
}
