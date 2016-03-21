package model;

import java.util.LinkedList;

public class Fanal {

    private String name;
    private int score;
    // Liaison inter les niveaux
    private LinkedList<Fanal> superiorFanals;
    private LinkedList<Fanal> inferiorFanals;
    private LinkedList<Fanal> hyperSuperiorFanals;
    private LinkedList<Fanal> hyperInferiorFanals;
    private boolean used;
    private int inDegree;
    private int outDegree;

    public Fanal(String nom, int score) {
        this.name = nom;
        this.score = score;
        superiorFanals = new LinkedList<>();
        inferiorFanals = new LinkedList<>();
        hyperSuperiorFanals = new LinkedList<>();
        hyperInferiorFanals = new LinkedList<>();
        used = false;
        this.inDegree = 0;
        this.outDegree = 0;
    }

    public Fanal(Fanal f, int score) {

        this.name = f.getFanalName();
        this.score = f.getScore();
        superiorFanals = new LinkedList<>();
        inferiorFanals = new LinkedList<>();
        hyperSuperiorFanals = new LinkedList<>();
        hyperInferiorFanals = new LinkedList<>();
        used = false;
        this.inDegree = 0;
        this.outDegree = 0;
    }

    public void setSuperiorFanals(Fanal fanalSup) {
        this.superiorFanals.add(fanalSup);
    }

    public void setInferiorFanals(Fanal fanalInf) {
        this.inferiorFanals.add(fanalInf);
    }

    public void setHyperSuperiorFanals(Fanal fanalHyperSup) {
        this.hyperSuperiorFanals.add(fanalHyperSup);
    }

    public void setHyperInferiorFanals(Fanal fanalHyperInf) {
        this.hyperInferiorFanals.add(fanalHyperInf);
    }

    public LinkedList<Fanal> getSuperiorFanals() {
        return this.superiorFanals;
    }

    public LinkedList<Fanal> getInferiorFanals() {
        return this.inferiorFanals;
    }

    public LinkedList<Fanal> getSuperiorHyperFanals() {
        return this.hyperSuperiorFanals;
    }

    public LinkedList<Fanal> getInferiorHyperFanals() {
        return this.hyperInferiorFanals;
    }

    public void setFanalName(String nom) {
        this.name = nom;
    }

    public String getFanalName() {
        return this.name;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "" + this.name;
    }

    //Verifie se les sommets ont le même nom

    @Override
    public boolean equals(Object o) {
        return name.equals(((Fanal) o).getFanalName());
    }

    //Verifie lequel des valeurs est plus grandes

    public int compareTo(Object o) {
        Fanal s = (Fanal) o;
        return this.name.compareTo(s.getFanalName());
    }

    //Renvoie le hashCode du nom

    @Override
    public int hashCode() {
        return name.hashCode();
    }

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
    
     public int getInDegree() {
        return this.inDegree;
    }

    public void increaseInDegree() {
        this.inDegree++;
    }

    public int getOutDegree() {
        return this.outDegree;
    }

    public void increaseOutDegree() {
        this.outDegree++;
    }

}
