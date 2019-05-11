package models;

import com.sun.xml.internal.bind.v2.TODO;
import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleFTable {
    private List<HashMap<String, List<Tuple<String, String>>>> identifiersList = new LinkedList<>();

    public void scopeEntry(){
        identifiersList.add(new HashMap<>());
    }

    public void newFunctionDeclaration(String identifier, List<SimpleParser.ParameterContext> paramList){
        int blockNumber = identifiersList.size()-1;
        if(identifiersList.get(blockNumber).get(identifier) == null){

            List<Tuple<String, String>> tupleList = new LinkedList<>();
            for (SimpleParser.ParameterContext param : paramList){
                tupleList.add(new Tuple<>(param.type().getText(), param.ID().getText()));
            }

            identifiersList.get(blockNumber).put(identifier, tupleList);
        } else {
            System.out.println("Funzione " + identifier + " dichiarata più volte.");
        }
    }

    public String useFunction(String identifier, LinkedList<String> paramList, SimpleVTable simpleVTable){
        for (HashMap<String, List<Tuple<String, String>>> hashTable : identifiersList){
            if(hashTable.get(identifier) != null){
                if (paramList != null){

                }
                return hashTable.get(identifier).toString();
            }
        }
        System.out.println("Funzione " + identifier + " non dichiarata.");
        return "err";
    }

    public boolean checkFunction(String identifier){
        for (HashMap hashTable : identifiersList){
            if(hashTable.get(identifier) != null){
                return true;
            }
        }
        return false;
    }

    public void scopeExit(){
        //remove the last block from the list of hashtable
        identifiersList.remove(identifiersList.size()-1);
    }
}
