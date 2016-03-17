package model;

import control.rules.LetterInformation;
import control.rules.PhonemeRules;
import java.util.ArrayList;
import java.util.LinkedHashMap;

public class Grapheme implements LetterInformation {

    // Ça correspond à l'unite d'orthographie
    private String unitePhono;
    // Liste de phonemes du grapheme
    private ArrayList<String> listePhonemes;

    public Grapheme(String unitePhono){
        this.unitePhono=unitePhono;
        this.listePhonemes=new ArrayList<>();
    }
    public Grapheme(String unitePhono, String [] listePhon){
        this.unitePhono=unitePhono;
        this.listePhonemes=new ArrayList<>();
        ajoutePhoneme(listePhon);
    }
    public Grapheme(String unitePhono, ArrayList<String> listePhonemes){
        this.unitePhono=unitePhono;
        this.listePhonemes=listePhonemes;
    }

    public String getUnitePhono() {
        return this.unitePhono;
    }
    
    public int getLongueurUnitePhono(){
        int result=0;
        if(this.unitePhono.contains(CARAC_ESP)){
            String s;
            for(int i=0; i<this.unitePhono.length();i++){
                s=this.unitePhono.substring(i, i+1);
                if(s.equals(CARAC_ESP))
                    result++;
            }
            s=this.unitePhono.replaceAll(PhonemeRules.VOYELLE_L,"").replaceAll(PhonemeRules.VOYELLE_R,"").replaceAll(PhonemeRules.CONSONNE_L,"").replaceAll(PhonemeRules.CONSONNE_R,"");
            result+=s.length();
        }else{
            result=this.unitePhono.length();
        }
        return result;
    }

   

    public boolean ajoutePhoneme(String phon) {
        if (!listePhonemes.contains(phon)) {
            listePhonemes.add(phon);
            return true;
        } else {
            return false;
        }
    }

    public boolean ajoutePhoneme(String[] listePhon) {
        for (String phon : listePhon) {
            if (!listePhonemes.contains(phon)) {
                listePhonemes.add(phon);
            }
        }
        return true;
    }

    public ArrayList<String> getListePhonemes() {
        return listePhonemes;
    }
    
    

}
