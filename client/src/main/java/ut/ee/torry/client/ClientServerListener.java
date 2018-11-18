package ut.ee.torry.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientServerListener {

    private static final Logger log = LoggerFactory.getLogger(DownloadTorrentTask.class);

    private final int port;

    public ClientServerListener(int port) {
        this.port = port;
    }

    public void startListener() throws IOException {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            ExecutorService executor = Executors.newCachedThreadPool();

            while (true) {
                Socket socket = serverSocket.accept();
                log.info("Starting to receive info");

                executor.execute(() -> {
                    try (DataInputStream in = new DataInputStream(new BufferedInputStream(socket.getInputStream()))) {
                        parseReceivedStream(in);
                    } catch (IOException e) {
                        log.error("Unable to read from socket: ", e);
                    }

                });

            }

        }
    }

    private void parseReceivedStream(DataInputStream dis) throws IOException {
        log.info("Reading info from stream");

        int len = dis.readInt();
        byte id = dis.readByte();

        switch (id) {
            case 7:
                short index = dis.readShort();
                byte[] bytes = new byte[len - 7];
                log.info("Received piece <len:{}><id:{}><index:{}><data:omitted>", len, id, index);
                break;
            default:
                log.warn("Received event with id {} which is not yet supported or unknown.", id);
        }

    }

}
