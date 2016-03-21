/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.interface_cuda;

import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/**
 *
 * @author msobroza
 */
public class InterfaceSender {
    
    protected final int serverPort;
    protected final String serverAdd;
    protected TTransport transport;
    protected TProtocol protocol;
    
    public InterfaceSender(String serverAdd, int serverPort){
        this.serverPort=serverPort;
        this.serverAdd=serverAdd;
        this.protocol= null;
        this.transport=null;
    } 
    
    public void openConnection(){
            this.transport = new TSocket(this.serverAdd, this.serverPort);
        try {
            this.transport.open();
        } catch (TTransportException ex) {
            Logger.getLogger(InterfaceSender.class.getName()).log(Level.SEVERE, null, ex);
        }
            this.protocol = new TBinaryProtocol(this.transport);
    }
    
    public void closeConnection(){
        this.transport.close();
        this.protocol= null;
        this.transport=null;
    }
}
