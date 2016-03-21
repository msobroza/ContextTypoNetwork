package model;

import java.util.LinkedList;

public class TournamentChain {

    //Liaison inter-couches

    private final LinkedList<Fanal> fanalsListA;
    private final LinkedList<Fanal> fanalsListB;

    public TournamentChain() {
        this.fanalsListA = new LinkedList<>();
        this.fanalsListB = new LinkedList<>();
    }

    public TournamentChain(LinkedList<Fanal> listeFanauxA, LinkedList<Fanal> listeFanauxB) {
        this.fanalsListA = listeFanauxA;
        this.fanalsListB = listeFanauxB;
    }

    public void addFanalA(Fanal fA) {
        if (!existsFanalA(fA)) {
            this.fanalsListA.addLast(fA);
        }
    }

    public void addFanalB(Fanal fB) {
        if (!existsFanalB(fB)) {
            this.fanalsListB.addLast(fB);
        }
    }

    public boolean existsFanalA(Fanal fA) {
        for (Fanal f : fanalsListA) {
            if (f.equals(fA)) {
                return true;
            }
        }
        return false;
    }

    public boolean existsFanalB(Fanal fB) {
        for (Fanal f : fanalsListB) {
            if (f.equals(fB)) {
                return true;
            }
        }
        return false;
    }

    public LinkedList<Fanal> getListA() {
        return fanalsListA;
    }

    public LinkedList<Fanal> getListB() {
        return fanalsListA;
    }
}
