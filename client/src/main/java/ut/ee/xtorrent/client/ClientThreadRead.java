package ut.ee.xtorrent.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

public class ClientThreadRead extends  Thread{

    private int port;


    public ClientThreadRead(int port) {
        this.port = port;
    }

    public void run() {
        while(true) {
            try (
                    Socket socket = new Socket("localhost", port);
                    BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))
            ) {
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    System.out.println("Other client: " + fromServer);
                }
            } catch (IOException e) {
                System.out.println("ServerSide not available");
            }
            try {
                Thread.sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
