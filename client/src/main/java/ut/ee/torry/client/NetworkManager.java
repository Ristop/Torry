package ut.ee.torry.client;

import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class NetworkManager {

    private final Socket socket;

    public NetworkManager(String host, int port) throws IOException {
        this.socket = new Socket(host, port);
    }

    public void sendPiece(Piece piece) throws IOException {
        DataOutputStream dataOutputStream = new DataOutputStream(socket.getOutputStream());
        dataOutputStream.write(piece.getBytes());
        socket.close();
    }

}
