package graph;

import java.util.ArrayList;
import java.util.Collection;
import model.Fanal;

public class TriangularGraph extends Graph {

    private ArrayList<ArrayList<Edge>> L;
    private Numerotation numerotation;
    private int arcsCompteur;

    public TriangularGraph(int n) {
        this.numerotation = new Numerotation(n);
        L = new ArrayList<>();
        arcsCompteur = 0;
    }

    @Override
    public int size() {
        return L.size();
    }

    @Override
    public TriangularGraph copyGraph(int h) {
        int n = this.size();
        // Creer une nouvelle liste de graphes
        TriangularGraph G = new TriangularGraph(n);
        Fanal s;
        Fanal t;
        for (int i = 0; i < this.numerotation.getListeClusters().size(); i++) {
            //Ajoute tous les clusters dans le graphe
            G.numerotation.addCluster();
        }
        for (int i = 0; i < n; i++) {
            // Ajoute les n sommes dans le graphe
            s = this.numerotation.getElement(i);
            t = new Fanal(s, 0);
            // Duplie le fanal
            t.setFanalName("h:" + h + "," + t.getFanalName());
            G.addNode(t);
            t = G.numerotation.getElement(i);
            // Ajoute le sommet dans le bon cluster
            G.numerotation.addFanalCluster(this.numerotation.numCluster(s), t);
        }
        for (int i = 0; i < n; i++) {
            // Recopie la liste des arcs d'une ligne
            ArrayList<Edge> Li = G.L.get(i);
            for (Edge a : L.get(i)) { // Prendre la liste d'arcs une ligne
                Li.add(a); // Il ajoute chaque élément de la colonne dans la bonne ordre
                G.arcsCompteur++;
            }
            G.L.set(i, Li);
        }
        return G;

    }

    @Override
    public void addNode(Fanal s) {
        if (this.numerotation.addElement(s)) // Modifiquei set por add
        {
            L.add(this.numerotation.getNumber(s), new ArrayList<Edge>());
        }
    }

    @Override
    public boolean existsEdge(Fanal s, Fanal t) {
        for (Edge a : L.get(numerotation.getNumber(s))) {
            if ((a.getDestinationFanal()).equals(t)) {
                return true;
            }
        }
        return false;

    }

    public boolean existsEdge(int i, int j) {
        Fanal t = numerotation.getElement(j);
        for (Edge a : L.get(i)) {
            if (a.getDestinationFanal().equals(t)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void addEdge(Fanal s, Fanal t, int val, boolean oriente) {
        this.addNode(s);
        this.addNode(t);
        int si = this.numerotation.getNumber(s);
        L.get(si).add(new Edge(s, t, val, oriente));
        arcsCompteur++;

    }

    public void ajouterArc(int i, int j, int val, boolean oriente) {
        L.get(i).add(new Edge(this.numerotation.getElement(i),
                this.numerotation.getElement(j), val, oriente));
        arcsCompteur++;
    }

    @Override
    public int getEdgeValue(Fanal s, Fanal t) {
        for (Edge a : L.get(this.numerotation.getNumber(s))) {
            if (a.getDestinationFanal().equals(t)) {
                return a.getValue();
            }
        }
        return -1; // Par convention la valeur pas utilisé est -1
    }

    @Override
    public Edge getEdge(Fanal s, Fanal t) {
        for (Edge a : L.get(this.numerotation.getNumber(s))) {
            if (a.getDestinationFanal().equals(t)) {
                return a;
            }
        }
        return null;
    }

    @Override
    public Edge[] getEdgesList(Fanal s) {
        ArrayList<Edge> lst = L.get(this.numerotation.getNumber(s));

        return (Edge[]) lst.toArray(new Edge[lst.size()]);
    }

    public int valeurArc(int i, int j) {
        Fanal t = this.numerotation.getElement(j);
        for (Edge a : L.get(i)) {
            if (a.getDestinationFanal().equals(t)) {
                return a.getValue();
            }
        }
        return -1; // Par convention la valeur pas utilisé est -1
    }

    @Override
    public void removeEdge(Fanal s, Fanal t) {
        int si = this.numerotation.getNumber(s);
        Edge a = null;
        for (Edge atemp : L.get(this.numerotation.getNumber(s))) {
            if (atemp.getDestinationFanal().equals(t)) {
                a = atemp;
                break;
            }
        }
        if (a != null) {
            L.get(this.numerotation.getNumber(s)).remove(a);
        }
    }

    public void modifyValue(Fanal s, Fanal t, int val) {
        for (Edge a : L.get(this.numerotation.getNumber(s))) {
            if (a.getDestinationFanal().equals(t)) {
                a.setValue(val);
                return;
            }
        }

    }

    public ArrayList<Edge> neighboors(Fanal s) {
        return L.get(this.numerotation.getNumber(s));
    }

    @Override
    public Collection<Fanal> getAllNodes() {
        return this.numerotation.getElements();
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
        return this.arcsCompteur;
    }
}
