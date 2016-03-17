
package graph;

import model.Fanal;

public class Edge {
    
    private Fanal orig;
    private Fanal dest;
    private int valeur;
    private boolean active;
    private boolean oriente;
    
    // Méthode constructeur de l'Arc
    public Edge(Fanal orig, Fanal dest, int valeur, boolean oriente){
        this.orig=orig;
        this.dest=dest;
        this.valeur=valeur;
        this.oriente=oriente;
        
        // Incrémente le degré sortant du fanal d'origine et le degré entrant du fanal destinataire
        orig.incDegSortant();
        dest.incDegEntrant();
    }
    // Ce méthode va dupliquer l'arc selon ses attributs
    public Edge(Edge arc){
        this.orig=arc.getOrig();
        this.dest=arc.getDest();
        this.oriente=arc.oriente;
    }
    public boolean isOriente(){
        return this.oriente;
    }
    public void setOriente(boolean oriente){
        this.oriente=oriente;
    }
    public boolean isActive(){
        return this.active;
    }
    public void setActive(boolean active){
        this.active=active;
    }
    public Fanal getOrig(){
        return orig;
    }
    public Fanal getDest(){
        return dest;
    }
    public int getValeur(){
        return valeur;
    }
    public void setOrig(Fanal orig){
        this.orig=orig;
    }
    public void setDest(Fanal dest){
        this.dest=dest;
    }
    public void setValeur(int valeur){
        this.valeur=valeur;
    }
    @Override
    public boolean equals(Object o){
        Edge arc=(Edge)o;
        return this.orig.equals(arc.getOrig())&& 
                this.dest.equals(arc.getDest())&& this.valeur==arc.getValeur();
    }
    
    @Override
    public String toString(){
        return "("+this.orig+"||||"+this.dest+")";
    }
    @Override
    public int hashCode(){
        String str=""+this;
        return str.hashCode();
    }
    
    
}