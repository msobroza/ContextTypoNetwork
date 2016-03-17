package control.network;

import control.NetworkControl;
import control.ContextTypoNetwork;
import control.decoder.FuzzyDecoder;
import control.decoder.TriangularDecoder;
import control.rules.LetterInformation;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import model.Clique;
import model.Fanal;
import model.FanalFlous;
import model.MacroFanal;

public class InterfaceNetwork extends TriangularNetwork implements LetterInformation {

    // Ils définissent le type d'information à être appris pour chaque reseau
    public static final int TYPE_INFONS_MOTS = 0;
    public static final int TYPE_INFONS_PHONEMES = 1;

    // Ils définissent le type de liaison entre les cliques frontieres des reseaux
    public static int TYPE_LIAISON_UNIDIRECTIONNELLE = 0;
    public static int TYPE_LIAISON_BIDIRECTIONNELLE = 1;

    // Ils définissent la direction d'activation
    public static int ACTIVATION_VERS_DROITE = 0;
    public static int ACTIVATION_VERS_GAUCHE = 1;

    // Ces attributs representent les reseaux de l'interface reseaux
    private final ArrayList<Network> listeReseaux;
    private final ArrayList<Integer> listeTypeReseaux;
    private final NetworkControl controleReseaux;
    private ArrayList<HashSet<Fanal>> listeFanauxDecodes;

    public static HashMap<Fanal, Integer> fanauxActifsLateral;

    public InterfaceNetwork() {
        super(0, 0, 0, 0, false, false, false);
        this.listeReseaux = null;
        this.listeTypeReseaux = null;
        this.controleReseaux = null;
    }

    public InterfaceNetwork(NetworkControl controleReseaux) {
        // On instancie un reseau du type triangulaire
        super(200, 100, 8, 1, false, false, true);
        this.listeReseaux = new ArrayList<>();
        this.listeTypeReseaux = new ArrayList<>();
        this.controleReseaux = controleReseaux;
        this.listeFanauxDecodes = new ArrayList<>();
        // Ce sont les valeurs de score de chaque macro fanal
        fanauxActifsLateral = new HashMap<>();
        if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
            if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
                this.listeFanauxDecodes.add(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE, new HashSet<Fanal>());
            } else {
                this.listeFanauxDecodes.add(NetworkControl.INDICE_RESEAU_TRIANG, new HashSet<Fanal>());
            }
            this.listeFanauxDecodes.add(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, new HashSet<Fanal>());
        }

    }

    // On ajoute un reseau d'indice "indiceReseau" et avec un type
    public void ajouterReseau(Network r, int indiceReseau, int typeReseau) {
        this.listeReseaux.add(indiceReseau, r);
        this.listeTypeReseaux.add(indiceReseau, typeReseau);
    }

    @Override
    public Clique apprendreMot(String mot) {

        Clique cliqueInterface, cliqueInf, cliqueGauche, cliqueDroite;
        cliqueGauche = null;
        cliqueDroite = null;
        TriangularLevel n;
        // Il obtient le niveau h=0
        n = (TriangularLevel) this.listeNiveaux.get(0);
        if (n.existeClique("<" + mot + ">")) {
            return null;
        } else {
            ContextTypoNetwork.logger.debug("Réseau d'interface - <" + mot + "> -");
            // Il ajoute la clique du mot dans le niveau h=0 du reseau interface
            cliqueInterface = n.ajouterClique("<" + mot + ">", 1, "", 0, "", 0, this.NOMBRE_FANAUX_PAR_CLIQUE);
            for (int i = 0; i < listeReseaux.size(); i++) {
                // Il selectionne le type d'information à garder
                if (listeTypeReseaux.get(i) == TYPE_INFONS_MOTS && listeReseaux.get(i).getTypeReseau() == NetworkControl.RESEAU_FLOUS) {
                    String motVar = "<" + mot + ">";
                    if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_DROITE) {
                        int taille = motVar.length();
                        for (int j = 0; j < ((FuzzyNetwork) listeReseaux.get(i)).NOMBRE_CLUSTERS - taille; j++) {
                            motVar = motVar + "*";
                        }
                    }
                    cliqueInf = ((FuzzyLevel) ((FuzzyNetwork) listeReseaux.get(i)).listeNiveaux.get(0)).getCliqueMotFlous(motVar);
                    ContextTypoNetwork.logger.debug("Clique mot: " + mot + " : " + cliqueInf.getInfo());
                } else {
                    if (ContextTypoNetwork.APPRENTISSAGE_PHONS_LIA) {
                        if (listeReseaux.get(i).getTypeReseau() == NetworkControl.RESEAU_FLOUS) {
                            String phon = "<" + controleReseaux.getPhonLia(mot) + ">";
                            if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_GAUCHE) {

                                int taille = NetworkControl.getLongueurPhon(phon);
                                System.out.println("Tamanho antes insercao: " + taille + ", clique: " + phon);
                                for (int j = 0; j < ((FuzzyNetwork) listeReseaux.get(i)).NOMBRE_CLUSTERS - taille; j++) {
                                    phon = phon + "##";
                                }
                            }
                            System.out.println("Clique procurado: " + phon);
                            cliqueInf = ((FuzzyLevel) ((FuzzyNetwork) listeReseaux.get(i)).listeNiveaux.get(0)).getCliqueMotFlous(phon);
                            if (cliqueInf == null) {
                                System.out.println("clique nulo");
                            }
                        } else {
                            cliqueInf = ((TriangularNetwork) listeReseaux.get(i)).getNiveauSup().getCliqueMot("<" + controleReseaux.getPhonLia(mot) + ">");
                        }

                        ContextTypoNetwork.logger.debug("Clique phoneme: " + controleReseaux.getPhonLia(mot) + " : " + cliqueInf.getInfo());
                    } else {
                        if (listeReseaux.get(i).getTypeReseau() == NetworkControl.RESEAU_FLOUS) {
                            cliqueInf = ((FuzzyLevel) ((FuzzyNetwork) listeReseaux.get(i)).listeNiveaux.get(0)).getCliqueMotFlous("<" + controleReseaux.getPhonLexique(mot) + ">");
                        } else {
                            cliqueInf = ((TriangularNetwork) listeReseaux.get(i)).getNiveauSup().getCliqueMot("<" + controleReseaux.getPhonLexique(mot) + ">");
                        }

                        ContextTypoNetwork.logger.debug("Clique phoneme: " + controleReseaux.getPhonLexique(mot) + " : " + cliqueInf.getInfo());
                    }
                }

                // Il cree la liaison entre les deux niveaux
                for (Fanal fInf : cliqueInf.getListe()) {
                    for (Fanal fSup : cliqueInterface.getListe()) {
                        //TypoMultireseaux.logger.debug(fInf + " -> " + fSup);
                        n.creerLiaisonInterNiveaux(fInf, fSup);
                    }
                }
                if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES || NetworkControl.ACTIVE_CONNECTIONS_LATERALES_TRIANG) {
                    // Il selectionne les fanaux de la clique à gauche et de la clique à droite
                    if (i == 0) {
                        cliqueGauche = cliqueInf;
                    } else {
                        cliqueDroite = cliqueInf;
                    }
                }

            }
            if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES || NetworkControl.ACTIVE_CONNECTIONS_LATERALES_TRIANG) {
                for (Fanal fGauche : cliqueGauche.getListe()) {
                    for (Fanal fDroite : cliqueDroite.getListe()) {
                        // Il crée les liaisons laterales
                        ((TriangularLevel) this.getListeNiveaux().get(0)).creerLiaisonInterHyperNiveaux(fGauche, fDroite);
                    }
                }
            }
            return cliqueInterface;
        }

    }

    public LinkedList<Fanal> decoderInterfaceReseaux(String motMod, LinkedList<List<String>> phonLexique) {
        HashSet<Fanal> lstFanauxTriangUnion = new HashSet<>();
        HashSet<Fanal> lstFanauxFlousUnion = new HashSet<>();
        LinkedList<Fanal> lstFanauxFlous = new LinkedList<>();
        LinkedList<Fanal> lstFanauxTriang = new LinkedList<>();
        LinkedList<Fanal> lstFanauxInf = new LinkedList<>();
        TriangularNetwork rTriang;
        FuzzyNetwork rFlousDroite, rFlousGauche;
        TriangularDecoder dTriang;
        FuzzyDecoder dFlousDroite;
        FuzzyDecoder dFlousGauche;
        String motFloue, phonMod, phonFlous;
        // ---- Flous de mots -------
        rFlousDroite = (FuzzyNetwork) listeReseaux.get(NetworkControl.INDICE_RESEAU_FLOUS_DROITE);
        // Il prend le decodeur du Reseau Flous
        dFlousDroite = (FuzzyDecoder) (rFlousDroite).getDecodeur();
        // Ajoute postambule dans le mot (information de non information)
        if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_DROITE) {
            int taille = motMod.length();
            for (int i = 0; i < rFlousDroite.NOMBRE_CLUSTERS - taille; i++) {
                motMod = motMod + "*";
            }
        }
        // TO DO - Creer juste pour tester la dynamique de deletion des caracteres
        if (motMod.length() < rFlousDroite.NOMBRE_CLUSTERS) {

            motFloue = "<" + NetworkControl.insertionLettre(motMod.substring(1, motMod.length() - 1), CARAC_EFFAC, rFlousDroite.NOMBRE_CLUSTERS - motMod.length()) + ">";
            // Il réalise la propagation et decodage (0 est la fênetre initialle)
            dFlousDroite.reconnaitreBottomUp(motFloue, 0, false, 0);
            // Il obtient les fanaux gagnants du decodage flous
            for (LinkedList<Fanal> lst : dFlousDroite.getWinnersSeqBottomUpFlous(motFloue)) {
                lstFanauxFlous.addAll(lst);
            }
        } else {
            // Il réalise la propagation et decodage
            dFlousDroite.reconnaitreBottomUp(motMod, 0, false, 0);
            // Il obtient les fanaux gagnants du decodage flous
            for (LinkedList<Fanal> lst : dFlousDroite.getWinnersSeqBottomUpFlous(motMod)) {
                lstFanauxFlous.addAll(lst);
            }
        }
        lstFanauxFlousUnion.addAll(lstFanauxFlous);
        // Si on actice l'option active reseau flous phonemes
        if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
            phonMod = "";
            for (List<String> sousPhon : phonLexique) {
                for (String partie : sousPhon) {
                    phonMod += partie;
                }
            }

            //activerConnectionsLaterales(lstFanauxFlous, ACTIVATION_VERS_GAUCHE);
            // ---- Flous de phonemes -------
            // Il prendre le ReseauFlous de phonemes
            rFlousGauche = (FuzzyNetwork) listeReseaux.get(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE);
            // Il prend le decodeur du Reseau Flous
            dFlousGauche = (FuzzyDecoder) (rFlousGauche).getDecodeur();

            if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_GAUCHE) {
                int taille = NetworkControl.getLongueurPhon(phonMod);
                for (int i = 0; i < rFlousGauche.NOMBRE_CLUSTERS - taille; i++) {
                    phonMod = phonMod + "##";
                }
            }
            System.out.println("comodo: " + phonMod);
            if (NetworkControl.getLongueurPhon(phonMod) < rFlousGauche.NOMBRE_CLUSTERS) {
                // Si la longueur du mot est plus petite que le nombre de clusters on remplace la diference pour caracteres effaces
                phonFlous = "<" + NetworkControl.insertionPhon(phonMod.substring(1, phonMod.length() - 1), CARAC_EFFAC, rFlousGauche.NOMBRE_CLUSTERS - NetworkControl.getLongueurPhon(phonMod)) + ">";
                // Il réalise la propagation et decodage (0 est la fênetre initiale)
                dFlousGauche.reconnaitreBottomUp(phonFlous, 0, true, 0);
                // Il obtient les fanaux gagnants du decodage flous
                for (LinkedList<Fanal> lst : dFlousGauche.getWinnersSeqBottomUpFlous(phonFlous)) {
                    lstFanauxTriang.addAll(lst);
                }

            } else {
                // Il réalise la propagation et decodage
                dFlousGauche.reconnaitreBottomUp(phonMod, 0, true, 0);
                // Il obtient les fanaux gagnants du decodage flous
                for (LinkedList<Fanal> lst : dFlousGauche.getWinnersSeqBottomUpFlous(phonMod)) {
                    lstFanauxTriang.addAll(lst);
                }
            }

            lstFanauxTriangUnion.addAll(lstFanauxTriang);
            /*for (Fanal f : lstFanauxTriang) {
             lstFanauxTriangUnion.add(((FanalFlous) f).getMacroFanal());
             }*/
        } else {
            LinkedList<Fanal> lstAux = new LinkedList<>();
            lstAux.addAll(lstFanauxFlous);
            activerConnectionsLaterales(lstAux, ACTIVATION_VERS_GAUCHE);
            // Si il y a un reseau triangulaire de phonemes
            rTriang = (TriangularNetwork) listeReseaux.get(NetworkControl.INDICE_RESEAU_TRIANG);
            // Il prend le decodeur du Reseau Triangulaire
            dTriang = (TriangularDecoder) (rTriang.getDecodeur());
            lstFanauxTriang.addAll(dTriang.getWinnersBottomUpHyperDeuxNiveaux(phonLexique));
            lstFanauxTriangUnion.addAll(lstFanauxTriang);
        }

        // Ajoute la liste de gagnants des reseaux
        if (ContextTypoNetwork.METRIQUES_PAR_RESEAU) {
            this.listeFanauxDecodes.set(NetworkControl.INDICE_RESEAU_FLOUS_DROITE, lstFanauxFlousUnion);
            ContextTypoNetwork.logger.warn("#Flous Mots # " + this.listeFanauxDecodes.get(NetworkControl.INDICE_RESEAU_FLOUS_DROITE).size());
            if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
                this.listeFanauxDecodes.set(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE, lstFanauxTriangUnion);
                ContextTypoNetwork.logger.warn("#Flous Phons # " + this.listeFanauxDecodes.get(NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE).size());
            } else {
                this.listeFanauxDecodes.set(NetworkControl.INDICE_RESEAU_TRIANG, lstFanauxTriangUnion);
                ContextTypoNetwork.logger.warn("#Triang # " + this.listeFanauxDecodes.get(NetworkControl.INDICE_RESEAU_TRIANG).size());
            }

        }

        if (TriangularDecoder.UNION_BOOSTING) {
            lstFanauxInf.addAll(lstFanauxTriangUnion);
        } else {
            lstFanauxInf.addAll(lstFanauxTriang);
        }
        if (!NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
            lstFanauxInf.addAll(lstFanauxFlousUnion);
        } else {
            lstFanauxInf.addAll(lstFanauxFlousUnion);
        }

        ContextTypoNetwork.logger.debug("Nombre fanaux avant propagation :" + lstFanauxInf.size());
        if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES || NetworkControl.ACTIVE_CONNECTIONS_LATERALES_TRIANG) {
            // À chaque recherche de mot on redemarre les activations laterales
            remiseZeroPropagationLaterale();
        }

        return ((TriangularDecoder) this.getDecodeur()).getWinnersInterfaceReseaux(lstFanauxInf);
    }

    private void activerConnectionsLaterales(LinkedList<Fanal> fanauxActives, int directionActivation) {
        for (Fanal f : fanauxActives) {
            activerConnectionsLaterales(f, directionActivation);
        }

    }

    private void activerConnectionsLaterales(Fanal fanalActive, int directionActivation) {
        MacroFanal fmacro;
        // Convention: HyperSup est a droite et HyperInf est a gauche 
        if (directionActivation == ACTIVATION_VERS_GAUCHE) {
            for (Fanal f : fanalActive.getFanauxHyperInf()) {
                if (NetworkControl.ACTIVE_LATERALE_MACRO) {
                    fmacro = ((FanalFlous) f).getMacroFanal();
                    if (fanauxActifsLateral.containsKey(fmacro)) {
                        fanauxActifsLateral.put(fmacro, fanauxActifsLateral.get(fmacro) + 1);
                    } else {
                        fanauxActifsLateral.put(fmacro, 1);
                    }
                } else {
                    if (fanauxActifsLateral.containsKey(f)) {
                        fanauxActifsLateral.put(f, fanauxActifsLateral.get(f) + 1);
                    } else {
                        fanauxActifsLateral.put(f, 1);
                    }
                }

            }
        } else {
            for (Fanal f : fanalActive.getFanauxHyperSup()) {
                if (NetworkControl.ACTIVE_LATERALE_MACRO) {
                    fmacro = ((FanalFlous) f).getMacroFanal();
                    if (fanauxActifsLateral.containsKey(fmacro)) {
                        fanauxActifsLateral.put(fmacro, fanauxActifsLateral.get(fmacro) + 1);
                    } else {
                        fanauxActifsLateral.put(fmacro, 1);
                    }
                } else {
                    if (fanauxActifsLateral.containsKey(f)) {
                        fanauxActifsLateral.put(f, fanauxActifsLateral.get(f) + 1);
                    } else {
                        fanauxActifsLateral.put(f, 1);
                    }
                }

            }
        }
    }

    public double getTauxMatchingReseau(int indiceReseau, String motAppris) {
        int nombreFanauxTrouves = 0;
        HashSet<Fanal> fanauxGagnantsParReseau = listeFanauxDecodes.get(indiceReseau);
        HashSet<Fanal> macroFanauxGagnantsParReseau = new HashSet<>();
        LinkedList<Fanal> fanauxCorrects;
        int nombreFanauxParClique;
        if (indiceReseau == NetworkControl.INDICE_RESEAU_FLOUS_DROITE) {
            if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_DROITE) {
                int taille = motAppris.length();
                for (int i = 0; i < ((FuzzyNetwork) this.listeReseaux.get(indiceReseau)).NOMBRE_CLUSTERS - taille; i++) {
                    motAppris = motAppris + "*";
                }
            }

            fanauxCorrects = this.listeReseaux.get(indiceReseau).getListeNiveaux().get(0).getCliqueMot(motAppris).getListe();
            for (Fanal f : fanauxGagnantsParReseau) {
                macroFanauxGagnantsParReseau.add(((FanalFlous) f).getMacroFanal());
            }
        } else {
            if (NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES) {
                if (ContextTypoNetwork.TAILLE_VARIABLE_RESEAU_FLOU_GAUCHE) {
                    int taille = NetworkControl.getLongueurPhon(motAppris);
                    for (int i = 0; i < ((FuzzyNetwork) this.listeReseaux.get(indiceReseau)).NOMBRE_CLUSTERS - taille; i++) {
                        motAppris = motAppris + "##";
                    }
                }
                fanauxCorrects = this.listeReseaux.get(indiceReseau).getListeNiveaux().get(0).getCliqueMot(motAppris).getListe();
                for (Fanal f : fanauxGagnantsParReseau) {
                    macroFanauxGagnantsParReseau.add(((FanalFlous) f).getMacroFanal());
                }
            } else {
                fanauxCorrects = ((TriangularNetwork) this.listeReseaux.get(indiceReseau)).getNiveauSup().getCliqueMot(motAppris).getListe();
            }
        }
        if (NetworkControl.INDICE_RESEAU_FLOUS_DROITE == indiceReseau || (NetworkControl.INDICE_RESEAU_FLOUS_GAUCHE == indiceReseau && NetworkControl.ACTIVE_RESEAU_FLOUS_PHONEMES)) {
            for (Fanal f : fanauxCorrects) {
                if (macroFanauxGagnantsParReseau.contains(f)) {
                    nombreFanauxTrouves++;
                }
            }
            nombreFanauxParClique = ((FuzzyNetwork) this.listeReseaux.get(indiceReseau)).NOMBRE_FANAUX_PAR_CLIQUE;
        } else {
            for (Fanal f : fanauxCorrects) {
                if (fanauxGagnantsParReseau.contains(f)) {
                    nombreFanauxTrouves++;
                }
            }
            nombreFanauxParClique = ((TriangularNetwork) this.listeReseaux.get(indiceReseau)).NOMBRE_FANAUX_PAR_CLIQUE;
        }

        return ((double) nombreFanauxTrouves) / nombreFanauxParClique;
    }

    public HashSet<Fanal> getListeWinnersReseau(int indiceReseau) {
        return this.listeFanauxDecodes.get(indiceReseau);
    }

    public Network getReseau(int indiceReseau) {
        return this.listeReseaux.get(indiceReseau);
    }

    private void remiseZeroPropagationLaterale() {
        fanauxActifsLateral = new HashMap<>();
    }

}
