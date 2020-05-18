package GameService;

import DataClasses.TTT_GameData;
import MainServer.MainServer;
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

                if(!packet.getType().equals("ENC-MSG"))
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                System.out.println("Received from Client: " + ENC.getType());
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
                        }
                        catch (NullPointerException e) {
                            // game no longer exists
                            ConnectFailedMessage COF = (ConnectFailedMessage) MessageFactory.getMessage("COF-MSG");
                            COF.setGameActive(false);
                            EncapsulatedMessage ENC_COF = new EncapsulatedMessage("COF-MSG",
                                    ENC.getidentifier(), COF);
                            GameServer.getInstance().sendPacket(new Packet("ENC-MSG", ENC_COF));
                        } catch (Exception e) {
                            // game is full
                            ConnectFailedMessage COF = (ConnectFailedMessage) MessageFactory.getMessage("COF-MSG");
                            COF.setGameActive(true);
                            EncapsulatedMessage ENC_COF = new EncapsulatedMessage("COF-MSG",
                                    ENC.getidentifier(), COF);
                            GameServer.getInstance().sendPacket(new Packet("ENC-MSG", ENC_COF));
                        }
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
                            System.out.println("New ai game: " + ttt_game.getGameId());

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
                        System.out.println("New pvp game: " + ttt_game.getGameId());

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
                            MOV.setTurn(current_game.getTurn());

                            // If the game is over: send game result and end the game
                            if (current_game.isFinished()) {
                                games.remove(current_game.getGameId());
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
                            System.out.println("Illegal move received from client");
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
                        try {
                            if(games.get(SPC.getGameId()).isActive()) {
                                TTT_Board board = (TTT_Board) games.get(SPC.getGameId()).getBoard();

                                SPC.setGameBoard(board.getCurrentBoard());

                                // Send encapsulated spectate message
                                GameServer.getInstance().sendPacket(packet);
                            }
                        }
                        catch (NullPointerException e) {System.out.println("Invalid game id: " + SPC.getGameId());}
                        catch (Exception e) {e.printStackTrace();}
                        break;

                    case "CNC-MSG": // Concede
                        ConcedeMessage CNC = (ConcedeMessage) ENC.getMsg();
                        games.remove(CNC.getGameId());
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
