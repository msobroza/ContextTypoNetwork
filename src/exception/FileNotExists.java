/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package exception;

/**
 *
 * @author msobroza
 */
public class FileNotExists extends Exception{
    
    public FileNotExists (String fileName){
        super("File does not exist: "+fileName);
    }
}
