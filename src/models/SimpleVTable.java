package models;


import javafx.util.Pair;
import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleVTable {

    private int uniqueIdentifier = 0;

    private int getUniqueIdentifier() { return uniqueIdentifier++; }

    public List<HashMap> identifiersList = new LinkedList<HashMap>();

    public List<HashMap> identifierAndAddress = new LinkedList<HashMap>();

    public SimpleVTable() {}

    public SimpleVTable(SimpleVTable copyInstance) {
        for (HashMap hashmap: copyInstance.identifiersList) {
            this.identifiersList.add(new HashMap(hashmap));
        }
        for (HashMap hashmap: copyInstance.identifierAndAddress) {
            this.identifierAndAddress.add(new HashMap(hashmap));
        }
    }

    public void scopeEntry(){
        identifiersList.add(new HashMap());
        identifierAndAddress.add(new HashMap());
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

        for (int i = 0; i < identifiersList.size(); i++){
            Map<String, String> map = identifiersList.get(i);
            for (Map.Entry<String, String> entry : map.entrySet()) {
                String id = entry.getKey();
                String type = entry.getValue();
                if (otherTable.getVarType(id).equals("err") || !otherTable.getVarType(id).equals(type)){
                    return false;
                }
            }
        }
        return true;
    }



    public Boolean deleteIdentifier(String identifier){
        int addressToDelete = -1;
        for(int i = identifiersList.size()-1; i >= 0; i--){
            if( identifiersList.get(i).get(identifier) != null){
                addressToDelete = (int) identifierAndAddress.get(i).get(identifier);
                break;
            }
        }
        int index = 0;
        if(addressToDelete != -1){

            for (HashMap paramToDelete: identifierAndAddress) {
                Map<String, Integer> map = new HashMap<String, Integer>(paramToDelete);

                for (Map.Entry<String, Integer> entry : map.entrySet()) {
                    String id = entry.getKey();
                    int address = entry.getValue();
                    if(address == addressToDelete){
                        paramToDelete.remove(id);
                        identifiersList.get(index).remove(id);
                    }
                }
                index++;
            }
            return true;
        }

        return false;
    }

    public void newIdentifierDeclaration(String identifier, String type){
        int blockNumber = identifiersList.size()-1;
        if(identifiersList.get(blockNumber).get(identifier) == null){
            identifiersList.get(blockNumber).put(identifier, type);
            identifierAndAddress.get(blockNumber).put(identifier, getUniqueIdentifier());
        } else {
            System.out.println("Variabile " + identifier + " dichiarata più volte.");
        }
    }

    public void createAssociationBetweenIdentifiers(String formal, String actual){

        for(int i = identifierAndAddress.size()-1; i >= 0; i--){
            System.out.println("Hashmap -> "+identifierAndAddress.get(i));
            if(identifierAndAddress.get(i).get(formal) != null){
                for(int j = identifierAndAddress.size()-1; j >=0; j--){
                    if(identifierAndAddress.get(j).get(actual) != null){
                        identifierAndAddress.get(i).put(formal, identifierAndAddress.get(j).get(actual));
                        break;
                    }
                }
            }
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
        identifierAndAddress.remove(identifierAndAddress.size()-1);
    }
}

