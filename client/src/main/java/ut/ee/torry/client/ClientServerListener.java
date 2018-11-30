package ut.ee.torry.client;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.client.event.Handshake;
import ut.ee.torry.client.event.RequestPiece;
import ut.ee.torry.client.event.SendPiece;
import ut.ee.torry.client.event.TorryRequest;

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

    private final BlockingQueue<TorryRequest> requestQueue;

    public ClientServerListener(int port, BlockingQueue<TorryRequest> requestQueue) {
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
                        requestQueue.put(parseHandshake(in));
                        while (!socket.isClosed()) {
                            requestQueue.put(parseLengthPrefixedMessage(in));
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

    private TorryRequest parseHandshake(DataInputStream dis) throws IOException {
        byte pstrLen = dis.readByte();

        byte[] pstrBytes = new byte[pstrLen];
        dis.read(pstrBytes);
        String pstr = new String(pstrBytes);

        // Reserved 8 bits
        for (int i = 0; i < 8; i++) {
            byte b = dis.readByte();
        }

        byte[] torrentHashBytes = new byte[40];
        dis.read(torrentHashBytes);
        String torrentHash = new String(torrentHashBytes);

        byte[] peerIdBytes = new byte[20];
        dis.read(peerIdBytes);
        String peerId = new String(peerIdBytes);

        log.info("Received handshake from client {} for torrent {} with protocol {}", peerId, torrentHash, pstr);
        return new Handshake(peerId, torrentHash, pstr);
    }

    private TorryRequest parseLengthPrefixedMessage(DataInputStream dis) throws IOException {
        log.info("Reading info from stream");

        int len = dis.readInt();
        byte id = dis.readByte();

        if (id == 6) {
            short index = dis.readShort();
            log.info("Received request piece <len:{}><id:{}><index:{}>", len, id, index);
            return new RequestPiece(index);
        } else if (id == 7) {
            short index = dis.readShort();
            byte[] bytes = new byte[len - 5];
            dis.read(bytes);
            log.info("Received piece <len:{}><id:{}><index:{}><data:omitted>", len, id, index);
            return new SendPiece(index, bytes);
        } else {
            log.warn("Received event with id {} which is not yet supported or unknown.", id);
            throw new NotImplementedException("Received event with id " + id + " which is not yet supported or unknown.");
        }

    }

}
