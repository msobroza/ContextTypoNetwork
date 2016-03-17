package control.network;

import model.Fanal;
import model.MacroFanal;
import graph.FuzzyGraph;
import control.rules.LetterInformation;
import control.ContextTypoNetwork;
import static control.rules.LetterInformation.NB_CAS;
import static control.rules.LetterInformation.VOYELLES;
import static control.rules.LetterInformation.VOYELLES_A;
import static control.rules.LetterInformation.VOYELLES_E;
import static control.rules.LetterInformation.VOYELLES_I;
import static control.rules.LetterInformation.VOYELLES_O;
import static control.rules.LetterInformation.VOYELLES_U;
import static control.rules.LetterInformation.typeQuestion;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import tools.CircularLinkedList;
import model.Clique;

public class FuzzyLevel extends Level implements LetterInformation {

    private final FuzzyNetwork r;
    private final HashMap<String, Clique> mapCliqueFlousStr;

    public FuzzyLevel(int h, FuzzyNetwork r) {
        int n = r.NOMBRE_FANAUX_PAR_CLUSTER * r.NOMBRE_CLUSTERS; //Nombre de sommets
        this.graphe = new FuzzyGraph(n);
        this.sousGraphe = new FuzzyGraph(n);
        this.mapCliquesStr = new HashMap<>();
        this.mapCTournois = new HashMap<>();
        this.mapCliqueFlousStr = new HashMap<>();
        this.h = h;
        this.r = r;

    }

    private FuzzyLevel(FuzzyLevel n, int h) { //Pour reprendre d'un autre niveau deja existant
        this.h = h;
        this.graphe = n.graphe.copie(h);
        this.sousGraphe = n.graphe.copie(h);
        this.mapCliquesStr = new HashMap<>();
        this.mapCliqueFlousStr = new HashMap<>();
        this.mapCTournois = new HashMap<>();
        this.r = n.r;
    }

    @Override
    public Level copie(int h) {
        return new FuzzyLevel(this, h);
    }

    public static Fanal tirer1FanalAleat(LinkedList<Fanal> lst, int h) {
        Random r = new Random();
        int num = r.nextInt(lst.size());
        return lst.get(num);
    }

    public static List<Fanal> tirerNfanauxAleat(LinkedList<Fanal> lst, int n) {
        List<Fanal> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    public static List<Integer> tirerNnumAleat(List<Integer> lst, int n) {
        List<Integer> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    public void ajouterAnticipCirculaire(String info, boolean phoneme) {
        // Creation de la liste de fanaux composant la chaine correspondant au mot appris
        if (FuzzyNetwork.FLOU2FLOU) {
            LinkedList<MacroFanal> listFanaux = new CircularLinkedList<>();
            if (!phoneme) {
                for (int i = 0; i < info.length(); i++) {
                    String lettre = info.substring(i, i + 1);
                    // Note : si on utilise des macrofanaux, la méthode getFanal de la classe Cluster renvoit un macrofanal
                    listFanaux.add(((FuzzyGraph) this.graphe).getCluster(i).getMacroFanal(lettre));
                }
            } else {
                int iCluster = 0;
                System.out.println("Phoneme a apprendre: "+info+" Longueur: "+info.length());
                for (int i = 0; i < info.length(); i++) {

                    if (info.substring(i,i+1).equals(CARAC_DEBUT) || info.substring(i,i+1).equals(CARAC_FIN)) {
                        String lettre = info.substring(i, i + 1);
                        // Note : si on utilise des macrofanaux, la méthode getFanal de la classe Cluster renvoit un macrofanal
                        listFanaux.add(((FuzzyGraph) this.graphe).getCluster(iCluster).getMacroFanal(lettre));
                    } else {
                        String phon = info.substring(i, i + 2);
                        listFanaux.add(((FuzzyGraph) this.graphe).getCluster(iCluster).getMacroFanal(phon));
                        i++;
                    }
                    iCluster++;

                }
            }

            ContextTypoNetwork.logger.debug("- " + info + " -");
            // Definition de la clique
            Clique c = new Clique(info);
            for (MacroFanal f : listFanaux) {
                // On ajoute le fanal a la liste des fanaux composant la clique
                ContextTypoNetwork.logger.debug("Infon: " + f.getLettre() + " Fanal: " + f.getNom());
                c.ajouterFanal(f);
            }

            // On crée les connexions entre fanaux de macrofanaux différents
            LinkedList<Fanal> fanauxOrig;
            LinkedList<Fanal> fanauxDest;

            // On cree les liaisons orientees du graphe pour creer la chaine de tournoi
            LinkedList<Fanal> fanauxActifs;
            LinkedList<LinkedList<Fanal>> fanauxALier = new CircularLinkedList<>();

            // Liste de fanaux pour la clique de fanaux flous
            LinkedList<Fanal> listeFanauxFlous = new LinkedList<>();
            // Pour chaque macrofanal, on sélectionne les fanaux activés par la lettre apprise
            for (int i = 0; i < listFanaux.size(); i++) {
                // Contiendra la liste des fanaux actifs dans le macrofanal courant
                fanauxActifs = new LinkedList<>();

                // Les fanaux à activer diffère selon la méthode par contexte ou par mitose
                if (FuzzyNetwork.MITOSE_FANAUX) {
                    // En cas de mitose, on sélectionne le dernier fanal crée dans chaque macrofanal 
                    fanauxActifs.add(listFanaux.get(i).getListFanaux().getLast());
                    // Cette operation ne marche que pour le cas de mitose (car il n'y a que un fanal actif par sac)
                    listeFanauxFlous.add(listFanaux.get(i).getListFanaux().getLast());
                } else {
                    // Initialisation d'une liste contenant les Id des fanaux d'origine contenus dans le macrofanal
                    LinkedList<Integer> idFanaux = FuzzyLevel.infoContext(info, i);
                    // On récupère les fanaux correspondant aux Id

                    for (Integer idFanal : idFanaux) {
                        fanauxActifs.add(listFanaux.get(i).getListFanaux().get(idFanal));
                    }
                }
                fanauxALier.add(i, fanauxActifs);
            }
            // On détermine les fanaux de départ et d'arrivée, puis on les lie
            for (int i = 0; i < listFanaux.size(); i++) {
                fanauxOrig = fanauxALier.get(i);
                fanauxDest = new LinkedList<>();
                for (int j = 1; j <= r.RECOUVR_CIRCULAIRE; j++) {
                    fanauxDest.addAll(fanauxALier.get(i + j));

                }
                creerChaineTournois(fanauxOrig, fanauxDest, this.graphe);
            }
            Clique cliqueFlous = new Clique(info);
            for (Fanal f : listeFanauxFlous) {
                cliqueFlous.ajouterFanal(f);
            }
            System.out.println("Clique guardado: "+info);
            this.mapCliqueFlousStr.put(info, cliqueFlous);
            this.mapCliquesStr.put(info, c);
        } else {
            LinkedList<Fanal> listFanaux = new CircularLinkedList<>();
            for (int i = 0; i < info.length(); i++) {
                String lettre = info.substring(i, i + 1);
                // Note : si on utilise des macrofanaux, la méthode getFanal de la classe Cluster renvoit un macrofanal
                listFanaux.add(((FuzzyGraph) this.graphe).getCluster(i).getFanal(lettre));
            }
            ContextTypoNetwork.logger.debug("- " + info + " -");
            // Definition de la clique
            Clique c = new Clique(info);
            for (Fanal f : listFanaux) {
                // On ajoute le fanal a la liste des fanaux composant la clique
                c.ajouterFanal(f);
            }

            // On crée les connexions entre fanaux de macrofanaux différents
            LinkedList<Fanal> fanauxOrig;
            LinkedList<Fanal> fanauxDest;

            // On détermine les fanaux de départ et d'arrivée, puis on les lie
            for (int i = 0; i < listFanaux.size(); i++) {
                fanauxOrig = new LinkedList<>();
                fanauxDest = new LinkedList<>();

                fanauxOrig.add(listFanaux.get(i));
                for (int j = 1; j <= r.RECOUVR_CIRCULAIRE; j++) {
                    fanauxDest.add(listFanaux.get(i + j));
                }
                creerChaineTournois(fanauxOrig, fanauxDest, this.graphe);
            }
            this.mapCliquesStr.put(info, c);
        }

    }

    public Clique getCliqueMotFlous(String mot) {
        if (!this.mapCliqueFlousStr.containsKey(mot)) {
            return null;
        }
        return this.mapCliqueFlousStr.get(mot);
    }

    // Implementation d'information par contexte des lettres
    public static LinkedList<Integer> infoContext(String mot, int position) {
        LinkedList<Integer> resultat = new LinkedList<>();
        if (typeQuestion == LetterInformation.QType.PREV3) {
            if (isPrevConsonne(mot, position)) {
                resultat.add(0);
            }
            if (isPrevDansFamille(mot, position, VOYELLES)) {
                resultat.add(1);
            }
            if (!isPrevLettre(mot, position)) {
                resultat.add(2);
            }
        } else if (typeQuestion == LetterInformation.QType.PREV7) {
            if (isPrevConsonne(mot, position)) {
                resultat.add(0);
            }
            if (isPrevDansFamille(mot, position, VOYELLES_A)) {
                resultat.add(1);
            }
            if (isPrevDansFamille(mot, position, VOYELLES_E)) {
                resultat.add(2);
            }
            if (isPrevDansFamille(mot, position, VOYELLES_I)) {
                resultat.add(3);
            }
            if (isPrevDansFamille(mot, position, VOYELLES_O)) {
                resultat.add(4);
            }
            if (isPrevDansFamille(mot, position, VOYELLES_U)) {
                resultat.add(5);
            }
            if (!isPrevLettre(mot, position)) {
                resultat.add(6);
            }
        } else {
            // Cas random
            Random randGen = new Random();
            resultat.add(randGen.nextInt(NB_CAS));
        }
        return resultat;
    }

    public static boolean isPrevDansFamille(String mot, int position, String famille) {
        if (position == 0) {
            return false;
        } else if (famille.contains(mot.subSequence(position - 1, position))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPrevConsonne(String mot, int position) {
        if (position == 0) {
            return false;
        } else if (LetterInformation.CONSONNES.contains(mot.subSequence(position - 1, position))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNextConsonne(String mot, int position) {
        if (position == mot.length()) {
            return false;
        } else if (LetterInformation.CONSONNES.contains(mot.subSequence(position, position + 1))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPrevVoyelle(String mot, int position) {
        if (position == 0) {
            return false;
        } else if (LetterInformation.VOYELLES.contains(mot.subSequence(position - 1, position))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isNextVoyelle(String mot, int position) {
        if (position == mot.length()) {
            return false;
        } else if (LetterInformation.VOYELLES.contains(mot.subSequence(position, position + 1))) {
            return true;
        } else {
            return false;
        }
    }

    public static boolean isPrevLettre(String mot, int position) {
        if (position == 0) {
            return false;
        } else if (LetterInformation.NON_LETTRES.contains(mot.subSequence(position - 1, position))) {
            return false;
        } else {
            return true;
        }
    }

    public static boolean isNextLettre(String mot, int position) {
        if (position == mot.length()) {
            return false;
        } else if (LetterInformation.NON_LETTRES.contains(mot.subSequence(position, position + 1))) {
            return false;
        } else {
            return true;
        }
    }
}
