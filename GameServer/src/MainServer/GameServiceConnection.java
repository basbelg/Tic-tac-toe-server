package MainServer;

import DataClasses.TTT_GameData;
import DataClasses.TTT_ViewerData;
import Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

public class GameServiceConnection implements Runnable{
    private static GameServiceConnection instance = new GameServiceConnection(8001);

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Thread thread;
    private BlockingQueue<Packet> requests;

    private GameServiceConnection(int port) {
        try {
            System.out.println("Create GameServiceConnection");
            socket = new Socket("localhost", port);
            output = new ObjectOutputStream(socket.getOutputStream());
            input = new ObjectInputStream(socket.getInputStream());

            thread = new Thread(this);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static GameServiceConnection getInstance() {return instance;}

    public synchronized void sendPacket(Packet packet) {
        try {
            output.writeObject(packet);
            output.flush();
            output.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!thread.isInterrupted()) {
                Packet packet = (Packet) input.readObject();

                if (!packet.getType().equals("ENC-MSG"))
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                System.out.println("Received from Game Microservice: " + ENC.getType());
                switch (ENC.getType()) {
                    //--------------------------------------------------------------------------------------------------
                    //                                             Move
                    //--------------------------------------------------------------------------------------------------
                    case "MOV-MSG":
                        MoveMessage MOV = (MoveMessage) ENC.getMsg();
                        LegalMoveMessage LEM = (LegalMoveMessage) MessageFactory.getMessage("LEM-MSG");
                        LEM.setNextMove(MOV.getMoveInfo().getNextMove());

                        // Send move to players
                        TTT_GameData current_game = MainServer.getInstance().getGame_by_id().get(MOV.getGameId());
                        Client player1 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer1Id());

                        if(MainServer.getInstance().getGame_by_id().get(MOV.getGameId()).getPlayer2Id() != 1)
                        {
                            Client player2 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer2Id());
                            player2.sendPacket(new Packet("LEM-MSG", LEM));
                        }

                        player1.sendPacket(new Packet("LEM-MSG", LEM));

                        // Send move to viewers
                        synchronized (MainServer.getInstance().getActiveViewers().get(MOV.getGameId())) {
                            Iterator<TTT_ViewerData> i = MainServer.getInstance().getActiveViewers().
                                    get(MOV.getGameId()).iterator();
                            while (i.hasNext()) {
                                TTT_ViewerData viewer = i.next();
                                Client c = MainServer.getInstance().getClientIDMap().get(viewer.getViewer_id());
                                c.sendPacket(new Packet("LEM-MSG", LEM));
                            }
                        }
                        SQLServiceConnection.getInstance().sendPacket(new Packet("MOV-MSG", MOV));
                        MainServer.getInstance().notifyObservers(MOV, MOV.getGameId());
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                Illegal Move/Connect Failed
                    //--------------------------------------------------------------------------------------------------
                    case "ILM-MSG":
                    case "COF-MSG":
                        MainServer.getInstance().getClientIDMap().get(ENC.getidentifier()).sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                     Handle Low Priority Messages on Publisher Thread
                    //--------------------------------------------------------------------------------------------------
                    case "CNT-MSG":
                    case "CAI-MSG":
                    case "CLB-MSG":
                    case "GRE-MSG":
                    case "SPC-MSG":
                        MainServer.getInstance().getRequests().add(packet);

                        break;
                }
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void terminateConnection() {
        thread.interrupt();
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
