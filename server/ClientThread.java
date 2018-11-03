package server;

import org.json.simple.parser.ParseException;

import java.awt.*;
import java.io.*;
import java.net.Socket;


public class ClientThread extends Thread {

    private Socket socket;
    private ChatServer server;
    private PrintWriter writer;
    private String username;
    private boolean loggedin = false;

    public ClientThread(Socket socket, ChatServer server){
        this.socket = socket;
        this.server = server;
    }

    public void run() {
        try {
            InputStream inputStream = socket.getInputStream();
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            OutputStream outputStream = socket.getOutputStream();
            writer = new PrintWriter(outputStream, true);

            printUsers();//Show connected users

            String clientMessage;
            String serverMessage;

            loop: while (!socket.isClosed()) {
                clientMessage = reader.readLine();//Reads input from WriteThread

                String action = getAction(clientMessage);//Action of user
                String message = getMessage(clientMessage, action);//Message from user

                switch(action) {
                    case "login":

                        if(loggedin) {//if user is already logged on
                            sendMessage("Already logged in as " + username);
                            break;
                        }


                        try {
                            username = message.substring(1, message.length());
                            username = username.substring(0, username.indexOf(" "));//parse out username
                            switch(server.confirmUser(username, message.replace(" " + username + " ", ""))){//Check confirm user method on ChatServer
                                case "loggedin":
                                    sendMessage("loggedin");
                                    sendMessage(username);
                                    server.broadcast("New user connected: " + username, this);
                                    loggedin = true;
                                    serverOutput(username + " login.");
                                    break;

                                case "wrong":
                                    sendMessage("Invalid credentials");
                                    break;

                                case "used":
                                    sendMessage(username + " is already logged in");
                                    break;
                            }

                        } catch (ParseException e) {
                            e.printStackTrace();
                        } catch (Exception e){
                            sendMessage("Invalid credentials");//Invalid credentials resulting in error
                        }
                        break;

                    case "logout":
                        if(!loggedin) {
                            sendMessage("You are not logged in");
                            break loop;
                        }
                        sendMessage("> Logging out...");
                        serverOutput(username + " logout.");
                        server.removeClient(username, this);
                        loggedin = false;
                        socket.close();
                        break;

                    case "who":
                        if(!loggedin) {
                            sendMessage("You are not logged in");
                            break;
                        }
                        printUsers();
                        break;

                    case "send all ":
                        if(!loggedin) {
                            sendMessage("You are not logged in");
                            break;
                        }
                        serverMessage = "[" + username + "]: " + message;
                        server.broadcast(serverMessage, this);
                        serverOutput(username + " [to all]: " + message);
                        break;

                    case "send ":
                        if(!loggedin) {
                            sendMessage("You are not logged in");
                            break;
                        }
                        String user = message.substring(0, message.indexOf(" "));//Parse username to send to
                        message = message.replace(user + " ", "");
                        clientMessage = "[" + username + "]: " + message;
                        server.sendTo(clientMessage, user);
                        serverOutput(username + " [to " + user + "]: " + message);
                        break;

                    default:
                        sendMessage("Invalid action");
                        break;
                }

            }

            serverMessage = username + " has left.";
            server.broadcast(serverMessage, this);//Sends message that a user has left


        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    void printUsers() {
        if(server.hasUsers()) {//Prints all users if there are any
            String message = "Connected users: " + server.getUsernames();
            sendMessage(message);
        }
        else {
            sendMessage("No other users connected");
        }
    }

    void sendMessage(String message) {//Send message to ReadThread
        writer.println(message);
    }

    public String getUsername() {
        return username;
    }

    private String getAction(String string) {

        try {
            if (string.substring(0, string.indexOf(" ")).equals("send")) {//If first word of action is send
                string = string.replace("send ", "");
                if (string.substring(0, string.indexOf(" ")).equals("all"))//If second word of action is all
                    return "send all ";
                else
                    return "send ";
            } else
                return string.substring(0, string.indexOf(" "));//Return first word if there is a space
        }
        catch (Exception e) {
            if(string.equals("who") || string.equals("logout"))//return individual words
                return string;
            else return "invalid";
        }
    }

    private String getMessage(String string, String action){
        return string.replace(action, "");//Returns message as message - action
    }

    private void serverOutput(String string){
        System.out.println("> " + string);
    }
}
