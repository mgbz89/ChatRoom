package client;

import com.sun.corba.se.pept.transport.ReaderThread;
import server.ClientThread;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public class Client {

    private String hostname;
    private int port;
    private WriteThread wt;
    private ReadThread rt;

    public Client(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void execute() {
        try {
            Socket socket = new Socket(hostname, port);
            System.out.println("Connected to the chat server");//Connects to server
            wt = new WriteThread(socket, this);
            rt = new ReadThread(socket, this);
            wt.start();//Begins sending thread
            rt.start();//Begins receiving thread


        } catch (UnknownHostException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        int port = 15201;
        String hostname = "localhost";

        Client client = new Client(hostname, port);
        client.execute();
    }

    public void endThreads(){//Ends the threads
        wt.interrupt();
        rt.interrupt();
    }
}
