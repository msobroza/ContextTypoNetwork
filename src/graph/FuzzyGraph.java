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
    private LinkedList<MacroFanal> macroFanalsList;
    private LinkedList<FanalFlous> fanals;

    private Numerotation numerotation;
    private int counterEdges;

    public FuzzyGraph(int n) {
        this.numerotation = new Numerotation(n);
        L = new ArrayList<>();
        clusters = new CircularLinkedList<>();
        macroFanalsList = new LinkedList<>();
        fanals = new LinkedList<>();
        counterEdges = 0;
    }

    @Override
    public int size() {
        if (FuzzyNetwork.FLOU2FLOU) {
            return macroFanalsList.size();
        } else {
            return fanals.size();
        }
    }

    @Override
    public FuzzyGraph copyGraph(int h) {
        int n = this.size();
        // Crée un nouveau graphe de type liste
        FuzzyGraph G = new FuzzyGraph(n);
        Cluster cCopie;

        //Ajoute tous les clusters dans le graphe
        for (int i = 0; i < this.clusters.size(); i++) {
            cCopie = new Cluster(this.clusters.get(i).getClusterName());
            G.ajouterCluster(cCopie);
        }

        if (FuzzyNetwork.FLOU2FLOU) {
            MacroFanal mfOrig, mfCopie;
            FanalFlous fCopie;

            // Ajoute les n macrofanaux dans le graphe
            for (int i = 0; i < n; i++) {
                // macrofanal du graphe d'origine (à copier)
                mfOrig = this.macroFanalsList.get(i);
                // Détermine le cluster qui contiendra mfCopie dans le nouveau graphe
                cCopie = G.getCluster(this.clusters.indexOf(mfOrig.getCluster()));

                // macrofanal copié, à ajouter dans le nouveau graphe
                mfCopie = new MacroFanal(mfOrig, 0);
                // Supprime les références contenues dans le nouveau macrofanal (ce sont les références vers les fanaux de l'ancien macrofanal)
                mfCopie.setListFanaux(new LinkedList<FanalFlous>());
                mfCopie.setLettre(mfOrig.getLetter());

                // Ajoute le macrofanal dans le graphe
                G.addMacroFanal(mfCopie);

                // Lie le cluster et le macrofanal
                cCopie.addMacroFanal(mfCopie);
                // Crée l'association lettre -> numero de fanal dans cluster
                cCopie.linkMacroFanalLetter(mfCopie, mfCopie.getLetter());

                // Copie des fanaux contenus dans le macrofanal
                for (FanalFlous fOrig : mfOrig.getListFanaux()) {
                    // Copie le fanal contenu dans le macrofanal  
                    fCopie = new FanalFlous(fOrig, 0);
                    // Ajoute le fanal copie dans le macrofanal copie
                    mfCopie.addFanal(fCopie);
                    // Ajoute le fanal copie dans le bon cluster
                    cCopie.addFanal(fCopie);
                    // Creer l'association lettre -> numero de fanal dans cluster
                    cCopie.linkFanalLetter(fCopie, fCopie.getLetter());
                    // Ajoute le fanal dans le graphe
                    G.addNode(fCopie);
                }
            }
        } else {
            FanalFlous fOrig;
            FanalFlous fCopie;
            for (int i = 0; i < n; i++) {
                // Récupère le fanal original
                fOrig = this.fanals.get(i);
                // Détermine le cluster qui contiendra mfCopie dans le nouveau graphe
                cCopie = G.getCluster(this.clusters.indexOf(fOrig.getCluster()));

                // Copie le sommet
                fCopie = new FanalFlous(fOrig, 0);
                fCopie.setFanalName("h:" + h + "," + fCopie.getFanalName());
                // Ajouter le sommet dans le cluster
                cCopie.addFanal(fCopie);
                // Creer l'association lettre -> numero de fanal dans cluster
                cCopie.linkFanalLetter(fCopie, fCopie.getLetter());
                // Ajouter le sommet dans le graphe
                G.addNode(fCopie);
            }
        }

        for (int i = 0; i < fanals.size(); i++) {
            // Recopie la liste des arcs d'une ligne
            ArrayList<Edge> Li = new ArrayList<Edge>();
            for (Edge a : this.L.get(i)) { // Prendre la liste d'arcs une ligne
                Li.add(a); // Il ajoute chaque élément de la colonne dans la bonne ordre
                G.counterEdges++;
            }
            G.L.set(i, Li);
        }
        return G;
    }

    public void ajouterCluster(Cluster c) {
        // TODO Auto-generated method stub
        clusters.add(c);

    }

    @Override
    public void addNode(Fanal s) {
        // Ajoute le sommet dans la liste de numerotation et dans la liste du graphe
        if (!this.fanals.contains(((FanalFlous) s))) {
            ((FanalFlous) s).setId(fanals.size());
            this.fanals.add(((FanalFlous) s));
    		// TODO supprimer la partie commentée si dessous si fonctionnel

            //if(Reseau.FLOU2FLOU){ // en Flou de flou, les arcs relient les fanaux contenus dans les macrofanaux 
            this.L.add(((FanalFlous) s).getId(), new ArrayList<Edge>());
            //}
        }
    }

    public void addMacroFanal(MacroFanal mf) {
        // Ajoute le sommet dans la liste de numerotation et dans la liste du graphe
        if (!this.macroFanalsList.contains(mf)) {
            mf.setId(macroFanalsList.size());
            this.macroFanalsList.add(mf);
        }
    }

    @Override
    public boolean existsEdge(Fanal s, Fanal t) {
        for (Edge a : L.get(((FanalFlous) s).getId())) {
            if ((a.getDestinationFanal()).equals(t)) {
                return true;
            }
        }
        return false;

    }

    public boolean existsEdge(int i, int j) {
        FanalFlous t = this.fanals.get(j);
        for (Edge a : L.get(i)) {
            if (a.getDestinationFanal().equals(t)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addEdge(Fanal s, Fanal t, int val, boolean oriente) {
        int si = ((FanalFlous) s).getId();
        L.get(si).add(new Edge(s, t, val, oriente));
        counterEdges++;

    }

    public void ajouterArc(int i, int j, int val, boolean oriente) {
        L.get(i).add(new Edge(this.fanals.get(i),
                this.fanals.get(j), val, oriente));
        counterEdges++;
    }

    @Override
    public int getEdgeValue(Fanal s, Fanal t) {
        for (Edge a : L.get(((FanalFlous) s).getId())) {
            if (a.getDestinationFanal().equals(t)) {
                return a.getValue();
            }
        }
        return -1; // Par convention la valeur pas utilis�e est -1
    }

    public Cluster getCluster(int id) {
        return clusters.get(id);
    }

    public MacroFanal getMacroFanal(int id) {
        return macroFanalsList.get(id);
    }

    public int getNumberEdges() {
        int result = 0;
        for (int i = 0; i < L.size(); i++) {
            result += L.get(i).size();
        }
        return result;
    }

    public int getNumberFanals() {
        return fanals.size();
    }

    public int getNumberMacroFanals() {
        return macroFanalsList.size();
    }

    public LinkedList<Integer> getDistributionOutDegree() {
        int degMax = 100;
        int deg;
        LinkedList<Integer> result = new LinkedList<>();
        for (int i = 0; i <= degMax; i++) {
            result.add(0);
        }
        for (int i = 0; i < fanals.size(); i++) {
            deg = Math.min(fanals.get(i).getOutDegree(), degMax);
            result.set(deg, result.get(deg) + 1);
        }
        return result;
    }

    public LinkedList<Integer> getDistributionInDegree() {
        int degMax = 300;
        return this.getDistributionInDegree(degMax);
    }

    public LinkedList<Integer> getDistributionInDegree(int degMax) {
        int deg;
        LinkedList<Integer> result = new LinkedList<>();

        // Initialisation de la distribution
        for (int i = 0; i <= degMax; i++) {
            result.add(0);
        }

        for (int i = 0; i < fanals.size(); i++) {
            deg = Math.min(fanals.get(i).getInDegree(), degMax);
            result.set(deg, result.get(deg) + 1);
        }

        for (int k = 0; k < this.fanals.size(); k++) {
            if (fanals.get(k).getInDegree() >= degMax) {
                ContextTypoNetwork.logger.debug("Fanal : " + this.fanals.get(k).getFanalName() + " - " + this.fanals.get(k).getLetter());
            }

        }

        return result;
    }

    public ArrayList<FanalFlous> getFanauxGrandDegE(int degMax) {
        ArrayList<FanalFlous> resultat = new ArrayList<>();
        int deg;
        LinkedList<Integer> degs = new LinkedList<>();
        // Initialisation du degré de chaque fanaux
        for (int i = 0; i < this.fanals.size(); i++) {
            degs.add(0);
        }

        for (int i = 0; i < L.size(); i++) {
            for (int j = 0; j < L.get(i).size(); j++) {
                for (int k = 0; k < this.fanals.size(); k++) {
                    if (L.get(i).get(j).getDestinationFanal().equals(this.fanals.get(k))) {
                        deg = Math.min(degs.get(k) + 1, degMax);
                        degs.set(k, deg);
                    }

                }
            }
        }
        for (int k = 0; k < this.fanals.size(); k++) {
            if (degs.get(k) >= degMax) {
                resultat.add(this.fanals.get(k));
            }
        }
        return resultat;
    }

    public double getDensity() {
        int nbConnexionsMax = 0;
        ArrayList<Integer> nbFanauxParCluster = new ArrayList<>();
        for (Cluster c : this.getClusters()) {
            nbFanauxParCluster.add(c.getFanalsList().size());
        }
        for (int i = 0; i < nbFanauxParCluster.size(); i++) {
            for (int j = i + 1; j < nbFanauxParCluster.size(); j++) {
                nbConnexionsMax += nbFanauxParCluster.get(i) * nbFanauxParCluster.get(j);
            }
        }
        nbConnexionsMax *= 2; // On multiplie le nombre de connexions par deux dans le cas des graphes orientés
        return (double) this.getNumberEdges() / nbConnexionsMax;
    }

    @Override
    public Edge getEdge(Fanal s, Fanal t) {
        for (Edge a : L.get(((FanalFlous) s).getId())) {
            if (a.getDestinationFanal().equals(t)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Edge[] getEdgesList(Fanal s) {
        ArrayList<Edge> lst = L.get(((FanalFlous) s).getId());

        return (Edge[]) lst.toArray(new Edge[lst.size()]);
    }

    public int edgeValue(int i, int j) {
        FanalFlous t = this.fanals.get(j);
        for (Edge a : L.get(i)) {
            if (a.getDestinationFanal().equals(t)) {
                return a.getValue();
            }
        }
        return -1; // Par convention la valeur pas utilisé est -1
    }

    @Override
    public void removeEdge(Fanal s, Fanal t) {
        int si = ((FanalFlous) s).getId();
        Edge a = null;
        for (Edge atemp : L.get(((FanalFlous) s).getId())) {
            if (atemp.getDestinationFanal().equals(t)) {
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
            if (a.getDestinationFanal().equals(t)) {
                a.setValue(val);
                return;
            }
        }

    }

    public ArrayList<Edge> neighboors(FanalFlous s) {
        return L.get(s.getId());
    }

    @Override
    public Collection<Fanal> getAllNodes() {
        LinkedList<Fanal> result = new LinkedList<Fanal>();
        for (FanalFlous fFlous : this.fanals) {
            result.add((Fanal) fFlous);
        }
        return result;
    }

    public Collection<MacroFanal> getMacroFanalsList() {
        return this.macroFanalsList;
    }

    // Retourne vrai si le caractère est dans la str
    public static boolean contains(String str, char c) {
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
        for (int i = 0; i < this.size(); i++) {
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
    public int getEdgesCounter() {
        return this.counterEdges;
    }

    public LinkedList<Cluster> getClusters() {
        return clusters;
    }

    public LinkedList<FanalFlous> getFanalsList() {
        return fanals;
    }
}
