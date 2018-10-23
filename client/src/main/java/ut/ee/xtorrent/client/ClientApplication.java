package ut.ee.xtorrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.Banner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

@SpringBootApplication(scanBasePackages = {"ut.ee.xtorrent"})
public class ClientApplication {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    protected ClientApplication() {
    }

    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ClientApplication.class);
        app.setBannerMode(Banner.Mode.LOG);
        app.run(args);

//        int peerServerPort = Integer.parseInt(args[0]);
//        int otherClientPort = Integer.parseInt(args[1]);
//        String hasFile, needsFile;
//        hasFile = args[2];
//      //  String pathToNeededFile = args[3];
//
//        new ClientThreadRead(otherClientPort).start();
//        //startClient("localhost",6869);
//        startServerSocket(peerServerPort, hasFile);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down client.");
    }


    public static void startServerSocket(int port, String hasFile) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new ClientThreadWrite(socket, hasFile).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}