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
    public static int[] orientation_list = {PAST_WORDS, FUTURE_WORDS};
    public static int REGION_WORDS_ORIENTATION = 0;
    public static int REGION_WORDS_NETWORK = -1;

    public VirtualWordContextDecoder(VirtualNetwork net) {
        this.net = net;
    }

    // Deprecated: It works only for ngram=1
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
            decodingInputs.add(new DecodingInputWordNetwork(activationWords.get(i), activationLayers.get(i).getH(), orientationList.get(i), mainCliquesLevel.getH()));
        }
        return net.getVirtualInterface().getActivatedWordsNetwork(decodingInputs, mainCliquesLevel.getH());
    }

    public List<String> decodingUnknownWordSentence(List<String> sentence, List<String> regionWordsList) throws TException {
        VirtualLevelCliques rootLayer = net.getMainLevel(1);
        List<DecodingInputWordNetwork> decodingInputs = new ArrayList<>();
        for (String regionWord : regionWordsList) {
            decodingInputs.add(new DecodingInputWordNetwork(regionWord, REGION_WORDS_NETWORK, REGION_WORDS_ORIENTATION, rootLayer.getH()));
        }
        decodingInputs.addAll(generateListOfInputDecoding(sentence));
        return net.getVirtualInterface().getActivatedWordsNetwork(decodingInputs, rootLayer.getH());
    }

    public List<DecodingInputWordNetwork> generateListOfInputDecoding(List<String> sentenceTokenized) {
        List<DecodingInputWordNetwork> result = new ArrayList<>();
        int unknownWordPos = getFirstUnknownWord(sentenceTokenized);
        String nword;
        for (int ngram : net.getListNwords()) {
            for (VirtualLevelTournamentChain doubleLayer : net.getLayersFromMain(ngram)) {
                for (int orientation : orientation_list) {
                    nword = null;
                    //System.out.println("ngram: " + ngram + " unkPos: " + unknownWordPos + " R: " + doubleLayer.anticipationDistance() + " orientation: " + orientation);
                    nword = generateWindowNgram(sentenceTokenized, ngram, unknownWordPos, doubleLayer.anticipationDistance(), orientation);
                    if (nword != null && !nword.isEmpty()) {
                        //System.out.println("Clique net: " + ((VirtualNetwork) net).getIndexMainNetwork(ngram) + " Activating word: " + nword + " in " + doubleLayer.getH() + " with orientation: " + orientation);
                        result.add(new DecodingInputWordNetwork(nword, doubleLayer.getH(), ((VirtualNetwork) net).getIndexMainNetwork(ngram), orientation));
                    }
                }

            }
        }
        return result;
    }

    public static String generateWindowNgram(List<String> sentenceTokenized, int ngram, int unknownWordPos, int r, int orientation) {
        // Verificar
        String result = "";
        int posLimit;
        if (orientation == PAST_WORDS) {
            posLimit = unknownWordPos - ngram - (r - 1);
            if (posLimit >= 0) {
                for (int i = 0; i < ngram; i++) {
                    result += sentenceTokenized.get(i + posLimit) + CONCAT_SYMBOL;
                }
            } else {
                return null;
            }
        } else if (orientation == FUTURE_WORDS) {
            posLimit = unknownWordPos + ngram + (r - 1);
            if (posLimit < sentenceTokenized.size()) {
                for (int i = unknownWordPos + r; i <= posLimit; i++) {
                    result += sentenceTokenized.get(i) + CONCAT_SYMBOL;
                }
            } else {
                return null;
            }
        } else {
            return null;
        }
        if (ngram == 1) {
            result = result.replace(CONCAT_SYMBOL, "");
        }
        return result;
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
