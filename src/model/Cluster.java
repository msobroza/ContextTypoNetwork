package model;

import java.util.HashMap;
import java.util.LinkedList;

public class Cluster {

    private String nom;
    // Liste des fanaux du cluster
    private LinkedList<FanalFlous> fanaux;
    private LinkedList<MacroFanal> macroFanaux;
    private HashMap<String, FanalFlous> hlf;
    private HashMap<String, MacroFanal> hlmf;
    private boolean used;

    public Cluster(String nom) {
        this.nom = nom;
        this.fanaux = new LinkedList<>();
        this.macroFanaux = new LinkedList<>();
        // Génération du mapping entre lettre et position du fanal dans le cluster
        this.hlf = new HashMap<>();
        this.hlmf = new HashMap<>();
        used = false;
    }

    public void ajouterFanal(FanalFlous f) {
        this.fanaux.add(f);
        f.setCluster(this);
    }

    public void ajouterMacroFanal(MacroFanal mf) {
        this.macroFanaux.add(mf);
        mf.setCluster(this);
    }

    public boolean associerFanalLettre(FanalFlous f, String lettre) {
        if (!hlf.containsKey(lettre)) {
            hlf.put(lettre, f);
            return true;
        }
        return false;
    }

    public boolean associerMacroFanalLettre(MacroFanal f, String lettre) {
        if (!hlmf.containsKey(lettre)) {
            hlmf.put(lettre, f);
            return true;
        }
        return false;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public FanalFlous getFanal(int i) {
        return fanaux.get(i);
    }

    public FanalFlous getFanal(String lettre) {
        return hlf.get(lettre);
    }

    public MacroFanal getMacroFanal(String lettre) {
        return hlmf.get(lettre);
    }

    public LinkedList<MacroFanal> getMacroFanaux() {
        return macroFanaux;
    }

    public void setMacroFanaux(LinkedList<MacroFanal> macroFanaux) {
        this.macroFanaux = macroFanaux;
    }

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    @Override
    public String toString() {
        return "" + this.nom;
    }

    //Renvoie le hashCode du nom
    @Override
    public int hashCode() {
        return nom.hashCode();
    }

    public LinkedList<FanalFlous> getListeFanaux() {
        return fanaux;
    }
}
