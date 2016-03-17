package graph;

import java.util.Collection;
import model.Fanal;

public abstract class Graph {

    public abstract int taille();

    public abstract Graph copie(int h);

    public abstract void ajouterSommet(Fanal s);

    public abstract boolean existeArc(Fanal s, Fanal t);

    public abstract void ajouterArc(Fanal s, Fanal t, int val, boolean oriente);

    public abstract int valeurArc(Fanal s, Fanal t);

    public abstract void enleverArc(Fanal s, Fanal t);

    public abstract Edge getArc(Fanal s, Fanal t);

    public abstract Edge[] getListeArc(Fanal s);

    public abstract Collection<Fanal> sommets();

    @Override
    public abstract String toString();

    public abstract Numerotation getNumerotation();

    public abstract int getCompteurArcs();

}
