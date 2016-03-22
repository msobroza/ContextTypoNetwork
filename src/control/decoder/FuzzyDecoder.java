package control.decoder;

import control.NetworkControl;
import control.network.FuzzyLevel;
import control.network.FuzzyNetwork;
import control.ContextTypoNetwork;
import control.rules.LetterInformation;
import control.rules.PhonemeRules;
import control.network.InterfaceNetwork;
import graph.Edge;
import graph.Graph;
import graph.FuzzyGraph;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import model.Clique;
import model.Cluster;
import model.Fanal;
import model.FuzzyFanal;
import model.MacroFanal;

public class FuzzyDecoder extends Decoder implements LetterInformation {

    protected final int GWsTA_vertical_1;
    protected final int GWsTA_vertical_2;
    protected final int GWsTA_horizontal;

    public static final int[] CIRCULAR_WINDOW = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    public static final boolean USE_FUZZY_BOOSTING = true;
    public static final boolean N_ITERATIONS_BEFORE_BOOSTING = true;
    public static final boolean ITERATION_UNIQUE = false;
    public static final boolean SEVERALS_BOOSTING = true;
    public static final int FILTER_SUPPLENESS = 2;
    public static int MAX_BOOSTING;

    // Contient les fanaux les plus activés de chaque niveau Bottom-up
    protected LinkedList<LinkedList<Fanal>> bestScoresBottomUp;
    // Contient les derniers fanaux les plus activés Bottom-up
    protected LinkedList<Fanal> bestScoresBottomUpLast;
    // Contient les sequences des fanaux trouvés Bottom-up
    protected HashMap<String, LinkedList<LinkedList<Fanal>>> seqsPropagBottomUp;
    // Contient les sequences des fanaux flous trouvés Bottom-up 
    protected HashMap<String, LinkedList<LinkedList<Fanal>>> seqsPropagBottomUpFlous;
    //Il permet de savoir les de quelle cluster vient les activations d'un fanal SoM
    protected LinkedList<HashMap<Fanal, HashMap<Cluster, Integer>>> activationsListClusters;
    // Contient les sequences Top-Down
    protected LinkedList<HashMap<Integer, LinkedList<Fanal>>> seqsPropagTopDown;
    // Il permet de savoir combien de sequences il y a pour chaque niveau au moment du decodage
    protected int[] seqsTopDownCounter;

    private final FuzzyNetwork net;

    public FuzzyDecoder(FuzzyNetwork r) {
        MAX_BOOSTING = r.FANALS_PER_CLIQUE * 3;
        //MAX_BOOSTING = 3;
        GWsTA_vertical_1 = r.FANALS_PER_CLIQUE;
        GWsTA_vertical_2 = r.FANALS_PER_CLIQUE * 2;
        GWsTA_horizontal = r.FANALS_PER_CLIQUE;

        this.net = r;
        seqsPropagBottomUp = new HashMap<>();
        seqsPropagBottomUpFlous = new HashMap<>();
        bestScoresBottomUp = new LinkedList<>();
        seqsTopDownCounter = new int[FuzzyNetwork.hMax];
        seqsPropagTopDown = new LinkedList<>();
        bestScoresBottomUpLast = new LinkedList<>();
        activationsListClusters = new LinkedList<>();

        for (int i = 0; i < FuzzyNetwork.hMax; i++) {
            bestScoresBottomUp.add(i, new LinkedList<>());
            seqsPropagTopDown.add(i, new HashMap<>());
            seqsTopDownCounter[i] = 0;
            // Il utilise le SoM
            activationsListClusters.add(i, new HashMap<>());
        }

    }

    public double verifyDecodingBottomUp(String word, String targetWord) {
        Clique clique;
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = word.length();
            for (int i = 0; i < this.net.NUMBER_CLUSTERS - taille; i++) {
                word = word + PADDING_SYMBOL;
            }
            for (int i = 0; i < this.net.NUMBER_CLUSTERS - targetWord.length(); i++) {
                targetWord = targetWord + PADDING_SYMBOL;
            }
        }
        clique = net.getLevelsList().get(0).getWordClique(targetWord);
        if (clique == null) {
            return -1.0;
        }
        int i;
        int iMax = 0;
        for (LinkedList<Fanal> lstClique : seqsPropagBottomUp.get(word)) {
            i = 0;
            for (Fanal f : lstClique) {
                if (clique.existsFanal(f)) {
                    i++;
                }
            }
            if (i > iMax) {
                iMax = i;
            }
        }

        return ((double) iMax) / targetWord.length();
    }

    public LinkedList<LinkedList<Fanal>> getWinnersBottomUp(String word) {
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = word.length();
            for (int i = 0; i < this.net.NUMBER_CLUSTERS - taille; i++) {
                word = word + PADDING_SYMBOL;
            }
        }
        return seqsPropagBottomUp.get(word);
    }

    public String getWord(LinkedList<Fanal> fanalsList) {
        String word = "";
        HashMap<Cluster, String> mapCluster = new HashMap<>();
        for (Fanal f : fanalsList) {
            mapCluster.put(((MacroFanal) f).getCluster(), ((MacroFanal) f).getLetter());
        }
        FuzzyGraph g = (FuzzyGraph) net.getLevelsList().get(0).getGraph();
        for (int i = 0; i < net.NUMBER_CLUSTERS; i++) {
            word += mapCluster.get(g.getCluster(i));
        }
        return word;
    }

    // ------------- Decodage Bottom-up-------------------
    public void recognizePatternBottomUpDecoding(String word, int windowSize, boolean phonemeUsage, int suppleness) {
        String letter;
        List<String> phonList = null;
        FuzzyGraph g = (FuzzyGraph) net.getLevelsList().get(0).getGraph();
        LinkedList<MacroFanal> activesReseauDroite = new LinkedList<>();
        // INITIALISATION
        // Initialisation des scores des fanaux
        resetScores(0);
        // Initialisation du décodage du réseau
        resetShortMemory();

        LinkedList<Fanal> lstWinners = new LinkedList<>();
        Fanal fanalLetter;
        String sourceWord = word;
        int numberFanalsBefore, numberFanalsAfter;
        int nErasures = 0;
        ContextTypoNetwork.logger.debug("Première activation");
        System.out.println("Info: " + word);
        // PREMIERE ACTIVATION
        if (FuzzyNetwork.FLOU2FLOU) {
            boolean insertionCondition = false;
            HashSet<MacroFanal> activatedFanals = new HashSet<>();
            HashMap<MacroFanal, Integer> scoreFanals = new HashMap<>();
            // Recherche la position d'une lettre possiblement effacée
            for (int i = 0; i < word.length(); i++) {
                letter = word.substring(i, i + 1);
                if (letter.equals(ERASURE_SYMBOL)) {
                    nErasures++;
                }
            }

            // Recherche la position d'une lettre possiblement inserée
            if (phonemeUsage) {

                phonList = PhonemeRules.phonemesLIAToList(word);
                activesReseauDroite.addAll(thresholdLateralPropagation());
                for (MacroFanal mf : activesReseauDroite) {
                    System.out.println(mf + " -> " + mf.getLetter());
                }
                if (phonList.size() > net.NUMBER_CLUSTERS) {
                    insertionCondition = true;
                    int offsetPos;
                    ArrayList<Integer> posDeletions = new ArrayList<>();
                    ArrayList<Integer> posClusters = new ArrayList<>();
                    ArrayList<String> lettresDeletes = new ArrayList<>();
                    ArrayList<String> motAvecDeletions;
                    offsetPos = NetworkControl.getRandomPositionWord(phonList) + 1;
                    int pos;
                    for (int j = 0; j < phonList.size() - net.NUMBER_CLUSTERS; j++) {
                        pos = ((offsetPos + 2 * j) % (word.length() - 2)) + 1;
                        posDeletions.add(pos);
                    }
                    Collections.sort(posDeletions);
                    offsetPos = 0;
                    motAvecDeletions = new ArrayList<>(phonList);
                    for (int j = 0; j < phonList.size() - net.NUMBER_CLUSTERS; j++) {
                        lettresDeletes.add(motAvecDeletions.get(posDeletions.get(j) - offsetPos));
                        motAvecDeletions.remove(posDeletions.get(j) - offsetPos);
                        posClusters.add(posDeletions.get(j) - offsetPos - 1);
                        offsetPos++;
                    }
                    // Sélection de l'ensemble des fanaux activants (deletes)
                    ContextTypoNetwork.logger.debug("Activation macrofanaux - fanaux déletés");
                    int iC;
                    for (int j = 0; j < phonList.size() - net.NUMBER_CLUSTERS; j++) {
                        letter = lettresDeletes.get(j);
                        iC = posClusters.get(j);
                        int iFenetre = 0;
                        for (int iCluster = -windowSize; iCluster <= windowSize; iCluster++) {
                            MacroFanal mf = g.getCluster(iCluster + iC).getMacroFanal(letter);
                            ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                            activatedFanals.add(mf);
                            if (!scoreFanals.containsKey(mf)) {
                                scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]);
                            } else {
                                if (scoreFanals.get(mf) < FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]) {
                                    scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]);
                                }
                            }
                            iFenetre++;
                        }
                    }
                    phonList = new ArrayList<>(motAvecDeletions);
                }

                // Sélection de l'ensemble des fanaux activants
                ContextTypoNetwork.logger.debug("Activation macrofanaux");
                for (int i = 0; i < phonList.size(); i++) {
                    letter = phonList.get(i);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    int iFenetre = 0;
                    for (int iCluster = -windowSize; iCluster <= windowSize; iCluster++) {
                        MacroFanal mf = g.getCluster(iCluster + i).getMacroFanal(letter);
                        ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                        activatedFanals.add(mf);
                        if (!scoreFanals.containsKey(mf)) {

                            scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]);
                        } else {
                            // SoM
                            if (scoreFanals.get(mf) < FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]) {
                                scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]);
                            }
                        }
                        iFenetre++;
                    }
                }
            } else {
                if (word.length() > net.NUMBER_CLUSTERS) {
                    insertionCondition = true;
                    int offsetPos;
                    ArrayList<Integer> posDeletions = new ArrayList<>();
                    ArrayList<Integer> posClusters = new ArrayList<>();
                    ArrayList<String> lettresDeletes = new ArrayList<>();
                    String motAvecDeletions;
                    offsetPos = NetworkControl.getRandomPositionWord(word.substring(1, word.length() - 1)) + 1;
                    int pos;
                    for (int j = 0; j < word.length() - net.NUMBER_CLUSTERS; j++) {
                        pos = ((offsetPos + 2 * j) % (word.length() - 2)) + 1;
                        posDeletions.add(pos);
                    }
                    Collections.sort(posDeletions);
                    offsetPos = 0;
                    motAvecDeletions = word;
                    for (int j = 0; j < word.length() - net.NUMBER_CLUSTERS; j++) {
                        lettresDeletes.add(motAvecDeletions.substring(posDeletions.get(j) - offsetPos, posDeletions.get(j) - offsetPos + 1));
                        motAvecDeletions = motAvecDeletions.substring(0, posDeletions.get(j) - offsetPos) + motAvecDeletions.substring(posDeletions.get(j) - offsetPos + 1, motAvecDeletions.length());
                        posClusters.add(posDeletions.get(j) - offsetPos - 1);
                        offsetPos++;
                    }
                    // Sélection de l'ensemble des fanaux activants (deletes)
                    ContextTypoNetwork.logger.debug("Activation macrofanaux - fanaux déletés");
                    int iC;
                    for (int j = 0; j < word.length() - net.NUMBER_CLUSTERS; j++) {
                        letter = lettresDeletes.get(j);
                        iC = posClusters.get(j);
                        int iFenetre = 0;
                        for (int iCluster = -windowSize; iCluster <= windowSize; iCluster++) {
                            MacroFanal mf = g.getCluster(iCluster + iC).getMacroFanal(letter);
                            ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                            activatedFanals.add(mf);
                            if (!scoreFanals.containsKey(mf)) {
                                scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]);
                            } else {
                                // SoM
                                if (scoreFanals.get(mf) < FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]) {
                                    scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iFenetre]);
                                }
                            }
                            iFenetre++;
                        }
                    }
                    word = motAvecDeletions;
                }

                // Sélection de l'ensemble des fanaux activants
                ContextTypoNetwork.logger.debug("Activation macrofanaux");

                for (int i = 0; i < word.length(); i++) {
                    letter = word.substring(i, i + 1);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    int iWindow = 0;
                    for (int iCluster = -windowSize; iCluster <= windowSize; iCluster++) {
                        MacroFanal mf = g.getCluster(iCluster + i).getMacroFanal(letter);
                        ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                        activatedFanals.add(mf);
                        if (!scoreFanals.containsKey(mf)) {
                            scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iWindow]);
                        } else {
                            // SoM
                            if (scoreFanals.get(mf) < FuzzyDecoder.CIRCULAR_WINDOW[iWindow]) {
                                scoreFanals.put(mf, FuzzyDecoder.CIRCULAR_WINDOW[iWindow]);
                            }
                        }
                        iWindow++;
                    }
                }
            }
            ContextTypoNetwork.logger.debug("Propagation via chaînes de tournois");
            //Activation des connections SoM
            ContextTypoNetwork.logger.debug("Nombre de fanaux Activés: " + activatedFanals.size());
            for (MacroFanal fActivant : activatedFanals) {

                activationMacroFanalConnections(fActivant, g, scoreFanals.get(fActivant));
            }
        } else {
            ContextTypoNetwork.logger.debug("Activation fanaux");
            HashSet<Fanal> activatedFanals = new HashSet<>();
            if (phonemeUsage) {
                for (int i = 0; i < phonList.size(); i++) {
                    letter = phonList.get(i);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    for (int iCluster = -windowSize; iCluster <= windowSize; iCluster++) {
                        activatedFanals.add(g.getCluster(iCluster + i).getFanal(letter));
                    }
                }
            } else {
                for (int i = 0; i < word.length(); i++) {
                    letter = word.substring(i, i + 1);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    for (int iCluster = -windowSize; iCluster <= windowSize; iCluster++) {
                        activatedFanals.add(g.getCluster(iCluster + i).getFanal(letter));
                    }
                }
            }

            ContextTypoNetwork.logger.debug("Propagation via chaînes de tournois");
            //Activation des connections SoM
            for (Fanal fActivated : activatedFanals) {
                activationTournamentChainsConnections(fActivated, g);
            }
        }

        // PREMIER FILTRAGE
        ContextTypoNetwork.logger.debug("Premier filtrage");
        LinkedList<Fanal> fanalsList;
        for (int i = 0; i < word.length(); i++) {
            fanalsList = new LinkedList<>();
            if (FuzzyNetwork.FLOU2FLOU) {
                for (MacroFanal mFanal : g.getCluster(i).getMacroFanalsList()) {
                    fanalsList.addAll(mFanal.getListFanaux());
                }
                fanalsList = this.thresholdingFilter(fanalsList, net.FANALS_PER_CLIQUE - nErasures - suppleness, false);

            } else {
                fanalsList = this.localWinnersTakeAll(g.getCluster(i).getFanalsList(), false);
            }
            lstWinners.addAll(fanalsList);
        }
        bestScoresBottomUp.set(0, lstWinners);

        // ITERATIONS DE DECODAGE
        if (FuzzyDecoder.USE_FUZZY_BOOSTING) {
            if (FuzzyDecoder.N_ITERATIONS_BEFORE_BOOSTING) {
                numberFanalsBefore = 0;
                numberFanalsAfter = lstWinners.size();
                while (numberFanalsBefore != numberFanalsAfter) {
                    resetScores(0);
                    for (Fanal f : bestScoresBottomUp.get(0)) {
                        //                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          f.setScore(f.getScore()+GAMA);
                        activationTournamentChainsConnections(f, g);
                    }

                    lstWinners = this.thresholdingFilter(bestScoresBottomUp.get(0), net.FANALS_PER_CLIQUE - nErasures - suppleness, false);

                    bestScoresBottomUp.set(0, lstWinners);
                    numberFanalsBefore = numberFanalsAfter;
                    numberFanalsAfter = lstWinners.size();
                }
            }
            Fanal fanalBoosting;
            LinkedList<Fanal> lstBoosting = new LinkedList<>(lstWinners);
            HashSet<Fanal> lst = new HashSet<>();
            LinkedList<LinkedList<Fanal>> globalWinners;
            HashMap<Cluster, String> mapCluster = new HashMap<>();

            // Si effacement, on supprime les fanaux issus du cluster associé à la lettre effacée de la liste des fanaux à booster 
            if (nErasures != 0) {
                for (Fanal f : lstWinners) {
                    for (int i = 0; i < net.NUMBER_CLUSTERS; i++) {
                        if (g.getCluster(i).getMacroFanalsList().contains(((FuzzyFanal) f).getMacroFanal()) && ((FuzzyFanal) f).getMacroFanal().getLetter().equals(ERASURE_SYMBOL)) {
                            lstBoosting.remove(f);
                        }
                    }
                }
            }
            LinkedList<Fanal> lstActives = new LinkedList<>(lstWinners);
            boolean valCluster = false;
            boolean valClusterGlobal = false;
            lstWinners = new LinkedList<>();
            globalWinners = new LinkedList<>();
            int nValids = 0;
            while (nValids < FuzzyDecoder.MAX_BOOSTING && lstBoosting.size() > 0 && (!valClusterGlobal || FuzzyDecoder.SEVERALS_BOOSTING)) {
                // Réinitialisation de la liste des fanaux gagnants
                lstWinners = new LinkedList<>();
                // Réinitialisation des scores
                resetScores(0);
                    // Tirage aléatoire d'un fanal gagnant lors de l'itération précédente pour le boosting
                fanalBoosting = FuzzyLevel.pickRandomFanal(lstBoosting);
                lstBoosting.remove(fanalBoosting);
                //System.out.println(lstBoosting.size());
                ContextTypoNetwork.logger.debug("Fanal booste: " + fanalBoosting.getFanalName() + " :" + lstBoosting.size() + " :" + ((FuzzyFanal) fanalBoosting).getMacroFanal().getLetter());

                // Activation des connexions du fanal boosté
                activationTournamentChainsConnections(fanalBoosting, g);
                // Filtrage sur les gagnants
                lstWinners.addAll(this.localWinnersTakeAll(lstActives, false));

                numberFanalsBefore = 0;
                numberFanalsAfter = lstWinners.size();

                bestScoresBottomUp.set(0, lstWinners);
                while (numberFanalsBefore != numberFanalsAfter) {

                    resetScores(0);
                    for (Fanal f : bestScoresBottomUp.get(0)) {
                        activationTournamentChainsConnections(f, g);
                    }

                    lstWinners = this.thresholdingFilter(bestScoresBottomUp.get(0), net.FANALS_PER_CLIQUE - nErasures, false);

                    bestScoresBottomUp.set(0, lstWinners);
                    numberFanalsBefore = numberFanalsAfter;
                    numberFanalsAfter = lstWinners.size();
                }
                numberFanalsBefore = 0;
                numberFanalsAfter = lstWinners.size();
                while (numberFanalsBefore != numberFanalsAfter) {
                    lstWinners = new LinkedList<>();
                    resetScores(0);
                    for (Fanal f : bestScoresBottomUp.get(0)) {
                        f.setScore(f.getScore() + GAMA);
                        activationTournamentChainsConnections(f, g);
                    }//System.out.println("Cluster: "+i+" Fanaux: "+lstFanaux.size());

                    for (int i = 0; i < net.NUMBER_CLUSTERS; i++) {
                        lstWinners.addAll(this.localWinnersTakeAll(g.getCluster(i).getFanalsList(), false));
                    }
                    bestScoresBottomUp.set(0, lstWinners);
                    ContextTypoNetwork.logger.debug("Nombre de fanaux: " + lstWinners.size());
                    numberFanalsBefore = numberFanalsAfter;
                    numberFanalsAfter = lstWinners.size();
                }
                //Vérification si tous les clusteurs sont representés
                valCluster = clusterValidation(lstWinners);
                // Il cree un effet mémoire pour valide cluster global
                if (valCluster) {
                    globalWinners.add(lstWinners);
                    valClusterGlobal = true;
                    nValids++;
                }
            }
            if (!valClusterGlobal && windowSize < net.NUMBER_CLUSTERS) {
                recognizePatternBottomUpDecoding(sourceWord, windowSize + 1, phonemeUsage, suppleness);
                return;
            } else {
                if (!valClusterGlobal && suppleness < FuzzyDecoder.FILTER_SUPPLENESS) {
                    recognizePatternBottomUpDecoding(sourceWord, 0, phonemeUsage, suppleness + 1);
                    return;
                } else {
                    LinkedList<LinkedList<Fanal>> lstResult = null;
                    if (FuzzyNetwork.FLOU2FLOU) {
                        lstResult = new LinkedList<>();
                        this.seqsPropagBottomUpFlous.put(sourceWord, globalWinners);
                        for (int i = 0; i < globalWinners.size(); i++) {
                            lstWinners = globalWinners.get(i);
                                // Il garde les fanaux flous gagnants
                            // Récupération des macrofanaux gagnants à la fin du décodage
                            lst = new HashSet<>();
                            mapCluster.clear();
                            for (Fanal f : lstWinners) {
                                lst.add(((FuzzyFanal) f).getMacroFanal());

                            }
                            lstWinners = new LinkedList<>();
                            for (Fanal f : lst) {
                                lstWinners.add(f);
                                mapCluster.put(((FuzzyFanal) f).getCluster(), ((FuzzyFanal) f).getLetter());
                            }
                            lstResult.add(lstWinners);
                            ContextTypoNetwork.logger.debug("Mot trouvee: ");
                            for (int j = 0; j < net.NUMBER_CLUSTERS; j++) {
                                ContextTypoNetwork.logger.debug(mapCluster.get(g.getCluster(j)));
                            }
                            ContextTypoNetwork.logger.debug("Nombre de macrofanaux : " + lstWinners.size());
                        }

                    } else {
                        ContextTypoNetwork.logger.debug("Nombre de fanaux : " + lstWinners.size());
                    }

                    seqsPropagBottomUp.put(word, lstResult);
                    return;
                }
            }

        } else {
            numberFanalsBefore = 0;
            numberFanalsAfter = lstWinners.size();
            while (numberFanalsBefore != numberFanalsAfter) {
                lstWinners = new LinkedList<>();
                resetScores(0);
                for (Fanal f : bestScoresBottomUp.get(0)) {
                    //f.setScore(f.getScore()+GAMA);
                    activationTournamentChainsConnections(f, g);
                }

                for (int i = 0; i < net.NUMBER_CLUSTERS; i++) {
                    lstWinners.addAll(this.localWinnersTakeAll(g.getCluster(i).getFanalsList(), false));
                }
                bestScoresBottomUp.set(0, lstWinners);
                ContextTypoNetwork.logger.debug("Nombre de fanaux: " + lstWinners.size());
                numberFanalsBefore = numberFanalsAfter;
                numberFanalsAfter = lstWinners.size();
            }
        }
        LinkedList<LinkedList<Fanal>> auxList = new LinkedList<>();
        auxList.add(fanalsList);
        seqsPropagBottomUp.put(word, auxList);

    }

    public LinkedList<LinkedList<Fanal>> getWinnersSeqBottomUp(String mot) {
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = mot.length();
            for (int i = 0; i < this.net.NUMBER_CLUSTERS - taille; i++) {
                mot = mot + PADDING_SYMBOL;
            }
        }
        return seqsPropagBottomUp.get(mot);
    }

    public LinkedList<LinkedList<Fanal>> getWinnersPatternsFuzzyDecoding(String word) {
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int size = word.length();
            for (int i = 0; i < this.net.NUMBER_CLUSTERS - size; i++) {
                word = word + PADDING_SYMBOL;
            }
        }
        return this.seqsPropagBottomUpFlous.get(word);
    }

    public boolean clusterValidation(LinkedList<Fanal> lst) {
        LinkedList lstCluster = new LinkedList<>();
        HashSet<MacroFanal> lst_macro = new HashSet<>();
        for (Fanal f : lst) {
            lst_macro.add(((FuzzyFanal) f).getMacroFanal());
        }
        for (MacroFanal f : lst_macro) {
            if (!lstCluster.contains(f.getCluster())) {
                lstCluster.add(f.getCluster());

            }
        }
        return lstCluster.size() == net.NUMBER_CLUSTERS && lst_macro.size() == net.FANALS_PER_CLIQUE;

    }

    public void activationFanalConnections(Fanal f, int h) {
        Fanal l;
        Edge a;
        FuzzyLevel n = (FuzzyLevel) net.getLevelsList().get(h);
        Edge[] listEdges = n.getGraph().getEdgesList(f);
        for (int i = 0; i < listEdges.length; i++) {
            if (listEdges[i] != null) {
                listEdges[i].setActive(true);
                if (listEdges[i].getSourceFanal().equals(f)) {
                    l = listEdges[i].getDestinationFanal();
                    a = n.getGraph().getEdge(l, f);
                } else {
                    l = listEdges[i].getSourceFanal();
                    a = n.getGraph().getEdge(f, l);
                }
                if (a != null) {
                    a.setActive(true);
                }
                l.setScore(l.getScore() + 1);
            }
        }
    }

    public void activationFanalConnections(Fanal f, int h, int score) {
        Fanal l;
        Edge a;
        FuzzyLevel n = (FuzzyLevel) net.getLevelsList().get(h);
        Edge[] listEdges = n.getGraph().getEdgesList(f);
        for (int i = 0; i < listEdges.length; i++) {
            if (listEdges[i] != null) {
                listEdges[i].setActive(true);
                if (listEdges[i].getSourceFanal().equals(f)) {
                    l = listEdges[i].getDestinationFanal();
                    a = n.getGraph().getEdge(l, f);
                } else {
                    l = listEdges[i].getSourceFanal();
                    a = n.getGraph().getEdge(f, l);
                }
                if (a != null) {
                    a.setActive(true);
                }
                l.setScore(l.getScore() + score);
            }
        }
    }

    private void activationMacroFanalConnections(MacroFanal fMacro, Graph g, int score) {
        for (Fanal fOrig : fMacro.getListFanaux()) {
            Fanal fDest;
            Edge[] listeArc = g.getEdgesList(fOrig);
            HashMap<Cluster, Integer> listClusters;
            HashMap<Fanal, HashMap<Cluster, Integer>> mapCluster;
            fOrig.setScore(fOrig.getScore() + GAMA_1);
            //fOrig.setScore(fOrig.getScore() + score);
            for (int i = 0; i < listeArc.length; i++) {
                if (listeArc[i] != null) {
                    if (listeArc[i].getSourceFanal().equals(fOrig)) {
                        fDest = listeArc[i].getDestinationFanal();
                        listeArc[i].setActive(true);
                        // Si il n'existe pas d'activation provenant du cluster de f
                        if (!clusterActivationSoM(fDest, ((FuzzyFanal) fOrig).getMacroFanal().getCluster(), 0)) {
                            fDest.setScore(fDest.getScore() + 1);
                            // S'il n'y a pas de liste initialisé des clusters pour le macro fanal de destination
                            if (!activationsListClusters.get(0).containsKey(fDest)) {
                                listClusters = new HashMap<>();
                                listClusters.put(((FuzzyFanal) fOrig).getMacroFanal().getCluster(), score);
                                mapCluster = activationsListClusters.get(0);
                                mapCluster.put(fDest, listClusters);
                                activationsListClusters.set(0, mapCluster);
                            } else {
                                // Il ajoute le cluster
                                listClusters = new HashMap<>(activationsListClusters.get(0).get(fDest));
                                listClusters.put(((FuzzyFanal) fOrig).getMacroFanal().getCluster(), score);
                                mapCluster = activationsListClusters.get(0);
                                mapCluster.put(fDest, listClusters);
                                activationsListClusters.set(0, mapCluster);
                            }
                            // Si il existe deja une activation provenant du meme fanal
                        } else {
                            // Il garde le max de activations SoS
                            if (activationsListClusters.get(0).get(fDest).get(((FuzzyFanal) fOrig).getMacroFanal().getCluster()) < score) {
                                activationsListClusters.get(0).get(fDest).put(((FuzzyFanal) fOrig).getMacroFanal().getCluster(), score);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean clusterActivationSoM(Fanal fDest, Cluster c, int h) {
        if (activationsListClusters.get(h).containsKey(fDest)) {
            return activationsListClusters.get(h).get(fDest).containsKey(c);
        } else {
            return false;
        }
    }

    // Cette methode réalise le SoM
    private void activationTournamentChainsConnections(Fanal fSource, Graph g) {
        Fanal fDest;
        Edge[] listEdges = g.getEdgesList(fSource);
        HashMap<Cluster, Integer> listClusters;
        HashMap<Fanal, HashMap<Cluster, Integer>> mapCluster;
        fSource.setScore(fSource.getScore() + GAMA_1);
        for (int i = 0; i < listEdges.length; i++) {
            if (listEdges[i] != null) {
                if (listEdges[i].getSourceFanal().equals(fSource)) {
                    fDest = listEdges[i].getDestinationFanal();
                    listEdges[i].setActive(true);
                    // Si il n'existe pas d'activation provenant du cluster de f
                    if (!clusterActivationSoM(fDest, ((FuzzyFanal) fSource).getCluster(), 0)) {
                        fDest.setScore(fDest.getScore() + 1);
                        if (!activationsListClusters.get(0).containsKey(fDest)) {
                            listClusters = new HashMap<>();
                            listClusters.put(((FuzzyFanal) fSource).getCluster(), 1);
                            mapCluster = activationsListClusters.get(0);
                            mapCluster.put(fDest, listClusters);
                            activationsListClusters.set(0, mapCluster);
                        } else {
                            listClusters = new HashMap<>(activationsListClusters.get(0).get(fDest));
                            listClusters.put(((FuzzyFanal) fSource).getCluster(), 1);
                            mapCluster = activationsListClusters.get(0);
                            mapCluster.put(fDest, listClusters);
                            activationsListClusters.set(0, mapCluster);
                        }
                    }
                }
            }
        }
    }

    public void activateInferiorFanals(Fanal l, int h) {
        //Si il y a des connexions inferieurs, il les active 
        // Ensuite, il active les connexions des fanaux inferieurs
        if (!l.getInferiorFanals().isEmpty()) {
            for (Fanal t : l.getInferiorFanals()) {

                t.setScore(t.getScore() + 1);
            }
        }
    }

    public void activateSuperiorFanals(Fanal l, int h) {
        //Si il y a des connexions superieures, il les active 
        // Ensuite, il active les connexions des fanaux superieures
        if (!l.getSuperiorFanals().isEmpty()) {
            for (Fanal t : l.getSuperiorFanals()) {
                t.setScore(t.getScore() + 1);
            }
        }
    }

    public void activateHyperSuperiorFanals(Fanal l, int h) {
        //Si il y a des connexions superieures, il les active 
        // Ensuite, il active les connexions des fanaux superieures
        if (!l.getSuperiorHyperFanals().isEmpty()) {
            for (Fanal t : l.getSuperiorHyperFanals()) {
                t.setScore(t.getScore() + 1);
            }
        }
    }

    public void resetScores(int h) {
        FuzzyLevel n;
        if (h == -1) {
            for (int i = 0; i < FuzzyNetwork.hMax; i++) {
                n = (FuzzyLevel) net.getLevelsList().get(i);
                for (Fanal f : n.getGraph().getAllNodes()) {
                    f.setScore(0);
                }
                for (Fanal f : ((FuzzyGraph) n.getGraph()).getMacroFanalsList()) {
                    f.setScore(0);
                }
            }
            return;
        } else {
            n = (FuzzyLevel) net.getLevelsList().get(h);
            for (Fanal f : n.getGraph().getAllNodes()) {
                f.setScore(0);
                // Habiliter ligne quand utiliser le SOM
                //this.desactiverConnectionsFanal(f,h);
            }
            for (Fanal f : ((FuzzyGraph) n.getGraph()).getMacroFanalsList()) {
                f.setScore(0);
            }
        }
        // Il utilise le SoM
        activationsListClusters.set(0, new HashMap<Fanal, HashMap<Cluster, Integer>>());
    }

    public void resetShortMemory() {
        seqsPropagBottomUp = new HashMap<>();
        bestScoresBottomUpLast = new LinkedList<>();
        for (int i = 0; i < FuzzyNetwork.hMax; i++) {
            this.bestScoresBottomUp.add(i, new LinkedList<Fanal>());
            seqsPropagTopDown.add(i, new HashMap<Integer, LinkedList<Fanal>>());
            seqsTopDownCounter[i] = 0;
            // Il utilise le SoM
            activationsListClusters.add(i, new HashMap<Fanal, HashMap<Cluster, Integer>>());

        }
    }

    public LinkedList<Fanal> loserKickOut(LinkedList<Fanal> lst_orig, int h) {
        LinkedList<Fanal> lst = new LinkedList<>();
        FuzzyLevel n = (FuzzyLevel) net.getLevelsList().get(h);
        int minScore = net.FANALS_PER_CLUSTER * net.NUMBER_CLUSTERS;
        int maxScore = -1;
        for (Fanal f : lst_orig) {
            if (f.getScore() < minScore) {
                minScore = f.getScore();
            }
            if (f.getScore() > maxScore) {
                maxScore = f.getScore();
            }
        }
        ContextTypoNetwork.logger.debug("LoserKickOut - Score max: " + maxScore);
        ContextTypoNetwork.logger.debug("LoserKickOut - Score min: " + minScore);
        for (Fanal f : lst_orig) {
            if (f.getScore() != minScore) {
                lst.add(f);
            }
        }
        ContextTypoNetwork.logger.debug("LoserKickOut - Nombre fanaux après: " + lst.size());
        return lst;
    }

    // Il prend les premiers numElements
    public LinkedList<Fanal> globalWinnersTakeAll(int h, int numElements) {

        LinkedList<Fanal> lstWinner = new LinkedList<>();
        FuzzyLevel n = (FuzzyLevel) net.getLevelsList().get(h);
        LinkedList<Fanal>[] vectorScores;
        vectorScores = (LinkedList<Fanal>[]) new LinkedList<?>[net.FANALS_PER_CLUSTER * net.NUMBER_CLUSTERS];
        for (int i = 0; i < vectorScores.length; i++) {
            vectorScores[i] = new LinkedList<>();
        }
        int maxScore = 0;
        for (Fanal f : n.getGraph().getAllNodes()) {
            vectorScores[f.getScore()].addLast(f);
            if (f.getScore() > maxScore) {
                maxScore = f.getScore();
            }
        }
        int i = maxScore;
        int c = 0;
        while ((c < numElements) && (i > 0)) {
            if (!vectorScores[i].isEmpty()) {
                for (Fanal f : vectorScores[i]) {
                    lstWinner.addLast(f);
                    c++;
                }
            }
            i--;
        }
        Fanal f;
        Fanal f_last;
        f_last = lstWinner.getLast();
        Iterator<Fanal> it = vectorScores[f_last.getScore()].descendingIterator();
        while (it.hasNext()) {
            f = it.next();
            if (f.equals(f_last)) {
                break;
            } else {
                lstWinner.addLast(f);
            }
        }
        return lstWinner;
    }

    public HashSet<MacroFanal> thresholdLateralPropagation() {
        FuzzyGraph g = (FuzzyGraph) net.getLevelsList().get(0).getGraph();
        HashSet<MacroFanal> result = new HashSet<>();
        LinkedList<MacroFanal> winnersCluster = new LinkedList<>();
        int scoreMax, scoreFanal;
        for (int iClust = 0; iClust < net.NUMBER_CLUSTERS; iClust++) {
            scoreMax = 0;
            for (MacroFanal mfCluster : g.getCluster(iClust).getMacroFanalsList()) {
                if (InterfaceNetwork.fanalScoreMap.containsKey(mfCluster)) {
                    scoreFanal = InterfaceNetwork.fanalScoreMap.get(mfCluster);
                    if (scoreFanal > scoreMax) {
                        scoreMax = scoreFanal;
                    }
                }
            }
            for (MacroFanal mfCluster : g.getCluster(iClust).getMacroFanalsList()) {
                if (InterfaceNetwork.fanalScoreMap.containsKey(mfCluster)) {
                    scoreFanal = InterfaceNetwork.fanalScoreMap.get(mfCluster);
                    if (scoreFanal == scoreMax) {
                        winnersCluster.add(mfCluster);
                    }
                }
            }
            result.addAll(winnersCluster);
        }
        return result;
    }

    private LinkedList<Fanal> thresholdingFilter(LinkedList<Fanal> lst, int minScore, boolean secondIt) {
        LinkedList<Fanal> listeWinners = new LinkedList<>();
        int scoreMax = 0;
        int scoreFanal;
        for (Fanal f : lst) {
            if (f.getScore() >= minScore) {
                listeWinners.add(f);
            }
        }
        if (secondIt) {
            scoreMax = 0;
            LinkedList<Fanal> listeWinnersAux = new LinkedList<>(listeWinners);
            for (Fanal f : listeWinnersAux) {
                scoreFanal = 0;
                if (activationsListClusters.get(0).containsKey(f)) {
                    for (Cluster c : activationsListClusters.get(0).get(f).keySet()) {
                        scoreFanal += activationsListClusters.get(0).get(f).get(c);
                    }
                    ContextTypoNetwork.logger.debug("Second iteration LWsTA: " + f + " Lettre: " + ((FuzzyFanal) f).getLetter() + " Score: " + scoreFanal);
                    if (scoreFanal > scoreMax) {
                        listeWinners = new LinkedList<>();
                        listeWinners.add(f);
                        scoreMax = scoreFanal;
                    } else {
                        if (scoreFanal == scoreMax) {
                            listeWinners.add(f);
                        }
                    }
                }

            }
        }
        return listeWinners;
    }

    private LinkedList<Fanal> localWinnersTakeAll(Collection<? extends Fanal> FanauxCluster, boolean secondIt) {
        int scoreMax = 0;
        int scoreFanal;
        LinkedList<Fanal> winnersList = new LinkedList<>();
        for (Fanal f : FanauxCluster) {
            if (f.getScore() > scoreMax) {
                scoreMax = f.getScore();
            }
        }
        for (Fanal f : FanauxCluster) {
            if (f.getScore() == scoreMax) {
                winnersList.add(f);
            }
        }

        if (secondIt) {
            scoreMax = 0;
            LinkedList<Fanal> listeWinnersAux = new LinkedList<>(winnersList);
            for (Fanal f : listeWinnersAux) {
                scoreFanal = 0;
                for (Cluster c : activationsListClusters.get(0).get(f).keySet()) {
                    scoreFanal += activationsListClusters.get(0).get(f).get(c);
                }
                ContextTypoNetwork.logger.debug("Second iteration LWsTA: " + f + " Lettre: " + ((FuzzyFanal) f).getLetter() + " Score: " + scoreFanal);
                if (scoreFanal > scoreMax) {
                    winnersList = new LinkedList<>();
                    winnersList.add(f);
                    scoreMax = scoreFanal;
                } else {
                    if (scoreFanal == scoreMax) {
                        winnersList.add(f);
                    }
                }
            }
        }

        return winnersList;
    }

}
