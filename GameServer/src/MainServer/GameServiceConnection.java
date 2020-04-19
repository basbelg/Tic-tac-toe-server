package MainServer;

import Messages.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
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

    public void sendPacket(Packet packet) {
        try {
            output.writeObject(packet);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (!thread.isInterrupted()) {
                Packet packet = (Packet) input.readObject();

                // handle

                // send to all players and spectators
                // Server.getInstance().getClients().get(1).sendPacket(new Move); ...
                // for(Client client: Server.getInstance().getClients()) ...
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
