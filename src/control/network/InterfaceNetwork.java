package control.network;

import control.NetworkControl;
import control.ContextTypoNetwork;
import control.decoder.FuzzyDecoder;
import control.decoder.TriangularDecoder;
import control.rules.LetterInformation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import model.Clique;
import model.Fanal;
import model.FuzzyFanal;

public class InterfaceNetwork extends TriangularNetwork implements LetterInformation {

    // Ils définissent le type d'information à être appris pour chaque reseau
    public enum InformationContent {

        WORDS(0), PHONEMES(1), SENTENCES(2);

        private int value;

        private InformationContent(int value) {
            this.value = value;
        }

        public int getValue() {
            return this.value;
        }

    }

    // Ils définissent le type de liaison entre les cliques frontieres des reseaux
    public enum LinkType {

        UNIDIRECTIONAL(0), BIDIRECTIONAL(1);

        private int type;

        private LinkType(int type) {
            this.type = type;
        }

        public int getType() {
            return this.type;
        }

    }

    // Ils définissent la direction d'activation
    public enum ActivationType {

        ACTIVATION_TO_RIGHT(0), ACTIVATION_TO_LEFT_SIDE(1);
        private int type;

        private ActivationType(int type) {
            this.type = type;
        }

        public int getValue() {
            return this.type;
        }
    }

    // Ces attributs representent les reseaux de l'interface reseaux
    private final ArrayList<Network> multimodalNetworks;
    private final ArrayList<InformationContent> typeNetworks;
    private final NetworkControl networkControl;
    private ArrayList<HashSet<Fanal>> decodedFanals;

    public static HashMap<Fanal, Integer> fanalScoreMap;

    public InterfaceNetwork() {
        super(0, 0, 0, 0, false, false);
        this.multimodalNetworks = null;
        this.typeNetworks = null;
        this.networkControl = null;
    }

    public InterfaceNetwork(NetworkControl controleReseaux) {
        // On instancie un reseau du type triangulaire
        super(200, 100, 8, 1, false, true);
        this.multimodalNetworks = new ArrayList<>();
        this.typeNetworks = new ArrayList<>();
        this.networkControl = controleReseaux;
        this.decodedFanals = new ArrayList<>();
        // Ce sont les valeurs de score de chaque macro fanal
        fanalScoreMap = new HashMap<>();
        if (ContextTypoNetwork.RATES_PER_NETWORK) {

            this.decodedFanals.add(NetworkControl.IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), new HashSet<>());

            this.decodedFanals.add(NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), new HashSet<>());

            if (ContextTypoNetwork.USE_CONTEXT_INFORMATION) {

                this.decodedFanals.add(NetworkControl.IndexNetwork.VIRTUAL_CLIQUES_NETWORK.getIndex(), new HashSet<>());
            }

        }

    }

    // On ajoute un reseau d'indice "indiceReseau" et avec un type
    public void addNetwork(Network r, int indiceReseau, InformationContent typeReseau) {
        this.multimodalNetworks.add(indiceReseau, r);
        this.typeNetworks.add(indiceReseau, typeReseau);
    }

    @Override
    public Clique learnWord(String word) {

        Clique cliqueInterface, cliqueInf, cliqueLeft, cliqueRight;
        cliqueLeft = null;
        cliqueRight = null;
        TriangularLevel n;
        // Il obtient le niveau h=0
        n = (TriangularLevel) this.levelsList.get(0);
        if (n.existsClique(BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL)) {
            return null;
        } else {
            cliqueInf = null;
            ContextTypoNetwork.logger.debug("Réseau d'interface - " + BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL + " -");
            // Il ajoute la clique du mot dans le niveau h=0 du reseau interface
            cliqueInterface = n.addClique(BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL, 1, "", 0, "", 0, this.FANALS_PER_CLIQUE);
            for (int i = 0; i < multimodalNetworks.size(); i++) {
                // Il selectionne le type d'information à garder
                if (typeNetworks.get(i) == InformationContent.WORDS && multimodalNetworks.get(i).getTypeReseau() == NetworkControl.TypeNetwork.FUZZY_NETWORK) {
                    String variableWord = BEGIN_WORD_SYMBOL + word + END_WORD_SYMBOL;
                    if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
                        int size = variableWord.length();
                        for (int j = 0; j < ((FuzzyNetwork) multimodalNetworks.get(i)).NUMBER_CLUSTERS - size; j++) {
                            variableWord = variableWord + PADDING_SYMBOL;
                        }
                    }
                    cliqueInf = ((FuzzyLevel) ((FuzzyNetwork) multimodalNetworks.get(i)).levelsList.get(0)).getCliqueWordFuzzy(variableWord);
                    ContextTypoNetwork.logger.debug("Clique mot: " + word + " : " + cliqueInf.getInfo());
                } else {
                    if (typeNetworks.get(i) == InformationContent.PHONEMES && multimodalNetworks.get(i).getTypeReseau() == NetworkControl.TypeNetwork.TRIANGULAR_NETWORK) {
                        cliqueInf = ((TriangularNetwork) multimodalNetworks.get(i)).getSuperiorLevel().getWordClique(BEGIN_WORD_SYMBOL + networkControl.getPhoneme(word) + END_WORD_SYMBOL);
                        ContextTypoNetwork.logger.debug("Clique phoneme: " + networkControl.getPhoneme(word) + " : " + cliqueInf.getInfo());
                    } else {
                        continue;
                    }
                }
                // Il cree la liaison entre les deux niveaux
                for (Fanal fInf : cliqueInf.getFanalsList()) {
                    for (Fanal fSup : cliqueInterface.getFanalsList()) {
                        //TypoMultireseaux.logger.debug(fInf + " -> " + fSup);
                        n.createLinkBetweenLevels(fInf, fSup);
                    }
                }
                if (NetworkControl.ACTIVATE_LATERAL_CONNECTIONS) {
                    // Il selectionne les fanaux de la clique à gauche et de la clique à droite
                    if (i == 0) {
                        cliqueLeft = cliqueInf;
                    } else {
                        cliqueRight = cliqueInf;
                    }
                }

            }
            if (NetworkControl.ACTIVATE_LATERAL_CONNECTIONS) {
                for (Fanal fLeft : cliqueLeft.getFanalsList()) {
                    for (Fanal fRight : cliqueRight.getFanalsList()) {
                        // Il crée les liaisons laterales
                        ((TriangularLevel) this.getLevelsList().get(0)).createLinkBetweenHyperLevels(fLeft, fRight);
                    }
                }
            }
            return cliqueInterface;
        }
    }

    public LinkedList<Fanal> decoderInterfaceReseaux(String modifiedWord, LinkedList<List<String>> phonemes) {
        HashSet<Fanal> triangularFanalsSet = new HashSet<>();
        HashSet<Fanal> fuzzyFanalsSet = new HashSet<>();
        LinkedList<Fanal> fuzzyFanalsList = new LinkedList<>();
        LinkedList<Fanal> triangularFanalsList = new LinkedList<>();
        LinkedList<Fanal> inferiorFanalsList = new LinkedList<>();
        TriangularNetwork triangularNetwork;
        FuzzyNetwork fuzzyRightNetwork;
        TriangularDecoder triangularDecoder;
        FuzzyDecoder fuzzyDecoder;
        String fuzzyWord;
        // ---- Flous de mots -------
        fuzzyRightNetwork = (FuzzyNetwork) multimodalNetworks.get(NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex());
        // Il prend le decodeur du Reseau Flous
        fuzzyDecoder = (FuzzyDecoder) (fuzzyRightNetwork).getDecodeur();
        // Ajoute postambule dans le mot (information de non information)
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = modifiedWord.length();
            for (int i = 0; i < fuzzyRightNetwork.NUMBER_CLUSTERS - taille; i++) {
                modifiedWord = modifiedWord + PADDING_SYMBOL;
            }
        }
        if (modifiedWord.length() < fuzzyRightNetwork.NUMBER_CLUSTERS) {

            fuzzyWord = BEGIN_WORD_SYMBOL + NetworkControl.insertLetter(modifiedWord.substring(1, modifiedWord.length() - 1), ERASURE_SYMBOL, fuzzyRightNetwork.NUMBER_CLUSTERS - modifiedWord.length()) + END_WORD_SYMBOL;
            // Il réalise la propagation et decodage (0 est la fênetre initialle)
            fuzzyDecoder.recognizePatternBottomUpDecoding(fuzzyWord, 0, false, 0);
            // Il obtient les fanaux gagnants du decodage flous
            for (LinkedList<Fanal> lst : fuzzyDecoder.getWinnersPatternsFuzzyDecoding(fuzzyWord)) {
                fuzzyFanalsList.addAll(lst);
            }
        } else {
            // Il réalise la propagation et decodage
            fuzzyDecoder.recognizePatternBottomUpDecoding(modifiedWord, 0, false, 0);
            // Il obtient les fanaux gagnants du decodage flous
            for (LinkedList<Fanal> lst : fuzzyDecoder.getWinnersPatternsFuzzyDecoding(modifiedWord)) {
                fuzzyFanalsList.addAll(lst);
            }
        }
        fuzzyFanalsSet.addAll(fuzzyFanalsList);

        LinkedList<Fanal> lstAux = new LinkedList<>();
        lstAux.addAll(fuzzyFanalsList);
        InterfaceNetwork.this.activateLateralConnections(lstAux, ActivationType.ACTIVATION_TO_LEFT_SIDE.getValue());
        // Si il y a un reseau triangulaire de phonemes
        triangularNetwork = (TriangularNetwork) multimodalNetworks.get(NetworkControl.IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex());
        // Il prend le decodeur du Reseau Triangulaire
        triangularDecoder = (TriangularDecoder) (triangularNetwork.getDecoder());
        triangularFanalsList.addAll(triangularDecoder.getWinnersTwoLevelsJumpDecoding(phonemes));
        triangularFanalsSet.addAll(triangularFanalsList);

        // Ajoute la liste de gagnants des reseaux
        if (ContextTypoNetwork.RATES_PER_NETWORK) {
            this.decodedFanals.set(NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex(), fuzzyFanalsSet);
            ContextTypoNetwork.logger.warn("#Flous Mots # " + this.decodedFanals.get(NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()).size());

            this.decodedFanals.set(NetworkControl.IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex(), triangularFanalsSet);
            ContextTypoNetwork.logger.warn("#Triang # " + this.decodedFanals.get(NetworkControl.IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex()).size());

        }

        if (TriangularDecoder.UNION_BOOSTING) {
            inferiorFanalsList.addAll(triangularFanalsSet);
        } else {
            inferiorFanalsList.addAll(triangularFanalsList);
        }

        inferiorFanalsList.addAll(fuzzyFanalsSet);

        ContextTypoNetwork.logger.debug("Nombre fanaux avant propagation :" + inferiorFanalsList.size());
        if (NetworkControl.ACTIVATE_LATERAL_CONNECTIONS) {
            // À chaque recherche de mot on redemarre les activations laterales
            remiseZeroPropagationLaterale();
        }

        return ((TriangularDecoder) this.getDecoder()).getWinnersInterfaceNetwork(inferiorFanalsList);
    }

    private void activateLateralConnections(LinkedList<Fanal> activatedFanals, int directionActivation) {
        for (Fanal f : activatedFanals) {
            activateLateralConnections(f, directionActivation);
        }

    }

    private void activateLateralConnections(Fanal fanalActive, int directionActivation) {
        // Convention: HyperSup est a droite et HyperInf est a gauche 
        if (directionActivation == ActivationType.ACTIVATION_TO_LEFT_SIDE.getValue()) {
            for (Fanal f : fanalActive.getInferiorHyperFanals()) {

                if (fanalScoreMap.containsKey(f)) {
                    fanalScoreMap.put(f, fanalScoreMap.get(f) + 1);
                } else {
                    fanalScoreMap.put(f, 1);
                }
            }

        } else {
            for (Fanal f : fanalActive.getSuperiorHyperFanals()) {

                if (fanalScoreMap.containsKey(f)) {
                    fanalScoreMap.put(f, fanalScoreMap.get(f) + 1);
                } else {
                    fanalScoreMap.put(f, 1);
                }

            }
        }
    }

    public double getMatchingRateNetwork(int networkIndex, String learntWords) {
        int foundFanals = 0;
        HashSet<Fanal> winnersFanalsPerNetwork = decodedFanals.get(networkIndex);
        HashSet<Fanal> macroWinnersFanalsPerNetwork = new HashSet<>();
        LinkedList<Fanal> correctFanals;
        int fanalsPerClique;
        if (networkIndex == NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()) {
            // Substituir por uma unica funcao
            if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
                int size = learntWords.length();
                for (int i = 0; i < ((FuzzyNetwork) this.multimodalNetworks.get(networkIndex)).NUMBER_CLUSTERS - size; i++) {
                    learntWords = learntWords + PADDING_SYMBOL;
                }
            }

            correctFanals = this.multimodalNetworks.get(networkIndex).getLevelsList().get(0).getWordClique(learntWords).getFanalsList();
            for (Fanal f : winnersFanalsPerNetwork) {
                macroWinnersFanalsPerNetwork.add(((FuzzyFanal) f).getMacroFanal());
            }
        } else {

            correctFanals = ((TriangularNetwork) this.multimodalNetworks.get(networkIndex)).getSuperiorLevel().getWordClique(learntWords).getFanalsList();

        }
        if (NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex() == networkIndex) {
            for (Fanal f : correctFanals) {
                if (macroWinnersFanalsPerNetwork.contains(f)) {
                    foundFanals++;
                }
            }
            fanalsPerClique = ((FuzzyNetwork) this.multimodalNetworks.get(networkIndex)).FANALS_PER_CLIQUE;
        } else {
            for (Fanal f : correctFanals) {
                if (winnersFanalsPerNetwork.contains(f)) {
                    foundFanals++;
                }
            }
            fanalsPerClique = ((TriangularNetwork) this.multimodalNetworks.get(networkIndex)).FANALS_PER_CLIQUE;
        }

        return ((double) foundFanals) / fanalsPerClique;
    }

    public HashSet<Fanal> getWinnersFanalsNetwork(int indiceReseau) {
        return this.decodedFanals.get(indiceReseau);
    }

    public Network getNetwork(int indiceReseau) {
        return this.multimodalNetworks.get(indiceReseau);
    }

    private void remiseZeroPropagationLaterale() {
        fanalScoreMap = new HashMap<>();
    }

}
