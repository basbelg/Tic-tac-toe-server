package SQLService;

import Messages.Packet;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class SQLServer implements Runnable {
    private static SQLServer instance = new SQLServer(8002);

    private Socket socket;
    private ServerSocket serverSocket;
    private ObjectInputStream input;
    private ObjectOutputStream output;

    private Thread thread;
    private BlockingQueue<Packet> requests;

    private SQLServer(int port) {
        try {
            serverSocket = new ServerSocket(port);

            requests = new ArrayBlockingQueue<>(256);

            thread = new Thread(this);
            thread.start();
        } catch (IOException e) {e.printStackTrace();}
    }

    public static SQLServer getInstance() {return instance;}
    public BlockingQueue<Packet> getRequests() {return requests;}

    public void sendPacket(Packet packet) {
        try {
            output.writeObject(packet);
            output.flush();
            output.reset();
            System.out.println("Output to Main Server: " + packet.getType());
        } catch (IOException e) {e.printStackTrace();}
    }

    @Override
    public void run() {
        System.out.println("Create SQLServer");
        try {
            socket = serverSocket.accept();
            input = new ObjectInputStream(socket.getInputStream());
            output = new ObjectOutputStream(socket.getOutputStream());
            System.out.println("Main Server connected to SQL Server");

            SQLHandler.getInstance();
            while(!thread.isInterrupted()) {
                Packet packet = (Packet) input.readObject();
                System.out.println("SQLServer: " + packet.getType());
                requests.add(packet);
            }
        } catch (IOException | ClassNotFoundException e) {e.printStackTrace();}
        finally {SQLHandler.getInstance().terminateServer();}
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
