package SQLService;

import DataClasses.GameInfo;
import DataClasses.TTT_GameData;
import DataClasses.User;
import Database.DBManager;
import Messages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SQLHandler implements Runnable{
    private static SQLHandler instance = new SQLHandler();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private SQLHandler() {
        requests = SQLServer.getInstance().getRequests();

        thread = new Thread(this);
        thread.run();
    }

    public static SQLHandler getInstance() {return instance;}

    @Override
    public void run() {
        try {
            while(!thread.isInterrupted()) {
                Packet packet = requests.take();

                switch(packet.getType()) {
                    case "ENC-MSG": // encapsulated message
                        EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                        switch (ENC.getType()) {
                            case "GMP-MSG": // Games played
                                GamesPlayedMessage GMP = (GamesPlayedMessage) ENC.getMsg();

                                // Update Message with list of gameinfo
                                List<Object> games = DBManager.getInstance().list(TTT_GameData.class);
                                List<GameInfo> infoList = new ArrayList<>();
                                for(Object obj: games) {
                                    TTT_GameData game = (TTT_GameData) obj;
                                    User second_player = null;
                                    if(game.getPlayer1Id() == GMP.getPlayerId())
                                        second_player = (User) DBManager.getInstance().get(User.class, String.valueOf(game.getPlayer1Id()));
                                    else if(game.getPlayer2Id() == GMP.getPlayerId())
                                        second_player = (User) DBManager.getInstance().get(User.class, String.valueOf(game.getPlayer1Id()));
                                    infoList.add(new GameInfo(second_player.getUsername(), game.getStartingTime(), game.getId()));
                                }
                                GMP.setGameInfoList(infoList);

                                // Return to MainServer
                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "GVW-MSG": // Game Viewers
                                GameViewersMessage GVW = (GameViewersMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "DAC-MSG": // Deactivate Account
                                DeactivateAccountMessage DAC = (DeactivateAccountMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "UPA-MSG": // Update Account Info
                                UpdateAccountInfoMessage UPA = (UpdateAccountInfoMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "GLG-MSG": // Game Log
                                GameLogMessage GLG = (GameLogMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "STS-MSG": // Stats
                                StatsMessage STS = (StatsMessage) ENC.getMsg();



                                SQLServer.getInstance().sendPacket(packet);
                                break;
                        }
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                             Login
                    //--------------------------------------------------------------------------------------------------
                    case "LOG-MSG": // login

                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                        Create Account
                    //--------------------------------------------------------------------------------------------------
                    case "CAC-MSG": // create account
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Move
                    //--------------------------------------------------------------------------------------------------
                    case "MOV-MSG": // move
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Game
                    //--------------------------------------------------------------------------------------------------
                    case "SAV-MSG": // save
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
