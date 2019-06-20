package interpreter;

import models.SimpleFTable;
import models.SimpleVTable;
import models.Utilities.Tuple;
import parser.SimpleParser;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class SimpleFTableWithLabel{
    private int labelCounter = 0;

    private List<HashMap> identifierAndLabel = new LinkedList<>();

    private List<HashMap> identifierAndVariablesDeclared = new LinkedList<>();

    public void scopeEntry() {
        identifierAndLabel.add(new HashMap());
        identifierAndVariablesDeclared.add(new HashMap());
    }

    public String newFunctionDeclaration(String identifier, List<String> variablesDeclared){
        int blockNumber = identifierAndLabel.size()-1;
        String freshLabel = getFreshLabel();
        identifierAndVariablesDeclared.get(blockNumber).put(identifier, variablesDeclared);
        identifierAndLabel.get(blockNumber).put(identifier, freshLabel);

        return freshLabel;
    }

    public String getFunctionLabel(String identifier){
        for(int i = identifierAndLabel.size()-1; i >= 0; i--){
            if(identifierAndLabel.get(i).get(identifier) != null){
                return (String) identifierAndLabel.get(i).get(identifier);
            }
        }
        return null;
    }

    public List<String> getVariablesDeclared(String identifier){
            for(int i = identifierAndVariablesDeclared.size()-1; i >= 0; i--){
                if(identifierAndVariablesDeclared.get(i) != null){
                    return (List<String>) identifierAndVariablesDeclared.get(i).get(identifier);
                }
            }
            return null;
    }

    public int getNestingLevel(String identifier){
        for(int i = identifierAndLabel.size()-1; i >= 0; i--){
            if(identifierAndLabel.get(i) != null){
                return i;
            }
        }
        return -1;
    }

    public String getFreshLabel(){
        return "flabel" + labelCounter++;
    }

    public void scopeExit() {
        identifierAndLabel.remove(identifierAndLabel.size()-1);
        identifierAndVariablesDeclared.remove(identifierAndVariablesDeclared.size()-1);
    }

}
