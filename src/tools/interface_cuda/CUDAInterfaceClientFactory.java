package tools.interface_cuda;

public class CUDAInterfaceClientFactory extends InterfaceSender {

    private CUDAContextInterface.Client interfaceGPU;

    public CUDAInterfaceClientFactory(String serverAdd, int serverPort) {
        super(serverAdd, serverPort);
        this.interfaceGPU = null;
    }

    public CUDAContextInterface.Client getCUDAContextInterfaceClient() {
        if (interfaceGPU == null) {
            return interfaceGPU = new CUDAContextInterface.Client(protocol);
        } else {
            return interfaceGPU;
        }

    }

}
