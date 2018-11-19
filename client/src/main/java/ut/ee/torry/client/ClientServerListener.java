package ut.ee.torry.client;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.client.event.SendPiece;
import ut.ee.torry.client.event.TorrentRequest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientServerListener implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TorrentTask.class);

    private final int port;

    private final BlockingQueue<TorrentRequest> requestQueue;

    public ClientServerListener(int port, BlockingQueue<TorrentRequest> requestQueue) {
        this.port = port;
        this.requestQueue = requestQueue;
    }

    @Override
    public void run() {
        try {
            startListener();
        } catch (IOException e) {
            log.error("Unable to start listener: ", e);
        }
    }

    public void startListener() throws IOException {
        log.info("Starting server socket listening on port {}.", port);
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ExecutorService executor = Executors.newCachedThreadPool();

            while (true) {
                Socket socket = serverSocket.accept();
                log.info("Starting to receive info");

                executor.execute(() -> {
                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                        while (!socket.isClosed()) {
                            requestQueue.put(parseEventFromReceivedStream(in));
                        }
                    } catch (IOException e) {
                        log.error("Unable to read from socket: ", e);
                    } catch (InterruptedException e) {
                        log.error("Unable to add event to queue: ", e);
                    }

                });

            }

        }
    }

    private TorrentRequest parseEventFromReceivedStream(DataInputStream dis) throws IOException {
        log.info("Reading info from stream");

        int len = dis.readInt();
        byte id = dis.readByte();

        switch (id) {
            case 7:
                short index = dis.readShort();
                byte[] bytes = new byte[len - 5];
                dis.read(bytes);
                log.info("Received piece <len:{}><id:{}><index:{}><data:omitted>", len, id, index);
                return new SendPiece(index, bytes);
            default:
                log.warn("Received event with id {} which is not yet supported or unknown.", id);
                throw new NotImplementedException("Received event with id " + id + " which is not yet supported or unknown.");
        }

    }

}
