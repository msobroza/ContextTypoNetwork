package control.network;

import control.NetworkControl;
import control.rules.LetterInformation;
import control.ContextTypoNetwork;
import control.decoder.Decoder;
import control.decoder.FuzzyDecoder;
import graph.FuzzyGraph;

import java.util.ArrayList;
import java.util.LinkedList;
import model.Clique;
import model.Cluster;
import model.FuzzyFanal;
import model.MacroFanal;

public class FuzzyNetwork extends Network {

    // Nombre de clusters dans le reseau
    public int NUMBER_CLUSTERS;
    // Nombre de fanaux par clique (il faut qu'il soit pair si sous-�chantillonage)
    public int FANALS_PER_CLIQUE;
    // Nombre de fanaux par clique (lettres)
    //public static int NOMBRE_FANAUX_PAR_CLIQUE_H0 = ReseauFlous.NOMBRE_CLUSTERS;
    // Nombre de fanaux par cluster (Il faut utiliser un nombre pair)
    public int FANALS_PER_CLUSTER;
    // Nombre maximal de niveaux
    public static int hMax = 1;
    // Il active l'apprentissage des caractères délimiteurs
    public static boolean USE_DELIMITERS = true;
    // Recouvrement ciculaire pour l'anticipation
    public final int R_CIRCULAR;

    // Utilise le flou de flou
    public static boolean FLOU2FLOU = true;

    // Active le mode d'apprentissage avec mitose de fanaux
    public static boolean USE_MITOSIS = true;
    // Nombre d'iterations de mitose max
    public static int NB_MITOSIS_MAX = 1000000000;
    // Seuil de degré entrant provoquant la mitose d'un fanal 
    public static int THRESHOLD_DEG_MITOSE = 25;

    public int TYPE_INFONS;

    // Niveau Standard -> Il n'y a pas d'arc
    private FuzzyLevel standartLevel;
    private final FuzzyDecoder decoder;

    // Constructeur d'un réseau standard
    public FuzzyNetwork(int X, int type_infons) {
        System.out.println("Nombre de clusters: " + X);
        this.TYPE_RESEAU = NetworkControl.FUZZY_NETWORK;
        this.NUMBER_CLUSTERS = X;
        this.FANALS_PER_CLIQUE = X;
        this.R_CIRCULAR = this.NUMBER_CLUSTERS - 1;
        FuzzyNetwork.hMax = 1;
        this.TYPE_INFONS = type_infons;
        if (TYPE_INFONS == InterfaceNetwork.INFORMATION_CONTENT_WORDS) {
            this.FANALS_PER_CLUSTER = SYMBOLES.length();
        } else {
            this.FANALS_PER_CLUSTER = PHONEMES_LIA.length;
        }
        levelsList = new LinkedList<>();
        hCounter = -1;
        // Creation d'un niveau standard

        createStandartLevel();
        // Creation des hMax niveaux
        for (int i = 0; i < hMax; i++) {
            this.copyLevel();
        }
        decoder = new FuzzyDecoder(this);

    }

    // Constructeur d'un réseau initialisé (sans connexions), de même architecture que le réseau rPrev    
    public FuzzyNetwork(FuzzyNetwork rPrev) {
        this.TYPE_RESEAU = NetworkControl.FUZZY_NETWORK;
        this.NUMBER_CLUSTERS = rPrev.NUMBER_CLUSTERS;
        this.FANALS_PER_CLIQUE = rPrev.FANALS_PER_CLIQUE;
        this.R_CIRCULAR = NUMBER_CLUSTERS - 1;
        this.TYPE_INFONS = rPrev.TYPE_INFONS;
        if (TYPE_INFONS == InterfaceNetwork.INFORMATION_CONTENT_WORDS) {
            this.FANALS_PER_CLUSTER = SYMBOLES.length();
        } else {
            this.FANALS_PER_CLUSTER = PHONEMES_LIA.length;
        }
        FuzzyNetwork.hMax = 1;
        levelsList = new LinkedList<>();

        hCounter = -1;

        for (int i = 0; i < hMax; i++) {
            this.copyLevel();
        }

        // Creation d'un niveau standard
        this.createStandartLevel();
        // Creation des hMax niveaux
        for (int i = 0; i < hMax; i++) {
            hCounter++;
            levelsList.add(hCounter, (FuzzyLevel) rPrev.getLevelsList().get(hCounter).copy(hCounter));
        }
        decoder = new FuzzyDecoder(this);

    }

    private void createStandartLevel() {
        MacroFanal mf;
        FuzzyFanal f;
        Cluster c;
        String letter;
        this.standartLevel = new FuzzyLevel(hCounter, this);

        for (int iClust = 0; iClust < NUMBER_CLUSTERS; iClust++) {
            //Ajouter cluster
            c = new Cluster("c:" + iClust);
            ((FuzzyGraph) this.standartLevel.getGraph()).ajouterCluster(c);
            if (FLOU2FLOU) {
                // Détermination du nombre de fanaux par macrofanal
                int nbFanauxParMF;
                if (USE_MITOSIS) {
                    nbFanauxParMF = 1;
                } else {
                    nbFanauxParMF = LetterInformation.NB_CAS;
                }

                for (int iMFanal = 0; iMFanal < FANALS_PER_CLUSTER; iMFanal++) {
                    // On crée un nouveau macrofanal pour la lettre courante
                    mf = new MacroFanal("c:" + iClust + ",mf:" + iMFanal, 0);
                    ((FuzzyGraph) this.standartLevel.getGraph()).addMacroFanal(mf);
                    if (this.TYPE_INFONS == InterfaceNetwork.INFORMATION_CONTENT_PHONEMES) {
                        letter = PHONEMES_LIA[iMFanal];
                    } else {
                        // Determine la lettre correspondant au macrofanal créé
                        letter = SYMBOLES.substring(iMFanal, iMFanal + 1);
                    }

                    // Associe la lettre au macrofanal
                    mf.setLettre(letter);
                    // Ajouter le macrofanal dans le cluster
                    c.addMacroFanal(mf);
                    // Ajouter le macrofanal dans le graphe
                    ((FuzzyGraph) this.standartLevel.getGraph()).addNode(mf);
                    // Creer l'association lettre -> numero de macrofanal dans cluster
                    c.linkMacroFanalLetter(mf, letter);
                    // Ajout des fanaux dans le macrofanal
                    for (int iFanal = 0; iFanal < nbFanauxParMF; iFanal++) {
                        // Creer un nouveau sommet
                        f = new FuzzyFanal("c:" + iClust + ",mf:" + iMFanal + ",f:" + iFanal, 0);
                        // Associe la lettre au fanal
                        f.setLettre(letter);
                        // Ajouter le fanal dans le macrofanal
                        mf.addFanal(f);
                        // Ajouter le fanal dans le cluster
                        c.addFanal(f);
                    }
                }
            } else {
                for (int iFanal = 0; iFanal < FANALS_PER_CLUSTER; iFanal++) {
                    // Creer un nouveau sommet
                    f = new FuzzyFanal("c:" + iClust + ",mf:" + iFanal, 0);
                    if (this.TYPE_INFONS == InterfaceNetwork.INFORMATION_CONTENT_PHONEMES) {
                        letter = PHONEMES_LIA[iFanal];
                    } else {
                        // Determine la lettre correspondant au macrofanal créé
                        letter = SYMBOLES.substring(iFanal, iFanal + 1);
                    }
                    // Associe la lettre au fanal
                    f.setLettre(letter);
                    // Ajouter le sommet dans le cluster
                    c.addFanal(f);
                    // Creer l'association lettre -> numero de fanal dans cluster
                    c.linkFanalLetter(f, f.getLetter());

                    // Ajouter le sommet dans le graphe
                    ((FuzzyGraph) this.standartLevel.getGraph()).addNode(f);
                }
            }
        }
    }

    private Level copyLevel() {
        hCounter++;
        if (hCounter >= hMax) {
            return null;
        }
        levelsList.add(hCounter, ((FuzzyLevel) standartLevel).copy(hCounter));
        return levelsList.get(hCounter);
    }

    /* @Override
     public void apprendreDictionnaire(int n, int taille) {
     ResultSet rs;
     String mot;
     DB bd = new DB();
     bd.open();
     if (taille == -1) {
     rs = bd.result("SELECT `nomlemme` FROM lemme WHERE LENGTH(CONVERT(nomlemme USING latin1))<=" + hMax + " ORDER BY RAND();");
     } else {
     rs = bd.result("SELECT `nomlemme` FROM lemme WHERE LENGTH(CONVERT(nomlemme USING latin1))=" + taille + " ORDER BY RAND();");
     // rs=bd.result("SELECT `nomlemme` FROM lemme WHERE LENGTH(CONVERT(nomlemme USING latin1))="+taille+" ORDER BY nomlemme;");
     }
     int c = 0;
     numDoubleLettre = 0;
     numDoubleLettreNon = 0;
     try {
     while (rs.next() && c < n) {

     // // On active les caractères délimiteurs
     //if(Reseau.CARAC_DELIM){
     //	motsAppris.addLast("<"+rs.getString("nomlemme")+">");
     //}
     //else
     //}
     // Récupération du mot à apprendre
     mot = rs.getString("nomlemme");
     motsAppris.addLast(mot);

     // On active les caractères délimiteurs
     if (ReseauFlous.CARAC_DELIM) {
     mot = "<" + mot + ">";
     }
     int taille_mot = mot.length();
     if (TypoMultireseaux.plusieursLettres) {
     for (int i = 0; i < ReseauFlous.NOMBRE_FANAUX_PAR_CLIQUE - taille_mot; i++) {
     mot = mot + "#";
     }
     }

     if (!isDoubleLettre(mot)) {
     if (isDoubleLettreNonCons(mot)) {
     numDoubleLettreNon = numDoubleLettreNon + 1.0;
     if (ReseauFlous.AVEC_DOUBLE_LETTRES_NON_CONSEC) {
     this.apprendreMot(mot);
     c++;
     } else {
     // On apprend mais on ne l'utilise pas pour le decodage
     this.apprendreMot(mot);
     c++;
     motsAppris.removeLast();
     }
     } else {
     this.apprendreMot(mot);
     c++;
     }
     } else {
     numDoubleLettreNon = numDoubleLettreNon + 1.0;
     numDoubleLettre = numDoubleLettre + 1.0;
     if (ReseauFlous.AVEC_DOUBLE_LETTRES) {
     this.apprendreMot(mot);
     c++;
     } else {
     // On apprend mais on ne l'utilise pas pour le decodage
     this.apprendreMot(mot);
     motsAppris.removeLast();
     }

     }
     if (ReseauFlous.MITOSE_FANAUX) {
     // Réalisation de la mitose si besoin
     realiserMitose();
     }
     if (c % 1000 == 0) {
     TypoMultireseaux.logger.info(c + " mots appris...");
     }
     }
     if (c < (n - 1)) {
     TypoMultireseaux.logger.warn("Warning: Pas assez de mots dans la base de données (" + c + " au lieu de " + n + ")");
     }
     } catch (SQLException ex) {
     Logger.getLogger(ReseauFlous.class.getName()).log(Level.SEVERE, null, ex);
     }

     } */
    public Decoder getDecodeur() {
        return this.decoder;
    }

    /**
     * Apprend un mot au r�seau. Surcharge de methode, � utiliser pour les
     * niveaux en anneaux
     *
     * @param mot
     * @return booleen
     */
    @Override
    public Clique learnWord(String mot) {
        FuzzyLevel n = (FuzzyLevel) this.getLevelsList().get(0);
        if (ContextTypoNetwork.VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT) {
            int taille = mot.length();
            for (int i = 0; i < this.NUMBER_CLUSTERS - taille; i++) {
                mot = mot + "*";
            }
        }
        if (n.existsClique(mot)) {
            // Si la clique pour ce mot existe deja, il est interessant de la renforcer
            // (à voir + tard)
            // Pour l'instant, cela signifie que l'on a rien à faire
            return n.getWordClique(mot);
        } else {
            n.addCircularAnticipation(mot, false);
        }
        return n.getWordClique(mot);
    }

    @Override
    public Clique learnPhoneme(String phon) {
        FuzzyLevel n = (FuzzyLevel) this.getLevelsList().get(0);
        if (n.existsClique(phon)) {
            // Si la clique pour ce mot existe deja, il est interessant de la renforcer
            // (à voir + tard)
            // Pour l'instant, cela signifie que l'on a rien à faire
            return n.getWordClique(phon);
        } else {
            n.addCircularAnticipation(phon, true);
        }
        return n.getWordClique(phon);
    }

    public int mitosis() {
        // Pour tout les macrofanaux, si le dernier des fanaux créé est saturé, on crée un nouveau fanal
        MacroFanal mf;
        FuzzyFanal f;
        FuzzyGraph G = (FuzzyGraph) this.getLevelsList().get(0).getGraph();
        int nbMitose = 0;
        for (int k = 0; k < G.getNumberMacroFanals(); k++) {
            // récupération du macrofanal courant
            mf = G.getMacroFanal(k);
            // récupération du dernier fanal crée dans mf
            f = mf.getListFanaux().getLast();
            // Si le dernier fanal créé dans le macrofanal est saturé, on crée un nouveau fanal dans le macrofanal
            if (f.getInDegree() >= FuzzyNetwork.THRESHOLD_DEG_MITOSE) {
                // Creer un nouveau sommet
                FuzzyFanal fNew = new FuzzyFanal(f, 0);
                // Rennomer le fanal
                fNew.setFanalName(mf.getFanalName() + ",f:" + mf.getListFanaux().size());
                // Ajouter le fanal dans le macrofanal
                mf.addFanal(fNew);
                // Ajouter le fanal dans le cluster
                mf.getCluster().addFanal(fNew);
                // TODO : supprimer l'ajout du fanal dans le graphe ???
                // Ajouter le fanal dans le graphe
                G.addNode(fNew);
                ContextTypoNetwork.logger.debug("Création du fanal " + fNew.getFanalName());
                nbMitose++;
            }
        }
        return nbMitose;
    }

    @Override
    public String toString() {
        String result = "";
        for (Level n : levelsList) {
            result += n.toString() + "\n";

        }
        return result;
    }

    public int getNbArcs() {
        int result = 0;
        for (int i = 0; i < this.getLevelsList().size(); i++) {
            result += ((FuzzyGraph) this.getLevelsList().get(i).getGraph()).getNumberEdges();
        }
        return result;
    }

    public int getNbFanaux() {
        int result = 0;
        for (int i = 0; i < this.getLevelsList().size(); i++) {
            result += ((FuzzyGraph) this.getLevelsList().get(i).getGraph()).getNumberFanals();
        }
        return result;
    }

    public int getNbMacroFanaux() {
        int result = 0;
        for (int i = 0; i < this.getLevelsList().size(); i++) {
            result += ((FuzzyGraph) this.getLevelsList().get(i).getGraph()).getNumberMacroFanals();
        }
        return result;
    }

    public LinkedList<Integer> getDistriFanauxDegSortant() {
        return ((FuzzyGraph) this.getLevelsList().get(0).getGraph()).getDistributionOutDegree();
    }

    public LinkedList<Integer> getDistriFanauxDegEntrant() {
        return ((FuzzyGraph) this.getLevelsList().get(0).getGraph()).getDistributionInDegree();
    }

    public ArrayList<FuzzyFanal> getFanauxGrandDegE(int seuilDegMitose) {
        return ((FuzzyGraph) this.getLevelsList().get(0).getGraph()).getFanauxGrandDegE(seuilDegMitose);
    }

    public double getDensite() {
        return ((FuzzyGraph) this.getLevelsList().get(0).getGraph()).getDensity();
    }

}
