package models;

import models.behavior.BTBase;

import java.util.List;

public class SimpleStmtFunctioncall extends SimpleStmt {

    private String id;
    private List<SimpleExp> simpleExpList;

    public SimpleStmtFunctioncall(String id) {
        this.id = id;
        this.simpleExpList = simpleExpList;
    }

    @Override
    public List<SemanticError> checkSemantics(Environment e) {
        return null;
    }

    @Override
    public BTBase inferBehavior(Environment e) {
        return null;
    }
}
