/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.tokenizer;

/**
 *
 * @author msobroza
 */
public interface Tokenizer {
     public abstract String[] tokenize(String sentence);
     
     public abstract String[] tokenizeSimpleSplit(String sentence);
}
