package ut.ee.xtorrent.client;

import java.io.*;
import java.net.Socket;

public class ClientThreadWrite extends Thread {

    private Socket clientSocket;


    public ClientThreadWrite(Socket socket) {
        this.clientSocket = socket;
    }

    public void run() {

        String receivedMessage;
        try (BufferedReader socketIn = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             PrintWriter pw = new PrintWriter(clientSocket.getOutputStream(), true)){
            String user_input;
            while(true) {
                if((user_input = userInput.readLine()) != null){
                    pw.println(user_input);
                    Thread.sleep(1000);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
