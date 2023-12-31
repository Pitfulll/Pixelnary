
import java.io.*;
import java.net.*;
import java.util.ArrayList;
import java.util.concurrent.*;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.net.InetAddress;

/* TODO:
 * Add turn progression
 * Indicate when the round is over/starts 
 * Drawer wins when atleast half of the people guess correctly
 * something that shows how many people guess correctly
 */

public class drawingServer {

    String currentWord; 

    //word list from https://www.classywish.com/pictionary-words/
    String[] gameWords = {"Swing", "Coat", "Shoe", "Ocean", "Dog", "Mouth", "Milk", "Duck", "Skateboard", "Bird", "Mouse", "Whale", "Jacket", "Shirt", "Hippo", "Beach", "Egg", "Cookie", "Cheese", "Skip", "Drum", "homework", "glue", "eraser", "peace", "panic", "alarm", "far", "comfy", "dripping", "boring", "hot", "cold", "parents", "closet", "laugh", "falling", "sleepover", "calendar", "sunscreen", "panda", "detention", "hair", "ice skating", "afraid", "dictionary", "homerun", "root beer float", "hibernation", "street sweeper", "spitball", "drinking fountain", "imagination", "Angry", "Fireworks", "Pumpkin", "Baby", "Flower", "Rainbow", "Beard", "Flying saucer", "Recycle", "Bible", "Giraffe", "Sand castle", "Bikini", "Glasses", "Snowflake", "Book", "High heel", "Stairs", "Bucket", "Ice cream cone", "Starfish", "Bumble bee", "Igloo", "Strawberry", "Butterfly", "Lady bug", "Sun", "Camera", "Lamp", "Tire", "Cat", "Lion", "Toast", "Church", "Mailbox", "Toothbrush", "Crayon", "Night", "Toothpaste", "Dolphin", "Nose", "Truck", "Egg", "Olympics", "Volleyball", "Eiffel Tower", "Peanut", "half cardboard", "oar", "baby-sitter", "drip", "shampoo", "point", "time machine", "yardstick", "think", "lace darts", "world", "avocado bleach", "shower", "curtain", "extension cord dent", "birthday lap", "sandbox", "bruise", "quicksand", "fog", "gasoline", "pocket", "honk", "sponge", "rim", "bride", "wig", "zipper", "wag", "letter opener", "fiddle", "water buffalo", "pilot", "brand pail", "baguette", "rib mascot", "fireman", "pole zoo sushi", "fizz ceiling", "fan bald", "banister punk", "post office", "season", "Internet", "chess", "puppet", "chime", "ivy"};
    

    private ServerSocket serverSocket;
    int count = 0;
    private boolean isDrawer[] = {true};
    public drawingServer() {
        try {
            serverSocket = new ServerSocket(7634); 
            pickWord();
            System.out.println(currentWord);
        } catch (IOException e) {
            System.err.println("Could not listen on port");
            System.exit(1);
        }
    }

    public void execute() {
        try {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                count++;
                System.out.println("Number of clients: " + Integer.toString(count) + " Drawing Bool: " + String.valueOf(isDrawer[0]));
                Thread thread = new Thread(new ClientHandler(clientSocket, isDrawer, this));
                thread.start();
                isDrawer[0] = false;
            }
        } catch (IOException e) {
            
        }
    }

    public void closeServer()
    {
        try{
            if(serverSocket != null)
            {
                serverSocket.close();
            }
        }
        catch (IOException e)
        {

        }
    }

    //selects a random word from the list 
    public void pickWord(){
        Random random = new Random();
        int randomIdx = random.nextInt(gameWords.length);
        if (currentWord != null && currentWord.equals(gameWords[randomIdx])){
            pickWord();
        }
        currentWord = gameWords[randomIdx];
    }
    public static void main(String[] args) {
        drawingServer server = new drawingServer();
        server.execute();
    }

    private static class ClientHandler implements Runnable {
        
        private Socket clientSocket;
        public static ArrayList<ClientHandler> clients = new ArrayList<ClientHandler>();
        private ObjectOutputStream out;
        private ObjectInputStream in;
        public boolean isDrawer[] = {true};
        public static String[] data = new String[4096];
        public static int count = 0;
        public int[] coords;
        String[] msgToSend;
        drawingServer server;
        String[] keyword = new String[1];
 
        public ClientHandler(Socket socket, boolean[] isDrawer, drawingServer server) {
            this.clientSocket = socket;
            this.isDrawer[0] = isDrawer[0];
            this.server = server;
            keyword[0] = server.currentWord;
            clients.add(this);
            
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                in = new ObjectInputStream(clientSocket.getInputStream());

                this.out.writeObject(isDrawer);
                this.out.flush();
                
                if(isDrawer[0] == false)
                {
                    this.out.writeObject(data);
                    this.out.writeInt(count);
                    this.out.flush();
                }
                this.out.writeObject(keyword);
                this.out.flush();
                System.out.println("Keyword sent: " + keyword[0]);
                
            }catch(IOException e){
                closeAll(in, out, clientSocket);
            }
            
            
        }
        
        public void run() {
            
            while (clientSocket.isConnected()) {
                try {
                //Should only pass this when drawing
                int type = in.readInt();
                if(type == 0) // Drawing area data
                {
                    int[] pack = (int[])in.readObject();
                    String vals = Integer.toString(pack[0]) + "," + Integer.toString(pack[1]) + "," + Integer.toString(pack[2]);
                    data[count] = vals;
                    count++;
                    //message from a drawingClient is being sent to other clients
                    for(ClientHandler clientHandler: clients)
                    {
                        try{
                            if(clientHandler != this)
                            {
                                clientHandler.out.writeInt(0);
                                clientHandler.out.writeObject(pack);
                                clientHandler.out.flush();
                            }
                        }catch(IOException e){
                            closeAll(in, out, clientSocket);
                        }
                    }
                } 
                else if(type == 1) // Clear stuff (not done)
                {
                    boolean[] bool = (boolean[])in.readObject();
                    clearData();
                    for(ClientHandler clientHandler: clients)
                    {
                        try{
                            if(clientHandler != this)
                            {
                                System.out.println("Clear");
                                clientHandler.out.writeInt(2);
                                clientHandler.out.writeObject(bool);
                                clientHandler.out.flush();
                            }
                        }catch(IOException e){
                            closeAll(in, out, clientSocket);
                        }
                    }

                } 
                else if(type == 2) // Chat message
                {
                    try{
                        msgToSend = (String[])in.readObject();
                        String msg[] = {msgToSend[0].split("] ")[1]};
                        if(!msg[0].equals(keyword[0]))
                        {
                            for(ClientHandler clientHandler: clients)
                            {
                                if(clientHandler != this)
                                {
                                    System.out.println("Message Sent");
                                    clientHandler.out.writeInt(3);
                                    clientHandler.out.writeObject(msgToSend);
                                    clientHandler.out.flush();
                                }
                            }
                        }
                        else
                        {
                            msgToSend[0] = "[SERVER] YOU WIN! WORD WAS: " + keyword[0];
                            this.out.writeInt(3);
                            this.out.writeObject(msgToSend);
                            this.out.flush();
                            msgToSend[0] =  "[SERVER] SOMEONE HAS GUESSED THE WORD";
                            for(ClientHandler clientHandler: clients)
                            {
                                if(clientHandler != this)
                                {
                                    System.out.println("Message Sent");
                                    clientHandler.out.writeInt(3);
                                    clientHandler.out.writeObject(msgToSend);
                                    clientHandler.out.flush();
                                }
                            }
                        }

                    }catch(Exception e){
                        closeAll(in, out, clientSocket);
                    }
                }
                } catch (IOException e) {
                    closeAll(in, out, clientSocket);
                    break;
                }
                catch (ClassNotFoundException i)
                {
                    closeAll(in, out, clientSocket);
                    break;
                }
                
            }
            
        }

        public void newRound()
        {
            Timer timer = new Timer();
            TimerTask task = new TimerTask()
            {
                public void run()
                {
                    //Announce that round is over
                    //Switch Drawer and keyword

                }
            };
        }
        public void clearClients()
        {
            try{
                clearData();
                for(ClientHandler clientHandler: clients)
                {
                    System.out.println("Clear");
                    clientHandler.out.writeInt(2);
                    clientHandler.out.flush();
                }
            }catch(IOException e){
                closeAll(in, out, clientSocket);
            }
        }

        public void clearClients(ClientHandler client)
        {
            clearData();
            try{
                for(ClientHandler clientHandler: clients)
                {
                    
                    if(clientHandler != client)
                    {
                        System.out.println("Clear");
                        clientHandler.out.writeInt(2); 
                        clientHandler.out.flush();
                    }
                    
                }
            }catch(IOException e){
                closeAll(in, out, clientSocket);
            }
        }

        public void clearData()
        {
            for(int i = 0; i < count; i++)
            {
                data[i] = "";
            }
            count = 0;
        }

        public void changeDrawer(boolean stillHere)
        {
            try {
                out = new ObjectOutputStream(clientSocket.getOutputStream());
                boolean[] drawChange = {false};
                if(stillHere)
                {
                    this.out.writeInt(1);
                    this.out.writeObject(drawChange);
                    this.out.flush();
                    this.isDrawer[0] = false;
                    clearClients();
                }
                else
                {
                   clearClients(this);
                }
                drawChange[0] = true;
                System.out.println("Inside Change draw");
                
                for(int i = 0; i < clients.size(); i++)
                {
                    System.out.println(Boolean.toString(clients.get(i).isDrawer[0]) + " " + Integer.toString(i) + Integer.toString(clients.size()));
                    if(clients.get(i) == this && clients.get(i+1) != null)
                    {
                        clients.get(i+1).out.writeInt(1);
                        clients.get(i+1).out.writeObject(drawChange);
                        clients.get(i+1).out.flush();
                        clients.get(i+1).isDrawer[0] = true;
                        System.out.println("Made it " + Boolean.toString(clients.get(i+1).isDrawer[0]));
                        break;
                    }
                    else if(clients.get(i) == this && clients.get(i+1) == null)
                    {
                        System.out.println("Shouldn't be in here");
                        clients.get(0).out.writeInt(1);
                        clients.get(0).out.writeObject(drawChange);
                        clients.get(0).out.flush();
                        break;
                    }
                }
            
                
            }catch(IOException e){
                System.out.println("Error in change draw");
            }
        }

        public void removeClient()
        {
  
            if(clients.size() > 1 && isDrawer[0] == true)
            {
                System.out.println("Removed Drawer");
                changeDrawer(false);
                clients.remove(this);
                clearData();
                System.out.println("Past remove Client");
            }
            else{
                System.out.println("Removed Player " + Integer.toString(clients.size()) + " " + Boolean.toString(isDrawer[0]));
                clients.remove(this);
                server.isDrawer[0] = true;
                clearData();
            }
        }

        public void closeAll(ObjectInputStream in, ObjectOutputStream out, Socket sock)
        {
            System.out.println("close: " + Boolean.toString(isDrawer[0]));
            removeClient();
            try
            {
                if(in != null)
                {
                    in.close();
                }
                if(out != null)
                {
                    out.close();
                }
                if(sock != null)
                {
                    sock.close();
                }
            }
            catch(IOException e)
            {

            }
        }  
        
    }
}