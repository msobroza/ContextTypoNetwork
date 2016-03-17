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
    public int NOMBRE_FANAUX_PAR_CLIQUE = 8;
    // Nombre de fanaux par clique (lettres) -> Le nombre de clusters doit être plus grand que 50
    public int NOMBRE_FANAUX_PAR_CLIQUE_H0 = 20;
    // Nombre de fanaux par cluster (Il faut utiliser un nombre pair) (l)
    public int NOMBRE_FANAUX_PAR_CLUSTER = 300;
    // Active la creation d'un niveau supplementaires (unites composes)
    public boolean NIVEAU_SUPPLEMENTAIRE = true;
    // Nombre de clusters du niveau suplementaire
    public int NOMBRE_CLUSTERS_SUP = 500;
    // Nombre de fanaux par cluster du niveau suplementaire
    public int NOMBRE_FANAUX_PAR_CLUSTER_SUP = 500;
    // Nombre maximal de niveaux
    public int hMax = 14;
    // Active la propagation laterale
    public boolean PROPAG_LATERALE = true;
    // Active la propagation laterale a partir du niveau
    public int PROPAG_LATERALE_A_PARTIR_NIVEAU = 1;
    // Active le seuilage
    public boolean SEUILLAGE = false;
    // Active le losers kick out
    public boolean LOSERS_KICK_OUT = false;
    // Active le decodage par boosting
    public boolean BOOSTING = true;
    // Active l'union de fanaux avant propagation verticale
    public boolean UNION = true;
    // Active double lettres
    public boolean AVEC_DOUBLE_LETTRES = true;
    // Active double lettres non consecutives
    public boolean AVEC_DOUBLE_LETTRES_NON_CONSEC = true;
    // Active le sous-échantillonage
    public boolean SOUS_ECHANT_H0 = false;
    // Taux d'échantillonage en H0
    public double TAUX_ECHANT_H0 = 0.5;
    // Active la non superposition des fanaux en H0
    public boolean SANS_SUPERPOSITON_H0 = true;
    // Active l'apprentissage que d'une partie du reseau
    public boolean SOUS_PARTIE_RESEAU = false;
    public int HYPER_LIAISON_NOMBRE_NIVEAUX = 5;
    // Active l'apprentissage de recouvrement entre les niveaux
    public boolean RECOUVREMENT_HYPER_LIAISON = false;
    // Active Hyper liaison (liaison entre le premier niveau et le dernier)
    public boolean DECODAGE_HYPER_LIAISON = true;
    // Active Hyper liaison des bigrammes (liaison entre le deuxieme niveau et le dernier)
    public boolean DECODAGE_HYPER_LIAISON_BIGRAMMES = true;
    // Active Hyper liaison des bigrammes (liaison entre le troisieme niveau et le dernier)
    public boolean DECODAGE_HYPER_LIAISON_TRIGRAMMES = false;
    // Il active l'apprentissage des caractères délimiteurs
    public boolean CARAC_DELIM = true;
    // Il active le modèle de la méiose
    public boolean MEIOSE = true;
    // Il active le modèle de la meiose avec des liaisons unidirectionnelles montantes
    public boolean MEIOSE_LIAISON_UNI = true;
    // Il active la division du winner take all par side (utiliser pour le meiose)
    public boolean WINNER_SIDE = true;
    // Il active la division du winner take all en plusieurs divisions
    public boolean WINNER_PARTITIONS = false;
    // Nombre de partitions par side (c= alpha*2n) et 2n<=c
    public int WINNER_N_PARTITIONS_PAR_SIDE = 2;
    // Il active le decodage top down (Il faut activer la meiose_liaison_uni pour fonctionner correctement)
    public boolean TOP_DOWN = false;
    // Teste modele triangulaire avec des insertions
    public final boolean TESTE_INSERTIONS_LEFT = false;
    // C'est un parametre pour identifier si c'est un reseau interface
    public final boolean INTERFACE;
    // Il active une couche cachee de bigrammes ouverts
    public final boolean COUCHE_CACHE_BIGRAMMES_OUVERTS;

    // Niveau Standart -> Il n'y a pas des arcs
    protected TriangularLevel niveauStandart;
    // Niveau Supplementaire
    protected TriangularLevel niveauSup;
    // private NiveauTriang repetitionLettres;
    protected final TriangularDecoder decodeur;

    public TriangularNetwork(int l, int X, int c, int hMax, boolean NIVEAU_SUPPLEMENTAIRE, boolean COUCHE_CACHE_BIGRAMMES_OUVERTS,  boolean INTERFACE) {
        this.TYPE_RESEAU= NetworkControl.RESEAU_TRIANG;
        this.NOMBRE_FANAUX_PAR_CLUSTER = l;
        this.NOMBRE_CLUSTERS = X;
        this.NOMBRE_FANAUX_PAR_CLIQUE = c;
        this.hMax = hMax;
        this.NIVEAU_SUPPLEMENTAIRE = NIVEAU_SUPPLEMENTAIRE;
        this.INTERFACE = INTERFACE;
        this.COUCHE_CACHE_BIGRAMMES_OUVERTS = COUCHE_CACHE_BIGRAMMES_OUVERTS;
        listeNiveaux = new LinkedList<>();
        hCounter = -1;
        // Creation d'un niveau standart qui va etre replie pour tous les autres
        creerNiveauStandard();
        if (this.NIVEAU_SUPPLEMENTAIRE) {
            // Creation des hMax niveaux
            for (int i = 0; i < hMax - 1; i++) {
                creerNiveauCopie();
            }
            creerNiveauSupplementaire();

        } else {
            for (int i = 0; i < hMax; i++) {
                creerNiveauCopie();
            }
        }
        decodeur = new TriangularDecoder(this);

    }

    private void creerNiveauSupplementaire() {
        Fanal s;
        this.niveauSup = new TriangularLevel(hCounter, this, NOMBRE_FANAUX_PAR_CLUSTER_SUP, NOMBRE_CLUSTERS_SUP);
        int cluster;
        hCounter++;
        //hCounter==hMax-1
        for (int iClust = 0; iClust < NOMBRE_CLUSTERS_SUP; iClust++) {
            //Ajouter cluster
            cluster = niveauSup.getGraphe().getNumerotation().ajouterCluster();
            for (int iFanal = 0; iFanal < NOMBRE_FANAUX_PAR_CLUSTER_SUP; iFanal++) {
                // Creer un nouveau sommet
                s = new Fanal("c:" + iClust + ",f:" + iFanal, 0);
                //Ajouter le sommet dans le graphe
                this.niveauSup.getGraphe().ajouterSommet(s);
                // Ajouter le sommet dans le cluster
                this.niveauSup.getGraphe().getNumerotation().ajouterSommetCluster(cluster, s);
            }
        }
        listeNiveaux.add(hCounter, this.niveauSup);
    }

    private void creerNiveauStandard() {
        Fanal s;
        this.niveauStandart = new TriangularLevel(hCounter, this, NOMBRE_FANAUX_PAR_CLUSTER, NOMBRE_CLUSTERS);
        int cluster;
        for (int iClust = 0; iClust < NOMBRE_CLUSTERS; iClust++) {
            //Ajouter cluster
            cluster = niveauStandart.getGraphe().getNumerotation().ajouterCluster();
            for (int iFanal = 0; iFanal < NOMBRE_FANAUX_PAR_CLUSTER; iFanal++) {
                // Creer un nouveau sommet
                s = new Fanal("c:" + iClust + ",f:" + iFanal, 0);
                //Ajouter le sommet dans le graphe
                this.niveauStandart.getGraphe().ajouterSommet(s);
                // Ajouter le sommet dans le cluster
                this.niveauStandart.getGraphe().getNumerotation().ajouterSommetCluster(cluster, s);
            }
        }
    }

    private Level creerNiveauCopie() {
        hCounter++;
        if (hCounter == hMax) {
            return null;
        }
        listeNiveaux.add(hCounter, (TriangularLevel) niveauStandart.copie(hCounter));
        return listeNiveaux.get(hCounter);
    }

    public Level getNiveauSup() {
        return this.niveauSup;
    }
    
    @Override
    public Clique apprendreMot(String mot) {
        List<String> unite = new ArrayList<>();
        Level n;
        // Il ajoute chaque lettre dans l'unite
        for (int c = 0; c < mot.length(); c++) {
            unite.add(mot.substring(c, c + 1));
        }
        if (apprendreSousPartie(unite, unite.size(), true, true)) {
            if (!this.NIVEAU_SUPPLEMENTAIRE) {
                n = (TriangularLevel) listeNiveaux.get(unite.size() - 1);
            } else {
                n = this.niveauSup;
            }
            if (n.existeClique(mot)) {
                return n.getCliqueMot(mot);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    // Cette methode est differente de celle de mots car les unites de phonemes ont longueur inconnu
    @Override
    public Clique apprendrePhoneme(String phon) {
        TriangularLevel n;

        List<String> listePhon = PhonemeRules.separePhonemes(phon, ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA);
        if (apprendreSousPartie(listePhon, listePhon.size(), true, true)) {
            if (!this.NIVEAU_SUPPLEMENTAIRE) {
                n = (TriangularLevel) listeNiveaux.get(listePhon.size() - 1);
            } else {
                n = this.niveauSup;
            }
            if (n.existeClique(phon)) {
                return n.getCliqueMot(phon);
            } else {
                return null;
            }
        } else {
            return null;
        }
    }

    protected boolean apprendreSousPartie(List<String> unite, int maxLength, boolean begin, boolean end) {
        List<String> seqLeft = new ArrayList<>();
        List<String> seqRight = new ArrayList<>();
        if (unite.size() == 1) {
            return apprendreSeq(unite, seqLeft, seqRight, maxLength, begin, end);
        } else {
            seqLeft = unite.subList(0, unite.size() - 1);
            seqRight = unite.subList(1, unite.size());
            if (apprendreSousPartie(seqLeft, maxLength, begin, false) && apprendreSousPartie(seqRight, maxLength, false, end)) {
                return apprendreSeq(unite, seqLeft, seqRight, maxLength, begin, end);
            } else {
                return false;
            }
        }

    }

    private boolean apprendreSeq(List<String> listeSeq, List<String> listeSeqLeft, List<String> listeSeqRight, int maxLength, boolean begin, boolean end) {
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
        if (!this.NIVEAU_SUPPLEMENTAIRE || listeSeq.size() != maxLength) {
            n = (TriangularLevel) listeNiveaux.get(listeSeq.size() - 1);
        } else {
            n = this.niveauSup;
        }

        if (n.existeClique(seq) || (this.NIVEAU_SUPPLEMENTAIRE && this.SOUS_PARTIE_RESEAU && listeSeq.size() > this.HYPER_LIAISON_NOMBRE_NIVEAUX && listeSeq.size() != maxLength && (this.DECODAGE_HYPER_LIAISON || this.DECODAGE_HYPER_LIAISON_BIGRAMMES || this.DECODAGE_HYPER_LIAISON_TRIGRAMMES))) {
            // C'est interresant de renforcer la clique
            // Soit on change le taux d'echant ou le nombre des noueds dans la clique
            // Soit on utilise la même clique ou on utilise une autre
            return true;
        } else {
            // Si la sous échantillonnage entre le premiere et le deuxieme niveau est activée
            // Le premiere niveau a nombre_fanaux_par_clique_h0 fanaux
            if (seq.length() == 1 && this.SOUS_ECHANT_H0) {
                if (this.CARAC_DELIM && (seq.equals("<") || (seq.equals(">")))) {
                    n.ajouterClique(seq, listeSeq.size(), seqLeft, listeSeqLeft.size(), seqRight, listeSeqRight.size(), this.NOMBRE_FANAUX_PAR_CLIQUE_H0 * 2);
                } else {
                    n.ajouterClique(seq, listeSeq.size(), seqLeft, listeSeqLeft.size(), seqRight, listeSeqRight.size(), this.NOMBRE_FANAUX_PAR_CLIQUE_H0);
                }

            } else {
                n.ajouterClique(seq, listeSeq.size(), seqLeft, listeSeqLeft.size(), seqRight, listeSeqRight.size(), this.NOMBRE_FANAUX_PAR_CLIQUE);
                if (listeSeq.size() == maxLength && (this.DECODAGE_HYPER_LIAISON || this.DECODAGE_HYPER_LIAISON_BIGRAMMES || this.DECODAGE_HYPER_LIAISON_TRIGRAMMES)) {
                    // Ajouter hyper liaison au premier niveau
                    // On considere qu'on a déjà les cliques gardés dans la memoire
                    n.ajouterHyperLiaison(listeSeq);

                }
            }
            return true;
        }
    }

    public double reconnaitrePhoneme(String phoneme, String phonemeRecherche) {
        Double result;
        decodeur.remiseMemo();

        if (this.DECODAGE_HYPER_LIAISON_BIGRAMMES) {
            decodeur.reconnaitreBottomUpHyperDeuxNiveaux(PhonemeRules.phonemesCorrectesToList(PhonemeRules.separePhonemes("<" + phoneme + ">", ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA)), phonemeRecherche);
        } else {
            decodeur.reconnaitreBottomUpPhoneme(PhonemeRules.separePhonemes("<" + phoneme + ">", ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA), TriangularDecoder.LEFT, true, true);
        }
        if (this.NIVEAU_SUPPLEMENTAIRE) {
            result = decodeur.verifieDecodageBottomUp("<" + phonemeRecherche + ">", this.hMax - 1);
        } else {
            result = decodeur.verifieDecodageBottomUp("<" + phonemeRecherche + ">", PhonemeRules.separePhonemes("<" + phoneme + ">", ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA).size() - 1);
        }
        return result;
    }

    public double reconnaitrePhoneme(LinkedHashMap<String, ArrayList<String>> phon, String phonemeRecherche) {
        Double result;
        decodeur.remiseMemo();

        if (this.DECODAGE_HYPER_LIAISON_BIGRAMMES) {
            decodeur.reconnaitreBottomUpHyperDeuxNiveaux(PhonemeRules.graphemePhonemesToList(PhonemeRules.supprimePhonemesNulls(phon)), phonemeRecherche);
        } else {
            // decodeur.reconnaitreBottomUp(ReglesPhonemes.separePhonemes("<" + phoneme + ">"), DecodageTriang.LEFT, true, true);
        }
        if (this.NIVEAU_SUPPLEMENTAIRE) {

            result = decodeur.verifieDecodageBottomUp(phonemeRecherche, this.hMax - 1);
        } else {
            result = decodeur.verifieDecodageBottomUp("<" + phonemeRecherche + ">", this.hMax - 1);
            //result = decodeur.verifieDecodageBottomUp("<" + phonemeRecherche + ">", ReglesPhonemes.separePhonemes("<" + phoneme + ">").size() - 1);
        }

        return result;
    }

    public double reconnaitreMot(String mot, String motRecherche) {
        Double result;
        decodeur.remiseMemo();

        if (!this.TOP_DOWN) {
            if (this.DECODAGE_HYPER_LIAISON_BIGRAMMES || this.DECODAGE_HYPER_LIAISON_TRIGRAMMES) {
                if (this.DECODAGE_HYPER_LIAISON_TRIGRAMMES) {
                    decodeur.reconnaitreBottomUpHyperTroisNiveaux(mot);
                } else {
                    decodeur.reconnaitreBottomUpHyperDeuxNiveaux(mot);
                }

            } else {
                if (this.DECODAGE_HYPER_LIAISON) {
                    decodeur.reconnaitreBottomUpHyperLettres(mot);
                } else {
                    decodeur.reconnaitreBottomUpMot(mot, TriangularDecoder.LEFT, true, true);
                }
            }
        } else {
            decodeur.reconnaitreTopDown(this.getListeNiveaux().get(mot.length() - 1).getCliqueMot(mot).getListe(), mot.length());
        }

        if (this.TOP_DOWN) {
            result = decodeur.verifieDecodageTopDown(motRecherche);
        } else {
            result = decodeur.verifieDecodageBottomUp(motRecherche, motRecherche.length() - 1);
        }
        return result;
    }

    public LinkedList<Fanal> getFanauxUnite(String unite) {
        return decodeur.getWinnersSeqBottomUp(unite);
    }

    public Decoder getDecodeur() {
        return this.decodeur;
    }

    public double getTauxMatchingInterfaceReseaux(LinkedList<Fanal> fanauxGagnants, String motAppris) {
        int nombreFanauxTrouves = 0;
        LinkedList<Fanal> fanauxCorrects = this.getListeNiveaux().get(0).getCliqueMot(motAppris).getListe();
        for (Fanal f : fanauxCorrects) {
            if (fanauxGagnants.contains(f)) {
                nombreFanauxTrouves++;
            }
        }
        return ((double) nombreFanauxTrouves) / this.NOMBRE_FANAUX_PAR_CLIQUE;
    }

}