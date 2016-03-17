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

    // Tableau contenant la transcription de graphèmes-phonèmes
    public static LinkedHashMap<String, Grapheme> TGP;
    // Liste contenant le nombre de faux phonemes
    public static HashMap<String, Integer> fauxPhonemes;
    // Liste contenant les remplacements de phonemes par les classes (voyelles, consonnes)
    public static HashMap<String, String> mapPhonMod;
    // Liste qui contient tous les phonemes utilises
    public static TreeSet<String> tableauPhonemes;
    // Tableau contenant la correspondance des graphemes format LIA pour le format du lexique
    public static HashMap<String, String> TLL;

    public PhonemeRules() {
        TGP = new LinkedHashMap<>();
        TLL = new HashMap<>();
        fauxPhonemes = new HashMap<>();
        mapPhonMod = new HashMap<>();
        tableauPhonemes = new TreeSet<>();
        demarreTGP();
        demarreTLL();
        demarreTableauPhonemes();
    }

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
                    phonListe.add(CARAC_DEBUT);
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
                        phonListe.add(CARAC_FIN);
                    }
                    phonsSortie.put(keyPhon, phonListe);
                }
            }
            c++;
        }
        if (dernierMot.equals("") || !phonsSortie.get(dernierMot).contains(CARAC_FIN)) {
            phonListe = new ArrayList<>();
            phonListe.add(CARAC_FIN);
            phonsSortie.put(CARAC_FIN, phonListe);
        }
        return phonsSortie;
    }

    // Ce methode cherche les phonemes du mot (On divise en trigrammes) 
    public LinkedHashMap<String, ArrayList<String>> cherchePhonemesMot(String mot) {
        String sousMot;
        Grapheme gf;
        ArrayList<String> lstPhons;
        LinkedHashMap<String, ArrayList<String>> result = new LinkedHashMap<>();
        mot = CARAC_DEBUT + mot + CARAC_FIN;
        // On ajoute le symbole < afin de representer la derniere sequence
        int memoireGrapheme = 0;
        for (int i = 0; i < mot.length() - 1; i++) {
            if (i == mot.length() - 2) {
                sousMot = mot.substring(i) + CARAC_DEBUT;
            } else {
                sousMot = mot.substring(i, i + 3);
            }
            // On appelle la méthode chercheGrapheme pour chercher la meilleur correspondance du tri, bi...
            gf = chercheGrapheme(sousMot);
            // Si on ne trouve pas un grapheme, on ajoute un symbole nulle dans la liste de phonemes du trigramme considere
            if (gf == null) {
                lstPhons = new ArrayList<>();
                lstPhons.add(NULL);
                result.put(sousMot, lstPhons);
                if (memoireGrapheme > 0) {
                    memoireGrapheme--;
                }
            } else {
                // Si on trouve une sequence #, on essaie de trouver la correspondance 
                if (gf.getUnitePhono().contains(CARAC_ESP)) {
                    lstPhons = new ArrayList<>();
                    if (memoireGrapheme > gf.getLongueurUnitePhono()) {
                        lstPhons.add(NULL);
                        memoireGrapheme--;
                    } else {
                        for (String phonAbst : gf.getListePhonemes()) {
                            // On appelle le methode remplaceVoyelleConsoPhon afin de remplacer les symboles par les phonemes
                            // Et par la suite obtenir les phonemes correspondants au grapheme
                            lstPhons.addAll(remplaceVoyelleConsoPhon(mapPhonMod.get(gf.getUnitePhono()), phonAbst));
                        }
                        memoireGrapheme = gf.getLongueurUnitePhono();
                    }

                    result.put(sousMot, lstPhons);
                } else {
                    if (memoireGrapheme > gf.getLongueurUnitePhono()) {
                        lstPhons = new ArrayList<>();
                        lstPhons.add(NULL);
                        result.put(sousMot, lstPhons);
                        memoireGrapheme--;
                    } else {
                        memoireGrapheme = gf.getLongueurUnitePhono();
                        // Au cas ou il n'y a pas sequence avec #, on essaie de trouver d'autres sequences
                        result.put(sousMot, gf.getListePhonemes());
                    }
                }

            }
        }
        return result;
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

    // Cette méthode cherche le grapheme mieux adapté (sequence plus grande comme priorité)
    private Grapheme chercheGrapheme(String suite) {
        if (TGP.containsKey(suite)) {
            return TGP.get(suite);
        } else {
            // Si on ne trouve pas une lettre correspondant au grapheme on returne null;
            if (suite.length() == 1) {
                return null;
            } else {
                // Il remplace la sequence par une sequence modifie avec #
                for (String suiteMod : remplaceVoyelleConso(suite)) {
                    if (TGP.containsKey(suiteMod)) {
                        PhonemeRules.mapPhonMod.put(suiteMod, suite);
                        return TGP.get(suiteMod);
                    }
                }
                // Si le grapheme n'existe pas il cherche une sequence plus petite
                return chercheGrapheme(suite.substring(0, suite.length() - 1));
            }
        }
    }

    // A partir du grapheme d'origin il remplace le phoneme abstrait par les phonemes réels 
    private static ArrayList<String> remplaceVoyelleConsoPhon(String graphemeOrig, String phoneme) {
        ArrayList<String> result = new ArrayList<>();
        HashMap<String, String> subsMap = new HashMap<>();
        String[] listeExp = new String[]{VOYELLE_L, CONSONNE_L, VOYELLE_R, CONSONNE_R};
        String lettreLeft, lettreRight;
        String phonemeMod = phoneme;
        switch (graphemeOrig.length()) {
            // Si c'est un trigramme
            case 3:
                // Il garde toute les voyelles et consonnes
                lettreLeft = graphemeOrig.substring(0, 1);
                lettreRight = graphemeOrig.substring(2, 3);
                if (VOYELLES.contains(lettreLeft)) {
                    subsMap.put(VOYELLE_L, lettreLeft);
                }
                if (CONSONNES.contains(lettreLeft)) {
                    subsMap.put(CONSONNE_L, lettreLeft);
                }
                if (VOYELLES.contains(lettreRight)) {
                    subsMap.put(VOYELLE_R, lettreRight);
                }
                if (CONSONNES.contains(lettreRight)) {
                    subsMap.put(CONSONNE_R, lettreRight);
                }
                break;
            // Si c'est un bigramme
            case 2:
                // Il garde toute les voyelles et consonnes
                lettreLeft = graphemeOrig.substring(0, 1);
                lettreRight = graphemeOrig.substring(1, 2);
                if (VOYELLES.contains(lettreLeft)) {
                    subsMap.put(VOYELLE_L, lettreLeft);
                }
                if (CONSONNES.contains(lettreLeft)) {
                    subsMap.put(CONSONNE_L, lettreLeft);
                }
                if (VOYELLES.contains(lettreRight)) {
                    subsMap.put(VOYELLE_R, lettreRight);
                }
                if (CONSONNES.contains(lettreRight)) {
                    subsMap.put(CONSONNE_R, lettreRight);
                }
                break;
            case 0:
                result.add(phoneme);
                break;
            default:
                return null;
        }
        ArrayList<String> usedExpressions = new ArrayList<>();
        Set<List<String>> produitCartPhons;
        ArrayList<String> listePhonemesA, listePhonemesB;
        // Parmi la liste d'expressions il verifie lequelles ont été utilisées
        for (String exp : listeExp) {
            if (phoneme.contains(exp)) {
                usedExpressions.add(exp);
            }
        }
        switch (usedExpressions.size()) {
            // Nous pouvons avoir deux cas
            // Deux expressions ont été utilisées
            case 2:
                // Si l'expression reelle est un grapheme nous le remplaçons
                // Sinon on ne le considere pas dans la construction du phoneme
                if (!TGP.containsKey(subsMap.get(usedExpressions.get(0)))) {
                    listePhonemesA = new ArrayList<>();
                    listePhonemesA.add("");
                } else {
                    listePhonemesA = TGP.get(subsMap.get(usedExpressions.get(0))).getListePhonemes();
                }
                if (!TGP.containsKey(subsMap.get(usedExpressions.get(1)))) {
                    listePhonemesB = new ArrayList<>();
                    listePhonemesB.add("");
                } else {
                    listePhonemesB = TGP.get(subsMap.get(usedExpressions.get(1))).getListePhonemes();
                }
                produitCartPhons = PhonemeRules.cartesianStringProduct(listePhonemesA, listePhonemesB);
                for (List<String> composant : produitCartPhons) {
                    phonemeMod = phoneme;
                    for (int i = 0; i < usedExpressions.size(); i++) {
                        if (phonemeMod.contains(usedExpressions.get(i))) {
                            phonemeMod = phonemeMod.replace(usedExpressions.get(i), composant.get(i));
                        }
                    }
                    result.add(phonemeMod);
                }
                break;
            // Une seule expression a été utilisée    
            case 1:
                if (!TGP.containsKey(subsMap.get(usedExpressions.get(0)))) {
                    listePhonemesA = new ArrayList<>();
                    listePhonemesA.add("");
                } else {
                    listePhonemesA = TGP.get(subsMap.get(usedExpressions.get(0))).getListePhonemes();
                }
                for (String phon : listePhonemesA) {
                    phonemeMod = phoneme;
                    phonemeMod = phonemeMod.replace(usedExpressions.get(0), phon);
                    result.add(phonemeMod);
                }
                break;
            default:
                return null;
        }
        return result;
    }

    private static String[] remplaceVoyelleConso(String suite) {
        String[] result;
        switch (suite.length()) {
            // On va creer tous les cas de remplacements de lettres par les sequences
            case 3:
                result = new String[]{singleLettreVoyelleConso(suite.substring(0, 1), LEFT) + suite.substring(1, 2) + singleLettreVoyelleConso(suite.substring(2, 3), RIGHT)};
                break;
            case 2:
                result = new String[]{singleLettreVoyelleConso(suite.substring(0, 1), LEFT) + suite.substring(1, 2), suite.substring(0, 1) + singleLettreVoyelleConso(suite.substring(1, 2), RIGHT)};
                break;
            default:
                result = new String[]{suite};
                break;
        }
        return result;
    }

    // Remplace une lettre par la sequence correspondante avec #
    public static String singleLettreVoyelleConso(String lettre, boolean side) {
        if (PhonemeRules.CONSONNES.contains(lettre)) {
            if (side == LEFT) {
                return PhonemeRules.CONSONNE_L;
            } else {
                return PhonemeRules.CONSONNE_R;
            }

        } else {
            if (PhonemeRules.VOYELLES.contains(lettre)) {
                if (side == LEFT) {
                    return PhonemeRules.VOYELLE_L;
                } else {
                    return PhonemeRules.VOYELLE_R;
                }
            } else {
                return lettre;
            }
        }
    }

    // Calcule le nombre de phonemes divise par le nombre de phonemes utilises
    public static Double calculeTauxPrecision(String phons, LinkedHashMap<String, ArrayList<String>> lstPhonemes) {
        return ((double) nombrePhonemes(phons)) / nombrePhonemes(lstPhonemes);
    }

    // Verifie s'il a trouve tous les phonemes du mot dans la meme sequence
    public static boolean phonemesContient(String mot, String phons, LinkedHashMap<String, ArrayList<String>> lstPhonemes) {
        String phonsActives = "", unitePhon;
        String[] partiesPhons;
        for (String keyPhon : lstPhonemes.keySet()) {
            phonsActives += lstPhonemes.get(keyPhon).toString();
        }
        // Il cree une liste avec tous les phonemes actives en utilisant des separateurs
        for (int i = 0; i < phonsActives.length() - 1; i++) {
            if (phonsActives.substring(i, i + 2).equalsIgnoreCase("][")) {
                phonsActives = phonsActives.substring(0, i) + ";" + phonsActives.substring(i + 2);
            } else {
                if (phonsActives.substring(i, i + 1).equals(" ")) {
                    phonsActives = phonsActives.substring(0, i) + "" + phonsActives.substring(i + 1);
                }
            }
        }
        phonsActives = phonsActives.substring(1, phonsActives.length() - 1);
        partiesPhons = phonsActives.split(";");
        int debutIndice = 0;
        boolean uniteTrouve;
        for (int i = 0; i < phons.length(); i++) {
            unitePhon = phons.substring(i, i + 1);
            uniteTrouve = false;
            // Il verifie dans l'ordre tous les phonemes
            for (int j = debutIndice; j < partiesPhons.length; j++) {
                if (partiesPhons[j].contains(unitePhon)) {
                    uniteTrouve = true;
                    break;
                }
            }
            // Si il ne trouve pas uniteTrouve est false
            if (!uniteTrouve) {
                // Il garde un compteur avec les phonemes non trouves
                System.out.println("Non trouve: " + unitePhon + " ; Mot: " + mot);
                if (!fauxPhonemes.containsKey(unitePhon)) {
                    fauxPhonemes.put(unitePhon, 1);
                } else {
                    fauxPhonemes.put(unitePhon, fauxPhonemes.get(unitePhon) + 1);
                }
                return false;
            }
        }
        return true;

    }

    // Calcule le nombre des phonemes d'un mot
    private static int nombrePhonemes(String phons) {
        int count = 0;
        for (int i = 0; i < phons.length(); i++) {
            count++;
        }
        return count;
    }

    // Calcule le nombre des phonemes trouves
    private static int nombrePhonemes(LinkedHashMap<String, ArrayList<String>> phons) {
        int count = 0;
        for (String keyPhon : phons.keySet()) {
            if (!phons.get(keyPhon).isEmpty()) {
                for (String element : phons.get(keyPhon)) {
                    if (!element.equals(NULL)) {
                        for (int i = 0; i < element.length(); i++) {
                            count++;
                        }
                    }
                }
            }
        }
        return count;
    }

    private void ajouteRegle(String uniteGrapheme, String[] phonemes) {
        if (!TGP.containsKey(uniteGrapheme)) {
            TGP.put(uniteGrapheme, new Grapheme(uniteGrapheme, phonemes));
        }
    }

    private void ajouteRegle(String expressionGrapheme, String[] phonemes, String expA, String expB) {
        LinkedHashMap<String, Grapheme> regleComplexe = elargirReglesTGP(expressionGrapheme, phonemes, expA, expB);
        for (String formatGrapheme : regleComplexe.keySet()) {
            if (!TGP.containsKey(formatGrapheme)) {
                TGP.put(formatGrapheme, regleComplexe.get(formatGrapheme));
            }
        }

    }

    private void ajouteRegle(String expressionGrapheme, String[] phonemes, String expA) {
        LinkedHashMap<String, Grapheme> regleComplexe = elargirReglesTGP(expressionGrapheme, phonemes, expA);
        for (String formatGrapheme : regleComplexe.keySet()) {
            if (!TGP.containsKey(formatGrapheme)) {
                TGP.put(formatGrapheme, regleComplexe.get(formatGrapheme));
            }
        }
    }

    // Tableau Grapheme-Phoneme
    private void demarreTGP() {

        ajouteRegle("aa", new String[]{"a"}); // kraal |kRal|
        ajouteRegle("ach", new String[]{"O", "aSi"}); // yacht |jOt|, homme-machine |OmmaSin|
        ajouteRegle("ac>", new String[]{"a", "ak"}); // tabac |taba|, lac |lak|
        ajouteRegle("act", new String[]{"akt", "aks"}); // exact |Egzakt|, faction |faksj§|
        ajouteRegle("ae", new String[]{"a"}); // maelstrom |malstROm|
        ajouteRegle("ah", new String[]{"a"}); // trahit |tRai|
        ajouteRegle("ahs", new String[]{"a"}); // fellahs |fela|
        ajouteRegle("aid", new String[]{"E"}); //  laid |lE|
        ajouteRegle("aie", new String[]{"E", "e"}); // ivraie |ivRE|, paierie |peRi|
        ajouteRegle("ail", new String[]{"aj"}); // ail |aj|
        ajouteRegle("aim", new String[]{"5"});
        ajouteRegle("ai", new String[]{"E", "e"}); // air |ER|, aimer |eme| 
        ajouteRegle("aî", new String[]{"E", "e"}); // chaîne |SEn|, traîner |tRene|
        ajouteRegle("ain", new String[]{"5"});
        ajouteRegle("ais", new String[]{"E"}); // frais |fRE|
        ajouteRegle("aît", new String[]{"E"}); // connaît |konE|
        ajouteRegle("ait", new String[]{"E"}); // parfait |paRfE|
        ajouteRegle("aix", new String[]{"E"}); // aix |pE|
        ajouteRegle("aoû", new String[]{"u"}); // août |ut|
        ajouteRegle("am", new String[]{"@", "am"});  //  , ami |ami|
        ajouteRegle("â", new String[]{"a"}); // mâchicoulis |maSikuli|
        ajouteRegle("a", new String[]{"a", "O", "e", "E"}); // ami |ami| , , relayer |R°leje|, rayon |REj§|
        ajouteRegle("à", new String[]{"a"}); // voilà |vwala|
        ajouteRegle("an", new String[]{"@", "an"});
        ajouteRegle("aon", new String[]{"@", "an"}); // , paonne |pan|
        ajouteRegle("ap>", new String[]{"a"}); // drap |dRa|
        ajouteRegle("aps", new String[]{"a"}); // draps |dRa|
        ajouteRegle("ars", new String[]{"a"}); // gars |ga|
        ajouteRegle("as>", new String[]{"a"}); // cas |ka|
        ajouteRegle("at>", new String[]{"a"}); // rat |Ra|
        ajouteRegle("âts", new String[]{"a"}); // dégâts |dega|
        ajouteRegle("ats", new String[]{"a"}); // rats |Ra|
        ajouteRegle("au", new String[]{"O", "o"}); // , austère |ostER|
        ajouteRegle("aye", new String[]{"E"}); // paye |pEj|
        ajouteRegle("ay", new String[]{"E"}); // saynète |sEnEt|
        ajouteRegle("az>", new String[]{"a"}); //raz |Ra|  
        ajouteRegle("bb", new String[]{"b"});
        ajouteRegle("b", new String[]{"b", "p"});
        ajouteRegle("cc", new String[]{"k", "ks"});
        ajouteRegle("ch", new String[]{"S", "k"});
        ajouteRegle("c", new String[]{"k", "s"});
        ajouteRegle("cu", new String[]{"ky"});  // verificar
        ajouteRegle("ç", new String[]{"s"});
        ajouteRegle("dd", new String[]{"d"});
        ajouteRegle("dh", new String[]{"d"});
        ajouteRegle("d", new String[]{"d"});
        ajouteRegle("d>", new String[]{NULL});
        ajouteRegle("ea", new String[]{"i"}); // leader |lid9R|
        ajouteRegle("eil", new String[]{"Ej", "eil"});
        ajouteRegle("ei", new String[]{"e", "E"});
        ajouteRegle("em", new String[]{"5", "@", "Em", "°m"});
        ajouteRegle("é", new String[]{"e"});
        ajouteRegle("è", new String[]{"E"});
        ajouteRegle("ê", new String[]{"E"});
        ajouteRegle("ë", new String[]{"E"});
        ajouteRegle("e", new String[]{"E", "e", "°", "2", "3", "i"});
        ajouteRegle("ee", new String[]{"i"}); //jeep |dZip|
        ajouteRegle("e>", new String[]{"e", NULL});
        ajouteRegle("en", new String[]{"5", "@", "En", "°n"}); // , , , venin |v°n5|
        ajouteRegle("er>", new String[]{"e", "E"});
        ajouteRegle("es>", new String[]{"e", NULL});
        ajouteRegle("et", new String[]{"e", "E", "°t"}); // retrouvant |R°tRuv@
        ajouteRegle("eû", new String[]{"2", "9", "y"}); // eûmes |ym|
        ajouteRegle("eu", new String[]{"2", "9", "y"}); // eu |y|
        ajouteRegle("ez", new String[]{"e", "E"});
        ajouteRegle("ff", new String[]{"f"});
        ajouteRegle("f", new String[]{"f"});
        ajouteRegle("ge", new String[]{"Z"});
        ajouteRegle("gg", new String[]{"g"});
        ajouteRegle("g", new String[]{"g", "Z"});
        ajouteRegle("gn", new String[]{"N"});
        ajouteRegle("gu", new String[]{"g"});
        ajouteRegle("ha", new String[]{"a"}); // haillon |aj§|
        ajouteRegle("hau", new String[]{"o"}); // haubert |obER|
        ajouteRegle("hâ", new String[]{"a"}); // hâter |ate|
        ajouteRegle("hi", new String[]{"i", "j"}); //hiver |ivER|, hiatus |jatys|
        ajouteRegle("hit", new String[]{"i"}); //trahit |tRai|
        ajouteRegle("hie", new String[]{"i", "je"}); // envahie |@vai|, cahiers |kaje|
        ajouteRegle("ho", new String[]{"O"}); //dehors |d2OR|
        ajouteRegle("hô", new String[]{"o"}); // hôpital |opital|
        ajouteRegle("his", new String[]{"i", "is"}); // histoire |istwaR|, envahis |@vai|
        ajouteRegle("hu", new String[]{"8", "y"}); // huile |8il|, huchet |ySE|
        ajouteRegle("hue", new String[]{"y"}); // moustachue |mustaSy|
        ajouteRegle("hy", new String[]{"i"}); //  déshydrater |dezidRat|
        ajouteRegle("ids", new String[]{"a"}); // poids |pwa|
        ajouteRegle("im", new String[]{"5", "im"});
        ajouteRegle("î", new String[]{"i"}); // île |il| 
        ajouteRegle("ï", new String[]{"i"}); // inouï |inwi| 
        ajouteRegle("i", new String[]{"i", "j"}); // livre |livR|
        ajouteRegle("ie", new String[]{"i", "j"}); // vie |vi|, transuranienne |tR@zyRanjEn|
        ajouteRegle("ïe", new String[]{"i", "j"}); // inouïe |inwi|, trisaïeul | tRizaj9l
        ajouteRegle("in", new String[]{"5", "in", "an"}); // moine |mwan|
        ajouteRegle("is", new String[]{"a", "i"}); // pois |pwa|, avis |avi|
        ajouteRegle("ïs", new String[]{"i", "j"}); // aïs |ai|, thaïs | taj |
        ajouteRegle("it", new String[]{"i"}); // lit |li|
        ajouteRegle("its", new String[]{"i"}); // petits        
        ajouteRegle("ix", new String[]{"a", "i"}); // voix |vwa|, prix |pRi|
        ajouteRegle("iz", new String[]{"i"}); // riz |Ri|
        ajouteRegle("j", new String[]{"Z"});
        ajouteRegle("k", new String[]{"k"});
        ajouteRegle("ll", new String[]{"l", "j"}); // brillant |bRij@| 
        ajouteRegle("l", new String[]{"l"});
        ajouteRegle("mm", new String[]{"m"});
        ajouteRegle("m", new String[]{"m"});
        ajouteRegle("ng", new String[]{"G", "ng"});
        ajouteRegle("n", new String[]{"n"});
        ajouteRegle("nn", new String[]{"n"});
        ajouteRegle("oe", new String[]{"wa"});
        ajouteRegle("oê", new String[]{"wa"});
        ajouteRegle("oeu", new String[]{"2", "9"});
        ajouteRegle("oi", new String[]{"wa"});
        ajouteRegle("oî", new String[]{"wa"});
        ajouteRegle("oin", new String[]{"w5"});
        ajouteRegle("om", new String[]{"§", "Om", "om"});
        ajouteRegle("omm", new String[]{"§", "Om", "om"});
        ajouteRegle("ô", new String[]{"O"});
        ajouteRegle("o", new String[]{"o", "O"});
        ajouteRegle("on", new String[]{"§", "On", "on"});
        ajouteRegle("oo", new String[]{"O", "o", "oo", "u"});
        ajouteRegle("où", new String[]{"u"});
        ajouteRegle("oû", new String[]{"u"});
        ajouteRegle("ou", new String[]{"u", "w"});
        ajouteRegle("oy", new String[]{"wa"});
        ajouteRegle("ph", new String[]{"f"});
        ajouteRegle("p", new String[]{"p"});
        ajouteRegle("pp", new String[]{"p"});
        ajouteRegle("qu", new String[]{"k"});
        ajouteRegle("r", new String[]{"R"});
        ajouteRegle("rs>", new String[]{"R", NULL});
        ajouteRegle("rr", new String[]{"R"});
        ajouteRegle("sc", new String[]{"sk"});
        ajouteRegle("s>", new String[]{NULL});
        ajouteRegle("s", new String[]{"z", "s"});
        ajouteRegle("ss", new String[]{"s"});
        ajouteRegle("th", new String[]{"t"});
        ajouteRegle("ti", new String[]{"tj", "sj"});
        ajouteRegle("tti", new String[]{"ti", "tj"});
        ajouteRegle("t>", new String[]{NULL});
        ajouteRegle("t", new String[]{"t"});
        ajouteRegle("tt", new String[]{"t"});
        ajouteRegle("ue", new String[]{"9", "y", "°"}); // querelle |k°REl|
        ajouteRegle("ueu", new String[]{"2"}); // queue |k2|
        ajouteRegle("ue>", new String[]{NULL, "y"}); // macaque |makak|, charrue |SaRy|
        ajouteRegle("üe", new String[]{"u"});
        ajouteRegle("um", new String[]{"1", "ym"}); // argument |aRgym@| 
        ajouteRegle("u", new String[]{"y", "i", "8", "9", "u"}); // , business |biznEs|, fluidité |fl8idite|, trustais |tR9stE|,  yuppies |jupi|
        ajouteRegle("û", new String[]{"y"});
        ajouteRegle("un", new String[]{"1", "yn"}); // tunisien |tynizj5|
        ajouteRegle("v", new String[]{"v"});
        ajouteRegle("w", new String[]{"w"});
        ajouteRegle("x", new String[]{"ks", "gz"});
        ajouteRegle("x>", new String[]{NULL});
        ajouteRegle("y", new String[]{"i", "j"});
        ajouteRegle("ye", new String[]{"i", "j"}); //abbaye |abei|
        ajouteRegle("yes", new String[]{"i", "j"}); //abbayes |abei|, surpaye |syRpEj|
        ajouteRegle("yg", new String[]{"i"}); //amygdale |amidal|
        ajouteRegle("ys", new String[]{"i", "j"}); // pays |pei|, boys |bOj|
        ajouteRegle("z", new String[]{"z"});
    }

    /*
     private void demarreTGP() {

     LinkedHashMap<String, Grapheme> regleComplexe;
     // Voyelles orales
     ajouteRegle("a", new String[]{"a", "O"});
     ajouteRegle("à", new String[]{"a"});
     ajouteRegle("â", new String[]{"a"});
     ajouteRegle("é", new String[]{"e"});
     ajouteRegle("è", new String[]{"E"});
     ajouteRegle("ê", new String[]{"E"});
     ajouteRegle("ai", new String[]{"E"});
     ajouteRegle("aî", new String[]{"E", "e"});
     ajouteRegle("e", new String[]{"E", "e", "°", "2", "3"});
     ajouteRegle("e>", new String[]{"e",NULL});
     ajouteRegle("es>", new String[]{"e", NULL});
     ajouteRegle("ei", new String[]{"e", "E"});
     ajouteRegle("er>", new String[]{"e", "E"});
     ajouteRegle("et", new String[]{"e", "E"});
     ajouteRegle("ez", new String[]{"e", "E"});
     ajouteRegle("ë", new String[]{"E"});
     ajouteRegle("üe", new String[]{"u"});
     ajouteRegle("ue", new String[]{"9", "y"});
     ajouteRegle("eu", new String[]{"2", "9", "y"});
     ajouteRegle("eû", new String[]{"2", "9"});
     ajouteRegle("oeu", new String[]{"2", "9"});
     ajouteRegle("o", new String[]{"o", "O"});
     ajouteRegle("ô", new String[]{"O"});
     ajouteRegle("oo", new String[]{"O", "o", "oo", "u"});
     ajouteRegle("au", new String[]{"O", "o"});
     ajouteRegle("eau", new String[]{"O", "o"});
     // Semi-consonnes
     ajouteRegle("i", new String[]{"i", "j"}); // Régles especiales  
     ajouteRegle("î", new String[]{"i"});
     ajouteRegle("ï", new String[]{"i"});
     ajouteRegle("y", new String[]{"i", "j"});
     ajouteRegle("u", new String[]{"y"}); // Quand c'est suivi pour une voyelle
     ajouteRegle("û", new String[]{"y"});
     ajouteRegle("ou", new String[]{"u", "w"});
     ajouteRegle("oû", new String[]{"u"});
     ajouteRegle("où", new String[]{"u"});
     // Consonnes
     ajouteRegle("l", new String[]{"l"});
     ajouteRegle("ll", new String[]{"l", "j"});
     ajouteRegle("ail", new String[]{"aj"});
     ajouteRegle("eil", new String[]{"Ej", "eil"});
     ajouteRegle("r", new String[]{"R"});
     ajouteRegle("r", new String[]{"R"});
     ajouteRegle("rr", new String[]{"R"});
     ajouteRegle("f", new String[]{"f"});
     ajouteRegle("ff", new String[]{"f"});
     ajouteRegle("ph", new String[]{"f"});
     ajouteRegle("j", new String[]{"Z"});
     ajouteRegle("v", new String[]{"v"});
     ajouteRegle("z", new String[]{"z"});
     ajouteRegle("p", new String[]{"p"});
     ajouteRegle("pp", new String[]{"p"});
     ajouteRegle("t", new String[]{"t"});
     ajouteRegle("tt", new String[]{"t"});
     ajouteRegle("th", new String[]{"t"});
     ajouteRegle("ti", new String[]{"tj", "sj"});
     ajouteRegle("b", new String[]{"b", "p"});
     ajouteRegle("bb", new String[]{"b"});
     ajouteRegle("d", new String[]{"d"});
     ajouteRegle("dd", new String[]{"d"});
     ajouteRegle("m", new String[]{"m"});
     ajouteRegle("mm", new String[]{"m"});
     ajouteRegle("n", new String[]{"n"});
     ajouteRegle("nn", new String[]{"n"});
     ajouteRegle("gn", new String[]{"N"});
     ajouteRegle("ng", new String[]{"G", "ng"});
     ajouteRegle("g", new String[]{"g", "Z"});
     ajouteRegle("gg", new String[]{"g"});
     ajouteRegle("gu", new String[]{"g"});
     ajouteRegle("ge", new String[]{"Z"});
     // Il manque le grapheme gh ghetto
     ajouteRegle("c", new String[]{"k", "s"});
     // Voyelle suivate a, o, u  -> k cactus ; i, e, y -> s
     ajouteRegle("cc", new String[]{"k", "ks"});
     // La meme chose pour cc
     ajouteRegle("qu", new String[]{"k"});
     ajouteRegle("k", new String[]{"k"});
     ajouteRegle("x", new String[]{"ks", "gz"});
     // x Pareil
     ajouteRegle("s", new String[]{"z", "s"});
     ajouteRegle("ç", new String[]{"s"});
     ajouteRegle("ss", new String[]{"s"});
     ajouteRegle("sc", new String[]{"s"}); // sk
     ajouteRegle("s>", new String[]{NULL});
     ajouteRegle("x>", new String[]{NULL});
     ajouteRegle("t>", new String[]{NULL});
     ajouteRegle("d>", new String[]{NULL});
     ajouteRegle("s>", new String[]{NULL});
     // Voyelles nasales
     ajouteRegle("an",new String[]{"@", "an"}); // apres voyelle
     // anticipation et animation
     ajouteRegle("am", new String[]{"@", "am"});
     ajouteRegle("aon", new String[]{"@"});
     ajouteRegle("on", new String[]{"§", "On", "on"});
     ajouteRegle("om", new String[]{"§", "Om", "om"});
     ajouteRegle("ain", new String[]{"5"});
     ajouteRegle("aim", new String[]{"5"});
     ajouteRegle("in", new String[]{"5", "in"});
     ajouteRegle("im", new String[]{"5", "im"});
     ajouteRegle("en",  new String[]{"5", "@", "En"}); // suivi par une consonne pas voyelle
     ajouteRegle("em", new String[]{"5", "@", "Em"});
     ajouteRegle("un", new String[]{"1"});
     ajouteRegle("um", new String[]{"1"});
     ajouteRegle("ch", new String[]{"S", "k"});
     ajouteRegle("dh", new String[]{"d"});
     ajouteRegle("oi", new String[]{"wa"});
     ajouteRegle("oî", new String[]{"wa"});
     ajouteRegle("oy", new String[]{"wa"});
     ajouteRegle("oin", new String[]{"w5"});
     ajouteRegle("w", new String[]{"w"});
     ajouteRegle("oe", new String[]{"wa"});
     ajouteRegle("oê", new String[]{"wa"});
     // Ajout de règles complexes
     //ajouteRegle(CONSONNE_L + "e", new String[]{CONSONNE_L + "°"}, CONSONNES);
     //ajouteRegle("i" + VOYELLE_R, new String[]{"j" + VOYELLE_R}, VOYELLES);
     //ajouteRegle("i" + CONSONNE_R, new String[]{"i" + CONSONNE_R}, CONSONNES);
     //ajouteRegle("u" + VOYELLE_R, new String[]{"8" + VOYELLE_R}, VOYELLES);
     //ajouteRegle(CONSONNE_L + "u" + CONSONNE_R, new String[]{CONSONNE_L + "y" + CONSONNE_R}, CONSONNES, CONSONNES);
     //ajouteRegle(VOYELLE_L + "en", new String[]{VOYELLE_L + "5"}, VOYELLES);
     //ajo1uteRegle("en" + CONSONNE_R, new String[]{"@" + CONSONNE_R}, CONSONNES);
     } */
    /*private void demarreTGP() {
     // Voyelles orales
     TGP.put("a", new Grapheme("a", new String[]{"a", "O"}));
     TGP.put("à", new Grapheme("à", new String[]{"a"}));
     TGP.put("â", new Grapheme("â", new String[]{"a"}));
     TGP.put("é", new Grapheme("é", new String[]{"e"}));
     TGP.put("è", new Grapheme("è", new String[]{"E"}));
     TGP.put("ê", new Grapheme("ê", new String[]{"E"}));
     TGP.put("ai", new Grapheme("ai", new String[]{"E"}));
     TGP.put("aî", new Grapheme("aî", new String[]{"E", "e"}));
     TGP.put("e", new Grapheme("e", new String[]{"E", "e", "°"}));
     //TGP.put(CONSONNE_L + "e", new Grapheme(CONSONNE_L + "e", new String[]{CONSONNE_L + "°"}));
     TGP.put("es>", new Grapheme("es>", new String[]{"e", NULL}));
     TGP.put("e>", new Grapheme("e>", new String[]{"e", NULL}));
     TGP.put("ei", new Grapheme("ei", new String[]{"e", "E"}));
     TGP.put("er", new Grapheme("er", new String[]{"e", "E"}));
     TGP.put("et", new Grapheme("et", new String[]{"e", "E"}));
     TGP.put("ez", new Grapheme("ez", new String[]{"e", "E"}));
     TGP.put("ë", new Grapheme("ë", new String[]{"E"}));
     TGP.put("üe", new Grapheme("üe", new String[]{"u"}));
     TGP.put("eu", new Grapheme("eu", new String[]{"2", "9", "y"}));
     TGP.put("eû", new Grapheme("eû", new String[]{"2", "9"}));
     TGP.put("oeu", new Grapheme("oeu", new String[]{"2", "9"}));
     TGP.put("o", new Grapheme("o", new String[]{"o", "O"}));
     TGP.put("ô", new Grapheme("ô", new String[]{"O"}));
     TGP.put("oo", new Grapheme("oo", new String[]{"O", "o", "oo", "u"}));
     TGP.put("au", new Grapheme("au", new String[]{"O", "o"}));
     TGP.put("eau", new Grapheme("eau", new String[]{"O", "o"}));
     // Semi-consonnes
     TGP.put("i", new Grapheme("i", new String[]{"i", "j"}));
     TGP.put("î", new Grapheme("î", new String[]{"i"}));
     TGP.put("ï", new Grapheme("ï", new String[]{"i"}));
     TGP.put("y", new Grapheme("y", new String[]{"i", "j"}));
     TGP.put("u", new Grapheme("u", new String[]{"y", "8", "u"}));
     //TGP.put(CONSONNE_L + "u" + CONSONNE_R, new Grapheme(CONSONNE_L + "u" + CONSONNE_R, new String[]{CONSONNE_L + "y" + CONSONNE_R}));
     //TGP.put("u" + VOYELLE_R, new Grapheme("u" + VOYELLE_R, new String[]{"8" + VOYELLE_R, "y" + VOYELLE_R}));
     TGP.put("û", new Grapheme("û", new String[]{"y"}));
     TGP.put("ou", new Grapheme("ou", new String[]{"u", "w"}));
     TGP.put("oû", new Grapheme("oû", new String[]{"u"}));
     TGP.put("où", new Grapheme("où", new String[]{"u"}));
     // Consonnes
     TGP.put("l", new Grapheme("l", new String[]{"l"}));
     TGP.put("ll", new Grapheme("ll", new String[]{"l","j"}));
     // Modificar isso depois
     //TGP.put("ll", new Grapheme("ll", new String[]{"l", "j"}));
     TGP.put("ail", new Grapheme("ail", new String[]{"aj"}));
     TGP.put("eil", new Grapheme("eil", new String[]{"Ej", "eil","ej"}));
     TGP.put("r", new Grapheme("r", new String[]{"R"}));
     TGP.put("rr", new Grapheme("rr", new String[]{"R"}));
     TGP.put("f", new Grapheme("f", new String[]{"f"}));
     TGP.put("ff", new Grapheme("ff", new String[]{"f"}));
     TGP.put("ph", new Grapheme("ph", new String[]{"f"}));
     TGP.put("j", new Grapheme("j", new String[]{"Z"}));
     TGP.put("v", new Grapheme("v", new String[]{"v"}));
     TGP.put("z", new Grapheme("z", new String[]{"z"}));
     TGP.put("p", new Grapheme("p", new String[]{"p"}));
     TGP.put("pp", new Grapheme("pp", new String[]{"p"}));
     TGP.put("t", new Grapheme("t", new String[]{"t"}));
     TGP.put("tt", new Grapheme("tt", new String[]{"t"}));
     TGP.put("th", new Grapheme("th", new String[]{"t"}));
     TGP.put("ti", new Grapheme("ti", new String[]{"tj", "sj"}));
     TGP.put("b", new Grapheme("b", new String[]{"b", "p"}));
     TGP.put("bb", new Grapheme("bb", new String[]{"b"}));
     TGP.put("d", new Grapheme("d", new String[]{"d"}));
     TGP.put("dd", new Grapheme("dd", new String[]{"d"}));
     TGP.put("m", new Grapheme("m", new String[]{"m"}));
     TGP.put("mm", new Grapheme("mm", new String[]{"m"}));
     TGP.put("n", new Grapheme("n", new String[]{"n"}));
     TGP.put("nn", new Grapheme("nn", new String[]{"n"}));
     TGP.put("gn", new Grapheme("gn", new String[]{"N"}));
     TGP.put("ng", new Grapheme("ng", new String[]{"G", "ng"}));
     TGP.put("g", new Grapheme("g", new String[]{"g", "Z"}));
     TGP.put("gg", new Grapheme("gg", new String[]{"g"}));
     TGP.put("gu", new Grapheme("gu", new String[]{"g"}));
     TGP.put("ge", new Grapheme("ge", new String[]{"Z"}));
     TGP.put("c", new Grapheme("c", new String[]{"k", "s"}));
     TGP.put("cc", new Grapheme("cc", new String[]{"k", "ks"}));
     TGP.put("qu", new Grapheme("qu", new String[]{"k"}));
     TGP.put("k", new Grapheme("k", new String[]{"k"}));
     TGP.put("x", new Grapheme("x", new String[]{"ks", "gz"}));
     // TGP.put("<s", new Grapheme("<s", new String[]{"s"}));
     TGP.put("s", new Grapheme("s", new String[]{"z", "s"}));
     TGP.put("ç", new Grapheme("ç", new String[]{"s"}));
     TGP.put("ss", new Grapheme("ss", new String[]{"s"}));
     TGP.put("sc", new Grapheme("sc", new String[]{"s"}));
     TGP.put("s>", new Grapheme("s>", new String[]{NULL}));
     TGP.put("x>", new Grapheme("x>", new String[]{NULL}));
     TGP.put("t>", new Grapheme("t>", new String[]{NULL}));
     TGP.put("d>", new Grapheme("d>", new String[]{NULL}));
     TGP.put("s>", new Grapheme("s>", new String[]{NULL}));
     // Voyelles nasales
     TGP.put("an", new Grapheme("an", new String[]{"@", "an"}));
     TGP.put("am", new Grapheme("am", new String[]{"@", "am"}));
     TGP.put("aon", new Grapheme("aon", new String[]{"@"}));
     TGP.put("on", new Grapheme("on", new String[]{"§", "On", "on"}));
     TGP.put("om", new Grapheme("om", new String[]{"§", "Om", "om"}));
     TGP.put("ain", new Grapheme("ain", new String[]{"5"}));
     TGP.put("aim", new Grapheme("aim", new String[]{"5"}));
     TGP.put("in", new Grapheme("in", new String[]{"5", "in"}));
     TGP.put("im", new Grapheme("im", new String[]{"5", "im"}));
     // TGP.put("en", new Grapheme("en", new String[]{"5", "@", "En"})); // suivi par une consonne
     TGP.put("en", new Grapheme("en", new String[]{"2n","5", "@"}));
     // TGP.put("en" + CONSONNE_R, new Grapheme("en" + CONSONNE_R, new String[]{"@" + CONSONNE_R}));
     // TGP.put(VOYELLE_L + "en", new Grapheme(VOYELLE_L + "en", new String[]{VOYELLE_L + "5"}));
     // TGP.put("em", new Grapheme("em", new String[]{"5", "@", "Em"}));
     TGP.put("em", new Grapheme("em", new String[]{"5", "@"}));
     TGP.put("un", new Grapheme("un", new String[]{"1"}));
     TGP.put("<un", new Grapheme("<un", new String[]{"y"}));
     TGP.put("um", new Grapheme("um", new String[]{"1", "Om"}));
     TGP.put("ch", new Grapheme("ch", new String[]{"S", "k"}));
     TGP.put("dh", new Grapheme("dh", new String[]{"d"}));
     TGP.put("oi", new Grapheme("oi", new String[]{"wa"}));
     TGP.put("oî", new Grapheme("oî", new String[]{"wa"}));
     TGP.put("oy", new Grapheme("oy", new String[]{"wa"}));
     TGP.put("oin", new Grapheme("oin", new String[]{"w5"}));
     TGP.put("w", new Grapheme("w", new String[]{"w"}));
     TGP.put("oe", new Grapheme("oe", new String[]{"wa"}));
     TGP.put("oê", new Grapheme("oê", new String[]{"wa"}));
     } */
    private void demarreTLL() {

        TLL.put("ii", "i");
        TLL.put("ei", "E;e");
        TLL.put("ai", "E;e");
        TLL.put("aa", "a");
        TLL.put("oo", "o;O");
        TLL.put("au", "o");
        TLL.put("ou", "u;w");
        TLL.put("uu", "y;8");
        TLL.put("EU", "2");
        TLL.put("oe", "2;9");
        TLL.put("ee", "°");
        TLL.put("eu", "2;°;9");
        TLL.put("in", "5");
        TLL.put("an", "@");
        TLL.put("on", "§");
        TLL.put("un", "1");
        TLL.put("yy", "j");
        TLL.put("ww", "w");
        TLL.put("pp", "p");
        TLL.put("tt", "t");
        TLL.put("kk", "k");
        TLL.put("bb", "b");
        TLL.put("dd", "d");
        TLL.put("gg", "g");
        TLL.put("ff", "f");
        TLL.put("ss", "s;z");
        TLL.put("ch", "S");
        TLL.put("vv", "v");
        TLL.put("zz", "z");
        TLL.put("jj", "Z");
        TLL.put("ll", "l");
        TLL.put("rr", "R");
        TLL.put("mm", "m");
        TLL.put("nn", "n");
        TLL.put("uy", "8");
        TLL.put("nngg", "G");
        TLL.put("nnyy", "N;nj");
        //TLL.put("vvou", "vw");  // Problema nao eh minimal
        //TLL.put("uuoo", "8O;8o");
        TLL.put("##", "");

    }

    public LinkedList<List<String>> parseurFormatLiaToLexique(String phonLIA) {

        LinkedList<List<String>> result = new LinkedList<>();
        ArrayList<String> listePhons;
        String[] phonsAux;
        String phon;
        listePhons = new ArrayList<>();
        listePhons.add("<");
        result.add(listePhons);
        for (int i = 0; i < phonLIA.length() - 1; i++) {
            listePhons = new ArrayList<>();
            // Il essaie d'abord de trouver une correspondance avec les sous phonemes de taille 4
            if (i + 3 < phonLIA.length() && TLL.containsKey(phonLIA.substring(i, i + 4))) {
                phon = phonLIA.substring(i, i + 4);
                // S'il n'y a pas de caracteres séparateurs
                if (!TLL.get(phon).contains(";")) {
                    listePhons.add(TLL.get(phon));
                } else {
                    phonsAux = TLL.get(phon).split(";");
                    for (String p : phonsAux) {
                        listePhons.add(p);
                    }
                }
                i += 3;
                // S'il ne trouve pas il essaie de trouver une avec les sous phonemes de taille 2
            } else {
                phon = phonLIA.substring(i, i + 2);
                if (TLL.containsKey(phon)) {
                    // S'il n'y a pas de caracteres séparateurs
                    if (!TLL.get(phon).contains(";")) {
                        listePhons.add(TLL.get(phon));
                    } else {
                        phonsAux = TLL.get(phon).split(";");
                        for (String p : phonsAux) {
                            listePhons.add(p);
                        }
                    }
                    i++;
                }
            }
            result.add(listePhons);
        }
        listePhons = new ArrayList<>();
        listePhons.add(">");
        result.add(listePhons);
        return result;
    }

    public LinkedList<List<String>> parseurFormatLiaToListe(String phonLIA) {

        LinkedList<List<String>> result = new LinkedList<>();
        phonLIA = "<" + phonLIA + ">";
        List<String> listeAux;
        for (String phon : separePhonemes(phonLIA, true)) {
            listeAux = new ArrayList<>();
            listeAux.add(phon);
            result.add(listeAux);
        }
        return result;
    }

    public static boolean contientPhonemesLiaLexique(String mot, String phonsLexique, LinkedList<List<String>> phonLIA) {
        String unitePhon;
        int debutIndice = 0;
        boolean uniteTrouve;
        for (int i = 0; i < phonsLexique.length(); i++) {
            unitePhon = phonsLexique.substring(i, i + 1);
            uniteTrouve = false;
            for (int j = debutIndice; j < phonLIA.size(); j++) {
                for (int k = 0; k < phonLIA.get(j).size(); k++) {
                    if (phonLIA.get(j).get(k).contains(unitePhon)) {
                        uniteTrouve = true;
                        break;
                    }
                }
            }
            if (!uniteTrouve) {
                System.out.println("Non trouve: " + unitePhon + " ; Mot: " + mot + "; PhonLexique: " + phonsLexique + "; PhonsLia: " + phonemeLiaToString(phonLIA));
                if (!fauxPhonemes.containsKey(unitePhon)) {
                    fauxPhonemes.put(unitePhon, 1);
                } else {
                    fauxPhonemes.put(unitePhon, fauxPhonemes.get(unitePhon) + 1);
                }
                return false;
            }
        }
        return true;
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

    // Apres le demarrage du TGP, il cree une liste avec tous les phonemes utilisees
    public static void demarreTableauPhonemes() {
        for (String grapheme : TGP.keySet()) {
            for (String phon : TGP.get(grapheme).getListePhonemes()) {
                if (!tableauPhonemes.contains(phon)) {
                    tableauPhonemes.add(phon);
                }
            }
        }
    }

    // Separe en phonemes plus petits
    public static List<String> separePhonemes(String phon, boolean phonLia) {
        String seq = "";
        int offset;
        String[] seqMod;
        ArrayList<String> result = new ArrayList<>();
        if (phonLia) {
            seq = LetterInformation.CARAC_DEBUT;
            result.add(seq);
            phon = phon.substring(1, phon.length() - 1);
            for (int pos = 0; pos < phon.length(); pos = pos + 2) {
                result.add(phon.substring(pos, pos + 2));
            }
            seq = LetterInformation.CARAC_FIN;
            result.add(seq);
        } else {
            for (int pos = 0; pos < phon.length(); pos = offset + 1) {
                if (phon.length() - pos > 2) {
                    offset = pos + 2;
                } else {
                    offset = phon.length() - 1;
                }
                while (offset >= pos) {
                    seq = phon.substring(pos, offset + 1);
                    if (tableauPhonemes.contains(seq) || seq.length() == 1) {
                        break;
                    }
                    offset--;
                }
                result.add(seq);
            }
        }
        return result;
    }

    public static List<String> separePhonemes(LinkedList<List<String>> unite) {
        List<String> result = new ArrayList<>();
        for (List<String> lstUnite : unite) {
            result.add(lstUnite.get(0));
        }
        return result;
    }

    public LinkedHashMap<String, Grapheme> elargirReglesTGP(String expression, String[] lstPhonemes, String listeLettresA, String listeLettresB) {
        ArrayList<String> lstPhonsFinal;
        LinkedHashMap<String, Grapheme> result = new LinkedHashMap<>();
        String[] lstPhonemesAux;
        String premierSubs = "", deuxSubs = "", formatGrapheme, lettreA, lettreB;

        if (expression.contains(VOYELLE_L)) {
            premierSubs = VOYELLE_L;
        }
        if (expression.contains(CONSONNE_L)) {
            premierSubs = CONSONNE_L;
        }
        if (expression.contains(VOYELLE_R)) {
            deuxSubs = VOYELLE_R;
        }
        if (expression.contains(CONSONNE_R)) {
            deuxSubs = CONSONNE_R;
        }
        for (int i = 0; i < listeLettresA.length(); i++) {
            for (int j = 0; j < listeLettresB.length(); j++) {
                lstPhonsFinal = new ArrayList<>();
                lettreA = listeLettresA.substring(i, i + 1);
                lettreB = listeLettresB.substring(j, j + 1);
                lstPhonemesAux = lstPhonemes.clone();
                formatGrapheme = expression.replaceFirst(premierSubs, lettreA);
                formatGrapheme = formatGrapheme.replaceFirst(deuxSubs, lettreB);
                for (int k = 0; k < lstPhonemesAux.length; k++) {
                    lstPhonsFinal.addAll(remplaceVoyelleConsoPhon(formatGrapheme, lstPhonemesAux[k]));
                }
                result.put(formatGrapheme, new Grapheme(formatGrapheme, lstPhonsFinal));
            }
        }

        return result;
    }

    public LinkedHashMap<String, Grapheme> elargirReglesTGP(String expression, String[] lstPhonemes, String listeLettres) {
        ArrayList<String> lstPhonsFinal;
        LinkedHashMap<String, Grapheme> result = new LinkedHashMap<>();
        String premierSubs = "", formatGrapheme, lettre;
        String[] lstPhonemesAux;
        if (expression.contains(VOYELLE_L)) {
            premierSubs = VOYELLE_L;
        }
        if (expression.contains(CONSONNE_L)) {
            premierSubs = CONSONNE_L;
        }
        if (expression.contains(VOYELLE_R)) {
            premierSubs = VOYELLE_R;
        }
        if (expression.contains(CONSONNE_R)) {
            premierSubs = CONSONNE_R;
        }
        for (int i = 0; i < listeLettres.length(); i++) {
            lstPhonsFinal = new ArrayList<>();
            lettre = listeLettres.substring(i, i + 1);
            formatGrapheme = expression.replaceFirst(premierSubs, lettre);
            lstPhonemesAux = lstPhonemes.clone();
            for (int j = 0; j < lstPhonemesAux.length; j++) {
                lstPhonsFinal.addAll(remplaceVoyelleConsoPhon(formatGrapheme, lstPhonemesAux[j]));
            }
            result.put(formatGrapheme, new Grapheme(formatGrapheme, lstPhonsFinal));
        }
        return result;
    }

    public static List<String> phonemesLIAToList(String phon) {
        List<String> result = new ArrayList<>();
        for (int i = 0; i < phon.length(); i++) {
            if (!phon.substring(i, i + 1).equals(CARAC_EFFAC) && !phon.substring(i, i + 1).equals(CARAC_DEBUT) && !phon.substring(i, i + 1).equals(CARAC_FIN)) {
                result.add(phon.substring(i, i + 2));
                i++;
            } else {
                result.add(phon.substring(i, i + 1));
            }

        }
        return result;
    }

    // Calcule le produit cartesian de deux listes des chaines de caracteres
    public static Set<List<String>> cartesianStringProduct(ArrayList<String> first, ArrayList<String> second) {
        Set<String> premListe = new HashSet<>(first);
        Set<String> deuxListe = new HashSet<>(second);
        Set<List<String>> cartesianProduct = Sets.cartesianProduct(premListe, deuxListe);
        return cartesianProduct;
    }

}
