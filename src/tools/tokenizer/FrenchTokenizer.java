/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package tools.tokenizer;

import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.process.TokenizerFactory;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author msobroza
 */
public class FrenchTokenizer implements Tokenizer {

    private final TokenizerFactory<CoreLabel> tokenizerFactory = edu.stanford.nlp.international.french.process.FrenchTokenizer.ftbFactory();

    @Override
    public String[] tokenize(String sentence) {
        Reader r = new StringReader(sentence);
        edu.stanford.nlp.process.Tokenizer<CoreLabel> tokenizer = tokenizerFactory.getTokenizer(r);

        List<String> l = new ArrayList<>();
        while (tokenizer.hasNext()) {
            l.add(tokenizer.next().word());
        }

        String[] tok = new String[l.size()];
        int i = 0;
        for (String s : l) {
            tok[i++] = s;
        }
        return tok;
    }
    
    public static String reverseToken(String [] tokens, String concat_symbol){
        String result="";
        for(String s:tokens){
            result=result+s+concat_symbol;
        }
        return result;
    }
    
    @Override
    public String[] tokenizeSimpleSplit(String sentence, String concatSymbol){
        return sentence.split(concatSymbol);
    }
    

}
