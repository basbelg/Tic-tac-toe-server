package MainServer;

import DataClasses.TTT_GameData;
import DataClasses.TTT_ViewerData;
import Database.DBManager;
import Messages.*;
import ServerInterfaces.ServerListener;

import java.io.IOException;
import java.io.Serializable;
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

    // UI
    private List<ServerListener> observers;

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

            // UI
            observers = Collections.synchronizedList(new ArrayList<>());

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
        Publisher.getInstance();

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

    // UI
    public void addObserver(ServerListener listener) {observers.add(listener);}
    public void removeObserver(ServerListener listener) {observers.remove(listener);}
    public void notifyObservers(Serializable msg, Object data) {
        synchronized (observers) {
            Iterator<ServerListener> iterator = observers.iterator();
            while(iterator.hasNext())
            {
                ServerListener listener = iterator.next();
                switch(listener.getClass().getSimpleName())
                {
                    case "ServerController":
                        if(msg instanceof EncapsulatedMessage || msg instanceof DeactivateAccountMessage || msg instanceof AccountSuccessfulMessage ||
                           msg instanceof AllGamesMessage || msg instanceof RegisteredUsersMessage || msg instanceof UpdateAccountInfoMessage ||
                           msg instanceof LoginSuccessfulMessage || msg instanceof DisconnectMessage || msg instanceof GameResultMessage ||
                           msg instanceof ConnectToLobbyMessage || msg instanceof CreateAIGameMessage || msg instanceof SpectateMessage ||
                           msg instanceof StopSpectatingMessage || msg instanceof CreateLobbyMessage || msg instanceof InactiveGameMessage)
                        {
                            listener.update(msg, data);
                        }
                        break;
                    case "GameDetailsController":
                        if(msg instanceof AllGameInfoMessage || msg instanceof MoveMessage || msg instanceof GameResultMessage ||
                            msg instanceof SpectateMessage)
                        {
                            listener.update(msg, data);
                        }
                        break;
                    case "ModifyPlayerController":
                        if(msg instanceof AdminAccountFailedMessage || msg instanceof AdminAccountSuccessfulMessage)
                        {
                            listener.update(msg, data);
                        }
                        break;

                }
            }
        }
    }


}
