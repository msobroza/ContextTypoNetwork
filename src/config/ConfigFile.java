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
        
        WORDS(0), ERRORS(1), ERRORS_PHONS(2);
        private final int index;
        TestWords(int index) {
            this.index=index;
        }
        
        public int getIndex(){
            return this.index;
        }
    }
    
    public enum TrainWords {
        
        WORDS(0),PHONS(1);
        private final int index;
        TrainWords(int index) {
            this.index=index;
        }
        
        public int getIndex(){
            return this.index;
        }
    }

}
