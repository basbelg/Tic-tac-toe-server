package MainServer;

import DataClasses.User;
import Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.lang.invoke.SerializedLambda;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Client implements Runnable{
    private User user = null;

    private Thread thread;

    private Socket socket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private List<Client> clients;
    private BlockingQueue<Packet> requests;

    public Client(Socket socket) {
        try {
            this.socket = socket;
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());

            this.clients = MainServer.getInstance().getClients();
            this.requests = MainServer.getInstance().getRequests();

            thread = new Thread(this);
            thread.run();
        } catch (IOException e) {
            e.printStackTrace();
        }
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

                switch (packet.getType()) {
                    case "CNT-MSG": // Connect to lobby
                        break;
                    case "CAI-MSG": // Create AI Game Lobby
                        break;
                    case "CLB-MSG": // Create Game Lobby
                        break;
                    case "MOV-MSG": // Move
                        break;
                    case "SPC-MSG": // Spectate
                        break;

                    case "DAC-MSG": // Deactivate Account
                        break;
                    case "GLG-MSG": // Game Log
                        break;
                    case "GRE-MSG": // Game Result
                        
                        SQLServiceConnection.getInstance().sendPacket(packet);
                        break;
                    case "GVW-MSG": // Game Viewers
                        ((GameViewersMessage)packet.getData()).setUserId(user.getId());
                        SQLServiceConnection.getInstance().sendPacket(packet);
                        break;

                    case "CAC-MSG": // Create Account
                        CreateAccountMessage CAC = (CreateAccountMessage) packet.getData();
                        user = new User(0, CAC.getNewUser().getUsername(), null, null, null, false);
                        SQLServiceConnection.getInstance().sendPacket(packet);
                        break;

                    case "LOG-MSG": // Login
                        LoginMessage LOG = (LoginMessage) packet.getData();
                        boolean LOF = false;

                        for(Client client: clients)
                            if(client.user != null && client.user.getUsername() == LOG.getUsername()) {
                                sendPacket(new Packet("LOF-MSG", (AccountFailedMessage) MessageFactory.getMessage("LOF-MSG")));
                                LOF = true;
                                break;
                            }

                        if(!LOF) {
                            user = new User(LOG.getUsername(), null, null, null);
                            SQLServiceConnection.getInstance().sendPacket(packet);
                        }
                        break;

                    case "STS-MSG": // Stats
                        SQLServiceConnection.getInstance().sendPacket(packet);
                        break;
                    case "UPA-MSG": // Update Account Info
                        ((UpdateAccountInfoMessage)packet.getData()).getUpdatedUser().setId(user.getId());
                        SQLServiceConnection.getInstance().sendPacket(packet);
                        break;
                    default:
                        requests.add(packet);
                }
            }
        } catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
        finally {synchronized (clients) {clients.remove(this);}}
    }

    public void terminateConnection() {
        thread.interrupt();
        try {
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public User getUser() {return user;}
}
