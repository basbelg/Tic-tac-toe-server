package MainServer;

import DataClasses.TTT_GameData;
import DataClasses.TTT_ViewerData;
import Messages.Packet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static java.util.Collections.synchronizedList;

public class MainServer implements Runnable {
    private static MainServer instance = new MainServer(8000);

    // Thread
    private Thread thread;
    private BlockingQueue<Packet> requests;

    // Server
    private Socket socket;
    private ServerSocket serverSocket;

    // Client
    private int count;
    private List<Client> clients;
    private Map<Integer, Client> client_by_id;

    // Lobby
    private List<TTT_GameData> active_games;
    private Map<String, TTT_GameData> game_by_id;
    private Map<String, List<TTT_ViewerData>> active_viewers;

    private MainServer(int port) {
        try {
            // Server
            serverSocket = new ServerSocket(port);

            // Client
            clients = synchronizedList(new ArrayList<Client>());
            requests = new ArrayBlockingQueue<>(512);
            client_by_id = Collections.synchronizedMap(new HashMap<>());
            count = 0;

            // active lobby info
            active_games = Collections.synchronizedList(new ArrayList<>());
            game_by_id = Collections.synchronizedMap(new HashMap<>());
            active_viewers = Collections.synchronizedMap(new HashMap<>());

            // Thread
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {e.printStackTrace();}
    }

    public static MainServer getInstance() {return instance;}
    public BlockingQueue<Packet> getRequests() {return requests;}
    public List<Client> getClients() {return clients;}
    public List<TTT_GameData> getActiveGames() {return active_games;}
    public Map<String, List<TTT_ViewerData>> getActiveViewers() {return active_viewers;}
    public Map<Integer, Client> getClientIDMap() {return client_by_id;}
    public Map<String, TTT_GameData> getGame_by_id() {return game_by_id;}

    @Override
    public void run() {
        try {
            while(!thread.isInterrupted()) {
                socket = serverSocket.accept();
                System.out.println("socket accepted: " + socket.toString());

                clients.add(new Client(socket));
                ++count;
            }
        } catch(IOException e) {
            e.printStackTrace();
        } finally {
            System.out.println("server thread terminated");
            synchronized (clients) {
                Iterator i = clients.iterator();
                while(i.hasNext())
                    ((Client)i.next()).terminateConnection();
            }
        }
    }

    public synchronized void terminateServer() {
        thread.interrupt();
        try {serverSocket.close();} catch (IOException e) {e.printStackTrace();}
    }
}
