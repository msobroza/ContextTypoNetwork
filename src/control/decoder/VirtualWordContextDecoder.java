/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package control.decoder;

import control.network.VirtualLevelCliques;
import control.network.VirtualLevelTournamentChain;
import control.network.VirtualNetwork;
import control.rules.LetterInformation;
import java.util.ArrayList;
import java.util.List;
import org.apache.thrift.TException;
import tools.interface_cuda.DecodingInputWordNetwork;

/**
 *
 * @author msobroza
 */
public class VirtualWordContextDecoder extends Decoder implements LetterInformation {

    private final VirtualNetwork net;
    public static int FUTURE_WORDS = -1;
    public static int PAST_WORDS = 1;
    public static int REGION_WORDS_ORIENTATION = 0;
    public static int REGION_WORDS_NETWORK = -1;

    public VirtualWordContextDecoder(VirtualNetwork net) {
        this.net = net;
    }

    public List<String> decodingUnknownWordSentence(List<String> sentence) throws TException {
        int unknownWordPos = getFirstUnknownWord(sentence);
        List<Integer> relativePositions = new ArrayList<>();
        if (unknownWordPos != -1) {
            relativePositions.addAll(getRelativePositionList(sentence, unknownWordPos));
        }
        VirtualLevelCliques mainCliquesLevel = net.getMainLevel(1);
        List<VirtualLevelTournamentChain> doubleLayers = net.getLayersFromMain(mainCliquesLevel);
        List<VirtualLevelTournamentChain> activationLayers = new ArrayList<>();
        List<Integer> orientationList = new ArrayList<>();
        List<String> activationWords = new ArrayList<>();
        int r;
        for (int i = 0; i < sentence.size(); i++) {
            r = Math.abs(relativePositions.get(i));
            if (r != 0 && r <= mainCliquesLevel.getMaxAnticipationDistance()) {
                for (VirtualLevelTournamentChain sequenceLayer : doubleLayers) {
                    if (sequenceLayer.anticipationDistance() == r) {
                        activationLayers.add(sequenceLayer);
                        if (relativePositions.get(i) > 0) {
                            orientationList.add(PAST_WORDS);
                        } else {
                            orientationList.add(FUTURE_WORDS);
                        }
                        activationWords.add(sentence.get(i));
                    }
                }
            }
        }
        List<DecodingInputWordNetwork> decodingInputs = new ArrayList<>();
        for (int i = 0; i < activationLayers.size(); i++) {
            decodingInputs.add(new DecodingInputWordNetwork(activationWords.get(i), activationLayers.get(i).getH(), orientationList.get(i)));
        }
        return net.getVirtualInterface().getActivatedWordsNetwork(decodingInputs, mainCliquesLevel.getH());
    }

    public List<String> decodingUnknownWordSentence(List<String> sentence, List<String> regionWordsList) throws TException {
        int unknownWordPos = getFirstUnknownWord(sentence);
        List<Integer> relativePositions = new ArrayList<>();
        if (unknownWordPos != -1) {
            relativePositions.addAll(getRelativePositionList(sentence, unknownWordPos));
        }
        VirtualLevelCliques mainCliquesLevel = net.getMainLevel(1);
        List<VirtualLevelTournamentChain> doubleLayers = net.getLayersFromMain(mainCliquesLevel);
        List<VirtualLevelTournamentChain> activationLayers = new ArrayList<>();
        List<Integer> orientationList = new ArrayList<>();
        List<String> activationWords = new ArrayList<>();
        int r;
        for (int i = 0; i < sentence.size(); i++) {
            r = Math.abs(relativePositions.get(i));
            if (r != 0 && r <= mainCliquesLevel.getMaxAnticipationDistance()) {
                for (VirtualLevelTournamentChain sequenceLayer : doubleLayers) {
                    if (sequenceLayer.anticipationDistance() == r) {
                        activationLayers.add(sequenceLayer);
                        if (relativePositions.get(i) > 0) {
                            orientationList.add(PAST_WORDS);
                        } else {
                            orientationList.add(FUTURE_WORDS);
                        }
                        activationWords.add(sentence.get(i));
                    }
                }
            }
        }
        List<DecodingInputWordNetwork> decodingInputs = new ArrayList<>();
        for (String regionWord : regionWordsList) {
            decodingInputs.add(new DecodingInputWordNetwork(regionWord, REGION_WORDS_NETWORK, REGION_WORDS_ORIENTATION));
        }
        for (int i = 0; i < activationLayers.size(); i++) {
            decodingInputs.add(new DecodingInputWordNetwork(activationWords.get(i), activationLayers.get(i).getH(), orientationList.get(i)));
        }
        return net.getVirtualInterface().getActivatedWordsNetwork(decodingInputs, mainCliquesLevel.getH());
    }

    public static List<Integer> getRelativePositionList(List<String> sentence, int position) {
        List<Integer> result = new ArrayList<>();
        for (int i = 0; i < sentence.size(); i++) {
            result.add(position - i);
        }
        return result;
    }

    public static int getFirstUnknownWord(List<String> wordsSequence) {
        for (int pos = 0; pos < wordsSequence.size(); pos++) {
            if (wordsSequence.get(pos).contains(UNKNOWN_WORD_SYMBOLS)) {
                return pos;
            }
        }
        return -1;
    }

}
