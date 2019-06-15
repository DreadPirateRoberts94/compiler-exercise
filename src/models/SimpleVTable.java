package models;


import javafx.util.Pair;
import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleVTable {
    public List<HashMap> identifiersList = new LinkedList<HashMap>();

    public SimpleVTable() {}

    public SimpleVTable(SimpleVTable copyInstance) {
        for (HashMap hashmap: copyInstance.identifiersList) {
            this.identifiersList.add(new HashMap(hashmap));
        }
    }

    public void scopeEntry(){
        identifiersList.add(new HashMap());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        SimpleVTable otherTable = (SimpleVTable) o;
        return identifiersList.equals(otherTable.identifiersList);
    }



    public Boolean deleteIdentifier(String identifier){
        int identifierToDelete = -1;
        int identifierIndex = 0;

        for(HashMap hashTable : identifiersList){
            if(hashTable.get(identifier) != null){
                identifierToDelete = identifierIndex;
            }
            identifierIndex++;
        }

        if(identifierToDelete != -1){
            identifiersList.get(identifierToDelete).remove(identifier);
            return true;
        }

        return false;
    }

    public void newIdentifierDeclaration(String identifier, String type){
        int blockNumber = identifiersList.size()-1;
        if(identifiersList.get(blockNumber).get(identifier) == null){
            identifiersList.get(blockNumber).put(identifier, type);
        } else {
            System.out.println("Variabile " + identifier + " dichiarata più volte.");
        }
    }

    public String getVarType(String identifier){
        for (HashMap hashTable : identifiersList){
            if(hashTable.get(identifier) != null){
                return (String) hashTable.get(identifier);
            }
        }
        return "err";
    }

    public Boolean isVarDeclared(String identifier){
        for (HashMap hashTable : identifiersList){
            if(hashTable.get(identifier) != null){
                return true;
            }
        }
        System.out.println("Variabile " + identifier + " non dichiarata.");
        return false;
    }

    public void scopeExit(){
        //remove the last block from the list of hashtable
        identifiersList.remove(identifiersList.size()-1);
    }
}

