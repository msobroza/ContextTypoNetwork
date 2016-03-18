package control.network;

import control.ContextTypoNetwork;
import graph.Graph;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import model.TournamentChain;
import model.Clique;
import model.Fanal;

public abstract class Level {

    protected int h;
    protected Graph graphe;
    protected Graph sousGraphe;
    protected HashMap<String, Clique> mapCliquesStr;
    protected HashMap<String, TournamentChain> mapCTournois;

    public int getH() {
        return this.h;
    }

    public Graph getGraphe() {
        return this.graphe;
    }

    public Graph getSousGraphe() {
        return this.sousGraphe;
    }

    public boolean existsClique(String info) {
        return mapCliquesStr.containsKey(info);
    }
    
    public Clique getWordClique(String mot) {
        if (!this.existsClique(mot)) {
            return null;
        }
        return this.mapCliquesStr.get(mot);
    }
    
    @Override
    public String toString() {
        return this.graphe.toString();
    }
    

    public abstract Level copie(int h);

    protected boolean creerClique(List<Fanal> liste_f) {
        if (liste_f.isEmpty()) {
            return true;
        } else {
            boolean existeArc;
            for (int i = 1; i < liste_f.size(); i++) {
                existeArc = false;
                // Si la connexion existe déjà, on ne va pas la creer (non-orienté)
                if (this.graphe.existeArc(liste_f.get(0), liste_f.get(i))) {
                    if (!this.graphe.getArc(liste_f.get(0), liste_f.get(i)).isOriente()) {
                        existeArc = true;
                    }
                }
                if (!existeArc) {
                    //Il cree des arcs sans orientation
                    // Creer les arcs de la clique
                    this.graphe.ajouterArc(liste_f.get(0), liste_f.get(i), 0, false);
                    this.graphe.ajouterArc(liste_f.get(i), liste_f.get(0), 0, false);
                }
            }
            liste_f.remove(0);
            creerClique(liste_f);
            return false;
        }
    }

    protected boolean creerChaineTournois(List<Fanal> liste_fA, List<Fanal> liste_fB, Graph g) {

        boolean existeArc;
        for (int i = 0; i < liste_fA.size(); i++) {
            for (int j = 0; j < liste_fB.size(); j++) {
                existeArc = false;
                if (g.existeArc(liste_fA.get(i), liste_fB.get(j))) {
                    if (g.getArc(liste_fA.get(i), liste_fB.get(j)).isOriente()) {
                        existeArc = true;
                    }
                }
                if (!existeArc) {
                    // S'ils n'existent pas encore, on cree les arcs avec orientation pour la chaine de tournois
                    //TypoMultireseaux.logger.debug(": " + liste_fA.get(i) + " -> " + liste_fB.get(j));
                    g.ajouterArc(liste_fA.get(i), liste_fB.get(j), 0, true);
                }
            }
        }
        return true;
    }

    protected TournamentChain ajouterCTournois(Clique l_clique, Graph l_graphe, Clique r_clique, Graph r_graphe, String info, Graph g) {
        if (mapCTournois.containsKey(info)) {
            return mapCTournois.get(info);
        } else {
            LinkedList<Fanal> listeA = new LinkedList<>();
            LinkedList<Fanal> listeB = new LinkedList<>();
            
            // On cherche les fanaux correspondants aux 2 cliques inferieures dans le sous-graphe
            for (Fanal f : l_clique.getFanalsList()) {
                listeA.addLast(g.getNumerotation().elementAt(l_graphe.getNumerotation().numero(f)));
            }
            for (Fanal f : r_clique.getFanalsList()) {
                listeB.addLast(g.getNumerotation().elementAt(r_graphe.getNumerotation().numero(f)));
            }
            TournamentChain c = new TournamentChain(listeA, listeB);            
            if (creerChaineTournois(listeA, listeB, g)) {
                this.mapCTournois.put(info, c);
                return c;
            } else {
                return null;
            }

        }
    }
    
    public LinkedList<String> getKeyCliques() {
        Set<String> set_of_keys = this.mapCliquesStr.keySet();
        LinkedList<String> list_of_keys = new LinkedList<>();
        for (String k : set_of_keys) {
            list_of_keys.add(k);
        }
        return list_of_keys;
    }

    
}
