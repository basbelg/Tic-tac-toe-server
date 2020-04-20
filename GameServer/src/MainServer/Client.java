package MainServer;

import DataClasses.LobbyInfo;
import DataClasses.TTT_GameData;
import DataClasses.User;
import Messages.*;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Client implements Runnable, Serializable {
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
                    case "CAC-MSG": // Create Account
                        CreateAccountMessage CAC = (CreateAccountMessage) packet.getData();
                        user = new User(0, CAC.getNewUser().getUsername(), null, null, null, false);
                        SQLServiceConnection.getInstance().sendPacket(packet);
                        break;

                    case "LOG-MSG": // Login
                        LoginMessage LOG = (LoginMessage) packet.getData();
                        user = new User(LOG.getUsername(), null, null, null);
                        EncapsulatedMessage ENC_LOG = new EncapsulatedMessage(packet.getType(), this, packet.getData());
                        requests.add(new Packet("ENC-MSG", ENC_LOG));
                        break;

                    case "AAG-MSG": // All active games
                        AllActiveGamesMessage AAG = (AllActiveGamesMessage) packet.getData();
                        List<LobbyInfo> allGames = new ArrayList<>();
                        synchronized (MainServer.getInstance().getActiveGames()) {
                            Iterator<TTT_GameData> iterator = MainServer.getInstance().getActiveGames().iterator();
                            while(iterator.hasNext()) {

                            }
                        }

                        break;

                    case "SPC-MSG": // Spectate
                    case "CNT-MSG": // Connect to lobby
                    case "CAI-MSG": // Create AI Game Lobby
                    case "CLB-MSG": // Create Game Lobby
                    case "MOV-MSG": // Move
                        EncapsulatedMessage ENC_Game = new EncapsulatedMessage(packet.getType(), Integer.valueOf(user.getId()), packet.getData());
                        GameServiceConnection.getInstance().sendPacket(new Packet("ENC-MSG", ENC_Game));
                        break;

                    case "GMP-MSG": // Games played
                    case "GVW-MSG": // Game Viewers
                    case "DAC-MSG": // Deactivate Account
                    case "UPA-MSG": // Update Account Info
                    case "GLG-MSG": // Game Log
                    case "STS-MSG": // Stats
                    case "IAG-MSG": // Inactive game message
                        EncapsulatedMessage ENC = new EncapsulatedMessage(packet.getType(), Integer.valueOf(user.getId()), packet.getData());
                        SQLServiceConnection.getInstance().sendPacket(new Packet("ENC-MSG", ENC));
                        break;
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
