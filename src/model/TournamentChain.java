package model;

import java.util.LinkedList;

public class TournamentChain {

    //Liaison inter-couches

    private LinkedList<Fanal> listeFanauxA;
    private LinkedList<Fanal> listeFanauxB;

    public TournamentChain() {
        this.listeFanauxA = new LinkedList<>();
        this.listeFanauxB = new LinkedList<>();
    }

    public TournamentChain(LinkedList<Fanal> listeFanauxA, LinkedList<Fanal> listeFanauxB) {
        this.listeFanauxA = listeFanauxA;
        this.listeFanauxB = listeFanauxB;
    }

    public void ajouterFanalA(Fanal fA) {
        if (!existeFanalA(fA)) {
            this.listeFanauxA.addLast(fA);
        }
    }

    public void ajouterFanalB(Fanal fB) {
        if (!existeFanalB(fB)) {
            this.listeFanauxB.addLast(fB);
        }
    }

    public boolean existeFanalA(Fanal fA) {
        for (Fanal f : listeFanauxA) {
            if (f.equals(fA)) {
                return true;
            }
        }
        return false;
    }

    public boolean existeFanalB(Fanal fB) {
        for (Fanal f : listeFanauxB) {
            if (f.equals(fB)) {
                return true;
            }
        }
        return false;
    }

    public LinkedList<Fanal> getListeA() {
        return listeFanauxA;
    }

    public LinkedList<Fanal> getListeB() {
        return listeFanauxA;
    }
}
