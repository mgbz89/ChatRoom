package server;


import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashSet;
import java.util.Set;

public class ChatServer {

    private int port;
    private Set<String> usernames = new HashSet<>();
    private Set<ClientThread> clientThreads = new HashSet<>();

    public ChatServer(int port) {
        this.port = port;
    }

    public void execute() {
        //Opens server
        try(ServerSocket serverSocket = new ServerSocket(port)) {

            System.out.println("Chat server is listening on port " + port);

            while(true) {
                //Accepts clients if there are less than 3
                if (clientThreads.size() < 3){
                    Socket socket = serverSocket.accept();
                    System.out.println("Connection made");
                    ClientThread newUser = new ClientThread(socket, this);
                    clientThreads.add(newUser);
                    newUser.start();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args){
        ChatServer server = new ChatServer(15201);
        server.execute();
    }

    void removeClient(String username, ClientThread client) {
        boolean removed = usernames.remove(username);
        if(removed) {
            clientThreads.remove(client);
            System.out.println("The user " + username + " hes left.");
        }
    }

    Set<String> getUsernames() {
        return this.usernames;
    }

    boolean hasUsers() {
        return !this.usernames.isEmpty();
    }

    //Sends message to all client threads
    void broadcast(String message, ClientThread exclude){
        for(ClientThread client : clientThreads){
            if(client != exclude) {
                client.sendMessage(message);
            }
        }
    }

    //Sends message to individual client thread
    void sendTo(String message, String to){
        for(ClientThread client : clientThreads){
            if(client.getUsername().equals(to)){
                client.sendMessage(message);
            }
        }
    }

    //Confirms user is valid using json
    String confirmUser(String username, String password) throws IOException, ParseException {
        Object obj =  new JSONParser().parse(new FileReader("/Users/MatthewBarber/IdeaProjects/ChatServer/src/server/users.json"));
        JSONArray users = (JSONArray) obj;
        for (int i = 0; i < users.size(); i++) {
            JSONObject jsonobject = (JSONObject) users.get(i);
            if (username.equals(jsonobject.get("username")) && password.equals(jsonobject.get("password"))) {

                if(usernames.contains(username))
                    return "used";//if this user is already logged on
                if(usernames.size() == 3)
                    return "toomany";//if there are too many users logged on
                usernames.add(username);
                return "loggedin";//if the user is valid
            }
        }
        return "wrong";//if credentials are wrong
    }
}
