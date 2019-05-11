package models;


import javafx.util.Pair;
import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleVTable {
    private List<HashMap> identifiersList = new LinkedList<HashMap>();

    public void scopeEntry(){
        identifiersList.add(new HashMap());
    }

    public void newIdentifierDeclaration(String identifier, String type){
        int blockNumber = identifiersList.size()-1;
        if(identifiersList.get(blockNumber).get(identifier) == null){
            identifiersList.get(blockNumber).put(identifier, type);
        } else {
            System.out.println("Variabile " + identifier + " dichiarata più volte.");
        }
    }

    public Object useVariable(String identifier){
        for (HashMap hashTable : identifiersList){
            if(hashTable.get(identifier) != null){
                return hashTable.get(identifier);
            }
        }
        System.out.println("Variabile " + identifier + " non dichiarata.");
        return null;
    }

    public void scopeExit(){
        //remove the last block from the list of hashtable
        identifiersList.remove(identifiersList.size()-1);
    }
}
