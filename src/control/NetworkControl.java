package control;

import control.decoder.VirtualWordContextDecoder;
import control.rules.LetterInformation;
import control.rules.PhonemeRules;
import control.network.InterfaceNetwork;
import control.network.Network;
import control.network.FuzzyNetwork;
import control.network.TriangularNetwork;
import control.network.VirtualNetwork;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import model.Fanal;
import org.apache.thrift.TException;
import tools.interface_cuda.CUDAContextInterface;
import tools.tokenizer.FrenchTokenizer;

public class NetworkControl implements LetterInformation {

    public enum IndexNetwork {

        LOCAL_TRIANGULAR_NETWORK_INDEX(0), LOCAL_FUZZY_NETWORK_INDEX(1), VIRTUAL_CLIQUES_NETWORK(2);
        int value;

        private IndexNetwork(int value) {
            this.value = value;
        }

        public int getIndex() {
            return this.value;
        }
    }

    public enum TypeNetwork {

        TRIANGULAR_NETWORK(0), FUZZY_NETWORK(1), VIRTUAL_NETWORK(2), RANDOM_CLIQUES_NETWORK(3), RANDOM_SEQUENCES_NETWORK(4);
        int value;

        private TypeNetwork(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }
    }

    // Il cree un reseau interface (triangulaire et flous)
    public static final boolean ACTIVATE_INTERFACE_NETWORK = true;
    // Il active l'apprentissage des connexions laterales
    public static final boolean ACTIVATE_LATERAL_CONNECTIONS = true;

    private final LinkedList<Network> multimodalNetworks;
    private final LinkedList<String> storedWords;
    private final LinkedList<String> storedSentences;
    private final LinkedList<String> learntWords;
    private final HashMap<String, String> wordsPhonemesMap;
    private double errorRate;
    private double matchingRate;
    private final InterfaceNetwork interfaceNetworkInstance;
    private ArrayList<Double> matchingRatePerNetwork;
    private ArrayList<Double> errorRatePerNetwork;
    private CUDAContextInterface.Client virtualInterface;
    private final int NUMBER_CLUSTERS_FUZZY_NETWORK = 15;

    public NetworkControl() {
        multimodalNetworks = new LinkedList<>();
        storedWords = new LinkedList<>();
        storedSentences = new LinkedList<>();
        learntWords = new LinkedList<>();
        wordsPhonemesMap = new HashMap<>();
        errorRate = 0.0;
        matchingRate = 0.0;
        // On instancie un reseau du type triangulaire (int l, int X, int c, int hMax, NIVEAU_SUPPLEMENTAIRE)
        multimodalNetworks.add(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), new TriangularNetwork(200, 100, 8, 14, true, false));

        // On instancie un reseau du type flous de mots
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            multimodalNetworks.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), new FuzzyNetwork(NUMBER_CLUSTERS_FUZZY_NETWORK, InterfaceNetwork.InformationContent.WORDS));
        } else {
            multimodalNetworks.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), new FuzzyNetwork(ContextTypoNetwork.numberLetters + 2, InterfaceNetwork.InformationContent.WORDS));
        }

        if (ACTIVATE_INTERFACE_NETWORK) {
            interfaceNetworkInstance = new InterfaceNetwork(this);

            interfaceNetworkInstance.addNetwork(multimodalNetworks.get(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex()), IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), InterfaceNetwork.InformationContent.PHONEMES);

            interfaceNetworkInstance.addNetwork(multimodalNetworks.get(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()), IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), InterfaceNetwork.InformationContent.WORDS);
            if (ContextTypoNetwork.RATES_PER_NETWORK) {
                matchingRatePerNetwork = new ArrayList<>();
                errorRatePerNetwork = new ArrayList<>();
            }
        }
    }

    // This constructor is used for sentences
    public NetworkControl(CUDAContextInterface.Client virtualInterface) throws TException {

        this.virtualInterface = virtualInterface;
        multimodalNetworks = new LinkedList<>();
        storedWords = new LinkedList<>();
        storedSentences = new LinkedList<>();
        learntWords = new LinkedList<>();
        wordsPhonemesMap = new HashMap<>();
        errorRate = 0.0;
        matchingRate = 0.0;
        // On instancie un reseau du type triangulaire (int l, int X, int c, int hMax, NIVEAU_SUPPLEMENTAIRE)
        multimodalNetworks.add(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), new TriangularNetwork(200, 100, 8, 14, true, false));

        // On instancie un reseau du type flous de mots
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            multimodalNetworks.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), new FuzzyNetwork(NUMBER_CLUSTERS_FUZZY_NETWORK, InterfaceNetwork.InformationContent.WORDS));
        } else {
            multimodalNetworks.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), new FuzzyNetwork(ContextTypoNetwork.numberLetters + 2, InterfaceNetwork.InformationContent.WORDS));
        }

        if (ContextTypoNetwork.USE_CONTEXT_INFORMATION) {
            multimodalNetworks.add(IndexNetwork.VIRTUAL_CLIQUES_NETWORK.getIndex(), new VirtualNetwork(this.virtualInterface));
        }

        if (ACTIVATE_INTERFACE_NETWORK) {
            interfaceNetworkInstance = new InterfaceNetwork(this);

            interfaceNetworkInstance.addNetwork(multimodalNetworks.get(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex()), IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), InterfaceNetwork.InformationContent.PHONEMES);

            interfaceNetworkInstance.addNetwork(multimodalNetworks.get(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()), IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), InterfaceNetwork.InformationContent.WORDS);

            if (ContextTypoNetwork.USE_CONTEXT_INFORMATION) {
                interfaceNetworkInstance.addNetwork(multimodalNetworks.get(IndexNetwork.VIRTUAL_CLIQUES_NETWORK.getIndex()), IndexNetwork.VIRTUAL_CLIQUES_NETWORK.getIndex(), InterfaceNetwork.InformationContent.SENTENCES);
            }
            if (ContextTypoNetwork.RATES_PER_NETWORK) {
                matchingRatePerNetwork = new ArrayList<>();
                errorRatePerNetwork = new ArrayList<>();
            }
        }
    }

    public void decoderPhase(List<String> incorrectSentencesList, List<String> correctWordList, List<String> errorWordList, List<String> errorPhonList) throws TException {
        String word, modifiedWord, phon;
        Double match, matchR1aux, matchR2aux, matchR1, matchR2;

        matchR1 = 0.0;
        matchR2 = 0.0;
        int error = 0;
        int errorR1 = 0;
        int errorR2 = 0;
        LinkedList<List<String>> phonemesList;
        LinkedList<Fanal> winnersList;

        List<String> sentenceWords;
        int samples = correctWordList.size();
        FrenchTokenizer tokenizer = new FrenchTokenizer();
        VirtualNetwork contextNet = (VirtualNetwork) multimodalNetworks.get(IndexNetwork.VIRTUAL_CLIQUES_NETWORK.getIndex());
        VirtualWordContextDecoder contextDecoder = new VirtualWordContextDecoder(contextNet);
        List<String> activatedContextWords;
        for (int jSamples = 0; jSamples < samples; jSamples++) {
            // Decoding in context words network
            sentenceWords = new ArrayList<>(Arrays.asList(tokenizer.tokenize(incorrectSentencesList.get(jSamples))));
            activatedContextWords = contextDecoder.decodingUnknownWordSentence(sentenceWords);
            ContextTypoNetwork.logger.debug("Mots contexte: " + activatedContextWords);

            word = correctWordList.get(jSamples);
            modifiedWord = errorWordList.get(jSamples);
            ContextTypoNetwork.logger.debug("Mot entree: " + modifiedWord);
            String phonLia = errorPhonList.get(jSamples);
            // Il convertit les règles du format Lia pour le format du lexique ou du LIA selon apprentissage

            phonemesList = PhonemeRules.phonemeToListParser(phonLia);

            ContextTypoNetwork.logger.debug("Phoneme entree: " + phonemesList);
            winnersList = interfaceNetworkInstance.decoderInterfaceReseaux(BEGIN_WORD_SYMBOL + modifiedWord + END_WORD_SYMBOL, phonemesList, activatedContextWords);
            match = interfaceNetworkInstance.getMatchingRateInterfaceNetwork(winnersList, BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL);
            matchingRate += match;
            if (ContextTypoNetwork.RATES_PER_NETWORK) {

                phon = wordsPhonemesMap.get(word);

                matchR1aux = interfaceNetworkInstance.getMatchingRateNetwork(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), BEGIN_WORD_SYMBOL + phon + END_WORD_SYMBOL);

                matchR2aux = interfaceNetworkInstance.getMatchingRateNetwork(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL);
                matchR1 += matchR1aux;
                matchR2 += matchR2aux;

                if (matchR1aux < 1.0 || interfaceNetworkInstance.getWinnersFanalsNetwork(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex()).size() != ((TriangularNetwork) interfaceNetworkInstance.getNetwork(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex())).FANALS_PER_CLIQUE) {
                    errorR1++;
                }

                if (matchR2aux < 1.0 || interfaceNetworkInstance.getWinnersFanalsNetwork(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()).size() != ((FuzzyNetwork) interfaceNetworkInstance.getNetwork(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex())).FANALS_PER_CLIQUE) {
                    errorR2++;
                }
            }

            if (match < 1.0 || winnersList.size() != interfaceNetworkInstance.FANALS_PER_CLIQUE) {
                ContextTypoNetwork.logger.debug("Erreur: " + word + " Match: " + match);
                error++;
            }
        }
        if (ContextTypoNetwork.RATES_PER_NETWORK) {
            this.matchingRatePerNetwork.add(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), matchR1 / samples);
            this.matchingRatePerNetwork.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), matchR2 / samples);
            this.errorRatePerNetwork.add(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), (double) errorR1 / samples);
            this.errorRatePerNetwork.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), (double) errorR2 / samples);
        }
        matchingRate = matchingRate / samples;
        errorRate = ((double) error) / samples;
    }

    public void decoderPhase(List<String> correctWordList, List<String> errorWordList, List<String> errorPhonList) {

        String word, modifiedWord, phon;
        Double match, matchR1aux, matchR2aux, matchR1, matchR2;

        matchR1 = 0.0;
        matchR2 = 0.0;
        int error = 0;
        int errorR1 = 0;
        int errorR2 = 0;
        LinkedList<List<String>> phonemesList;
        LinkedList<Fanal> winnersList;

        int samples = correctWordList.size();

        for (int jSamples = 0; jSamples < samples; jSamples++) {

            word = correctWordList.get(jSamples);
            modifiedWord = errorWordList.get(jSamples);
            ContextTypoNetwork.logger.debug("Mot entree: " + modifiedWord);
            String phonLia = errorPhonList.get(jSamples);
            // Il convertit les règles du format Lia pour le format du lexique ou du LIA selon apprentissage

            phonemesList = PhonemeRules.phonemeToListParser(phonLia);

            ContextTypoNetwork.logger.debug("Phoneme entree: " + phonemesList);
            winnersList = interfaceNetworkInstance.decoderInterfaceReseaux(BEGIN_WORD_SYMBOL + modifiedWord + END_WORD_SYMBOL, phonemesList);
            match = interfaceNetworkInstance.getMatchingRateInterfaceNetwork(winnersList, BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL);
            matchingRate += match;
            if (ContextTypoNetwork.RATES_PER_NETWORK) {

                phon = wordsPhonemesMap.get(word);

                matchR1aux = interfaceNetworkInstance.getMatchingRateNetwork(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), BEGIN_WORD_SYMBOL + phon + END_WORD_SYMBOL);

                matchR2aux = interfaceNetworkInstance.getMatchingRateNetwork(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL);
                matchR1 += matchR1aux;
                matchR2 += matchR2aux;

                if (matchR1aux < 1.0 || interfaceNetworkInstance.getWinnersFanalsNetwork(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex()).size() != ((TriangularNetwork) interfaceNetworkInstance.getNetwork(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex())).FANALS_PER_CLIQUE) {
                    errorR1++;
                }

                if (matchR2aux < 1.0 || interfaceNetworkInstance.getWinnersFanalsNetwork(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()).size() != ((FuzzyNetwork) interfaceNetworkInstance.getNetwork(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex())).FANALS_PER_CLIQUE) {
                    errorR2++;
                }
            }

            if (match < 1.0 || winnersList.size() != interfaceNetworkInstance.FANALS_PER_CLIQUE) {
                ContextTypoNetwork.logger.debug("Erreur: " + word + " Match: " + match);
                error++;
            }
        }
        if (ContextTypoNetwork.RATES_PER_NETWORK) {
            this.matchingRatePerNetwork.add(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), matchR1 / samples);
            this.matchingRatePerNetwork.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), matchR2 / samples);
            this.errorRatePerNetwork.add(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), (double) errorR1 / samples);
            this.errorRatePerNetwork.add(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), (double) errorR2 / samples);
        }
        matchingRate = matchingRate / samples;
        errorRate = ((double) error) / samples;
    }

    public String getPhoneme(String word) {
        return this.wordsPhonemesMap.get(word);
    }

    public void learningSentencesPhase(List<String> trainingSentencesList) throws TException {
        String sentenceToken;
        FrenchTokenizer tokenizer = new FrenchTokenizer();
        VirtualNetwork contextNetwork = (VirtualNetwork) multimodalNetworks.get(IndexNetwork.VIRTUAL_CLIQUES_NETWORK.getIndex());
        for (String sentence : trainingSentencesList) {
            // Tokenization
            sentenceToken = FrenchTokenizer.concatTokens(tokenizer.tokenize(sentence), CONCAT_SYMBOL);
            storedSentences.add(sentenceToken);
        }
        contextNetwork.learnWordSequences(1, storedSentences);
    }

    public void learningWordsPhase(List<String> trainingWordsList, List<String> trainingPhonsList) {
        String word;
        for (int i = 0; i < trainingWordsList.size(); i++) {
            word = trainingWordsList.get(i);
            storedWords.add(word);
            wordsPhonemesMap.put(word, trainingPhonsList.get(i));
        }
        // Phase d'apprentissage des réseaux de neurones
        TriangularNetwork triangularNetworkLeft;
        FuzzyNetwork fuzzyNetworkRight;

        triangularNetworkLeft = (TriangularNetwork) multimodalNetworks.get(IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex());

        fuzzyNetworkRight = (FuzzyNetwork) multimodalNetworks.get(IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex());
        String phon;
        for (String wordAux : getShuffle(storedWords, storedWords.size())) {

            // Apprentissage des mots dans le reseau flous
            fuzzyNetworkRight.learnWord(BEGIN_WORD_SYMBOL + wordAux + END_WORD_SYMBOL);
            if (FuzzyNetwork.USE_MITOSIS) {
                // Réalisation de la mitose si besoin
                fuzzyNetworkRight.mitosis();
            }
            phon = wordsPhonemesMap.get(wordAux);

            triangularNetworkLeft.learnPhoneme(BEGIN_WORD_SYMBOL + phon + END_WORD_SYMBOL);

            // Creer l'interface entre les deux reseaux     
            learntWords.add(wordAux);
            if (ACTIVATE_INTERFACE_NETWORK) {
                this.interfaceNetworkInstance.learnWord(wordAux);
            }
        }
        ContextTypoNetwork.logger.warn("Nombre de mots appris: " + learntWords.size());
    }

    public String getLearntWords(int i) {
        return this.learntWords.get(i);
    }

    public Double getMatchingRate() {
        return this.matchingRate;
    }

    public Double getMatchingRate(int networkIndex) {
        return this.matchingRatePerNetwork.get(networkIndex);
    }

    public Double getErrorRate() {
        return this.errorRate;
    }

    public Double getErrorRate(int networkIndex) {
        return this.errorRatePerNetwork.get(networkIndex);
    }

    private static List<String> getShuffle(LinkedList<String> lst, int nElements) {
        List<String> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, nElements);
    }

    // Methode statique pour l'insertion aleatoire des lettres dans un mot
    public static String insertLetter(String word, String letter) {
        Random randGen;
        int pos;
        String result = word;
        randGen = new Random(); // Générateur de nombres aléatoires

        // Sélection aléatoire d'une position dans le mot
        if (letter.equals(ERASURE_SYMBOL)) {
            if (!word.contains(ERASURE_SYMBOL)) {
                pos = word.length() / 2;
            } else {
                pos = randGen.nextInt(word.length() + 1);
            }
        } else {
            pos = randGen.nextInt(word.length() + 1);
        }

        result = result.substring(0, pos) + letter + result.substring(pos);

        return result;
    }

    // Methode statique pour l'insertion aleatoire des lettres dans un mot
    public static String insertLetter(String word, String letter, int numberOfTimes) {
        String result = word;
        for (int i = 0; i < numberOfTimes; i++) {
            result = NetworkControl.insertLetter(result, letter);
        }

        return result;
    }

    public static int getRandomPositionWord(String mot) {
        Random randGen = new Random();
        int pos = randGen.nextInt(mot.length());
        return pos;
    }

    public static int getRandomPositionWord(List<String> phon) {
        Random randGen = new Random();
        phon.remove(BEGIN_WORD_SYMBOL);
        phon.remove(END_WORD_SYMBOL);
        int pos = randGen.nextInt(phon.size());
        return pos;
    }

    public static int getPhonemeSize(String phon) {
        int result = 0;
        for (int i = 0; i < phon.length(); i++) {
            if (!phon.substring(i, i + 1).equals(BEGIN_WORD_SYMBOL) && !phon.substring(i, i + 1).equals(END_WORD_SYMBOL)) {
                i++;
            }
            result++;
        }
        return result;
    }

}
