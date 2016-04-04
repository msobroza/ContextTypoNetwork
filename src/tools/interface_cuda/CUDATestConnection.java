package tools.interface_cuda;


import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.thrift.TException;
import org.apache.thrift.protocol.TBinaryProtocol;
import org.apache.thrift.protocol.TProtocol;
import org.apache.thrift.transport.TSocket;
import org.apache.thrift.transport.TTransport;
import org.apache.thrift.transport.TTransportException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author msobroza
 */
public class CUDATestConnection {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args){
       TProtocol protocol;
       TTransport transport = new TSocket("10.29.232.217", 9697);
        try {
            transport.open();
            System.out.println("passou");
            protocol = new TBinaryProtocol(transport);
            CUDAContextInterface.Client interfaceGPU = new CUDAContextInterface.Client(protocol);
            interfaceGPU.createContextNetwork(1,"/home/msobroza/git/CBNN_CUDA/CBNN_ContextNetworks/configMots");
            } catch (TTransportException ex) {
            System.out.println("nao passou");
            Logger.getLogger(CUDATestConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (TException ex) {
            Logger.getLogger(CUDATestConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
   
    
}
