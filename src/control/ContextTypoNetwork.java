package control;

import config.ConfigFile;
import exception.FileNotExists;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import tools.FileIO;

public class ContextTypoNetwork {

    
    public static int nblettres =7;
    // Interaction fichiers
    public static boolean FILE_INTERACTION = true;
    // Training file
    public static String train_words_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/training/words/train_words_set.pickle.7";
    // Test file
    public static String test_words_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/test/words/test_words_set_ins_1.pickle.7";
    // Taux de matching par reseau
    public static boolean RATES_PER_NETWORK = true;
    // Active taille variable reseau flou
    public static boolean VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT = false;

    public static Logger logger = Logger.getRootLogger();

    public static void main(String[] args) throws FileNotExists {

        BasicConfigurator.configure();
        logger.setLevel(Level.ALL);
        NetworkControl cr = new NetworkControl();
        // Apprentissage et decodage des mots
        if (FILE_INTERACTION) {
            if (!FileIO.fileExists(train_words_file)) {
                throw new FileNotExists(train_words_file);
            }
            
            HashMap<Integer, List<String>> trainInput = new HashMap<>(FileIO.readSplittedFile(train_words_file));
            cr.learningPhase(trainInput.get(ConfigFile.TrainWords.WORDS.getIndex()), trainInput.get(ConfigFile.TrainWords.PHONS.getIndex()));
        }

        ContextTypoNetwork.logger.debug("Apprentissage OK! ");

        if (FILE_INTERACTION) {
            if (!FileIO.fileExists(test_words_file)) {
                throw new FileNotExists(test_words_file);
            }
            HashMap<Integer, List<String>> testInput = new HashMap<>(FileIO.readSplittedFile(test_words_file));
            cr.decoderPhase(testInput.get(ConfigFile.TestWords.WORDS.getIndex()).subList(0, 100), testInput.get(ConfigFile.TestWords.ERRORS.getIndex()).subList(0, 100), testInput.get(ConfigFile.TestWords.ERRORS_PHONS.getIndex()).subList(0, 100));
        }

        if (ContextTypoNetwork.RATES_PER_NETWORK) {
            System.out.println("Taux matching Reseau Triang: " + cr.getMatchingRate(NetworkControl.TRIANGULAR_NETWORK_INDEX));
            System.out.println("Taux d'erreur Reseau Triang: " + cr.getErrorRate(NetworkControl.TRIANGULAR_NETWORK_INDEX));
            System.out.println("Taux matching Reseau Flous: " + cr.getMatchingRate(NetworkControl.FUZZY_NETWORK_INDEX));
            System.out.println("Taux d'erreur Reseau Flous: " + cr.getErrorRate(NetworkControl.FUZZY_NETWORK_INDEX));
        }

        System.out.println("Taux matching: " + cr.getMatchingRate());
        System.out.println("Taux erreur: " + cr.getErrorRate());

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
