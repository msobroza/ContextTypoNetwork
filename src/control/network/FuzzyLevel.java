package control.network;

import model.Fanal;
import model.MacroFanal;
import graph.FuzzyGraph;
import control.rules.LetterInformation;
import control.ContextTypoNetwork;
import static control.rules.LetterInformation.NB_CAS;

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
        int n = r.FANALS_PER_CLUSTER * r.NUMBER_CLUSTERS; //Nombre de sommets
        this.graph = new FuzzyGraph(n);
        this.subGraph = new FuzzyGraph(n);
        this.mapCliquesStr = new HashMap<>();
        this.mapCTournois = new HashMap<>();
        this.mapCliqueFlousStr = new HashMap<>();
        this.h = h;
        this.r = r;

    }

    private FuzzyLevel(FuzzyLevel n, int h) { //Pour reprendre d'un autre niveau deja existant
        this.h = h;
        this.graph = n.graph.copyGraph(h);
        this.subGraph = n.graph.copyGraph(h);
        this.mapCliquesStr = new HashMap<>();
        this.mapCliqueFlousStr = new HashMap<>();
        this.mapCTournois = new HashMap<>();
        this.r = n.r;
    }

    @Override
    public Level copy(int h) {
        return new FuzzyLevel(this, h);
    }

    public static Fanal pickRandomFanal(LinkedList<Fanal> lst) {
        Random r = new Random();
        int num = r.nextInt(lst.size());
        return lst.get(num);
    }

    public static List<Fanal> pickRandomFanals(LinkedList<Fanal> lst, int n) {
        List<Fanal> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    public static List<Integer> pickRandomNumbers(List<Integer> lst, int n) {
        List<Integer> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, n);
    }

    public void addCircularAnticipation(String info, boolean phoneme) {
        // Creation de la liste de fanaux composant la chaine correspondant au mot appris
        if (FuzzyNetwork.FLOU2FLOU) {
            LinkedList<MacroFanal> fanalsList = new CircularLinkedList<>();
            if (!phoneme) {
                for (int i = 0; i < info.length(); i++) {
                    String letter = info.substring(i, i + 1);
                    // Note : si on utilise des macrofanaux, la méthode getFanal de la classe Cluster renvoit un macrofanal
                    fanalsList.add(((FuzzyGraph) this.graph).getCluster(i).getMacroFanal(letter));
                }
            } else {
                int iCluster = 0;
                ContextTypoNetwork.logger.debug("Phoneme a apprendre: " + info + " Longueur: " + info.length());
                for (int i = 0; i < info.length(); i++) {

                    if (info.substring(i, i + 1).equals(BEGIN_WORD_CHAR) || info.substring(i, i + 1).equals(END_WORD_CHAR)) {
                        String lettre = info.substring(i, i + 1);
                        // Note : si on utilise des macrofanaux, la méthode getFanal de la classe Cluster renvoit un macrofanal
                        fanalsList.add(((FuzzyGraph) this.graph).getCluster(iCluster).getMacroFanal(lettre));
                    } else {
                        String phon = info.substring(i, i + 2);
                        fanalsList.add(((FuzzyGraph) this.graph).getCluster(iCluster).getMacroFanal(phon));
                        i++;
                    }
                    iCluster++;

                }
            }

            ContextTypoNetwork.logger.debug("- " + info + " -");
            // Definition de la clique
            Clique c = new Clique(info);
            for (MacroFanal f : fanalsList) {
                // On ajoute le fanal a la liste des fanaux composant la clique
                ContextTypoNetwork.logger.debug("Infon: " + f.getLetter() + " Fanal: " + f.getFanalName());
                c.addFanal(f);
            }

            // On crée les connexions entre fanaux de macrofanaux différents
            LinkedList<Fanal> sourceFanals;
            LinkedList<Fanal> destinationFanals;

            // On cree les liaisons orientees du graphe pour creer la chaine de tournoi
            LinkedList<Fanal> activatedFanals;
            LinkedList<LinkedList<Fanal>> linkedFanals = new CircularLinkedList<>();

            // Liste de fanaux pour la clique de fanaux flous
            LinkedList<Fanal> fuzzyListFanals = new LinkedList<>();
            // Pour chaque macrofanal, on sélectionne les fanaux activés par la lettre apprise
            for (int i = 0; i < fanalsList.size(); i++) {
                // Contiendra la liste des fanaux actifs dans le macrofanal courant
                activatedFanals = new LinkedList<>();

                // Les fanaux à activer diffère selon la méthode par contexte ou par mitose
                if (FuzzyNetwork.USE_MITOSIS) {
                    // En cas de mitose, on sélectionne le dernier fanal crée dans chaque macrofanal 
                    activatedFanals.add(fanalsList.get(i).getListFanaux().getLast());
                    // Cette operation ne marche que pour le cas de mitose (car il n'y a que un fanal actif par sac)
                    fuzzyListFanals.add(fanalsList.get(i).getListFanaux().getLast());
                } else {
                    // Initialisation d'une liste contenant les Id des fanaux d'origine contenus dans le macrofanal
                    LinkedList<Integer> idFanals = FuzzyLevel.infoContext(info, i);
                    // On récupère les fanaux correspondant aux Id

                    for (Integer idFanal : idFanals) {
                        activatedFanals.add(fanalsList.get(i).getListFanaux().get(idFanal));
                    }
                }
                linkedFanals.add(i, activatedFanals);
            }
            // On détermine les fanaux de départ et d'arrivée, puis on les lie
            for (int i = 0; i < fanalsList.size(); i++) {
                sourceFanals = linkedFanals.get(i);
                destinationFanals = new LinkedList<>();
                for (int j = 1; j <= r.R_CIRCULAR; j++) {
                    destinationFanals.addAll(linkedFanals.get(i + j));

                }
                createTournamentChain(sourceFanals, destinationFanals, this.graph);
            }
            Clique cliqueFlous = new Clique(info);
            for (Fanal f : fuzzyListFanals) {
                cliqueFlous.addFanal(f);
            }
            ContextTypoNetwork.logger.debug("Clique guardado: " + info);
            this.mapCliqueFlousStr.put(info, cliqueFlous);
            this.mapCliquesStr.put(info, c);
        } else {
            LinkedList<Fanal> fanalsList = new CircularLinkedList<>();
            for (int i = 0; i < info.length(); i++) {
                String lettre = info.substring(i, i + 1);
                // Note : si on utilise des macrofanaux, la méthode getFanal de la classe Cluster renvoit un macrofanal
                fanalsList.add(((FuzzyGraph) this.graph).getCluster(i).getFanal(lettre));
            }
            ContextTypoNetwork.logger.debug("- " + info + " -");
            // Definition de la clique
            Clique c = new Clique(info);
            for (Fanal f : fanalsList) {
                // On ajoute le fanal a la liste des fanaux composant la clique
                c.addFanal(f);
            }

            // On crée les connexions entre fanaux de macrofanaux différents
            LinkedList<Fanal> fanauxOrig;
            LinkedList<Fanal> fanauxDest;

            // On détermine les fanaux de départ et d'arrivée, puis on les lie
            for (int i = 0; i < fanalsList.size(); i++) {
                fanauxOrig = new LinkedList<>();
                fanauxDest = new LinkedList<>();

                fanauxOrig.add(fanalsList.get(i));
                for (int j = 1; j <= r.R_CIRCULAR; j++) {
                    fanauxDest.add(fanalsList.get(i + j));
                }
                createTournamentChain(fanauxOrig, fanauxDest, this.graph);
            }
            this.mapCliquesStr.put(info, c);
        }

    }

    public Clique getCliqueWordFuzzy(String mot) {
        if (!this.mapCliqueFlousStr.containsKey(mot)) {
            return null;
        }
        return this.mapCliqueFlousStr.get(mot);
    }

    
    public static LinkedList<Integer> infoContext(String mot, int position) {
        LinkedList<Integer> resultat = new LinkedList<>();
        // Cas random
        Random randGen = new Random();
        resultat.add(randGen.nextInt(NB_CAS));

        return resultat;
    }
}
