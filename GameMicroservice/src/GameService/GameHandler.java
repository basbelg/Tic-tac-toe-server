package GameService;

import MainServer.Client;
import Messages.*;
import TicTacToe.TTT_Board;
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
    //private HashMap<String, List<Client>> subscribers;

    private GameHandler() {
        requests = GameServer.getInstance().getRequests();

        games = new HashMap<>();

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
                            CAI.setGameLobbyId(ttt_game.getGameId());
                            ((Client)ENC.getidentifier()).sendPacket(new Packet("CAI-MSG", CAI));
                        } catch (Exception e) {e.printStackTrace();}
                        break;
                    case "CLB-MSG": // Create Game Lobby
                        CreateLobbyMessage CLB = (CreateLobbyMessage) ENC.getMsg();

                        TTT_Game ttt_game = new TTT_Game();
                        games.putIfAbsent(ttt_game.getGameId(), ttt_game);
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
                            }

                        } catch (Exception e) {
                            e.printStackTrace();
                            ((Client)ENC.getidentifier()).sendPacket(new Packet("ENC-MSG",(IllegalMoveMessage)MessageFactory.getMessage("ILM-MSG")));
                        }
                        break;
                    case "SPC-MSG": // Spectate Message
                        SpectateMessage SPC = (SpectateMessage) ENC.getMsg();

                        if(games.get(SPC.getGameId()).isActive()) {
                            try {
                                TTT_Board board = (TTT_Board) games.get(SPC.getGameId()).getBoard();
                                int[][] arrayBoard = new int[3][3];
                                for(int i = 0; i < 9; i++)
                                    arrayBoard[i/3][i%3] = board.getPlayerAt(i/3, i%3);
                                SPC.setGameBoard(arrayBoard);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            ((Client)ENC.getidentifier()).sendPacket(new Packet("SPC-MSG", SPC));
                        }
                        break;
                }
            }
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public synchronized void terminateServer() {
        thread.interrupt();
        requests = null;
    }
}
