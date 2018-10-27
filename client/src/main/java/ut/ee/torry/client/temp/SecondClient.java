package ut.ee.torry.client.temp;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class SecondClient {

    public static void main(String[] args) throws IOException, InterruptedException {
        new ClientThreadRead(6868).start();
        startServerSocket(6869);
    }


    public static void startServerSocket(int port) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
//                new ClientThreadWrite(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
