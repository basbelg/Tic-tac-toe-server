package MainServer;

import DataClasses.TTT_GameData;
import Messages.*;

import java.util.Iterator;
import java.util.concurrent.BlockingQueue;

public class Publisher implements Runnable{
    private static Publisher instance = new Publisher();

    private BlockingQueue<Packet> requests;
    private Thread thread;

    private Publisher() {
        requests = MainServer.getInstance().getRequests();

        thread = new Thread(this);
        thread.run();
    }

    public static Publisher getInstance() {return instance;}

    @Override
    public void run() {
        try {
            while (!thread.isInterrupted()) {
                Packet packet = requests.take();

                if (packet.getType() == "ENC-MSG") {
                    EncapsulatedMessage ENC = (EncapsulatedMessage) packet.getData();
                    switch (ENC.getType()) {
                        case "LOG-MSG": // Login
                            LoginMessage LOG = (LoginMessage) ENC.getMsg();
                            boolean LOF = false;

                            synchronized (MainServer.getInstance().getActiveGames()) {
                                Iterator<Client> iterator = MainServer.getInstance().getClients().iterator();
                                while (iterator.hasNext()) {
                                    Client client = iterator.next();
                                    if (client.getUser() != null && client.getUser().getUsername() == LOG.getUsername()) {
                                        LOF = true;
                                        break;
                                    }
                                }
                            }
                            if (LOF)
                                ((Client) ENC.getidentifier()).sendPacket(new Packet("LOF-MSG", (AccountFailedMessage) MessageFactory.getMessage("LOF-MSG")));
                            else
                                SQLServiceConnection.getInstance().sendPacket(packet);
                            break;
                    }
                }
            }
        } catch (InterruptedException | NullPointerException e) {
            e.printStackTrace();
        }
    }

    public synchronized void terminateServer() {
        thread.interrupt();
        requests = null;
    }
}
