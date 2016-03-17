
package tools;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;     //For sending messages over the network
import java.net.ServerSocket;   //Socket for Server
import java.net.Socket;         //Socket for Client

public class TCPServer extends Thread{
    
    public static final int SERVERPORT=8081;
    private boolean running=false;
    private PrintWriter mOut;
    
    private OnMessageReceived messageListener;
    
    private String query;
    
    //constructor
    public TCPServer(OnMessageReceived messageListener){
        this.messageListener=messageListener;
    }
    
    public void sendMessage(String message){
        if(mOut!=null && !mOut.checkError()){
            mOut.println(message);
            mOut.flush();
        }
    }
    
    @Override
    public void run(){
        super.run();
        running=true;
        try{
            System.out.println("Server: Connecting....");
            //This is the Socket for the Server
            //it waits until a client contacts the server.
            ServerSocket serverSocket=new ServerSocket(SERVERPORT);
            
            //This is the Socket for the Client
            //the method "accept()" of the Server Socket listens 
            //for a request for connection.
            Socket client =serverSocket.accept();
            System.out.println("Server: Connection accepted");
            
            try{
                //sends message to the client
                mOut=new PrintWriter(new BufferedWriter(new OutputStreamWriter(client.getOutputStream())), true);
                
                //reads meassage received from the client
                BufferedReader in=new BufferedReader(new InputStreamReader(client.getInputStream()));
                        
                //this infinite while loop listens 
                //for incming messages from the Client
                while(running){
                    //incoming message
                    String message=in.readLine();
                    if (message!=null&&messageListener!=null){

                        messageListener.messageReceived(message);
                        
                    }
                }
            }catch(Exception ex){
                ex.printStackTrace();
                System.out.println("Server: Error");
            }finally{
                client.close();
                System.out.println("Server: Client Socket closed.");
            }
        }catch(Exception ex){
            ex.printStackTrace();
            System.out.println("Server: Error");
        }
    }
    
    //Declare the interface. The method messageReceived(String message) 
    //will must be implemented in the ServerBoard
    //class at on startServer button click
    public interface OnMessageReceived {
        public void messageReceived(String message);
    }
}

