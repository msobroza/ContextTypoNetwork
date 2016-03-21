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
import model.FanalFlous;
import model.MacroFanal;

public class FuzzyDecoder extends Decoder implements LetterInformation {

    protected final int GWsTA_vertical_1;
    protected final int GWsTA_vertical_2;
    protected final int GWsTA_horizontal;

    public static final int[] FENETRE_CIRCULAIRE = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1};
    public static final boolean BOOSTING_FLOUS = true;
    public static final boolean N_ITERATIONS_AVANT_BOOSTING = true;
    public static final boolean ITERATION_UNIQUE = false;
    public static final boolean PLUSIEURS_BOOSTING = true;
    public static final int SOUPLESSE_FILTRAGE = 2;
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
    protected LinkedList<HashMap<Fanal, HashMap<Cluster, Integer>>> activationsListeClusters;
    // Contient les sequences Top-Down
    protected LinkedList<HashMap<Integer, LinkedList<Fanal>>> seqsPropagTopDown;
    // Il permet de savoir combien de sequences il y a pour chaque niveau au moment du decodage
    protected int[] seqsTopDownCounter;

    private final FuzzyNetwork r;

    public FuzzyDecoder(FuzzyNetwork r) {
        MAX_BOOSTING = r.FANALS_PER_CLIQUE * 3;
        //MAX_BOOSTING = 3;
        GWsTA_vertical_1 = r.FANALS_PER_CLIQUE;
        GWsTA_vertical_2 = r.FANALS_PER_CLIQUE * 2;
        GWsTA_horizontal = r.FANALS_PER_CLIQUE;

        this.r = r;
        seqsPropagBottomUp = new HashMap<>();
        seqsPropagBottomUpFlous = new HashMap<>();
        bestScoresBottomUp = new LinkedList<>();
        seqsTopDownCounter = new int[FuzzyNetwork.hMax];
        seqsPropagTopDown = new LinkedList<>();
        bestScoresBottomUpLast = new LinkedList<>();
        activationsListeClusters = new LinkedList<>();

        for (int i = 0; i < FuzzyNetwork.hMax; i++) {
            bestScoresBottomUp.add(i, new LinkedList<Fanal>());
            seqsPropagTopDown.add(i, new HashMap<Integer, LinkedList<Fanal>>());
            seqsTopDownCounter[i] = 0;
            // Il utilise le SoM
            activationsListeClusters.add(i, new HashMap<Fanal, HashMap<Cluster, Integer>>());
        }

    }

    public double verifieDecodageBottomUp(String mot, String motRecherche) {
        Clique clique;
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = mot.length();
            for (int i = 0; i < this.r.NUMBER_CLUSTERS - taille; i++) {
                mot = mot + "*";
            }
            for (int i = 0; i < this.r.NUMBER_CLUSTERS - motRecherche.length(); i++) {
                motRecherche = motRecherche + "*";
            }
        }
        clique = r.getLevelsList().get(0).getWordClique(motRecherche);
        if (clique == null) {
            return -1.0;
        }
        int i;
        int iMax = 0;
        for (LinkedList<Fanal> lstClique : seqsPropagBottomUp.get(mot)) {
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

        return ((double) iMax) / motRecherche.length();
    }

    public LinkedList<LinkedList<Fanal>> getWinnersBottomUp(String mot) {
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = mot.length();
            for (int i = 0; i < this.r.NUMBER_CLUSTERS - taille; i++) {
                mot = mot + "*";
            }
        }
        return seqsPropagBottomUp.get(mot);
    }

    public String getMot(LinkedList<Fanal> lstFanaux) {
        String mot = "";
        HashMap<Cluster, String> mapCluster = new HashMap<>();
        for (Fanal f : lstFanaux) {
            mapCluster.put(((MacroFanal) f).getCluster(), ((MacroFanal) f).getLetter());
        }
        FuzzyGraph g = (FuzzyGraph) r.getLevelsList().get(0).getGraph();
        for (int i = 0; i < r.NUMBER_CLUSTERS; i++) {
            mot += mapCluster.get(g.getCluster(i));
        }
        return mot;
    }

    // ------------- Decodage Bottom-up-------------------
    public void recognizePatternBottomUpDecoding(String mot, int taille_fenetre, boolean utilisationPhoneme, int souplesse) {
        String lettre;
        List<String> phonListe = null;
        FuzzyGraph g = (FuzzyGraph) r.getLevelsList().get(0).getGraph();
        LinkedList<MacroFanal> activesReseauDroite = new LinkedList<>();
        // INITIALISATION
        // Initialisation des scores des fanaux
        remiseZero(0);
        // Initialisation du décodage du réseau
        remiseMemo();

        LinkedList<Fanal> lstWinners = new LinkedList<>();
        Fanal fanal_lettre;
        String motOrig = mot;
        int nombreFanauxAvant, nombreFanauxApres;
        int nEffac = 0;
        ContextTypoNetwork.logger.debug("Première activation");
        System.out.println("Info: " + mot);
        // PREMIERE ACTIVATION
        if (FuzzyNetwork.FLOU2FLOU) {
            boolean conditionInsertion = false;
            HashSet<MacroFanal> fanauxActivants = new HashSet<>();
            HashMap<MacroFanal, Integer> scoreFanaux = new HashMap<>();
            // Recherche la position d'une lettre possiblement effacée
            for (int i = 0; i < mot.length(); i++) {
                lettre = mot.substring(i, i + 1);
                if (lettre.equals(ERASURE_CHAR)) {
                    nEffac++;
                }
            }

            // Recherche la position d'une lettre possiblement inserée
            if (utilisationPhoneme) {

                phonListe = PhonemeRules.phonemesLIAToList(mot);
                activesReseauDroite.addAll(filtragePropagationLaterale());
                for (MacroFanal mf : activesReseauDroite) {
                    System.out.println(mf + " -> " + mf.getLetter());
                }
                if (phonListe.size() > r.NUMBER_CLUSTERS) {
                    conditionInsertion = true;
                    int offsetPos;
                    ArrayList<Integer> posDeletions = new ArrayList<>();
                    ArrayList<Integer> posClusters = new ArrayList<>();
                    ArrayList<String> lettresDeletes = new ArrayList<>();
                    ArrayList<String> motAvecDeletions;
                    offsetPos = NetworkControl.getRandomPositionWord(phonListe) + 1;
                    int pos;
                    for (int j = 0; j < phonListe.size() - r.NUMBER_CLUSTERS; j++) {
                        pos = ((offsetPos + 2 * j) % (mot.length() - 2)) + 1;
                        posDeletions.add(pos);
                    }
                    Collections.sort(posDeletions);
                    offsetPos = 0;
                    motAvecDeletions = new ArrayList<>(phonListe);
                    for (int j = 0; j < phonListe.size() - r.NUMBER_CLUSTERS; j++) {
                        lettresDeletes.add(motAvecDeletions.get(posDeletions.get(j) - offsetPos));
                        motAvecDeletions.remove(posDeletions.get(j) - offsetPos);
                        posClusters.add(posDeletions.get(j) - offsetPos - 1);
                        offsetPos++;
                    }
                    // Sélection de l'ensemble des fanaux activants (deletes)
                    ContextTypoNetwork.logger.debug("Activation macrofanaux - fanaux déletés");
                    int iC;
                    for (int j = 0; j < phonListe.size() - r.NUMBER_CLUSTERS; j++) {
                        lettre = lettresDeletes.get(j);
                        iC = posClusters.get(j);
                        int iFenetre = 0;
                        for (int iCluster = -taille_fenetre; iCluster <= taille_fenetre; iCluster++) {
                            MacroFanal mf = g.getCluster(iCluster + iC).getMacroFanal(lettre);
                            ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                            fanauxActivants.add(mf);
                            if (!scoreFanaux.containsKey(mf)) {
                                /*if (activesReseauDroite.contains(mf)) {
                                 scoreFanaux.put(mf, DecodageFlous.FENETRE_CIRCULAIRE[iFenetre] + 1);
                                 } else {*/
                                scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                                // }
                            } else {
                                // SoM
                               /* if (activesReseauDroite.contains(mf)) {
                                 scoreFanaux.put(mf, DecodageFlous.FENETRE_CIRCULAIRE[iFenetre] + 1);
                                 } */
                                if (scoreFanaux.get(mf) < FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]) {
                                    scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                                }
                            }
                            iFenetre++;
                        }
                    }
                    phonListe = new ArrayList<>(motAvecDeletions);
                }

                // Sélection de l'ensemble des fanaux activants
                ContextTypoNetwork.logger.debug("Activation macrofanaux");
                for (int i = 0; i < phonListe.size(); i++) {
                    lettre = phonListe.get(i);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    int iFenetre = 0;
                    for (int iCluster = -taille_fenetre; iCluster <= taille_fenetre; iCluster++) {
                        MacroFanal mf = g.getCluster(iCluster + i).getMacroFanal(lettre);
                        ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                        fanauxActivants.add(mf);
                        if (!scoreFanaux.containsKey(mf)) {

                            scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                        } else {
                            // SoM
                            if (scoreFanaux.get(mf) < FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]) {
                                scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                            }
                        }
                        iFenetre++;
                    }
                }
            } else {
                if (mot.length() > r.NUMBER_CLUSTERS) {
                    conditionInsertion = true;
                    int offsetPos;
                    ArrayList<Integer> posDeletions = new ArrayList<>();
                    ArrayList<Integer> posClusters = new ArrayList<>();
                    ArrayList<String> lettresDeletes = new ArrayList<>();
                    String motAvecDeletions;
                    offsetPos = NetworkControl.getRandomPositionWord(mot.substring(1, mot.length() - 1)) + 1;
                    int pos;
                    for (int j = 0; j < mot.length() - r.NUMBER_CLUSTERS; j++) {
                        pos = ((offsetPos + 2 * j) % (mot.length() - 2)) + 1;
                        posDeletions.add(pos);
                    }
                    Collections.sort(posDeletions);
                    offsetPos = 0;
                    motAvecDeletions = mot;
                    for (int j = 0; j < mot.length() - r.NUMBER_CLUSTERS; j++) {
                        lettresDeletes.add(motAvecDeletions.substring(posDeletions.get(j) - offsetPos, posDeletions.get(j) - offsetPos + 1));
                        motAvecDeletions = motAvecDeletions.substring(0, posDeletions.get(j) - offsetPos) + motAvecDeletions.substring(posDeletions.get(j) - offsetPos + 1, motAvecDeletions.length());
                        posClusters.add(posDeletions.get(j) - offsetPos - 1);
                        offsetPos++;
                    }
                    // Sélection de l'ensemble des fanaux activants (deletes)
                    ContextTypoNetwork.logger.debug("Activation macrofanaux - fanaux déletés");
                    int iC;
                    for (int j = 0; j < mot.length() - r.NUMBER_CLUSTERS; j++) {
                        lettre = lettresDeletes.get(j);
                        iC = posClusters.get(j);
                        int iFenetre = 0;
                        for (int iCluster = -taille_fenetre; iCluster <= taille_fenetre; iCluster++) {
                            MacroFanal mf = g.getCluster(iCluster + iC).getMacroFanal(lettre);
                            ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                            fanauxActivants.add(mf);
                            if (!scoreFanaux.containsKey(mf)) {
                                scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                            } else {
                                // SoM
                                if (scoreFanaux.get(mf) < FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]) {
                                    scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                                }
                            }
                            iFenetre++;
                        }
                    }
                    mot = motAvecDeletions;
                }

                // Sélection de l'ensemble des fanaux activants
                ContextTypoNetwork.logger.debug("Activation macrofanaux");

                for (int i = 0; i < mot.length(); i++) {
                    lettre = mot.substring(i, i + 1);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    int iFenetre = 0;
                    for (int iCluster = -taille_fenetre; iCluster <= taille_fenetre; iCluster++) {
                        MacroFanal mf = g.getCluster(iCluster + i).getMacroFanal(lettre);
                        ContextTypoNetwork.logger.debug("MF : " + mf + ", Lettre : " + mf.getLetter());
                        fanauxActivants.add(mf);
                        if (!scoreFanaux.containsKey(mf)) {
                            scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                        } else {
                            // SoM
                            if (scoreFanaux.get(mf) < FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]) {
                                scoreFanaux.put(mf, FuzzyDecoder.FENETRE_CIRCULAIRE[iFenetre]);
                            }
                        }
                        iFenetre++;
                    }
                }
            }
            ContextTypoNetwork.logger.debug("Propagation via chaînes de tournois");
            //Activation des connections SoM
            ContextTypoNetwork.logger.debug("Nombre de fanaux Activés: " + fanauxActivants.size());
            for (MacroFanal fActivant : fanauxActivants) {

                activerConnectionsMacroFanal(fActivant, g, scoreFanaux.get(fActivant));
            }
        } else {
            ContextTypoNetwork.logger.debug("Activation fanaux");
            HashSet<Fanal> fanauxActivants = new HashSet<>();
            if (utilisationPhoneme) {
                for (int i = 0; i < phonListe.size(); i++) {
                    lettre = phonListe.get(i);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    for (int iCluster = -taille_fenetre; iCluster <= taille_fenetre; iCluster++) {
                        fanauxActivants.add(g.getCluster(iCluster + i).getFanal(lettre));
                    }
                }
            } else {
                for (int i = 0; i < mot.length(); i++) {
                    lettre = mot.substring(i, i + 1);
                    // Cherche le fanal correspondant a la lettre en utilisant la fenetre circulaire
                    for (int iCluster = -taille_fenetre; iCluster <= taille_fenetre; iCluster++) {
                        fanauxActivants.add(g.getCluster(iCluster + i).getFanal(lettre));
                    }
                }
            }

            ContextTypoNetwork.logger.debug("Propagation via chaînes de tournois");
            //Activation des connections SoM
            for (Fanal fActivant : fanauxActivants) {
                activerConnectionsChaineTournois(fActivant, g);
            }
        }

        // PREMIER FILTRAGE
        ContextTypoNetwork.logger.debug("Premier filtrage");
        LinkedList<Fanal> lstFanaux;
        for (int i = 0; i < mot.length(); i++) {
            lstFanaux = new LinkedList<>();
            if (FuzzyNetwork.FLOU2FLOU) {
                for (MacroFanal mFanal : g.getCluster(i).getMacroFanalsList()) {
                    lstFanaux.addAll(mFanal.getListFanaux());
                }
                lstFanaux = this.thresholdingFilter(lstFanaux, r.FANALS_PER_CLIQUE - nEffac - souplesse, false);

            } else {
                lstFanaux = this.localWinnersTakeAll(g.getCluster(i).getFanalsList(), false);
            }
            lstWinners.addAll(lstFanaux);
        }
        bestScoresBottomUp.set(0, lstWinners);

        // ITERATIONS DE DECODAGE
        if (FuzzyDecoder.BOOSTING_FLOUS) {
            if (FuzzyDecoder.N_ITERATIONS_AVANT_BOOSTING) {
                nombreFanauxAvant = 0;
                nombreFanauxApres = lstWinners.size();
                while (nombreFanauxAvant != nombreFanauxApres) {
                    remiseZero(0);
                    for (Fanal f : bestScoresBottomUp.get(0)) {
                        //                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                          f.setScore(f.getScore()+GAMA);
                        activerConnectionsChaineTournois(f, g);
                    }

                    lstWinners = this.thresholdingFilter(bestScoresBottomUp.get(0), r.FANALS_PER_CLIQUE - nEffac - souplesse, false);

                    bestScoresBottomUp.set(0, lstWinners);
                    nombreFanauxAvant = nombreFanauxApres;
                    nombreFanauxApres = lstWinners.size();
                }
            }
            Fanal fanalBoosting;
            LinkedList<Fanal> lstBoosting = new LinkedList<>(lstWinners);
            HashSet<Fanal> lst = new HashSet<>();
            LinkedList<LinkedList<Fanal>> globalWinners;
            HashMap<Cluster, String> mapCluster = new HashMap<>();

            // Si effacement, on supprime les fanaux issus du cluster associé à la lettre effacée de la liste des fanaux à booster 
            if (nEffac != 0) {
                for (Fanal f : lstWinners) {
                    for (int i = 0; i < r.NUMBER_CLUSTERS; i++) {
                        if (g.getCluster(i).getMacroFanalsList().contains(((FanalFlous) f).getMacroFanal()) && ((FanalFlous) f).getMacroFanal().getLetter().equals(ERASURE_CHAR)) {
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
            int nValides = 0;
            while (nValides < FuzzyDecoder.MAX_BOOSTING && lstBoosting.size() > 0 && (!valClusterGlobal || FuzzyDecoder.PLUSIEURS_BOOSTING)) {
                // Réinitialisation de la liste des fanaux gagnants
                lstWinners = new LinkedList<>();
                // Réinitialisation des scores
                remiseZero(0);
                    // Tirage aléatoire d'un fanal gagnant lors de l'itération précédente pour le boosting
                //System.out.println("Taille liste boosting: "+lstBoosting.size());
                //System.out.println("Taille fenetre: "+taille_fenetre);
                fanalBoosting = FuzzyLevel.pickRandomFanal(lstBoosting);
                lstBoosting.remove(fanalBoosting);
                //System.out.println(lstBoosting.size());
                ContextTypoNetwork.logger.debug("Fanal booste: " + fanalBoosting.getFanalName() + " :" + lstBoosting.size() + " :" + ((FanalFlous) fanalBoosting).getMacroFanal().getLetter());

                // Activation des connexions du fanal boosté
                activerConnectionsChaineTournois(fanalBoosting, g);
                // Filtrage sur les gagnants
                lstWinners.addAll(this.localWinnersTakeAll(lstActives, false));

                nombreFanauxAvant = 0;
                nombreFanauxApres = lstWinners.size();

                bestScoresBottomUp.set(0, lstWinners);
                while (nombreFanauxAvant != nombreFanauxApres) {

                    remiseZero(0);
                    for (Fanal f : bestScoresBottomUp.get(0)) {
                        activerConnectionsChaineTournois(f, g);
                    }

                    lstWinners = this.thresholdingFilter(bestScoresBottomUp.get(0), r.FANALS_PER_CLIQUE - nEffac, false);

                    bestScoresBottomUp.set(0, lstWinners);
                    nombreFanauxAvant = nombreFanauxApres;
                    nombreFanauxApres = lstWinners.size();
                }
                nombreFanauxAvant = 0;
                nombreFanauxApres = lstWinners.size();
                while (nombreFanauxAvant != nombreFanauxApres) {
                    lstWinners = new LinkedList<>();
                    remiseZero(0);
                    for (Fanal f : bestScoresBottomUp.get(0)) {
                        f.setScore(f.getScore() + GAMA);
                        activerConnectionsChaineTournois(f, g);
                    }//System.out.println("Cluster: "+i+" Fanaux: "+lstFanaux.size());

                    for (int i = 0; i < r.NUMBER_CLUSTERS; i++) {
                        lstWinners.addAll(this.localWinnersTakeAll(g.getCluster(i).getFanalsList(), false));
                    }
                    bestScoresBottomUp.set(0, lstWinners);
                    ContextTypoNetwork.logger.debug("Nombre de fanaux: " + lstWinners.size());
                    nombreFanauxAvant = nombreFanauxApres;
                    nombreFanauxApres = lstWinners.size();
                }
                //Vérification si tous les clusteurs sont representés
                valCluster = valideClusters(lstWinners);
                // Il cree un effet mémoire pour valide cluster global
                if (valCluster) {
                    globalWinners.add(lstWinners);
                    valClusterGlobal = true;
                    nValides++;
                }
            }
            if (!valClusterGlobal && taille_fenetre < r.NUMBER_CLUSTERS) {
                recognizePatternBottomUpDecoding(motOrig, taille_fenetre + 1, utilisationPhoneme, souplesse);
                return;
            } else {
                if (!valClusterGlobal && souplesse < FuzzyDecoder.SOUPLESSE_FILTRAGE) {
                    recognizePatternBottomUpDecoding(motOrig, 0, utilisationPhoneme, souplesse + 1);
                    return;
                } else {
                    LinkedList<LinkedList<Fanal>> lstResult = null;
                    if (FuzzyNetwork.FLOU2FLOU) {
                        lstResult = new LinkedList<>();
                        this.seqsPropagBottomUpFlous.put(motOrig, globalWinners);
                        for (int i = 0; i < globalWinners.size(); i++) {
                            lstWinners = globalWinners.get(i);
                                // Il garde les fanaux flous gagnants
                            // Récupération des macrofanaux gagnants à la fin du décodage
                            lst = new HashSet<>();
                            mapCluster.clear();
                            for (Fanal f : lstWinners) {
                                lst.add(((FanalFlous) f).getMacroFanal());

                            }
                            lstWinners = new LinkedList<>();
                            for (Fanal f : lst) {
                                lstWinners.add(f);
                                mapCluster.put(((FanalFlous) f).getCluster(), ((FanalFlous) f).getLetter());
                            }
                            lstResult.add(lstWinners);
                            ContextTypoNetwork.logger.debug("Mot trouvee: ");
                            for (int j = 0; j < r.NUMBER_CLUSTERS; j++) {
                                ContextTypoNetwork.logger.debug(mapCluster.get(g.getCluster(j)));
                            }
                            ContextTypoNetwork.logger.debug("Nombre de macrofanaux : " + lstWinners.size());
                        }

                    } else {
                        ContextTypoNetwork.logger.debug("Nombre de fanaux : " + lstWinners.size());
                    }

                    seqsPropagBottomUp.put(mot, lstResult);
                    return;
                }
            }

        } else {
            nombreFanauxAvant = 0;
            nombreFanauxApres = lstWinners.size();
            while (nombreFanauxAvant != nombreFanauxApres) {
                lstWinners = new LinkedList<>();
                remiseZero(0);
                for (Fanal f : bestScoresBottomUp.get(0)) {
                    //f.setScore(f.getScore()+GAMA);
                    activerConnectionsChaineTournois(f, g);
                }

                for (int i = 0; i < r.NUMBER_CLUSTERS; i++) {
                    lstWinners.addAll(this.localWinnersTakeAll(g.getCluster(i).getFanalsList(), false));
                }
                bestScoresBottomUp.set(0, lstWinners);
                ContextTypoNetwork.logger.debug("Nombre de fanaux: " + lstWinners.size());
                nombreFanauxAvant = nombreFanauxApres;
                nombreFanauxApres = lstWinners.size();
            }
        }
        LinkedList<LinkedList<Fanal>> auxListe = new LinkedList<>();
        auxListe.add(lstFanaux);
        seqsPropagBottomUp.put(mot, auxListe);

    }

    public LinkedList<LinkedList<Fanal>> getWinnersSeqBottomUp(String mot) {
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = mot.length();
            for (int i = 0; i < this.r.NUMBER_CLUSTERS - taille; i++) {
                mot = mot + "*";
            }
        }
        return seqsPropagBottomUp.get(mot);
    }

    public LinkedList<LinkedList<Fanal>> getWinnersPatternsFuzzyDecoding(String mot) {
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = mot.length();
            for (int i = 0; i < this.r.NUMBER_CLUSTERS - taille; i++) {
                mot = mot + "*";
            }
        }
        return this.seqsPropagBottomUpFlous.get(mot);
    }

    public boolean valideClusters(LinkedList<Fanal> lst) {
        LinkedList lstCluster = new LinkedList<>();
        HashSet<MacroFanal> lst_macro = new HashSet<>();
        for (Fanal f : lst) {
            lst_macro.add(((FanalFlous) f).getMacroFanal());
        }
        for (MacroFanal f : lst_macro) {
            if (!lstCluster.contains(f.getCluster())) {
                lstCluster.add(f.getCluster());

            }
        }
        return lstCluster.size() == r.NUMBER_CLUSTERS && lst_macro.size() == r.FANALS_PER_CLIQUE;

    }

    public void activerConnexionsFanal(Fanal f, int h) {
        Fanal l;
        Edge a;
        FuzzyLevel n = (FuzzyLevel) r.getLevelsList().get(h);
        Edge[] listeArc = n.getGraph().getEdgesList(f);
        for (int i = 0; i < listeArc.length; i++) {
            if (listeArc[i] != null) {
                listeArc[i].setActive(true);
                if (listeArc[i].getSourceFanal().equals(f)) {
                    l = listeArc[i].getDestinationFanal();
                    a = n.getGraph().getEdge(l, f);
                } else {
                    l = listeArc[i].getSourceFanal();
                    a = n.getGraph().getEdge(f, l);
                }
                if (a != null) {
                    a.setActive(true);
                }
                l.setScore(l.getScore() + 1);
            }
        }
    }

    public void activerConnexionsFanal(Fanal f, int h, int score) {
        Fanal l;
        Edge a;
        FuzzyLevel n = (FuzzyLevel) r.getLevelsList().get(h);
        Edge[] listeArc = n.getGraph().getEdgesList(f);
        for (int i = 0; i < listeArc.length; i++) {
            if (listeArc[i] != null) {
                listeArc[i].setActive(true);
                if (listeArc[i].getSourceFanal().equals(f)) {
                    l = listeArc[i].getDestinationFanal();
                    a = n.getGraph().getEdge(l, f);
                } else {
                    l = listeArc[i].getSourceFanal();
                    a = n.getGraph().getEdge(f, l);
                }
                if (a != null) {
                    a.setActive(true);
                }
                l.setScore(l.getScore() + score);
            }
        }
    }

    private void activerConnectionsMacroFanal(MacroFanal fMacro, Graph g, int score) {
        for (Fanal fOrig : fMacro.getListFanaux()) {
            Fanal fDest;
            Edge[] listeArc = g.getEdgesList(fOrig);
            HashMap<Cluster, Integer> listeClusters;
            HashMap<Fanal, HashMap<Cluster, Integer>> mapCluster;
            fOrig.setScore(fOrig.getScore() + GAMA_1);
            //fOrig.setScore(fOrig.getScore() + score);
            for (int i = 0; i < listeArc.length; i++) {
                if (listeArc[i] != null) {
                    if (listeArc[i].getSourceFanal().equals(fOrig)) {
                        fDest = listeArc[i].getDestinationFanal();
                        listeArc[i].setActive(true);
                        // Si il n'existe pas d'activation provenant du cluster de f
                        if (!clusterActiveSoM(fDest, ((FanalFlous) fOrig).getMacroFanal().getCluster(), 0)) {
                            fDest.setScore(fDest.getScore() + 1);
                            // S'il n'y a pas de liste initialisé des clusters pour le macro fanal de destination
                            if (!activationsListeClusters.get(0).containsKey(fDest)) {
                                listeClusters = new HashMap<>();
                                listeClusters.put(((FanalFlous) fOrig).getMacroFanal().getCluster(), score);
                                mapCluster = activationsListeClusters.get(0);
                                mapCluster.put(fDest, listeClusters);
                                activationsListeClusters.set(0, mapCluster);
                            } else {
                                // Il ajoute le cluster
                                listeClusters = new HashMap<>(activationsListeClusters.get(0).get(fDest));
                                listeClusters.put(((FanalFlous) fOrig).getMacroFanal().getCluster(), score);
                                mapCluster = activationsListeClusters.get(0);
                                mapCluster.put(fDest, listeClusters);
                                activationsListeClusters.set(0, mapCluster);
                            }
                            // Si il existe deja une activation provenant du meme fanal
                        } else {
                            // Il garde le max de activations SoS
                            if (activationsListeClusters.get(0).get(fDest).get(((FanalFlous) fOrig).getMacroFanal().getCluster()) < score) {
                                activationsListeClusters.get(0).get(fDest).put(((FanalFlous) fOrig).getMacroFanal().getCluster(), score);
                            }
                        }
                    }
                }
            }
        }
    }

    private boolean clusterActiveSoM(Fanal fDest, Cluster c, int h) {
        if (activationsListeClusters.get(h).containsKey(fDest)) {
            return activationsListeClusters.get(h).get(fDest).containsKey(c);
        } else {
            return false;
        }
    }

    // Cette methode réalise le SoM
    private void activerConnectionsChaineTournois(Fanal fOrig, Graph g) {
        Fanal fDest;
        Edge[] listeArc = g.getEdgesList(fOrig);
        HashMap<Cluster, Integer> listeClusters;
        HashMap<Fanal, HashMap<Cluster, Integer>> mapCluster;
        fOrig.setScore(fOrig.getScore() + GAMA_1);
        for (int i = 0; i < listeArc.length; i++) {
            if (listeArc[i] != null) {
                if (listeArc[i].getSourceFanal().equals(fOrig)) {
                    fDest = listeArc[i].getDestinationFanal();
                    listeArc[i].setActive(true);
                    // Si il n'existe pas d'activation provenant du cluster de f
                    if (!clusterActiveSoM(fDest, ((FanalFlous) fOrig).getCluster(), 0)) {
                        fDest.setScore(fDest.getScore() + 1);
                        if (!activationsListeClusters.get(0).containsKey(fDest)) {
                            listeClusters = new HashMap<>();
                            listeClusters.put(((FanalFlous) fOrig).getCluster(), 1);
                            mapCluster = activationsListeClusters.get(0);
                            mapCluster.put(fDest, listeClusters);
                            activationsListeClusters.set(0, mapCluster);
                        } else {
                            listeClusters = new HashMap<>(activationsListeClusters.get(0).get(fDest));
                            listeClusters.put(((FanalFlous) fOrig).getCluster(), 1);
                            mapCluster = activationsListeClusters.get(0);
                            mapCluster.put(fDest, listeClusters);
                            activationsListeClusters.set(0, mapCluster);
                        }
                    }
                }
            }
        }
    }

    public void activerFanauxInf(Fanal l, int h) {
        //Si il y a des connexions inferieurs, il les active 
        // Ensuite, il active les connexions des fanaux inferieurs
        if (!l.getInferiorFanals().isEmpty()) {
            for (Fanal t : l.getInferiorFanals()) {

                t.setScore(t.getScore() + 1);
            }
        }
    }

    public void activerFanauxSup(Fanal l, int h) {
        //Si il y a des connexions superieures, il les active 
        // Ensuite, il active les connexions des fanaux superieures
        if (!l.getSuperiorFanals().isEmpty()) {
            for (Fanal t : l.getSuperiorFanals()) {
                t.setScore(t.getScore() + 1);
            }
        }
    }

    public void activerFanauxHyperSup(Fanal l, int h) {
        //Si il y a des connexions superieures, il les active 
        // Ensuite, il active les connexions des fanaux superieures
        if (!l.getSuperiorHyperFanals().isEmpty()) {
            for (Fanal t : l.getSuperiorHyperFanals()) {
                t.setScore(t.getScore() + 1);
            }
        }
    }

    public void remiseZero(int h) {
        FuzzyLevel n;
        if (h == -1) {
            for (int i = 0; i < FuzzyNetwork.hMax; i++) {
                n = (FuzzyLevel) r.getLevelsList().get(i);
                for (Fanal f : n.getGraph().getAllNodes()) {
                    f.setScore(0);
                }
                for (Fanal f : ((FuzzyGraph) n.getGraph()).getMacroFanalsList()) {
                    f.setScore(0);
                }
            }
            return;
        } else {
            n = (FuzzyLevel) r.getLevelsList().get(h);
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
        activationsListeClusters.set(0, new HashMap<Fanal, HashMap<Cluster, Integer>>());
    }

    public void remiseMemo() {
        seqsPropagBottomUp = new HashMap<>();
        bestScoresBottomUpLast = new LinkedList<>();
        for (int i = 0; i < FuzzyNetwork.hMax; i++) {
            this.bestScoresBottomUp.add(i, new LinkedList<Fanal>());
            seqsPropagTopDown.add(i, new HashMap<Integer, LinkedList<Fanal>>());
            seqsTopDownCounter[i] = 0;
            // Il utilise le SoM
            activationsListeClusters.add(i, new HashMap<Fanal, HashMap<Cluster, Integer>>());

        }
    }

    public LinkedList<Fanal> loserKickOut(LinkedList<Fanal> lst_orig, int h) {
        LinkedList<Fanal> lst = new LinkedList<>();
        FuzzyLevel n = (FuzzyLevel) r.getLevelsList().get(h);
        int minScore = r.FANALS_PER_CLUSTER * r.NUMBER_CLUSTERS;
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
        FuzzyLevel n = (FuzzyLevel) r.getLevelsList().get(h);
        LinkedList<Fanal>[] vectorScores;
        vectorScores = (LinkedList<Fanal>[]) new LinkedList<?>[r.FANALS_PER_CLUSTER * r.NUMBER_CLUSTERS];
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

    public HashSet<MacroFanal> filtragePropagationLaterale() {
        FuzzyGraph g = (FuzzyGraph) r.getLevelsList().get(0).getGraph();
        HashSet<MacroFanal> result = new HashSet<>();
        LinkedList<MacroFanal> winnersCluster = new LinkedList<>();
        int scoreMax, scoreFanal;
        for (int iClust = 0; iClust < r.NUMBER_CLUSTERS; iClust++) {
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
                if (activationsListeClusters.get(0).containsKey(f)) {
                    for (Cluster c : activationsListeClusters.get(0).get(f).keySet()) {
                        scoreFanal += activationsListeClusters.get(0).get(f).get(c);
                    }
                    ContextTypoNetwork.logger.debug("Second iteration LWsTA: " + f + " Lettre: " + ((FanalFlous) f).getLetter() + " Score: " + scoreFanal);
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
        LinkedList<Fanal> listeWinners = new LinkedList<>();
        for (Fanal f : FanauxCluster) {
            if (f.getScore() > scoreMax) {
                scoreMax = f.getScore();
            }
        }
        for (Fanal f : FanauxCluster) {
            if (f.getScore() == scoreMax) {
                listeWinners.add(f);
            }
        }

        if (secondIt) {
            scoreMax = 0;
            LinkedList<Fanal> listeWinnersAux = new LinkedList<>(listeWinners);
            for (Fanal f : listeWinnersAux) {
                scoreFanal = 0;
                for (Cluster c : activationsListeClusters.get(0).get(f).keySet()) {
                    scoreFanal += activationsListeClusters.get(0).get(f).get(c);
                }
                ContextTypoNetwork.logger.debug("Second iteration LWsTA: " + f + " Lettre: " + ((FanalFlous) f).getLetter() + " Score: " + scoreFanal);
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

        return listeWinners;
    }

}
