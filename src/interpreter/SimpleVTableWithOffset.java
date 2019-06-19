package interpreter;


import javafx.util.Pair;
import models.SimpleVTable;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleVTableWithOffset extends SimpleVTable {

    private int offset = 1;

    private List<HashMap> identifierAndOffset = new LinkedList<>();

    @Override
    public void scopeEntry(){
        identifierAndAddress.add(new HashMap());
        identifierAndOffset.add(new HashMap());
        identifiersList.add(new HashMap());

        offset = 1;
    }


    public void varDeclaration(String id){
        int nestingLevel = identifierAndOffset.size()-1;

        identifierAndOffset.get(nestingLevel).put(id, offset);
        offset++;
    }

    public Pair getOffsetAndNestingLevel(String identifier){
        int nestingLevel = identifierAndOffset.size();
        int offset = 1;

        for(int i = identifierAndOffset.size()-1; i >= 0; i--){
            if(identifierAndOffset.get(i).get(identifier) != null){
                offset = (int) identifierAndOffset.get(i).get(identifier);
                return new Pair(offset, nestingLevel);

            }
            nestingLevel--;
        }

        return null;
    }

    @Override
    public void scopeExit(){
        //remove the last block from the list of hashtable
        identifierAndOffset.remove(identifierAndOffset.size()-1);
        identifierAndAddress.remove(identifierAndAddress.size()-1);
        identifiersList.remove(identifiersList.size()-1);
    }
}

