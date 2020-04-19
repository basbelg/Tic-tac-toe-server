package GameService;

import DataClasses.TTT_GameData;
import Database.DBManager;
import GameInterfaces.Move;
import Messages.*;
import TicTacToe.TTT_Game;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class GameHandler implements Runnable{
    private static GameHandler instance = new GameHandler();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private HashMap<String, TTT_Game> games;
    //private HashMap<String, TTT_GameData> gameData;

    private GameHandler() {
        requests = GameServer.getInstance().getRequests();

        games = new HashMap<>();
        gameData = new HashMap<>();

        thread = new Thread(this);
        thread.run();
    }

    public static GameHandler getInstance() {return instance;}

    @Override
    public void run() {
        try {
            while(!thread.isInterrupted()) {
                Packet packet = requests.take();

                if(packet.getType() != "ENC-MSG")
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();

                switch (ENC.getType()) {
                    case "CNT-MSG": // Connect to lobby
                        ConnectToLobbyMessage CNT = (ConnectToLobbyMessage) ENC.getMsg();

                        try {
                            TTT_Game ttt_game = games.get(CNT.getLobbyGameId());
                            ttt_game.startGame();
                            CNT.setLobbyGameId(ttt_game.getGameId());
                            GameServer.getInstance().sendPacket(packet);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

//                        TTT_GameData ttt_gameData = gameData.get(CNT.getLobbyGameId());
//
//                        if(ttt_gameData.getStartingTime() == null) {
//                            TTT_GameData ttt_gameData_updated = new TTT_GameData(ttt_game.getGameId(), CNT.getStartTime(), ttt_gameData.getPlayer1Id(), ttt_gameData.getPlayer2Id(), ttt_gameData.getPlayer1Id());
//                            gameData.remove(CNT.getLobbyGameId());
//                            gameData.put(ttt_game.getGameId(), ttt_gameData_updated);
//                        }
                        break;
                    case "CAI-MSG": // Create AI Game Lobby
                        CreateAIGameMessage CAI = (CreateAIGameMessage) ENC.getMsg();

                        try {
                            TTT_Game ttt_game = new TTT_Game();
                            ttt_game.startGame();
                            games.putIfAbsent(ttt_game.getGameId(), ttt_game);
                            CAI.setGameLobbyId(ttt_game.getGameId());
                            GameServer.getInstance().sendPacket(packet);
                        } catch (Exception e) {e.printStackTrace();}

                        //ttt_gameData = new TTT_GameData(ttt_game.getGameId(), CAI.getStartTime(), CAI.getPlayer1Id(), 0, CAI.getPlayer1Id());
                        //gameData.putIfAbsent(ttt_game.getGameId(), ttt_gameData);
                        break;
                    case "CLB-MSG": // Create Game Lobby
                        CreateLobbyMessage CLB = (CreateLobbyMessage) ENC.getMsg();

                        TTT_Game ttt_game = new TTT_Game();
                        games.putIfAbsent(ttt_game.getGameId(), ttt_game);
                        CLB.setGameLobbyId(ttt_game.getGameId());
                        GameServer.getInstance().sendPacket(packet);

                       // ttt_gameData = new TTT_GameData(ttt_game.getGameId(), null, CLB.getPlayer1Id(), 0, CLB.getPlayer1Id());
                      //  gameData.putIfAbsent(ttt_game.getGameId(), ttt_gameData);
                        break;
                    case "MOV-MSG": // Move
                        MoveMessage MOV = (MoveMessage) ENC.getMsg();

                        TTT_Game current_game = games.get(MOV.getGameId());
                        try {
                            current_game.performMove(MOV.getMoveInfo().getNextMove());
                            if(current_game.isFinished()) {
                                GameResultMessage GRE = (GameResultMessage) MessageFactory.getMessage("GRE-MSG");
                                GRE.setWinner(String.valueOf(current_game.getWinner()));
                                EncapsulatedMessage ENC_GRE = new EncapsulatedMessage("GRE-MSG", ENC.getidentifier(), GRE);
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            EncapsulatedMessage ENC_ILM = new EncapsulatedMessage("ILM-MSG", ENC.getidentifier(), (IllegalMoveMessage) MessageFactory.getMessage("ILM-MSG"));
                            GameServer.getInstance().sendPacket(new Packet("ENC-MSG", ENC_ILM));
                        }

                        break;
                }
            }
        } catch (IOException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public synchronized void terminateServer() {
        thread.interrupt();
        requests = null;
    }
}
