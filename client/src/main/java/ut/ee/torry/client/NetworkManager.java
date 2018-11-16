package ut.ee.torry.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ut.ee.torry.common.Peer;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkManager {

    private static final Logger log = LoggerFactory.getLogger(NetworkManager.class);

    private final Socket socket;

    public NetworkManager(Peer peer) throws IOException {
        this.socket = new Socket(peer.getIp(), peer.getPort());
    }

    /**
     * <len=0013><id=6><index>
     */
    public void requestPiece(int index) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            int len = 5;
            byte id = 6;

            dos.writeInt(len);
            dos.writeByte(id);
            dos.writeShort(index);

            log.info("Sent request piece request <len:{}><id:{}><data:omitted>", len, id, index);
        }
    }

    /**
     * This should return:
     * - <len=0009+X><id=7><index><begin><block>
     * But currently returns because we are sending the whole piece:
     * - <len=0007+X><id=7><index><piece>
     */
    public void sendPiece(Piece piece) throws IOException {
        try (DataOutputStream dos = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()))) {
            int len = 5 + piece.getBytes().length;
            byte id = 7;
            int index = piece.getId();

            dos.writeInt(len);
            dos.writeByte(id);
            dos.writeShort(index);
            dos.write(piece.getBytes());

            log.info("Sent send piece request <len:{}><id:{}><index:{}><data:omitted>", len, id, index);
        }
    }

}
