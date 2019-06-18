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

    private List<HashMap> identifierAndParams = new LinkedList<>();

    public void scopeEntry() {
        identifierAndLabel.add(new HashMap());
        identifierAndParams.add(new HashMap());
    }

    public String newFunctionDeclaration(String identifier, List<String> paramsList){
        int blockNumber = identifierAndLabel.size()-1;
        String freshLabel = getFreshLabel();
        identifierAndParams.get(blockNumber).put(identifier, paramsList);
        identifierAndLabel.get(blockNumber).put(identifier, freshLabel);

        return freshLabel;
    }

    public List<String> getParamsList(String identifier){
        for(int i = identifierAndParams.size()-1; i >= 0; i--){
            if(identifierAndParams.get(i) != null){
                return (List<String>) identifierAndParams.get(i).get(identifier);
            }
        }
        return null;
    }

    public String getFunctionLabel(String identifier){
        for(int i = identifierAndLabel.size()-1; i >= 0; i--){
            if(identifierAndLabel.get(i) != null){
                return (String) identifierAndLabel.get(i).get(identifier);
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
        identifierAndParams.remove(identifierAndParams.size()-1);
    }

}
