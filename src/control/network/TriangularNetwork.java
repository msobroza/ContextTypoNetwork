package control.network;

import control.NetworkControl;
import control.rules.PhonemeRules;
import control.ContextTypoNetwork;
import control.decoder.Decoder;
import control.decoder.TriangularDecoder;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import model.Clique;
import model.Fanal;

public class TriangularNetwork extends Network {

    // Nombre de clusters dans le réseau (X)
    public int NOMBRE_CLUSTERS = 300;
    // Nombre de fanaux par clique (il faut qu'il soit pair) (c)
    public int FANALS_PER_CLIQUE = 8;
    // Nombre de fanaux par clique (lettres) -> Le nombre de clusters doit être plus grand que 50
    public int FANALS_PER_CLIQUE_H0 = 20;
    // Nombre de fanaux par cluster (Il faut utiliser un nombre pair) (l)
    public int FANALS_PER_CLUSTER = 300;
    // Active la creation d'un niveau supplementaires (unites composes)
    public boolean ADDITIONAL_LEVEL = true;
    // Nombre de clusters du niveau suplementaire
    public int CLUSTERS_SUP = 500;
    // Nombre de fanaux par cluster du niveau suplementaire
    public int FANALS_PER_CLUSTER_SUP = 500;
    // Nombre maximal de niveaux
    public int hMax = 14;
    // Active la propagation laterale
    public boolean LATERAL_PROPAGATION = true;
    // Active la propagation laterale a partir du niveau
    public int STARTING_LATERAL_PROPAGATION_LEVEL = 1;
    // Active le seuilage
    public boolean SEUILLAGE = false;
    // Active le losers kick out
    public boolean LOSERS_KICK_OUT = false;
    // Active le decodage par boosting
    public boolean BOOSTING = true;
    // Active l'union de fanaux avant propagation verticale
    public boolean UNION = true;
    // Active le sous-échantillonage
    public boolean SUBSAMPLING_H0 = false;
    // Taux d'échantillonage en H0
    public double SUBSIMPLING_RATE_H0 = 0.5;
    // Active la non superposition des fanaux en H0
    public boolean WITHOUT_SUPERPOSITON_H0 = true;
    // Active l'apprentissage que d'une partie du reseau
    public boolean LEARN_SUBNETWORK = false;
    public int HYPER_LINK_LEVELS = 5;
    // Active l'apprentissage de recouvrement entre les niveaux
    public boolean DISTANCE_HYPER_LINK = false;
    // Active Hyper liaison (liaison entre le premier niveau et le dernier)
    public boolean DECODING_HYPER_LINK = true;
    // Active Hyper liaison des bigrammes (liaison entre le deuxieme niveau et le dernier)
    public boolean DECODING_HYPER_LINK_BIGRAMMES = true;
    // Il active l'apprentissage des caractères délimiteurs
    public boolean USE_DELIMITER_CHAR = true;
    // Il active le modèle de la méiose
    public boolean MEIOSIS = true;
    // Il active le modèle de la meiose avec des liaisons unidirectionnelles montantes
    public boolean MEIOSIS_LINK_UNIDIREC = true;
    // Il active la division du winner take all par side (utiliser pour le meiose)
    public boolean WINNER_SIDE = true;
    // Il active la division du winner take all en plusieurs divisions
    public boolean WINNER_PARTITIONS = false;
    // Nombre de partitions par side (c= alpha*2n) et 2n<=c
    public int WINNER_N_PARTITIONS_PAR_SIDE = 2;
    // Il active le decodage top down (Il faut activer la meiose_liaison_uni pour fonctionner correctement)
    public boolean TOP_DOWN = false;
    // C'est un parametre pour identifier si c'est un reseau interface
    public final boolean INTERFACE;

    // Niveau Standart -> Il n'y a pas des arcs
    protected TriangularLevel niveauStandart;
    // Niveau Supplementaire
    protected TriangularLevel superiorLevel;
    // private NiveauTriang repetitionLettres;
    protected final TriangularDecoder decodeur;

    public TriangularNetwork(int l, int X, int c, int hMax, boolean ADDITIONAL_LEVEL, boolean INTERFACE) {
        this.TYPE_NETWORK= NetworkControl.TypeNetwork.TRIANGULAR_NETWORK;
        this.FANALS_PER_CLUSTER = l;
        this.NOMBRE_CLUSTERS = X;
        this.FANALS_PER_CLIQUE = c;
        this.hMax = hMax;
        this.ADDITIONAL_LEVEL = ADDITIONAL_LEVEL;
        this.INTERFACE = INTERFACE;
        levelsList = new LinkedList<>();
        hCounter = -1;
        // Creation d'un niveau standart qui va etre replie pour tous les autres
        createStandartLevel();
        if (this.ADDITIONAL_LEVEL) {
            // Creation des hMax niveaux
            for (int i = 0; i < hMax - 1; i++) {
                copyLevel();
            }
            createAdditionalLevel();

        } else {
            for (int i = 0; i < hMax; i++) {
                copyLevel();
            }
        }
        decodeur = new TriangularDecoder(this);

    }

    private void createAdditionalLevel() {
        Fanal s;
        this.superiorLevel = new TriangularLevel(hCounter, this, FANALS_PER_CLUSTER_SUP, CLUSTERS_SUP);
        int cluster;
        hCounter++;
        //hCounter==hMax-1
        for (int iClust = 0; iClust < CLUSTERS_SUP; iClust++) {
            //Ajouter cluster
            cluster = superiorLevel.getGraph().getNumerotation().addCluster();
            for (int iFanal = 0; iFanal < FANALS_PER_CLUSTER_SUP; iFanal++) {
                // Creer un nouveau sommet
                s = new Fanal("c:" + iClust + ",f:" + iFanal, 0);
                //Ajouter le sommet dans le graphe
                this.superiorLevel.getGraph().addNode(s);
                // Ajouter le sommet dans le cluster
                this.superiorLevel.getGraph().getNumerotation().addFanalCluster(cluster, s);
            }
        }
        levelsList.add(hCounter, this.superiorLevel);
    }

    private void createStandartLevel() {
        Fanal s;
        this.niveauStandart = new TriangularLevel(hCounter, this, FANALS_PER_CLUSTER, NOMBRE_CLUSTERS);
        int cluster;
        for (int iClust = 0; iClust < NOMBRE_CLUSTERS; iClust++) {
            //Ajouter cluster
            cluster = niveauStandart.getGraph().getNumerotation().addCluster();
            for (int iFanal = 0; iFanal < FANALS_PER_CLUSTER; iFanal++) {
                // Creer un nouveau sommet
                s = new Fanal("c:" + iClust + ",f:" + iFanal, 0);
                //Ajouter le sommet dans le graphe
                this.niveauStandart.getGraph().addNode(s);
                // Ajouter le sommet dans le cluster
                this.niveauStandart.getGraph().getNumerotation().addFanalCluster(cluster, s);
            }
        }
    }

    private Level copyLevel() {
        hCounter++;
        if (hCounter == hMax) {
            return null;
        }
        levelsList.add(hCounter, (TriangularLevel) niveauStandart.copy(hCounter));
        return levelsList.get(hCounter);
    }

    public Level getSuperiorLevel() {
        return this.superiorLevel;
    }
    
    @Override
    public Clique learnWord(String mot) {
        List<String> unit = new ArrayList<>();
        Level n;
        // Il ajoute chaque lettre dans l'unite
        for (int c = 0; c < mot.length(); c++) {
            unit.add(mot.substring(c, c + 1));
        }
        if (learnSubword(unit, unit.size(), true, true)) {
            if (!this.ADDITIONAL_LEVEL) {
                n = (TriangularLevel) levelsList.get(unit.size() - 1);
            } else {
                n = this.superiorLevel;
            }
            if (n.existsClique(mot)) {
                return n.getWordClique(mot);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // Cette methode est differente de celle de mots car les unites de phonemes ont longueur inconnu
    @Override
    public Clique learnPhoneme(String phon) {
        TriangularLevel n;
        System.out.println("Este eh o phonema: "+phon);
        List<String> listePhon = PhonemeRules.splitPhoneme(phon);
        if (learnSubword(listePhon, listePhon.size(), true, true)) {
            if (!this.ADDITIONAL_LEVEL) {
                n = (TriangularLevel) levelsList.get(listePhon.size() - 1);
            } else {
                n = this.superiorLevel;
            }
            if (n.existsClique(phon)) {
                return n.getWordClique(phon);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected boolean learnSubword(List<String> unite, int maxLength, boolean begin, boolean end) {
        List<String> seqLeft = new ArrayList<>();
        List<String> seqRight = new ArrayList<>();
        if (unite.size() == 1) {
            return learnSequence(unite, seqLeft, seqRight, maxLength, begin, end);
        } else {
            seqLeft = unite.subList(0, unite.size() - 1);
            seqRight = unite.subList(1, unite.size());
            if (learnSubword(seqLeft, maxLength, begin, false) && learnSubword(seqRight, maxLength, false, end)) {
                return learnSequence(unite, seqLeft, seqRight, maxLength, begin, end);
            } else {
                return false;
            }
        }

    }

    private boolean learnSequence(List<String> listeSeq, List<String> listeSeqLeft, List<String> listeSeqRight, int maxLength, boolean begin, boolean end) {
        // 1- Choisir les fanaux
        // 2- Representer la clique dans le bon niveau
        // 3- Sous-echantilonner la clique et representer la clique supérieure
        // 4- Lier les fanaux des niveaux differentes (Fanal sup/Fanal inf)
        // 5- Representer la chaine de tournois pour la liaison des deux cliques
        String seq = "", seqLeft = "", seqRight = "";
        for (String s : listeSeq) {
            seq += s;
        }
        for (String s : listeSeqLeft) {
            seqLeft += s;
        }
        for (String s : listeSeqRight) {
            seqRight += s;
        }
        ContextTypoNetwork.logger.debug(seq + "= " + seqLeft + " | " + seqRight);
        TriangularLevel n;
        // Si le niveau supplementaire est active il le prendre pour realiser les connexions
        if (!this.ADDITIONAL_LEVEL || listeSeq.size() != maxLength) {
            n = (TriangularLevel) levelsList.get(listeSeq.size() - 1);
        } else {
            n = this.superiorLevel;
        }

        if (n.existsClique(seq) || (this.ADDITIONAL_LEVEL && this.LEARN_SUBNETWORK && listeSeq.size() > this.HYPER_LINK_LEVELS && listeSeq.size() != maxLength && (this.DECODING_HYPER_LINK || this.DECODING_HYPER_LINK_BIGRAMMES ))) {
            // C'est interresant de renforcer la clique
            // Soit on change le taux d'echant ou le nombre des noueds dans la clique
            // Soit on utilise la même clique ou on utilise une autre
            return true;
        } else {
            // Si la sous échantillonnage entre le premiere et le deuxieme niveau est activée
            // Le premiere niveau a nombre_fanaux_par_clique_h0 fanaux
            if (seq.length() == 1 && this.SUBSAMPLING_H0) {
                if (this.USE_DELIMITER_CHAR && (seq.equals(BEGIN_WORD_SYMBOL) || (seq.equals(END_WORD_SYMBOL)))) {
                    n.addClique(seq, listeSeq.size(), seqLeft, listeSeqLeft.size(), seqRight, listeSeqRight.size(), this.FANALS_PER_CLIQUE_H0 * 2);
                } else {
                    n.addClique(seq, listeSeq.size(), seqLeft, listeSeqLeft.size(), seqRight, listeSeqRight.size(), this.FANALS_PER_CLIQUE_H0);
                }

            } else {
                n.addClique(seq, listeSeq.size(), seqLeft, listeSeqLeft.size(), seqRight, listeSeqRight.size(), this.FANALS_PER_CLIQUE);
                if (listeSeq.size() == maxLength && (this.DECODING_HYPER_LINK || this.DECODING_HYPER_LINK_BIGRAMMES )) {
                    // Ajouter hyper liaison au premier niveau
                    // On considere qu'on a déjà les cliques gardés dans la memoire
                    n.ajouterHyperLiaison(listeSeq);

                }
            }
            return true;
        }
    }

    public double recognizePhoneme(String phoneme, String phonemeRecherche) {
        Double result;
        decodeur.remiseMemo();

        if (this.DECODING_HYPER_LINK_BIGRAMMES) {
            decodeur.recognizeBottomUpHyper2Levels(PhonemeRules.correctsPhonemesToList(PhonemeRules.splitPhoneme(BEGIN_WORD_SYMBOL + phoneme + END_WORD_SYMBOL)), phonemeRecherche);
        } else {
            decodeur.reconnaitreBottomUpPhoneme(PhonemeRules.splitPhoneme(BEGIN_WORD_SYMBOL + phoneme + END_WORD_SYMBOL), TriangularDecoder.LEFT, true, true);
        }
        if (this.ADDITIONAL_LEVEL) {
            result = decodeur.verifieDecodageBottomUp(BEGIN_WORD_SYMBOL + phonemeRecherche + END_WORD_SYMBOL, this.hMax - 1);
        } else {
            result = decodeur.verifieDecodageBottomUp(BEGIN_WORD_SYMBOL + phonemeRecherche + END_WORD_SYMBOL, PhonemeRules.splitPhoneme(BEGIN_WORD_SYMBOL + phoneme + END_WORD_SYMBOL).size() - 1);
        }
        return result;
    }

    public double recognizePhoneme(LinkedHashMap<String, ArrayList<String>> phon, String phonemeSearched) {
        Double result;
        decodeur.remiseMemo();

        if (this.DECODING_HYPER_LINK_BIGRAMMES) {
            decodeur.recognizeBottomUpHyper2Levels(PhonemeRules.graphemePhonemesToList(PhonemeRules.removeNullPhoneme(phon)), phonemeSearched);
        }
        if (this.ADDITIONAL_LEVEL) {

            result = decodeur.verifieDecodageBottomUp(phonemeSearched, this.hMax - 1);
        } else {
            result = decodeur.verifieDecodageBottomUp(BEGIN_WORD_SYMBOL + phonemeSearched + END_WORD_SYMBOL, this.hMax - 1);
        }

        return result;
    }

    public LinkedList<Fanal> getUnitFanals(String unite) {
        return decodeur.getWinnersSeqBottomUp(unite);
    }

    public Decoder getDecoder() {
        return this.decodeur;
    }

    public double getMatchingRateInterfaceNetwork(LinkedList<Fanal> fanauxGagnants, String motAppris) {
        int nombreFanauxTrouves = 0;
        LinkedList<Fanal> fanauxCorrects = this.getLevelsList().get(0).getWordClique(motAppris).getFanalsList();
        for (Fanal f : fanauxCorrects) {
            if (fanauxGagnants.contains(f)) {
                nombreFanauxTrouves++;
            }
        }
        return ((double) nombreFanauxTrouves) / this.FANALS_PER_CLIQUE;
    }

}