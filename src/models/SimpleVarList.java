package models;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;


public abstract class SimpleVarList extends  SimpleExp {
    private List<SimpleVar> listOfVar = new ArrayList<>();

    public SimpleVarList(List<SimpleVar> listOfVar) { this.listOfVar = listOfVar; }


}
