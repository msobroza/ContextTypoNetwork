package control.network;

import model.Fanal;
import model.Clique;
import graph.TriangularGraph;
import control.NetworkControl;
import control.ContextTypoNetwork;
import control.decoder.TriangularDecoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class TriangularLevel extends Level {

    private final TriangularNetwork r;

    public TriangularLevel(int h, TriangularNetwork r, int l, int X) {
        int n = l * X; //Nombre de sommets
        this.graph = new TriangularGraph(n);
        this.subGraph = new TriangularGraph(n);
        this.mapCliquesStr = new HashMap<>();
        this.mapCTournois = new HashMap<>();
        this.h = h;
        this.r = r;

    }

    public TriangularNetwork getTriangNetwork() {
        return r;
    }

    private TriangularLevel(TriangularLevel n, int h) { //Pour reprendre d'un autre niveau déjà existent
        this.h = h;
        this.graph = n.graph.copyGraph(h);
        this.subGraph = n.graph.copyGraph(h);
        this.mapCliquesStr = new HashMap<>();
        this.mapCTournois = new HashMap<>();
        this.r = n.r;
    }

// Change le nombre d'attributs
    public Clique addClique(String info, int infoLength, String seqLeft, int seqLeftLength, String seqRight, int seqRightLength, int numFanaux) {
        List<Fanal> list_fanaux = new ArrayList<>();
        Clique l_clique, r_clique;
        // On choisit par hasard numFanaux clusters
        List<Integer> list_clust = new LinkedList<>();
        List<Integer> list_clust_right = new LinkedList<>();
        List<Integer> list_clust_left = new LinkedList<>();

        for (int i = 0; i <= this.graph.getNumerotation().getClustersCounter(); i++) {
            // On prend les clusters non complets si la non-superposition en h0 est activee
            if (this.h != 0 || !r.WITHOUT_SUPERPOSITON_H0 || !(this.graph.getNumerotation().isClusterUsed(i))) {
                // Meiose etape 1 : separation des clusters du niveau courant en 2 sous-parties egales
                if (r.MEIOSIS && this.h != 0) {
                    if (this.getSideCluster(i) == TriangularDecoder.LEFT) {
                        list_clust_left.add(i);
                    } else {
                        list_clust_right.add(i);
                    }

                } else {
                    list_clust.add(i);
                }
            }
        }
        // Meiose etape 2+3+4: on choisit numFanaux/2 fanaux des clusters de gauche et numFanaux/2 fanaux des cluster de droite pour composer une nouvelle clique
        if (r.MEIOSIS && this.h != 0) {
            if (r.WINNER_PARTITIONS) {
                List<Integer> copy_list = list_clust_left;
                list_clust_left = new LinkedList<>();
                // Pour chaque partition il prendre le nombre necessaire de clusters
                for (int i = 1; i <= r.WINNER_N_PARTITIONS_PAR_SIDE; i++) {
                    list_clust_left.addAll(pickRandomNumbers(getPartition(copy_list, i), numFanaux / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE)));
                }
                copy_list = list_clust_right;
                list_clust_right = new LinkedList<>();
                for (int i = 1; i <= r.WINNER_N_PARTITIONS_PAR_SIDE; i++) {
                    list_clust_right.addAll(pickRandomNumbers(getPartition(copy_list, i), numFanaux / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE)));
                }

            } else {
                list_clust_left = pickRandomNumbers(list_clust_left, numFanaux / 2);
                list_clust_right = pickRandomNumbers(list_clust_right, numFanaux / 2);
            }
            // Ajout des fanaux gauches et fanaux droits dans la meme liste, g�n�rant plus qu'une seule clique.
            for (int i = 0; i < numFanaux / 2; i++) {
                list_fanaux.add(pickRandomFanal(this.graph.getNumerotation().getListeClusters().get(list_clust_left.get(i)), this.h, r.WITHOUT_SUPERPOSITON_H0));
            }
            for (int i = 0; i < numFanaux / 2; i++) {
                list_fanaux.add(pickRandomFanal(this.graph.getNumerotation().getListeClusters().get(list_clust_right.get(i)), this.h, r.WITHOUT_SUPERPOSITON_H0));
            }

        } else {
            list_clust = pickRandomNumbers(list_clust, numFanaux);
            for (int i = 0; i < numFanaux; i++) {
                list_fanaux.add(pickRandomFanal(this.graph.getNumerotation().getListeClusters().get(list_clust.get(i)), this.h, r.WITHOUT_SUPERPOSITON_H0 && !r.INTERFACE));
            }
        }
        if (infoLength > 1 && this.r.getLevelsList().get(seqLeftLength - 1).existsClique(seqLeft) && this.r.getLevelsList().get(seqRightLength - 1).existsClique(seqRight)) {
            List<Fanal> liste_left, liste_right;
            l_clique = this.r.getLevelsList().get(seqLeftLength - 1).mapCliquesStr.get(seqLeft);
            r_clique = this.r.getLevelsList().get(seqRightLength - 1).mapCliquesStr.get(seqRight);

            // Si la sous échantillonnage entre le premiere et le deuxieme niveau est habilitée
            // On va prendre taux_echant*nombre_de_fanaux pour lier au niveau superieur
            if (info.length() == 2 && r.SUBSAMPLING_H0) {
                liste_left = pickRandomFanals(l_clique.getFanalsList(), (int) Math.round(r.SUBSIMPLING_RATE_H0 * r.FANALS_PER_CLIQUE_H0));
                liste_right = pickRandomFanals(r_clique.getFanalsList(), (int) Math.round(r.SUBSIMPLING_RATE_H0 * r.FANALS_PER_CLIQUE_H0));
            } else {
                liste_left = l_clique.getFanalsList();
                liste_right = r_clique.getFanalsList();
            }

            // Si ce n'est pas habilitée la méiose il va créer la liaison avec la liste de fanaux
            if (!r.MEIOSIS) {
                for (Fanal f : list_fanaux) {
                    // Il creer la liaison inter-niveau
                    for (Fanal f_l : liste_left) {
                        createLinkBetweenLevels(f_l, f);
                    }
                    for (Fanal f_r : liste_right) {
                        createLinkBetweenLevels(f_r, f);
                    }
                }
            } else { // Il execute si la méiose est habilitée
                // Il prendre les numFanaux/2 premieres éléments pour lier avec la liste à gauche
                for (Fanal f_sup_left : list_fanaux.subList(0, numFanaux / 2)) {
                    for (Fanal f_l : liste_left) {
                        createLinkBetweenLevels(f_l, f_sup_left);
                    }
                    if (r.MEIOSIS_LINK_UNIDIREC) {
                        for (Fanal f_r : liste_right) {
                            creerLiaisonInterNiveauxUnidirectionnel(f_r, f_sup_left);
                        }
                    }

                }

                // Il prendre les numFanaux/2 dernières éléments pour lier avec la liste à droite
                for (Fanal f_sup_right : list_fanaux.subList(numFanaux / 2, numFanaux)) {
                    for (Fanal f_r : liste_right) {
                        createLinkBetweenLevels(f_r, f_sup_right);
                    }
                    if (r.MEIOSIS_LINK_UNIDIREC) {
                        for (Fanal f_l : liste_left) {
                            creerLiaisonInterNiveauxUnidirectionnel(f_l, f_sup_right);
                        }
                    }
                }
            }
            this.r.getLevelsList().get(seqLeftLength - 1).addCTournois(l_clique, this.r.getLevelsList().get(seqLeftLength - 1).subGraph, r_clique, this.r.getLevelsList().get(seqLeftLength - 1).subGraph, info, this.r.getLevelsList().get(seqLeftLength - 1).subGraph);
        }
        Clique c = new Clique(info);
        for (Fanal f : list_fanaux) {
            ContextTypoNetwork.logger.debug(f.getFanalName());
            // On utilise pour savoir quels sont les fanaux utilisés
            this.graph.getNumerotation().useFanal(f);
            c.addFanal(f);
        }
        createClique(list_fanaux);
        this.mapCliquesStr.put(info, c);

        return c;
    }

    @Override
    public Level copy(int h) {
        return new TriangularLevel(this, h);
    }

    public int getSideCluster(int numCluster) {
        if (numCluster < (int) r.NOMBRE_CLUSTERS / 2) {
            return TriangularDecoder.LEFT;
        } else {
            return TriangularDecoder.RIGHT;
        }
    }

    // fSup est dans la couche superieure
    public void createLinkBetweenLevels(Fanal fInf, Fanal fSup) {
        if (!fInf.getSuperiorFanals().contains(fSup)) {
            fInf.setSuperiorFanals(fSup);
        }
        if (!fSup.getInferiorFanals().contains(fInf)) {
            fSup.setInferiorFanals(fInf);
        }
    }

    // On ne cree que la liaison montante
    public void creerLiaisonInterNiveauxUnidirectionnel(Fanal fInf, Fanal fSup) {
        if (!fInf.getSuperiorFanals().contains(fSup)) {
            fInf.setSuperiorFanals(fSup);
        }
    }

    public void createLinkBetweenHyperLevels(Fanal fHyperInf, Fanal fHyperSup) {
        if (fHyperInf.getSuperiorHyperFanals().contains(fHyperSup) || fHyperSup.getInferiorFanals().contains(fHyperInf)) {
            return;
        }
        fHyperInf.setHyperSuperiorFanals(fHyperSup);
        fHyperSup.setHyperInferiorFanals(fHyperInf);
    }

    public void ajouterHyperLiaison(List<String> unite) {
        List<Fanal> liste_inf;
        String seqMot = "";
        for (String motAux : unite) {
            seqMot += motAux;
        }
        Clique c_hyperSup = this.mapCliquesStr.get(seqMot);
        LinkedList<Clique> c_hyperInf = new LinkedList<>();
        // Creer la liaison au niveau de lettres
        for (int i = 0; i < unite.size(); i++) {
            c_hyperInf.addLast(this.r.getLevelsList().get(0).mapCliquesStr.get(unite.get(i)));
        }
        int diff = r.HYPER_LINK_LEVELS;
        if (unite.size() <= r.HYPER_LINK_LEVELS) {
            diff = unite.size() - 1;
        }
        // Creer la liaison au niveau de sous-mots
        for (int delta = 1; delta < diff; delta++) {
            for (int i = 0; i < unite.size() - delta; i++) {
                String s = "";
                List<String> sSeq = new ArrayList<>();
                for (int j = i; j < i + (delta + 1); j++) {
                    s += unite.get(j);
                    sSeq.add(unite.get(j));
                }
                Clique c_inf = this.r.getLevelsList().get(delta).mapCliquesStr.get(s);
                c_hyperInf.addLast(c_inf);
                if (r.DISTANCE_HYPER_LINK && delta > 1) {
                    // Recouvrement
                    for (int j = 0; j < sSeq.size(); j++) {
                        String lt = sSeq.get(j);
                        for (Fanal f_rec_inf : this.r.getLevelsList().get(0).mapCliquesStr.get(lt).getFanalsList()) {
                            for (Fanal f_rec_sup : c_inf.getFanalsList()) {
                                createLinkBetweenHyperLevels(f_rec_inf, f_rec_sup);
                            }
                        }
                    }
                }
            }
        }
        // Si la sous échantillonnage entre le premiere et le deuxieme niveau est activee
        // On va prendre taux_echant*nombre_de_fanaux pour lier au niveau superieur   
        for (Clique c : c_hyperInf) {
            liste_inf = c.getFanalsList();
            for (Fanal f_inf : liste_inf) {
                for (Fanal f_sup : c_hyperSup.getFanalsList()) {
                    createLinkBetweenHyperLevels(f_inf, f_sup);
                }
            }
        }
    }

    public static Fanal pickRandomFanal(LinkedList<Fanal> lst, int h, boolean SANS_SUPERPOSITION_H0) {
        Random r = new Random();
        int num = r.nextInt(lst.size());
        if (SANS_SUPERPOSITION_H0 && h == 0) {
            do {
                num = r.nextInt(lst.size());
            } while (lst.get(num).isUsed());
        } else {
            num = r.nextInt(lst.size());
        }
        return lst.get(num);
    }

    public static List<Fanal> pickRandomFanals(LinkedList<Fanal> lst, int n) {
        List<Fanal> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    // Il accepte valeurs de partitions entre 1 et WINNER_N_PARTITIONS_PAR_SIDE
    public List<Integer> getPartition(List<Integer> lst_side_clusters, int partition) {
        List<Integer> lst_result;
        if ((partition < 1) || (partition > r.WINNER_N_PARTITIONS_PAR_SIDE)) {
            return null;
        }
        int nombreElements = (int) Math.ceil((double) lst_side_clusters.size() / r.WINNER_N_PARTITIONS_PAR_SIDE);
        if (partition != r.WINNER_N_PARTITIONS_PAR_SIDE) {
            lst_result = lst_side_clusters.subList((partition - 1) * nombreElements, partition * nombreElements);
        } else {
            lst_result = lst_side_clusters.subList((partition - 1) * nombreElements, lst_side_clusters.size());
        }
        return lst_result;

    }

    public int getPartitionCluster(int numCluster) {
        int partition;
        int nombreElements = (int) Math.ceil((double) r.NOMBRE_CLUSTERS / (2 * r.WINNER_N_PARTITIONS_PAR_SIDE));
        partition = (int) (numCluster / nombreElements) + 1;
        if (partition > r.WINNER_N_PARTITIONS_PAR_SIDE) {
            partition = partition - r.WINNER_N_PARTITIONS_PAR_SIDE;
        }
        return partition;

    }

    public static List<Integer> pickRandomNumbers(List<Integer> lst, int n) {
        List<Integer> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    public Clique getCliqueLetter(String lettre) {
        if (h != 0 || !this.existsClique(lettre)) {
            return null;
        }
        return this.mapCliquesStr.get(lettre);
    }

    public Clique getRandomCliqueLetter() {
        Random r = new Random();
        int numAleat = r.nextInt(this.mapCliquesStr.size());
        int c = 0;
        String keyAleat = "";
        for (String lettre : this.mapCliquesStr.keySet()) {
            if (c == numAleat) {
                keyAleat = lettre;
                break;
            }
            c++;
        }
        return getCliqueLetter(keyAleat);
    }

    public LinkedList<String> searchClique(LinkedList<Fanal> lst) {
        int maxScore = 0;
        HashMap<String, Integer> score = new HashMap<>();
        for (String s : this.getKeyCliques()) {
            Clique c = this.getWordClique(s);
            score.put(s, 0);
            for (Fanal f : lst) {
                if (c.existsFanal(f)) {
                    score.put(s, score.get(s) + 1);

                }
            }
            if (score.get(s) > maxScore) {
                maxScore = score.get(s);
            }
        }

        LinkedList<String> result = new LinkedList<>();
        for (String s : this.getKeyCliques()) {
            if (score.get(s) >= maxScore) {
                result.add(s);
            }
        }

        return result;
    }

    public double getHorizontaleDensity() {
        return ((double) this.getGraph().getEdgesCounter()) / (r.NOMBRE_CLUSTERS * (r.NOMBRE_CLUSTERS - 1) * r.FANALS_PER_CLUSTER * r.FANALS_PER_CLUSTER);
    }

    public double getVerticalDensity() {
        int compteurArcsVertical = 0;
        for (Fanal f : this.getGraph().getNumerotation().getElements()) {
            compteurArcsVertical += f.getSuperiorFanals().size();
        }
        return ((double) compteurArcsVertical) / (r.NOMBRE_CLUSTERS * (r.NOMBRE_CLUSTERS - 1) * r.FANALS_PER_CLUSTER * r.FANALS_PER_CLUSTER);
    }

    public double getInferiorLevelDensity() {
        int compteurArcsInfHyper = 0;
        for (Fanal f : this.getGraph().getNumerotation().getElements()) {
            compteurArcsInfHyper += f.getInferiorHyperFanals().size();
        }
        return ((double) compteurArcsInfHyper) / (r.NOMBRE_CLUSTERS * (r.NOMBRE_CLUSTERS - 1) * r.FANALS_PER_CLUSTER * r.FANALS_PER_CLUSTER);
    }

}
