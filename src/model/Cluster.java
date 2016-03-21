package model;

import java.util.HashMap;
import java.util.LinkedList;

public class Cluster {

    private String name;
    // Liste des fanaux du cluster
    private LinkedList<FanalFlous> fuzzyFanalsList;
    private LinkedList<MacroFanal> macroFanalsList;
    private HashMap<String, FanalFlous> mapIndexFuzzyFanal;
    private HashMap<String, MacroFanal> mapIndexMacroFanal;
    private boolean used;

    public Cluster(String nom) {
        this.name = nom;
        this.fuzzyFanalsList = new LinkedList<>();
        this.macroFanalsList = new LinkedList<>();
        // Génération du mapping entre lettre et position du fanal dans le cluster
        this.mapIndexFuzzyFanal = new HashMap<>();
        this.mapIndexMacroFanal = new HashMap<>();
        used = false;
    }

    public void addFanal(FanalFlous f) {
        this.fuzzyFanalsList.add(f);
        f.setCluster(this);
    }

    public void addMacroFanal(MacroFanal mf) {
        this.macroFanalsList.add(mf);
        mf.setCluster(this);
    }

    public boolean linkFanalLetter(FanalFlous f, String lettre) {
        if (!mapIndexFuzzyFanal.containsKey(lettre)) {
            mapIndexFuzzyFanal.put(lettre, f);
            return true;
        }
        return false;
    }

    public boolean linkMacroFanalLetter(MacroFanal f, String lettre) {
        if (!mapIndexMacroFanal.containsKey(lettre)) {
            mapIndexMacroFanal.put(lettre, f);
            return true;
        }
        return false;
    }

    public String getClusterName() {
        return name;
    }

    public void setClusterName(String nom) {
        this.name = nom;
    }

    public FanalFlous getFanal(int i) {
        return fuzzyFanalsList.get(i);
    }

    public FanalFlous getFanal(String lettre) {
        return mapIndexFuzzyFanal.get(lettre);
    }

    public MacroFanal getMacroFanal(String lettre) {
        return mapIndexMacroFanal.get(lettre);
    }

    public LinkedList<MacroFanal> getMacroFanalsList() {
        return macroFanalsList;
    }

    public void setMacroFanals(LinkedList<MacroFanal> macroFanaux) {
        this.macroFanalsList = macroFanaux;
    }

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "" + this.name;
    }

    //Renvoie le hashCode du nom
    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public LinkedList<FanalFlous> getFanalsList() {
        return fuzzyFanalsList;
    }
}
