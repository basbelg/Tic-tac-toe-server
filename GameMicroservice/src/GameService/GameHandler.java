package GameService;

import Messages.EncapsulatedMessage;
import Messages.Packet;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class GameHandler implements Runnable{
    private static GameHandler instance = new GameHandler();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private GameHandler() {
        requests = GameServer.getInstance().getRequests();

        thread = new Thread(this);
        thread.run();
    }

    public static GameHandler getInstance() {return instance;}

    @Override
    public void run() {
        try {
            while(!thread.isInterrupted()) {
                EncapsulatedMessage ENC = (EncapsulatedMessage) requests.take().getData();

                switch (ENC.getType())
            }
        } catch (IOException | InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public synchronized void terminateServer() {
        thread.interrupt();
        requests = null;
    }
}
