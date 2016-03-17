package model;

import java.util.LinkedList;

public class Fanal {

    private String nom;
    private int score;
    // Liaison inter les niveaux
    private LinkedList<Fanal> fanauxSup;
    private LinkedList<Fanal> fanauxInf;
    private LinkedList<Fanal> fanauxHyperSup;
    private LinkedList<Fanal> fanauxHyperInf;
    private boolean used;
    private int degEntrant;
    private int degSortant;

    public Fanal(String nom, int score) {
        this.nom = nom;
        this.score = score;
        fanauxSup = new LinkedList<>();
        fanauxInf = new LinkedList<>();
        fanauxHyperSup = new LinkedList<>();
        fanauxHyperInf = new LinkedList<>();
        used = false;
        this.degEntrant = 0;
        this.degSortant = 0;
    }

    public Fanal(Fanal f, int score) {

        this.nom = f.getNom();
        this.score = f.getScore();
        fanauxSup = new LinkedList<>();
        fanauxInf = new LinkedList<>();
        fanauxHyperSup = new LinkedList<>();
        fanauxHyperInf = new LinkedList<>();
        used = false;
        this.degEntrant = 0;
        this.degSortant = 0;
    }

    public void setFanalSup(Fanal fanalSup) {
        this.fanauxSup.add(fanalSup);
    }

    public void setFanalInf(Fanal fanalInf) {
        this.fanauxInf.add(fanalInf);
    }

    public void setFanalHyperSup(Fanal fanalHyperSup) {
        this.fanauxHyperSup.add(fanalHyperSup);
    }

    public void setFanalHyperInf(Fanal fanalHyperInf) {
        this.fanauxHyperInf.add(fanalHyperInf);
    }

    public LinkedList<Fanal> getFanauxSup() {
        return this.fanauxSup;
    }

    public LinkedList<Fanal> getFanauxInf() {
        return this.fanauxInf;
    }

    public LinkedList<Fanal> getFanauxHyperSup() {
        return this.fanauxHyperSup;
    }

    public LinkedList<Fanal> getFanauxHyperInf() {
        return this.fanauxHyperInf;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getNom() {
        return this.nom;
    }

    public int getScore() {
        return this.score;
    }

    public void setScore(int score) {
        this.score = score;
    }

    @Override
    public String toString() {
        return "" + this.nom;
    }

    //Verifie se les sommets ont le même nom

    @Override
    public boolean equals(Object o) {
        return nom.equals(((Fanal) o).getNom());
    }

    //Verifie lequel des valeurs est plus grandes

    public int compareTo(Object o) {
        Fanal s = (Fanal) o;
        return this.nom.compareTo(s.getNom());
    }

    //Renvoie le hashCode du nom

    @Override
    public int hashCode() {
        return nom.hashCode();
    }

    public boolean isUsed() {
        return this.used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }
    
     public int getDegEntrant() {
        return this.degEntrant;
    }

    public void incDegEntrant() {
        this.degEntrant++;
    }

    public int getDegSortant() {
        return this.degSortant;
    }

    public void incDegSortant() {
        this.degSortant++;
    }

}
