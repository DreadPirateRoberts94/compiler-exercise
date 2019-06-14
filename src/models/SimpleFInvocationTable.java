package models;

import models.Utilities.FunctionInfo;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleFInvocationTable {
    private List<HashMap> functionStack = new LinkedList<HashMap>();

    public void scopeEntry(){
        //remove the last block from the list of hashtable
        functionStack.add(new HashMap());
    }

    public void functionCall( String identifier, SimpleFTable simpleFTable, SimpleVTable simpleVTable, SimpleParser.BlockContext block){
        functionStack.get(functionStack.size()-1).put(identifier, new FunctionInfo(simpleFTable, simpleVTable, block));
    }

    public FunctionInfo getFunctionInfo(String identifier){
        for (HashMap hashTable : functionStack){
            if(hashTable.get(identifier) != null){
                return (FunctionInfo) hashTable.get(identifier);
            }
        }
        return null;
    }

    public void scopeExit(){
        //remove the last block from the list of hashtable
        functionStack.remove(functionStack.size()-1);
    }
}
