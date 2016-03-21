package control.network;

import control.rules.LetterInformation;
import java.util.LinkedList;
import model.Clique;

public abstract class Network implements LetterInformation {
    
    protected int hCounter;
    protected LinkedList<Level> levelsList;
    protected int TYPE_RESEAU;
    
    
    @Override
    public String toString() {
        String result = "";
        for (Level n : levelsList) {
            result += n.toString() + "\n";

        }
        return result;
    }
    public abstract Clique learnWord(String mot);
    
    public abstract Clique learnPhoneme(String phon);

    public LinkedList<Level> getLevelsList() {
        return levelsList;
    }
    
    public int getTypeReseau(){
        return TYPE_RESEAU;
    }
    

    protected static boolean isDoubleLettre(String mot) {
        for (int i = 0; i < mot.length() - 1; i++) {
            if (mot.charAt(i) == mot.charAt(i + 1)) {
                return true;
            }
        }
        return false;
    }

    protected static boolean isDoubleLettreNonCons(String mot) {
        for (int i = 0; i < mot.length() - 1; i++) {
            for (int j = i + 1; j < mot.length(); j++) {
                if (mot.charAt(i) == mot.charAt(j)) {
                    return true;
                }
            }
        }
        return false;
    }
}
