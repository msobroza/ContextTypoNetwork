package control;

import control.rules.LetterInformation;
import control.rules.PhonemeRules;
import control.network.InterfaceNetwork;
import control.network.Network;
import control.network.FuzzyNetwork;
import control.network.TriangularNetwork;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import model.Fanal;

public class NetworkControl implements LetterInformation {

    public static final int TRIANGULAR_NETWORK_INDEX = 0;
    public static final int INDICE_RESEAU_FLOUS_GAUCHE = 0;
    public static final int FUZZY_NETWORK_INDEX = 1;
    public static final int TRIANGULAR_NETWORK = 0;
    public static final int FUZZY_NETWORK = 1;

    // Il cree un reseau interface (triangulaire et flous)
    public static final boolean ACTIVATE_INTERFACE_NETWORK = true;
    // Il active l'apprentissage des connexions laterales
    public static final boolean ACTIVATE_LATERAL_CONNECTIONS = true;

    private final LinkedList<Network> multimodalNetworks;
    private final LinkedList<String> storedWords;
    private final LinkedList<String> learntWords;
    private final HashMap<String, String> wordsPhonemesMap;
    private double errorRate;
    private double matchingRate;
    private final InterfaceNetwork interfaceNetworkInstance;
    private ArrayList<Double> matchingRatePerNetwork;
    private ArrayList<Double> errorRatePerNetwork;
    private final int NOMBRE_CLUSTERS_RESEAU_FLOUS_DROITE = 15;

    public NetworkControl() {
        multimodalNetworks = new LinkedList<>();
        storedWords = new LinkedList<>();
        learntWords = new LinkedList<>();
        wordsPhonemesMap = new HashMap<>();
        errorRate = 0.0;
        matchingRate = 0.0;
        // On instancie un reseau du type triangulaire (int l, int X, int c, int hMax, NIVEAU_SUPPLEMENTAIRE)
        multimodalNetworks.add(TRIANGULAR_NETWORK_INDEX, new TriangularNetwork(200, 100, 8, 14, true, false));

        // On instancie un reseau du type flous de mots
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            multimodalNetworks.add(FUZZY_NETWORK_INDEX, new FuzzyNetwork(NOMBRE_CLUSTERS_RESEAU_FLOUS_DROITE, InterfaceNetwork.INFORMATION_CONTENT_WORDS));
        } else {
            multimodalNetworks.add(FUZZY_NETWORK_INDEX, new FuzzyNetwork(ContextTypoNetwork.nblettres + 2, InterfaceNetwork.INFORMATION_CONTENT_WORDS));
        }

        if (ACTIVATE_INTERFACE_NETWORK) {
            interfaceNetworkInstance = new InterfaceNetwork(this);

            interfaceNetworkInstance.addNetwork(multimodalNetworks.get(TRIANGULAR_NETWORK_INDEX), TRIANGULAR_NETWORK_INDEX, InterfaceNetwork.INFORMATION_CONTENT_PHONEMES);

            interfaceNetworkInstance.addNetwork(multimodalNetworks.get(FUZZY_NETWORK_INDEX), FUZZY_NETWORK_INDEX, InterfaceNetwork.INFORMATION_CONTENT_WORDS);
            if (ContextTypoNetwork.RATES_PER_NETWORK) {
                matchingRatePerNetwork = new ArrayList<>();
                errorRatePerNetwork = new ArrayList<>();
            }
        }
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
            winnersList = interfaceNetworkInstance.decoderInterfaceReseaux("<" + modifiedWord + ">", phonemesList);
            match = interfaceNetworkInstance.getMatchingRateInterfaceNetwork(winnersList, "<" + word + ">");
            matchingRate += match;
            if (ContextTypoNetwork.RATES_PER_NETWORK) {

                phon = wordsPhonemesMap.get(word);

                matchR1aux = interfaceNetworkInstance.getMatchingRateNetwork(NetworkControl.TRIANGULAR_NETWORK_INDEX, "<" + phon + ">");

                matchR2aux = interfaceNetworkInstance.getMatchingRateNetwork(NetworkControl.FUZZY_NETWORK_INDEX, "<" + word + ">");
                matchR1 += matchR1aux;
                matchR2 += matchR2aux;

                if (matchR1aux < 1.0 || interfaceNetworkInstance.getWinnersFanalsNetwork(NetworkControl.TRIANGULAR_NETWORK_INDEX).size() != ((TriangularNetwork) interfaceNetworkInstance.getNetwork(TRIANGULAR_NETWORK_INDEX)).FANALS_PER_CLIQUE) {
                    errorR1++;
                }

                if (matchR2aux < 1.0 || interfaceNetworkInstance.getWinnersFanalsNetwork(NetworkControl.FUZZY_NETWORK_INDEX).size() != ((FuzzyNetwork) interfaceNetworkInstance.getNetwork(FUZZY_NETWORK_INDEX)).FANALS_PER_CLIQUE) {
                    errorR2++;
                }
            }

            if (match < 1.0 || winnersList.size() != interfaceNetworkInstance.FANALS_PER_CLIQUE) {
                ContextTypoNetwork.logger.debug("Erreur: " + word + " Match: " + match);
                error++;
            }
        }
        if (ContextTypoNetwork.RATES_PER_NETWORK) {
            this.matchingRatePerNetwork.add(NetworkControl.TRIANGULAR_NETWORK_INDEX, matchR1 / samples);
            this.matchingRatePerNetwork.add(NetworkControl.FUZZY_NETWORK_INDEX, matchR2 / samples);
            this.errorRatePerNetwork.add(NetworkControl.TRIANGULAR_NETWORK_INDEX, (double) errorR1 / samples);
            this.errorRatePerNetwork.add(NetworkControl.FUZZY_NETWORK_INDEX, (double) errorR2 / samples);
        }
        matchingRate = matchingRate / samples;
        errorRate = ((double) error) / samples;
    }

    public String getPhoneme(String word) {
        return this.wordsPhonemesMap.get(word);
    }


    public void learningPhase(List<String> trainingWordsList, List<String> trainingPhonsList) {
        String word;
        //Verificar que as duas listas tem  o mesmo tamanho
        for (int i = 0; i < trainingWordsList.size(); i++) {
            word = trainingWordsList.get(i);
            storedWords.add(word);
            wordsPhonemesMap.put(word, trainingPhonsList.get(i));
        }
        // Phase d'apprentissage des réseaux de neurones
        TriangularNetwork triangularNetworkLeft;
        FuzzyNetwork fuzzyNetworkRight;

        triangularNetworkLeft = (TriangularNetwork) multimodalNetworks.get(TRIANGULAR_NETWORK_INDEX);

        fuzzyNetworkRight = (FuzzyNetwork) multimodalNetworks.get(FUZZY_NETWORK_INDEX);
        String phon;
        for (String wordAux : getShuffle(storedWords, storedWords.size())) {

            // Apprentissage des mots dans le reseau flous
            fuzzyNetworkRight.learnWord("<" + wordAux + ">");
            if (FuzzyNetwork.USE_MITOSIS) {
                // Réalisation de la mitose si besoin
                fuzzyNetworkRight.mitosis();
            }
            phon = wordsPhonemesMap.get(wordAux);

            triangularNetworkLeft.learnPhoneme("<" + phon + ">");

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
        if (letter.equals(ERASURE_CHAR)) {
            if (!word.contains(ERASURE_CHAR)) {
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
        phon.remove(BEGIN_WORD_CHAR);
        phon.remove(END_WORD_CHAR);
        int pos = randGen.nextInt(phon.size());
        return pos;
    }

    public static int getPhonemeSize(String phon) {
        int result = 0;
        for (int i = 0; i < phon.length(); i++) {
            if (!phon.substring(i, i + 1).equals(BEGIN_WORD_CHAR) && !phon.substring(i, i + 1).equals(END_WORD_CHAR)) {
                i++;
            }
            result++;
        }
        return result;
    }

}
