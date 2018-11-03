package client;

import java.io.*;
import java.net.Socket;

public class WriteThread extends Thread {

    private PrintWriter writer;
    private Socket socket;
    private  Client client;

    public WriteThread(Socket socket, Client client) {
        this.socket = socket;
        this.client = client;

        try {
            OutputStream outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream, true);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void run() {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));//Reads standard input

        String text = "";
        try {
            sleep(100);//Sleep to show connected users
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        while(!this.isInterrupted()) {
            System.out.print("> ");// New line ready

            try {
                text = reader.readLine();//Read standard input
            } catch (IOException e) {
                e.printStackTrace();
            }

            if(text.equals("logout"))
                client.endThreads();//If user wants to logout end threads

            writer.println(text);//Send message

        }
    }
}
