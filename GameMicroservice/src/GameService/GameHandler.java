package GameService;

import MainServer.Client;
import Messages.*;
import TicTacToe.TTT_Board;
import TicTacToe.TTT_Game;

import java.util.HashMap;
import java.util.concurrent.BlockingQueue;

public class GameHandler implements Runnable{
    private static GameHandler instance = new GameHandler();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private HashMap<String, TTT_Game> games;

    private GameHandler() {
        requests = GameServer.getInstance().getRequests();

        games = new HashMap<>();

        thread = new Thread(this);
        thread.start();
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
                    //--------------------------------------------------------------------------------------------------
                    //                                      Connect to Lobby
                    //--------------------------------------------------------------------------------------------------
                    case "CNT-MSG":
                        ConnectToLobbyMessage CNT = (ConnectToLobbyMessage) ENC.getMsg();

                        try {
                            // attempt to start game
                            TTT_Game ttt_game = games.get(CNT.getLobbyGameId());
                            ttt_game.startGame();
                            CNT.setLobbyGameId(ttt_game.getGameId());

                            // return updated encapsulated connect-to-lobby message
                            GameServer.getInstance().sendPacket(packet);
                        } catch (Exception e) {e.printStackTrace();}
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                     Create AI Game Lobby
                    //--------------------------------------------------------------------------------------------------
                    case "CAI-MSG":
                        CreateAIGameMessage CAI = (CreateAIGameMessage) ENC.getMsg();

                        try {
                            // start game and store the generated id
                            TTT_Game ttt_game = new TTT_Game();
                            ttt_game.startGame();
                            games.putIfAbsent(ttt_game.getGameId(), ttt_game);
                            CAI.setGameLobbyId(ttt_game.getGameId());

                            // return updated encapsulated create-ai-game message
                            GameServer.getInstance().sendPacket(packet);
                        } catch (Exception e) {e.printStackTrace();}
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                      Create Game Lobby
                    //--------------------------------------------------------------------------------------------------
                    case "CLB-MSG":
                        CreateLobbyMessage CLB = (CreateLobbyMessage) ENC.getMsg();

                        // Create new lobby and send packet back with id
                        TTT_Game ttt_game = new TTT_Game();
                        games.putIfAbsent(ttt_game.getGameId(), ttt_game);
                        CLB.setGameLobbyId(ttt_game.getGameId());

                        // return updated encapsulated create-game-lobby message
                        GameServer.getInstance().sendPacket(packet);
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                             Move
                    //--------------------------------------------------------------------------------------------------
                    case "MOV-MSG":
                        MoveMessage MOV = (MoveMessage) ENC.getMsg();
                        TTT_Game current_game = games.get(MOV.getGameId());

                        try {
                            // attempt move
                            current_game.performMove(MOV.getMoveInfo().getNextMove());

                            // If the game is over: send game result and end the game
                            if(current_game.isFinished()) {
                                current_game.endGame();
                                GameResultMessage GRE = (GameResultMessage) MessageFactory.getMessage("GRE-MSG");
                                GRE.setWinner(String.valueOf(current_game.getWinner()));
                                EncapsulatedMessage ENC_GRE = new EncapsulatedMessage("GRE-MSG",
                                        current_game.getGameId(), GRE);
                                GameServer.getInstance().sendPacket(new Packet("ENC-MSG", ENC_GRE));
                            }

                            // send move message
                            GameServer.getInstance().sendPacket(packet);
                        } catch (Exception e) {
                            // if the move is invalid: send an illegal move message
                            e.printStackTrace();
                            IllegalMoveMessage ILM = (IllegalMoveMessage) MessageFactory.getMessage("ILM-MSG");
                            EncapsulatedMessage ENC_ILM = new EncapsulatedMessage("ILM-MSG", ENC.getidentifier(), ILM);
                            GameServer.getInstance().sendPacket(new Packet("ENC-MSG", ENC_ILM));
                        }
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                            Spectate
                    //--------------------------------------------------------------------------------------------------
                    case "SPC-MSG":
                        SpectateMessage SPC = (SpectateMessage) ENC.getMsg();

                        // If the game is not over: send spectate message back with the current board
                        if(games.get(SPC.getGameId()).isActive()) {
                            try {
                                TTT_Board board = (TTT_Board) games.get(SPC.getGameId()).getBoard();
                                int[][] arrayBoard = new int[3][3];
                                for(int i = 0; i < 9; i++)
                                        arrayBoard[i/3][i%3] = (board.getPlayerAt(i/3, i%3) == 2)?
                                                -1: board.getPlayerAt(i/3, i%3);
                                SPC.setGameBoard(arrayBoard);
                            } catch (Exception e) {e.printStackTrace();}

                            // Send encapsulated spectate message
                            GameServer.getInstance().sendPacket(packet);
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
