package models;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import javafx.util.Pair;

public class FunctionCallStack {

    private List<Pair> functionCall = new LinkedList<Pair>();

    public void functionCall(String identifier, List<String> actualParameters){
        functionCall.add(new Pair(identifier, actualParameters));
    }

    public List<String> getActualParamsNthFunction(int index){
        return (List<String>) functionCall.get(index).getValue();
    }

    public String getIdentifierNthFunction(int index){
        return (String) functionCall.get(index).getKey();
    }

    public int size() {return functionCall.size(); }

    public void functionExit() { functionCall.remove(functionCall.size()-1); }
}
