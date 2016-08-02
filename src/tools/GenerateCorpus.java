/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import config.ConfigFile.TestSentences;
import config.ConfigFile.TrainSentences;
import static control.ContextTypoNetwork.logger;
import static control.ContextTypoNetwork.test_sentences_file;
import static control.ContextTypoNetwork.train_sentences_file;
import control.rules.LetterInformation;
import exception.FileNotExists;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.thrift.TException;
import static tools.FileIO.bufferedWrite;
import tools.tokenizer.FrenchTokenizer;

/**
 *
 * @author msobroza
 */
public class GenerateCorpus implements LetterInformation {

    public static int NUMBER_WORDS = 200;
    public static boolean REMOVE_MOST_FREQUENT_WORDS = true;
    public static String INPUT_TRAIN_FILE = train_sentences_file;
    public static String INPUT_TEST_FILE = test_sentences_file;
    public static String OUTPUT_TRAIN_FILE = "./corpus/training/sequences/train_msr200.pickle";
    public static String OUTPUT_TEST_FILE = "./corpus/test/sequences/test_msr200.pickle";
    public static boolean GENERATE_TRAIN = true;
    public static boolean GENERATE_TEST = true;
    public static Logger logger = Logger.getRootLogger();
    public static HashMap<String, Integer> countWordsMap;

    public static void main(String[] args) throws FileNotExists, TException {
        BasicConfigurator.configure();
        logger.setLevel(Level.ALL);
        FrenchTokenizer tokenizer = new FrenchTokenizer();
        countWordsMap = new HashMap<>();

        HashMap<Integer, List<String>> trainSentencesInput = new HashMap<>(FileIO.readSplittedFile(INPUT_TRAIN_FILE));
        List<String> linesTrainSentences = trainSentencesInput.get(TrainSentences.NORMALIZED_SENTENCE.getIndex());
        for (String lineTokens : linesTrainSentences) {
            for (String word : tokenizer.tokenizeSimpleSplit(lineTokens, REGEX_CONCAT_SYMBOL)) {
                if (countWordsMap.containsKey(word)) {
                    countWordsMap.put(word, countWordsMap.get(word) + 1);
                } else {
                    countWordsMap.put(word, 1);
                }
            }
        }

        List<Integer> countSorted;
        countSorted = new ArrayList(countWordsMap.values());
        Collections.sort(countSorted, Collections.reverseOrder());
        Set<Integer> mostFrequentValues = new HashSet();
        Set<String> mostFrequentWords = new HashSet();
        for (int k = 0; k < NUMBER_WORDS; k++) {
            mostFrequentValues.add(countSorted.get(k));
        }
        for (String word : countWordsMap.keySet()) {
            if (mostFrequentValues.contains(countWordsMap.get(word))) {
                mostFrequentWords.add(word);
            }
        }
        if (REMOVE_MOST_FREQUENT_WORDS) {

            if (GENERATE_TRAIN) {

                List<String> generatedSentences = new ArrayList<>();
                List<String> originalSentences = new ArrayList<>();
                int i = 0;
                for (String lineTokens : linesTrainSentences) {
                    String generatedLine = "";
                    String originalLine = trainSentencesInput.get(TrainSentences.ORIGINAL_SENTENCE.getIndex()).get(i);
                    boolean existsWord = false;
                    for (String word : tokenizer.tokenizeSimpleSplit(lineTokens, REGEX_CONCAT_SYMBOL)) {
                        if (!mostFrequentWords.contains(word)) {
                            generatedLine = generatedLine + word + CONCAT_SYMBOL;
                            existsWord = true;
                        }
                    }
                    if (existsWord) {
                        originalSentences.add(originalLine);
                        generatedSentences.add(generatedLine);
                    }
                    if (i % 100000 == 0) {
                        System.out.println("100000 more where write in train");
                    }
                    i++;
                }
                HashMap<Integer, List<String>> trainSentencesOutput = new HashMap<>();
                trainSentencesOutput.put(TrainSentences.ORIGINAL_SENTENCE.getIndex(), originalSentences);
                trainSentencesOutput.put(TrainSentences.NORMALIZED_SENTENCE.getIndex(), generatedSentences);
                bufferedWrite(trainSentencesOutput, OUTPUT_TRAIN_FILE);

            }
            if (GENERATE_TEST) {
                HashMap<Integer, List<String>> testSentences = new HashMap<>(FileIO.readSplittedFile(INPUT_TRAIN_FILE));
                List<String> linesTestSentences = new ArrayList<>(testSentences.get(TestSentences.ERROR_SENTENCE.getIndex()));
                List<String> generatedSentences = new ArrayList<>();
                int i = 0;
                for (String lineTokens : linesTestSentences) {
                    String generatedLine = "";
                    for (String word : tokenizer.tokenizeSimpleSplit(lineTokens, REGEX_CONCAT_SYMBOL)) {
                        if (!mostFrequentWords.contains(word)) {
                            generatedLine = generatedLine + word + CONCAT_SYMBOL;
                        }
                    }
                    generatedSentences.add(generatedLine);
                    if (i % 100000 == 0) {
                        System.out.println("100000 more where write in test");
                    }
                    i++;

                }
                testSentences.put(TestSentences.ERROR_SENTENCE.getIndex(), generatedSentences);

                bufferedWrite(testSentences, OUTPUT_TEST_FILE);
            }
        }
    }

}
