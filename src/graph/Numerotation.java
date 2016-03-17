package graph;

import model.Fanal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Numerotation {

    private int compteur;
    private int compteurCluster;
    //Numerotation fanal-int
    private HashMap<Fanal, Integer> HSI;
    // Liste de tout les fanaux du graphe
    private ArrayList<Fanal> VS;
    // Liste de Fanaux pour chaque cluster
    private LinkedList<LinkedList<Fanal>> listeClusters;
    // Association entre le fanal et le cluster
    private HashMap<Fanal, Integer> mapClusters;
    // Capacite de fanaux non-utilisés de clusters
    private ArrayList<Integer> capaciteCluster;

    public Numerotation(int n) {
        this.compteur = -1;
        // Numerotation des sommets
        this.HSI = new HashMap<>();
        // Liste avec les fanaux ajoutes
        this.VS = new ArrayList<>();
        // Chaque position c'est la numerotation d'un cluster
        // et la liste de sommets qui l'appartient
        this.listeClusters = new LinkedList<>();
        // Map avec la numerotation du cluster
        this.mapClusters = new HashMap<>();
        this.compteurCluster = -1;
        this.capaciteCluster = new ArrayList<>();

    }

    public int taille() {
        return VS.size();
    }

    public boolean ajouterElement(Fanal f) {
        if (!HSI.containsKey(f)) {
            compteur++;
            HSI.put(f, compteur);
            VS.add(compteur, f);
            return true;
        }
        return false;
    }

    // Renvoie le numero du sommet

    public int numero(Fanal f) {
        return HSI.get(f);
    }

    public Fanal elementAt(int i) {
        return VS.get(i);
    }

    public Collection<Fanal> getElements() {
        return VS;
    }

    public int ajouterCluster() {
        compteurCluster++;
        // Redemarre une liste de fanaux
        listeClusters.add(compteurCluster, new LinkedList<Fanal>());
        // Redemarre le compteur de space libre
        capaciteCluster.add(compteurCluster, 0);
        return compteurCluster;
    }

    // Ajoute le sommet dans le cluster

    public boolean ajouterSommetCluster(int iCluster, Fanal f) {
        if (iCluster <= compteurCluster && !existeFanalCluster(iCluster, f)) {
            listeClusters.get(iCluster).addLast(f);
            mapClusters.put(f, iCluster);
            // Augmente la capacité du cluster
            capaciteCluster.set(iCluster, capaciteCluster.get(iCluster) + 1);
            return true;
        } else {
            return false;
        }
    }

    // Verifie si le sommet déja existe dans le cluster

    public boolean existeFanalCluster(int iCluster, Fanal s) {
        if (iCluster <= compteurCluster) {
            for (Fanal stemp : listeClusters.get(iCluster)) {
                if (stemp.equals(s)) {
                    return true;
                }
            }
            return false;
        } else {
            return false;
        }
    }

    public void utiliserFanal(Fanal f) {
        // Fixe comme utilisé
        f.setUsed(true);
        // Diminue la capacité du cluster
        capaciteCluster.set(this.numCluster(f), capaciteCluster.get(numCluster(f)));
    }

    // Retourne le numero du cluster qui a le sommet

    public int numCluster(Fanal f) {
        return mapClusters.get(f);
    }

    public int getCompteurClusters() {
        return this.compteurCluster;
    }

    public LinkedList<LinkedList<Fanal>> getListeClusters() {
        return this.listeClusters;
    }

    public HashMap<Fanal, Integer> getMapClusters() {
        return this.mapClusters;
    }

    public boolean isClusterUsed(int iCluster) {
        if (capaciteCluster.get(iCluster) > 0) {
            return false;
        } else {
            return true;
        }
    }

}
