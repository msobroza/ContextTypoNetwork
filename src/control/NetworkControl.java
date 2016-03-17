package control;

import control.decoder.FuzzyDecoder;
import control.decoder.TriangularDecoder;
import control.rules.LetterInformation;
import control.rules.PhonemeRules;
import control.network.InterfaceNetwork;
import control.network.Network;
import control.network.FuzzyNetwork;
import control.network.TriangularNetwork;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import model.Fanal;
import model.MacroFanal;
import tools.AutomatedTelnetClient;
import tools.DB;

public class NetworkControl implements LetterInformation {

    public static final int INDICE_RESEAU_TRIANG = 0;
    public static final int INDICE_RESEAU_FLOUS_GAUCHE = 0;
    public static final int INDICE_RESEAU_FLOUS_DROITE = 1;
    public static final int RESEAU_TRIANG = 0;
    public static final int RESEAU_FLOUS = 1;

    // Il cree un reseau interface (triangulaire et flous)
    public static final boolean ACTIVE_INTERFACE_RESEAUX = true;
    // Il cree un reseau cachee des bigrammes ouverts
    public static final boolean ACTIVE_COUCHE_CACHE_BIGRAMMES_OUVERTS_TRIANG = false;
    // Il cree un reseau flous de phonemes a gauche
    public static final boolean ACTIVE_RESEAU_FLOUS_PHONEMES = false;
    // Il active les connexions laterales des macro fanaux
    public static final boolean ACTIVE_LATERALE_MACRO = false;
    // Il active l'apprentissage des connexions laterales
    public static final boolean ACTIVE_CONNECTIONS_LATERALES_TRIANG = true;
    // Insertion du caractere d'effacement au milieu du mot
    public static final boolean ACTIVE_INSERTION_MILIEU = false;

    private final LinkedList<Network> listeReseaux;
    private final LinkedList<String> motsStockes;
    private final LinkedList<String> motsAppris;
    private final HashMap<String, String> motsPhonsMap;
    private final HashMap<String, String> motsPhonsLiaMap;
    private final PhonemeRules reglesPhonemes;
    private double tauxErreur;
    private double tauxMatching;
    private final InterfaceNetwork interfaceReseaux;
    private ArrayList<Double> tauxMatchingParReseau;
    private ArrayList<Double> tauxErreurParReseau;
    private final int NOMBRE_CLUSTERS_RESEAU_FLOUS_DROITE = 15;

    // Traduction automatique de grapheme-phoneme par Telnet
    private AutomatedTelnetClient telnetClient;

    public NetworkControl() {
        listeReseaux = new LinkedList<>();
        motsStockes = new LinkedList<>();
        motsAppris = new LinkedList<>();
        motsPhonsMap = new HashMap<>();
        motsPhonsLiaMap = new HashMap<>();
        tauxErreur = 0.0;
        tauxMatching = 0.0;
        // On instancie une classe avec toutes les regles de conversion grapheme-phoneme
        reglesPhonemes = new PhonemeRules();

        if (ACTIVE_RESEAU_FLOUS_PHONEMES) {
            if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_GAUCHE) {
                // On instancie un reseau du type flous de phonemes
                listeReseaux.add(INDICE_RESEAU_FLOUS_GAUCHE, new FuzzyNetwork(NOMBRE_CLUSTERS_RESEAU_FLOUS_DROITE, InterfaceNetwork.TYPE_INFONS_PHONEMES));
            } else {
                // On instancie un reseau du type flous de phonemes
                listeReseaux.add(INDICE_RESEAU_FLOUS_GAUCHE, new FuzzyNetwork(8, InterfaceNetwork.TYPE_INFONS_PHONEMES));
            }
        } else {
            // On instancie un reseau du type triangulaire (int l, int X, int c, int hMax, NIVEAU_SUPPLEMENTAIRE)
            listeReseaux.add(INDICE_RESEAU_TRIANG, new TriangularNetwork(200, 100, 8, 14, true, ACTIVE_COUCHE_CACHE_BIGRAMMES_OUVERTS_TRIANG, false));
        }
        // On instancie un reseau du type flous de mots
        if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_DROITE) {
            listeReseaux.add(INDICE_RESEAU_FLOUS_DROITE, new FuzzyNetwork(NOMBRE_CLUSTERS_RESEAU_FLOUS_DROITE, InterfaceNetwork.TYPE_INFONS_MOTS));
        } else {
            listeReseaux.add(INDICE_RESEAU_FLOUS_DROITE, new FuzzyNetwork(ContextTypoNetwork.nblettres + 2, InterfaceNetwork.TYPE_INFONS_MOTS));
        }

        if (ACTIVE_INTERFACE_RESEAUX) {
            interfaceReseaux = new InterfaceNetwork(this);
            if (ACTIVE_RESEAU_FLOUS_PHONEMES) {
                interfaceReseaux.ajouterReseau(listeReseaux.get(INDICE_RESEAU_FLOUS_GAUCHE), INDICE_RESEAU_FLOUS_GAUCHE, InterfaceNetwork.TYPE_INFONS_PHONEMES);
            } else {
                interfaceReseaux.ajouterReseau(listeReseaux.get(INDICE_RESEAU_TRIANG), INDICE_RESEAU_TRIANG, InterfaceNetwork.TYPE_INFONS_PHONEMES);
            }
            interfaceReseaux.ajouterReseau(listeReseaux.get(INDICE_RESEAU_FLOUS_DROITE), INDICE_RESEAU_FLOUS_DROITE, InterfaceNetwork.TYPE_INFONS_MOTS);
            if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
                tauxMatchingParReseau = new ArrayList<>();
                tauxErreurParReseau = new ArrayList<>();
            }
        }
    }

    public void demarreSupportLiaTelnet() {
        // Vérifier si c'est mieux de fermer la connexion chaque fois ou de la laisser ouverte
        this.telnetClient = new AutomatedTelnetClient();
    }

    public String decoderMotDemonstrateur(String motMod) {
        LinkedList<String> phonemesProches = new LinkedList<>();
        LinkedList<String> motsProches = new LinkedList<>();
        TriangularNetwork rTriang;
        TriangularDecoder dTriang;
        FuzzyDecoder dFlous;

        // Inicialisation du client Telnet
        demarreSupportLiaTelnet();
        // Cherche le phoneme du mot en utilisant le client Telnet
        String phonLia = this.telnetClient.findPhonemeLia(motMod);
        telnetClient.disconnect();
        LinkedList<List<String>> phonLexique;
        // Il convertit les règles du format Lia pour le format du lexique ou du LIA selon apprentissage
        if (!ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
            phonLexique = reglesPhonemes.parseurFormatLiaToLexique(phonLia);
        } else {
            phonLexique = reglesPhonemes.parseurFormatLiaToListe(phonLia);
        }
        if (!NetworkControl.ACTIVE_INTERFACE_RESEAUX) {
            // Il prend le Reseau Triangulaire
            rTriang = (TriangularNetwork) listeReseaux.get(INDICE_RESEAU_TRIANG);
            // Il prend le decodeur du Reseau Triangulaire
            dTriang = (TriangularDecoder) (rTriang.getDecodeur());
            // Il cherche les phonemes le plus proches du motif
            phonemesProches = dTriang.getMotsPlusProches(dTriang.getWinnersBottomUpHyperDeuxNiveaux(phonLexique), rTriang.hMax - 1);
            // Il prend le decodeur du Reseau Flous
            dFlous = (FuzzyDecoder) ((FuzzyNetwork) listeReseaux.get(INDICE_RESEAU_FLOUS_DROITE)).getDecodeur();
            // Il réalise la propagation et decodage
            dFlous.reconnaitreBottomUp("<" + motMod + ">", 0, false, 0);
            for (LinkedList<Fanal> lst : dFlous.getWinnersBottomUp("<" + motMod + ">")) {
                motsProches.add(dFlous.getMot(lst));
            }
            return regleSelectionMots(phonemesProches, motsProches);
        } else {
            return regleSelectionMots(((TriangularDecoder) ((TriangularNetwork) interfaceReseaux).getDecodeur()).getMotsPlusProches(interfaceReseaux.decoderInterfaceReseaux("<" + motMod + ">", phonLexique), 0));
        }

    }

    public void phaseDecodage(List<String> correctWordList, List<String> errorWordList, List<String> errorPhonList) {

        String mot, motModifie, phon;
        Double match, matchR1aux, matchR2aux, matchR1, matchR2;

        matchR1 = 0.0;
        matchR2 = 0.0;
        int erreur = 0;
        int erreurR1 = 0;
        int erreurR2 = 0;
        LinkedList<List<String>> phonLexique;
        LinkedList<Fanal> listeGagnants;

        int nombreEchant = correctWordList.size();

        for (int nEchant = 0; nEchant < nombreEchant; nEchant++) {

            mot = correctWordList.get(nEchant);
            motModifie = errorWordList.get(nEchant);
            ContextTypoNetwork.logger.debug("Mot entree: " + motModifie);
            // Cherche le phoneme du mot en utilisant le client Telnet
            String phonLia = errorPhonList.get(nEchant);
            ContextTypoNetwork.logger.debug("Phoneme entree: " + phonLia);
            // Il convertit les règles du format Lia pour le format du lexique ou du LIA selon apprentissage
            if (!ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                phonLexique = reglesPhonemes.parseurFormatLiaToLexique(phonLia);
            } else {
                phonLexique = reglesPhonemes.parseurFormatLiaToListe(phonLia);
            }
            ContextTypoNetwork.logger.debug("Phoneme entree: " + phonLexique);
            listeGagnants = interfaceReseaux.decoderInterfaceReseaux("<" + motModifie + ">", phonLexique);
            match = interfaceReseaux.getTauxMatchingInterfaceReseaux(listeGagnants, "<" + mot + ">");
            tauxMatching += match;
            if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
                // Apprentissage des phonemes dans le reseau triangulaire
                if (ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                    phon = motsPhonsLiaMap.get(mot);
                } else {
                    phon = motsPhonsMap.get(mot);
                }
                if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
                    matchR1aux = interfaceReseaux.getTauxMatchingReseau(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE, "<" + phon + ">");
                } else {
                    matchR1aux = interfaceReseaux.getTauxMatchingReseau(NetworkControl.INDICE_RESEAU_TRIANG, "<" + phon + ">");
                }
                matchR2aux = interfaceReseaux.getTauxMatchingReseau(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, "<" + mot + ">");
                matchR1 += matchR1aux;
                matchR2 += matchR2aux;
                if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
                    if (matchR1aux < 1.0 || interfaceReseaux.getListeWinnersReseau(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE).size() != ((FuzzyNetwork) interfaceReseaux.getReseau(INDICE_RESEAU_FLOUS_GAUCHE)).NOMBRE_FANAUX_PAR_CLIQUE) {
                        erreurR1++;
                    }
                } else {
                    if (matchR1aux < 1.0 || interfaceReseaux.getListeWinnersReseau(NetworkControl.INDICE_RESEAU_TRIANG).size() != ((TriangularNetwork) interfaceReseaux.getReseau(INDICE_RESEAU_TRIANG)).NOMBRE_FANAUX_PAR_CLIQUE) {
                        erreurR1++;
                    }
                }

                if (matchR2aux < 1.0 || interfaceReseaux.getListeWinnersReseau(NetworkControl.INDICE_RESEAU_FLOUS_DROITE).size() != ((FuzzyNetwork) interfaceReseaux.getReseau(INDICE_RESEAU_FLOUS_DROITE)).NOMBRE_FANAUX_PAR_CLIQUE) {
                    erreurR2++;
                }
            }

            if (match < 1.0 || listeGagnants.size() != interfaceReseaux.NOMBRE_FANAUX_PAR_CLIQUE) {
                ContextTypoNetwork.logger.debug("Erreur: " + mot + " Match: " + match);
                erreur++;
            }
        }
        if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
            this.tauxMatchingParReseau.add(NetworkControl.INDICE_RESEAU_TRIANG, matchR1 / nombreEchant);
            this.tauxMatchingParReseau.add(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, matchR2 / nombreEchant);
            this.tauxErreurParReseau.add(NetworkControl.INDICE_RESEAU_TRIANG, (double) erreurR1 / nombreEchant);
            this.tauxErreurParReseau.add(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, (double) erreurR2 / nombreEchant);
        }
        telnetClient.disconnect();
        tauxMatching = tauxMatching / nombreEchant;
        tauxErreur = ((double) erreur) / nombreEchant;
    }

    public void phaseDecodage(String texteBonneOrdre, String texteTypo, boolean testeBench) {
        List<String> motsDecodage = new ArrayList<>();
        List<String> motsListeModif = new ArrayList<>();
        if (testeBench) {
            // Phase de stockage des mots et phonemes
            for (int i = 0; i < PONCTUATION.length; i++) {
                texteBonneOrdre = texteBonneOrdre.replaceAll(PONCTUATION[i], " ");
                texteTypo = texteTypo.replaceAll(PONCTUATION[i], " ");
            }
            String[] listeMotsBonneOrdre = texteBonneOrdre.split(" ");
            String[] listeMotsModif = texteTypo.split(" ");
            for (int i = 0; i < listeMotsBonneOrdre.length; i++) {
                motsDecodage.add(listeMotsBonneOrdre[i]);
                motsListeModif.add(listeMotsModif[i]);
            }
        } else {
            motsDecodage = getShuffle(motsAppris, ContextTypoNetwork.echantmots);
        }

        String mot, motModifie, phon;
        Double match, matchR1aux, matchR2aux, matchR1, matchR2;

        matchR1 = 0.0;
        matchR2 = 0.0;
        int erreur = 0;
        int erreurR1 = 0;
        int erreurR2 = 0;
        LinkedList<List<String>> phonLexique;
        LinkedList<Fanal> listeGagnants;
        // Inicialisation du client Telnet
        demarreSupportLiaTelnet();
        int nombreEchant = ContextTypoNetwork.echantmots;
        if (testeBench) {
            nombreEchant = motsDecodage.size();
        }
        for (int nEchant = 0; nEchant < nombreEchant; nEchant++) {

            mot = motsDecodage.get(nEchant);
            if (testeBench) {
                motModifie = motsListeModif.get(nEchant);
            } else {
                motModifie = modifieMot(mot);
            }

            ContextTypoNetwork.logger.debug("Mot entree: " + motModifie);
            // Cherche le phoneme du mot en utilisant le client Telnet
            String phonLia = this.telnetClient.findPhonemeLia(motModifie);
            ContextTypoNetwork.logger.debug("Phoneme entree: " + phonLia);
            // Il convertit les règles du format Lia pour le format du lexique ou du LIA selon apprentissage
            if (!ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                phonLexique = reglesPhonemes.parseurFormatLiaToLexique(phonLia);
            } else {
                phonLexique = reglesPhonemes.parseurFormatLiaToListe(phonLia);
            }
            ContextTypoNetwork.logger.debug("Phoneme entree: " + phonLexique);
            listeGagnants = interfaceReseaux.decoderInterfaceReseaux("<" + motModifie + ">", phonLexique);
            match = interfaceReseaux.getTauxMatchingInterfaceReseaux(listeGagnants, "<" + mot + ">");
            tauxMatching += match;
            if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
                // Apprentissage des phonemes dans le reseau triangulaire
                if (ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                    phon = motsPhonsLiaMap.get(mot);
                } else {
                    phon = motsPhonsMap.get(mot);
                }
                if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
                    matchR1aux = interfaceReseaux.getTauxMatchingReseau(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE, "<" + phon + ">");
                } else {
                    matchR1aux = interfaceReseaux.getTauxMatchingReseau(NetworkControl.INDICE_RESEAU_TRIANG, "<" + phon + ">");
                }
                matchR2aux = interfaceReseaux.getTauxMatchingReseau(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, "<" + mot + ">");
                matchR1 += matchR1aux;
                matchR2 += matchR2aux;
                if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
                    if (matchR1aux < 1.0 || interfaceReseaux.getListeWinnersReseau(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE).size() != ((FuzzyNetwork) interfaceReseaux.getReseau(INDICE_RESEAU_FLOUS_GAUCHE)).NOMBRE_FANAUX_PAR_CLIQUE) {
                        erreurR1++;
                    }
                } else {
                    if (matchR1aux < 1.0 || interfaceReseaux.getListeWinnersReseau(NetworkControl.INDICE_RESEAU_TRIANG).size() != ((TriangularNetwork) interfaceReseaux.getReseau(INDICE_RESEAU_TRIANG)).NOMBRE_FANAUX_PAR_CLIQUE) {
                        erreurR1++;
                    }
                }

                if (matchR2aux < 1.0 || interfaceReseaux.getListeWinnersReseau(NetworkControl.INDICE_RESEAU_FLOUS_DROITE).size() != ((FuzzyNetwork) interfaceReseaux.getReseau(INDICE_RESEAU_FLOUS_DROITE)).NOMBRE_FANAUX_PAR_CLIQUE) {
                    erreurR2++;
                }
            }

            if (match < 1.0 || listeGagnants.size() != interfaceReseaux.NOMBRE_FANAUX_PAR_CLIQUE) {
                ContextTypoNetwork.logger.debug("Erreur: " + mot + " Match: " + match);
                erreur++;
            }
        }
        if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
            this.tauxMatchingParReseau.add(NetworkControl.INDICE_RESEAU_TRIANG, matchR1 / nombreEchant);
            this.tauxMatchingParReseau.add(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, matchR2 / nombreEchant);
            this.tauxErreurParReseau.add(NetworkControl.INDICE_RESEAU_TRIANG, (double) erreurR1 / nombreEchant);
            this.tauxErreurParReseau.add(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, (double) erreurR2 / nombreEchant);
        }
        tauxMatching = tauxMatching / nombreEchant;
        tauxErreur = ((double) erreur) / nombreEchant;
    }

    public String getPhonLexique(String mot) {
        return this.motsPhonsMap.get(mot);
    }

    public String getPhonLia(String mot) {
        return this.motsPhonsLiaMap.get(mot);
    }

    public String regleSelectionMots(LinkedList<String> listePhonemes, LinkedList<String> listeMots) {
        String result = " # Mots: ";
        HashSet<String> ensembleMots = new HashSet<>();
        for (String mot : listeMots) {
            ensembleMots.add(mot);
        }
        for (String mot : ensembleMots) {
            result += mot + " ,";
        }
        result += " # Phonemes: ";
        for (String phon : listePhonemes) {
            result += phon + " ,";
        }
        return result;
    }

    public String regleSelectionMots(LinkedList<String> listeMots) {
        String result = "";
        HashSet<String> ensembleMots = new HashSet<>();
        for (String mot : listeMots) {
            ensembleMots.add(mot.substring(1, mot.length() - 1));
        }
        for (String mot : ensembleMots) {
            result += mot + " ,";
        }
        return result;
    }

    public void phaseApprentissage(List<String> trainingWordsList, List<String> trainingPhonsList) {
        String word;
        //Verificar que as duas listas tem  o mesmo tamanho
        for (int i = 0; i < trainingWordsList.size(); i++) {
            word = trainingWordsList.get(i);
            motsStockes.add(word);
            motsPhonsLiaMap.put(word, trainingPhonsList.get(i));
        }

        // Phase d'apprentissage des réseaux de neurones
        TriangularNetwork rTriang;
        FuzzyNetwork rFlousDroite;
        FuzzyNetwork rFlousGauche;
        if (ACTIVE_RESEAU_FLOUS_PHONEMES) {
            rFlousGauche = (FuzzyNetwork) listeReseaux.get(INDICE_RESEAU_FLOUS_GAUCHE);
        } else {
            rTriang = (TriangularNetwork) listeReseaux.get(INDICE_RESEAU_TRIANG);
        }
        rFlousDroite = (FuzzyNetwork) listeReseaux.get(INDICE_RESEAU_FLOUS_DROITE);
        String phon;
        for (String mot : getShuffle(motsStockes, motsStockes.size())) {

            // Apprentissage des mots dans le reseau flous
            rFlousDroite.apprendreMot("<" + mot + ">");
            if (FuzzyNetwork.MITOSE_FANAUX) {
                // Réalisation de la mitose si besoin
                rFlousDroite.realiserMitose();
            }
            if (ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                phon = motsPhonsLiaMap.get(mot);
            } else {
                phon = motsPhonsMap.get(mot);
            }

            if (ACTIVE_RESEAU_FLOUS_PHONEMES) {
                rFlousGauche.apprendrePhoneme("<" + phon + ">");
                if (FuzzyNetwork.MITOSE_FANAUX) {
                    // Réalisation de la mitose si besoin
                    rFlousGauche.realiserMitose();
                }
            } else {
                rTriang.apprendrePhoneme("<" + phon + ">");
            }

            // Creer l'interface entre les deux reseaux     
            motsAppris.add(mot);
            if (ACTIVE_INTERFACE_RESEAUX) {
                this.interfaceReseaux.apprendreMot(mot);
            }
        }
        ContextTypoNetwork.logger.warn("Nombre de mots appris: " + motsAppris.size());
    }

    public void phaseApprentissage(String texte, boolean typoBench) {
        if (typoBench) {
            // Phase de stockage des mots et phonemes
            ContextTypoNetwork.logger.debug(texte);
            for (int i = 0; i < PONCTUATION.length; i++) {
                texte = texte.replaceAll(PONCTUATION[i], " ");
            }
            ContextTypoNetwork.logger.debug(texte);
            String[] listeMots = texte.split(" ");
            String phonLia;
            demarreSupportLiaTelnet();
            for (int i = 0; i < listeMots.length; i++) {
                motsStockes.add(listeMots[i]);
                // Cherche le phoneme du mot en utilisant le client Telnet
                phonLia = this.telnetClient.findPhonemeLia(listeMots[i]);
                motsPhonsLiaMap.put(listeMots[i], phonLia);
            }
            telnetClient.disconnect();
        } else {
            // Phase de stockage des mots et phonemes
            if (ContextTypoNetwork.plusieursLettres) {
                for (int nLettres : ContextTypoNetwork.nombre_lettres) {
                    stockerMotsPhons(ContextTypoNetwork.nbmots, false, ContextTypoNetwork.lemmes, nLettres);
                }
            } else {
                stockerMotsPhons(ContextTypoNetwork.nbmots, false, ContextTypoNetwork.lemmes, ContextTypoNetwork.nblettres);
            }
        }

        // Phase d'apprentissage des réseaux de neurones
        TriangularNetwork rTriang;
        FuzzyNetwork rFlousDroite;
        FuzzyNetwork rFlousGauche;
        if (ACTIVE_RESEAU_FLOUS_PHONEMES) {
            rFlousGauche = (FuzzyNetwork) listeReseaux.get(INDICE_RESEAU_FLOUS_GAUCHE);
        } else {
            rTriang = (TriangularNetwork) listeReseaux.get(INDICE_RESEAU_TRIANG);
        }
        rFlousDroite = (FuzzyNetwork) listeReseaux.get(INDICE_RESEAU_FLOUS_DROITE);
        String phon;
        for (String mot : getShuffle(motsStockes, motsStockes.size())) {

            // Apprentissage des mots dans le reseau flous
            rFlousDroite.apprendreMot("<" + mot + ">");
            if (FuzzyNetwork.MITOSE_FANAUX) {
                // Réalisation de la mitose si besoin
                rFlousDroite.realiserMitose();
            }
            // Apprentissage des phonemes dans le reseau triangulaire
            if (ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                phon = motsPhonsLiaMap.get(mot);
            } else {
                phon = motsPhonsMap.get(mot);
            }
            if (ACTIVE_RESEAU_FLOUS_PHONEMES) {
                rFlousGauche.apprendrePhoneme("<" + phon + ">");
                if (FuzzyNetwork.MITOSE_FANAUX) {
                    // Réalisation de la mitose si besoin
                    rFlousGauche.realiserMitose();
                }
            } else {
                rTriang.apprendrePhoneme("<" + phon + ">");
            }

            // Creer l'interface entre les deux reseaux     
            motsAppris.add(mot);
            if (ACTIVE_INTERFACE_RESEAUX) {
                this.interfaceReseaux.apprendreMot(mot);
            }

        }

        ContextTypoNetwork.logger.warn("Nombre de mots appris: " + motsAppris.size());

    }

    public void phaseDecodageTriang() {
        List<String> motsDecodage = getShuffle(motsAppris, ContextTypoNetwork.echantmots);
        String mot, motModifie;
        TriangularNetwork rTriang;
        Double match;
        int erreur = 0;
        rTriang = (TriangularNetwork) listeReseaux.get(INDICE_RESEAU_TRIANG);
        for (int nEchant = 0; nEchant < ContextTypoNetwork.echantmots; nEchant++) {
            mot = motsDecodage.get(nEchant);
            motModifie = modifieMot(mot);
            //match = rTriang.reconnaitrePhoneme(motsPhonsMap.get(mot), motsPhonsMap.get(mot));
            System.out.println("Mot: " + motsPhonsMap.get(mot));
            System.out.println("Mot: " + reglesPhonemes.cherchePhonemesMot(motModifie));
            System.out.println("Mot: " + PhonemeRules.supprimePhonemesNulls(reglesPhonemes.cherchePhonemesMot(motModifie)));
            match = rTriang.reconnaitrePhoneme(reglesPhonemes.cherchePhonemesMot(motModifie), motsPhonsMap.get(mot));
            tauxMatching += match;
            if (match < 1.0 || rTriang.getFanauxUnite(motsPhonsMap.get(mot)).size() != rTriang.NOMBRE_FANAUX_PAR_CLIQUE) {
                System.out.println("Erreur: " + mot + " Match: " + match);
                erreur++;
            }
        }
        tauxMatching = tauxMatching / ContextTypoNetwork.echantmots;
        tauxErreur = ((double) erreur) / ContextTypoNetwork.echantmots;
    }

    public void phaseDecodageFlous() {
        List<String> motsDecodage = getShuffle(motsAppris, ContextTypoNetwork.echantmots);
        String mot, motModifie;
        FuzzyNetwork rFlous;
        Double match;
        int erreur = 0;
        rFlous = (FuzzyNetwork) listeReseaux.get(INDICE_RESEAU_FLOUS_DROITE);
        for (int nEchant = 0; nEchant < ContextTypoNetwork.echantmots; nEchant++) {
            mot = motsDecodage.get(nEchant);
            motModifie = modifieMot(mot);
            System.out.println("Mot correcte: " + mot);
            System.out.println("Mot modifié: " + motModifie);
            ((FuzzyDecoder) rFlous.getDecodeur()).reconnaitreBottomUp("<" + motModifie + ">", 0, false, 0);
            match = ((FuzzyDecoder) rFlous.getDecodeur()).verifieDecodageBottomUp("<" + motModifie + ">", "<" + mot + ">");
            tauxMatching += match;
            if (match < 1.0) {
                System.out.println("Erreur: " + mot + " Match: " + match);
                erreur++;
            }
        }
        tauxMatching = tauxMatching / ContextTypoNetwork.echantmots;
        tauxErreur = ((double) erreur) / ContextTypoNetwork.echantmots;
    }

    public String getMotAppris(int i) {
        return this.motsAppris.get(i);
    }

    public PhonemeRules getReglesPhonemes() {
        return this.reglesPhonemes;
    }

    public Double getTauxMatching() {
        return this.tauxMatching;
    }

    public Double getTauxMatching(int indiceReseau) {
        return this.tauxMatchingParReseau.get(indiceReseau);
    }

    public Double getTauxErreur() {
        return this.tauxErreur;
    }

    public Double getTauxErreur(int indiceReseau) {
        return this.tauxErreurParReseau.get(indiceReseau);
    }

    public static String modifieMot(String mot) {
        String motModifie = mot;
        if (ContextTypoNetwork.substitution || ContextTypoNetwork.effacement || ContextTypoNetwork.permutation > -1 || ContextTypoNetwork.delections > 0 || ContextTypoNetwork.insertions > 0) {
            if (ContextTypoNetwork.substitution) {
                motModifie = NetworkControl.substituerLettre(motModifie);
            }
            if (ContextTypoNetwork.effacement) {
                motModifie = NetworkControl.effacerLettre(motModifie, "_");
            }
            if (ContextTypoNetwork.permutation > -1) {
                motModifie = NetworkControl.permuterLettres(motModifie, ContextTypoNetwork.permutation);
            }
            if (ContextTypoNetwork.delections > 0) {
                motModifie = NetworkControl.deletionLettre(motModifie, ContextTypoNetwork.delections);
            }
            if (ContextTypoNetwork.insertions > 0) {
                motModifie = NetworkControl.insertionLettre(mot, ContextTypoNetwork.insertions);
            }
        }
        return motModifie;
    }

    private void stockerMotsPhons(int n, boolean longueurVariable, boolean lemme, int longueur) {
        ResultSet rs;
        DB bd = new DB();
        String mot, phon, phonLia = "";
        bd.open();
        if (longueurVariable) {
            if (lemme) {
                rs = bd.result("SELECT DISTINCT `nomlemme`, `phon_lia`, `2_phon` FROM lexique_lemme, lexique_mot WHERE lemme_idlemme=idlemme AND 14_islem=1 AND LENGTH(CONVERT(nomlemme USING latin1))<=" + longueur + " ORDER BY RAND();");
            } else {
                rs = bd.result("SELECT DISTINCT `1_ortho`, `phon_lia`, `2_phon` FROM lexique_mot WHERE LENGTH(CONVERT(1_ortho USING latin1))<=" + longueur + " ORDER BY RAND();");
            }
        } else {

            if (lemme) {
                rs = bd.result("SELECT DISTINCT `nomlemme`, `phon_lia`, `2_phon` FROM lexique_lemme, lexique_mot WHERE lemme_idlemme=idlemme AND 14_islem=1 AND LENGTH(CONVERT(nomlemme USING latin1))=" + longueur + " ORDER BY RAND();");
                //rs = bd.result("SELECT DISTINCT `nomlemme`, `phon_lia`, `2_phon` FROM lemme, mot WHERE lemme_idlemme=idlemme AND 14_islem=1 AND LENGTH(phon_lia)=" + 2 * longueurPhonemeLia + "  AND LENGTH(CONVERT(nomlemme USING latin1))=" + longueur + " ORDER BY RAND();");
                //rs = bd.result("SELECT DISTINCT `nomlemme`, `phon_lia`, `2_phon` FROM lemme, mot WHERE lemme_idlemme=idlemme AND 14_islem=1 AND LENGTH(CONVERT(nomlemme USING latin1))=" + longueur + " ORDER BY RAND();");
            } else {
                //rs = bd.result("SELECT DISTINCT `1_ortho`, `phon_lia`, `2_phon` FROM mot WHERE LENGTH(phon_lia)=" + 2 * longueurPhonemeLia + " AND LENGTH(CONVERT(1_ortho USING latin1))=" + longueur + " ORDER BY RAND();");
                rs = bd.result("SELECT DISTINCT `1_ortho`, `phon_lia`, `2_phon` FROM lexique_mot WHERE LENGTH(CONVERT(1_ortho USING latin1))=" + longueur + " ORDER BY RAND();");
            }

        }
        try {
            int c = 0;
            while (rs.next() && c < n) {
                if (lemme) {
                    mot = rs.getString("nomlemme");
                } else {
                    mot = rs.getString("1_ortho");
                }
                if (ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                    phonLia = rs.getString("phon_lia");
                    motsPhonsLiaMap.put(mot, phonLia);
                }
                phon = rs.getString("2_phon");
                motsStockes.add(mot);
                motsPhonsMap.put(mot, phon);
                ContextTypoNetwork.logger.warn(mot);
                c++;

            }
            if (c < (n - 1)) {
                ContextTypoNetwork.logger.warn("Warning: Pas assez de mots dans la base de données (" + c + " au lieu de " + n + ")");
            }
        } catch (SQLException ex) {
            ContextTypoNetwork.logger.error("Probleme BD");
        }
    }

    private static List<String> getShuffle(LinkedList<String> lst, int nElements) {
        List<String> copy = new LinkedList<>(lst);
        Collections.shuffle(copy);
        return copy.subList(0, nElements);
    }

    private static String effacerLettre(String mot, String c) {
        return substituerLettre(mot, c);
    }

    private static String substituerLettre(String mot) {
        int temp; // Position de la lettre à substituer
        String c;
        Random randGen = new Random(); // Générateur sde nombres aléatoires

        // Sélection aléatoire d'un caractère
        temp = randGen.nextInt(Network.SYMBOLES.length());
        c = Network.SYMBOLES.substring(temp, temp + 1);

        return substituerLettre(mot, c);
    }

    private static String substituerLettre(String mot, String c) {
        Random randGen = new Random(); // Générateur de nombres aléatoires
        // Sélection aléatoire d'une position dans le mot
        int pos = randGen.nextInt(mot.length());

        return mot.substring(0, pos) + c + mot.substring(pos + 1);
    }

    // Methode statique pour l'insertion aleatoire des lettres dans un mot
    public static String insertionLettre(String mot, String caracter) {
        Random randGen;
        int pos;
        String result = mot;
        randGen = new Random(); // Générateur de nombres aléatoires

        // Sélection aléatoire d'une position dans le mot
        if (caracter.equals(CARAC_EFFAC)) {
            if (!mot.contains(CARAC_EFFAC)) {
                pos = mot.length() / 2;
            } else {
                pos = randGen.nextInt(mot.length() + 1);
            }
        } else {
            if (ContextTypoNetwork.insertion_lettres_internes) {
                pos = mot.length() / 2;
            } else {
                pos = randGen.nextInt(mot.length() + 1);
            }

        }

        result = result.substring(0, pos) + caracter + result.substring(pos);

        return result;
    }

    public static String insertionPhon(String phon, String caracter, int nombreInsertions) {
        Random randGen;
        String result = phon;
        int pos;
        randGen = new Random(); // Générateur de nombres aléatoires
        for (int i = 0; i < nombreInsertions; i++) {
            // Sélection aléatoire d'une position dans le phoneme
            pos = randGen.nextInt((phon.length()) / 2 + 1);
            result = result.substring(0, pos * 2) + caracter + result.substring(pos * 2);
        }

        return result;
    }

    // Methode statique pour l'insertion aleatoire des lettres dans un mot
    public static String insertionLettre(String mot, int nombreInsertions) {
        Random randGen = new Random(); // Générateur de nombres aléatoires
        int temp;
        String c;
        String result = mot;
        // Sélection aléatoire d'un caractère
        for (int i = 0; i < nombreInsertions; i++) {
            temp = randGen.nextInt(Network.SYMBOLES_INSERTION.length());
            c = Network.SYMBOLES_INSERTION.substring(temp, temp + 1);
            result = insertionLettre(result, c);
        }

        return result;
    }

    // Methode statique pour l'insertion aleatoire des lettres dans un mot
    public static String insertionLettre(String mot, String caracter, int nombreInsertions) {
        String result = mot;
        for (int i = 0; i < nombreInsertions; i++) {
            result = insertionLettre(result, caracter);
        }

        return result;
    }

    public static String deletionLettre(String mot, int nombreDeletions) {
        Random randGen = new Random(); // Générateur de nombres aléatoires
        int pos;
        String result = mot;
        // Sélection aléatoire d'un caractère
        for (int i = 0; i < nombreDeletions; i++) {
            pos = randGen.nextInt(result.length());
            result = result.substring(0, pos) + result.substring(pos + 1);
        }
        return result;
    }

    public static int posAleatoireMot(String mot) {
        Random randGen = new Random();
        int pos = randGen.nextInt(mot.length());
        return pos;
    }

    public static int posAleatoirePhon(List<String> phon) {
        Random randGen = new Random();
        phon.remove(CARAC_DEBUT);
        phon.remove(CARAC_FIN);
        int pos = randGen.nextInt(phon.size());
        return pos;
    }

    private static String permuterLettres(String mot, int permutation) {
        int pos1, pos2; // Position des 2 lettres à permuter
        String resultat;
        Random randGen = new Random(); // Générateur sde nombres aléatoires
        //do {
        pos1 = randGen.nextInt(mot.length() - permutation);
        if (permutation > 0) { // Permutation à distance fixe
            pos2 = pos1 + permutation;
        } else if (permutation == 0) { // Permutation à distance variable

            pos2 = (pos1 + 1 + randGen.nextInt(mot.length() - 1)) % mot.length();
        } else { // Permutation avec soi-même (pas de permutation)
            pos2 = pos1;
        }
        // } while(TypoMultireseaux.permutation_restriction && VOYELLES.contains(mot.substring(pos1, pos1+1)) && VOYELLES.contains(mot.substring(pos2, pos2+1)));
        // Réordonnement 
        if (pos1 > pos2) {
            int temp = pos1;
            pos1 = pos2;
            pos2 = temp;
        }

        resultat = mot.substring(0, pos1)
                + mot.substring(pos2, pos2 + 1)
                + mot.substring(pos1 + 1, pos2)
                + mot.substring(pos1, pos1 + 1)
                + mot.substring(pos2 + 1);
        return resultat;
    }

    public static int getLongueurPhon(String phon) {
        int result=0;
        for(int i=0;i<phon.length();i++){
            if(!phon.substring(i,i+1).equals(CARAC_DEBUT) && !phon.substring(i,i+1).equals(CARAC_FIN)){
                i++;
            }
            result++;
        }
        return result;
        //String phonMod = phon.substring(1, phon.length() - 1);
        //return (phonMod.length() / 2) + 2;
    }

}
