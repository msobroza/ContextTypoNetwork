package control;

import config.ConfigFile;
import exception.FileNotExists;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import tools.FileIO;

public class ContextTypoNetwork {

    // OPTIONS D'ERREURS
    // Note : les positions d'erreur sont aléatoires
    // Permutation : distance entre les 2 lettres permutées
    // 0  : permutation à distance aléatoire
    // -1 : pas de permutation
    public static int permutation = -1;
    // Cas restrictif de permutation C-V ou C-C
    public static boolean permutation_restriction = false;
    // Il active le mode de substituition de lettres
    public static boolean substitution = false;
    // Il active le mode d'effacement de lettres
    public static boolean effacement = false;
    // Il active le mode de delection de lettres dans un mot
    // Note: 
    // Delections : Nombre de delections du mot
    // 0  : pas de delection
    // 1 : 1 lettre deletée
    // 2 : 2 lettres deletées
    // 3 : 3 lettres deletées ...
    public static int delections = 0;
    // Delection au millieu
    public static boolean delection_lettres_externes = false;
    // Il active le mode de delection de lettres dans un mot
    // Note: 
    // Insertions : Nombre de delections du mot
    // 0  : pas d'insertion
    // 1 : 1 lettre inseree
    // 2 : 2 lettres inseree
    // 3 : 3 lettres inseree ... 
    public static int insertions = 1;
    public static boolean insertion_lettres_internes = false;
    // Nombre de réseaux utilisés dans le teste
    public static int testmax = 5;
    // Nombre de mots appris pour chaque teste
    public static int nbmots = 10000;
    // Nombre d'échantillons pour chaque réseau
    //public static int echantmots = (int)(nbmots*0.05);
    public static int echantmots = 100;
    // Il active le mode d'insertion de mots avec plusiers nombres de lettres
    public static boolean plusieursLettres = false;
    // Nombre de lettres pour chaque mot (seulement active si plusiersLettres=false
    public static int nblettres = 7;
    // Nombre des lettres choisis pour l'insertion
    public static int[] nombre_lettres = {7, 8, 9};
    // L'utilisation de lemmes/mots
    public static boolean lemmes = true;
    // Nombre de phonemes apprises (reseau de taille fixe)
    public static int nbphonemes = 6;
    // Active l'apprentissage directement des phonemes lia
    public static boolean APPRENTISSAGE_PHONS_LIA = true;
    //Interaction terminal
    public static boolean INTERACTION_TERMINAL = false;
    // Interaction smartphone
    public static boolean INTERACTION_SMARTPHONE = false;
    // Interaction testes
    public static boolean INTERACTION_TESTES_UNIQUE = false;
    // Interaction testes plusieurs reseaux
    public static boolean INTERACTION_PLUSIEURS_RESEAUX = false;
    // Interaction fichiers
    public static boolean FILE_INTERACTION = false;
    // Training file
    public static String train_words_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/training/words/train_words_set.pickle.7";
    // Test file
    public static String test_words_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/test/words/test_words_set_ins_1.pickle.7";
    // Interaction texte
    public static boolean INTERACTION_TEXTE_TYPO = true;
    // Taux de matching par reseau
    public static boolean METRIQUES_PAR_RESEAU = true;
    // Active taille variable reseau flou
    public static boolean TAILLE_VARIABLE_RESEAU_FLOU_DROITE = INTERACTION_TEXTE_TYPO;
    // Active taille variable reseau flou
    public static boolean TAILLE_VARIABLE_RESEAU_FLOU_GAUCHE = false;

    public static Logger logger = Logger.getRootLogger();

    public static String texteBonP = "Selon une étude de l'Université de Cambridge, l'ordre des lettres dans un mot n'a pas d'importance, la seule chose importante est que la première et la dernière soit à la bonne place. Le reste peut être dans un désordre total et vous pouvez toujours lire sans problème. C'est parce que le cerveau humain ne lit pas chaque lettres elle-même, mais le mot comme un tout";
    public static String texteTypo = "Sleon une édtue de l'Uvinertisé de Cmabrigde, l'odrre des ltteers dnas un mto n'a pas d'ipmrotncae, la suele coshe ipmrotnate est que la pmeirère et la drenèire soit à la bnnoe pclae. Le rsete peut êrte dnas un dsérorde ttoal et vuos puoevz tujoruos lrie snas porlblème. C'est prace que le creaveu hmauin ne lit pas chuaqe ltetres elle-mmêe, mias le mot cmome un tuot";

    public static void main(String[] args) throws FileNotExists {

        BasicConfigurator.configure();
        logger.setLevel(Level.ALL);
        NetworkControl cr = new NetworkControl();
        // Apprentissage et decodage des mots
        if (INTERACTION_TEXTE_TYPO) {
            cr.phaseApprentissage(texteBonP.toLowerCase(), true);
        } else {
            if (FILE_INTERACTION) {
                if (!FileIO.fileExists(train_words_file)) {
                    throw new FileNotExists(train_words_file);
                }
                HashMap<Integer, List<String>> trainInput = new HashMap<>(FileIO.readSplittedFile(train_words_file));
                cr.phaseApprentissage(trainInput.get(ConfigFile.TrainWords.WORDS.getIndex()), trainInput.get(ConfigFile.TrainWords.PHONS.getIndex()));
            } else {
                cr.phaseApprentissage("", false);
            }
        }
        ContextTypoNetwork.logger.debug("Apprentissage OK! ");
        if (ContextTypoNetwork.INTERACTION_TERMINAL) {
            Scanner sc = new Scanner(System.in);
            while (true) {
                System.out.println("Veuillez saisir un mot :");
                String str = sc.nextLine();
                System.out.println("Vous avez saisi : " + str);
                System.out.println(cr.decoderMotDemonstrateur(str));
            }
        }
        if (ContextTypoNetwork.INTERACTION_SMARTPHONE) {
            ControlTCPServer ctcp = new ControlTCPServer(cr);
            ctcp.demarreServeurTCP();
        }
        if (ContextTypoNetwork.INTERACTION_TESTES_UNIQUE || ContextTypoNetwork.INTERACTION_TEXTE_TYPO) {
            if (ContextTypoNetwork.INTERACTION_TEXTE_TYPO) {
                cr.phaseDecodage(texteBonP.toLowerCase(), texteTypo.toLowerCase(), true);
            } else {
                if (FILE_INTERACTION) {
                    if (!FileIO.fileExists(test_words_file)) {
                        throw new FileNotExists(test_words_file);
                    }
                    HashMap<Integer, List<String>> testInput = new HashMap<>(FileIO.readSplittedFile(test_words_file));
                    cr.phaseDecodage(testInput.get(ConfigFile.TestWords.WORDS.getIndex()), testInput.get(ConfigFile.TestWords.ERRORS.getIndex()), testInput.get(ConfigFile.TestWords.ERRORS_PHONS.getIndex()));
                } else {
                    cr.phaseDecodage("", "", false);
                }
            }
            if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
                System.out.println("Taux matching Reseau Triang: " + cr.getTauxMatching(NetworkControl.INDICE_RESEAU_TRIANG));
                System.out.println("Taux d'erreur Reseau Triang: " + cr.getTauxErreur(NetworkControl.INDICE_RESEAU_TRIANG));
                System.out.println("Taux matching Reseau Flous: " + cr.getTauxMatching(NetworkControl.INDICE_RESEAU_FLOUS_DROITE));
                System.out.println("Taux d'erreur Reseau Flous: " + cr.getTauxErreur(NetworkControl.INDICE_RESEAU_FLOUS_DROITE));
            }

            System.out.println("Taux matching: " + cr.getTauxMatching());
            System.out.println("Taux erreur: " + cr.getTauxErreur());

        }

        //System.out.println(cr.decoderMotDemonstrateur(ControleReseaux.modifieMot(cr.getMotAppris(10))));
        //System.out.println(cr.decoderMotModifie("maison"));
        // Apprentissage et decodage des testes aleatoires
        /*
         boolean utilisationTemplate = false;
         boolean utilisationFichierSortie = false;
         String cheminTemplate = "";
         String fichierSortie = "";

         for (int i = 0; i < (args.length - 1); i++) {
         if (args[i].equals("-templateTypo")) {
         utilisationTemplate = true;
         cheminTemplate = args[i + 1];
         ModificationParametresDynamique.appliquerTemplate(cheminTemplate, ReseauTriang.class, TypoMultireseaux.class);
         }
         if (args[i].equals("-fichierSortie")) {
         utilisationFichierSortie = true;
         fichierSortie = args[i + 1];
         }
         if (args[i].equals("-nMots")) {
         ModificationParametresDynamique.modifierParametre(TypoMultireseaux.class, Integer.parseInt(args[i + 1]), "nbmots");
         echantmots = (int) (nbmots * 0.2);
         }
         if (args[i].equals("-plusieursLettres")) {
         if (Integer.parseInt(args[i + 1]) == 0) {
         ModificationParametresDynamique.modifierParametre(TypoMultireseaux.class, false, "plusieursLettres");
         } else {
         ModificationParametresDynamique.modifierParametre(TypoMultireseaux.class, true, "plusieursLettres");
         }
         }
         if (args[i].equals("-rangeLettres")) {
         int limInf = Integer.parseInt((args[i + 1].split(":"))[0]);
         int limSup = Integer.parseInt((args[i + 1].split(":"))[1]);
         int[] range = new int[limSup - limInf + 1];
         for (int j = limInf; j <= limSup; j++) {
         range[j - limInf] = j;
         }
         ModificationParametresDynamique.modifierParametre(TypoMultireseaux.class, range, "nombre_lettres");
         }
         if (args[i].equals("-ordreClique")) {
         ReseauTriang.NOMBRE_FANAUX_PAR_CLIQUE = Integer.parseInt(args[i + 1]);
         ReseauTriang.NOMBRE_FANAUX_PAR_CLIQUE_H0 = 2 * ReseauTriang.NOMBRE_FANAUX_PAR_CLIQUE;
         }

         }

         double moy = 0.0;
         double match = 0.0;
         int erreur = 0;
         double doubleLettres = 0.0;
         double doubleLettresNonCons = 0.0;
         double densiteMaxHorizontale = 0.0;
         double densiteMoyHorizontale = 0.0;
         double densiteMinHorizontale = Double.POSITIVE_INFINITY;
         double densiteMaxVerticale = 0.0;
         double densiteMoyVerticale = 0.0;
         double densiteMinVerticale = Double.POSITIVE_INFINITY;
         double densiteMaxHyper = 0.0;
         double densiteMoyHyper = 0.0;
         double densiteMinHyper = Double.POSITIVE_INFINITY;

         String motRecherche;
         String motMod = "";
         ReseauTriang r = null;

         for (int test = 0; test < testmax; test++) {
         r = new ReseauTriang();
         if (substitution) {
         r.apprendreMot("_", 1, true, true);
         }
         if (plusieursLettres) {
         for (int i = 0; i < nombre_lettres.length; i++) {
         r.apprendreDictionnaire(nbmots, nombre_lettres[i]);
         }
         } else {
         r.apprendreDictionnaire(nbmots, nblettres);
         }
         ArrayList<String> motsTest = new ArrayList<>();
         for (int i = 0; i < echantmots; i++) {
         TypoMultireseaux.logger.debug("===============================");
         // On prend au hasard un des mots appris par le reseau
         do {
         motRecherche = r.getMotApp();
         } while (motsTest.contains(motRecherche));
         motsTest.add(motRecherche);

         if (permutation < 0 && !substitution && !effacement) {
         motMod = motRecherche;
         } else {
         if (permutation >= 0) {
         motMod = TypoMultireseaux.permuterLettres(motRecherche, permutation);
         //motMod=TypoPhonTriang.inverseLettres(motRecherche,pos1,pos2);
         }
         if (substitution) {
         motMod = TypoMultireseaux.substituerLettre(motRecherche);
         //motMod=TypoPhonTriang.remplaceLettre(motRecherche, 3, '_');
         }
         if (effacement) {
         motMod = TypoMultireseaux.effacerLettre(motRecherche, "_");
         }

         }
         TypoMultireseaux.logger.debug("<MOT RECHERCHE: " + motRecherche);
         match = r.reconnaitreMot(motMod, motRecherche);
         TypoMultireseaux.logger.debug(">MOT RECHERCHE: " + motRecherche);
         TypoMultireseaux.logger.debug("Vrai score: " + match);
         if (ReseauTriang.TOP_DOWN) {
         if (match < 1.0) {
         erreur++;
         }
         } else {
         if (ReseauTriang.PHONEMES) {
         if (match < 1.0 || r.getFanauxPhoneme(motMod).size() != ReseauTriang.NOMBRE_FANAUX_PAR_CLIQUE) {
         System.out.println("Erro: " + motMod);
         erreur++;
         }
         } else {
         if (match < 1.0 || r.getFanauxMot(motMod).size() != ReseauTriang.NOMBRE_FANAUX_PAR_CLIQUE) {
         erreur++;
         }
         }

         }
         moy += match;
         }
         doubleLettres += r.getTauxDoubleLettre();
         doubleLettresNonCons += r.getTauxDoubleLettreNon();
         }
         TypoMultireseaux.logger.warn("!=====================================!");
         TypoMultireseaux.logger.warn("LETTRES INVERSÉES: " + permutation);
         if (permutation != -1) {
         TypoMultireseaux.logger.warn("Permutation activée");
         } else {
         TypoMultireseaux.logger.warn("");
         }
         TypoMultireseaux.logger.warn("CARACTÈRES DÉLIMITEURS: " + ReseauTriang.CARAC_DELIM);
         TypoMultireseaux.logger.warn("NOMBRE DE MOTS APPRIS: " + r.getDictionnaire().size());
         if (plusieursLettres) {
         TypoMultireseaux.logger.warn("NOMBRE DE LETTRES PAR MOT: ");
         for (int i = 0; i < nombre_lettres.length; i++) {
         TypoMultireseaux.logger.warn(nombre_lettres[i] + ", ");
         }
         TypoMultireseaux.logger.warn("\n");
         } else {
         TypoMultireseaux.logger.warn("NOMBRE DE LETTRES PAR MOT: " + nblettres);
         }
         TypoMultireseaux.logger.warn("NOMBRE DE CLUSTERS: " + ReseauTriang.NOMBRE_CLUSTERS);
         TypoMultireseaux.logger.warn("NOMBRE DE FANAUX PAR CLIQUE: " + ReseauTriang.NOMBRE_FANAUX_PAR_CLIQUE);
         TypoMultireseaux.logger.warn("NOMBRE DE FANAUX PAR CLUSTER: " + ReseauTriang.NOMBRE_FANAUX_PAR_CLUSTER);
         TypoMultireseaux.logger.warn("NOMBRE D'ÉCHANTILLONS DU TEST: " + testmax * echantmots);
         TypoMultireseaux.logger.warn("NOMBRE DE RÉSEAUX DU TEST: " + testmax);
         TypoMultireseaux.logger.warn("TAUX ERREUR: " + ((double) erreur) / (testmax * echantmots));
         TypoMultireseaux.logger.warn("TAUX MATCHING: " + moy / (testmax * echantmots));
         TypoMultireseaux.logger.warn("TAUX DOUBLES LETTRES: " + doubleLettres / testmax);
         TypoMultireseaux.logger.warn("TAUX DOUBLES LETTRES NON CONSECUTIVES: " + doubleLettresNonCons / testmax);
         int n_cliques;
         for (int i = 0; i < ReseauTriang.hMax; i++) {
         n_cliques = ((NiveauTriang)r.getListeNiveaux().get(i)).getKeyCliques().size();
         if (n_cliques == 0) {
         break;
         }
         TypoMultireseaux.logger.warn("NIVEAU: " + i + " NOMBRE DE CLIQUES: " + n_cliques);
         }
         int compteur = 0;
         for (int i = 0; i < ReseauTriang.hMax; i++) {
         NiveauTriang n = (NiveauTriang)r.getListeNiveaux().get(i);
         Double densite = n.getDensiteHorizontale();
         if (densite == 0.0) {
         break;
         }
         densiteMoyHorizontale += densite;
         if (densite > densiteMaxHorizontale) {
         densiteMaxHorizontale = densite;
         }
         if (densite < densiteMinHorizontale) {
         densiteMinHorizontale = densite;
         }
         compteur++;
         }
         densiteMoyHorizontale = densiteMoyHorizontale / (double) compteur;

         compteur = 0;
         for (int i = 0; i < ReseauTriang.hMax - 1; i++) {
         NiveauTriang n = (NiveauTriang)r.getListeNiveaux().get(i);
         Double densite = n.getDensiteVerticale();
         if (densite == 0.0) {
         break;
         }
         densiteMoyVerticale += densite;
         if (densite > densiteMaxVerticale) {
         densiteMaxVerticale = densite;
         }
         if (densite < densiteMinVerticale) {
         densiteMinVerticale = densite;
         }
         compteur++;
         }
         densiteMoyVerticale = densiteMoyVerticale / (double) compteur;

         compteur = 0;
         for (int i = 1; i < ReseauTriang.hMax; i++) {
         NiveauTriang n = (NiveauTriang)r.getListeNiveaux().get(i);
         Double densite = n.getDensiteInfHyper();
         densiteMoyHyper += densite;
         if (densite > densiteMaxHyper) {
         densiteMaxHyper = densite;
         }
         if (densite < densiteMinHyper) {
         if (densite != 0.0) {
         densiteMinHyper = densite;
         }
         }
         if (densite != 0.0) {
         compteur++;
         }
         }
         densiteMoyHyper = densiteMoyHyper / (double) compteur;

         // Fichier de sortie
         if (utilisationFichierSortie) {
         String out[] = new String[15];

         if (!WriterCSV.fileExists(fichierSortie)) {
         out[0] = "nombre_mots_appris";
         out[1] = "taux_matching";
         out[2] = "taux_erreur";
         out[3] = "taux_double_lettres_consecutives";
         out[4] = "taux_double_lettres_non_consecutives";
         out[5] = "densite_max_horizontale";
         out[6] = "densite_moy_horizontale";
         out[7] = "densite_min_horizontale";
         out[8] = "densite_max_verticale";
         out[9] = "densite_moy_verticale";
         out[10] = "densite_min_verticale";
         out[11] = "densite_max_hyper_inf";
         out[12] = "densite_moy_hyper_inf";
         out[13] = "densite_min_hyper_inf";
         out[14] = "ordre_cliques";
         WriterCSV.createCsvFile(fichierSortie, out);
         }

         //Nombre des mots appris
         out[0] = r.getDictionnaire().size() + "";
         //Taux de matching
         out[1] = (moy / (testmax * echantmots)) + "";
         //Taux d'erreur
         out[2] = (((double) erreur) / (testmax * echantmots)) + "";
         // Taux de double lettres consecutives
         out[3] = (doubleLettres / testmax) + "";
         // Taux de double lettres non consecutives
         out[4] = (doubleLettresNonCons / testmax) + "";
         // Densité max horizontale
         out[5] = densiteMaxHorizontale + "";
         // Densité moy horizontale
         out[6] = densiteMoyHorizontale + "";
         // Densité min horizontale
         out[7] = densiteMinHorizontale + "";
         // Densité max verticale
         out[8] = densiteMaxVerticale + "";
         // Densité moy verticale
         out[9] = densiteMoyVerticale + "";
         // Densité min verticale
         out[10] = densiteMinVerticale + "";
         // Densité max inf hyper
         out[11] = densiteMaxHyper + "";
         // Densité moy inf hyper
         out[12] = densiteMoyHyper + "";
         // Densité min inf hyper
         out[13] = densiteMinHyper + "";
         // Ordre des cliques
         out[14] = ReseauTriang.NOMBRE_FANAUX_PAR_CLIQUE + "";
         // Creer le fichier de sortie
         WriterCSV.createCsvFile(fichierSortie, out);
         }*/
        // DebugGUI c= new DebugGUI(r);
    }

    public static <K extends Comparable, V extends Comparable> Map<K, V> sortByValues(Map<K, V> map) {
        List<Map.Entry<K, V>> entries = new LinkedList<>(map.entrySet());

        Collections.sort(entries, (Map.Entry<K, V> o1, Map.Entry<K, V> o2) -> o1.getValue().compareTo(o2.getValue()));

        //LinkedHashMap will keep the keys in the order they are inserted
        //which is currently sorted on natural ordering
        Map<K, V> sortedMap = new LinkedHashMap<>();

        for (Map.Entry<K, V> entry : entries) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }

        return sortedMap;
    }
}
