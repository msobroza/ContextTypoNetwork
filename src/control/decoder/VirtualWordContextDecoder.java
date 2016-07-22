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
    public static int REGION_WORDS_ANTICIPATION = 0;
    public static int REGION_WORDS_IDSPLIT = -1;

    public VirtualWordContextDecoder(VirtualNetwork net) {
        this.net = net;
    }

    public List<String> decodingUnknownWordSentence(List<String> sentence, List<String> regionWordsList) throws TException {
        VirtualLevelCliques rootLayer = net.getMainLevel(1);
        List<DecodingInputWordNetwork> decodingInputs = new ArrayList<>();
        for (String regionWord : regionWordsList) {
            decodingInputs.add(new DecodingInputWordNetwork(regionWord, REGION_WORDS_NETWORK, REGION_WORDS_ORIENTATION, rootLayer.getH(), REGION_WORDS_ANTICIPATION, REGION_WORDS_IDSPLIT));
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
                    for (int offset = 0; offset < ngram; offset++) {
                        nword = generateWindowNgram(sentenceTokenized, ngram, unknownWordPos, offset, doubleLayer.anticipationDistance(), orientation);
                        if (nword != null && !nword.isEmpty()) {
                            if (ngram > 0) {
                                System.out.println(" DoubleLayer R: " + doubleLayer.anticipationDistance() + " offset: " + offset + " anticipationDistance: " + doubleLayer.anticipationDistance());
                            }
                            int idSplit;
                            if (orientation == PAST_WORDS) {
                                idSplit = offset;
                            } else {
                                idSplit = ngram - offset - 1;
                            }
                            System.out.println("nword: " + nword + " ngram: " + ngram + " unkPos: " + unknownWordPos + " R: " + doubleLayer.anticipationDistance() + " orientation: " + orientation + " idSplit: " + idSplit);
                            result.add(new DecodingInputWordNetwork(nword, doubleLayer.getH(), ((VirtualNetwork) net).getIndexMainNetwork(ngram), orientation, doubleLayer.anticipationDistance(), idSplit));
                        }
                    }
                }

            }
        }
        return result;
    }

    public static String generateWindowNgram(List<String> sentenceTokenized, int ngram, int unknownWordPos, int offset, int r, int orientation) {
        // Verificar
        String result = "";
        int posLimit;
        if (orientation == PAST_WORDS) {
            unknownWordPos -= offset;
            posLimit = unknownWordPos - ngram - (r - 1);
            if (posLimit >= 0) {
                for (int i = 0; i < ngram; i++) {
                    result += sentenceTokenized.get(i + posLimit) + CONCAT_SYMBOL;
                }
            } else {
                return null;
            }
        } else if (orientation == FUTURE_WORDS) {
            unknownWordPos += offset;
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
