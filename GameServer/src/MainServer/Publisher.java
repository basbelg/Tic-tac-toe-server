package MainServer;

import DataClasses.LobbyInfo;
import DataClasses.TTT_GameData;
import DataClasses.TTT_ViewerData;
import Messages.*;
import UI.Main;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Publisher implements Runnable{
    private static Publisher instance = new Publisher();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private Publisher() {
        requests = MainServer.getInstance().getRequests();

        thread = new Thread(this);
        thread.run();
    }

    public static Publisher getInstance() {return instance;}

    @Override
    public void run() {
        try {
            while (!thread.isInterrupted()) {
                Packet packet = requests.take();

                if (packet.getType() != "ENC-MSG")
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                switch (ENC.getType()) {
                    //--------------------------------------------------------------------------------------------------
                    //                                 Login Message (from client)
                    //--------------------------------------------------------------------------------------------------
                    case "LOG-MSG": // Login
                        LoginMessage LOG = (LoginMessage) ENC.getMsg();
                        boolean LOF = false;

                        synchronized (MainServer.getInstance().getActiveGames()) {
                            Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                            while (iterator.hasNext()) {
                                Client client = iterator.next();
                                if (client.getUser() != null && client.getUser().getUsername()==LOG.getUsername()) {
                                    LOF = true;
                                    break;
                                }
                            }
                        }

                        if (LOF) {
                            int id = (Integer) ENC.getidentifier();
                            MainServer.getInstance().getClientIDMap().get(id).sendPacket(new Packet("LOF-MSG",
                                    (AccountFailedMessage) MessageFactory.getMessage("LOF-MSG")));
                        }
                        else
                            SQLServiceConnection.getInstance().sendPacket(packet);
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                    All Active Games
                    //--------------------------------------------------------------------------------------------------
                    case "AAG-MSG":
                        AllActiveGamesMessage AAG = (AllActiveGamesMessage) ENC.getMsg();
                        List<LobbyInfo> allGames = new ArrayList<>();

                        synchronized (MainServer.getInstance().getActiveGames()) {
                            Iterator<TTT_GameData> iterator = MainServer.getInstance().getActiveGames().iterator();
                            while(iterator.hasNext()) {
                                TTT_GameData current_game = iterator.next();
                                    String creator = MainServer.getInstance().getClientIDMap().get(current_game
                                            .getPlayer1Id()).getUser().getUsername();
                                    allGames.add(new LobbyInfo(creator, current_game.getId(),
                                            (current_game.getPlayer2Id() == 0)? 1 : 2));
                            }
                        }

                        AAG.setAllActiveGames(allGames);
                        int id = (Integer) ENC.getidentifier();
                        MainServer.getInstance().getClientIDMap().get(id).sendPacket(new Packet("AAG-MSG", AAG));
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                      Connect to Lobby
                    //--------------------------------------------------------------------------------------------------
                    case "CNT-MSG":
                        ConnectToLobbyMessage CNT = (ConnectToLobbyMessage) ENC.getMsg();

                        // Create Save Game Message
                        TTT_GameData current_game = MainServer.getInstance().getGame_by_id().get(CNT.getLobbyGameId());
                        current_game.setPlayer2Id((int) ENC.getidentifier());
                        current_game.setStartingTime(CNT.getStartTime());
                        SaveGameMessage SAV = new SaveGameMessage(current_game);
                        SAV.setUpdate();

                        // Create Full Lobby Message
                        FullLobbyMessage FUL = (FullLobbyMessage) MessageFactory.getMessage("FUL-MSG");
                        FUL.setLobbyGameId(CNT.getLobbyGameId());

                        // Update Connect to Lobby
                        CNT.setPlayer1(MainServer.getInstance().getClientIDMap().get(SAV.getGame().getPlayer1Id()).
                                getUser().getUsername());

                        // Send a connect to lobby message to the players, a full lobby message to all clients, and a
                        // save game message to the sql microservice
                        synchronized (MainServer.getInstance().getClients()) {
                            Iterator<Client> i = MainServer.getInstance().getClients().iterator();
                            while(i.hasNext()) {
                                Client client = i.next();
                                if(client.getUser().getId() == SAV.getGame().getPlayer1Id() ||
                                        client.getUser().getId() == SAV.getGame().getPlayer2Id())
                                    client.sendPacket(new Packet("CNT-MSG", CNT));
                                else
                                    client.sendPacket(new Packet("FUL-MSG", FUL));
                            }
                        }
                        SQLServiceConnection.getInstance().sendPacket(new Packet("SAV-MSG", SAV));
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                     Create AI Game Lobby
                    //--------------------------------------------------------------------------------------------------
                    case "CAI-MSG":
                        CreateAIGameMessage CAI = (CreateAIGameMessage) ENC.getMsg();

                        // create new game object
                        current_game = new TTT_GameData(CAI.getGameLobbyId(), CAI.getStartTime(),
                                CAI.getPlayer1Id(), 1, CAI.getPlayer1Id());

                        // update server
                        MainServer.getInstance().getActiveGames().add(current_game);
                        MainServer.getInstance().getGame_by_id().put(current_game.getId(), current_game);
                        MainServer.getInstance().getActiveViewers().put(current_game.getId(),
                                Collections.synchronizedList(new ArrayList<>()));

                        // create save message to save to database
                        SAV = new SaveGameMessage(current_game);
                        SAV.setInsert();

                        // create new ai lobby message
                        NewAILobbyMessage NAI = (NewAILobbyMessage) MessageFactory.getMessage("NAI-MSG");
                        NAI.setCreatorUsername(MainServer.getInstance().getClientIDMap().get(CAI.getPlayer1Id()).
                                getUser().getUsername());
                        NAI.setGameLobbyId(current_game.getId());

                        // Send a create ai lobby message to the player, a new ai lobby message to all clients, and a
                        // save game message to the sql microservice
                        synchronized (MainServer.getInstance().getClients()) {
                            Iterator<Client> i = MainServer.getInstance().getClients().iterator();
                            while (i.hasNext()) {
                                Client client = i.next();
                                if(CAI.getPlayer1Id() == client.getUser().getId())
                                    client.sendPacket(new Packet("CAI-MSG", CAI));
                                else
                                    client.sendPacket(new Packet("NAI-MSG", NAI));
                            }
                        }
                        SQLServiceConnection.getInstance().sendPacket(new Packet("SAV-MSG", SAV));
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                      Create Game Lobby
                    //--------------------------------------------------------------------------------------------------
                    case "CLB-MSG":
                        CreateLobbyMessage CLB = (CreateLobbyMessage) ENC.getMsg();

                        // create new game object
                        current_game = new TTT_GameData(CLB.getGameLobbyId(), null,
                                CLB.getPlayer1Id(), 0, CLB.getPlayer1Id());

                        // update server
                        MainServer.getInstance().getActiveGames().add(current_game);
                        MainServer.getInstance().getGame_by_id().put(current_game.getId(), current_game);
                        MainServer.getInstance().getActiveViewers().put(current_game.getId(),
                                Collections.synchronizedList(new ArrayList<>()));

                        // create new ai lobby message
                        NewLobbyMessage NLB = (NewLobbyMessage) MessageFactory.getMessage("NLB-MSG");
                        NLB.setCreatorUsername(MainServer.getInstance().getClientIDMap().get(CLB.getPlayer1Id()).
                                getUser().getUsername());
                        NLB.setGameLobbyId(current_game.getId());

                        // Send a create ai lobby message to the player, a new ai lobby message to all clients, and a
                        // save game message to the sql microservice
                        synchronized (MainServer.getInstance().getClients()) {
                            Iterator<Client> i = MainServer.getInstance().getClients().iterator();
                            while (i.hasNext()) {
                                Client client = i.next();
                                if(CLB.getPlayer1Id() == client.getUser().getId())
                                    client.sendPacket(new Packet("CLB-MSG", CLB));
                                else
                                    client.sendPacket(new Packet("NLB-MSG", NLB));
                            }
                        }
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                        Game Result
                    //--------------------------------------------------------------------------------------------------
                    case "GRE-MSG":
                        GameResultMessage GRE = (GameResultMessage) ENC.getMsg();

                        // Create Save Game Message and update Game Result Message
                        current_game = MainServer.getInstance().getGame_by_id().get(ENC.getidentifier());
                        Client player1 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer1Id());
                        Client player2 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer2Id());

                        if(GRE.getWinner() == "0") {
                            current_game.setWinningPlayerId(0);
                            GRE.setWinner(null);
                        }
                        else {
                            current_game.setWinningPlayerId((GRE.getWinner() == "1")? current_game.getPlayer1Id():
                                    current_game.getPlayer2Id());
                            GRE.setWinner((GRE.getWinner() == "1")? player1.getUser().getUsername():
                                    player2.getUser().getUsername());
                        }

                        current_game.setEndTime(LocalDateTime.now());
                        SAV = new SaveGameMessage(current_game);
                        SAV.setUpdate();

                        // Send result message to players
                        player1.sendPacket(new Packet("GRE-MSG", GRE));
                        player2.sendPacket(new Packet("GRE-MSG", GRE));

                        // Send result to viewers
                        synchronized (MainServer.getInstance().getActiveViewers().get(current_game.getId())) {
                            Iterator<TTT_ViewerData> i = MainServer.getInstance().getActiveViewers().
                                    get(current_game.getId()).iterator();
                            while (i.hasNext()) {
                                TTT_ViewerData viewer = i.next();
                                Client c = MainServer.getInstance().getClientIDMap().get(viewer.getViewer_id());
                                c.sendPacket(new Packet("GRE-MSG", GRE));
                            }
                        }

                        // Update game in history
                        SQLServiceConnection.getInstance().sendPacket(new Packet("SAV-MSG", SAV));
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                            Spectate
                    //--------------------------------------------------------------------------------------------------
                    case "SPC-MSG":
                        SpectateMessage SPC = (SpectateMessage) ENC.getMsg();

                        // Set player usernames
                        current_game = MainServer.getInstance().getGame_by_id().get(SPC.getGameId());
                        player1 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer1Id());
                        player2 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer1Id());
                        SPC.setPlayer1Username(player1.getUser().getUsername());
                        SPC.setPlayer1Username(player2.getUser().getUsername());

                        // Add viewer to list of viewers
                        TTT_ViewerData viewer = new TTT_ViewerData(SPC.getGameId(), SPC.getSpectatorId());
                        MainServer.getInstance().getActiveViewers().get(SPC.getGameId()).add(viewer);

                        // Send to viewer
                        MainServer.getInstance().getClientIDMap().get(SPC.getSpectatorId()).sendPacket(
                                new Packet("SPC-MSG", SPC));
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
