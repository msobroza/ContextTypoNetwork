package model;

import java.util.LinkedList;

public class Clique {

    private String info;
    private LinkedList<Fanal> listeFanaux;

    public Clique(String info) {
        this.listeFanaux = new LinkedList<>();
        this.info = info;
    }

    public LinkedList<Fanal> getFanalsList() {
        return listeFanaux;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public void ajouterFanal(Fanal f) {
        if (!existeFanal(f)) {
            listeFanaux.addLast(f);
        }
    }

    public boolean existeFanal(Fanal f) {
        for (Fanal ftemp : listeFanaux) {
            if (ftemp.equals(f)) {
                return true;
            }
        }
        return false;
    }

}
