package UI;

import DataClasses.Spectator;
import DataClasses.TTT_ViewerData;
import GameInterfaces.Game;
import MainServer.*;
import Messages.*;
import ServerInterfaces.ServerListener;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.layout.GridPane;
import javafx.scene.text.Font;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class GameDetailsController implements Initializable, ServerListener
{
    public GridPane board;
    public Button previousButton;
    public Button nextButton;
    public Button cancelButton;
    public Label moveNumLabel;
    public Label playerTurnLabel;
    public Label outPlayersTurnLabel;
    public Label player1Label;
    public Label player2Label;
    public Label startTimeLabel;
    public Label endTimeLabel;
    public Label winnerLabel;
    public Label moveTimeLabel;
    public ListView spectatorsList;
    public Label tile00;
    public Label tile01;
    public Label tile02;
    public Label tile10;
    public Label tile11;
    public Label tile12;
    public Label tile20;
    public Label tile21;
    public Label tile22;
    private int moveCounter = 0;
    private String turn = "X";
    private AllGameInfoMessage gameData;
    private List<Object> allPlayers;

    public void onCancelClicked()
    {
        try
        {
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
        catch(IOException e)
        {
            e.printStackTrace();
        }
    }

    public void passInfo(String game_id, List<Object> allPlayers) {                                                  // pass proper id
        gameData = new AllGameInfoMessage();
        gameData.setId(game_id);
        SQLServiceConnection.getInstance().sendPacket(new Packet("AGI-MSG", gameData)); // handle message
        List<Node> boardTiles = new ArrayList<>();
        boardTiles.add(tile00);
        boardTiles.add(tile01);
        boardTiles.add(tile02);
        boardTiles.add(tile10);
        boardTiles.add(tile11);
        boardTiles.add(tile12);
        boardTiles.add(tile20);
        boardTiles.add(tile21);
        boardTiles.add(tile22);
        this.allPlayers = allPlayers;

        for(Node node : boardTiles) {
            Label tile = (Label) node;
            tile.setTextAlignment(TextAlignment.CENTER);
            tile.setFont(new Font(36));
        }
    }

    public void onNextClicked()
    {
        Platform.runLater(() -> {
            if(moveCounter < gameData.getGameLog().getMoveHistory().size())
            {
                moveCounter++;

                placeMove();
                moveNumLabel.setText((moveCounter + 1) + "/" + gameData.getGameLog().getMoveHistory().size());
                LocalDateTime time = gameData.getGameLog().getMoveHistory().get(moveCounter).getTimeMade();
                moveTimeLabel.setText((time.getMonth().toString()) + " " +
                        time.getDayOfMonth() + ", " + time.getYear() + "\n at " + (time.getHour() < 10 ? ("0" + time.getHour()) : time.getHour()) +
                        ":" + (time.getMinute() < 10 ? ("0" + time.getMinute()) : time.getMinute()) +
                        ":" + (time.getSecond() < 10 ? ("0" + time.getSecond()) : time.getSecond()));
                playerTurnLabel.setText((playerTurnLabel.getText()).equals(gameData.getGameLog().getPlayer1Username() + "\'s move!") ? (gameData.getGameLog().getPlayer2Username() + "\'s move!") : (gameData.getGameLog().getPlayer1Username() + "\'s move!"));

                if (moveCounter >= (gameData.getGameLog().getMoveHistory().size() - 1)) {
                    nextButton.setDisable(true);
                    if(gameData.getGameLog().getWinner() != null)
                    {
                        winnerLabel.setText("Winner: " + gameData.getGameLog().getWinner());
                    }
                } if (previousButton.isDisable()) {
                previousButton.setDisable(false);
            }
            }
        });
    }

    public void onPreviousClicked()
    {
        swapTurn();
        Platform.runLater(() -> {
            if(moveCounter >= 0)
            {
                int x = gameData.getGameLog().getMoveHistory().get(moveCounter).getNextMove().getRow();
                int y = gameData.getGameLog().getMoveHistory().get(moveCounter).getNextMove().getColumn();
                moveCounter--;

                if(x == 0 && y == 0)
                {
                    tile00.setText("");
                }
                else if(x == 0 && y == 1)
                {
                    tile01.setText("");
                }
                else if(x == 0 && y == 2)
                {
                    tile02.setText("");
                }
                else if(x == 1 && y == 0)
                {
                    tile10.setText("");
                }
                else if(x == 1 && y == 1)
                {
                    tile11.setText("");
                }
                else if(x == 1 && y == 2)
                {
                    tile12.setText("");
                }
                else if(x == 2 && y == 0)
                {
                    tile20.setText("");
                }
                else if(x == 2 && y == 1)
                {
                    tile21.setText("");
                }
                else if(x == 2 && y == 2)
                {
                    tile22.setText("");
                }
                if(!gameData.getGameLog().getMoveHistory().isEmpty())
                {
                    moveNumLabel.setText((moveCounter + 1) + "/" + gameData.getGameLog().getMoveHistory().size());
                }

                LocalDateTime time = gameData.getGameLog().getMoveHistory().get(moveCounter).getTimeMade();
                moveTimeLabel.setText((time.getMonth().toString()) + " " +
                        time.getDayOfMonth() + ", " + time.getYear() + "\n at " + (time.getHour() < 10 ? ("0" + time.getHour()) : time.getHour()) +
                        ":" + (time.getMinute() < 10 ? ("0" + time.getMinute()) : time.getMinute()) +
                        ":" + (time.getSecond() < 10 ? ("0" + time.getSecond()) : time.getSecond()));
                playerTurnLabel.setText((playerTurnLabel.getText()).equals(gameData.getGameLog().getPlayer1Username() + "\'s move!") ? (gameData.getGameLog().getPlayer2Username() + "\'s move!") : (gameData.getGameLog().getPlayer1Username() + "\'s move!"));

                if (moveCounter <= 0) {
                    previousButton.setDisable(true);
                } if (nextButton.isDisable()) {
                nextButton.setDisable(false);
                winnerLabel.setText("");
                outPlayersTurnLabel.setVisible(true);
                playerTurnLabel.setVisible(true);
            }
            }
        });
    }



    @Override
    public void initialize(URL url, ResourceBundle resourceBundle)
    { }

    @Override
    public void update(Serializable msg, Object data) {
        if(data instanceof String && gameData.getId().equals(data)) {
            Platform.runLater(() -> {
                switch (msg.getClass().getSimpleName()) {
                    case "MoveMessage": // If a new move comes in while observing active game
                        MoveMessage MOV = (MoveMessage) msg;
                        if(nextButton.isDisable())
                        {
                            nextButton.setDisable(false);
                        }
                        gameData.getGameLog().getMoveHistory().add(MOV.getMoveInfo());

                        if(moveNumLabel.getText().equals("0/0"))
                        {
                            placeMove();
                            LocalDateTime time = gameData.getGameLog().getMoveHistory().get(moveCounter).getTimeMade();
                            moveTimeLabel.setText((time.getMonth().toString()) + " " +
                                    time.getDayOfMonth() + ", " + time.getYear() + "\n at " + (time.getHour() < 10 ? ("0" + time.getHour()) : time.getHour()) +
                                    ":" + (time.getMinute() < 10 ? ("0" + time.getMinute()) : time.getMinute()) +
                                    ":" + (time.getSecond() < 10 ? ("0" + time.getSecond()) : time.getSecond()));
                        }

                        moveNumLabel.setText((moveCounter + 1)  + "/" + gameData.getGameLog().getMoveHistory().size());

                        // add new move to move list
                        break;

                    case "GameResultMessage": // If active game concludes
                        GameResultMessage GMR = (GameResultMessage) msg;
                        gameData.getGameLog().setWinner(GMR.getWinner());
                        gameData.getGameLog().setGameEnded(MainServer.getInstance().getGame_by_id().get(gameData.getId()).getEndTime());
                        LocalDateTime endtime = gameData.getGameLog().getGameEnded();
                        endTimeLabel.setText((endtime.getMonth().toString()) + " " +
                                endtime.getDayOfMonth() + ", " + endtime.getYear() + "\n at " + (endtime.getHour() < 10 ? ("0" + endtime.getHour()) : endtime.getHour()) +
                                ":" + (endtime.getMinute() < 10 ? ("0" + endtime.getMinute()) : endtime.getMinute()) +
                                ":" + (endtime.getSecond() < 10 ? ("0" + endtime.getSecond()) : endtime.getSecond()));

                        if (moveCounter >= (gameData.getGameLog().getMoveHistory().size() - 1)) {
                            if(gameData.getGameLog().getWinner() != null)
                            {
                                winnerLabel.setText("Winner: " + gameData.getGameLog().getWinner());
                            }
                        }
                        break;

                    case "SpectateMessage":
                        SpectateMessage SPC = (SpectateMessage) msg;
                        boolean found = false;
                        if(gameData.getGameViewers().getSpectators().isEmpty())
                        {
                            spectatorsList.getItems().clear();
                        }
                        for(Spectator s : gameData.getGameViewers().getSpectators())
                        {
                            if(s.getUsername().equals(MainServer.getInstance().getClientIDMap().get(SPC.getSpectatorId()).getUser().getUsername()))
                            {
                                found = true;
                                break;
                            }
                        }
                        if(!found)
                        {
                            gameData.getGameViewers().getSpectators().add(new Spectator(MainServer.getInstance().getClientIDMap().get(SPC.getSpectatorId()).getUser().getUsername()));
                            spectatorsList.getItems().add(new Label("User: " + MainServer.getInstance().getClientIDMap().get(SPC.getSpectatorId()).getUser().getUsername()));
                        }
                        break;

                    case "AllGameInfoMessage": // Pull game information from db
                        this.gameData = (AllGameInfoMessage) msg;
                        LocalDateTime starttime = gameData.getGameLog().getGameStarted();

                        startTimeLabel.setText((starttime.getMonth().toString()) + " " +
                                starttime.getDayOfMonth() + ", " + starttime.getYear() + "\n at " + (starttime.getHour() < 10 ? ("0" + starttime.getHour()) : starttime.getHour()) +
                                ":" + (starttime.getMinute() < 10 ? ("0" + starttime.getMinute()) : starttime.getMinute()) +
                                ":" + (starttime.getSecond() < 10 ? ("0" + starttime.getSecond()) : starttime.getSecond()));
                        if(gameData.getGameLog().getGameEnded() != null)
                        {
                            LocalDateTime end_time = gameData.getGameLog().getGameEnded();
                            endTimeLabel.setText((end_time.getMonth().toString()) + " " +
                                    end_time.getDayOfMonth() + ", " + end_time.getYear() + "\n at " + (end_time.getHour() < 10 ? ("0" + end_time.getHour()) : end_time.getHour()) +
                                    ":" + (end_time.getMinute() < 10 ? ("0" + end_time.getMinute()) : end_time.getMinute()) +
                                    ":" + (end_time.getSecond() < 10 ? ("0" + end_time.getSecond()) : end_time.getSecond()));
                        }

                        if(!gameData.getGameLog().getMoveHistory().isEmpty())
                        {
                            placeMove();
                            moveNumLabel.setText("1/" + gameData.getGameLog().getMoveHistory().size());
                            LocalDateTime time = gameData.getGameLog().getMoveHistory().get(moveCounter).getTimeMade();
                            moveTimeLabel.setText((time.getMonth().toString()) + " " +
                                    time.getDayOfMonth() + ", " + time.getYear() + "\n at " + (time.getHour() < 10 ? ("0" + time.getHour()) : time.getHour()) +
                                    ":" + (time.getMinute() < 10 ? ("0" + time.getMinute()) : time.getMinute()) +
                                    ":" + (time.getSecond() < 10 ? ("0" + time.getSecond()) : time.getSecond()));
                        }
                        else
                        {
                            nextButton.setDisable(true);
                        }

                        playerTurnLabel.setText(gameData.getGameLog().getPlayer1Username() + "\'s move!");
                        player1Label.setText(gameData.getGameLog().getPlayer1Username());
                        player2Label.setText(gameData.getGameLog().getPlayer2Username());

                        if (moveCounter >= (gameData.getGameLog().getMoveHistory().size() - 1)) {
                            if(gameData.getGameLog().getWinner() != null)
                            {
                                winnerLabel.setText("Winner: " + gameData.getGameLog().getWinner());
                            }
                        }

                        if(gameData.getGameViewers().getSpectators().isEmpty())
                        {
                            spectatorsList.getItems().add(new Label("No spectators for this game"));
                        }
                        else
                        {
                            for(Spectator spectator : gameData.getGameViewers().getSpectators())
                            {
                                spectatorsList.getItems().add(new Label("User: " + spectator.getUsername()));
                            }
                        }

                        //set other fxml elements
                        previousButton.setDisable(true);
                        break;
                }
            });
        }
    }

    private void placeMove()
    {
        if(!gameData.getGameLog().getMoveHistory().isEmpty())
        {
            int x = gameData.getGameLog().getMoveHistory().get(moveCounter).getNextMove().getRow();
            int y = gameData.getGameLog().getMoveHistory().get(moveCounter).getNextMove().getColumn();
            if(x == 0 && y == 0)
            {
                tile00.setText(turn);
                swapTurn();
            }
            else if(x == 0 && y == 1)
            {
                tile01.setText(turn);
                swapTurn();
            }
            else if(x == 0 && y == 2)
            {
                tile02.setText(turn);
                swapTurn();
            }
            else if(x == 1 && y == 0)
            {
                tile10.setText(turn);
                swapTurn();
            }
            else if(x == 1 && y == 1)
            {
                tile11.setText(turn);
                swapTurn();
            }
            else if(x == 1 && y == 2)
            {
                tile12.setText(turn);
                swapTurn();
            }
            else if(x == 2 && y == 0)
            {
                tile20.setText(turn);
                swapTurn();
            }
            else if(x == 2 && y == 1)
            {
                tile21.setText(turn);
                swapTurn();
            }
            else if(x == 2 && y == 2)
            {
                tile22.setText(turn);
                swapTurn();
            }
        }
    }

    private void swapTurn()
    {
        if(turn.equals("X"))
        {
            turn = "O";
        }
        else if(turn.equals("O"))
        {
            turn = "X";
        }
    }
}
