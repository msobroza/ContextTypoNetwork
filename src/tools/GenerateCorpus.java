/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools;

import config.ConfigFile.TestSentences;
import config.ConfigFile.TrainSentences;
import control.ContextTypoNetwork;
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
import java.util.LinkedList;
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

    public static int NUMBER_WORDS = 50;
    public static boolean REMOVE_MOST_FREQUENT_WORDS = true;
    public static String INPUT_TRAIN_FILE = train_sentences_file;
    public static String INPUT_TEST_FILE = test_sentences_file;
    public static String OUTPUT_TRAIN_FILE = "./corpus/training/sequences/train_msr50.pickle";
    public static String OUTPUT_TEST_FILE = "./corpus/test/sequences/test_msr50.pickle";
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
        int countW=1;
        for (String word : countWordsMap.keySet()) {
            if (mostFrequentValues.contains(countWordsMap.get(word))) {
                mostFrequentWords.add(word);
                ContextTypoNetwork.logger.debug("n: "+countW+" - most frequent word: "+word);
                countW++;
            }
        }
        if (REMOVE_MOST_FREQUENT_WORDS) {

            if (GENERATE_TRAIN) {

                int i = 0;
                boolean existsWord;
                ArrayList<String> trainOriginalSentence = new ArrayList<>(trainSentencesInput.get(TrainSentences.ORIGINAL_SENTENCE.getIndex()));
                List<String> generatedSentences = new ArrayList<>(linesTrainSentences.size());
                List<String> originalSentences = new ArrayList<>(linesTrainSentences.size());
                StringBuilder newLine = new StringBuilder(20000);
                for (String lineTokens : linesTrainSentences) {
                    existsWord = false;
                    String[] splittedLine = tokenizer.tokenizeSimpleSplit(lineTokens, REGEX_CONCAT_SYMBOL);
                    newLine.setLength(0);
                    for (String word : splittedLine) {
                        if (!mostFrequentWords.contains(word)) {
                            newLine.append(word);
                            newLine.append(CONCAT_SYMBOL);
                            existsWord = true;
                        }
                    }
                    if (existsWord) {
                        originalSentences.add(trainOriginalSentence.get(i));
                        generatedSentences.add(newLine.toString());
                    }
                    if (i % 100000 == 0) { 
                        ContextTypoNetwork.logger.debug("100000 more where write in train");
                    }
                    i++;
                }
                HashMap<Integer, List<String>> trainSentencesOutput = new HashMap<>();
                trainSentencesOutput.put(TrainSentences.ORIGINAL_SENTENCE.getIndex(), originalSentences);
                trainSentencesOutput.put(TrainSentences.NORMALIZED_SENTENCE.getIndex(), generatedSentences);
                ContextTypoNetwork.logger.debug("Writing train in file...");
                bufferedWrite(trainSentencesOutput, OUTPUT_TRAIN_FILE);

            }
            if (GENERATE_TEST) {
                HashMap<Integer, List<String>> testSentences = new HashMap<>(FileIO.readSplittedFile(INPUT_TEST_FILE));
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
                        ContextTypoNetwork.logger.debug("100000 more where write in test");
                    }
                    i++;

                }
                testSentences.put(TestSentences.ERROR_SENTENCE.getIndex(), generatedSentences);
                ContextTypoNetwork.logger.debug("Writing test in file...");
                bufferedWrite(testSentences, OUTPUT_TEST_FILE);
            }
        }
    }

}
