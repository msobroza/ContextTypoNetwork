package graph;

import control.network.FuzzyNetwork;
import control.ContextTypoNetwork;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;

import model.Cluster;
import model.Fanal;
import model.FanalFlous;
import model.MacroFanal;
import tools.CircularLinkedList;

public class FuzzyGraph extends Graph {

    private ArrayList<ArrayList<Edge>> L;
    private LinkedList<Cluster> clusters;
    private LinkedList<MacroFanal> macroFanaux;
    private LinkedList<FanalFlous> fanaux;

    private Numerotation numerotation;
    private int arcsCompteur;

    public FuzzyGraph(int n) {
        this.numerotation = new Numerotation(n);
        L = new ArrayList<>();
        clusters = new CircularLinkedList<>();
        macroFanaux = new LinkedList<>();
        fanaux = new LinkedList<>();
        arcsCompteur = 0;
    }

    @Override
    public int taille() {
        if (FuzzyNetwork.FLOU2FLOU) {
            return macroFanaux.size();
        } else {
            return fanaux.size();
        }
    }

    @Override
    public FuzzyGraph copie(int h) {
        int n = this.taille();
        // Crée un nouveau graphe de type liste
        FuzzyGraph G = new FuzzyGraph(n);
        Cluster cCopie;

        //Ajoute tous les clusters dans le graphe
        for (int i = 0; i < this.clusters.size(); i++) {
            cCopie = new Cluster(this.clusters.get(i).getNom());
            G.ajouterCluster(cCopie);
        }

        if (FuzzyNetwork.FLOU2FLOU) {
            MacroFanal mfOrig, mfCopie;
            FanalFlous fCopie;

            // Ajoute les n macrofanaux dans le graphe
            for (int i = 0; i < n; i++) {
                // macrofanal du graphe d'origine (à copier)
                mfOrig = this.macroFanaux.get(i);
                // Détermine le cluster qui contiendra mfCopie dans le nouveau graphe
                cCopie = G.getCluster(this.clusters.indexOf(mfOrig.getCluster()));

                // macrofanal copié, à ajouter dans le nouveau graphe
                mfCopie = new MacroFanal(mfOrig, 0);
                // Supprime les références contenues dans le nouveau macrofanal (ce sont les références vers les fanaux de l'ancien macrofanal)
                mfCopie.setListFanaux(new LinkedList<FanalFlous>());
                mfCopie.setLettre(mfOrig.getLettre());
                
                // Ajoute le macrofanal dans le graphe
                G.ajouterMacroFanal(mfCopie);

                // Lie le cluster et le macrofanal
                cCopie.ajouterMacroFanal(mfCopie);
                // Crée l'association lettre -> numero de fanal dans cluster
                cCopie.associerMacroFanalLettre(mfCopie, mfCopie.getLettre());

                // Copie des fanaux contenus dans le macrofanal
                for (FanalFlous fOrig : mfOrig.getListFanaux()) {
                    // Copie le fanal contenu dans le macrofanal  
                    fCopie = new FanalFlous(fOrig, 0);
                    // Ajoute le fanal copie dans le macrofanal copie
                    mfCopie.ajouterFanal(fCopie);
                    // Ajoute le fanal copie dans le bon cluster
                    cCopie.ajouterFanal(fCopie);
                    // Creer l'association lettre -> numero de fanal dans cluster
                    cCopie.associerFanalLettre(fCopie, fCopie.getLettre());
                    // Ajoute le fanal dans le graphe
                    G.ajouterSommet(fCopie);
                }
            }
        } else {
            FanalFlous fOrig;
            FanalFlous fCopie;
            for (int i = 0; i < n; i++) {
                // Récupère le fanal original
                fOrig = this.fanaux.get(i);
                // Détermine le cluster qui contiendra mfCopie dans le nouveau graphe
                cCopie = G.getCluster(this.clusters.indexOf(fOrig.getCluster()));

                // Copie le sommet
                fCopie = new FanalFlous(fOrig, 0);
                fCopie.setNom("h:" + h + "," + fCopie.getNom());
                // Ajouter le sommet dans le cluster
                cCopie.ajouterFanal(fCopie);
                // Creer l'association lettre -> numero de fanal dans cluster
                cCopie.associerFanalLettre(fCopie, fCopie.getLettre());
                // Ajouter le sommet dans le graphe
                G.ajouterSommet(fCopie);
            }
        }

        for (int i = 0; i < fanaux.size(); i++) {
            // Recopie la liste des arcs d'une ligne
            ArrayList<Edge> Li = new ArrayList<Edge>();
            for (Edge a : this.L.get(i)) { // Prendre la liste d'arcs une ligne
                Li.add(a); // Il ajoute chaque élément de la colonne dans la bonne ordre
                G.arcsCompteur++;
            }
            G.L.set(i, Li);
        }
        return G;

        /*
         // Ajoute les n macrofanaux dans le graphe
         for(int i=0; i<n; i++){
         // macrofanal du graphe d'origine (à copier)
         mfOrig=(MacroFanal) this.macroFanaux.get(i);
         // macrofanal copié, à ajouter dans le nouveau graphe
         mfCopie=new MacroFanal(mfOrig,0);
            
         // TODO Changer nom pour contenir niveau à terme
         //t.setNom("h:"+h+","+t.getNom());
            
         // Ajoute le macrofanal dans le graphe
         G.ajouterMacroFanal(mfCopie);
            
         // Détermine le cluster contenant le sommet t dans le nouveau graphe
         cCopie = G.getCluster(this.clusters.indexOf(mfOrig.getCluster()));
         // Lie le cluster et le macrofanal
         cCopie.ajouterFanal(mfCopie);
            
         // Détermine le macrofanal contenant le sommet t dans le nouveau graphe
         // mf = G.getMacroFanal(this.macroFanaux.indexOf(s.getMacroFanal()));
         // Lie le cluster et le fanal
         // mf.ajouterFanal(t);
            
         // Creer l'association lettre -> numero de fanal dans cluster
         cCopie.associerFanalLettre(mfCopie, mfCopie.getLettre());
            
         // Copie des fanaux compris dans le macrofanal
         if(Reseau.FLOU2FLOU){
         // On supprime les références contenues dans le nouveau macrofanal (ce sont les références vers les fanaux de l'ancien macrofanal)
         mfCopie.setListFanaux(new LinkedList<Fanal>());
         Fanal fCopie;
            	
         // Détermination du nombre de fanaux par macrofanal
         int nbFanauxParMF;
         if (Reseau.MITOSE_FANAUX){
         nbFanauxParMF = 1;
         }
         else{
         nbFanauxParMF = CtxtQuestion.NB_CAS;
         }
            	
         for(int iFanal=0;iFanal<nbFanauxParMF;iFanal++){
         if(Reseau.MODELE_CIRCULAIRE){
         // Copie le fanal contenu dans le macrofanal t  
         fCopie = new Fanal(mfOrig.getListFanaux().get(iFanal), 0);
         // Ajouter le fanal dans le macrofanal
         mfCopie.ajouterFanal(fCopie);
         // Ajouter le sommet dans le cluster // On ne l'ajoute pas dans le cas du flou de flou, on résere cela pour le macrofanal
         // c.ajouterFanal(fCopie);
         // Ajouter le fanal dans le graphe
         G.ajouterFanal(fCopie);
         }
         }
         }
         }    
         for(int i=0; i<fanaux.size(); i++){
         // Recopie la liste des arcs d'une ligne
         ArrayList<Arc> Li=G.L.get(i);
         for(Arc a: L.get(i)){ // Prendre la liste d'arcs une ligne
         Li.add(a); // Il ajoute chaque élément de la colonne dans la bonne ordre
         G.arcsCompteur++;
         }    
         G.L.set(i, Li);
         }
         return G;
         */
    }

    public void ajouterCluster(Cluster c) {
        // TODO Auto-generated method stub
        clusters.add(c);

    }

    @Override
    public void ajouterSommet(Fanal s) {
        // Ajoute le sommet dans la liste de numerotation et dans la liste du graphe
        if (!this.fanaux.contains(((FanalFlous) s))) {
            ((FanalFlous) s).setId(fanaux.size());
            this.fanaux.add(((FanalFlous) s));
    		// TODO supprimer la partie commentée si dessous si fonctionnel

            //if(Reseau.FLOU2FLOU){ // en Flou de flou, les arcs relient les fanaux contenus dans les macrofanaux 
            this.L.add(((FanalFlous) s).getId(), new ArrayList<Edge>());
            //}
        }
    }

    public void ajouterMacroFanal(MacroFanal mf) {
        // Ajoute le sommet dans la liste de numerotation et dans la liste du graphe
        if (!this.macroFanaux.contains(mf)) {
            mf.setId(macroFanaux.size());
            this.macroFanaux.add(mf);

    		// TODO supprimer la partie commentée si dessous si fonctionnel
    		/*
             if(!Reseau.FLOU2FLOU){ // en mode sans Flou de flou, les arcs relient les macrofanaux
             this.L.add(mf.getId(), new ArrayList<Arc>());
             }
             */
        }
    }

    @Override
    public boolean existeArc(Fanal s, Fanal t) {
        for (Edge a : L.get(((FanalFlous) s).getId())) {
            if ((a.getDest()).equals(t)) {
                return true;
            }
        }
        return false;

    }

    public boolean existeArc(int i, int j) {
        FanalFlous t = this.fanaux.get(j);
        for (Edge a : L.get(i)) {
            if (a.getDest().equals(t)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void ajouterArc(Fanal s, Fanal t, int val, boolean oriente) {
        int si = ((FanalFlous) s).getId();
        L.get(si).add(new Edge(s, t, val, oriente));
        arcsCompteur++;

    }

    public void ajouterArc(int i, int j, int val, boolean oriente) {
        L.get(i).add(new Edge(this.fanaux.get(i),
                this.fanaux.get(j), val, oriente));
        arcsCompteur++;
    }

    @Override
    public int valeurArc(Fanal s, Fanal t) {
        for (Edge a : L.get(((FanalFlous) s).getId())) {
            if (a.getDest().equals(t)) {
                return a.getValeur();
            }
        }
        return -1; // Par convention la valeur pas utilis�e est -1
    }

    public Cluster getCluster(int id) {
        return clusters.get(id);
    }

    public MacroFanal getMacroFanal(int id) {
        return macroFanaux.get(id);
    }

    public int getNbArcs() {
        int result = 0;
        for (int i = 0; i < L.size(); i++) {
            result += L.get(i).size();
        }
        return result;
    }

    public int getNbFanaux() {
        return fanaux.size();
    }

    public int getNbMacroFanaux() {
        return macroFanaux.size();
    }

    public LinkedList<Integer> getDistriFanauxDegSortant() {
        int degMax = 100;
        int deg;
        LinkedList<Integer> result = new LinkedList<>();
        for (int i = 0; i <= degMax; i++) {
            result.add(0);
        }
        for (int i = 0; i < fanaux.size(); i++) {
            deg = Math.min(fanaux.get(i).getDegSortant(), degMax);
            result.set(deg, result.get(deg) + 1);
        }
        return result;
    }

    public LinkedList<Integer> getDistriFanauxDegEntrant() {
        int degMax = 300;
        return this.getDistriFanauxDegEntrant(degMax);
    }

    public LinkedList<Integer> getDistriFanauxDegEntrant(int degMax) {
        int deg;
        LinkedList<Integer> result = new LinkedList<>();

        // Initialisation de la distribution
        for (int i = 0; i <= degMax; i++) {
            result.add(0);
        }

        for (int i = 0; i < fanaux.size(); i++) {
            deg = Math.min(fanaux.get(i).getDegEntrant(), degMax);
            result.set(deg, result.get(deg) + 1);
        }

        for (int k = 0; k < this.fanaux.size(); k++) {
            if (fanaux.get(k).getDegEntrant() >= degMax) {
                ContextTypoNetwork.logger.debug("Fanal : " + this.fanaux.get(k).getNom() + " - " + this.fanaux.get(k).getLettre());
            }

        }

        return result;
    }

    public ArrayList<FanalFlous> getFanauxGrandDegE(int degMax) {
        ArrayList<FanalFlous> resultat = new ArrayList<>();
        int deg;
        LinkedList<Integer> degs = new LinkedList<>();
        // Initialisation du degré de chaque fanaux
        for (int i = 0; i < this.fanaux.size(); i++) {
            degs.add(0);
        }

        for (int i = 0; i < L.size(); i++) {
            for (int j = 0; j < L.get(i).size(); j++) {
                for (int k = 0; k < this.fanaux.size(); k++) {
                    if (L.get(i).get(j).getDest().equals(this.fanaux.get(k))) {
                        deg = Math.min(degs.get(k) + 1, degMax);
                        degs.set(k, deg);
                    }

                }
            }
        }
        for (int k = 0; k < this.fanaux.size(); k++) {
            if (degs.get(k) >= degMax) {
                resultat.add(this.fanaux.get(k));
            }
        }
        return resultat;
    }

    public double getDensite() {
        int nbConnexionsMax = 0;
        ArrayList<Integer> nbFanauxParCluster = new ArrayList<>();
        for (Cluster c : this.getClusters()) {
            nbFanauxParCluster.add(c.getListeFanaux().size());
        }
        for (int i = 0; i < nbFanauxParCluster.size(); i++) {
            for (int j = i + 1; j < nbFanauxParCluster.size(); j++) {
                nbConnexionsMax += nbFanauxParCluster.get(i) * nbFanauxParCluster.get(j);
            }
        }
        nbConnexionsMax *= 2; // On multiplie le nombre de connexions par deux dans le cas des graphes orientés
        return (double) this.getNbArcs() / nbConnexionsMax;
    }

    @Override
    public Edge getArc(Fanal s, Fanal t) {
        for (Edge a : L.get(((FanalFlous) s).getId())) {
            if (a.getDest().equals(t)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Edge[] getListeArc(Fanal s) {
        ArrayList<Edge> lst = L.get(((FanalFlous) s).getId());

        return (Edge[]) lst.toArray(new Edge[lst.size()]);
    }

    public int valeurArc(int i, int j) {
        FanalFlous t = this.fanaux.get(j);
        for (Edge a : L.get(i)) {
            if (a.getDest().equals(t)) {
                return a.getValeur();
            }
        }
        return -1; // Par convention la valeur pas utilisé est -1
    }

    @Override
    public void enleverArc(Fanal s, Fanal t) {
        int si = ((FanalFlous) s).getId();
        Edge a = null;
        for (Edge atemp : L.get(((FanalFlous) s).getId())) {
            if (atemp.getDest().equals(t)) {
                a = atemp;
                break;
            }
        }
        if (a != null) {
            L.get(((FanalFlous) s).getId()).remove(a);
        }
    }

    public void modifierValeur(FanalFlous s, FanalFlous t, int val) {
        for (Edge a : L.get(s.getId())) {
            if (a.getDest().equals(t)) {
                a.setValeur(val);
                return;
            }
        }

    }

    public ArrayList<Edge> voisins(FanalFlous s) {
        return L.get(s.getId());
    }

    @Override
    public Collection<Fanal> sommets() {
        LinkedList<Fanal> result = new LinkedList<Fanal>();
        for (FanalFlous fFlous : this.fanaux) {
            result.add((Fanal) fFlous);
        }
        return result;
    }

    public Collection<MacroFanal> macroSommets() {
        return this.macroFanaux;
    }

    // Retourne vrai si le caractère est dans la str
    public static boolean contient(String str, char c) {
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) == c) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {
        String result = "";
        for (int i = 0; i < this.taille(); i++) {
            for (Edge a : L.get(i)) // Prendre la liste d'arcs une ligne
            {
                result += a.toString();
            }
            result += "\n";
        }
        return result;
    }

    @Override
    public Numerotation getNumerotation() {
        return this.numerotation;
    }

    @Override
    public int getCompteurArcs() {
        return this.arcsCompteur;
    }

    public LinkedList<Cluster> getClusters() {
        return clusters;
    }

    public LinkedList<FanalFlous> getFanaux() {
        return fanaux;
    }
}
