package ut.ee.torry.client;

import be.christophedetroyer.torrent.Torrent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.common.Peer;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkManager implements AutoCloseable {

    private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);

    private static final String PSTR = "BitTorrent protocol";

    private final String ip;
    private final int port;
    private final Socket socket;

    public NetworkManager(Peer peer) throws IOException {
        this.ip = peer.getIp();
        this.port = peer.getPort();
        this.socket = new Socket(ip, port);
    }

    /**
     * This must be the first message transmitted by the client!
     * <pstrlen><pstr><reserved><info_hash><peer_id>
     */
    public void handShake(Torrent torrent, String peerId) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        int pstrLen = 19;

        dos.writeByte(pstrLen);
        dos.writeBytes(PSTR);

        // Reserved 8 bits
        for (int i = 0; i < 8; i++) {
            dos.writeByte(0);
        }

        // 20-bytes
        dos.writeBytes(torrent.getInfo_hash());

        // 20-bytes
        dos.writeBytes(peerId);

        log.info("Sent handshake request to peer: {}", peerId);
    }

    /**
     * <len=005><id=6><index>
     */
    public void requestPiece(int index) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        int len = 5;
        byte id = 6;

        dos.writeInt(len);
        dos.writeByte(id);
        dos.writeShort(index);
        dos.flush();

        log.info("Sent request piece request <len:{}><id:{}><index:{}>", len, id, index);
    }

    /**
     * This should return:
     * - <len=0009+X><id=7><index><begin><block>
     * But currently returns because we are sending the whole piece:
     * - <len=0007+X><id=7><index><piece>
     */
    public void sendPiece(Piece piece) throws IOException {
        DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));
        int len = 5 + piece.getBytes().length;
        byte id = 7;
        int index = piece.getId();

        dos.writeInt(len);
        dos.writeByte(id);
        dos.writeShort(index);
        dos.write(piece.getBytes());

        log.info("Sent send piece request <len:{}><id:{}><index:{}><data:omitted>", len, id, index);
    }

    @Override
    public void close() throws Exception {
        socket.close();
    }

}
