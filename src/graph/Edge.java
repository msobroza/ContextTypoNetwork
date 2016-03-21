
package graph;

import model.Fanal;

public class Edge {
    
    private Fanal sourceFanal;
    private Fanal destinationFanal;
    private int value;
    private boolean active;
    private boolean oriented;
    
    // Méthode constructeur de l'Arc
    public Edge(Fanal orig, Fanal dest, int valeur, boolean oriente){
        this.sourceFanal=orig;
        this.destinationFanal=dest;
        this.value=valeur;
        this.oriented=oriente;
        
        // Incrémente le degré sortant du fanal d'origine et le degré entrant du fanal destinataire
        orig.increaseOutDegree();
        dest.increaseInDegree();
    }
    // Ce méthode va dupliquer l'arc selon ses attributs
    public Edge(Edge arc){
        this.sourceFanal=arc.getSourceFanal();
        this.destinationFanal=arc.getDestinationFanal();
        this.oriented=arc.oriented;
    }
    public boolean isOriented(){
        return this.oriented;
    }
    public void setOriented(boolean oriente){
        this.oriented=oriente;
    }
    public boolean isActive(){
        return this.active;
    }
    public void setActive(boolean active){
        this.active=active;
    }
    public Fanal getSourceFanal(){
        return sourceFanal;
    }
    public Fanal getDestinationFanal(){
        return destinationFanal;
    }
    public int getValue(){
        return value;
    }
    public void setSourceFanal(Fanal orig){
        this.sourceFanal=orig;
    }
    public void setDestinationFanal(Fanal dest){
        this.destinationFanal=dest;
    }
    public void setValue(int valeur){
        this.value=valeur;
    }
    @Override
    public boolean equals(Object o){
        Edge arc=(Edge)o;
        return this.sourceFanal.equals(arc.getSourceFanal())&& 
                this.destinationFanal.equals(arc.getDestinationFanal())&& this.value==arc.getValue();
    }
    
    @Override
    public String toString(){
        return "("+this.sourceFanal+"||||"+this.destinationFanal+")";
    }
    @Override
    public int hashCode(){
        String str=""+this;
        return str.hashCode();
    }
    
    
}