package control.decoder;

import control.NetworkControl;
import control.network.TriangularLevel;
import control.network.TriangularNetwork;
import control.ContextTypoNetwork;
import control.rules.PhonemeRules;
import control.network.InterfaceNetwork;
import graph.Edge;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import model.Clique;
import model.Cluster;
import model.Fanal;

public class TriangularDecoder extends Decoder {

    private static int GWsTA_vertical_1;
    private static int GWsTA_vertical_2;
    private static int GWsTA_horizontal;
    private static int MAX_BOOSTING;
    private final TriangularNetwork r;
    // Il active le MAX_BOOSTING
    public static final boolean PLUSIEURS_BOOSTING = true;
    // Il active le UNION_BOOSTING
    public static final boolean UNION_BOOSTING = true;
    private static final int COEFF_DECISION_PROPAGATION = 20; //20
    private static final int COEFF_DECISION_CONTINUER_DECODAGE = 1;

    // Contient les fanaux les plus activés de chaque niveau Bottom-up
    protected LinkedList<LinkedList<Fanal>> bestScoresBottomUp;
    // Contient les derniers fanaux les plus activés Bottom-up
    protected LinkedList<Fanal> bestScoresBottomUpLast;
    // Contient les sequences des fanaux trouvés Bottom-up
    protected HashMap<String, LinkedList<Fanal>> seqsPropagBottomUp;
    //Il permet de savoir les de quelle cluster vient les activations d'un fanal SoM
    protected LinkedList<HashMap<Fanal, HashMap<Cluster, Integer>>> activationsListeClusters;
    // Contient les sequences Top-Down
    protected LinkedList<HashMap<Integer, LinkedList<Fanal>>> seqsPropagTopDown;
    // Il permet de savoir combien de sequences il y a pour chaque niveau au moment du decodage
    protected int[] seqsTopDownCounter;

    public TriangularDecoder(TriangularNetwork r) {

        this.MAX_BOOSTING = 1;
        this.r = r;
        this.GWsTA_vertical_1 = r.FANALS_PER_CLIQUE;
        this.GWsTA_vertical_2 = r.FANALS_PER_CLIQUE * 8;
        this.GWsTA_horizontal = r.FANALS_PER_CLIQUE;
        seqsPropagBottomUp = new HashMap<>();
        bestScoresBottomUp = new LinkedList<>();
        seqsTopDownCounter = new int[r.hMax];
        seqsPropagTopDown = new LinkedList<>();
        bestScoresBottomUpLast = new LinkedList<>();
        for (int i = 0; i < r.hMax; i++) {
            bestScoresBottomUp.add(i, new LinkedList<Fanal>());
            seqsPropagTopDown.add(i, new HashMap<Integer, LinkedList<Fanal>>());
            seqsTopDownCounter[i] = 0;

        }
    }

    public double verifieDecodageTopDown(String motRecherche) {
        HashMap<Integer, LinkedList<Fanal>> lettres = new HashMap<>();
        for (int i = 0; i < motRecherche.length(); i++) {
            lettres.put(i, r.getLevelsList().get(0).getWordClique(motRecherche.charAt(i) + "").getFanalsList());
        }
        int count = 0;
        for (int j = 0; j < seqsTopDownCounter[0]; j++) {
            ContextTypoNetwork.logger.info("La lettre est " + ((TriangularLevel) r.getLevelsList().get(0)).searchClique((seqsPropagTopDown.get(0).get(j))));
            if (seqsPropagTopDown.get(0).get(j).containsAll(lettres.get(j))) {
                count++;
            }
        }
        return ((double) count) / seqsTopDownCounter[0];
    }

    public double verifieDecodageBottomUp(String motRecherche, int h) {
        Clique clique;
        clique = r.getLevelsList().get(h).getWordClique("<" + motRecherche + ">");
        if (clique == null) {

            return -1.0;
        }
        int i = 0;

        for (Fanal f : this.bestScoresBottomUp.get(h)) {
            if (clique.existsFanal(f)) {
                i++;
            }
        }
        // Mudar para mais dinamico
        return ((double) i) / r.FANALS_PER_CLIQUE;
    }

    // ---------- Decodage Top-down ------------------------
    public boolean reconnaitreTopDown(LinkedList<Fanal> cliqueInitial, int tailleMot) {
        int maxNiveau = tailleMot;
        LinkedList<Fanal> winnerListe, winner_1;
        for (int count = 0; count < r.hMax; count++) {
            if (tailleMot <= count) {
                seqsTopDownCounter[count] = 0;
            } else {
                seqsTopDownCounter[count] = maxNiveau;
                maxNiveau--;
            }
        }
        seqsPropagTopDown.get(tailleMot - 1).put(0, cliqueInitial);
        for (int h = tailleMot - 1; h >= 1; h--) {
            for (int i = 0; i < seqsTopDownCounter[h]; i++) {
                remiseZero(h - 1);
                if (i == 0) {
                    // Propagation SofS
                    propagationSofSSeqTopDown(h, i, TriangularDecoder.LEFT);
                    // Itération verticale
                    winnerListe = globalWinnersTakeAll(h - 1, TriangularDecoder.GWsTA_vertical_1);

                    // Début de l'iteration horizontale
                    for (int j = 0; j < it_GWsTA; j++) {
                        remiseZero(h - 1);
                        for (Fanal f : winnerListe) {
                            f.setScore(f.getScore() + GAMA_1);
                            activerConnexionsFanal(f, h - 1);
                        }
                        winnerListe = globalWinnersTakeAll(h - 1, GWsTA_horizontal);
                    }

                    // Fin de l'iteration horizontale
                    // Il memorise la sequence propagée
                    seqsPropagTopDown.get(h - 1).put(i, winnerListe);

                }
                remiseZero(h - 1);
                // Si il est le dernier element 
                if ((i + 1) == seqsTopDownCounter[h]) {
                    if (r.PROPAG_LATERALE && h != -1) {
                        // Activer le sequence lateral du element (h-1,i)
                        activerChaineTournois(seqsPropagTopDown.get(h - 1).get(i), h - 1);
                    }
                    propagationSofSSeqTopDown(h, i, TriangularDecoder.RIGHT);
                    // Itération verticale
                    winnerListe = globalWinnersTakeAll(h - 1, TriangularDecoder.GWsTA_vertical_1);
                    // Début de l'iteration horizontale
                    for (int j = 0; j < it_GWsTA; j++) {
                        remiseZero(h - 1);
                        for (Fanal f : winnerListe) {
                            f.setScore(f.getScore() + GAMA_1);
                            activerConnexionsFanal(f, h - 1);
                        }
                        winnerListe = globalWinnersTakeAll(h - 1, GWsTA_horizontal);
                    }
                    // Fin de l'iteration horizontale
                    // Il memorise la sequence propagée
                    seqsPropagTopDown.get(h - 1).put(i + 1, winnerListe);
                } else {
                    propagationSofSSeqTopDown(h, i, TriangularDecoder.RIGHT);
                    if (r.PROPAG_LATERALE && h != -1) {
                        // Activer le sequence lateral du element (h-1,i)
                        activerChaineTournois(seqsPropagTopDown.get(h - 1).get(i), h - 1);
                    }
                    propagationSofSSeqTopDown(h, i + 1, TriangularDecoder.LEFT);
                    // Itération verticale
                    winnerListe = globalWinnersTakeAll(h - 1, TriangularDecoder.GWsTA_vertical_1);
                    // Début de l'iteration horizontale
                    for (int j = 0; j < it_GWsTA; j++) {
                        remiseZero(h - 1);
                        for (Fanal f : winnerListe) {
                            f.setScore(f.getScore() + GAMA);
                            activerConnexionsFanal(f, h - 1);
                        }
                        winnerListe = globalWinnersTakeAll(h - 1, GWsTA_horizontal);
                    }
                    // Fin de l'iteration horizontale
                    // Il memorise la sequence propagée
                    seqsPropagTopDown.get(h - 1).put(i + 1, winnerListe);
                    remiseZero(h - 1);
                }

            }

        }
        return true;
    }

    // Propagation Top-down
    public boolean propagationSofSSeqTopDown(int h, int seq, int side) {
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        int cluster;
        for (Fanal f : seqsPropagTopDown.get(h).get(seq)) {
            cluster = n.getGraph().getNumerotation().numCluster(f);
            if (n.getSideCluster(cluster) == side) {
                activerFanauxInf(f, h);
            }
        }
        return true;
    }

    public LinkedList<Fanal> getWinnersSeqBottomUp(String mot) {
        return seqsPropagBottomUp.get(mot);
    }

    // ------------- Decodage Bottom-up-------------------

    public boolean reconnaitreBottomUpPhoneme(List<String> listeS, int side, boolean begin, boolean end) {
        String seqLeft = "";
        String seqRight = "";
        String s = "";
        boolean gaucheSeq, droiteSeq;
        List<String> listeSeqLeft, listeSeqRight;
        for (String aux : listeS) {
            s += aux;
        }
        if (listeS.size() == 1) {
            return propagationLetterBottomUp(s, side);
        } else {
            listeSeqLeft = listeS.subList(0, listeS.size() - 1);
            listeSeqRight = listeS.subList(1, listeS.size());
            for (String aux : listeSeqLeft) {
                seqLeft += aux;
            }
            for (String aux : listeSeqRight) {
                seqRight += aux;
            }
            if (seqsPropagBottomUp.containsKey(s)) {
                return propagationTriangBottomUp(listeS.size() - 1, s, side, begin, end);
            } else {
                gaucheSeq = reconnaitreBottomUpPhoneme(listeSeqLeft, TriangularDecoder.LEFT, begin, false);
                droiteSeq = reconnaitreBottomUpPhoneme(listeSeqRight, TriangularDecoder.RIGHT, false, end);
                if (gaucheSeq && droiteSeq) {
                    if (begin && end) {
                        return true;
                    } else {
                        return propagationTriangBottomUp(listeS.size() - 1, s, side, begin, end);
                    }
                } else {
                    return false;
                }
            }
        }
    }

    public LinkedList<Fanal> getWinnersTwoLevelsJumpDecoding(LinkedList<List<String>> mot) {
        remiseMemo();
        for (int i = 0; i < this.r.hMax; i++) {
            remiseZero(i);
        }
        List<String> lstPhons = PhonemeRules.splitPhoneme(mot);
        boolean last = false;
        for (int i = 0; i < lstPhons.size(); i++) {
            if (i == lstPhons.size() - 1) {
                last = true;
            }
            // Il propage toutes les lettres au derniere niveau
            propagationHyperSofSSeqBottomUp(lstPhons.get(i), 1, last);
        }

        if (NetworkControl.ACTIVATE_LATERAL_CONNECTIONS) {
            for (Fanal f : InterfaceNetwork.fanalScoreMap.keySet()) {
                f.setScore(f.getScore() + InterfaceNetwork.fanalScoreMap.get(f));
            }
        }

        reconnaitreBottomUpPhoneme(lstPhons, TriangularDecoder.LEFT, true, true);

        if (r.ADDITIONAL_LEVEL) {
            return getWinnersBottomUp(r.hMax - 1, TriangularDecoder.LEFT, true, true);
        } else {
            return getWinnersBottomUp(mot.size() - 1, TriangularDecoder.LEFT, true, true);
        }
    }

    // Méthode du decodage bottom up Hyper
    public boolean reccognizeBottomUpHyperLetters(String s) {
        boolean last = false;
        for (int i = 0; i < s.length(); i++) {
            if (i == s.length() - 1) {
                last = true;
            }
            // Il propage toutes les lettres
            propagationHyperSofSSeqBottomUp(s.charAt(i) + "", 0, last);
            remiseZero(s.length() - 2);
        }
        propagationSofSSeqBottomUp(s.length() - 1, s, TriangularDecoder.LEFT, true, true);
        return last;
    }

    public boolean reccognizeBottomUpHyper3Levels(String mot) {

        boolean last = false;
        LinkedList<Fanal> winnerListe;

        for (int ti = 0; ti < mot.length() - 2; ti++) {
            if (ti == mot.length() - 3) {
                last = true;
            }
            for (int bi = 0; bi < 2; bi++) {
                String seqBigramme = mot.substring(ti + bi, ti + bi + 2);
                // Mémorisation, reutilise si on a déjà calculé
                if (seqsPropagBottomUp.containsKey(seqBigramme)) {

                    winnerListe = seqsPropagBottomUp.get(seqBigramme);
                } else {
                    // Propagation des lettres pour trouver les bigrammes
                    propagationLetterBottomUp(mot.charAt(ti + bi) + "", TriangularDecoder.LEFT);
                    propagationLetterBottomUp(mot.charAt(ti + bi + 1) + "", TriangularDecoder.RIGHT);
                    //Itération verticale
                    winnerListe = globalWinnersTakeAll(1, GWsTA_vertical_1);
                    // Début de l'iteration horizontale
                    for (int k = 0; k < it_GWsTA; k++) {
                        remiseZero(1);
                        for (Fanal f : winnerListe) {
                            f.setScore(f.getScore() + GAMA_1);
                            activerConnexionsFanal(f, 1);
                        }
                        if (r.MEIOSE && r.WINNER_SIDE) {
                            if (r.WINNER_PARTITIONS) {
                                winnerListe = new LinkedList<>();
                                for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                                    winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.RIGHT, j));
                                }
                                for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                                    winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.LEFT, j));
                                }
                            } else {
                                winnerListe = globalWinnersTakeAll(1, GWsTA_horizontal / 2, TriangularDecoder.LEFT);
                                // Normalement ils sont ensembles disjointes
                                winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / 2, TriangularDecoder.RIGHT));
                            }
                        } else {
                            winnerListe = globalWinnersTakeAll(1, GWsTA_horizontal);
                        }

                    }
                    ContextTypoNetwork.logger.info("Bigramme: " + seqBigramme + " Nombre de fanaux winners apres horizontal: " + winnerListe.size());
                    // Fin de l'iteration horizontale
                    remiseZero(1);
                    // Il memorise la sequence propagée
                    seqsPropagBottomUp.put(seqBigramme, winnerListe);
                }
                // Il mémorise les fanaux qui seront propagés au dernier (2 niveau)
                bestScoresBottomUpLast = unionSets(bestScoresBottomUpLast, winnerListe);
                if (bi == 0) { // Il est le premier bigramme
                    if (r.PROPAG_LATERALE) {
                        // Il active les sequences laterales
                        this.activerChaineTournois(winnerListe, 1);

                    }
                    bestScoresBottomUp.set(1, winnerListe);
                } else { // C'est la deuxième sequence de bigramme
                    bestScoresBottomUp.set(1, unionSets(bestScoresBottomUp.get(1), winnerListe));
                    LinkedList<Clique> lst_inf = new LinkedList<>();
                    lst_inf.add(((TriangularLevel) r.getLevelsList().get(0)).getCliqueLetter(mot.charAt(ti) + ""));
                    lst_inf.add(((TriangularLevel) r.getLevelsList().get(0)).getCliqueLetter(mot.charAt(ti + 1) + ""));
                    lst_inf.add(((TriangularLevel) r.getLevelsList().get(0)).getCliqueLetter(mot.charAt(ti + 2) + ""));
                    // Il active les fanaux hyper du premier au troisième niveau
                    for (Clique c : lst_inf) {
                        // Il mémorise les fanaux qui seront propagés au dernier (1 niveau)
                        bestScoresBottomUpLast = unionSets(bestScoresBottomUpLast, c.getFanalsList());
                        for (Fanal f : c.getFanalsList()) {
                            this.activerFanauxHyperSup(f, 2);
                        }
                    }
                    remiseZero(mot.length() - 1);
                    // Il active les connexions directes du deuxième au troisième niveau
                    for (Fanal f : bestScoresBottomUp.get(1)) {
                        this.activerFanauxSup(f, 1);
                    }
                    if (ti == 0) {
                        propagationSofSSeqBottomUp(2, mot.substring(ti, ti + 3), TriangularDecoder.LEFT, true, false);
                    } else {
                        if (last) {
                            propagationSofSSeqBottomUp(2, mot.substring(ti, ti + 3), TriangularDecoder.LEFT, false, true);
                        } else {
                            propagationSofSSeqBottomUp(2, mot.substring(ti, ti + 3), TriangularDecoder.LEFT, false, false);
                        }
                    }
                    // Il mémorise les fanaux qui seront propagés au dernier (3 niveau)
                    bestScoresBottomUpLast = unionSets(bestScoresBottomUpLast, bestScoresBottomUp.get(2));
                }
            }
        }
        for (Fanal f : bestScoresBottomUpLast) {
            this.activerFanauxHyperSup(f, mot.length() - 1);
        }
        propagationSofSSeqBottomUp(mot.length() - 1, mot, TriangularDecoder.LEFT, true, true);
        return true;
    }

    public boolean reccognizeBottomUpHyper2Levels(String mot) {

        boolean last = false;
        for (int i = 0; i < mot.length(); i++) {
            if (i == mot.length() - 1) {
                last = true;
            }
            // Il propage toutes les lettres au derniere niveau
            propagationHyperSofSSeqBottomUp(mot.charAt(i) + "", 1, last);
        }
        LinkedList<Fanal> winnerListe;
        last = false;
        for (int i = 0; i < mot.length() - 1; i++) {
            // Propagation des lettres pour trouver les bigrammes
            propagationLetterBottomUp(mot.charAt(i) + "", TriangularDecoder.LEFT);
            propagationLetterBottomUp(mot.charAt(i + 1) + "", TriangularDecoder.RIGHT);
            //Itération verticale
            winnerListe = globalWinnersTakeAll(1, GWsTA_vertical_1);
            // Début de l'iteration horizontale
            for (int k = 0; k < it_GWsTA; k++) {
                remiseZero(1);
                for (Fanal f : winnerListe) {
                    f.setScore(f.getScore() + GAMA_1);
                    activerConnexionsFanal(f, 1);
                }
                if (r.MEIOSE && r.WINNER_SIDE) {
                    if (r.WINNER_PARTITIONS) {
                        winnerListe = new LinkedList<>();
                        for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                            winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.RIGHT, j));
                        }
                        for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                            winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.LEFT, j));
                        }
                    } else {
                        winnerListe = globalWinnersTakeAll(1, GWsTA_horizontal / 2, TriangularDecoder.LEFT);
                        // Normalement ils sont ensembles disjointes
                        winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / 2, TriangularDecoder.RIGHT));
                    }
                } else {
                    winnerListe = globalWinnersTakeAll(1, GWsTA_horizontal);
                }

            }
            ContextTypoNetwork.logger.debug("Bigramme: " + mot.charAt(i) + mot.charAt(i + 1) + " Nombre de fanaux winners apres horizontal: " + winnerListe.size());
            // Fin de l'iteration horizontale
            remiseZero(1);
            // Début de la recherche pour le troisième niveau
            if (i == mot.length() - 2) {
                last = true;
            }
            //if(Reseau.UNION){
            // Il réalise l'union des listes
            //bestScoresBottomUp.set(1, unionEnsemble(bestScoresBottomUp.get(1),winnerListe));
            //}else{
            bestScoresBottomUp.set(1, joinSets(bestScoresBottomUp.get(1), winnerListe));
            //}
            ContextTypoNetwork.logger.debug("Jonction fanaux de bigrammes: " + bestScoresBottomUp.get(1).size());
            if (!last) {
                if (r.PROPAG_LATERALE) {
                    // Il active les sequences laterales
                    this.activerChaineTournois(winnerListe, 1);
                }
            } else {
                for (Fanal f : bestScoresBottomUp.get(1)) {
                    // Il active les fanaux connectes du niveau superieur et ses connexions      
                    this.activerFanauxHyperSup(f, mot.length() - 1);
                }
            }

        }

        propagationSofSSeqBottomUp(mot.length() - 1, mot, TriangularDecoder.LEFT, true, true);
        return last;
    }

    public boolean reccognizeBottomUpHyper2Levels(LinkedList<List<String>> mot, String motRecherche) {

        boolean last = false;
        for (int i = 0; i < mot.size(); i++) {

            if (i == mot.size() - 1) {
                last = true;
            }
            // Il propage toutes les lettres au derniere niveau
            propagationHyperSofSSeqBottomUp(mot.get(i), 1, last);
            System.out.println("Prop:" + mot.get(i));
        }
        LinkedList<Fanal> winnerListe;
        last = false;
        for (int i = 0; i < mot.size() - 1; i++) {
            // Propagation des lettres pour trouver les bigrammes
            TriangularDecoder.this.propagationLetterBottomUp(mot.get(i), TriangularDecoder.LEFT);
            TriangularDecoder.this.propagationLetterBottomUp(mot.get(i + 1), TriangularDecoder.RIGHT);
            //Itération verticale
            System.out.println(mot.get(i) + " et " + mot.get(i + 1));
            winnerListe = globalWinnersTakeAll(1, GWsTA_vertical_1);
            // Début de l'iteration horizontale
            for (int k = 0; k < it_GWsTA; k++) {
                remiseZero(1);
                for (Fanal f : winnerListe) {
                    f.setScore(f.getScore() + GAMA_1);
                    activerConnexionsFanal(f, 1);
                }
                if (r.MEIOSE && r.WINNER_SIDE) {
                    if (r.WINNER_PARTITIONS) {
                        winnerListe = new LinkedList<>();
                        for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                            winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.RIGHT, j));
                        }
                        for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                            winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.LEFT, j));
                        }
                    } else {
                        winnerListe = globalWinnersTakeAll(1, GWsTA_horizontal / 2, TriangularDecoder.LEFT);
                        // Normalement ils sont ensembles disjointes
                        winnerListe = unionSets(winnerListe, globalWinnersTakeAll(1, GWsTA_horizontal / 2, TriangularDecoder.RIGHT));
                    }
                } else {
                    winnerListe = globalWinnersTakeAll(1, GWsTA_horizontal);
                }

            }
            ContextTypoNetwork.logger.debug("Bigramme: " + mot.get(i) + mot.get(i + 1) + " Nombre de fanaux winners apres horizontal: " + winnerListe.size());
            // Fin de l'iteration horizontale
            remiseZero(1);
            // Début de la recherche pour le troisième niveau
            if (i == mot.size() - 2) {
                last = true;
            }
            //if(Reseau.UNION){
            // Il réalise l'union des listes
            //bestScoresBottomUp.set(1, unionEnsemble(bestScoresBottomUp.get(1),winnerListe));
            //}else{
            bestScoresBottomUp.set(1, joinSets(bestScoresBottomUp.get(1), winnerListe));
            //}
            ContextTypoNetwork.logger.debug("Jonction fanaux de bigrammes: " + bestScoresBottomUp.get(1).size());
            if (!last) {
                if (r.PROPAG_LATERALE) {
                    // Il active les sequences laterales
                    this.activerChaineTournois(winnerListe, 1);
                }
            } else {
                for (Fanal f : bestScoresBottomUp.get(1)) {
                    // Il active les fanaux connectes du niveau superieur et ses connexions      
                    this.activerFanauxHyperSup(f, mot.size() - 1);
                }
            }

        }
        if (r.ADDITIONAL_LEVEL) {
            propagationSofSSeqBottomUp(r.hMax - 1, motRecherche, TriangularDecoder.LEFT, true, true);
        } else {
            propagationSofSSeqBottomUp(mot.size() - 1, motRecherche, TriangularDecoder.LEFT, true, true);
        }

        return last;
    }

    // Méthode de propagation les lettres
    public boolean propagationHyperSofSSeqBottomUp(String seq, int hCible, boolean last) {
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(hCible - 1);
        Clique c;
        if (!n.existsClique(seq)) {
            return false;
        }

        // Il prend la clique connu pour l'activer
        c = n.getCliqueLetter(seq);
        if (r.UNION) {
            // Il réalise l'union des listes
            bestScoresBottomUp.set(hCible - 1, unionSets(bestScoresBottomUp.get(hCible - 1), c.getFanalsList()));
            ContextTypoNetwork.logger.debug(seq + ": apres union size: " + bestScoresBottomUp.get(hCible - 1).size());
        } else {
            bestScoresBottomUp.set(hCible - 1, joinSets(bestScoresBottomUp.get(hCible - 1), c.getFanalsList()));
        }
        if (last == true) {
            //Propagation verticale h+1
            ContextTypoNetwork.logger.debug("Nombre: " + bestScoresBottomUp.get(hCible - 1).size());
            for (Fanal f : bestScoresBottomUp.get(hCible - 1)) {
                // Il active les fanaux connectes du niveau superieur et ses connexions      
                this.activerFanauxHyperSup(f, hCible - 1);
            }
        }
        remiseZero(hCible - 1);
        return true;
    }

    // Méthode de propagation les lettres
    public boolean propagationHyperSofSSeqBottomUp(List<String> seqListe, int hCible, boolean last) {
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(hCible - 1);
        Clique c;
        for (String seq : seqListe) {
            if (!n.existsClique(seq)) {
                return false;
            }
        }
        for (String seq : seqListe) {
            // Il prend la clique connu pour l'activer
            c = n.getCliqueLetter(seq);
            if (r.UNION) {
                // Il réalise l'union des listes
                bestScoresBottomUp.set(hCible - 1, unionSets(bestScoresBottomUp.get(hCible - 1), c.getFanalsList()));
                ContextTypoNetwork.logger.debug(seqListe + ": apres union size: " + bestScoresBottomUp.get(hCible - 1).size());
            } else {
                bestScoresBottomUp.set(hCible - 1, joinSets(bestScoresBottomUp.get(hCible - 1), c.getFanalsList()));
            }
        }
        if (last == true) {
            //Propagation verticale h+1
            ContextTypoNetwork.logger.debug("Nombre: " + bestScoresBottomUp.get(hCible - 1).size());
            for (Fanal f : bestScoresBottomUp.get(hCible - 1)) {
                // Il active les fanaux connectes du niveau superieur et ses connexions      
                this.activerFanauxHyperSup(f, hCible - 1);
            }
        }
        remiseZero(hCible - 1);
        return true;
    }

    public LinkedList<Fanal> getWinnersInterfaceNetwork(LinkedList<Fanal> listeFanauxInf) {
        LinkedList<Fanal> winnerListe;
        int h = 0;
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        remiseMemo();
        remiseZero(h);
        for (Fanal fInf : listeFanauxInf) {
            for (Fanal fSup : fInf.getSuperiorFanals()) {
                fSup.setScore(fSup.getScore() + GAMA);
                activerConnexionsFanal(fSup, h);
            }
        }
        winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal);
        for (int i = 0; i < it_GWsTA; i++) {
            remiseZero(h);
            for (Fanal f : winnerListe) {
                f.setScore(f.getScore() + GAMA_1);
                activerConnexionsFanal(f, h);
            }
            winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal);
        }

        ContextTypoNetwork.logger.debug("Nombre fanaux après GlobalWinners:" + winnerListe.size());

        bestScoresBottomUp.set(h, winnerListe);
        return winnerListe;
    }

    public boolean propagationSofSSeqBottomUp(int h, String seq, int side, boolean begin, boolean end) {
        LinkedList<Fanal> winnerListe;
        HashMap<Fanal, Integer> scoreActives = new HashMap<>();
        Set<Fanal> ensembleFanaux;
        double match_score;
        if (h < r.hMax) {
            //Utilisation de la memorisation
            if (seqsPropagBottomUp.containsKey(seq)) {
                winnerListe = seqsPropagBottomUp.get(seq);
            } else {

                // Début de l'iteration horizontale
                if (r.LOSERS_KICK_OUT && h >= 2) {
                    // Pré-filtrage
                    winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_2);

                    for (Fanal f : winnerListe) {
                        ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                    }
                    // Premiere phase
                    //remiseZero(h);
                    for (Fanal f : winnerListe) {
                        f.setScore(f.getScore() + GAMA_1);
                        activerConnexionsFanal(f, h, f.getScore());
                    }
                    while (!sameScore(winnerListe)) {
                        winnerListe = loserKickOut(winnerListe, h);
                        remiseZero(h);
                        for (Fanal f : winnerListe) {
                            f.setScore(f.getScore() + GAMA_1);
                            activerConnexionsFanal(f, h);
                        }
                    }
                    // Deuxieme phase
                    remiseZero(h);
                    ContextTypoNetwork.logger.debug("Nombre fanaux avant GlobalWinners:" + winnerListe.size());
                    for (Fanal f : winnerListe) {
                        f.setScore(f.getScore() + GAMA_1);
                        activerConnexionsFanal(f, h);
                    }
                    winnerListe = globalWinnersTakeAll(h, r.FANALS_PER_CLIQUE);
                    ContextTypoNetwork.logger.debug("Nombre fanaux après GlobalWinners:" + winnerListe.size());
                    // Troisieme phase est egal la premiere
                    remiseZero(h);
                    for (Fanal f : winnerListe) {
                        f.setScore(f.getScore() + GAMA_1);
                        activerConnexionsFanal(f, h);
                    }
                    while (!sameScore(winnerListe)) {
                        winnerListe = loserKickOut(winnerListe, h);
                        remiseZero(h);
                        for (Fanal f : winnerListe) {
                            f.setScore(f.getScore() + GAMA_1);
                            activerConnexionsFanal(f, h);
                        }
                    }

                } else {

                    if (r.BOOSTING && h >= 2) {

                        // Pré-filtrage
                        winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_2);
                        LinkedList<Fanal> lstActives = new LinkedList<>(winnerListe);
                        for (Fanal f : winnerListe) {
                            scoreActives.put(f, f.getScore());
                            ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                        }

                        // Deuxieme phase
                        ContextTypoNetwork.logger.debug("Nombre fanaux avant GlobalWinners:" + winnerListe.size());
                        LinkedList<Fanal> lstBoosting = new LinkedList<>(winnerListe);
                        LinkedList<Fanal> winnerListeAux;
                        winnerListe = new LinkedList<>();
                        Fanal fanalBoosting;
                        int ordreCliqueBoosting = r.FANALS_PER_CLIQUE;
                        int ordreMaxClique = 0;
                        boolean conditionArret = false;
                        boolean cliqueTrouveGlobal = false;
                        boolean cliqueTrouve;
                        int nValides = 0;
                        // Il s'arrete quand il trouve la clique maximale
                        while (!conditionArret) {
                            // Il utilise le fanal avec plus d'activation pour le booster
                            remiseZero(h);
                            cliqueTrouve = false;
                            fanalBoosting = lstBoosting.getFirst();
                            fanalBoosting.setScore(fanalBoosting.getScore() + GAMA_1);
                            activerConnexionsFanal(fanalBoosting, h);
                            winnerListeAux = globalWinnersTakeAll(lstActives, GWsTA_vertical_2);
                            // Troisieme phase est egal la premiere
                            remiseZero(h);
                            for (Fanal f : winnerListeAux) {
                                f.setScore(f.getScore() + GAMA_1);
                                activerConnexionsFanal(f, h);
                            }
                            // Il fait convergir à une clique dont tous les fanaux ont le même score (afin d'éliminer les fanaux parasites)
                            while (!sameScore(winnerListeAux)) {
                                winnerListeAux = loserKickOut(winnerListeAux, h);
                                remiseZero(h);
                                for (Fanal f : winnerListeAux) {
                                    f.setScore(f.getScore() + GAMA_1);
                                    activerConnexionsFanal(f, h);
                                }
                            }
                            ContextTypoNetwork.logger.debug("Nombre fanaux après boosting:" + winnerListeAux.size());
                            // Valider un par cluster et le c fanaux obtenus pour la condition d'arret
                            if (winnerListeAux.size() == ordreCliqueBoosting) {
                                cliqueTrouveGlobal = true;
                                //ensembleFanaux = new HashSet<>(winnerListe);
                                //cliqueNorme.put(ensembleFanaux, DecodageTriang.calculerNormeScores(ensembleFanaux, scoreActives));
                                winnerListe = unionSets(winnerListe, winnerListeAux);
                                nValides++;
                            }

                            // Il garde la plus grande valeur de c trouvee
                            if (winnerListeAux.size() <= r.FANALS_PER_CLIQUE && winnerListeAux.size() > ordreMaxClique) {
                                ordreMaxClique = winnerListeAux.size();
                            }
                            System.out.println("Nombre de fanaux actives au debut (Il ne peut pas changer): " + lstActives.size());
                            // Il supprime de la liste du boosting le fanal boosté
                            lstBoosting.remove(fanalBoosting);
                            if (cliqueTrouveGlobal && (lstBoosting.size() == 0 || nValides >= TriangularDecoder.MAX_BOOSTING)) {
                                conditionArret = true;
                                break;
                            }
                            // Il verifie si c'est necessaire d'autres iterations (par rapport à la plus grande valeur de c trouve)
                            if (lstBoosting.size() == 0 && ordreMaxClique != ordreCliqueBoosting) {
                                // Il redemarre la liste de fanaux du boosting
                                lstBoosting = new LinkedList<>(lstActives);
                                // Il remonte l'arbre de décision avec l'ordre de clique la plus grande
                                System.out.println("Change décision boosting: " + ordreCliqueBoosting + " -> " + ordreMaxClique);
                                ordreCliqueBoosting = ordreMaxClique;
                                conditionArret = false;

                            }
                        }

                    } else {
                        if (r.SEUILLAGE) {
                            winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_2);
                            for (Fanal f : winnerListe) {
                                ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                            }
                            // Seuillage (Diminuer le nombre des fanaux parasites)
                            int nombre_fanaux_avant = winnerListe.size();
                            for (int i = 0; i < it_GWsTA * 10; i++) {
                                remiseZero(h);
                                LinkedList<Fanal> copy = new LinkedList<>(winnerListe);
                                for (Fanal f : winnerListe) {
                                    f.setScore(f.getScore() + GAMA);
                                    activerConnexionsFanal(f, h);
                                }
                                winnerListe = this.thresholdingFilter(h, r.FANALS_PER_CLIQUE + GAMA - 1);
                                if (winnerListe.size() == nombre_fanaux_avant || winnerListe.size() == 0) {
                                    if (winnerListe.size() == 0) {
                                        winnerListe = copy;
                                    }
                                    break;
                                }
                                nombre_fanaux_avant = winnerListe.size();
                                ContextTypoNetwork.logger.debug("Nombre fanaux après seuillage: " + winnerListe.size());
                            }

                        } else {
                            winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_1);
                            //winnerListe=this.thresholdingFilter(h,20);
                            for (Fanal f : winnerListe) {
                                ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                            }
                        }
                    }
                }
                for (int i = 0; i < it_GWsTA; i++) {
                    remiseZero(h);
                    for (Fanal f : winnerListe) {
                        f.setScore(f.getScore() + GAMA_1);
                        activerConnexionsFanal(f, h);
                    }
                    if (r.MEIOSE && r.WINNER_SIDE) {
                        if (r.WINNER_PARTITIONS) {
                            winnerListe = new LinkedList<>();
                            for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                                winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.RIGHT, j));
                            }
                            for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                                winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.LEFT, j));
                            }
                        } else {
                            winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal / 2, TriangularDecoder.LEFT);
                            // Normalement ils sont ensembles disjointes
                            winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / 2, TriangularDecoder.RIGHT));
                        }
                    } else {
                        winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal);
                    }
                }
                // Fin de l'iteration horizontale
                // Il memorise la sequence propagée
                seqsPropagBottomUp.put(seq, winnerListe);
                for (Fanal f : winnerListe) {
                    ContextTypoNetwork.logger.info("it_hor " + f + " Score: " + f.getScore());
                }
                match_score = verifieDecodageBottomUp(seq, h);
                ContextTypoNetwork.logger.info("Niveau: " + h + "; Seq: " + seq + "; MATCHING: " + match_score);
                ContextTypoNetwork.logger.info("----------------------------------------------");
                // Début verifie les mots troubes
                LinkedList<String> motsMatch = ((TriangularLevel) this.r.getLevelsList().get(h)).searchClique(winnerListe);
                if (motsMatch.size() > 1 || match_score != 1.0) {
                    ContextTypoNetwork.logger.info("Mauvais: Match ambigu: ");
                    for (String s : motsMatch) {
                        ContextTypoNetwork.logger.info(s);
                    }
                }
                // Fin verifie les mots troubes
                ContextTypoNetwork.logger.info("------------------ Fin sequence--------------------");
            }
            if (h != r.hMax - 1) {
                if (side == TriangularDecoder.RIGHT) {
                    if (r.UNION) {
                        // Il réalise l'union des listes
                        bestScoresBottomUp.set(h, unionSets(bestScoresBottomUp.get(h), winnerListe));
                    } else {
                        bestScoresBottomUp.set(h, joinSets(bestScoresBottomUp.get(h), winnerListe));
                    }
                    //Propagation verticale h+1
                    for (Fanal f : bestScoresBottomUp.get(h)) {
                        activerFanauxSup(f, h);
                    }
                }

                remiseZero(h - 1);
                remiseZero(h);
                if (side == TriangularDecoder.LEFT) {
                    if (r.PROPAG_LATERALE) {
                        // Il active les sequences laterales
                        this.activerChaineTournois(winnerListe, h);
                    }
                    // Il memorise les winners de la clique à gauche
                    bestScoresBottomUp.set(h, winnerListe);
                }
            }

        }

        return true;
    }

    public boolean propagationTriangBottomUp(int h, String seq, int side, boolean begin, boolean end) {
        LinkedList<Fanal> winnerListe;
        Set<Fanal> ensembleFanaux;
        double match_score;
        if (h < r.hMax) {
            //Utilisation de la memorisation
            if (seqsPropagBottomUp.containsKey(seq)) {
                winnerListe = seqsPropagBottomUp.get(seq);
            } else {
                // Pré-filtrage
                winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_1);
                for (Fanal f : winnerListe) {
                    ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                }
                for (int i = 0; i < it_GWsTA; i++) {
                    remiseZero(h);
                    for (Fanal f : winnerListe) {
                        f.setScore(f.getScore() + GAMA_1);
                        activerConnexionsFanal(f, h);
                    }
                    if (r.MEIOSE && r.WINNER_SIDE) {
                        if (r.WINNER_PARTITIONS) {
                            winnerListe = new LinkedList<>();
                            for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                                winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.RIGHT, j));
                            }
                            for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                                winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.LEFT, j));
                            }
                        } else {
                            winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal / 2, TriangularDecoder.LEFT);
                            // Normalement ils sont ensembles disjointes
                            winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / 2, TriangularDecoder.RIGHT));
                        }
                    } else {
                        winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal);
                    }
                }
                // Fin de l'iteration horizontale
                // Il memorise la sequence propagée
                seqsPropagBottomUp.put(seq, winnerListe);
                for (Fanal f : winnerListe) {
                    ContextTypoNetwork.logger.info("it_hor " + f + " Score: " + f.getScore());
                }
                match_score = verifieDecodageBottomUp(seq, h);
                ContextTypoNetwork.logger.info("Niveau: " + h + "; Seq: " + seq + "; MATCHING: " + match_score);
                ContextTypoNetwork.logger.info("----------------------------------------------");
                // Début verifie les mots troubes
                LinkedList<String> motsMatch = ((TriangularLevel) this.r.getLevelsList().get(h)).searchClique(winnerListe);
                if (motsMatch.size() > 1 || match_score != 1.0) {

                    for (String s : motsMatch) {
                        ContextTypoNetwork.logger.info(s);
                        if (!s.equals(seq)) {
                            ContextTypoNetwork.logger.info("Mauvais: Match ambigu: ");
                        }
                    }
                }
                // Fin verifie les mots troubes
                ContextTypoNetwork.logger.info("------------------ Fin sequence--------------------");
                if (winnerListe.size() <= r.FANALS_PER_CLIQUE * COEFF_DECISION_PROPAGATION) {
                    ContextTypoNetwork.logger.debug("Sequence hyper propagee: " + seq);
                    for (Fanal f : winnerListe) {
                        // Il active les fanaux connectes du niveau superieur et ses connexions      
                        this.activerFanauxHyperSup(f, h);
                    }
                }
            }
            if (h != r.hMax - 1) {
                if (side == TriangularDecoder.RIGHT) {

                    if (r.UNION) {
                        // Il réalise l'union des listes
                        bestScoresBottomUp.set(h, unionSets(bestScoresBottomUp.get(h), winnerListe));
                    } else {
                        bestScoresBottomUp.set(h, joinSets(bestScoresBottomUp.get(h), winnerListe));
                    }
                    ContextTypoNetwork.logger.debug("Sequence right propage: " + seq + " Nombre fanaux: " + bestScoresBottomUp.get(h).size());
                    //Propagation verticale h+1
                    for (Fanal f : bestScoresBottomUp.get(h)) {
                        activerFanauxSup(f, h);
                    }
                }

                remiseZero(h - 1);
                remiseZero(h);
                if (side == TriangularDecoder.LEFT) {

                    if (r.PROPAG_LATERALE) {
                        ContextTypoNetwork.logger.debug("Sequence left chaine: " + seq + " Nombre fanaux: " + winnerListe.size());
                        // Il active les sequences laterales
                        this.activerChaineTournois(winnerListe, h);
                    }
                    // Il memorise les winners de la clique à gauche
                    bestScoresBottomUp.set(h, winnerListe);
                }
            }
            if (winnerListe.size() <= r.FANALS_PER_CLIQUE * COEFF_DECISION_CONTINUER_DECODAGE) {
                return true;
            } else {
                return false;
            }
        }

        return true;
    }

    public LinkedList<Fanal> getWinnersBottomUp(int h, int side, boolean begin, boolean end) {
        LinkedList<Fanal> winnerListe;
        //HashMap<Set<Fanal>, Integer> cliqueNorme = new HashMap<>();
        HashMap<Fanal, Integer> scoreActives = new HashMap<>();
        //Set<Fanal> ensembleFanaux;
        // Début de l'iteration horizontale
        if (r.LOSERS_KICK_OUT && h >= 2) {
            // Pré-filtrage
            winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_2);

            for (Fanal f : winnerListe) {
                ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
            }
            // Premiere phase
            //remiseZero(h);
            for (Fanal f : winnerListe) {
                f.setScore(f.getScore() + GAMA_1);
                activerConnexionsFanal(f, h, f.getScore());
            }
            while (!sameScore(winnerListe)) {
                winnerListe = loserKickOut(winnerListe, h);
                remiseZero(h);
                for (Fanal f : winnerListe) {
                    f.setScore(f.getScore() + GAMA_1);
                    activerConnexionsFanal(f, h);
                }
            }
            // Deuxieme phase
            remiseZero(h);
            ContextTypoNetwork.logger.debug("Nombre fanaux avant GlobalWinners:" + winnerListe.size());
            for (Fanal f : winnerListe) {
                f.setScore(f.getScore() + GAMA_1);
                activerConnexionsFanal(f, h);
            }
            winnerListe = globalWinnersTakeAll(h, r.FANALS_PER_CLIQUE);
            ContextTypoNetwork.logger.debug("Nombre fanaux après GlobalWinners:" + winnerListe.size());
            // Troisieme phase est egal la premiere
            remiseZero(h);
            for (Fanal f : winnerListe) {
                f.setScore(f.getScore() + GAMA_1);
                activerConnexionsFanal(f, h);
            }
            while (!sameScore(winnerListe)) {
                winnerListe = loserKickOut(winnerListe, h);
                remiseZero(h);
                for (Fanal f : winnerListe) {
                    f.setScore(f.getScore() + GAMA_1);
                    activerConnexionsFanal(f, h);
                }
            }

        } else {
            if (r.BOOSTING && h >= 2) {

                // Pré-filtrage
                winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_2);
                LinkedList<Fanal> lstActives = new LinkedList<>(winnerListe);
                for (Fanal f : winnerListe) {
                    scoreActives.put(f, f.getScore());
                    ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                }

                // Deuxieme phase
                ContextTypoNetwork.logger.debug("Nombre fanaux avant GlobalWinners:" + winnerListe.size());
                LinkedList<Fanal> lstBoosting = new LinkedList<>(winnerListe);
                LinkedList<Fanal> winnerListeAux;
                winnerListe = new LinkedList<>();
                Fanal fanalBoosting;
                int ordreCliqueBoosting = r.FANALS_PER_CLIQUE;
                int ordreMaxClique = 0;
                boolean conditionArret = false;
                boolean cliqueTrouveGlobal = false;
                boolean cliqueTrouve;
                int nValides = 0;
                // Il s'arrete quand il trouve la clique maximale
                while (!conditionArret) {
                    // Il utilise le fanal avec plus d'activation pour le booster
                    remiseZero(h);
                    cliqueTrouve = false;
                    fanalBoosting = lstBoosting.getFirst();
                    fanalBoosting.setScore(fanalBoosting.getScore() + GAMA_1);
                    activerConnexionsFanal(fanalBoosting, h);
                    winnerListeAux = globalWinnersTakeAll(lstActives, GWsTA_vertical_2);
                    // Troisieme phase est egal la premiere
                    remiseZero(h);
                    for (Fanal f : winnerListeAux) {
                        f.setScore(f.getScore() + GAMA_1);
                        activerConnexionsFanal(f, h);
                    }
                    // Il fait convergir à une clique dont tous les fanaux ont le même score (afin d'éliminer les fanaux parasites)
                    while (!sameScore(winnerListeAux)) {
                        winnerListeAux = loserKickOut(winnerListeAux, h);
                        remiseZero(h);
                        for (Fanal f : winnerListeAux) {
                            f.setScore(f.getScore() + GAMA_1);
                            activerConnexionsFanal(f, h);
                        }
                    }
                    ContextTypoNetwork.logger.debug("Fanal boosté: " + fanalBoosting);
                    ContextTypoNetwork.logger.debug("Nombre fanaux après boosting:" + winnerListeAux.size());
                    // Valider un par cluster et le c fanaux obtenus pour la condition d'arret
                    if (winnerListeAux.size() == ordreCliqueBoosting) {
                        cliqueTrouveGlobal = true;
                        //ensembleFanaux = new HashSet<>(winnerListe);
                        //cliqueNorme.put(ensembleFanaux, DecodageTriang.calculerNormeScores(ensembleFanaux, scoreActives));
                        if (TriangularDecoder.UNION_BOOSTING) {
                            winnerListe = unionSets(winnerListe, winnerListeAux);
                        } else {
                            winnerListe = joinSets(winnerListe, winnerListeAux);
                        }

                        nValides++;
                    }

                    // Il garde la plus grande valeur de c trouvee
                    if (winnerListeAux.size() <= r.FANALS_PER_CLIQUE && winnerListeAux.size() > ordreMaxClique) {
                        ordreMaxClique = winnerListeAux.size();
                    }
                    // Il suprimme le fanal boosté
                    lstBoosting.remove(fanalBoosting);
                    if (cliqueTrouveGlobal && (lstBoosting.size() == 0) || ((nValides >= TriangularDecoder.MAX_BOOSTING) && TriangularDecoder.PLUSIEURS_BOOSTING)) {
                        conditionArret = true;
                        break;
                    }
                    // Il verifie si c'est necessaire d'autres iterations (par rapport à la plus grande valeur de c trouve)
                    if (lstBoosting.size() == 0 && ordreMaxClique != ordreCliqueBoosting) {
                        // Il redemarre la liste de fanaux du boosting
                        lstBoosting = new LinkedList<>(lstActives);
                        // Il remonte l'arbre de décision avec l'ordre de clique la plus grande
                        System.out.println("Change décision boosting: " + ordreCliqueBoosting + " -> " + ordreMaxClique);
                        ordreCliqueBoosting = ordreMaxClique;
                        conditionArret = false;
                    }
                }

            } else {
                if (r.SEUILLAGE) {
                    winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_2);
                    for (Fanal f : winnerListe) {
                        ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                    }
                    // Seuillage (Diminuer le nombre des fanaux parasites)
                    int nombre_fanaux_avant = winnerListe.size();
                    for (int i = 0; i < it_GWsTA * 10; i++) {
                        remiseZero(h);
                        LinkedList<Fanal> copy = new LinkedList<>(winnerListe);
                        for (Fanal f : winnerListe) {
                            f.setScore(f.getScore() + GAMA);
                            activerConnexionsFanal(f, h);
                        }
                        winnerListe = this.thresholdingFilter(h, r.FANALS_PER_CLIQUE + GAMA - 1);
                        if (winnerListe.size() == nombre_fanaux_avant || winnerListe.size() == 0) {
                            if (winnerListe.size() == 0) {
                                winnerListe = copy;
                            }
                            break;
                        }
                        nombre_fanaux_avant = winnerListe.size();
                        ContextTypoNetwork.logger.debug("Nombre fanaux après seuillage: " + winnerListe.size());
                    }

                } else {
                    winnerListe = globalWinnersTakeAll(h, GWsTA_vertical_1);
                    //winnerListe=this.thresholdingFilter(h,20);
                    for (Fanal f : winnerListe) {
                        ContextTypoNetwork.logger.info("it_vert " + f + " Score: " + f.getScore());
                    }
                }
            }
        }
        for (int i = 0; i < it_GWsTA; i++) {
            remiseZero(h);
            for (Fanal f : winnerListe) {
                f.setScore(f.getScore() + GAMA_1);
                activerConnexionsFanal(f, h);
            }
            if (r.MEIOSE && r.WINNER_SIDE) {
                if (r.WINNER_PARTITIONS) {
                    winnerListe = new LinkedList<>();
                    for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                        winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.RIGHT, j));
                    }
                    for (int j = 1; j <= r.WINNER_N_PARTITIONS_PAR_SIDE; j++) {
                        winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE), TriangularDecoder.LEFT, j));
                    }
                } else {
                    winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal / 2, TriangularDecoder.LEFT);
                    // Normalement ils sont ensembles disjointes
                    winnerListe = unionSets(winnerListe, globalWinnersTakeAll(h, GWsTA_horizontal / 2, TriangularDecoder.RIGHT));
                }
            } else {
                winnerListe = globalWinnersTakeAll(h, GWsTA_horizontal);
            }
        }
        // Fin de l'iteration horizontale
        for (Fanal f : winnerListe) {
            ContextTypoNetwork.logger.info("it_hor " + f + " Score: " + f.getScore());
        }
        // Début verifie les mots troubes
        LinkedList<String> motsMatch = ((TriangularLevel) this.r.getLevelsList().get(h)).searchClique(winnerListe);
        if (motsMatch.size() > 1) {
            ContextTypoNetwork.logger.info("Mauvais: Match ambigu: ");
            for (String s : motsMatch) {
                ContextTypoNetwork.logger.info(s);
            }
        }
        // Fin verifie les mots troubes
        ContextTypoNetwork.logger.info("------------------ Fin sequence--------------------");
        if (h != r.hMax - 1) {
            if (side == TriangularDecoder.RIGHT) {
                if (r.UNION) {
                    // Il réalise l'union des listes
                    bestScoresBottomUp.set(h, unionSets(bestScoresBottomUp.get(h), winnerListe));
                } else {
                    bestScoresBottomUp.set(h, joinSets(bestScoresBottomUp.get(h), winnerListe));
                }
                //Propagation verticale h+1
                for (Fanal f : bestScoresBottomUp.get(h)) {
                    activerFanauxSup(f, h);
                }
            }

            remiseZero(h - 1);
            remiseZero(h);
            if (side == TriangularDecoder.LEFT) {
                if (r.PROPAG_LATERALE) {
                    // Il active les sequences laterales
                    this.activerChaineTournois(winnerListe, h);
                }
                // Il memorise les winners de la clique à gauche
                bestScoresBottomUp.set(h, winnerListe);
            }
        }

        return winnerListe;
    }

    // Il utilise la distance de Hamming pour chercher les mots le plus proches
    public LinkedList<String> getCloserWords(LinkedList<Fanal> winnerListe, int h) {
        LinkedList<String> wordsMatch = ((TriangularLevel) this.r.getLevelsList().get(h)).searchClique(winnerListe);
        return wordsMatch;
    }

    public boolean propagationLetterBottomUp(String lettre, int side) {
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(0);
        Clique c;
        if (!n.existsClique(lettre)) {
            return false;
        }
        // Il prend la clique connu pour l'activer
        c = n.getCliqueLetter(lettre);
        ContextTypoNetwork.logger.debug("Activation lettre: " + lettre);
        if (side == TriangularDecoder.RIGHT) {

            if (r.UNION) {
                // Il réalise l'union des listes
                bestScoresBottomUp.set(0, unionSets(bestScoresBottomUp.get(0), c.getFanalsList()));
            } else {
                bestScoresBottomUp.set(0, joinSets(bestScoresBottomUp.get(0), c.getFanalsList()));
            }
            //Propagation verticale h+1
            for (Fanal f : bestScoresBottomUp.get(0)) {
                // Il active les fanaux connectes du niveau superieur et ses connexions

                activerFanauxSup(f, 0);
            }
        }
        if (side == TriangularDecoder.LEFT) {
            // Il active les sequences laterales
            //this.activerChaineTournois(winnerListe,h);
            // Il memorise les winners de la clique à gauche
            if (r.TESTE_INSERTIONS_LEFT) {
                if (c.getInfo().equals("<")) {
                    bestScoresBottomUp.set(0, c.getFanalsList());
                } else {
                    bestScoresBottomUp.set(0, unionSets(n.getRandomCliqueLetter().getFanalsList(), c.getFanalsList()));
                }
            } else {
                bestScoresBottomUp.set(0, c.getFanalsList());
            }

        }
        remiseZero(0);
        return true;
    }

    public boolean propagationLetterBottomUp(List<String> lettreListe, int side) {
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(0);
        Clique c = null;
        boolean pasDeClique = true;
        List<String> lettreListeCopie = new ArrayList<>(lettreListe);
        for (String lettre : lettreListe) {
            if (n.existsClique(lettre)) {
                pasDeClique = false;
            } else {
                lettreListeCopie.remove(lettre);
                System.out.println("Clique non existante: " + lettre);
            }
        }
        if (pasDeClique) {
            return false;
        }
        // Il prend la clique connu pour l'activer

        if (side == TriangularDecoder.RIGHT) {
            for (String lettre : lettreListeCopie) {
                c = n.getCliqueLetter(lettre);
                if (r.UNION) {
                    // Il réalise l'union des listes
                    bestScoresBottomUp.set(0, unionSets(bestScoresBottomUp.get(0), c.getFanalsList()));
                } else {
                    bestScoresBottomUp.set(0, joinSets(bestScoresBottomUp.get(0), c.getFanalsList()));
                }
            }

            //Propagation verticale h+1
            for (Fanal f : bestScoresBottomUp.get(0)) {
                // Il active les fanaux connectes du niveau superieur et ses connexions
                activerFanauxSup(f, 0);
            }
        }
        if (side == TriangularDecoder.LEFT) {
            // Il active les sequences laterales
            //this.activerChaineTournois(winnerListe,h);
            // Il memorise les winners de la clique à gauche
            if (r.TESTE_INSERTIONS_LEFT) {
                if (c.getInfo().equals("<")) {
                    bestScoresBottomUp.set(0, c.getFanalsList());
                } else {
                    bestScoresBottomUp.set(0, unionSets(n.getRandomCliqueLetter().getFanalsList(), c.getFanalsList()));
                }
            } else {
                for (int i = 0; i < lettreListeCopie.size(); i++) {
                    c = n.getCliqueLetter(lettreListeCopie.get(0));
                    if (i == 0) {
                        bestScoresBottomUp.set(0, c.getFanalsList());
                    } else {
                        if (r.UNION) {
                            // Il réalise l'union des listes
                            bestScoresBottomUp.set(0, unionSets(bestScoresBottomUp.get(0), c.getFanalsList()));
                        } else {
                            bestScoresBottomUp.set(0, joinSets(bestScoresBottomUp.get(0), c.getFanalsList()));
                        }
                    }

                }

            }

        }
        remiseZero(0);
        return true;
    }

    public void activerConnexionsFanal(Fanal f, int h) {
        Fanal l;
        Edge a;
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
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
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
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

    private void activerConnectionsChaineTournois(Fanal f_sous, int h) {
        Fanal l, f_niv;
        Edge a;
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        Edge[] listeArc = n.getSubGraphe().getEdgesList(f_sous);
        for (int i = 0; i < listeArc.length; i++) {
            a = null;
            if (listeArc[i] != null) {
                if (listeArc[i].getSourceFanal().equals(f_sous)) {
                    l = listeArc[i].getDestinationFanal();
                    listeArc[i].setActive(true);
                    f_niv = n.getGraph().getNumerotation().elementAt(n.getSubGraphe().getNumerotation().numero(l));
                    f_niv.setScore(f_niv.getScore() + 1);
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
            ContextTypoNetwork.logger.debug("Fanal actif: " + l);
            for (Fanal t : l.getSuperiorHyperFanals()) {
                t.setScore(t.getScore() + 1);
            }
        }
    }

    private void activerChaineTournois(LinkedList<Fanal> lst, int h) {
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        Fanal f_sous;
        for (Fanal f : lst) {
            //System.out.println("Il active le tournois: "+f);
            f_sous = n.getSubGraphe().getNumerotation().elementAt(n.getGraph().getNumerotation().numero(f));
            activerConnectionsChaineTournois(f_sous, h);
        }
    }

    // Utilisee pour le Reseau bigramme ouvert
    public void activerChaineTournois(LinkedList<Fanal> lst, TriangularLevel niveauFanauxInf, int h) {
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        Fanal f_sous;
        for (Fanal f : lst) {
            f_sous = n.getSubGraphe().getNumerotation().elementAt(niveauFanauxInf.getGraph().getNumerotation().numero(f));
            activerConnectionsChaineTournois(f_sous, h);
        }
    }

    public void remiseZero(int h) {
        TriangularLevel n;
        if (h == -1) {
            for (int i = 0; i < r.hMax; i++) {
                n = (TriangularLevel) r.getLevelsList().get(i);
                for (Fanal f : n.getGraph().getAllNodes()) {
                    f.setScore(0);
                }
            }
            return;
        }
        n = (TriangularLevel) r.getLevelsList().get(h);
        for (Fanal f : n.getGraph().getAllNodes()) {
            f.setScore(0);
            // Habiliter ligne quand utiliser le SOM
            //this.desactiverConnectionsFanal(f,h);
        }

    }

    public void remiseMemo() {
        seqsPropagBottomUp = new HashMap<>();
        bestScoresBottomUpLast = new LinkedList<>();
        for (int i = 0; i < r.hMax; i++) {
            this.bestScoresBottomUp.add(i, new LinkedList<Fanal>());
            seqsPropagTopDown.add(i, new HashMap<Integer, LinkedList<Fanal>>());
            seqsTopDownCounter[i] = 0;
        }
    }

    // Il prend en compte la position du cluster
    public LinkedList<Fanal> globalWinnersTakeAll(int h, int numElements, int side) {
        int cluster;
        LinkedList<Fanal> lstWinner = new LinkedList<>();
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        LinkedList<Fanal>[] vectorScores;
        vectorScores = (LinkedList<Fanal>[]) new LinkedList<?>[r.NOMBRE_FANAUX_PAR_CLUSTER * r.NOMBRE_CLUSTERS];
        for (int i = 0; i < vectorScores.length; i++) {
            vectorScores[i] = new LinkedList<>();
        }
        int maxScore = 0;
        for (Fanal f : n.getGraph().getAllNodes()) {
            cluster = n.getGraph().getNumerotation().numCluster(f);
            if (n.getSideCluster(cluster) == side) {
                vectorScores[f.getScore()].addLast(f);
                if (f.getScore() > maxScore) {
                    maxScore = f.getScore();
                }
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
        Fanal f, f_last;
        if (!lstWinner.isEmpty()) {
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
        }

        return lstWinner;
    }

    // Il prend en compte aussi la partition
    public LinkedList<Fanal> globalWinnersTakeAll(int h, int numElements, int side, int partition) {
        int cluster;
        LinkedList<Fanal> lstWinner = new LinkedList<>();
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        LinkedList<Fanal>[] vectorScores;
        vectorScores = (LinkedList<Fanal>[]) new LinkedList<?>[r.NOMBRE_FANAUX_PAR_CLUSTER * r.NOMBRE_CLUSTERS];
        for (int i = 0; i < vectorScores.length; i++) {
            vectorScores[i] = new LinkedList<>();
        }
        int maxScore = 0;
        for (Fanal f : n.getGraph().getAllNodes()) {
            cluster = n.getGraph().getNumerotation().numCluster(f);
            if (n.getPartitionCluster(cluster) == partition && n.getSideCluster(cluster) == side) {
                vectorScores[f.getScore()].addLast(f);
                if (f.getScore() > maxScore) {
                    maxScore = f.getScore();
                }
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
        Fanal f, f_last;
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

    public LinkedList<Fanal> loserKickOut(LinkedList<Fanal> lst_orig, int h) {
        LinkedList<Fanal> lst = new LinkedList<>();
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        int minScore = r.NOMBRE_FANAUX_PAR_CLUSTER * r.NOMBRE_CLUSTERS;
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

    public boolean sameScore(LinkedList<Fanal> lst_orig) {
        int firstScore = 0;
        boolean first = true;
        for (Fanal f : lst_orig) {
            if (first) {
                firstScore = f.getScore();
                first = false;
            } else {
                if (f.getScore() != firstScore) {
                    return false;
                }
            }
        }
        return true;
    }

    // Il prend les premiers numElements
    public LinkedList<Fanal> globalWinnersTakeAll(int h, int numElements) {

        LinkedList<Fanal> lstWinner = new LinkedList<>();
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        LinkedList<Fanal>[] vectorScores;
        vectorScores = (LinkedList<Fanal>[]) new LinkedList<?>[r.NOMBRE_FANAUX_PAR_CLUSTER * r.NOMBRE_CLUSTERS * 50];
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
        Fanal f, f_last;
        if (!lstWinner.isEmpty()) {
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
        }

        return lstWinner;
    }

    public LinkedList<Fanal> globalWinnersTakeAll(LinkedList<Fanal> lstFanaux, int numElements) {

        LinkedList<Fanal> lstWinner = new LinkedList<>();
        LinkedList<Fanal>[] vectorScores;
        vectorScores = (LinkedList<Fanal>[]) new LinkedList<?>[r.NOMBRE_FANAUX_PAR_CLUSTER * r.NOMBRE_CLUSTERS];
        for (int i = 0; i < vectorScores.length; i++) {
            vectorScores[i] = new LinkedList<>();
        }
        int maxScore = 0;
        for (Fanal f : lstFanaux) {
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
        Fanal f, f_last;
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

    public LinkedList<Fanal> thresholdingFilter(int h, int minScore) {
        LinkedList<Fanal> lstWithoutLosers = new LinkedList<>();
        TriangularLevel n = (TriangularLevel) r.getLevelsList().get(h);
        for (Fanal f : n.getGraph().getAllNodes()) {
            if (f.getScore() >= minScore) {
                lstWithoutLosers.add(f);
            }
        }
        return lstWithoutLosers;
    }

}
