package models;

import javafx.util.Pair;
import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleFTable {
    //Boolean is on if the parameter is passed by var
    public List<HashMap<String, List<Tuple<Boolean, String, String>>>> fTable = new LinkedList<>();

    public SimpleFTable() {}

    public SimpleFTable(SimpleFTable copyInstance){ this.fTable = copyInstance.fTable; }

    public void scopeEntry(){
        fTable.add(new HashMap<>());
    }

    public void newFunctionDeclaration(String identifier, List<SimpleParser.ParameterContext> paramList, SimpleVTable simpleVTable, Boolean mustScopeBeDeclared){
        int blockNumber = fTable.size()-1;
        if(mustScopeBeDeclared){ simpleVTable.scopeEntry(); }

        if(!isFunDeclared(identifier)){

            List<Tuple<Boolean, String, String>> tupleList = new LinkedList<>();
            for (SimpleParser.ParameterContext param : paramList){
                tupleList.add(new Tuple<>(param.getText().startsWith("var") , param.type().getText(), param.ID().getText()));
                simpleVTable.newIdentifierDeclaration(param.ID().getText(), param.type().getText() );
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
                        if (actualParamIdList.get(paramIndex) != null && !simpleVTable.isVarDeclared(actualParamId)){
                            System.out.println("Parametro attuale " + actualParamId + " non può essere passato per riferimento.");
                        }
                    }

                    if (!formalParamList.get(paramIndex).type.equals(actualParamTypeList.get(paramIndex))){
                        System.out.println("Tipo del parametro attuale " + actualParamId + " non conforme");
                    }
                    paramIndex++;
                }
            }
        }
    }

    public boolean isNthParamVar(String identifier, int index){
        for (HashMap hashTable : fTable){
            if(hashTable.get(identifier) != null){
               List params = (List) hashTable.get(identifier);
               Tuple param = (Tuple) params.get(index);
               return (boolean) param.var;
            }
        }
        return false;
    }

    public  List<String> getFunctionFormalVarParams(String identifier){
        List<String> formalParamsPassedByVar = new LinkedList<>();
        for (HashMap hashTable : fTable){
            if(hashTable.get(identifier) != null){
                for (Tuple<Boolean, String, String> param: (List<Tuple<Boolean, String, String>>) hashTable.get(identifier)) {
                    if(param.var){
                        formalParamsPassedByVar.add(param.value);
                    } else {
                        formalParamsPassedByVar.add("");
                    }
                }
                return formalParamsPassedByVar;
            }
        }
        return formalParamsPassedByVar;
    }

    public List<Pair> getFunctionFormalParamsAndType(String identifier){
        List<Pair> formalParams = new LinkedList<>();
        for (HashMap hashTable : fTable){
            if(hashTable.get(identifier) != null){
                for (Tuple<Boolean, String, String> param: (List<Tuple<Boolean, String, String>>) hashTable.get(identifier)) {
                    formalParams.add(new Pair(param.value, param.type));
                }
                return formalParams;
            }
        }
        return formalParams;
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
