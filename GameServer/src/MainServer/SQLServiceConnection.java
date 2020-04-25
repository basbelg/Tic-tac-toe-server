package MainServer;

import Messages.DeactivateAccountMessage;
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

public class SQLServiceConnection implements Runnable{
    private static SQLServiceConnection instance = new SQLServiceConnection(8002);

    private Socket socket;
    private ServerController listener;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Thread thread;

    private SQLServiceConnection(int port) {
        try {
            socket = new Socket("localhost", port);
            output = new ObjectOutputStream(socket.getOutputStream()); // OUTPUT HAS TO COME FIRST
            input = new ObjectInputStream(socket.getInputStream());

            thread = new Thread(this);
            thread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static SQLServiceConnection getInstance() {return instance;}

    public void setListener(ServerController listener)
    {
        this.listener = listener;
    }

    public void updateUI() { listener.update(new DeactivateAccountMessage()); }

    public void sendPacket(Packet packet) {
        try {
            output.writeObject(packet);
            output.flush();
            output.reset();
            System.out.println("Output to SQL Microservice: " + packet.getType());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            System.out.println("Create SQLServiceConnection");
            while (!thread.isInterrupted()) {
                Packet packet = (Packet) input.readObject();


                if(!packet.getType().equals("ENC-MSG"))
                    continue;

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();

                System.out.println("Received from SQLConnection: " + ENC.getType());
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
                        synchronized (MainServer.getInstance().getClients()) {
                            Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                            while (iterator.hasNext()) {
                                client = iterator.next();
                                if(client.getUser() != null) {
                                    if (client.getUser().getUsername().equals(ENC.getidentifier()) && client.getUser().
                                            getId() == 0) {
                                        client.sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                                        break;
                                    }
                                }
                            }
                        }
                            client.setUser(LOS.getUser());
                            MainServer.getInstance().getClientIDMap().put(client.getUser().getId(), client);
                            listener.update(ENC.getMsg());

                        break;

                    // -----------------------------------------------------------------------------------------
                    //                                        Create Account
                    //------------------------------------------------------------------------------------------
                    case "ACS-MSG": // create/update account

                    case "ACF-MSG": // create/update account
                        if(!(ENC.getidentifier() instanceof String)) {
                            MainServer.getInstance().getClientIDMap().get(ENC.getidentifier()).
                                    sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                        }

                    case "LOF-MSG": // login failed
                        if(ENC.getidentifier() instanceof String)
                            synchronized (MainServer.getInstance().getClients()) {
                                Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                                while(iterator.hasNext()) {
                                    client = iterator.next();
                                    if(client.getUser() != null) {
                                        if (client.getUser().getUsername().equals(ENC.getidentifier()) && client.getUser().
                                                getId() == 0) {
                                            client.sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
                                            break;
                                        }
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
