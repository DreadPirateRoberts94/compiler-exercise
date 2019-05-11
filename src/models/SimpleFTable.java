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

    public void useFunction(String identifier, LinkedList<String> paramList, SimpleVTable simpleVTable){
        for (HashMap<String, List<Tuple<String, String>>> hashTable : identifiersList){
            if(hashTable.get(identifier) != null){
                for (int i = 0; i < hashTable.get(identifier).size(); i++){
                    //check if function is called with the right number of parameters
                    if (paramList.size() == hashTable.get(identifier).size()){
                        //get type of actual param
                        String actualType = simpleVTable.useVariable(paramList.get(i)).toString();
                        //if actualType is null then it wasn't declared
                        if (actualType != null){
                            //check type conformity (formal and actual params)
                            if (!hashTable.get(identifier).get(i).type.equals(actualType)){
                                System.out.println("Funzione " + identifier + " chiamata con parametro " + paramList.get(i) + " non conforme al tipaggio");
                            }
                        } else {
                            System.out.println("Funzione " + identifier + " chiamata con parametro " + paramList.get(i) + " non dichiarato");
                        }
                    } else {
                        System.out.println("Funzione " + identifier + " chiamata con numero errato di parametri");
                        return;
                    }
                }
                return;
            }
        }
        System.out.println("Funzione " + identifier + " non dichiarata.");
    }

    public void scopeExit(){
        //remove the last block from the list of hashtable
        identifiersList.remove(identifiersList.size()-1);
    }
}
