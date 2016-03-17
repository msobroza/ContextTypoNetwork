package control.decoder;

import control.network.FuzzyNetwork;
import java.util.HashMap;
import java.util.LinkedList;
import model.Cluster;
import model.Fanal;

public abstract class Decoder {

    // Parametres du decodeur
    protected static final int GAMA = 1000;
    public static final int GAMA_1 = 1;
    protected static final int it_GWsTA = 10;
    public static final int LEFT = 0;
    public static final int RIGHT = 1;
    


    public static LinkedList<Fanal> unionEnsemble(LinkedList<Fanal> l_a, LinkedList<Fanal> l_b) {
        LinkedList<Fanal> l_union = new LinkedList<>();
        for (Fanal f : l_a) {
            l_union.add(f);
        }
        for (Fanal f : l_b) {
            if (!l_a.contains(f)) {
                l_union.add(f);
            }
        }
        return l_union;
    }

    public static LinkedList<Fanal> jonctionEnsemble(LinkedList<Fanal> l_a, LinkedList<Fanal> l_b) {
        LinkedList<Fanal> lst = new LinkedList<>();
        for (Fanal f : l_a) {
            lst.add(f);
        }
        for (Fanal f : l_b) {
            lst.add(f);
        }
        return lst;
    }
}
