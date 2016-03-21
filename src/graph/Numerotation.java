package graph;

import model.Fanal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;

public class Numerotation {

    private int counterFanals;
    private int counterClusters;
    //Numerotation fanal-int
    private HashMap<Fanal, Integer> mapFanalIndex;
    // Liste de tout les fanaux du graphe
    private ArrayList<Fanal> graphFanals;
    // Liste de Fanaux pour chaque cluster
    private LinkedList<LinkedList<Fanal>> clustersList;
    // Association entre le fanal et le cluster
    private HashMap<Fanal, Integer> mapFanalCluster;
    // Capacite de fanaux non-utilisés de clusters
    private ArrayList<Integer> capacityCluster;

    public Numerotation(int n) {
        this.counterFanals = -1;
        // Numerotation des sommets
        this.mapFanalIndex = new HashMap<>();
        // Liste avec les fanaux ajoutes
        this.graphFanals = new ArrayList<>();
        // Chaque position c'est la numerotation d'un cluster
        // et la liste de sommets qui l'appartient
        this.clustersList = new LinkedList<>();
        // Map avec la numerotation du cluster
        this.mapFanalCluster = new HashMap<>();
        this.counterClusters = -1;
        this.capacityCluster = new ArrayList<>();

    }

    public int taille() {
        return graphFanals.size();
    }

    public boolean ajouterElement(Fanal f) {
        if (!mapFanalIndex.containsKey(f)) {
            counterFanals++;
            mapFanalIndex.put(f, counterFanals);
            graphFanals.add(counterFanals, f);
            return true;
        }
        return false;
    }

    // Renvoie le numero du sommet

    public int numero(Fanal f) {
        return mapFanalIndex.get(f);
    }

    public Fanal elementAt(int i) {
        return graphFanals.get(i);
    }

    public Collection<Fanal> getElements() {
        return graphFanals;
    }

    public int ajouterCluster() {
        counterClusters++;
        // Redemarre une liste de fanaux
        clustersList.add(counterClusters, new LinkedList<Fanal>());
        // Redemarre le compteur de space libre
        capacityCluster.add(counterClusters, 0);
        return counterClusters;
    }

    // Ajoute le sommet dans le cluster

    public boolean ajouterSommetCluster(int iCluster, Fanal f) {
        if (iCluster <= counterClusters && !existeFanalCluster(iCluster, f)) {
            clustersList.get(iCluster).addLast(f);
            mapFanalCluster.put(f, iCluster);
            // Augmente la capacité du cluster
            capacityCluster.set(iCluster, capacityCluster.get(iCluster) + 1);
            return true;
        } else {
            return false;
        }
    }

    // Verifie si le sommet déja existe dans le cluster

    public boolean existeFanalCluster(int iCluster, Fanal s) {
        if (iCluster <= counterClusters) {
            for (Fanal stemp : clustersList.get(iCluster)) {
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
        capacityCluster.set(this.numCluster(f), capacityCluster.get(numCluster(f)));
    }

    // Retourne le numero du cluster qui a le sommet

    public int numCluster(Fanal f) {
        return mapFanalCluster.get(f);
    }

    public int getCompteurClusters() {
        return this.counterClusters;
    }

    public LinkedList<LinkedList<Fanal>> getListeClusters() {
        return this.clustersList;
    }

    public HashMap<Fanal, Integer> getMapClusters() {
        return this.mapFanalCluster;
    }

    public boolean isClusterUsed(int iCluster) {
        if (capacityCluster.get(iCluster) > 0) {
            return false;
        } else {
            return true;
        }
    }

}
