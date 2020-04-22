package MainServer;

import Messages.EncapsulatedMessage;
import Messages.LoginSuccessfulMessage;
import Messages.Packet;
import UI.ServerController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

public class SQLServiceConnection implements Runnable{
    private static SQLServiceConnection instance = new SQLServiceConnection(8002);

    private Socket socket;
    private ServerController listener;
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

    public void setListener(ServerController listener)
    {
        this.listener = listener;
    }

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

                if(packet.getType() != "ENC-MSG")
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                switch (ENC.getType()) {
                    case "UPA-MSG": // Update Account Info
                    case "STS-MSG": // Stats
                    case "GLG-MSG": // Game Log
                    case "DAC-MSG": // Deactivate Account
                    case "GVW-MSG": // Game Viewers
                    case "GMP-MSG": // Games played
                        // Return to MainServer
                        MainServer.getInstance().getClientIDMap().get(ENC.getidentifier()).
                                sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                        break;

                    //------------------------------------------------------------------------------------------
                    //                                             Login
                    //------------------------------------------------------------------------------------------
                    case "LOS-MSG": // login succeeded
                        LoginSuccessfulMessage LOS = (LoginSuccessfulMessage) ENC.getMsg();
                        Client client = null;
                        Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                        while(iterator.hasNext()) {
                            client = iterator.next();
                            if(client.getUser().getUsername() == ENC.getidentifier() && client.getUser().
                                    getId() == 0) {
                                client.sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                                break;
                            }
                        }
                        client.setUser(LOS.getUser());
                        MainServer.getInstance().getClientIDMap().put(client.getUser().getId(), client);
                        break;

                    case "LOF-MSG": // login failed


                    // -----------------------------------------------------------------------------------------
                    //                                        Create Account
                    //------------------------------------------------------------------------------------------
                    case "ACS-MSG": // create account

                    case "ACF-MSG": // create account
                        synchronized (MainServer.getInstance().getClients()) {
                            iterator = MainServer.getInstance().getClients().iterator();
                            while(iterator.hasNext()) {
                                client = iterator.next();
                                if(client.getUser().getUsername() == ENC.getidentifier() && client.getUser().
                                        getId() == 0) {
                                    client.sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                                    break;
                                }
                            }
                        }
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
