package MainServer;

import DataClasses.LobbyInfo;
import DataClasses.TTT_GameData;
import DataClasses.TTT_ViewerData;
import GameInterfaces.Move;
import Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
            socket = new Socket("localhost", port);
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());

            thread = new Thread(this);
            thread.run();
        } catch (IOException e) {
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

                if (packet.getType() != "ENC-MSG")
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                switch (ENC.getType()) {
                    //--------------------------------------------------------------------------------------------------
                    //                                             Move
                    //--------------------------------------------------------------------------------------------------
                    case "MOV-MSG":
                        MoveMessage MOV = (MoveMessage) ENC.getMsg();

                        // Send move to players
                        TTT_GameData current_game = MainServer.getInstance().getGame_by_id().get(MOV.getGameId());
                        Client player1 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer1Id());
                        Client player2 = MainServer.getInstance().getClientIDMap().get(current_game.getPlayer2Id());
                        player1.sendPacket(new Packet("MOV-MSG", MOV));
                        player2.sendPacket(new Packet("MOV-MSG", MOV));

                        // Send move to viewers
                        synchronized (MainServer.getInstance().getActiveViewers().get(MOV.getGameId())) {
                            Iterator<TTT_ViewerData> i = MainServer.getInstance().getActiveViewers().
                                    get(MOV.getGameId()).iterator();
                            while (i.hasNext()) {
                                TTT_ViewerData viewer = i.next();
                                Client c = MainServer.getInstance().getClientIDMap().get(viewer.getViewer_id());
                                c.sendPacket(new Packet("MOV-MSG", MOV));
                            }
                        }
                        SQLServiceConnection.getInstance().sendPacket(new Packet("MOV-MSG", MOV));
                        break;

                    //--------------------------------------------------------------------------------------------------
                    //                                       Illegal Move
                    //--------------------------------------------------------------------------------------------------
                    case "ILM-MSG":
                        IllegalMoveMessage ILM = (IllegalMoveMessage) ENC.getMsg();


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
