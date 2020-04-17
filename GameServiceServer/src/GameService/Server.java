package GameService;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class Server implements Runnable{
    private static Server instance = new Server(8001);

    private Socket socket;
    private ServerSocket serverSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Thread thread;
    private BlockingQueue<Packet> requests;

    private Server(int port) {
        try {
            serverSocket = new ServerSocket(port);

            requests = new ArrayBlockingQueue<>(256);

            thread = new Thread(this);
            thread.run();
        } catch (IOException e) {e.printStackTrace();}
    }

    public static Server getInstance() {return instance;}
    public BlockingQueue<Packet> getRequests() {return requests;}

    public void sendPacket(Packet packet) {
        try {
            output.writeObject(packet);
            output.flush();
            output.reset();
        } catch (IOException e) {e.printStackTrace();}
    }

    @Override
    public void run() {
        try {
            socket = serverSocket.accept();
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());

            while(!thread.isInterrupted()) {
                Packet packet = (Packet) input.readObject();
                requests.add(packet);
            }
        } catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
        finally {

        }
    }

    public void terminateServer() {
        thread.interrupt();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
