package model;

import java.util.LinkedList;

public class Clique {

    private String info;
    private LinkedList<Fanal> fanalsList;

    public Clique(String info) {
        this.fanalsList = new LinkedList<>();
        this.info = info;
    }

    public LinkedList<Fanal> getFanalsList() {
        return fanalsList;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void addFanal(Fanal f) {
        if (!existsFanal(f)) {
            fanalsList.addLast(f);
        }
    }

    public boolean existsFanal(Fanal f) {
        for (Fanal ftemp : fanalsList) {
            if (ftemp.equals(f)) {
                return true;
            }
        }
        return false;
    }

}
