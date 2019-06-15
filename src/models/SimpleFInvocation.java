package models;

import parser.SimpleParser;

import java.util.HashMap;

public class SimpleFInvocation {
    private HashMap functionStack = new HashMap();


    public void functionCall( String identifier, SimpleParser.BlockContext block){
        functionStack.put(identifier, block);
    }

    public SimpleParser.BlockContext getFunctionBlock(String identifier) {

        return (SimpleParser.BlockContext) functionStack.get(identifier);
    }


}
