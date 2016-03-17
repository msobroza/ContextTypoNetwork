package model;

import java.util.LinkedList;

public class MacroFanal extends FanalFlous {

    private LinkedList<FanalFlous> listFanaux;

    public MacroFanal(String nom, int score) {
        super(nom, score);
        listFanaux = new LinkedList<>();
    }

    public MacroFanal(FanalFlous f, int score) {
        super(f, score);
        listFanaux = new LinkedList<>();
    }

    public boolean ajouterFanal(FanalFlous f) {
        if (!listFanaux.contains(f)) {
            listFanaux.add(f);
            f.setMacroFanal(this);
            return true;
        } else {
            return false;
        }
    }

    public LinkedList<FanalFlous> getListFanaux() {
        return listFanaux;
    }

    public void setListFanaux(LinkedList<FanalFlous> listFanaux) {
        this.listFanaux = listFanaux;
    }
}
