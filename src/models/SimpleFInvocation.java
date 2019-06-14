package models;

import models.Utilities.FunctionInfo;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleFInvocation {
    private HashMap functionStack = new HashMap();


    public void functionCall( String identifier, SimpleFTable simpleFTable, SimpleVTable simpleVTable, SimpleParser.BlockContext block){
        functionStack.put(identifier, new FunctionInfo(simpleFTable, simpleVTable, block));
    }

    public FunctionInfo getFunctionInfo(String identifier) {

        return (FunctionInfo) functionStack.get(identifier);
    }


}
