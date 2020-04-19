package MainServer;

import Messages.Packet;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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

    private MainServer(int port) {
        try {
            // Server
            serverSocket = new ServerSocket(port);

            // Client
            clients = synchronizedList(new ArrayList<Client>());
            requests = new ArrayBlockingQueue<>(512);
            count = 0;

            // Thread
            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {e.printStackTrace();}
    }

    public static MainServer getInstance() {return instance;}
    public BlockingQueue<Packet> getRequests() {return requests;}
    public List<Client> getClients() {return clients;}

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
