package models;

import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleFTable {
    private List<HashMap<String, List<Tuple<Boolean, String, String>>>> fTable = new LinkedList<>();

    public void scopeEntry(){
        fTable.add(new HashMap<>());
    }

    public void newFunctionDeclaration(String identifier, List<SimpleParser.ParameterContext> paramList){
        int blockNumber = fTable.size()-1;
        if(!isFunDeclared(identifier)){

            List<Tuple<Boolean, String, String>> tupleList = new LinkedList<>();
            for (SimpleParser.ParameterContext param : paramList){
                tupleList.add(new Tuple<>(param.getText().startsWith("var") , param.type().getText(), param.ID().getText()));
            }

            fTable.get(blockNumber).put(identifier, tupleList);
        } else {
            System.out.println("Funzione " + identifier + " dichiarata più volte.");
        }
    }

    public void useFunction(String identifier, LinkedList<String> actualParamIdList, LinkedList<String> actualParamTypeList, SimpleVTable simpleVTable){
        if (!isFunDeclared(identifier)){
            System.out.println("Funzione " + identifier + " non dichiarata.");
        } else {
            int funIndex = getFunIndex(identifier);
            int paramIndex = 0;
            List<Tuple<Boolean, String, String>> formalParamList = fTable.get(funIndex).get(identifier);

            if (formalParamList.size() != actualParamTypeList.size()){
                System.out.println("Numero di parametri attuali non conformi");
            } else {
                for (String actualParamId : actualParamIdList){
                    if (formalParamList.get(paramIndex).var){
                        if (!simpleVTable.isVarDeclared(actualParamId)){
                            System.out.println("Parametro attuale non può essere passato per riferimento.");
                        }
                    }

                    System.out.println(formalParamList.get(paramIndex).type);
                    System.out.println(simpleVTable.getVarType(actualParamTypeList.get(paramIndex)));

                    if (!formalParamList.get(paramIndex).type.equals(simpleVTable.getVarType(actualParamTypeList.get(paramIndex)))){
                        System.out.println("Tipo del parametro attuale non conforme");
                    }
                    paramIndex++;
                }
            }
        }
    }

    public int getFunIndex(String identifier){
        int index = 0;
        for (HashMap hashTable : fTable){
            if(hashTable.get(identifier) != null){

                return index;
            }
            index++;
        }
        return -1;
    }

    public boolean isFunDeclared(String identifier){
        for (HashMap hashTable : fTable){
            if(hashTable.get(identifier) != null){
                return true;
            }
        }
        return false;
    }

    public void scopeExit(){
        //remove the last block from the list of hashtable
        fTable.remove(fTable.size()-1);
    }
}
