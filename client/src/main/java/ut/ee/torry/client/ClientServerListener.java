package ut.ee.torry.client;

import org.apache.commons.lang3.NotImplementedException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.client.event.BitField;
import ut.ee.torry.client.event.Handshake;
import ut.ee.torry.client.event.Have;
import ut.ee.torry.client.event.RequestPiece;
import ut.ee.torry.client.event.SendPiece;
import ut.ee.torry.client.event.TorryRequest;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientServerListener implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(TorrentTask.class);

    private final int port;

    private final Map<String, BlockingQueue<TorryRequest>> requestQueues;

    public ClientServerListener(int port, Map<String, BlockingQueue<TorryRequest>> requestQueues) {
        this.port = port;
        this.requestQueues = requestQueues;
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
                        Handshake handshake = parseHandshake(in);

                        BlockingQueue<TorryRequest> requestQueue = requestQueues.get(handshake.getTorrentHash());

                        requestQueue.put(handshake);
                        while (!socket.isClosed()) {
                            requestQueue.put(parseLengthPrefixedMessage(in, handshake.getPeerId()));
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

    private Handshake parseHandshake(DataInputStream dis) throws IOException {
        byte pstrLen = dis.readByte();

        byte[] pstrBytes = new byte[pstrLen];
        dis.readFully(pstrBytes);
        String pstr = new String(pstrBytes);

        // Reserved 8 bits
        for (int i = 0; i < 8; i++) {
            byte b = dis.readByte();
        }

        byte[] torrentHashBytes = new byte[40];
        dis.readFully(torrentHashBytes);
        String torrentHash = new String(torrentHashBytes);

        byte[] peerIdBytes = new byte[20];
        dis.readFully(peerIdBytes);
        String peerId = new String(peerIdBytes);

        log.info("Received handshake from client {} for torrent {} with protocol {}", peerId, torrentHash, pstr);
        return new Handshake(peerId, torrentHash, pstr);
    }

    private TorryRequest parseLengthPrefixedMessage(DataInputStream dis, String peerId) throws IOException {
        int len = dis.readInt();
        byte id = dis.readByte();

        if (id == 4) {
            short index = dis.readShort();
            log.info("Received have from peer {}", peerId);
            return new Have(peerId, index);
        } else if (id == 5) {
            boolean[] bitfield = new boolean[len - 1];
            for (int i = 0; i < len - 1; i++) {
                bitfield[i] = dis.readBoolean();
            }
            log.info("Received bitField <bitfield:<omitted>> from peer {}", peerId);
            return new BitField(bitfield, peerId);
        } else if (id == 6) {
            short index = dis.readShort();
            log.info("Received request piece <len:{}><id:{}><index:{}>", len, id, index);
            return new RequestPiece(index, peerId);
        } else if (id == 7) {
            short index = dis.readShort();
            byte[] bytes = new byte[len - 5];
            dis.readFully(bytes);
            log.info("Received piece <len:{}><id:{}><index:{}><data:<omitted>>", len, id, index);
            return new SendPiece(index, bytes);
        } else {
            log.warn("Received event with id {} which is not yet supported or unknown.", id);
            throw new NotImplementedException("Received event with id " + id + " which is not yet supported or unknown.");
        }

    }

}
