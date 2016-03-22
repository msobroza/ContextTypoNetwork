package control.network;

import control.NetworkControl.TypeNetwork;
import control.rules.LetterInformation;
import java.util.LinkedList;
import model.Clique;

public abstract class Network implements LetterInformation {
    
    protected int hCounter;
    protected LinkedList<Level> levelsList;
    protected TypeNetwork TYPE_NETWORK;
    
    
    @Override
    public String toString() {
        String result = "";
        for (Level n : levelsList) {
            result += n.toString() + "\n";

        }
        return result;
    }
    public abstract Clique learnWord(String word);
    
    public abstract Clique learnPhoneme(String phon);

    public LinkedList<Level> getLevelsList() {
        return levelsList;
    }
    
    public TypeNetwork getTypeReseau(){
        return TYPE_NETWORK;
    }
}
