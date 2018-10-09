package ut.ee.xtorrent.client;

import java.io.*;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class ClientThreadWrite extends Thread {

    private Socket clientSocket;
    private String hasFile;


    public ClientThreadWrite(Socket socket, String hasFile) {
        this.clientSocket = socket;
        this.hasFile = hasFile;
    }

    public void run() {


        try (BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             DataOutputStream dos = new DataOutputStream(clientSocket.getOutputStream()))
        //     PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true))
        {

            Path pathToFile = Paths.get(hasFile);
            byte[] bytesOfFile  = Files.readAllBytes(pathToFile);

            dos.write(bytesOfFile.length);
            dos.write(bytesOfFile);
            dos.flush();

   /*
            String user_input;
            while(true) {
                if((user_input = userInput.readLine()) != null){
                  //  pw.println(user_input);
                    Thread.sleep(1000);
                }
            }
            */
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
