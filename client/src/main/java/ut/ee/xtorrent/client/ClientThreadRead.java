package ut.ee.xtorrent.client;

import org.apache.commons.io.FileUtils;

import java.io.*;
import java.net.Socket;

public class ClientThreadRead extends  Thread{

    private int port;


    public ClientThreadRead(int port) {
        this.port = port;
    }

    public void run() {

        boolean notConnected = true;
        while(notConnected)
            try (
                    Socket socket = new Socket("localhost", port);
        //            BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()))

                    DataInputStream dis = new DataInputStream(socket.getInputStream())
            ) {
                byte incomingFileLength = dis.readByte();
                byte[] file = new byte[incomingFileLength];
                dis.readFully(file);

                FileUtils.writeByteArrayToFile(new File("file_from_other_client.txt"), file);
           //     System.out.println(new String(file));

                notConnected = false;

             /*
                String fromServer;
                while ((fromServer = in.readLine()) != null) {
                    System.out.println("Other client: " + fromServer);
                }

                */
            } catch (IOException e) {
                System.out.println("Other client not yet available");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
            }
    }
}
