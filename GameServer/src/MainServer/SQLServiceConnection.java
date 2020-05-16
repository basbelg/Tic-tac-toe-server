package MainServer;

import Messages.*;
import ServerInterfaces.ServerListener;
import UI.Main;
import UI.ServerController;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
import java.util.ArrayList;
import java.util.Iterator;

public class SQLServiceConnection implements Runnable{
    private static SQLServiceConnection instance = new SQLServiceConnection(8002);

    private Socket socket;
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

                if(!packet.getType().equals("ENC-MSG")) {
                    switch (packet.getType()) {
                        case "RUS-MSG": // All Registered Users
                        case "AGS-MSG": // All Games
                        case "AAF-MSG": // Admin Update Account Failed
                        case "AAS-MSG": // Admin Update Account Successful
                            MainServer.getInstance().notifyObservers(packet.getData(), null);
                            break;
                        case "AGI-MSG": // Game Info
                            AllGameInfoMessage AGI = (AllGameInfoMessage) packet.getData();
                            MainServer.getInstance().notifyObservers(packet.getData(), AGI.getId());
                            break;
                    }
                    continue;
                }

                EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();

                System.out.println("Received from SQLConnection: " + ENC.getType());
                switch (ENC.getType()) {
                    case "UPA-MSG": // Update Account Info
                        Client client = MainServer.getInstance().getClientIDMap().get(ENC.getidentifier());
                        MainServer.getInstance().notifyObservers(ENC.getMsg(), null);
                        if(client == null)
                            break;
                        client.setUser(((UpdateAccountInfoMessage)ENC.getMsg()).getUpdatedUser());
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
                        client = null;
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
                            MainServer.getInstance().notifyObservers(ENC.getMsg(), null);
                        break;

                    // -----------------------------------------------------------------------------------------
                    //                                        Create Account
                    //------------------------------------------------------------------------------------------
                    case "ACS-MSG": // create/update account
                        MainServer.getInstance().notifyObservers(ENC.getMsg(), null);
                    case "ACF-MSG": // create/update account
                        if(!(ENC.getidentifier() instanceof String)) {
                            Client account_creator = MainServer.getInstance().getClientIDMap().get(ENC.getidentifier());
                            if(account_creator != null)
                                account_creator.sendPacket(new Packet(ENC.getType(), ENC.getMsg()));
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
