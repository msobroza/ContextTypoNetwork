package control;

import tools.TCPServer;


public class ControlTCPServer {
    
    private TCPServer mServer;
    private NetworkControl cReseaux;
    
    
    public ControlTCPServer(NetworkControl cReseaux){
        this.cReseaux=cReseaux;
        
    }

    public void demarreServeurTCP(){
        //creates the object OnMessageReceived asked by the TCPServer constructor
            mServer = new TCPServer(new TCPServer.OnMessageReceived() {

                @Override
                //this method declared in the interface from TCPServer class is implemented here
                //this method is actually a callback method, because it will run every time when it will be called from
                //TCPServer class (at the while loop)
                public void messageReceived(String message) {
                    //get the Results from DB and sends it back to the Caller (Client)
                    System.out.println("message Received: "+message);
                    String result=cReseaux.decoderMotDemonstrateur(message);
                    System.out.println("message Sent: "+result);
                    mServer.sendMessage(result);
                }
            });
            mServer.start();
    }
}
