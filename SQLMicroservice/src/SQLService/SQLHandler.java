package SQLService;

import DataClasses.*;
import Database.DBManager;
import Messages.*;
import TicTacToe.TTT_Move;

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
        thread.start();
    }

    public static SQLHandler getInstance() {return instance;}

    @Override
    public void run() {
        System.out.println("Create SQLHandler");
        try {
            while(!thread.isInterrupted()) {
                Packet packet = requests.take();

                System.out.println("SQLHandler: " + packet.getType());

                switch(packet.getType()) {
                    case "ENC-MSG": // encapsulated message
                        EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                        System.out.println("SQLHandler (ENC): " + ENC.getType());
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
                                        second_player = (User) DBManager.getInstance().get(User.class, String.valueOf(game.getPlayer2Id()));
                                    else if(game.getPlayer2Id() == GMP.getPlayerId())
                                        second_player = (User) DBManager.getInstance().get(User.class, String.valueOf(game.getPlayer1Id()));

                                    if(second_player != null)
                                        infoList.add(new GameInfo(second_player.getUsername(), game.getStartingTime(), game.getId()));
                                }
                                GMP.setGameInfoList(infoList);

                                // Return to MainServer
                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "GVW-MSG": // Game Viewers
                                GameViewersMessage GVW = (GameViewersMessage) ENC.getMsg();

                                List<Object> viewers = DBManager.getInstance().query(TTT_ViewerData.class, GVW.getGameId());
                                List<Spectator> spectators = new ArrayList<>();

                                for(Object obj : viewers) {
                                    User viewer = (User) DBManager.getInstance().get(User.class, String.valueOf(((TTT_ViewerData) obj).getViewer_id()));
                                    spectators.add(new Spectator(viewer.getUsername()));
                                }

                                GVW.setSpectators(spectators);
                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "DAC-MSG": // Deactivate Account
                                DeactivateAccountMessage DAC = (DeactivateAccountMessage) ENC.getMsg();

                                User deactivatedAccount = (User) DBManager.getInstance().get(User.class, String.valueOf(DAC.getUserId()));

                                deactivatedAccount.setIsActive(false);

                                DBManager.getInstance().update(deactivatedAccount);
                                break;
                            case "UPA-MSG": // Update Account Info
                                UpdateAccountInfoMessage UPA = (UpdateAccountInfoMessage) ENC.getMsg();

                                List<Object> users = DBManager.getInstance().query(User.class, "active");
                                boolean UAC_Failed = false;

                                for(Object obj: users) {
                                    User user = (User) obj;
                                    if(user.getUsername().equals(UPA.getUpdatedUser().getUsername()) && user.getId() != UPA.getUpdatedUser().getId()) {
                                        AccountFailedMessage ACF = (AccountFailedMessage) MessageFactory.getMessage("ACF-MSG");
                                        SQLServer.getInstance().sendPacket(new Packet("ENC-MSG", new EncapsulatedMessage("ACF-MSG", ENC.getidentifier(), ACF)));
                                        UAC_Failed = true;
                                        break;
                                    }
                                }

                                if(!UAC_Failed) {
                                    SQLServer.getInstance().sendPacket(new Packet("ENC-MSG", new EncapsulatedMessage("UPA-MSG", ENC.getidentifier(), UPA)));
                                    DBManager.getInstance().update(UPA.getUpdatedUser());
                                }

                                break;
                            case "GLG-MSG": // Game Log
                                GameLogMessage GLG = (GameLogMessage) ENC.getMsg();

                                TTT_GameData gameData = (TTT_GameData) DBManager.getInstance().get(TTT_GameData.class, GLG.getGameId());

                                GLG.setGameStarted(gameData.getStartingTime());
                                GLG.setGameEnded(gameData.getEndTime());

                                User player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getPlayer1Id()));
                                GLG.setPlayer1Username(player.getUsername());
                                player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getPlayer2Id()));
                                GLG.setPlayer2Username(player.getUsername());
                                if(gameData.getWinningPlayerId() != 0) {
                                    player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getWinningPlayerId()));
                                    GLG.setWinner(player.getUsername() + " has won the game");
                                }
                                else
                                    GLG.setWinner("It's a tie!");


                                List<Object> moveData = DBManager.getInstance().query(TTT_MoveData.class, GLG.getGameId());
                                List<MoveInfo> moves = new ArrayList<>();

                                int playerNum = 1;

                                for(Object obj : moveData) {
                                    TTT_MoveData move = (TTT_MoveData) obj;
                                    moves.add(new MoveInfo(new TTT_Move((playerNum == 1 ? playerNum++ : playerNum--), move.getRow(), move.getColumn()), move.getTime()));
                                }

                                GLG.setMoveHistory(moves);
                                SQLServer.getInstance().sendPacket(packet);
                                break;
                            case "STS-MSG": // Stats
                                StatsMessage STS = (StatsMessage) ENC.getMsg();

                                int id = (int) ENC.getidentifier();
                                int[] stats = new int[3];

                                // Update Message with list of gameinfo
                                List<Object> all_games = DBManager.getInstance().list(TTT_GameData.class);
                                for(Object obj: all_games) {
                                    TTT_GameData game = (TTT_GameData) obj;
                                    if(game.getWinningPlayerId() != -1 && (game.getPlayer1Id() == id || game.getPlayer2Id() == id)) {
                                        if(game.getWinningPlayerId() == id)
                                            ++stats[0];
                                        else if(game.getWinningPlayerId() == 0)
                                            ++stats[1];
                                        else
                                            ++stats[2];
                                    }
                                }

                                // update sts message
                                STS.setWins(stats[0]);
                                STS.setTies(stats[1]);
                                STS.setLosses(stats[2]);

                                SQLServer.getInstance().sendPacket(packet);
                                break;

                            case "SPC-MSG": //Spectator
                                SpectateMessage SPC = (SpectateMessage) ENC.getMsg();
                                boolean isDuplicate = false;

                                List<Object> current_spectators = DBManager.getInstance().list(TTT_ViewerData.class);
                                for(Object obj: current_spectators) {
                                    TTT_ViewerData cv = (TTT_ViewerData) obj;
                                    if(cv.getViewer_id() == SPC.getSpectatorId() && cv.getGame_id().equals(SPC.getGameId())) {
                                        isDuplicate = true;
                                        break;
                                    }
                                }

                                if(!isDuplicate) {
                                    TTT_ViewerData viewer = new TTT_ViewerData(SPC.getGameId(), SPC.getSpectatorId());
                                    DBManager.getInstance().insert(viewer);
                                }
                                break;

                            //------------------------------------------------------------------------------------------
                            //                                        Create Account
                            //------------------------------------------------------------------------------------------
                            case "CAC-MSG": // create account
                                CreateAccountMessage CAC = (CreateAccountMessage) ENC.getMsg();

                                users = DBManager.getInstance().query(User.class, "active");
                                boolean CAC_Failed = false;
                                for(Object obj: users) {
                                    User user = (User) obj;
                                    if(user.getUsername().equals(CAC.getNewUser().getUsername())) {
                                        AccountFailedMessage ACF = (AccountFailedMessage) MessageFactory.
                                            getMessage("ACF-MSG");
                                        SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                                new EncapsulatedMessage("ACF-MSG", ENC.getidentifier(), ACF)));
                                        CAC_Failed = true;
                                        break;
                                    }
                                }

                                if(!CAC_Failed) {
                                    AccountSuccessfulMessage ACS = (AccountSuccessfulMessage) MessageFactory.
                                            getMessage("ACS-MSG");
                                    SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                            new EncapsulatedMessage("ACS-MSG", ENC.getidentifier(), ACS)));
                                    DBManager.getInstance().insert(CAC.getNewUser());
                                }
                                break;

                            //------------------------------------------------------------------------------------------
                            //                                             Login
                            //------------------------------------------------------------------------------------------
                            case "LOG-MSG": // login
                                LoginMessage LOG = (LoginMessage) ENC.getMsg();

                                users = DBManager.getInstance().query(User.class, "active");
                                boolean LOG_Failed = true;
                                for(Object obj: users) {
                                    User user = (User) obj;
                                    System.out.println(user.getUsername() + " " + user.getPassword());
                                    System.out.println(LOG.getUsername() + " " + LOG.getPassword());
                                    if(user.getUsername().equals(LOG.getUsername()) && user.getPassword().equals(LOG.getPassword()) &&
                                            user.getId() != 1) {
                                        // Send Login Success
                                        LoginSuccessfulMessage LOS = (LoginSuccessfulMessage) MessageFactory.getMessage("LOS-MSG");
                                        LOS.setUser(user);
                                        SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                                new EncapsulatedMessage("LOS-MSG", ENC.getidentifier(), LOS)));
                                        LOG_Failed = false;
                                        break;
                                    }
                                }

                                if(LOG_Failed) {
                                    // Send Login Failed
                                    LoginFailedMessage LOF = (LoginFailedMessage) MessageFactory.getMessage("LOF-MSG");
                                    SQLServer.getInstance().sendPacket(new Packet("ENC-MSG",
                                            new EncapsulatedMessage("LOF-MSG", ENC.getidentifier(), LOF)));
                                }
                                break;
                        }
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Move
                    //--------------------------------------------------------------------------------------------------
                    case "MOV-MSG": // move
                        MoveMessage MOV = (MoveMessage) packet.getData();
                        TTT_MoveData move = new TTT_MoveData(MOV.getGameId(), MOV.getMovingPlayerId(),
                                MOV.getMoveInfo().getTimeMade(), MOV.getMoveInfo().getNextMove().getRow(),
                                MOV.getMoveInfo().getNextMove().getColumn(), MOV.getTurn());
                        DBManager.getInstance().insert(move);
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                          Save Game
                    //--------------------------------------------------------------------------------------------------
                    case "SAV-MSG": // save
                        SaveGameMessage SAV = (SaveGameMessage) packet.getData();
                        if(SAV.isInsert()) {
                            System.out.println("is insert");
                            DBManager.getInstance().insert(SAV.getGame());
                        }
                        else if(SAV.isUpdate()) {
                            System.out.println("is update");
                            DBManager.getInstance().update(SAV.getGame());
                        }
                        break;

                    case "AGS-MSG":
                        AllGamesMessage AGS = (AllGamesMessage) packet.getData();
                        AGS.setGames(DBManager.getInstance().list(TTT_GameData.class));
                        SQLServer.getInstance().sendPacket(packet);
                        break;

                    case "RUS-MSG":
                        RegisteredUsersMessage RUS = (RegisteredUsersMessage) packet.getData();
                        RUS.setUsers(DBManager.getInstance().query(User.class, "active"));
                        SQLServer.getInstance().sendPacket(packet);
                        break;

                    case "AAU-MSG":
                        AdminAccountUpdateMessage AAU = (AdminAccountUpdateMessage) packet.getData();
                        List<Object> users = DBManager.getInstance().query(User.class, "active");
                        boolean UAC_Failed = false;

                        for(Object obj: users) {
                            User user = (User) obj;
                            if(user.getUsername().equals(AAU.getUPA().getUpdatedUser().getUsername()) && user.getId() != AAU.getId()) {
                                UAC_Failed = true;
                                SQLServer.getInstance().sendPacket(new Packet("AAF-MSG", new AdminAccountFailedMessage()));
                                break;
                            }
                        }

                        if(!UAC_Failed) {
                            SQLServer.getInstance().sendPacket(new Packet("AAS-MSG", new AdminAccountSuccessfulMessage()));
                            SQLServer.getInstance().sendPacket(new Packet("ENC-MSG", new EncapsulatedMessage("UPA-MSG", AAU.getId(), AAU.getUPA())));
                            DBManager.getInstance().update(AAU.getUPA().getUpdatedUser());
                        }
                        break;

                    case "AGI-MSG":
                        AllGameInfoMessage AGI = (AllGameInfoMessage) packet.getData();

                        TTT_GameData gameData = (TTT_GameData) DBManager.getInstance().get(TTT_GameData.class, AGI.getId());
                        AGI.getGameLog().setGameStarted(gameData.getStartingTime());
                        AGI.getGameLog().setGameEnded(gameData.getEndTime());
                        User player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getPlayer1Id()));
                        AGI.getGameLog().setPlayer1Username(player.getUsername());
                        player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getPlayer2Id()));
                        AGI.getGameLog().setPlayer2Username(player.getUsername());
                        if(gameData.getWinningPlayerId() != 0 && gameData.getWinningPlayerId() != -1) {
                            player = (User) DBManager.getInstance().get(User.class, String.valueOf(gameData.getWinningPlayerId()));
                            AGI.getGameLog().setWinner(player.getUsername() + " has won the game");
                        }
                        else if(gameData.getWinningPlayerId() != -1)
                            AGI.getGameLog().setWinner("It's a tie!");

                        List<Object> list = DBManager.getInstance().query(TTT_MoveData.class, AGI.getId());
                        List<MoveInfo> moves = new ArrayList<>();
                        int playerNum = 1;
                        for(Object obj : list) {
                            move = (TTT_MoveData) obj;
                            moves.add(new MoveInfo(new TTT_Move((playerNum == 1 ? playerNum++ : playerNum--), move.getRow(), move.getColumn()), move.getTime()));
                        }
                        AGI.getGameLog().setMoveHistory(moves);

                        list = DBManager.getInstance().query(TTT_ViewerData.class, AGI.getId());
                        List<Spectator> spectators = new ArrayList<>();
                        for(Object obj : list) {
                            User viewer = (User) DBManager.getInstance().get(User.class, String.valueOf(((TTT_ViewerData) obj).getViewer_id()));
                            spectators.add(new Spectator(viewer.getUsername()));
                        }
                        AGI.getGameViewers().setSpectators(spectators);

                        SQLServer.getInstance().sendPacket(packet);
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
