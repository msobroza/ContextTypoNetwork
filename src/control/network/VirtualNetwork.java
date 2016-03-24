/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.network;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import model.Clique;
import org.apache.thrift.TException;
import tools.interface_cuda.CUDAContextInterface;
import tools.interface_cuda.ContextNetwork;

/**
 *
 * @author msobroza
 */
public class VirtualNetwork extends Network {

    private final CUDAContextInterface.Client virtualInterface;
    private final HashMap<Integer, VirtualLevelCliques> mapNwordsMainLevel;
    private final HashMap<VirtualLevelCliques, List<VirtualLevelTournamentChain>> mapMainLevelDoubleLayers;
    private final List<CONFIG_NET_FILES> configFiles;

    enum CONFIG_NET_FILES {

        MAIN("/home/msobroza/cliques.config"), R1("/home/msobroza/r1.config"), R2("/home/msobroza/r2.config"), R3("/home/msobroza/r3.config"), R4("/home/msobroza/r4.config");

        private final String serverPath;

        private CONFIG_NET_FILES(String serverPath) {
            this.serverPath = serverPath;
        }

        @Override
        public String toString() {
            return this.serverPath;
        }

    }

    public VirtualNetwork(CUDAContextInterface.Client virtualInterface) {
        this.virtualInterface = virtualInterface;
        this.hCounter = -1;
        this.mapMainLevelDoubleLayers = new HashMap<>();
        this.mapNwordsMainLevel = new HashMap<>();
        this.configFiles = new ArrayList<>();
        this.initConfigNetworksFiles();

    }

    public void createLevels(int nwords) throws TException {
        createCliquesLevel(CONFIG_NET_FILES.MAIN.toString(), nwords);
        int mainIdLevel = mapNwordsMainLevel.get(nwords).getH();
        for (int r = 1; r < configFiles.size(); r++) {
            createSequenceLevel(this.configFiles.get(r).toString(), nwords, r, mainIdLevel);
        }
    }

    private void initConfigNetworksFiles() {
        this.configFiles.add(CONFIG_NET_FILES.MAIN);
        this.configFiles.add(CONFIG_NET_FILES.R1);
        //this.configFiles.add(CONFIG_NET_FILES.R2);
        //this.configFiles.add(CONFIG_NET_FILES.R3);
        //this.configFiles.add(CONFIG_NET_FILES.R4);
    }

    public CUDAContextInterface.Client getVirtualInterface() {
        return this.virtualInterface;
    }

    public Level createCliquesLevel(String configFile, int nwords) throws TException {
        this.hCounter++;
        VirtualLevelCliques l = new VirtualLevelCliques(hCounter);
        this.virtualInterface.createContextNetwork(l.getH(), configFile);
        this.levelsList.add(l.getH(), l);
        this.mapNwordsMainLevel.put(nwords, l);
        return l;
    }

    public Level createSequenceLevel(String configFile, int nwords, int anticipationDistance, int mainIdLevel) throws TException {
        this.hCounter++;
        VirtualLevelTournamentChain l = new VirtualLevelTournamentChain(hCounter, mapNwordsMainLevel.get(nwords).getH(), anticipationDistance);
        this.virtualInterface.createContextNetwork(l.h, configFile);
        this.levelsList.add(l.getH(), l);
        VirtualLevelCliques mainLayer = (VirtualLevelCliques) this.levelsList.get(mainIdLevel);
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

    public List<VirtualLevelTournamentChain> getLayerFromMain(VirtualLevelCliques mainLevel) {
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
        ContextNetwork c = new ContextNetwork();
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
