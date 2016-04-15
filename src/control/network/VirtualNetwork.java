/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.network;

import control.NetworkControl;
import control.NetworkControl.TypeNetwork;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import model.Clique;
import org.apache.thrift.TException;
import tools.interface_cuda.CUDAContextInterface;
import tools.interface_cuda.ContextNetwork;

/**
 *
 * @author msobroza
 */
public final class VirtualNetwork extends Network {

    private final CUDAContextInterface.Client virtualInterface;
    private final HashMap<Integer, VirtualLevelCliques> mapNwordsMainLevel;
    private final HashMap<VirtualLevelCliques, List<VirtualLevelTournamentChain>> mapMainLevelDoubleLayers;
    private final List<CONFIG_NET_FILES> configFiles;
    private final static String CONFIG_DIR="/home/msobroza/git/CBNN_CUDA/CBNN_ContextNetworks";
    private final static int [] N_WORDS_VECTOR ={1};
    
    enum CONFIG_NET_FILES {

        MAIN(CONFIG_DIR+"/configMots"), R1(CONFIG_DIR+"/configSequence"), R2(CONFIG_DIR+"/configSequence"), R3(CONFIG_DIR+"/configSequence"), R4(CONFIG_DIR+"/configSequence");

        private final String serverPath;

        private CONFIG_NET_FILES(String serverPath) {
            this.serverPath = serverPath;
        }

        @Override
        public String toString() {
            return this.serverPath;
        }

    }

    public VirtualNetwork(CUDAContextInterface.Client virtualInterface) throws TException {
        this.virtualInterface = virtualInterface;
        this.hCounter = -1;
        this.mapMainLevelDoubleLayers = new HashMap<>();
        this.mapNwordsMainLevel = new HashMap<>();
        this.configFiles = new ArrayList<>();
        this.levelsList= new LinkedList<>();
        this.TYPE_NETWORK=TypeNetwork.VIRTUAL_NETWORK;
        this.initConfigNetworksFiles();
        for(int nword:N_WORDS_VECTOR){
            createLevels(nword);
        }

    }

    public void createLevels(int nwords) throws TException {
        createCliquesLevel(CONFIG_NET_FILES.MAIN.toString(), nwords);
        int mainIdLevel = mapNwordsMainLevel.get(nwords).getH();
        System.out.println("mainIdLevel: "+mainIdLevel);
        for (int r = 1; r < configFiles.size(); r++) {
            createSequenceLevel(this.configFiles.get(r).toString(), nwords, r, mainIdLevel);
        }
    }

    private void initConfigNetworksFiles() {
        this.configFiles.add(CONFIG_NET_FILES.MAIN);
        this.configFiles.add(CONFIG_NET_FILES.R1);
        this.configFiles.add(CONFIG_NET_FILES.R2);
        this.configFiles.add(CONFIG_NET_FILES.R3);
        this.configFiles.add(CONFIG_NET_FILES.R4);
    }

    public CUDAContextInterface.Client getVirtualInterface() {
        return this.virtualInterface;
    }

    public Level createCliquesLevel(String configFile, int nwords) throws TException {
        this.hCounter++;
        VirtualLevelCliques l = new VirtualLevelCliques(hCounter);
         System.out.println("Virtual level cliques criando... "+hCounter);
         System.out.println("l: "+l.h+" config file: "+configFile);
        this.levelsList.add(l.getH(), l);
        this.virtualInterface.createContextNetwork(l.getH(), configFile);
        this.mapNwordsMainLevel.put(nwords, l);
        return l;
    }

    public Level createSequenceLevel(String configFile, int nwords, int anticipationDistance, int mainIdLevel) throws TException {
        this.hCounter++;
        System.out.println("Virtual level tournament criando... "+hCounter);
        VirtualLevelTournamentChain l = new VirtualLevelTournamentChain(hCounter, mapNwordsMainLevel.get(nwords).getH(), anticipationDistance);
        System.out.println("l: "+l.h+" config file: "+configFile);
        this.virtualInterface.createContextNetwork(l.h, configFile);
        this.levelsList.add(l.getH(), l);
        VirtualLevelCliques mainLayer = (VirtualLevelCliques) this.mapNwordsMainLevel.get(nwords);
        List<VirtualLevelTournamentChain> doubleLayers;
        if (mapMainLevelDoubleLayers.containsKey(mainLayer)) {
            doubleLayers = mapMainLevelDoubleLayers.get(mainLayer);
        } else {
            doubleLayers = new ArrayList<>();
        }
        doubleLayers.add(l);
        mainLayer.increaseMaxAnticipationDistance();
        mapMainLevelDoubleLayers.put(mainLayer, doubleLayers);
        return l;
    }

    public List<VirtualLevelTournamentChain> getLayersFromMain(VirtualLevelCliques mainLevel) {
        return this.mapMainLevelDoubleLayers.get(mainLevel);
    }

    public List<VirtualLevelTournamentChain> getLayerFromMain(int nwords) {
        return this.mapMainLevelDoubleLayers.get(this.getMainLevel(nwords));
    }

    public VirtualLevelCliques getMainLevel(int nwords) {
        return this.mapNwordsMainLevel.get(nwords);
    }

    public boolean destroyAllVirtualLevels() throws TException {
        for (int i = 0; i <= hCounter; i++) {
            if (this.virtualInterface.destroyContextNetwork(i) != 0) {
                return false;
            } else {
                this.levelsList.remove(i);
            }
        }

        return true;
    }

    public boolean learnWordSequences(int nwords, List<String> sentences) throws TException {
        VirtualLevelCliques mainLevel = mapNwordsMainLevel.get(nwords);
        List<VirtualLevelTournamentChain> sequencesLevelsList = new ArrayList<>(mapMainLevelDoubleLayers.get(mainLevel));
        List<ContextNetwork> contextList = new ArrayList<>();
        for (VirtualLevelTournamentChain l : sequencesLevelsList) {
            
            contextList.add(new ContextNetwork(l.getH(), mainLevel.getH(), l.anticipationDistance()));
        }
        for(ContextNetwork n:contextList){
            System.out.println("idNet: "+n.idNetwork+"idMain: "+n.idMainWordNetwork+"d: "+n.distance);
        }
        return this.virtualInterface.learnCompleteSequences(contextList, sentences) == 0;
    }

    @Override
    public Clique learnWord(String word) {
        return null;
    }

    @Override
    public Clique learnPhoneme(String phon) {
        return null;
    }
}
