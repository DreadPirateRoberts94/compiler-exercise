package interpreter;

import models.SimpleFTable;
import models.SimpleVTable;
import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.LinkedList;
import java.util.List;

public class SimpleFTableWithLabel extends SimpleFTable {
    private int labelCounter = 0;

    @Override
    public void newFunctionDeclaration(String identifier, List<SimpleParser.ParameterContext> paramList, SimpleVTable simpleVTable, Boolean mustScopeBeDeclared){
        
    }

    public String getFreshLabel(){
        return "flabel" + labelCounter++;
    }
}
