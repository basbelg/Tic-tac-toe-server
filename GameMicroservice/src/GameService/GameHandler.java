package GameService;

import DataClasses.TTT_GameData;
import Database.DBManager;
import GameInterfaces.Move;
import MainServer.Client;
import Messages.*;
import TicTacToe.TTT_Game;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class GameHandler implements Runnable{
    private static GameHandler instance = new GameHandler();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private HashMap<String, TTT_Game> games;
    private HashMap<String, List<Client>> subscribers;

    private GameHandler() {
        requests = GameServer.getInstance().getRequests();

        games = new HashMap<>();
        subscribers = new HashMap<>();

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
                            subscribers.get(ttt_game.getGameId()).add((Client) ENC.getidentifier());
                            CNT.setLobbyGameId(ttt_game.getGameId());
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                        break;
                    case "CAI-MSG": // Create AI Game Lobby
                        CreateAIGameMessage CAI = (CreateAIGameMessage) ENC.getMsg();

                        try {
                            TTT_Game ttt_game = new TTT_Game();
                            ttt_game.startGame();
                            games.putIfAbsent(ttt_game.getGameId(), ttt_game);
                            subscribers.putIfAbsent(ttt_game.getGameId(), new ArrayList<>());
                            subscribers.get(ttt_game.getGameId()).add((Client) ENC.getidentifier());
                            CAI.setGameLobbyId(ttt_game.getGameId());
                            ((Client)ENC.getidentifier()).sendPacket(new Packet("CAI-MSG", CAI));
                        } catch (Exception e) {e.printStackTrace();}
                        break;
                    case "CLB-MSG": // Create Game Lobby
                        CreateLobbyMessage CLB = (CreateLobbyMessage) ENC.getMsg();

                        TTT_Game ttt_game = new TTT_Game();
                        games.putIfAbsent(ttt_game.getGameId(), ttt_game);
                        subscribers.putIfAbsent(ttt_game.getGameId(), new ArrayList<>());
                        subscribers.get(ttt_game.getGameId()).add((Client) ENC.getidentifier());
                        CLB.setGameLobbyId(ttt_game.getGameId());
                        ((Client)ENC.getidentifier()).sendPacket(new Packet("CLB-MSG", CLB));
                        break;
                    case "MOV-MSG": // Move
                        MoveMessage MOV = (MoveMessage) ENC.getMsg();

                        TTT_Game current_game = games.get(MOV.getGameId());
                        try {
                            current_game.performMove(MOV.getMoveInfo().getNextMove());
                            if(current_game.isFinished()) {
                                GameResultMessage GRE = (GameResultMessage) MessageFactory.getMessage("GRE-MSG");
                                GRE.setWinner(subscribers.get(current_game.getGameId()).get(current_game.getWinner() - 1).getUser().getUsername());
                            }
                            else
                                for(Client client: subscribers.get(current_game.getGameId()))
                                    client.sendPacket(new Packet("MOV-MSG", MOV));
                        } catch (Exception e) {
                            e.printStackTrace();
                            EncapsulatedMessage ENC_ILM = new EncapsulatedMessage("ILM-MSG", ENC.getidentifier(), (IllegalMoveMessage) MessageFactory.getMessage("ILM-MSG"));
                            GameServer.getInstance().sendPacket(new Packet("ENC-MSG", ENC_ILM));
                        }
                        break;
                    case "SPC-MSG": // Spectate Message

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