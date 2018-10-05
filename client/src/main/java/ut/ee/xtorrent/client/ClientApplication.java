package ut.ee.xtorrent.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PreDestroy;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;

@SpringBootApplication(scanBasePackages = {"ut.ee.xtorrent"})
public class ClientApplication {

    private static final Logger log = LoggerFactory.getLogger(ClientApplication.class);

    protected ClientApplication() {
    }

    public static void main(String[] args) throws IOException, InterruptedException {
//        SpringApplication app = new SpringApplication(ClientApplication.class);
//        app.setBannerMode(Banner.Mode.LOG);
//        app.run(args);

        new ClientThreadRead(6869).start();
        //startClient("localhost",6869);
        startServerSocket(6868);
    }

    @PreDestroy
    public void shutdown() {
        log.info("Shutting down client.");
    }


    public static void startServerSocket(int port) throws IOException {

        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server is listening on port " + port);
            while (true) {
                Socket socket = serverSocket.accept();
                System.out.println("New client connected");
                new ClientThreadWrite(socket).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}