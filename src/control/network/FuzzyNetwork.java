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
import model.FanalFlous;
import model.MacroFanal;

public class FuzzyNetwork extends Network {

    // Nombre de clusters dans le reseau
    public int NOMBRE_CLUSTERS;
    // Nombre de fanaux par clique (il faut qu'il soit pair si sous-�chantillonage)
    public int NOMBRE_FANAUX_PAR_CLIQUE;
    // Nombre de fanaux par clique (lettres)
    //public static int NOMBRE_FANAUX_PAR_CLIQUE_H0 = ReseauFlous.NOMBRE_CLUSTERS;
    // Nombre de fanaux par cluster (Il faut utiliser un nombre pair)
    public int NOMBRE_FANAUX_PAR_CLUSTER;
    // Nombre maximal de niveaux
    public static int hMax = 1;
    // Active le seuilage
    public static boolean SEUILLAGE = false;
    // Active le losers kick out
    public static boolean LOSERS_KICK_OUT = false;
    // Active double lettres
    public static boolean AVEC_DOUBLE_LETTRES = true;
    // Active double lettres non consecutives
    public static boolean AVEC_DOUBLE_LETTRES_NON_CONSEC = true;
    // Il active l'apprentissage des caractères délimiteurs
    public static boolean CARAC_DELIM = true;
    // Recouvrement ciculaire pour l'anticipation
    public final int RECOUVR_CIRCULAIRE;

    // Utilise le flou de flou
    public static boolean FLOU2FLOU = true;

    // Active le mode d'apprentissage avec mitose de fanaux
    public static boolean MITOSE_FANAUX = true;
    // Nombre d'iterations de mitose max
    public static int NB_MITOSE_MAX = 1000000000;
    // Seuil de degré entrant provoquant la mitose d'un fanal 
    public static int SEUIL_DEG_MITOSE = 25;

    public int TYPE_INFONS;

    // Niveau Standard -> Il n'y a pas d'arc
    private FuzzyLevel niveauStandard;
    private final FuzzyDecoder decodeur;

    // Constructeur d'un réseau standard
    public FuzzyNetwork(int X, int type_infons) {
        System.out.println("Nombre de clusters: "+X);
        this.TYPE_RESEAU = NetworkControl.RESEAU_FLOUS;
        this.NOMBRE_CLUSTERS = X;
        this.NOMBRE_FANAUX_PAR_CLIQUE = X;
        this.RECOUVR_CIRCULAIRE = this.NOMBRE_CLUSTERS - 1;
        FuzzyNetwork.hMax = 1;
        this.TYPE_INFONS = type_infons;
        if (TYPE_INFONS == InterfaceNetwork.TYPE_INFONS_MOTS) {
            this.NOMBRE_FANAUX_PAR_CLUSTER = SYMBOLES.length();
        } else {
            this.NOMBRE_FANAUX_PAR_CLUSTER = PHONEMES_LIA.length;
        }
        listeNiveaux = new LinkedList<>();
        hCounter = -1;
        // Creation d'un niveau standard

        creerNiveauStandard();
        // Creation des hMax niveaux
        for (int i = 0; i < hMax; i++) {
            this.creerNiveauCopie();
        }
        decodeur = new FuzzyDecoder(this);

    }

    // Constructeur d'un réseau initialisé (sans connexions), de même architecture que le réseau rPrev    
    public FuzzyNetwork(FuzzyNetwork rPrev) {
        this.TYPE_RESEAU = NetworkControl.RESEAU_FLOUS;
        this.NOMBRE_CLUSTERS = rPrev.NOMBRE_CLUSTERS;
        this.NOMBRE_FANAUX_PAR_CLIQUE = rPrev.NOMBRE_FANAUX_PAR_CLIQUE;
        this.RECOUVR_CIRCULAIRE = NOMBRE_CLUSTERS - 1;
        this.TYPE_INFONS = rPrev.TYPE_INFONS;
        if (TYPE_INFONS == InterfaceNetwork.TYPE_INFONS_MOTS) {
            this.NOMBRE_FANAUX_PAR_CLUSTER = SYMBOLES.length();
        } else {
            this.NOMBRE_FANAUX_PAR_CLUSTER = PHONEMES_LIA.length;
        }
        FuzzyNetwork.hMax = 1;
        listeNiveaux = new LinkedList<>();

        hCounter = -1;

        for (int i = 0; i < hMax; i++) {
            this.creerNiveauCopie();
        }

        // Creation d'un niveau standard
        this.creerNiveauStandard();
        // Creation des hMax niveaux
        for (int i = 0; i < hMax; i++) {
            hCounter++;
            listeNiveaux.add(hCounter, (FuzzyLevel) rPrev.getListeNiveaux().get(hCounter).copie(hCounter));
        }
        decodeur = new FuzzyDecoder(this);

    }

    private void creerNiveauStandard() {
        MacroFanal mf;
        FanalFlous f;
        Cluster c;
        String lettre;
        this.niveauStandard = new FuzzyLevel(hCounter, this);

        for (int iClust = 0; iClust < NOMBRE_CLUSTERS; iClust++) {
            //Ajouter cluster
            c = new Cluster("c:" + iClust);
            ((FuzzyGraph) this.niveauStandard.getGraphe()).ajouterCluster(c);
            if (FLOU2FLOU) {
                // Détermination du nombre de fanaux par macrofanal
                int nbFanauxParMF;
                if (MITOSE_FANAUX) {
                    nbFanauxParMF = 1;
                } else {
                    nbFanauxParMF = LetterInformation.NB_CAS;
                }

                for (int iMFanal = 0; iMFanal < NOMBRE_FANAUX_PAR_CLUSTER; iMFanal++) {
                    // On crée un nouveau macrofanal pour la lettre courante
                    mf = new MacroFanal("c:" + iClust + ",mf:" + iMFanal, 0);
                    ((FuzzyGraph) this.niveauStandard.getGraphe()).ajouterMacroFanal(mf);
                    if (this.TYPE_INFONS == InterfaceNetwork.TYPE_INFONS_PHONEMES) {
                        lettre = PHONEMES_LIA[iMFanal];
                    } else {
                        // Determine la lettre correspondant au macrofanal créé
                        lettre = SYMBOLES.substring(iMFanal, iMFanal + 1);
                    }

                    // Associe la lettre au macrofanal
                    mf.setLettre(lettre);
                    // Ajouter le macrofanal dans le cluster
                    c.ajouterMacroFanal(mf);
                    // Ajouter le macrofanal dans le graphe
                    ((FuzzyGraph) this.niveauStandard.getGraphe()).ajouterSommet(mf);
                    // Creer l'association lettre -> numero de macrofanal dans cluster
                    c.associerMacroFanalLettre(mf, lettre);
                    // Ajout des fanaux dans le macrofanal
                    for (int iFanal = 0; iFanal < nbFanauxParMF; iFanal++) {
                        // Creer un nouveau sommet
                        f = new FanalFlous("c:" + iClust + ",mf:" + iMFanal + ",f:" + iFanal, 0);
                        // Associe la lettre au fanal
                        f.setLettre(lettre);
                        // Ajouter le fanal dans le macrofanal
                        mf.ajouterFanal(f);
                        // Ajouter le fanal dans le cluster
                        c.ajouterFanal(f);
                    }
                }
            } else {
                for (int iFanal = 0; iFanal < NOMBRE_FANAUX_PAR_CLUSTER; iFanal++) {
                    // Creer un nouveau sommet
                    f = new FanalFlous("c:" + iClust + ",mf:" + iFanal, 0);
                    if (this.TYPE_INFONS == InterfaceNetwork.TYPE_INFONS_PHONEMES) {
                        lettre = PHONEMES_LIA[iFanal];
                    } else {
                        // Determine la lettre correspondant au macrofanal créé
                        lettre = SYMBOLES.substring(iFanal, iFanal + 1);
                    }
                    // Associe la lettre au fanal
                    f.setLettre(lettre);
                    // Ajouter le sommet dans le cluster
                    c.ajouterFanal(f);
                    // Creer l'association lettre -> numero de fanal dans cluster
                    c.associerFanalLettre(f, f.getLettre());

                    // Ajouter le sommet dans le graphe
                    ((FuzzyGraph) this.niveauStandard.getGraphe()).ajouterSommet(f);
                }
            }
        }
    }

    private Level creerNiveauCopie() {
        hCounter++;
        if (hCounter >= hMax) {
            return null;
        }
        listeNiveaux.add(hCounter, ((FuzzyLevel) niveauStandard).copie(hCounter));
        return listeNiveaux.get(hCounter);
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
        return this.decodeur;
    }

    /**
     * Apprend un mot au r�seau. Surcharge de methode, � utiliser pour les
     * niveaux en anneaux
     *
     * @param mot
     * @return booleen
     */
    @Override
    public Clique apprendreMot(String mot) {
        FuzzyLevel n = (FuzzyLevel) this.getListeNiveaux().get(0);
        if(ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_DROITE){
            int taille=mot.length();
            for(int i=0; i<this.NOMBRE_CLUSTERS-taille;i++){
                mot=mot+"*";
            }
        }
        if (n.existeClique(mot)) {
            // Si la clique pour ce mot existe deja, il est interessant de la renforcer
            // (à voir + tard)
            // Pour l'instant, cela signifie que l'on a rien à faire
            return n.getCliqueMot(mot);
        } else {
            n.ajouterAnticipCirculaire(mot, false);
        }
        return n.getCliqueMot(mot);
    }

    @Override
    public Clique apprendrePhoneme(String phon) {
        FuzzyLevel n = (FuzzyLevel) this.getListeNiveaux().get(0);
         if(ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_GAUCHE){
            int taille=NetworkControl.getLongueurPhon(phon);
            for(int i=0; i<this.NOMBRE_CLUSTERS-taille;i++){
                phon=phon+"##";
            }
        }
        if (n.existeClique(phon)) {
            // Si la clique pour ce mot existe deja, il est interessant de la renforcer
            // (à voir + tard)
            // Pour l'instant, cela signifie que l'on a rien à faire
            return n.getCliqueMot(phon);
        } else {
            n.ajouterAnticipCirculaire(phon, true);
        }
        return n.getCliqueMot(phon);
    }

    public int realiserMitose() {
        // Pour tout les macrofanaux, si le dernier des fanaux créé est saturé, on crée un nouveau fanal
        MacroFanal mf;
        FanalFlous f;
        FuzzyGraph G = (FuzzyGraph) this.getListeNiveaux().get(0).getGraphe();
        int nbMitose = 0;
        for (int k = 0; k < G.getNbMacroFanaux(); k++) {
            // récupération du macrofanal courant
            mf = G.getMacroFanal(k);
            // récupération du dernier fanal crée dans mf
            f = mf.getListFanaux().getLast();
            // Si le dernier fanal créé dans le macrofanal est saturé, on crée un nouveau fanal dans le macrofanal
            if (f.getDegEntrant() >= FuzzyNetwork.SEUIL_DEG_MITOSE) {
                // Creer un nouveau sommet
                FanalFlous fNew = new FanalFlous(f, 0);
                // Rennomer le fanal
                fNew.setNom(mf.getNom() + ",f:" + mf.getListFanaux().size());
                // Ajouter le fanal dans le macrofanal
                mf.ajouterFanal(fNew);
                // Ajouter le fanal dans le cluster
                mf.getCluster().ajouterFanal(fNew);
                // TODO : supprimer l'ajout du fanal dans le graphe ???
                // Ajouter le fanal dans le graphe
                G.ajouterSommet(fNew);
                ContextTypoNetwork.logger.debug("Création du fanal " + fNew.getNom());
                nbMitose++;
            }
        }
        return nbMitose;
    }

    @Override
    public String toString() {
        String result = "";
        for (Level n : listeNiveaux) {
            result += n.toString() + "\n";

        }
        return result;
    }

    public int getNbArcs() {
        int result = 0;
        for (int i = 0; i < this.getListeNiveaux().size(); i++) {
            result += ((FuzzyGraph) this.getListeNiveaux().get(i).getGraphe()).getNbArcs();
        }
        return result;
    }

    public int getNbFanaux() {
        int result = 0;
        for (int i = 0; i < this.getListeNiveaux().size(); i++) {
            result += ((FuzzyGraph) this.getListeNiveaux().get(i).getGraphe()).getNbFanaux();
        }
        return result;
    }

    public int getNbMacroFanaux() {
        int result = 0;
        for (int i = 0; i < this.getListeNiveaux().size(); i++) {
            result += ((FuzzyGraph) this.getListeNiveaux().get(i).getGraphe()).getNbMacroFanaux();
        }
        return result;
    }

    public LinkedList<Integer> getDistriFanauxDegSortant() {
        return ((FuzzyGraph) this.getListeNiveaux().get(0).getGraphe()).getDistriFanauxDegSortant();
    }

    public LinkedList<Integer> getDistriFanauxDegEntrant() {
        return ((FuzzyGraph) this.getListeNiveaux().get(0).getGraphe()).getDistriFanauxDegEntrant();
    }

    public ArrayList<FanalFlous> getFanauxGrandDegE(int seuilDegMitose) {
        return ((FuzzyGraph) this.getListeNiveaux().get(0).getGraphe()).getFanauxGrandDegE(seuilDegMitose);
    }

    public double getDensite() {
        return ((FuzzyGraph) this.getListeNiveaux().get(0).getGraphe()).getDensite();
    }

}
