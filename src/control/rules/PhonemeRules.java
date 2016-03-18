package control.rules;

import com.google.common.collect.Sets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import model.Grapheme;

public class PhonemeRules implements LetterInformation {

    public static boolean LEFT = false;
    public static boolean RIGHT = true;
    public static String VOYELLE_L = "#VL";
    public static String VOYELLE_R = "#VR";
    public static String CONSONNE_L = "#CL";
    public static String CONSONNE_R = "#CR";
    // Ça represent une valeur nulle
    public static String NULL = "#N";


    public static LinkedHashMap<String, ArrayList<String>> supprimePhonemesNulls(LinkedHashMap<String, ArrayList<String>> phonsEntree) {
        LinkedHashMap<String, ArrayList<String>> phonsSortie = new LinkedHashMap<>(phonsEntree);
        ArrayList<String> phonListe;
        boolean premierElement = true;
        String dernierMot = "";
        int c = 0;
        for (String keyPhon : phonsEntree.keySet()) {
            if (phonsEntree.get(keyPhon).size() == 1 && phonsEntree.get(keyPhon).get(0).equals(NULL)) {
                if (premierElement) {
                    phonListe = new ArrayList<>();
                    phonListe.add(BEGIN_WORD_CHAR);
                    phonsSortie.put(keyPhon, phonListe);
                    premierElement = false;
                } else {
                    phonsSortie.remove(keyPhon);
                }

            } else {
                // Supprime le null, si c'est la fin on le remplace pour >
                if (phonsEntree.get(keyPhon).contains(NULL)) {
                    phonListe = new ArrayList<>(phonsSortie.get(keyPhon));
                    phonListe.remove(NULL);
                    if (phonsEntree.size() - 1 == c) {
                        dernierMot = keyPhon;
                        phonListe.add(END_WORD_CHAR);
                    }
                    phonsSortie.put(keyPhon, phonListe);
                }
            }
            c++;
        }
        if (dernierMot.equals("") || !phonsSortie.get(dernierMot).contains(END_WORD_CHAR)) {
            phonListe = new ArrayList<>();
            phonListe.add(END_WORD_CHAR);
            phonsSortie.put(END_WORD_CHAR, phonListe);
        }
        return phonsSortie;
    }

    // Ce methode permet de lire la liste de phonemes
    public static String phonemesToString(LinkedHashMap<String, ArrayList<String>> lstPhon) {
        String result = "";
        for (String key : lstPhon.keySet()) {
            result += key + " -> ";
            for (String ph : lstPhon.get(key)) {
                result += ph + ";";
            }
        }
        return result;
    }

    // Conversion des formats -> format graphe pour une liste de phonemes
    public static LinkedList<List<String>> graphemePhonemesToList(LinkedHashMap<String, ArrayList<String>> lstPhon) {
        LinkedList<List<String>> result = new LinkedList<>();
        for (String grapheme : lstPhon.keySet()) {
            result.add(lstPhon.get(grapheme));
        }
        return result;
    }

    // Conversion des formats -> liste de phonemes correctes pour une liste d'une liste de phonemes
    public static LinkedList<List<String>> phonemesCorrectesToList(List<String> phonsCorrectes) {
        LinkedList<List<String>> result = new LinkedList<>();
        List<String> listePhons;
        for (String phon : phonsCorrectes) {
            listePhons = new ArrayList<>();
            listePhons.add(phon);
            result.add(listePhons);
        }
        return result;
    }


    public static LinkedList<List<String>> phonemeToListParser(String phonLIA) {

        LinkedList<List<String>> result = new LinkedList<>();
        phonLIA = "<" + phonLIA + ">";
        List<String> listeAux;
        for (String phon : separePhonemes(phonLIA)) {
            listeAux = new ArrayList<>();
            listeAux.add(phon);
            result.add(listeAux);
        }
        return result;
    }

    public static String phonemeLiaToString(LinkedList<List<String>> phonLIA) {
        String result = "";
        for (List<String> lstPhons : phonLIA) {
            if (lstPhons.size() == 1) {
                result += lstPhons.get(0);
            } else {
                result += "(";
                for (String p : lstPhons) {
                    result = result + p + ";";
                }
                result = result.substring(0, result.length() - 1) + ")";
            }
        }
        return result;
    }

    // Separe en phonemes plus petits
    public static List<String> separePhonemes(String phon) {
        String seq = "";
        int offset;
        String[] seqMod;
        ArrayList<String> result = new ArrayList<>();

        seq = LetterInformation.BEGIN_WORD_CHAR;
        result.add(seq);
        phon = phon.substring(1, phon.length() - 1);
        for (int pos = 0; pos < phon.length(); pos = pos + 2) {
            result.add(phon.substring(pos, pos + 2));
        }
        seq = LetterInformation.END_WORD_CHAR;
        result.add(seq);

        return result;
    }

    public static List<String> separePhonemes(LinkedList<List<String>> unite) {
        List<String> result = new ArrayList<>();
        for (List<String> lstUnite : unite) {
            result.add(lstUnite.get(0));
        }
        return result;
    }

    public static List<String> phonemesLIAToList(String phon) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < phon.length(); i++) {
            if (!phon.substring(i, i + 1).equals(ERASURE_CHAR) && !phon.substring(i, i + 1).equals(BEGIN_WORD_CHAR) && !phon.substring(i, i + 1).equals(END_WORD_CHAR)) {
                result.add(phon.substring(i, i + 2));
                i++;
            } else {
                result.add(phon.substring(i, i + 1));
            }

        }
        return result;
    }


}
