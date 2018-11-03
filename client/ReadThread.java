package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;

public class ReadThread extends Thread {

    private BufferedReader reader;
    private Socket socket;
    private Client client;

    public ReadThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            InputStream inputStream = socket.getInputStream();
            reader = new BufferedReader(new InputStreamReader(inputStream));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        while(!this.isInterrupted()) {
            try {
                String response = reader.readLine();//Read server response

                if(response.equals("loggedin")){
                    System.out.print("Logged in as ");
                    response = reader.readLine();//Log in as user
                }

                System.out.println(response);//Print out response
                if(response.equals("> Logging out..."))//Exit loop if user is logging out
                    continue;
                System.out.print("> ");//New line ready for input
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return;
    }
}
