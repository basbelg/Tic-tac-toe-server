package GameService;

import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class GameHandler implements Runnable{
    GameHandler instance = new GameHandler();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private GameHandler() {
        requests = Server.getInstance().getRequests();

        thread = new Thread(this);
        thread.run();
    }

    @Override
    public void run() {
        try {
            while(!thread.isInterrupted()) {
                Packet packet = requests.take();


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
