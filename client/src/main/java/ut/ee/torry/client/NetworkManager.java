package ut.ee.torry.client;

import ut.ee.torry.common.Peer;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.Arrays;

public class NetworkManager {

    private final Socket socket;

    public NetworkManager(Peer peer) throws IOException {
        this.socket = new Socket(peer.getIp(), peer.getPort());
    }

    public void sendPiece(Piece piece) throws IOException {
        BufferedOutputStream dataOutputStream = new BufferedOutputStream(socket.getOutputStream());
        System.out.println(piece.getBytes().length);
        dataOutputStream.write(Arrays.copyOfRange(piece.getBytes(), 0, 4000));
        socket.close();
    }

}
