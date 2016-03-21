package model;

import java.util.LinkedList;

public class MacroFanal extends FuzzyFanal {

    private LinkedList<FuzzyFanal> listFanaux;

    public MacroFanal(String nom, int score) {
        super(nom, score);
        listFanaux = new LinkedList<>();
    }

    public MacroFanal(FuzzyFanal f, int score) {
        super(f, score);
        listFanaux = new LinkedList<>();
    }

    public boolean addFanal(FuzzyFanal f) {
        if (!listFanaux.contains(f)) {
            listFanaux.add(f);
            f.setMacroFanal(this);
            return true;
        } else {
            return false;
        }
    }

    public LinkedList<FuzzyFanal> getListFanaux() {
        return listFanaux;
    }

    public void setListFanaux(LinkedList<FuzzyFanal> listFanaux) {
        this.listFanaux = listFanaux;
    }
}
