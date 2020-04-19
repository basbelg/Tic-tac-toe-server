package MainServer;

import Messages.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.nio.channels.ClosedByInterruptException;
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

                // handle

                // send to relevant party
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
