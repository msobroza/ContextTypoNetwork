package model;

public class FanalFlous extends Fanal {

    private int id;
    private String letter;
    private Cluster cluster;
    private MacroFanal macroFanal;

    public FanalFlous(String nom, int score) {
        super(nom, score);
        this.letter = "";
        this.macroFanal = null;
        this.id = -1;

    }

    public FanalFlous(FanalFlous f, int score) {
        super((Fanal) f, score);
        this.letter = "";
        this.macroFanal = null;
        this.id = f.getId();
        this.cluster = f.getCluster();
    }

    public String getLetter() {
        return this.letter;
    }

    public void setLettre(String lettre) {
        this.letter = lettre;
    }

    public MacroFanal getMacroFanal() {
        return this.macroFanal;
    }

    public void setMacroFanal(MacroFanal macroFanal) {
        this.macroFanal = macroFanal;
    }

    public int getId() {
        return this.id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setCluster(Cluster cluster) {
        this.cluster = cluster;
    }

    public Cluster getCluster() {
        return this.cluster;
    }

}
