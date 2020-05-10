package MainServer;

import DataClasses.Spectator;
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
        System.out.println("Create Publisher");
        requests = MainServer.getInstance().getRequests();

        thread = new Thread(this);
        thread.start();
    }

    public static Publisher getInstance() {return instance;}

    @Override
    public void run() {
        try {
            while (!thread.isInterrupted()) {
                Packet packet = requests.take();

                if (!packet.getType().equals("ENC-MSG"))
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                System.out.println("Received by publisher: " + ENC.getType());
                switch (ENC.getType()) {
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
                        SAV.setInsert();

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
                                if(client.getUser() != null)
                                {
                                    if(client.getUser().getId() == SAV.getGame().getPlayer1Id() ||
                                            client.getUser().getId() == SAV.getGame().getPlayer2Id())
                                        client.sendPacket(new Packet("CNT-MSG", CNT));
                                    else
                                        client.sendPacket(new Packet("FUL-MSG", FUL));
                                }

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
                                if(client.getUser() != null)
                                {
                                    if(CAI.getPlayer1Id() == client.getUser().getId())
                                        client.sendPacket(new Packet("CAI-MSG", CAI));
                                    else
                                        client.sendPacket(new Packet("NAI-MSG", NAI));
                                }

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

                        // create new lobby message
                        NewLobbyMessage NLB = (NewLobbyMessage) MessageFactory.getMessage("NLB-MSG");
                        NLB.setCreatorUsername(MainServer.getInstance().getClientIDMap().get(CLB.getPlayer1Id()).
                                getUser().getUsername());
                        NLB.setGameLobbyId(current_game.getId());

                        // Send a create lobby message to the player and a new lobby message to all clients
                        synchronized (MainServer.getInstance().getClients()) {
                            Iterator<Client> i = MainServer.getInstance().getClients().iterator();
                            while (i != null && i.hasNext()) {
                                Client client = i.next();
                                if (client.getUser() != null)
                                {
                                    if(CLB.getPlayer1Id() == client.getUser().getId())
                                        client.sendPacket(new Packet("CLB-MSG", CLB));
                                    else
                                        client.sendPacket(new Packet("NLB-MSG", NLB));
                                }

                            }
                        }


                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                        Game Result
                    //--------------------------------------------------------------------------------------------------
                    case "GRE-MSG":
                        GameResultMessage GRE = (GameResultMessage) ENC.getMsg();
                        InactiveGameMessage IAG = (InactiveGameMessage) MessageFactory.getMessage("IAG-MSG");

                        // Create Save Game Message and update Game Result Message
                        current_game = MainServer.getInstance().getGame_by_id().get(ENC.getidentifier());
                        Client player1 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer1Id());
                        Client player2 = null;

                        if(current_game.getPlayer2Id() != 1)
                        {
                            player2 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer2Id());
                        }

                        if(GRE.getWinner().equals("0")) {
                            current_game.setWinningPlayerId(0);
                            GRE.setWinner(null);
                        }
                        else {
                            current_game.setWinningPlayerId((GRE.getWinner().equals("1"))? current_game.getPlayer1Id():
                                    current_game.getPlayer2Id());
                            if(current_game.getPlayer2Id() != 1) {
                                player2 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer2Id());
                                GRE.setWinner((GRE.getWinner().equals("1"))? player1.getUser().getUsername():
                                        player2.getUser().getUsername());
                            }
                            else
                                GRE.setWinner((GRE.getWinner().equals("1"))? player1.getUser().getUsername(): "AI Player");
                        }

                        current_game.setEndTime(LocalDateTime.now());
                        IAG.setFinishedGameId(current_game.getId());
                        SAV = new SaveGameMessage(current_game);
                        SAV.setUpdate();

                        // Send result message to players
                        player1.sendPacket(new Packet("GRE-MSG", GRE));
                        if(current_game.getPlayer2Id() != 1)
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

                        // Notify all clients that the game has ended
                        synchronized (MainServer.getInstance().getClients()) {
                            Iterator<Client> i = MainServer.getInstance().getClients().iterator();
                            while (i.hasNext()) {
                                Client client = i.next();
                                if(client.getUser() != null)
                                {
                                    client.sendPacket(new Packet("IAG-MSG", IAG));
                                }

                            }
                        }

                        // Update game in history
                        SQLServiceConnection.getInstance().sendPacket(new Packet("SAV-MSG", SAV));

                        // remove game from active games list
                        MainServer.getInstance().getActiveGames().remove(current_game); // remove is already synchronized
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                            Spectate
                    //--------------------------------------------------------------------------------------------------
                    case "SPC-MSG":
                        SpectateMessage SPC = (SpectateMessage) ENC.getMsg();

                        // Set player usernames
                        current_game = MainServer.getInstance().getGame_by_id().get(SPC.getGameId());
                        player1 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer1Id());
                        SPC.setPlayer1Username(player1.getUser().getUsername());

                        if(current_game.getPlayer2Id() != 1) {
                            player2 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer2Id());
                            SPC.setPlayer2Username(player2.getUser().getUsername());
                        }
                        else
                            SPC.setPlayer2Username("AI Player");

                        // Add viewer to list of viewers
                        TTT_ViewerData viewer = new TTT_ViewerData(SPC.getGameId(), SPC.getSpectatorId());
                        MainServer.getInstance().getActiveViewers().get(SPC.getGameId()).add(viewer);

                        // Send viewer information to the SQL microservice
                        SQLServiceConnection.getInstance().sendPacket(new Packet("ENC-MSG", ENC));

                        // Send to viewer
                        MainServer.getInstance().getClientIDMap().get(SPC.getSpectatorId()).sendPacket(
                                new Packet("SPC-MSG", SPC));
                        break;

                    case "GVW-MSG":
                        GameViewersMessage GVW = (GameViewersMessage) ENC.getMsg();
                        List<TTT_ViewerData> currentViewers = MainServer.getInstance().getActiveViewers().get(GVW.getGameId());
                        synchronized (currentViewers)
                        {
                            Iterator<TTT_ViewerData> iterator = currentViewers.iterator();
                            List<Spectator> spectators = new ArrayList<>();
                            while(iterator.hasNext())
                            {
                                TTT_ViewerData v = iterator.next();
                                spectators.add(new Spectator(MainServer.getInstance().getClientIDMap().get(v.getViewer_id()).getUser().getUsername()));
                            }
                            GVW.setSpectators(spectators);
                        }
                        MainServer.getInstance().getClientIDMap().get(ENC.getidentifier()).sendPacket(new Packet("GVW-MSG", GVW));
                        break;

                    case "SSP-MSG":
                        StopSpectatingMessage SSP = (StopSpectatingMessage) ENC.getMsg();
                        List<TTT_ViewerData> viewers = MainServer.getInstance().getActiveViewers().get(SSP.getGameId());
                        synchronized (viewers) {
                            Iterator<TTT_ViewerData> iterator = viewers.iterator();
                            while(iterator.hasNext()) {
                                TTT_ViewerData spectator = iterator.next();
                                if(((int)ENC.getidentifier()) == spectator.getViewer_id()) {
                                    viewers.remove(spectator);
                                    break;
                                }
                            }
                        }

                        MainServer.getInstance().getClientIDMap().get(ENC.getidentifier()).sendPacket(
                                new Packet("SSP-MSG", SSP));
                        break;

                    case "DIS-MSG":
                    case "IAG-MSG":
                        int user_id = (int) ENC.getidentifier();
                        TTT_GameData user_game = null;

                        // Locate client's game
                        if(ENC.getType().equals("DIS-MSG"))
                            synchronized (MainServer.getInstance().getActiveGames()) {
                                Iterator<TTT_GameData> iterator = MainServer.getInstance().getActiveGames().iterator();
                                while(iterator.hasNext()) {
                                    TTT_GameData game = iterator.next();
                                    if(user_id == game.getPlayer1Id() || user_id == game.getPlayer2Id()) {
                                        user_game = game;
                                        break;
                                    }
                                }
                            }
                        else if(ENC.getType().equals("IAG-MSG"))
                            user_game = MainServer.getInstance().getGame_by_id()
                                    .get(((InactiveGameMessage) ENC.getMsg()).getFinishedGameId());

                        // If the client was in a game
                        if(user_game != null) {
                            if (user_game.getPlayer2Id() == 0) { // if lobby has only one player
                                // notify all clients that lobby no longer exists
                                IAG = (InactiveGameMessage) MessageFactory.getMessage("IAG-MSG");
                                IAG.setFinishedGameId(user_game.getId());
                                synchronized (MainServer.getInstance().getClients()) {
                                    Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                                    while(iterator.hasNext())
                                        iterator.next().sendPacket(new Packet("IAG-MSG", IAG));
                                }

                                MainServer.getInstance().getActiveGames().remove(user_game);
                            } else {
                                // send a GRE message to notify the other player if applicable, and save the game to
                                // the db
                                GRE = (GameResultMessage) MessageFactory.getMessage("GRE-MSG");
                                GRE.setWinner(String.valueOf((user_id == user_game.getPlayer1Id()) ? 2 : 1));
                                EncapsulatedMessage ENC_GRE = new EncapsulatedMessage("GRE-MSG", user_game.getId(), GRE);
                                MainServer.getInstance().getRequests().add(new Packet("ENC-MSG", ENC_GRE));
                            }

                            // end the game on the GameServer to prevent new players from joining, etc.
                            ConcedeMessage CNC = (ConcedeMessage) MessageFactory.getMessage("CNC-MSG");
                            CNC.setGameId(user_game.getId());
                            EncapsulatedMessage ENC_CNC = new EncapsulatedMessage("CNC-MSG", user_game.getId(), CNC);
                            GameServiceConnection.getInstance().sendPacket(new Packet("ENC-MSG", ENC_CNC));
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
