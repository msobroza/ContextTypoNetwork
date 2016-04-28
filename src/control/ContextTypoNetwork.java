package control;

import config.ConfigFile;
import exception.FileNotExists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import tools.FileIO;
import tools.interface_cuda.CUDAContextInterface;
import tools.interface_cuda.CUDAInterfaceClientFactory;

public class ContextTypoNetwork {

    public static final String CUDA_SERVER_HOSTNAME = "10.29.232.217";

    public static final int CUDA_SERVER_PORTNUMBER = 9698;

    public static int numberLetters = 7;
    // Interaction fichiers
    public static boolean FILE_WORDS_INTERACTION = true;
    // Training file
    public static String train_words_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/training/words/train_words_set.pickle.7";
    // Training sentences file
    //public static String train_sentences_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/training/sequences/train_sequences_set_sarney.pickle";
    //public static String train_sentences_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/training/sequences/train_sequences_set_complete.pickle";
    //public static String train_sentences_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/training/sequences/train_sequences_set.pickle";
    public static String train_sentences_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/training/sequences/train_sequences_small.pickle";
    // Test file
    public static String test_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/test/words/test_words_set_ins_1.pickle.7";
    // Test sentence file
    public static String test_sentences_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/test/sequences/test_sequences_small.pickle";
    //public static String test_sentences_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/corpus/test/sequences/test_sequences_errors_set_ins_1.pickle.7";
    public static boolean TEST_SENTENCES_TOKENISED = true;
    // Use log file
    public static boolean USE_LOG_OUT_FILE = false;
    // Out file
    public static String out_file = "/home/msobroza/NetBeansProjects/ContextTypoNetwork/out.log";
    // Taux de matching par reseau
    public static boolean RATES_PER_NETWORK = false;
    // Active taille variable reseau flou
    public static boolean VARIABLE_WORDS_SIZE_FUZZY_NETWORK_RIGHT = false;
    // Use context information
    public static boolean USE_CONTEXT_INFORMATION = true;
    // Test only context network
    public static boolean TEST_ONLY_CONTEXT_NETWORK = true;

    public static Logger logger = Logger.getRootLogger();

    enum ConfigParameters {

        INPUT_TYPE("-input_type"), LETTERS_NUMBER("-n_letters"), TRAIN_WORDS_FILE("-train_words_file"),
        TRAIN_SENTENCES_FILE("-train_sentences_file"), TEST_FILE("-test_file"), OUT_FILE("-o"), CUDA_SERVERNAME("-server"), CUDA_SERVER_PORTNUMBER("-port");

        String paramCommand;

        private ConfigParameters(String paramCommand) {
            this.paramCommand = paramCommand;
        }

        @Override
        public String toString() {
            return this.paramCommand;
        }
    }

    enum InputType {

        WORDS("words"), SENTENCES("sentences");

        String type;

        private InputType(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }

    public static void main(String[] args) throws FileNotExists, TException {

        NetworkControl controlNetwork;
        BasicConfigurator.configure();
        logger.setLevel(Level.ALL);
        HashMap<ConfigParameters, String> configurationMap = new HashMap<>();
        if (args.length > 2) {
            for (int i = 0; i < args.length; i++) {
                if (ConfigParameters.INPUT_TYPE.toString().equals(args[i])) {
                    if ((i + 1) < args.length) {
                        // Validation of a valid input type expression
                        for (InputType type : InputType.values()) {
                            if (type.toString().equals(args[i + 1])) {
                                configurationMap.put(ConfigParameters.INPUT_TYPE, args[i + 1]);
                                break;
                            }
                        }
                    }
                } else {
                    if (ConfigParameters.LETTERS_NUMBER.toString().equals(args[i]) && (i + 1) < args.length) {
                        if ((i + 1) < args.length) {
                            // Validation of number format exception
                            try {
                                configurationMap.put(ConfigParameters.LETTERS_NUMBER, Integer.parseInt(args[i + 1]) + "");
                            } catch (NumberFormatException n) {
                            }
                        }
                    } else {
                        if (ConfigParameters.OUT_FILE.toString().equals(args[i])) {
                            if ((i + 1) < args.length) {
                                // Validation of a valid output file
                                if (!FileIO.fileExists(args[i + 1])) {
                                    throw new FileNotExists(args[i + 1]);
                                } else {
                                    configurationMap.put(ConfigParameters.OUT_FILE, args[i + 1]);
                                }
                            }
                        } else {
                            if (ConfigParameters.TRAIN_WORDS_FILE.toString().equals(args[i])) {
                                if ((i + 1) < args.length) {
                                    // Validation of a valid train file
                                    if (!FileIO.fileExists(args[i + 1])) {
                                        throw new FileNotExists(args[i + 1]);
                                    } else {
                                        configurationMap.put(ConfigParameters.TRAIN_WORDS_FILE, args[i + 1]);
                                    }
                                }
                            } else {
                                if (ConfigParameters.TRAIN_SENTENCES_FILE.toString().equals(args[i])) {
                                    if ((i + 1) < args.length) {
                                        // Validation of a valid train file
                                        if (!FileIO.fileExists(args[i + 1])) {
                                            throw new FileNotExists(args[i + 1]);
                                        } else {
                                            configurationMap.put(ConfigParameters.TRAIN_SENTENCES_FILE, args[i + 1]);
                                        }
                                    }
                                } else {
                                    if (ConfigParameters.TEST_FILE.toString().equals(args[i])) {
                                        if ((i + 1) < args.length) {
                                            // Validation of a valid test file
                                            if (!FileIO.fileExists(args[i + 1])) {
                                                throw new FileNotExists(args[i + 1]);
                                            } else {
                                                configurationMap.put(ConfigParameters.TEST_FILE, args[i + 1]);
                                            }
                                        }
                                    } else {
                                        if (ConfigParameters.CUDA_SERVERNAME.toString().equals(args[i])) {
                                            if ((i + 1) < args.length) {
                                                configurationMap.put(ConfigParameters.CUDA_SERVERNAME, args[i + 1]);
                                            }
                                        } else {
                                            if (ConfigParameters.CUDA_SERVER_PORTNUMBER.toString().equals(args[i])) {
                                                if ((i + 1) < args.length) {
                                                    configurationMap.put(ConfigParameters.CUDA_SERVER_PORTNUMBER, args[i + 1]);
                                                }
                                            }
                                        }
                                    }
                                }

                            }
                        }
                    }
                }
            }
        }
        if (configurationMap.containsKey(ConfigParameters.INPUT_TYPE)) {
            FILE_WORDS_INTERACTION = (InputType.WORDS).toString().equals(configurationMap.get(ConfigParameters.INPUT_TYPE));
            USE_CONTEXT_INFORMATION = (InputType.SENTENCES).toString().equals(configurationMap.get(ConfigParameters.INPUT_TYPE));
            if (configurationMap.containsKey(ConfigParameters.LETTERS_NUMBER)) {
                numberLetters = Integer.parseInt(configurationMap.get(ConfigParameters.LETTERS_NUMBER));
            }
            if (configurationMap.containsKey(ConfigParameters.TRAIN_WORDS_FILE)) {
                train_words_file = configurationMap.get(ConfigParameters.TRAIN_WORDS_FILE);
            }
            if (configurationMap.containsKey(ConfigParameters.TRAIN_SENTENCES_FILE)) {
                train_sentences_file = configurationMap.get(ConfigParameters.TRAIN_SENTENCES_FILE);
            }
            if (configurationMap.containsKey(ConfigParameters.TEST_FILE)) {
                test_file = configurationMap.get(ConfigParameters.TEST_FILE);
            }

            if (configurationMap.containsKey(ConfigParameters.OUT_FILE)) {
                out_file = configurationMap.get(ConfigParameters.OUT_FILE);
                USE_LOG_OUT_FILE = true;
            }
        }
        CUDAInterfaceClientFactory seqSender = null;
        // It instantiates control network
        if (USE_CONTEXT_INFORMATION) {
            // Files sentence interaction
            seqSender = new CUDAInterfaceClientFactory(CUDA_SERVER_HOSTNAME, CUDA_SERVER_PORTNUMBER);
            seqSender.openConnection();
            CUDAContextInterface.Client client = seqSender.getCUDAContextInterfaceClient();
            controlNetwork = new NetworkControl(client);
            // Learn sentences
            if (!FileIO.fileExists(train_sentences_file)) {
                throw new FileNotExists(train_sentences_file);
            }
            HashMap<Integer, List<String>> trainSentencesInput = new HashMap<>(FileIO.readSplittedFile(train_sentences_file));
            controlNetwork.learningSentencesPhase(trainSentencesInput.get(ConfigFile.TrainSentences.NORMALIZED_SENTENCE.getIndex()));
            ContextTypoNetwork.logger.debug("Apprentissage de phrases OK! ");
        } else {
            controlNetwork = new NetworkControl();
        }
        // Learn words

        /*if (!FileIO.fileExists(train_words_file)) {
         throw new FileNotExists(train_words_file);
         }
         HashMap<Integer, List<String>> trainWordsInput = new HashMap<>(FileIO.readSplittedFile(train_words_file));
         controlNetwork.learningWordsPhase(trainWordsInput.get(ConfigFile.TrainWords.WORDS.getIndex()), trainWordsInput.get(ConfigFile.TrainWords.PHONS.getIndex()));
         ContextTypoNetwork.logger.debug("Apprentissage de mots OK! ");*/
        if (USE_CONTEXT_INFORMATION) {
            // It verifies the test file exists
            if (!FileIO.fileExists(test_sentences_file)) {
                throw new FileNotExists(test_sentences_file);
            }
            HashMap<Integer, List<String>> testInput = new HashMap<>(FileIO.readSplittedFile(test_sentences_file));
            controlNetwork.decoderPhase(testInput.get(ConfigFile.TestSentences.ERROR_SENTENCE.getIndex()), testInput.get(ConfigFile.TestSentences.WORD.getIndex()), testInput.get(ConfigFile.TestSentences.ERROR_WORD.getIndex()), testInput.get(ConfigFile.TestSentences.ERROR_PHON.getIndex()));
        } else {
            // It verifies the test file exists
            if (!FileIO.fileExists(test_file)) {
                throw new FileNotExists(test_file);
            }

            HashMap<Integer, List<String>> testInput = new HashMap<>(FileIO.readSplittedFile(test_file));
            controlNetwork.decoderPhase(testInput.get(ConfigFile.TestWords.WORD.getIndex()).subList(0, 100), testInput.get(ConfigFile.TestWords.ERROR.getIndex()).subList(0, 100), testInput.get(ConfigFile.TestWords.ERROR_PHON.getIndex()).subList(0, 100));
        }

        List<String> result = new ArrayList<>();

        if (ContextTypoNetwork.RATES_PER_NETWORK && !ContextTypoNetwork.TEST_ONLY_CONTEXT_NETWORK) {

            result.add("Taux matching Reseau Triang: " + controlNetwork.getMatchingRate(NetworkControl.IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex()));
            result.add("Taux d'erreur Reseau Triang: " + controlNetwork.getErrorRate(NetworkControl.IndexNetwork.LOCAL_TRIANGULAR_NETWORK_INDEX.getIndex()));
            result.add("Taux matching Reseau Flous: " + controlNetwork.getMatchingRate(NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()));
            result.add("Taux matching Reseau Flous: " + controlNetwork.getMatchingRate(NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()));
            result.add("Taux d'erreur Reseau Flous: " + controlNetwork.getErrorRate(NetworkControl.IndexNetwork.LOCAL_FUZZY_NETWORK_INDEX.getIndex()));
        }
        result.add("Taux matching: " + controlNetwork.getMatchingRate());
        result.add("Taux erreur: " + controlNetwork.getErrorRate());
        ContextTypoNetwork.logger.error(result);
        if (USE_LOG_OUT_FILE) {
            FileIO.bufferedWrite(result, out_file);
        }
        if (seqSender != null) {
            seqSender.closeConnection();
        }

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
