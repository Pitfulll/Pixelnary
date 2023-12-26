/* Natalie, Trinity, Yan 
 * CSCI 2113, The George Washington University 
 * 
 * This file handels the client side of the server. 
 * A thread is run to update the viewer every second with an 
 * image of the drawing being made by the drawing client 
 * 
*/



import java.io.*;
import java.net.*;
import javax.swing.*;
import java.util.ArrayList;

public class drawingClient {
    //Natalie 11/27
    Socket sock;
    //drawingGUI gui;
    boolean isConnected; 
    String IP = "localhost"; //TO DO: Fill value once server is done 
    int port = 7634; //TO DO: Fill value once server is done
    drawingGUI gui;
    ObjectOutputStream toServe;
    
    public drawingClient(drawingGUI gui){
        this.gui = gui;
        
        try{
            InetAddress localhost = InetAddress.getLocalHost();
            System.out.println("System IP Address : " +
            (localhost.getHostAddress()).trim());

            sock = new Socket(IP,port);
            toServe = new ObjectOutputStream(sock.getOutputStream());
        }catch(Exception e){
            System.err.println("Cannot Connect");
            System.exit(1);
        }
        gui.loading = false;
        isConnected = true;
        gui.clearScreen();
        
        Thread t = new updateThread();
        t.start();
    }

    //getter for connection status 
    //Natalie 11/28
    public boolean isConnected(){
        return isConnected;
    }

    //add to output stream
    //Natalie 
    public void writeToServer(int color, int x, int y){
         
        try{
            int[] pack = {color, x, y};
            //System.out.println("Made it, cords: {" + Integer.toString(x) + ", " + Integer.toString(y) +"}");
            toServe.writeInt(0);
            toServe.writeObject(pack);
            toServe.flush();

        } catch(Exception e){

        }
        
    }

    public void clear(){
         
        try{
            boolean[] bool = {true};
            toServe.writeInt(1);
            toServe.writeObject(bool);
            toServe.flush();

        } catch(Exception e){

        }
    }

    public void writeMessage(String[] mes){
        try{
            toServe.writeInt(2);
            toServe.writeObject(mes);
            String msg[] = {mes[0].split("] ")[1]};
            if(msg[0].equals(gui.currentWord))
            {
                System.out.println("Currently: " + gui.currentWord);
                gui.messageField.setEditable(false);
            }
            toServe.flush();
        }catch(IOException e)
        {

        }
    }

    //TO DO: Finish this up once method are written in drawingArea and drawingGUI
    //main thread for managing updates 
    //Natalie 11/28
    //client buttons
    private class updateThread extends Thread{
        
        public boolean firstTime;
        public void run(){
        ObjectInputStream in;
            
            try {
                in = new ObjectInputStream(sock.getInputStream());
                //System.out.println("Made it, boolean: " + String.valueOf(((boolean[])in.readObject())[0]));
                boolean[] bool = (boolean[])in.readObject();
                gui.isViewerMode = !bool[0];
                if(gui.isViewerMode != true)
                {
                    //gui.messageField.setEditable(false);
                }
                System.out.println(String.valueOf(bool[0]));
                if(bool[0] == false)
                {
                    String[] arr;
                    arr = (String[])in.readObject();
                    int count = in.readInt();
                    updateArea(arr, count);
                }
                String[] keyword = (String[])in.readObject();
                System.out.println("Keyword: " + keyword[0]);
                gui.currentWord = keyword[0];
                if(bool[0] == false)
                {
                    gui.labelUpdate(0);
                }
                else if (bool[0] == true)
                {
                    gui.labelUpdate(1);
                }
                
                while(isConnected()){
                    try{
                        int type = in.readInt();
                        if(type == 0)
                        {
                            int[] pack = (int[]) in.readObject();                   
                            //only update if in viewer mode
                            if(gui.isViewerMode == true){
                                gui.updateButtons(pack[0],pack[1],pack[2]);
                                
                            }
                        }
                        else if(type == 1)
                        {
                            boolean[] nbool = (boolean[])in.readObject();
                            gui.isViewerMode = !nbool[0];
                        }
                        else if(type == 2)
                        {
                            System.out.println("Made it to clear");
                            //boolean[] test = (boolean[])in.readObject();
                            gui.clearScreen();
                        }
                        else if(type == 3)
                        {
                            String[] msg = (String[]) in.readObject(); 
                            gui.sendMessage(msg[0]);
                        }
                    }catch(ClassNotFoundException e){
                        System.out.println("class not found");
                    }
                    
                }
            
            }catch (IOException e){
                System.out.println("interrupted");
            }
            
            catch(ClassNotFoundException i)
            {
                
            }
            
        }
        public void updateArea(String[] arr, int count)
        {
            for(int i = 0; i < count; i++)
            {
                String[] val = arr[i].split(",");
                gui.updateButtons(Integer.parseInt(val[0]), Integer.parseInt(val[1]), Integer.parseInt(val[2]));
            }
        }
    }

    public void close(){
        try {
            sock.close();
            isConnected = false;
            gui.isConnected = false;
            gui.labelUpdate(2);
            gui.clearScreen();
            gui.isViewerMode = false;
            System.out.println("Disconnected");
        } catch (Exception e) {
          
            System.out.println("did not close");
        }
    }

}