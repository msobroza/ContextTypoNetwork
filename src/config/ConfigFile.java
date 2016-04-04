/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package config;

/**
 *
 * @author msobroza
 */
public class ConfigFile {

    public enum TestWords {

        WORD(0), ERROR(1), ERROR_PHON(2);
        private final int index;

        TestWords(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    public enum TestSentences {

        ORIGINAL_SENTENCE(0), ERROR_SENTENCE(1), WORD(2), ERROR_WORD(3), ERROR_PHON(4);
        private final int index;

        TestSentences(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    public enum TrainWords {

        WORDS(0), PHONS(1);
        private final int index;

        TrainWords(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

    public enum TrainSentences {

        ORIGINAL_SENTENCE(0), NORMALIZED_SENTENCE(1);
        private final int index;

        TrainSentences(int index) {
            this.index = index;
        }

        public int getIndex() {
            return this.index;
        }
    }

}
