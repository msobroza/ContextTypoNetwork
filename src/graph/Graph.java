package graph;

import java.util.Collection;
import model.Fanal;

public abstract class Graph {

    public abstract int size();

    public abstract Graph copyGraph(int h);

    public abstract void addNode(Fanal s);

    public abstract boolean existsEdge(Fanal s, Fanal t);

    public abstract void addEdge(Fanal s, Fanal t, int val, boolean oriented);

    public abstract int getEdgeValue(Fanal s, Fanal t);

    public abstract void removeEdge(Fanal s, Fanal t);

    public abstract Edge getEdge(Fanal s, Fanal t);

    public abstract Edge[] getEdgesList(Fanal s);

    public abstract Collection<Fanal> getAllNodes();

    @Override
    public abstract String toString();

    public abstract Numerotation getNumerotation();

    public abstract int getEdgesCounter();

}
