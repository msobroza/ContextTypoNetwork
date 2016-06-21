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
    private final HashMap<Integer, Integer> mapNwordsAnticipation;
    private final HashMap<VirtualLevelCliques, List<VirtualLevelTournamentChain>> mapMainLevelDoubleLayers;
    private final List<CONFIG_NET_FILES> configFiles;
    private final static String CONFIG_DIR = "/home/msobroza/git/CBNN_CUDA/CBNN_ContextNetworks";

    enum CONFIG_NET_FILES {

        MAIN_WORDS(CONFIG_DIR + "/configMots"), R1(CONFIG_DIR + "/configSequence"), R2(CONFIG_DIR + "/configSequence"), R3(CONFIG_DIR + "/configSequence"), R4(CONFIG_DIR + "/configSequence"),
        MAIN_BIWORDS(CONFIG_DIR + "/configMots"), R1_BIWORDS(CONFIG_DIR + "/configSequence");

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
        this.mapNwordsAnticipation = new HashMap<>();
        this.configFiles = new ArrayList<>();
        this.levelsList = new LinkedList<>();
        this.TYPE_NETWORK = TypeNetwork.VIRTUAL_NETWORK;
        this.initConfigNetworksFiles();
        for (int nword : mapNwordsAnticipation.keySet()) {
            System.out.println(nword);
            createLevels(nword, mapNwordsAnticipation.get(nword));
        }

    }

    public void createLevels(int nwords, int anticipation) throws TException {
        int mainIdLevel = getIndexMainNetwork(nwords);
        createCliquesLevel(this.configFiles.get(mainIdLevel).toString(), anticipation, nwords);
        System.out.println("mainIdLevel: " + mainIdLevel);
        for (int r = 1; r <= anticipation; r++) {
            createSequenceLevel(this.configFiles.get(mainIdLevel + r).toString(), nwords, r, mainIdLevel);
        }
    }

    private int getIndexMainNetwork(int nword) {
        int index = 0;
        for (int n = 1; n < nword; n++) {
            if (mapNwordsAnticipation.containsKey(n)) {
                index += mapNwordsAnticipation.get(n) + 1;
            }
        }
        return index;
    }

    private void initConfigNetworksFiles() {
        this.mapNwordsAnticipation.put(1, 4);
        this.mapNwordsAnticipation.put(2, 1);
        this.configFiles.add(CONFIG_NET_FILES.MAIN_WORDS);
        this.configFiles.add(CONFIG_NET_FILES.R1);
        this.configFiles.add(CONFIG_NET_FILES.R2);
        this.configFiles.add(CONFIG_NET_FILES.R3);
        this.configFiles.add(CONFIG_NET_FILES.R4);
        this.configFiles.add(CONFIG_NET_FILES.MAIN_BIWORDS);
        this.configFiles.add(CONFIG_NET_FILES.R1_BIWORDS);
    }

    public int getAnticipation(int nword) {
        return this.mapNwordsAnticipation.get(nword);
    }

    public List<Integer> getListNwords() {
        List<Integer> result = new ArrayList<>();
        result.addAll(this.mapNwordsAnticipation.keySet());
        return result;
    }

    public CUDAContextInterface.Client getVirtualInterface() {
        return this.virtualInterface;
    }

    public Level createCliquesLevel(String configFile, int anticipation, int nwords) throws TException {
        this.hCounter++;
        VirtualLevelCliques l = new VirtualLevelCliques(hCounter);
        System.out.println("Virtual level cliques criando... " + hCounter);
        System.out.println("l: " + l.h + " config file: " + configFile);
        this.mapNwordsMainLevel.put(nwords, l);
        this.levelsList.add(l.getH(), l);
        this.virtualInterface.createContextNetwork(l.getH(), configFile);
        return l;
    }

    public Level createSequenceLevel(String configFile, int nwords, int anticipationDistance, int mainIdLevel) throws TException {
        this.hCounter++;
        VirtualLevelCliques mainLayer = (VirtualLevelCliques) this.mapNwordsMainLevel.get(nwords);
        System.out.println("Virtual level tournament criando... " + hCounter);
        System.out.println("Virtual level tournament idMainLevel: " + mapNwordsMainLevel.get(nwords).getH());
        VirtualLevelTournamentChain l = new VirtualLevelTournamentChain(hCounter, mainLayer.getH(), anticipationDistance, nwords);
        System.out.println("l: " + l.h + " config file: " + configFile);
        this.virtualInterface.createContextNetwork(l.h, configFile);
        this.levelsList.add(l.getH(), l);
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

    public List<VirtualLevelTournamentChain> getLayersFromMain(int nwords) {
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

    public boolean learnWordSequences(List<String> sentences) throws TException {
        VirtualLevelCliques mainLevel;
        List<ContextNetwork> contextList = new ArrayList<>();
        System.out.println("learnWordSequences with " + sentences.size() + "sentences");
        for (int nwordKey : mapNwordsMainLevel.keySet()) {
            System.out.println("N Words: " + nwordKey);
            mainLevel = mapNwordsMainLevel.get(nwordKey);
            for (VirtualLevelTournamentChain l : mapMainLevelDoubleLayers.get(mainLevel)) {
                contextList.add(new ContextNetwork(l.getH(), mainLevel.getH(), l.anticipationDistance(), nwordKey));
            }
        }
        for (ContextNetwork n : contextList) {
            System.out.println("idNet: " + n.idNetwork + "idMain: " + n.idMainWordNetwork + "d: " + n.distance + "nWords: " + n.nwords);
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
