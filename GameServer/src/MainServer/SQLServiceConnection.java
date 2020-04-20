package MainServer;

import DataClasses.GameInfo;
import DataClasses.TTT_GameData;
import DataClasses.User;
import Database.DBManager;
import Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SQLServiceConnection implements Runnable{
    private static SQLServiceConnection instance = new SQLServiceConnection(8002);

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Thread thread;
    private BlockingQueue<Packet> requests;

    private SQLServiceConnection(int port) {
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

    public static SQLServiceConnection getInstance() {return instance;}

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

                switch (packet.getType()) {
                    case "ENC-MSG": // encapsulated message
                        EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                        switch (ENC.getType()) {
                            case "STS-MSG": // Stats
                            case "GLG-MSG": // Game Log
                            case "DAC-MSG": // Deactivate Account
                            case "GVW-MSG": // Game Viewers
                            case "GMP-MSG": // Games played
                                // Return to MainServer
                                MainServer.getInstance().getClientIDMap().get(ENC.getidentifier()).
                                        sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                                break;
                            case "UPA-MSG": // Update Account Info
                                UpdateAccountInfoMessage UPA = (UpdateAccountInfoMessage) ENC.getMsg();

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
        } catch (ClosedByInterruptException e) {
            e.printStackTrace();
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
